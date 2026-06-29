import json
import re
import time
import os
import random
from datetime import datetime
import urllib3
from datetime import datetime
from bs4 import BeautifulSoup
from typing import Dict, Optional, List, Set
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# 屏蔽 SSL 不安全警告
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


class GeekbenchFullScraper:
    """Geekbench 全量 CPU/GPU 爬虫（Cloudflare 验证绕过版）"""

    BASE_URL = "https://browser.geekbench.com"

    def __init__(
            self,
            delay: float = 2.0,
            output_dir: str = "geekbench_data",
            headless: bool = False,
            ignore_ssl: bool = True,
            no_proxy: bool = False  # 新增：是否强制直连，不走系统代理
    ):
        self.delay = delay
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

        # 配置 Chrome 选项
        options = uc.ChromeOptions()

        # ========== 核心加载优化 ==========
        # 只等待 DOM 加载完成，不等图片、样式、字体等资源，大幅提速
        options.page_load_strategy = "eager"
        # 禁用图片加载
        options.add_argument("--blink-settings=imagesEnabled=false")
        # 禁用字体加载
        options.add_argument("--disable-fonts")
        # 禁用扩展和插件
        options.add_argument("--disable-extensions")
        options.add_argument("--disable-plugins")
        # 禁用 GPU 加速，避免渲染卡顿
        options.add_argument("--disable-gpu")
        options.add_argument("--disable-software-rasterizer")

        # 基础反检测参数
        options.add_argument("--disable-blink-features=AutomationControlled")
        options.add_argument("--start-maximized")
        options.add_argument("--disable-infobars")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        # SSL/证书兼容
        if ignore_ssl:
            options.add_argument("--ignore-certificate-errors")
            options.add_argument("--ignore-ssl-errors")
            options.add_argument("--allow-running-insecure-content")

        # 强制直连，绕过公司代理（网络慢时优先开启）
        if no_proxy:
            options.add_argument("--no-proxy-server")

        if headless:
            options.add_argument("--headless=new")

        # 启动浏览器
        # 启动反检测浏览器，使用本地 Chrome + 本地驱动，跳过自动下载
        self.driver = uc.Chrome(
            options=options,
            browser_executable_path=r"C:\Program Files\Google\Chrome\Application\chrome.exe",
            # 指定你刚放好的本地驱动文件
            driver_executable_path=r"./chromedriver.exe",
            version_main=147,
            patcher_force_close=True
        )

        # ========== 延长所有超时阈值 ==========
        self.driver.set_page_load_timeout(180)  # 页面加载超时放宽到 3 分钟
        self.driver.set_script_timeout(120)  # JS 脚本执行超时
        self.wait = WebDriverWait(self.driver, 90)  # 元素等待超时放宽到 90 秒

        # 会话预热
        self._warmup_session()

    def _warmup_session(self):
        """预热会话，自动过首页 Cloudflare 验证"""
        print("正在打开首页并完成人机验证...")
        self.driver.get(self.BASE_URL)
        try:
            # 等待页面出现可识别元素，代表验证通过
            self.wait.until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "nav, .list-group, h1"))
            )
            print("验证通过，会话初始化完成")
        except Exception:
            print("等待验证超时，请手动在浏览器中完成验证")
        time.sleep(random.uniform(1, 2))

    def _get_page_soup(self, url: str, wait_selector: str = "table tbody tr") -> Optional[BeautifulSoup]:
        try:
            # 随机延迟
            time.sleep(self.delay + random.uniform(-0.5, 1.5))

            # 发起页面访问
            self.driver.get(url)

            # 先等 DOM 基础加载
            time.sleep(3)

            # 循环检测目标元素是否出现，最多等 90 秒
            max_wait = 90
            start = time.time()
            element_found = False

            while time.time() - start < max_wait:
                try:
                    # 检查目标元素是否已渲染
                    elements = self.driver.find_elements(By.CSS_SELECTOR, wait_selector)
                    if elements and len(elements) > 3:  # 至少有3行数据，说明加载成功
                        element_found = True
                        break
                except Exception:
                    pass

                # 检测是否卡在验证页
                try:
                    page_text = self.driver.find_element(By.TAG_NAME, "body").text.lower()
                    if "cloudflare" in page_text or "verify" in page_text:
                        print("检测到人机验证，请在浏览器中手动完成...")
                        time.sleep(10)
                except Exception:
                    pass

                time.sleep(2)

            if not element_found:
                print(f"警告：超时仍未加载出数据元素 {wait_selector}，将尝试解析当前页面")

            # 无论是否完全加载，都返回当前源码尝试解析
            return BeautifulSoup(self.driver.page_source, "html.parser")

        except Exception as e:
            print(f"页面加载失败 {url}: {type(e).__name__}: {e}")
            try:
                self.driver.save_screenshot(f"{self.output_dir}/error_screenshot.png")
                with open(f"{self.output_dir}/error_page.html", "w", encoding="utf-8") as f:
                    f.write(self.driver.page_source)
                print("已保存错误截图和页面源码，便于排查")
            except Exception:
                pass
            return None

    @staticmethod
    def _parse_number(text: str) -> Optional[float]:
        match = re.search(r"[\d.]+", text.replace(",", ""))
        return float(match.group()) if match else None

    @staticmethod
    def _extract_brand(name: str) -> str:
        name_lower = name.lower()
        if "intel" in name_lower:
            return "Intel"
        elif "amd" in name_lower or "ryzen" in name_lower or "athlon" in name_lower or "radeon" in name_lower:
            return "AMD"
        elif "apple" in name_lower:
            return "Apple"
        elif "nvidia" in name_lower or "geforce" in name_lower or "rtx" in name_lower:
            return "NVIDIA"
        elif "qualcomm" in name_lower or "snapdragon" in name_lower:
            return "Qualcomm"
        return "Unknown"

    # ====================== 列表页爬取 ======================

    def crawl_cpu_list(self, max_page: int = None, resume: bool = True) -> List[Dict]:
        output_file = f"{self.output_dir}/cpu_slug_list.json"
        cpu_list = []
        slug_set = set()
        start_page = 1
        max_page_limit = 200  # 最大页码兜底，防止无限循环

        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                cpu_list = json.load(f)
            slug_set = {item["slug"] for item in cpu_list}
            # 粗略估算起始页，不做严格依赖
            start_page = len(cpu_list) // 50 + 1
            print(f"列表断点续爬：已加载 {len(cpu_list)} 条，从第 {start_page} 页继续")

        page = start_page
        while True:
            if max_page and page > max_page:
                break
            if page > max_page_limit:
                print(f"已达到最大页码限制 {max_page_limit}，终止遍历")
                break

            url = f"{self.BASE_URL}/processor-benchmarks?page={page}"
            print(f"正在爬取 CPU 列表第 {page} 页")

            soup = self._get_page_soup(url, wait_selector="table tbody tr")
            if not soup:
                print(f"CPU 列表第 {page} 页加载失败，终止遍历")
                break

            rows = soup.select("table tbody tr")
            if not rows:
                print(f"CPU 列表已到末尾，共爬取 {page - 1} 页")
                break

            new_count = 0
            for row in rows:
                link = row.select_one("td:first-child a")
                if not link:
                    continue
                href = link.get("href", "").strip()
                if href.startswith("/processors/"):
                    slug = href.replace("/processors/", "")
                    name = link.get_text(strip=True)
                    if slug not in slug_set:
                        cpu_list.append({"slug": slug, "name": name})
                        slug_set.add(slug)
                        new_count += 1

            # 本页无新增数据，判定到末尾
            if new_count == 0:
                print(f"第 {page} 页无新增数据，已到达列表末尾")
                break

            # 每页落盘
            with open(output_file, "w", encoding="utf-8") as f:
                json.dump(cpu_list, f, indent=2, ensure_ascii=False)
            print(f"第 {page} 页完成，新增 {new_count} 条，累计 {len(cpu_list)} 个型号")

            page += 1

        print(f"CPU 列表收集完成，共 {len(cpu_list)} 个型号")
        return cpu_list

    def crawl_gpu_list(self, max_page: int = None, resume: bool = True) -> List[Dict]:
        output_file = f"{self.output_dir}/gpu_slug_list.json"
        gpu_list = []
        slug_set = set()
        start_page = 1
        max_page_limit = 200

        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                gpu_list = json.load(f)
            slug_set = {item["slug"] for item in gpu_list}
            start_page = len(gpu_list) // 50 + 1
            print(f"GPU列表断点续爬：已加载 {len(gpu_list)} 条，从第 {start_page} 页继续")

        page = start_page
        while True:
            if max_page and page > max_page:
                break
            if page > max_page_limit:
                print(f"已达到最大页码限制 {max_page_limit}，终止遍历")
                break

            url = f"{self.BASE_URL}/gpu-benchmarks?page={page}"
            print(f"正在爬取 GPU 列表第 {page} 页")

            soup = self._get_page_soup(url, wait_selector="table tbody tr")
            if not soup:
                print(f"GPU 列表第 {page} 页加载失败，终止遍历")
                break

            rows = soup.select("table tbody tr")
            if not rows:
                print(f"GPU 列表已到末尾，共爬取 {page - 1} 页")
                break

            new_count = 0
            for row in rows:
                link = row.select_one("td:first-child a")
                if not link:
                    continue
                href = link.get("href", "").strip()
                # gpu汇总页固定前缀 /gpus/
                if href.startswith("/gpus/"):
                    slug = href.replace("/gpus/", "")
                    name = link.get_text(strip=True)
                    if slug not in slug_set:
                        gpu_list.append({"slug": slug, "name": name})
                        slug_set.add(slug)
                        new_count += 1

            if new_count == 0:
                print(f"第 {page} 页无新增数据，已到达列表末尾")
                break

            with open(output_file, "w", encoding="utf-8") as f:
                json.dump(gpu_list, f, indent=2, ensure_ascii=False)
            print(f"第 {page} 页完成，新增 {new_count} 条，累计 {len(gpu_list)} 个显卡")

            page += 1

        print(f"GPU 列表收集完成，共 {len(gpu_list)} 个型号")
        return gpu_list

    # ====================== 详情页爬取 ======================

    def scrape_cpu_detail(self, slug: str) -> Optional[Dict]:
        """爬取单个 CPU 详情（兼容Base Power、Maximum Power、Package）"""
        url = f"{self.BASE_URL}/processors/{slug}"
        soup = self._get_page_soup(url, wait_selector="h1, table.table")
        if not soup:
            return None

        name_elem = soup.find("h1")
        name = name_elem.get_text(strip=True).replace(" Benchmarks", "") if name_elem else slug
        page_text = soup.get_text()

        specs_raw = {}
        spec_table = soup.find("table", class_="table")
        if spec_table:
            for row in spec_table.find_all("tr"):
                cells = row.find_all("td")
                if len(cells) == 2:
                    key = cells[0].get_text(strip=True)
                    value = cells[1].get_text(strip=True)
                    specs_raw[key] = value

        # 正则抓取分数
        single_core = None
        multi_core = None
        single_match = re.search(r"(\d{3,6})\s*Single-Core Score", page_text)
        multi_match = re.search(r"(\d{3,6})\s*Multi-Core Score", page_text)
        if single_match:
            single_core = int(single_match.group(1))
        if multi_match:
            multi_core = int(multi_match.group(1))

        # 核心、线程
        cores = int(self._parse_number(specs_raw.get("Cores", "0"))) if specs_raw.get("Cores") else 0
        threads = int(self._parse_number(specs_raw.get("Threads", "0"))) if specs_raw.get("Threads") else 0

        # 主频
        base_clock = 0.0
        freq_text = specs_raw.get("Frequency", "") or specs_raw.get("Base Frequency", "")
        if freq_text and "MHz" in freq_text:
            num = self._parse_number(freq_text)
            if num:
                base_clock = round(num / 1000, 3)

        boost_clock = 0.0
        boost_text = specs_raw.get("Maximum Frequency", "")
        if boost_text and "MHz" in boost_text:
            num = self._parse_number(boost_text)
            if num:
                boost_clock = round(num / 1000, 3)

        # 基础功耗 TDP / Base Power
        tdp = None
        power_keys = ["TDP", "Base Power"]
        for pk in power_keys:
            if pk in specs_raw and "W" in specs_raw[pk]:
                num = self._parse_number(specs_raw[pk])
                if num:
                    tdp = int(num)
                    break

        # 最大功耗 Maximum Power
        max_power = None
        max_power_text = specs_raw.get("Maximum Power", "")
        if max_power_text and "W" in max_power_text:
            num = self._parse_number(max_power_text)
            if num:
                max_power = int(num)

        # 架构、插槽
        architecture = specs_raw.get("Codename", specs_raw.get("Architecture", "Unknown"))
        package = specs_raw.get("Package", "")

        # 缓存、制程兜底正则（仅老CPU页面存在）
        l1_cache = ""
        l2_cache = ""
        l3_cache = ""
        lithography = ""
        l1_match = re.search(r"L1 Cache[:：]\s*([\d\.]+ [KMG]B)", page_text)
        l2_match = re.search(r"L2 Cache[:：]\s*([\d\.]+ [KMG]B)", page_text)
        l3_match = re.search(r"L3 Cache[:：]\s*([\d\.]+ [KMG]B)", page_text)
        lit_match = re.search(r"Lithography[:：]\s*([\d\.]+ nm)", page_text)
        if l1_match: l1_cache = l1_match.group(1)
        if l2_match: l2_cache = l2_match.group(1)
        if l3_match: l3_cache = l3_match.group(1)
        if lit_match: lithography = lit_match.group(1)

        specs_dict = {
            "cores": cores,
            "threads": threads,
            "baseClockGHz": base_clock,
            "boostClockGHz": boost_clock,
            "tdpWatts": tdp,
            "maxPowerW": max_power,
            "lithography": lithography,
            "package": package,
            "family": specs_raw.get("Family", ""),
            "model": specs_raw.get("Model", ""),
            "stepping": specs_raw.get("Stepping", ""),
            "l1_cache": l1_cache,
            "l2_cache": l2_cache,
            "l3_cache": l3_cache,
            "instruction_sets": specs_raw.get("Instruction Sets", ""),
            "official_link": specs_raw.get("Official Website", "")
        }

        return {
            "name": name,
            "brand": self._extract_brand(name),
            "category": "pc_cpu",
            "overall_score": multi_core if multi_core else 0,
            "architecture": architecture,
            "specs": json.dumps(specs_dict),
            "benchmarks": [
                {"source": "Geekbench", "metric": "single_core", "score": single_core, "unit": "points"},
                {"source": "Geekbench", "metric": "multi_core", "score": multi_core, "unit": "points"}
            ],
            "launch_date": datetime.now().isoformat(),
            "data_source": "Geekbench Browser",
            "detail_url": url
        }

    def scrape_gpu_detail(self, slug: str) -> Optional[Dict]:
        url = f"{self.BASE_URL}/gpus/{slug}"
        soup = self._get_page_soup(url, wait_selector="h1, table.table")
        if not soup:
            return None

        name_elem = soup.find("h1")
        name = name_elem.get_text(strip=True).replace(" Benchmarks", "") if name_elem else slug
        page_text = soup.get_text()

        specs_raw = {}
        spec_table = soup.find("table", class_="table")
        if spec_table:
            for row in spec_table.find_all("tr"):
                cells = row.find_all("td")
                if len(cells) == 2:
                    k = cells[0].get_text(strip=True)
                    v = cells[1].get_text(strip=True)
                    specs_raw[k] = v

        # 抓取三类分数 opencl / vulkan / metal
        opencl_score = None
        vulkan_score = None
        metal_score = None
        opencl_match = re.search(r"(\d{5,8})\s*OpenCL Score", page_text)
        vulkan_match = re.search(r"(\d{5,8})\s*Vulkan Score", page_text)
        metal_match = re.search(r"(\d{3,8})\s*Metal Score", page_text)
        if opencl_match: opencl_score = int(opencl_match.group(1))
        if vulkan_match: vulkan_score = int(vulkan_match.group(1))
        if metal_match and metal_match.group(1).lower() != "n/a":
            metal_score = int(metal_match.group(1))

        benchmarks = []
        if opencl_score is not None:
            benchmarks.append({"source": "Geekbench", "metric": "opencl", "score": opencl_score, "unit": "points"})
        if vulkan_score is not None:
            benchmarks.append({"source": "Geekbench", "metric": "vulkan", "score": vulkan_score, "unit": "points"})
        if metal_score is not None:
            benchmarks.append({"source": "Geekbench", "metric": "metal", "score": metal_score, "unit": "points"})

        overall_score = max([b["score"] for b in benchmarks]) if benchmarks else 0

        # 提取所有硬件参数
        architecture = specs_raw.get("Architecture", "")
        codename = specs_raw.get("Codename", "")
        process_tech = specs_raw.get("Process", "")
        memory_total = specs_raw.get("Memory", "")
        memory_width = specs_raw.get("Memory Width", "")
        memory_clock = specs_raw.get("Memory Clock", "")
        mem_bandwidth = specs_raw.get("Memory Bandwidth", "")
        tdp_val = None
        tdp_text = specs_raw.get("TDP", "")
        if tdp_text and "W" in tdp_text:
            tdp_val = int(self._parse_number(tdp_text))
        bus_interface = specs_raw.get("Bus Interface", "")
        manufacturer = specs_raw.get("Manufacturer", "")

        specs_dict = {
            "architecture": architecture,
            "codename": codename,
            "process": process_tech,
            "memory": memory_total,
            "memory_width_bits": memory_width,
            "memory_clock": memory_clock,
            "memory_bandwidth": mem_bandwidth,
            "tdpWatts": tdp_val,
            "bus_interface": bus_interface,
            "manufacturer": manufacturer
        }

        return {
            "name": name,
            "brand": self._extract_brand(name),
            "category": "pc_gpu",
            "overall_score": overall_score,
            "architecture": architecture,
            "specs": json.dumps(specs_dict, ensure_ascii=False),
            "benchmarks": benchmarks,
            "launch_date": datetime.now().isoformat(),
            "data_source": "Geekbench Browser",
            "detail_url": url
        }

    # ====================== 批量爬取 + 断点续爬 ======================

    def batch_scrape_cpus(self, cpu_list: List[Dict], resume: bool = True) -> List[Dict]:
        output_file = f"{self.output_dir}/all_cpus.json"
        scraped_slugs: Set[str] = set()

        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                existing_data = json.load(f)
            scraped_slugs = {item["detail_url"].split("/")[-1] for item in existing_data}
            results = existing_data
            print(f"断点续爬：已加载 {len(results)} 条已爬取数据")
        else:
            results = []

        total = len(cpu_list)
        for idx, item in enumerate(cpu_list, 1):
            slug = item["slug"]
            if slug in scraped_slugs:
                continue

            print(f"[{idx}/{total}] 正在爬取 CPU: {item['name']}")
            detail = self.scrape_cpu_detail(slug)
            if detail:
                results.append(detail)
                scraped_slugs.add(slug)

                if idx % 10 == 0:
                    with open(output_file, "w", encoding="utf-8") as f:
                        json.dump(results, f, indent=2, ensure_ascii=False)
                    print(f"已保存 {len(results)} 条 CPU 数据")

        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print(f"CPU 批量爬取完成，共 {len(results)} 条数据，已保存到 {output_file}")
        return results

    def batch_scrape_gpus(self, gpu_list: List[Dict], resume: bool = True) -> List[Dict]:
        output_file = f"{self.output_dir}/all_gpus.json"
        scraped_slugs: Set[str] = set()

        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                existing_data = json.load(f)
            scraped_slugs = {item["detail_url"].split("/")[-1] for item in existing_data}
            results = existing_data
            print(f"断点续爬：已加载 {len(results)} 条已爬取数据")
        else:
            results = []

        total = len(gpu_list)
        for idx, item in enumerate(gpu_list, 1):
            slug = item["slug"]
            if slug in scraped_slugs:
                continue

            print(f"[{idx}/{total}] 正在爬取 GPU: {item['name']}")
            detail = self.scrape_gpu_detail(slug)
            if detail:
                results.append(detail)
                scraped_slugs.add(slug)

                if idx % 10 == 0:
                    with open(output_file, "w", encoding="utf-8") as f:
                        json.dump(results, f, indent=2, ensure_ascii=False)
                    print(f"已保存 {len(results)} 条 GPU 数据")

        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print(f"GPU 批量爬取完成，共 {len(results)} 条数据，已保存到 {output_file}")
        return results

    def close(self):
        """安全关闭浏览器，避免句柄报错"""
        try:
            if hasattr(self, 'driver') and self.driver is not None:
                try:
                    self.driver.quit()
                except Exception:
                    pass
                self.driver = None
        except Exception:
            pass


def main():
    scraper = None
    try:
        scraper = GeekbenchFullScraper(
            delay=2.5,
            headless=False,
            ignore_ssl=True,
            no_proxy=True
        )

        # 直接读取本地已有的CPU清单，跳过列表爬取
        with open("geekbench_data/cpu_slug_list.json", "r", encoding="utf-8") as f:
            cpu_list = json.load(f)
        print(f"读取到本地CPU清单共 {len(cpu_list)} 条，开始详情爬取")

        scraper.batch_scrape_cpus(cpu_list, resume=True)
        print("✅ CPU详情爬取完成")

    finally:
        if scraper:
            scraper.close()


if __name__ == "__main__":
    main()
import json
import re
import time
import os
import random
from datetime import datetime
import urllib3
from bs4 import BeautifulSoup
from typing import Dict, Optional, List, Set
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

class AnTuTuScraper:
    BASE_URL = "https://www.antutu.com"

    def __init__(
            self,
            delay: float = 3.5,
            output_dir: str = "antutu_data",
            headless: bool = False,
            ignore_ssl: bool = True,
            no_proxy: bool = False
    ):
        self.delay = delay
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

        options = uc.ChromeOptions()
        options.page_load_strategy = "normal"
        options.add_argument("--blink-settings=imagesEnabled=false")
        options.add_argument("--disable-blink-features=AutomationControlled")
        options.add_argument("--start-maximized")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        if ignore_ssl:
            options.add_argument("--ignore-certificate-errors")
            options.add_argument("--ignore-ssl-errors")
            options.add_argument("--allow-running-insecure-content")
        if no_proxy:
            options.add_argument("--no-proxy-server")
        if headless:
            options.add_argument("--headless=new")

        self.driver = uc.Chrome(
            options=options,
            browser_executable_path=r"C:\Program Files\Google\Chrome\Application\chrome.exe",
            driver_executable_path=r"./chromedriver.exe",
            version_main=147,
            patcher_force_close=True
        )

        self.driver.set_page_load_timeout(180)
        self.driver.set_script_timeout(120)
        self.wait_long = WebDriverWait(self.driver, 90)
        self.wait_short = WebDriverWait(self.driver, 10)
        print("浏览器初始化完成，跳过首页预热")

    def _check_cloudflare(self):
        """检测人机验证并等待"""
        for _ in range(20):
            text = self.driver.page_source.lower()
            if "just a moment" not in text and "cloudflare" not in text:
                break
            print("检测Cloudflare验证，等待10s...")
            time.sleep(10)

    def _get_page_soup(self, url: str, wait_selector: str = "li,tr") -> Optional[BeautifulSoup]:
        try:
            time.sleep(self.delay + random.uniform(-1.2, 1.2))
            self.driver.get(url)
            self._check_cloudflare()

            loaded = False
            start = time.time()
            max_wait = 60
            while time.time() - start < max_wait:
                eles = self.driver.find_elements(By.CSS_SELECTOR, wait_selector)
                if len(eles) > 2:
                    loaded = True
                    break
                time.sleep(2)
            if not loaded:
                print(f"警告 {url} 未加载目标元素，保存调试页面")
                with open(f"{self.output_dir}/debug_page.html", "w", encoding="utf-8") as f:
                    f.write(self.driver.page_source)
            return BeautifulSoup(self.driver.page_source, "html.parser")
        except Exception as e:
            print(f"页面访问失败 {url} : {str(e)}")
            return None

    @staticmethod
    def _parse_number(text: str) -> Optional[int]:
        match = re.search(r"[\d,]+", text)
        if not match:
            return None
        return int(match.group().replace(",", ""))

    @staticmethod
    def _extract_brand(name: str) -> str:
        name_lower = name.lower()
        if "qualcomm" in name_lower or "骁龙" in name_lower:
            return "Qualcomm"
        elif "mediatek" in name_lower or "天玑" in name_lower or "mtk" in name_lower:
            return "MediaTek"
        elif "exynos" in name_lower or "三星" in name_lower:
            return "Samsung"
        elif "apple" in name_lower or "a" in name_lower and "series" in name_lower:
            return "Apple"
        elif "nvidia" in name_lower or "rtx" in name_lower or "geforce" in name_lower:
            return "NVIDIA"
        elif "amd" in name_lower or "radeon" in name_lower or "rx" in name_lower:
            return "AMD"
        return "Unknown"

    # ===================== 1、手机整机榜单（原逻辑保留） =====================
    def crawl_phone_list(self, max_page: int = None, resume: bool = True) -> List[Dict]:
        output_file = f"{self.output_dir}/antutu_phone_slug_list.json"
        phone_list = []
        slug_set = set()
        start_page = 1
        max_page_limit = 150

        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                phone_list = json.load(f)
            slug_set = {item["slug"] for item in phone_list}
            start_page = len(phone_list) // 40 + 1
            print(f"手机列表续爬，已有{len(phone_list)}台")

        page = start_page
        while True:
            if max_page and page > max_page: break
            if page > max_page_limit: break
            url = f"{self.BASE_URL}/ranking/rank1.htm?page={page}"
            print(f"爬手机榜单第{page}页")
            soup = self._get_page_soup(url, "table tbody tr")
            if not soup: break
            rows = soup.select("table tbody tr")
            if not rows: break
            new_add = 0
            for row in rows:
                link = row.select_one("td:nth-child(2) a")
                if not link: continue
                href = link.get("href", "")
                if "/device/" not in href: continue
                slug = href.strip("/device/").strip("/")
                name = link.get_text(strip=True)
                if slug not in slug_set:
                    phone_list.append({"slug": slug, "name": name})
                    slug_set.add(slug)
                    new_add += 1
            if new_add == 0: break
            with open(output_file, "w", encoding="utf-8") as f:
                json.dump(phone_list, f, indent=2, ensure_ascii=False)
            page += 1
        print(f"手机列表完成，共{len(phone_list)}台")
        return phone_list

    def scrape_phone_detail(self, slug: str) -> Optional[Dict]:
        url = f"{self.BASE_URL}/device/{slug}"
        soup = self._get_page_soup(url, "h1")
        if not soup: return None
        name_tag = soup.find("h1")
        phone_name = name_tag.get_text(strip=True) if name_tag else slug
        page_text = soup.get_text()
        info_dict = {}
        info_table = soup.find("table", class_="info-table")
        if info_table:
            for tr in info_table.find_all("tr"):
                tds = tr.find_all("td")
                if len(tds) == 2:
                    info_dict[tds[0].get_text(strip=True)] = tds[1].get_text(strip=True)
        total_score = self._parse_number(re.search(r"总分[:：]\s*([\d,]+)", page_text).group(1)) if re.search(r"总分[:：]\s*([\d,]+)", page_text) else 0
        cpu_score = self._parse_number(re.search(r"CPU[:：]\s*([\d,]+)", page_text).group(1)) if re.search(r"CPU[:：]\s*([\d,]+)", page_text) else None
        gpu_score = self._parse_number(re.search(r"GPU[:：]\s*([\d,]+)", page_text).group(1)) if re.search(r"GPU[:：]\s*([\d,]+)", page_text) else None
        mem_score = self._parse_number(re.search(r"内存[:：]\s*([\d,]+)", page_text).group(1)) if re.search(r"内存[:：]\s*([\d,]+)", page_text) else None
        ux_score = self._parse_number(re.search(r"UX[:：]\s*([\d,]+)", page_text).group(1)) if re.search(r"UX[:：]\s*([\d,]+)", page_text) else None
        benchmarks = []
        if cpu_score: benchmarks.append({"source":"AnTuTu","metric":"cpu","score":cpu_score,"unit":"points"})
        if gpu_score: benchmarks.append({"source":"AnTuTu","metric":"gpu","score":gpu_score,"unit":"points"})
        if mem_score: benchmarks.append({"source":"AnTuTu","metric":"mem","score":mem_score,"unit":"points"})
        if ux_score: benchmarks.append({"source":"AnTuTu","metric":"ux","score":ux_score,"unit":"points"})
        soc = info_dict.get("处理器", "")
        specs_dict = {
            "soc": soc, "ram_storage": info_dict.get("内存/存储", ""),
            "release_date": info_dict.get("发布时间", ""),
            "manufacturer": info_dict.get("厂商", self._extract_brand(phone_name)),
            "process": "", "base_clock_ghz": "", "max_clock_ghz": "",
            "tdp_watts": "", "cache_info": ""
        }
        return {
            "name": phone_name, "brand": self._extract_brand(phone_name),
            "category": "android_phone", "overall_score": total_score,
            "architecture": soc, "specs": json.dumps(specs_dict, ensure_ascii=False),
            "benchmarks": benchmarks, "launch_date": datetime.now().isoformat(),
            "data_source": "AnTuTu Official", "detail_url": url
        }

    def batch_scrape_phones(self, phone_list: List[Dict], resume: bool = True) -> List[Dict]:
        output_file = f"{self.output_dir}/all_antutu_phone.json"
        scraped_slugs = set()
        results = []
        if resume and os.path.exists(output_file):
            try:
                with open(output_file, "r", encoding="utf-8") as f:
                    cnt = f.read().strip()
                    if cnt:
                        results = json.loads(cnt)
                        scraped_slugs = {i["detail_url"].split("/")[-1] for i in results}
                        print(f"加载历史手机{len(results)}条")
            except json.JSONDecodeError:
                print("手机JSON损坏，重新爬")
        total = len(phone_list)
        for idx, item in enumerate(phone_list, 1):
            slug = item["slug"]
            if slug in scraped_slugs: continue
            print(f"[{idx}/{total}] 爬取 {item['name']}")
            data = self.scrape_phone_detail(slug)
            if data:
                results.append(data)
                scraped_slugs.add(slug)
                if idx % 10:
                    with open(output_file, "w", encoding="utf-8") as f:
                        json.dump(results, f, indent=2, ensure_ascii=False)
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print(f"手机爬取完成，共{len(results)}条")
        return results

    # ===================== 2、新增：手机SoC天梯榜 rank301 =====================
    def crawl_mobile_soc_list(self, resume: bool = True) -> List[Dict]:
        """爬取https://www.antutu.com/ranking/rank301.htm 全页SoC"""
        output_file = f"{self.output_dir}/antutu_mobile_soc_list.json"
        soc_list = []
        seen_names = set()
        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                soc_list = json.load(f)
            seen_names = {i["name"] for i in soc_list}
            print(f"SoC列表续爬，已有{len(soc_list)}款芯片")
        url = f"{self.BASE_URL}/ranking/rank301.htm"
        print("开始爬取手机SoC天梯榜 rank301")
        soup = self._get_page_soup(url, "table tbody tr")
        if not soup:
            print("SoC榜单页面加载失败")
            return soc_list
        # 新版安兔兔榜单是表格行
        rows = soup.select("table tbody tr")
        for row in rows:
            tds = row.select("td")
            if len(tds) < 3:
                continue
            # 第二列：芯片名称+架构
            name_text = tds[1].get_text(strip=True)
            # 第三列：分数
            score_text = tds[2].get_text(strip=True)
            score = self._parse_number(score_text)
            if score is None:
                continue
            # 分离名称和括号内架构
            arch = ""
            if "(" in name_text and ")" in name_text:
                name_part = name_text.split("(")[0].strip()
                arch_part = name_text.split("(")[-1].replace(")", "").strip()
                soc_name = name_part
                arch = arch_part
            else:
                soc_name = name_text
            if soc_name in seen_names:
                continue
            soc_list.append({
                "name": soc_name,
                "architecture_text": arch,
                "total_score": int(score)
            })
            seen_names.add(soc_name)
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(soc_list, indent=2, fp=f, ensure_ascii=False)
        print(f"SoC天梯榜爬取完成，共{len(soc_list)}款芯片")
        return soc_list

    def parse_all_mobile_soc(self, soc_list: List[Dict]) -> List[Dict]:
        """将SoC列表转为统一JSON结构，和Geekbench对齐"""
        out = []
        for item in soc_list:
            name = item["name"]
            arch = item["architecture_text"]
            score = item["total_score"]
            specs_dict = {
                "cores_arch_desc": arch,
                "lithography": "", "tdpWatts": None,
                "l1_cache": "", "l2_cache": "", "l3_cache": "",
                "baseClockGHz": "", "boostClockGHz": ""
            }
            out.append({
                "name": name,
                "brand": self._extract_brand(name),
                "category": "mobile_soc",
                "overall_score": score,
                "architecture": arch,
                "specs": json.dumps(specs_dict, ensure_ascii=False),
                "benchmarks": [{"source":"AnTuTu","metric":"soc_cpu_gpu_total","score":score,"unit":"points"}],
                "launch_date": datetime.now().isoformat(),
                "data_source": "AnTuTu rank301 SoC天梯榜",
                "detail_url": f"{self.BASE_URL}/ranking/rank301.htm"
            })
        output_file = f"{self.output_dir}/all_antutu_mobile_soc.json"
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(out, indent=2, fp=f, ensure_ascii=False)
        print(f"手机SoC完整数据已保存至 {output_file}，共{len(out)}条")
        return out

    # ===================== 3、新增：PC GPU AI榜单 pc201 =====================
    def crawl_pc_gpu_ai_list(self, resume: bool = True) -> List[Dict]:
        """爬取https://www.antutu.com/ranking/pc201.htm AI显卡榜单"""
        output_file = f"{self.output_dir}/antutu_pc_gpu_ai_list.json"
        gpu_list = []
        seen_names = set()
        if resume and os.path.exists(output_file):
            with open(output_file, "r", encoding="utf-8") as f:
                gpu_list = json.load(f)
            seen_names = {i["gpu_name"] for i in gpu_list}
            print(f"PC GPU AI列表续爬，已有{len(gpu_list)}款")
        url = f"{self.BASE_URL}/ranking/pc201.htm"
        print("开始爬取PC GPU AI性能榜 pc201")
        soup = self._get_page_soup(url, "table tbody tr")
        if not soup:
            print("PC GPU AI榜单加载失败")
            return gpu_list
        # 表格行解析
        rows = soup.select("table tbody tr")
        for row in rows:
            tds = row.select("td")
            if len(tds) < 3:
                continue
            gpu_name = tds[1].get_text(strip=True)
            score_text = tds[2].get_text(strip=True)
            ai_score = self._parse_number(score_text)
            if ai_score is None:
                continue
            if gpu_name in seen_names:
                continue
            gpu_list.append({"gpu_name": gpu_name, "ai_total_score": int(ai_score)})
            seen_names.add(gpu_name)
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(gpu_list, indent=2, fp=f, ensure_ascii=False)
        print(f"PC GPU AI榜单爬取完成，共{len(gpu_list)}款显卡")
        return gpu_list

    def parse_all_pc_gpu_ai(self, gpu_list: List[Dict]) -> List[Dict]:
        """转为统一JSON，作为Geekbench AI分数补充"""
        out = []
        for item in gpu_list:
            gpu_name = item["gpu_name"]
            ai_score = item["ai_total_score"]
            specs_dict = {
                "memory": "", "memory_width_bits": "", "tdpWatts": None,
                "architecture": "", "codename": "", "process": ""
            }
            out.append({
                "name": gpu_name,
                "brand": self._extract_brand(gpu_name),
                "category": "pc_gpu_ai",
                "overall_score": ai_score,
                "architecture": "",
                "specs": json.dumps(specs_dict, ensure_ascii=False),
                "benchmarks": [{"source":"AnTuTu","metric":"ai_large_model","score":ai_score,"unit":"points"}],
                "launch_date": datetime.now().isoformat(),
                "data_source": "AnTuTu pc201 PC GPU AI榜",
                "detail_url": f"{self.BASE_URL}/ranking/pc201.htm"
            })
        output_file = f"{self.output_dir}/all_antutu_pc_gpu_ai.json"
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(out, indent=2, fp=f, ensure_ascii=False)
        print(f"PC GPU AI完整数据保存至 {output_file}，共{len(out)}条")
        return out

    def close(self):
        try:
            if self.driver:
                try: self.driver.quit()
                except Exception: pass
                self.driver = None
        except Exception: pass

def main():
    scraper = None
    try:
        scraper = AnTuTuScraper(delay=3.5, headless=False, no_proxy=True)
        # 1、手机整机（原有逻辑，按需开启）
        # phone_list = scraper.crawl_phone_list()
        # scraper.batch_scrape_phones(phone_list)

        # 2、爬手机SoC天梯榜 rank301
        soc_raw = scraper.crawl_mobile_soc_list(resume=True)
        scraper.parse_all_mobile_soc(soc_raw)

        # 3、爬PC GPU AI榜 pc201
        gpu_raw = scraper.crawl_pc_gpu_ai_list(resume=True)
        scraper.parse_all_pc_gpu_ai(gpu_raw)

        print("✅ SoC + PC GPU AI 榜单全部采集完成，可作为Geekbench补充")
    finally:
        if scraper:
            scraper.close()

if __name__ == "__main__":
    main()
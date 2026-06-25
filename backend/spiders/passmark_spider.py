"""
PassMark 数据爬虫
从 PassMark 抓取 CPU/GPU 性能数据
"""

import scrapy
from .items import HardwareItem, BenchmarkItem
import json
from datetime import datetime


class PassMarkSpider(scrapy.Spider):
    name = "passmark"
    allowed_domains = ["www.cpubenchmark.net", "www.videocardbenchmark.net"]
    start_urls = [
        "https://www.cpubenchmark.net/cpu_list.php",
    ]

    custom_settings = {
        'CONCURRENT_REQUESTS': 1,
        'DOWNLOAD_DELAY': 2,
        'USER_AGENT': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }

    def parse(self, response):
        """解析 CPU 列表页面"""
        # 解析 CPU 列表
        for row in response.css("tbody tr"):
            cells = row.css("td")
            if len(cells) < 10:
                continue

            # 提取排名和名称
            rank_cell = cells[0]
            name_cell = cells[1]

            rank = rank_cell.css("::text").get()
            name = name_cell.css("a::text").get()

            if not name:
                continue

            # 提取分数
            score = cells[2].css("::text").get()
            if not score:
                continue

            # 提取详情页链接
            detail_link = name_cell.css("a::attr(href)").get()
            if detail_link:
                detail_url = response.urljoin(detail_link)
                yield response.follow(
                    detail_url,
                    callback=self.parse_cpu_detail,
                    cb_kwargs={
                        "name": name.strip(),
                        "score": score.strip(),
                    }
                )

        # GPU 页面
        gpu_url = "https://www.videocardbenchmark.net/gpu_list.php"
        yield response.follow(gpu_url, callback=self.parse_gpu_list)

    def parse_gpu_list(self, response):
        """解析 GPU 列表页面"""
        for row in response.css("tbody tr"):
            cells = row.css("td")
            if len(cells) < 10:
                continue

            name_cell = cells[1]
            name = name_cell.css("a::text").get()
            score = cells[2].css("::text").get()

            if not name or not score:
                continue

            detail_link = name_cell.css("a::attr(href)").get()
            if detail_link:
                detail_url = response.urljoin(detail_link)
                yield response.follow(
                    detail_url,
                    callback=self.parse_gpu_detail,
                    cb_kwargs={
                        "name": name.strip(),
                        "score": score.strip(),
                    }
                )

    def parse_cpu_detail(self, response, name, score):
        """解析 CPU 详情页"""
        item = HardwareItem()

        item["name"] = name
        item["brand"] = self._extract_brand(name)
        item["category"] = "pc_cpu"
        item["overall_score"] = float(score.replace(",", ""))

        # 提取规格信息
        specs = {}
        specs["cores"] = self._extract_int(response, "CPU Cores")
        specs["threads"] = self._extract_int(response, "Threads")
        specs["tdpWatts"] = self._extract_int(response, "TDP (Power)")
        specs["lithography"] = self._extract_text(response, "Lithography")

        # 频率信息
        clock_speed = self._extract_text(response, "Clock Speed")
        if clock_speed:
            try:
                specs["baseClockGHz"] = float(clock_speed.split("@")[1].replace("GHz", "").strip())
            except (IndexError, ValueError):
                pass

        item["specs"] = json.dumps(specs) if specs else None

        # 提取跑分数据
        benchmarks = [
            BenchmarkItem(
                source="PassMark",
                metric="multi_core",
                score=float(score.replace(",", "")),
                unit="points"
            )
        ]

        # 提取其他分数
        single_thread = self._extract_score(response, "CPU Mark (Single Thread)")
        if single_thread:
            benchmarks.append(BenchmarkItem(
                source="PassMark",
                metric="single_core",
                score=single_thread,
                unit="points"
            ))

        item["benchmarks"] = [dict(b) for b in benchmarks]

        item["architecture"] = self._extract_text(response, "Socket")
        item["launch_date"] = datetime.now().isoformat()
        item["data_source"] = "PassMark"

        yield item

    def parse_gpu_detail(self, response, name, score):
        """解析 GPU 详情页"""
        item = HardwareItem()

        item["name"] = name
        item["brand"] = self._extract_brand(name)
        item["category"] = "pc_gpu"
        item["overall_score"] = float(score.replace(",", ""))

        # 提取规格信息
        specs = {}
        specs["vramGB"] = self._extract_int(response, "Video Memory")
        specs["memoryType"] = self._extract_text(response, "Memory Type")

        item["specs"] = json.dumps(specs) if specs else None

        # 提取跑分数据
        benchmarks = [
            BenchmarkItem(
                source="PassMark",
                metric="gpu",
                score=float(score.replace(",", "")),
                unit="points"
            )
        ]

        item["benchmarks"] = [dict(b) for b in benchmarks]

        item["architecture"] = self._extract_text(response, "Architecture")
        item["launch_date"] = datetime.now().isoformat()
        item["data_source"] = "PassMark"

        yield item

    def _extract_brand(self, name):
        """从名称中提取品牌"""
        brands = ["Intel", "AMD", "Apple", "NVIDIA", "AMD"]
        for brand in brands:
            if brand.lower() in name.lower():
                return brand
        return "Unknown"

    def _extract_int(self, response, label):
        """提取整数值"""
        # PassMark 的页面结构可能不同，需要根据实际情况调整
        text = response.css(f"td:contains('{label}') + td::text").get()
        if text:
            try:
                return int(text.replace(",", ""))
            except ValueError:
                return None
        return None

    def _extract_text(self, response, label):
        """提取文本"""
        text = response.css(f"td:contains('{label}') + td::text").get()
        return text.strip() if text else None

    def _extract_score(self, response, label):
        """提取分数"""
        text = response.css(f"td:contains('{label}') + td::text").get()
        if text:
            try:
                return float(text.replace(",", ""))
            except ValueError:
                return None
        return None

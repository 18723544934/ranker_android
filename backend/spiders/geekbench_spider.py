"""
Geekbench 数据爬虫
从 Geekbench Browser 抓取 CPU/GPU 性能数据
"""

import scrapy
from scrapy.http import FormRequest
from .items import HardwareItem, BenchmarkItem
import json
from datetime import datetime


class GeekbenchSpider(scrapy.Spider):
    name = "geekbench"
    allowed_domains = ["browser.geekbench.com"]
    start_urls = [
        "https://browser.geekbench.com/processor-benchmarks.html",
    ]

    def parse(self, response):
        """解析处理器排行榜页面"""
        # CPU 处理器
        yield from self.parse_processor_list(response, "cpu")

        # GPU 处理器
        gpu_url = "https://browser.geekbench.com/vulkan-benchmarks.html"
        yield response.follow(gpu_url, callback=self.parse_processor_list, cb_kwargs={"category": "gpu"})

    def parse_processor_list(self, response, category="cpu"):
        """解析处理器列表"""
        for row in response.css("tr"):
            cells = row.css("td")
            if len(cells) < 6:
                continue

            # 提取排名和名称
            rank = cells[0].css("::text").get()
            name = cells[1].css("a ::text").get()
            score = cells[2].css("::text").get()

            if not name or not score:
                continue

            # 提取详情页链接
            detail_link = cells[1].css("a::attr(href)").get()
            if detail_link:
                detail_url = response.urljoin(detail_link)
                yield response.follow(
                    detail_url,
                    callback=self.parse_processor_detail,
                    cb_kwargs={
                        "name": name.strip(),
                        "score": score.strip(),
                        "category": category
                    }
                )

        # 分页
        next_page = response.css("a.next ::attr(href)").get()
        if next_page:
            yield response.follow(next_page, callback=self.parse_processor_list, cb_kwargs={"category": category})

    def parse_processor_detail(self, response, name, score, category):
        """解析处理器详情页"""
        item = HardwareItem()

        item["name"] = name
        item["brand"] = self._extract_brand(name)
        item["category"] = "pc_cpu" if category == "cpu" else "pc_gpu"
        item["overall_score"] = float(score.replace(",", ""))

        # 提取规格信息
        specs = {}
        if category == "cpu":
            specs["cores"] = self._extract_int(response, "Cores")
            specs["threads"] = self._extract_int(response, "Threads")
            specs["baseClockGHz"] = self._extract_float(response, "Base Frequency")
            specs["boostClockGHz"] = self._extract_float(response, "Turbo Frequency")
            specs["tdpWatts"] = self._extract_int(response, "TDP")
            specs["lithography"] = self._extract_text(response, " lithography")
        else:
            specs["vramGB"] = self._extract_int(response, "Memory")
            specs["memoryType"] = self._extract_text(response, "Memory Type")

        item["specs"] = json.dumps(specs) if specs else None

        # 提取跑分数据
        benchmarks = []
        if category == "cpu":
            # 单核和多核分数
            single_core = self._extract_score(response, "Single Core")
            multi_core = self._extract_score(response, "Multi Core")
            if single_core:
                benchmarks.append(BenchmarkItem(
                    source="Geekbench",
                    metric="single_core",
                    score=single_core,
                    unit="points"
                ))
            if multi_core:
                benchmarks.append(BenchmarkItem(
                    source="Geekbench",
                    metric="multi_core",
                    score=multi_core,
                    unit="points"
                ))
        else:
            # GPU 分数
            gpu_score = self._extract_score(response, "Vulkan")
            if gpu_score:
                benchmarks.append(BenchmarkItem(
                    source="Geekbench",
                    metric="gpu",
                    score=gpu_score,
                    unit="points"
                ))

        item["benchmarks"] = [dict(b) for b in benchmarks]

        item["architecture"] = self._extract_architecture(response)
        item["launch_date"] = datetime.now().isoformat()
        item["data_source"] = "Geekbench Browser"

        yield item

    def _extract_brand(self, name):
        """从名称中提取品牌"""
        brands = ["Intel", "AMD", "Apple", "Qualcomm", "Samsung", "NVIDIA", "AMD"]
        for brand in brands:
            if brand.lower() in name.lower():
                return brand
        return "Unknown"

    def _extract_architecture(self, response):
        """提取架构信息"""
        return response.css("li:contains('Architecture') + span::text").get()

    def _extract_int(self, response, label):
        """提取整数值"""
        text = response.css(f"li:contains('{label}') + span::text").get()
        return int(text.replace(",", "")) if text else None

    def _extract_float(self, response, label):
        """提取浮点数值"""
        text = response.css(f"li:contains('{label}') + span::text").get()
        if not text:
            return None
        # 处理 GHz 单位
        if "GHz" in text:
            return float(text.replace("GHz", "").strip())
        return float(text)

    def _extract_text(self, response, label):
        """提取文本"""
        return response.css(f"li:contains('{label}') + span::text").get()

    def _extract_score(self, response, label):
        """提取分数"""
        text = response.css(f"li:contains('{label}') + span::text").get()
        if text:
            return float(text.replace(",", ""))
        return None

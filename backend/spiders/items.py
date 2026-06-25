"""
Scrapy Items 定义
定义爬虫数据模型
"""

import scrapy


class HardwareItem(scrapy.Item):
    """硬件数据项"""
    name = scrapy.Field()
    brand = scrapy.Field()
    category = scrapy.Field()
    architecture = scrapy.Field()
    overall_score = scrapy.Field()
    specs = scrapy.Field()
    benchmarks = scrapy.Field()
    launch_date = scrapy.Field()
    data_source = scrapy.Field()


class BenchmarkItem(scrapy.Item):
    """跑分数据项"""
    source = scrapy.Field()
    metric = scrapy.Field()
    score = scrapy.Field()
    unit = scrapy.Field()

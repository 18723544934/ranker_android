# This package will contain the spiders of your Scrapy project

import scrapy

from .geekbench_spider import GeekbenchSpider
from .passmark_spider import PassMarkSpider

__all__ = ['GeekbenchSpider', 'PassMarkSpider']

# This package will contain the spiders of your Scrapy project
#
# Please refer to the documentation for information on how to create and manage
# your spiders.

import scrapy

from .spiders.geekbench_spider import GeekbenchSpider
from .spiders.passmark_spider import PassMarkSpider

__all__ = ['GeekbenchSpider', 'PassMarkSpider']

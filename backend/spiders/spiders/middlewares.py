"""
Scrapy 中间件配置
"""

from scrapy import signals
from scrapy.exceptions import NotConfigured
from scrapy.http import HtmlResponse
import logging

logger = logging.getLogger(__name__)


class SpiderMiddleware:
    """
    爬虫中间件
    处理爬虫生命周期事件
    """

    @classmethod
    def from_crawler(cls, crawler):
        s = cls()
        crawler.signals.connect(s.spider_opened, signal=signals.spider_opened)
        crawler.signals.connect(s.spider_closed, signal=signals.spider_closed)
        return s

    def spider_opened(self, spider):
        spider.logger.info(f"爬虫 {spider.name} 开始运行")

    def spider_closed(self, spider):
        spider.logger.info(f"爬虫 {spider.name} 运行结束")


class RetryMiddleware:
    """
    重试中间件
    处理请求重试逻辑
    """

    def __init__(self, settings):
        self.max_retry_times = settings.getint('RETRY_TIMES', 3)
        self.retry_http_codes = settings.getlist('RETRY_HTTP_CODES', [500, 502, 503])

    def process_response(self, request, response, spider):
        if response.status in self.retry_http_codes:
            request.dont_filter = True
            spider.logger.warning(f"重试请求 {request.url} (状态码: {response.status})")
            return request
        return response


class ProxyMiddleware:
    """
    代理中间件
    如果需要使用代理，可以在这里配置
    """

    def __init__(self, proxy_url=None):
        self.proxy_url = proxy_url

    def process_request(self, request, spider):
        if self.proxy_url:
            request.meta['proxy'] = self.proxy_url
            spider.logger.debug(f"使用代理: {self.proxy_url}")


class UserAgentMiddleware:
    """
    User-Agent 中间件
    为不同类型的请求设置不同的 User-Agent
    """

    def process_request(self, request, spider):
        if 'User-Agent' not in request.headers:
            request.headers['User-Agent'] = spider.settings.get('USER_AGENT', 'PerfTopBot/1.0')

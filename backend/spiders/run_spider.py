#!/usr/bin/env python3
"""
Scrapy 爬虫运行脚本
用于手动触发数据爬取任务
"""

import sys
import os
from scrapy.crawler import CrawlerProcess
from scrapy.utils.project import get_project_settings

# 添加项目根目录到 Python 路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

def run_spider(spider_name):
    """运行指定的爬虫"""
    settings = get_project_settings()
    process = CrawlerProcess(settings)
    
    print(f"开始运行爬虫: {spider_name}")
    process.crawl(spider_name)
    print(f"爬虫 {spider_name} 运行完成")

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("用法: python run_spider.py <spider_name>")
        print("可用爬虫: geekbench, passmark")
        sys.exit(1)
    
    spider_name = sys.argv[1]
    
    valid_spiders = {
        'geekbench': 'spiders.geekbench_spider.GeekbenchSpider',
        'passmark': 'spiders.passmark_spider.PassMarkSpider'
    }
    
    if spider_name not in valid_spiders:
        print(f"错误: 未知的爬虫 '{spider_name}'")
        print(f"可用爬虫: {', '.join(valid_spiders.keys())}")
        sys.exit(1)
    
    try:
        run_spider(valid_spiders[spider_name])
    except Exception as e:
        print(f"错误: {str(e)}")
        sys.exit(1)

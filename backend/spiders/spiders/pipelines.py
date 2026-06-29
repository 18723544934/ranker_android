"""
Scrapy Pipelines
处理爬取的数据并保存到数据库和文件
"""

import json
import os
from datetime import datetime
from scrapy.exceptions import DropItem
import psycopg2
from dotenv import load_dotenv

load_dotenv()


class DatabasePipeline:
    """
    数据库管道
    将抓取的硬件数据保存到 PostgreSQL 数据库
    """

    def __init__(self):
        self.connection = None
        self.cursor = None

    def open_spider(self, spider):
        """爬虫开始时打开数据库连接"""
        try:
            self.connection = psycopg2.connect(
                os.getenv('DATABASE_URL', 'postgresql://perftop:perftop_password@localhost:5432/perftop')
            )
            self.cursor = self.connection.cursor()
            spider.logger.info("数据库连接已建立")
        except Exception as e:
            spider.logger.error(f"数据库连接失败: {str(e)}")

    def close_spider(self, spider):
        """爬虫结束时关闭数据库连接"""
        if self.cursor:
            self.cursor.close()
        if self.connection:
            self.connection.close()
        spider.logger.info("数据库连接已关闭")

    def process_item(self, item, spider):
        """处理每个抓取的硬件项"""
        try:
            # 检查硬件是否已存在
            self.cursor.execute(
                "SELECT id FROM hardwares WHERE name = %s AND category = %s",
                (item.get('name'), item.get('category'))
            )
            existing = self.cursor.fetchone()

            if existing:
                # 更新现有硬件
                self.cursor.execute("""
                    UPDATE hardwares SET
                        brand = %s,
                        architecture = %s,
                        launch_date = %s,
                        specs_json = %s,
                        overall_score = %s,
                        updated_at = %s
                    WHERE id = %s
                """, (
                    item.get('brand'),
                    item.get('architecture'),
                    item.get('launch_date'),
                    json.dumps(item.get('specs', {})),
                    item.get('overall_score'),
                    datetime.now(),
                    existing[0]
                ))
                hardware_id = existing[0]
            else:
                # 插入新硬件
                self.cursor.execute("""
                    INSERT INTO hardwares (name, brand, category, architecture, launch_date, specs_json, overall_score, created_at, updated_at)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    RETURNING id
                """, (
                    item.get('name'),
                    item.get('brand'),
                    item.get('category'),
                    item.get('architecture'),
                    item.get('launch_date'),
                    json.dumps(item.get('specs', {})),
                    item.get('overall_score'),
                    datetime.now(),
                    datetime.now()
                ))
                hardware_id = self.cursor.fetchone()[0]

            self.connection.commit()
            spider.logger.info(f"已保存硬件: {item.get('name')} (ID: {hardware_id})")

            # 处理跑分数据
            benchmarks = item.get('benchmarks', [])
            for benchmark in benchmarks:
                self._save_benchmark(hardware_id, benchmark, spider)

            return item

        except Exception as e:
            spider.logger.error(f"保存硬件失败: {str(e)}")
            self.connection.rollback()
            raise DropItem(f"数据库保存失败: {str(e)}")

    def _save_benchmark(self, hardware_id, benchmark, spider):
        """保存跑分数据"""
        self.cursor.execute("""
            INSERT INTO benchmarks (hardware_id, source, metric, score, unit, created_at)
            VALUES (%s, %s, %s, %s, %s, %s)
            ON CONFLICT (hardware_id, source, metric) DO UPDATE SET
                score = EXCLUDED.score,
                unit = EXCLUDED.unit,
                created_at = %s
        """, (
            hardware_id,
            benchmark['source'],
            benchmark['metric'],
            benchmark['score'],
            benchmark['unit'],
            datetime.now()
        ))
        self.connection.commit()


class JsonWriterPipeline:
    """
    JSON 文件写入管道
    将抓取的数据保存为 JSON 文件
    """

    def __init__(self):
        self.files = {}

    def open_spider(self, spider):
        """爬虫开始时创建输出文件"""
        output_dir = spider.settings.get('OUTPUT_DIR', 'output')
        os.makedirs(output_dir, exist_ok=True)

        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        filename = f"hardware_{timestamp}.json"
        filepath = os.path.join(output_dir, filename)

        self.files[spider] = open(filepath, 'w', encoding='utf-8')
        spider.logger.info(f"创建输出文件: {filepath}")

    def close_spider(self, spider):
        """爬虫结束时关闭文件"""
        if spider in self.files:
            self.files[spider].close()
            spider.logger.info(f"输出文件已保存")
            del self.files[spider]

    def process_item(self, item, spider):
        """将每个项目写入 JSON 文件"""
        line = json.dumps(dict(item), ensure_ascii=False) + "\n"
        self.files[spider].write(line)
        return item


class DuplicateFilterPipeline:
    """
    重复数据过滤管道
    过滤掉重复的硬件数据
    """

    def __init__(self):
        self.seen_items = set()

    def process_item(self, item, spider):
        """检查是否已处理过相同的硬件"""
        item_key = (item.get('name'), item.get('category'))
        if item_key in self.seen_items:
            spider.logger.debug(f"跳过重复项: {item.get('name')}")
            raise DropItem(f"重复项: {item.get('name')}")

        self.seen_items.add(item_key)
        return item


class DataValidationPipeline:
    """
    数据验证管道
    验证抓取的数据是否符合要求
    """

    def process_item(self, item, spider):
        """验证数据完整性"""
        required_fields = ['name', 'brand', 'category', 'overall_score']

        for field in required_fields:
            if not item.get(field):
                spider.logger.warning(f"缺少必需字段: {field}")
                raise DropItem(f"缺少必需字段: {field}")

        # 验证分数范围
        score = item.get('overall_score', 0)
        if not isinstance(score, (int, float)) or score < 0:
            spider.logger.warning(f"无效的分数: {score}")
            raise DropItem(f"无效的分数: {score}")

        # 验证分类
        valid_categories = ['pc_cpu', 'pc_gpu', 'mobile_cpu', 'mobile_gpu']
        if item.get('category') not in valid_categories:
            spider.logger.warning(f"无效的分类: {item.get('category')}")
            raise DropItem(f"无效的分类: {item.get('category')}")

        return item


class StatsPipeline:
    """
    统计管道
    记录爬虫运行统计信息
    """

    def __init__(self):
        self.item_count = 0
        self.start_time = None

    def open_spider(self, spider):
        """爬虫开始时记录开始时间"""
        self.start_time = datetime.now()
        spider.logger.info(f"爬虫开始: {self.start_time}")

    def close_spider(self, spider):
        """爬虫结束时输出统计信息"""
        if self.start_time:
            duration = datetime.now() - self.start_time
            spider.logger.info(
                f"爬虫结束 - 耗时: {duration}, "
                f"处理项目数: {self.item_count}"
            )

    def process_item(self, item, spider):
        """统计处理的项目数量"""
        self.item_count += 1
        return item

"""
Scrapy Pipelines
处理爬取的数据并保存到数据库和文件
"""

import json
import os
from datetime import datetime
from itemadapter import ItemAdapter
from scrapy.exceptions import DropItem


class DatabasePipeline:
    """将数据保存到数据库的 Pipeline"""

    def __init__(self):
        self.database_url = os.getenv('DATABASE_URL', 'postgresql://perftop:perftop_password@localhost:5432/perftop')
        # 这里应该初始化数据库连接
        # 实际项目中应该使用 SQLAlchemy 或直接的数据库连接
        self.items_seen = set()

    def process_item(self, item, spider):
        adapter = ItemAdapter(item)
        if adapter.is_valid():
            # 检查是否已经处理过该硬件（通过名称和类别）
            item_key = f"{adapter.get('name')}_{adapter.get('category')}"
            if item_key in self.items_seen:
                spider.logger.info(f"跳过重复项: {adapter.get('name')}")
                raise DropItem(f"Duplicate item: {adapter.get('name')}")

            self.items_seen.add(item_key)

            # 这里应该将数据保存到数据库
            # 示例代码（需要根据实际数据库结构调整）:
            # hardware = Hardware(
            #     name=adapter.get('name'),
            #     brand=adapter.get('brand'),
            #     category=adapter.get('category'),
            #     architecture=adapter.get('architecture'),
            #     overall_score=adapter.get('overall_score'),
            #     specs_json=adapter.get('specs'),
            #     launch_date=adapter.get('launch_date')
            # )
            # self.session.add(hardware)
            # self.session.commit()

            spider.logger.info(f"保存硬件到数据库: {adapter.get('name')}")
            return item
        else:
            spider.logger.error(f"无效的硬件数据: {item}")
            raise DropItem("Invalid hardware data")

    def close_spider(self, spider):
        # 关闭数据库连接
        pass


class JsonWriterPipeline:
    """将数据保存到 JSON 文件的 Pipeline"""

    def __init__(self):
        self.output_dir = getattr(self, 'output_dir', 'output')
        os.makedirs(self.output_dir, exist_ok=True)
        self.current_file = None
        self.current_data = []
        self.file_counter = 0

    def open_spider(self, spider):
        spider_name = spider.name
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        self.current_file = os.path.join(
            self.output_dir,
            f'{spider_name}_{timestamp}.json'
        )
        self.current_data = []
        self.file_counter = 0
        spider.logger.info(f"开始写入文件: {self.current_file}")

    def process_item(self, item, spider):
        adapter = ItemAdapter(item)
        if adapter.is_valid():
            self.current_data.append(dict(item))
            self.file_counter += 1

            # 每100条数据写入一次文件
            if self.file_counter >= 100:
                self._write_to_file()
                self.file_counter = 0
            return item
        else:
            spider.logger.error(f"跳过无效项: {item}")
            return None

    def close_spider(self, spider):
        # 写入剩余数据
        if self.current_data:
            self._write_to_file()

        if self.current_file and os.path.exists(self.current_file):
            spider.logger.info(f"完成写入文件: {self.current_file} (共 {self.file_counter} 条)")

    def _write_to_file(self):
        """将当前数据写入文件"""
        if not self.current_file or not self.current_data:
            return

        try:
            # 追加模式写入
            mode = 'a' if os.path.exists(self.current_file) else 'w'
            with open(self.current_file, mode, encoding='utf-8') as f:
                if mode == 'w':
                    # 新文件，写入数组开头
                    f.write('[\n')
                    for i, item in enumerate(self.current_data):
                        if i > 0:
                            f.write(',\n')
                        json.dump(item, f, ensure_ascii=False, indent=2)
                    f.write('\n]')
                else:
                    # 追加模式，需要处理 JSON 数组格式
                    # 这里简化处理，实际应该更复杂
                    f.write(',\n')
                    for i, item in enumerate(self.current_data):
                        if i > 0:
                            f.write(',\n')
                        json.dump(item, f, ensure_ascii=False, indent=2)
                    f.write('\n]')

            self.current_data = []
        except Exception as e:
            spider.logger.error(f"写入文件失败: {e}")


class DeduplicationPipeline:
    """去重 Pipeline - 根据名称和类别去重"""

    def __init__(self):
        self.seen = set()

    def process_item(self, item, spider):
        adapter = ItemAdapter(item)
        key = f"{adapter.get('name')}_{adapter.get('category')}_{adapter.get('brand')}"

        if key in self.seen:
            spider.logger.info(f"发现重复项，跳过: {adapter.get('name')}")
            raise DropItem(f"Duplicate hardware: {adapter.get('name')}")

        self.seen.add(key)
        return item


class DataValidationPipeline:
    """数据验证 Pipeline - 确保数据完整和有效"""

    def process_item(self, item, spider):
        adapter = ItemAdapter(item)

        # 验证必填字段
        required_fields = ['name', 'brand', 'category', 'overall_score']
        for field in required_fields:
            if not adapter.get(field):
                spider.logger.error(f"缺少必填字段: {field}")
                raise DropItem(f"Missing required field: {field}")

        # 验证分数范围
        score = adapter.get('overall_score')
        if score is not None and (score < 0 or score > 100000):
            spider.logger.warning(f"分数超出合理范围: {score}")

        # 验证规格 JSON
        specs = adapter.get('specs')
        if specs:
            try:
                if isinstance(specs, str):
                    json.loads(specs)
            except json.JSONDecodeError:
                spider.logger.error(f"规格 JSON 格式错误: {specs}")
                raise DropItem("Invalid specs JSON format")

        return item

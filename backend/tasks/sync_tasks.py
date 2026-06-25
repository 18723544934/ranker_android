"""
数据同步任务
使用 Celery 定期从数据源同步硬件数据
"""

from celery import shared_task
import subprocess
import os
from datetime import datetime, timedelta
from tasks.celery_app import app
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@shared_task(bind=True)
def sync_geekbench_data(self):
    """
    同步 Geekbench 数据
    运行 Scrapy 爬虫抓取最新数据
    """
    logger.info("开始同步 Geekbench 数据...")
    task_id = self.request.id

    try:
        # 构建爬虫命令
        scrapy_cmd = [
            'scrapy',
            'crawl',
            'geekbench',
            '-o',
            f'output/geekbench_{task_id}.json',
            '-s',
            'LOG_LEVEL=INFO'
        ]

        # 执行爬虫
        result = subprocess.run(
            scrapy_cmd,
            cwd=os.path.join(os.path.dirname(__file__), '../spiders'),
            capture_output=True,
            text=True,
            timeout=1800  # 30分钟超时
        )

        if result.returncode == 0:
            logger.info(f"Geekbench 数据同步完成: {result.stdout}")
            return {
                'status': 'success',
                'message': 'Geekbench 数据同步成功',
                'output_file': f'geekbench_{task_id}.json',
                'timestamp': datetime.now().isoformat()
            }
        else:
            logger.error(f"Geekbench 数据同步失败: {result.stderr}")
            return {
                'status': 'failed',
                'message': 'Geekbench 数据同步失败',
                'error': result.stderr,
                'timestamp': datetime.now().isoformat()
            }

    except subprocess.TimeoutExpired:
        logger.error("Geekbench 数据同步超时")
        return {
            'status': 'timeout',
            'message': 'Geekbench 数据同步超时',
            'timestamp': datetime.now().isoformat()
        }
    except Exception as e:
        logger.error(f"Geekbench 数据同步异常: {str(e)}")
        return {
            'status': 'error',
            'message': f'Geekbench 数据同步异常: {str(e)}',
            'timestamp': datetime.now().isoformat()
        }


@shared_task(bind=True)
def sync_passmark_data(self):
    """
    同步 PassMark 数据
    运行 Scrapy 爬虫抓取最新数据
    """
    logger.info("开始同步 PassMark 数据...")
    task_id = self.request.id

    try:
        scrapy_cmd = [
            'scrapy',
            'crawl',
            'passmark',
            '-o',
            f'output/passmark_{task_id}.json',
            '-s',
            'LOG_LEVEL=INFO'
        ]

        result = subprocess.run(
            scrapy_cmd,
            cwd=os.path.join(os.path.dirname(__file__), '../spiders'),
            capture_output=True,
            text=True,
            timeout=1800
        )

        if result.returncode == 0:
            logger.info(f"PassMark 数据同步完成: {result.stdout}")
            return {
                'status': 'success',
                'message': 'PassMark 数据同步成功',
                'output_file': f'passmark_{task_id}.json',
                'timestamp': datetime.now().isoformat()
            }
        else:
            logger.error(f"PassMark 数据同步失败: {result.stderr}")
            return {
                'status': 'failed',
                'message': 'PassMark 数据同步失败',
                'error': result.stderr,
                'timestamp': datetime.now().isoformat()
            }

    except subprocess.TimeoutExpired:
        logger.error("PassMark 数据同步超时")
        return {
            'status': 'timeout',
            'message': 'PassMark 数据同步超时',
            'timestamp': datetime.now().isoformat()
        }
    except Exception as e:
        logger.error(f"PassMark 数据同步异常: {str(e)}")
        return {
            'status': 'error',
            'message': f'PassMark 数据同步异常: {str(e)}',
            'timestamp': datetime.now().isoformat()
        }


@shared_task
def cleanup_old_data():
    """
    清理旧数据
    删除超过30天的临时文件和日志
    """
    logger.info("开始清理旧数据...")

    try:
        # 清理输出目录中的旧文件
        output_dir = os.path.join(os.path.dirname(__file__), '../spiders/output')
        cutoff_date = datetime.now() - timedelta(days=30)

        if os.path.exists(output_dir):
            deleted_count = 0
            for filename in os.listdir(output_dir):
                filepath = os.path.join(output_dir, filename)
                if os.path.isfile(filepath):
                    file_time = datetime.fromtimestamp(os.path.getmtime(filepath))
                    if file_time < cutoff_date:
                        os.remove(filepath)
                        deleted_count += 1

            logger.info(f"清理完成，删除了 {deleted_count} 个旧文件")
            return {
                'status': 'success',
                'message': f'清理完成，删除了 {deleted_count} 个旧文件',
                'deleted_count': deleted_count,
                'timestamp': datetime.now().isoformat()
            }
        else:
            logger.warning("输出目录不存在")
            return {
                'status': 'skipped',
                'message': '输出目录不存在',
                'timestamp': datetime.now().isoformat()
            }

    except Exception as e:
        logger.error(f"清理旧数据异常: {str(e)}")
        return {
            'status': 'error',
            'message': f'清理旧数据异常: {str(e)}',
            'timestamp': datetime.now().isoformat()
        }


@shared_task
def manual_sync_all():
    """
    手动触发全量同步
    依次执行所有数据源的同步任务
    """
    logger.info("开始手动全量同步...")

    try:
        # 同步 Geekbench
        geekbench_result = sync_geekbench_data.delay()
        if geekbench_result.get('status') != 'success':
            logger.warning(f"Geekbench 同步失败: {geekbench_result.get('message')}")

        # 同步 PassMark
        passmark_result = sync_passmark_data.delay()
        if passmark_result.get('status') != 'success':
            logger.warning(f"PassMark 同步失败: {passmark_result.get('message')}")

        logger.info("手动全量同步完成")
        return {
            'status': 'success',
            'message': '全量同步完成',
            'geekbench': geekbench_result,
            'passmark': passmark_result,
            'timestamp': datetime.now().isoformat()
        }

    except Exception as e:
        logger.error(f"手动全量同步异常: {str(e)}")
        return {
            'status': 'error',
            'message': f'手动全量同步异常: {str(e)}',
            'timestamp': datetime.now().isoformat()
        }


if __name__ == '__main__':
    # 测试任务
    print("测试同步任务...")
    result = sync_geekbench_data.delay()
    print(f"结果: {result.get()}")

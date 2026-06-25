"""
Celery 应用配置
用于后台定时任务和异步任务处理
"""

from celery import Celery
import os
from dotenv import load_dotenv

load_dotenv()

# Celery 配置
app = Celery(
    'perftop',
    broker=os.getenv('REDIS_URL', 'redis://localhost:6379/0'),
    backend=os.getenv('REDIS_URL', 'redis://localhost:6379/0'),
    include=['tasks.sync_tasks']
)

# 可选配置
app.conf.update(
    task_serializer='json',
    accept_content=['json'],
    result_serializer='json',
    timezone='Asia/Shanghai',
    enable_utc=True,
    task_track_started=True,
    task_time_limit=30 * 60,  # 30分钟超时
    task_soft_time_limit=25 * 60,  # 25分钟软超时
)

# 定时任务配置
app.conf.beat_schedule = {
    'sync-geekbench-every-6-hours': {
        'task': 'tasks.sync_tasks.sync_geekbench_data',
        'schedule': 21600.0,  # 6小时
    },
    'sync-passmark-every-12-hours': {
        'task': 'tasks.sync_tasks.sync_passmark_data',
        'schedule': 43200.0,  # 12小时
    },
    'cleanup-old-data-daily': {
        'task': 'tasks.sync_tasks.cleanup_old_data',
        'schedule': 86400.0,  # 24小时
    },
}

if __name__ == '__main__':
    app.start()

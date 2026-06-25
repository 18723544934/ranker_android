#!/bin/bash

echo "========================================="
echo "PerfTop 项目完整性检查"
echo "========================================="
echo ""

# 检查 Android 项目结构
echo "[1/5] 检查 Android 项目结构..."
required_android_dirs=(
    "app/src/main/java/com/perftop/android"
    "app/src/main/java/com/perftop/android/core"
    "app/src/main/java/com/perftop/android/domain"
    "app/src/main/java/com/perftop/android/data"
    "app/src/main/java/com/perftop/android/presentation"
    "app/src/main/res"
    "app/src/main/res/values"
    "app/src/main/res/drawable"
    "app/src/main/res/mipmap-hdpi"
    "app/src/main/res/mipmap-mdpi"
    "app/src/main/res/mipmap-xhdpi"
    "app/src/main/res/mipmap-xxhdpi"
    "app/src/main/res/mipmap-xxxhdpi"
)

missing_dirs=()
for dir in "${required_android_dirs[@]}"; do
    if [ ! -d "$dir" ]; then
        missing_dirs+=("$dir ")
    fi
done

if [ ${#missing_dirs[@]} -gt 0 ]; then
    echo "[警告] 缺失目录: ${missing_dirs[*]}"
else
    echo "[完成] Android 目录结构完整"
fi

# 检查后端项目结构
echo ""
echo "[2/5] 检查后端项目结构..."
required_backend_dirs=(
    "backend/api"
    "backend/database"
    "backend/spiders"
    "backend/tasks"
)

missing_dirs=()
for dir in "${required_backend_dirs[@]}"; do
    if [ ! -d "$dir" ]; then
        missing_dirs+=("$dir ")
    fi
done

if [ ${#missing_dirs[@]} -gt 0 ]; then
    echo "[警告] 缺失目录: ${missing_dirs[*]}"
else
    echo "[完成] 后端目录结构完整"
fi

# 检查关键文件
echo ""
echo "[3/5] 检查关键文件..."
critical_files=(
    "app/build.gradle.kts"
    "app/src/main/AndroidManifest.xml"
    "app/src/main/java/com/perftop/android/MainActivity.kt"
    "app/src/main/java/com/perftop/android/PerfTopApplication.kt"
    "app/src/main/res/values/strings.xml"
    "app/src/main/res/values/themes.xml"
    "app/src/main/res/values/colors.xml"
    "backend/requirements.txt"
    "backend/docker-compose.yml"
    "backend/api/main.py"
    "backend/database/models.py"
    "backend/spiders/scrapy.cfg"
    "backend/spiders/items.py"
    "backend/tasks/celery_app.py"
    "backend/.env.example"
    "README.md"
    "start.bat"
    "start.sh"
)

missing_files=()
for file in "${critical_files[@]}"; do
    if [ ! -f "$file" ]; then
        missing_files+=("$file ")
    fi
done

if [ ${#missing_files[@]} -gt 0 ]; then
    echo "[警告] 缺失文件: ${missing_files[*]}"
else
    echo "[完成] 关键文件完整"
fi

# 检查 Kotlin 文件语法
echo ""
echo "[4/5] 检查 Kotlin 文件..."
kotlin_files=(
    "app/src/main/java/com/perftop/android/MainActivity.kt"
    "app/src/main/java/com/perftop/android/PerfTopApplication.kt"
    "app/src/main/java/com/perftop/android/core/navigation/Screen.kt"
    "app/src/main/java/com/perftop/android/core/navigation/PerfTopNavGraph.kt"
)

syntax_errors=0
for file in "${kotlin_files[@]}"; do
    if [ -f "$file" ]; then
        # 检查是否有基本的 package 语句
        if ! grep -q "^package " "$file" 2>/dev/null; then
            echo "[错误] $file 缺少 package 语句"
            syntax_errors=$((syntax_errors + 1))
        fi
    fi
done

if [ $syntax_errors -eq 0 ]; then
    echo "[完成] Kotlin 文件语法检查通过"
else
    echo "[错误] 发现 $syntax_errors 个语法错误"
fi

# 检查 Python 文件
echo ""
echo "[5/5] 检查 Python 文件..."
python_files=(
    "backend/api/main.py"
    "backend/database/models.py"
    "backend/spiders/geekbench_spider.py"
    "backend/tasks/celery_app.py"
)

python_errors=0
for file in "${python_files[@]}"; do
    if [ -f "$file" ]; then
        # 检查是否有基本的导入
        if ! python3 -m py_compile "$file" 2>/dev/null; then
            echo "[错误] $file 有语法错误"
            python_errors=$((python_errors + 1))
        fi
    done

if [ $python_errors -eq 0 ]; then
    echo "[完成] Python 文件语法检查通过"
else
    echo "[错误] 发现 $python_errors 个 Python 错误"
fi

# 总结
echo ""
echo "========================================="
echo "检查完成"
echo "========================================="
if [ ${#missing_dirs[@]} -gt 0 ] && [ ${#missing_files[@]} -gt 0 ] && [ $syntax_errors -eq 0 ] && [ $python_errors -eq 0 ]; then
    echo "[状态] 项目结构完整，可以开始编译"
    exit 0
else
    echo "[状态] 项目存在缺失或错误，请检查上述警告"
    exit 1
fi

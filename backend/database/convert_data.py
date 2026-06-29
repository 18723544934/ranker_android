# -*- coding: utf-8 -*-
"""
数据转换脚本：将原始数据转换为符合模板格式的 JSON
支持：
1. CPU 数据：all_cpus.json + techpowerup_cpu.json -> cpu_specs_data.json
2. GPU 数据：techpowerup_gpu.json + all-gpus.json -> gpu_specs_data.json
"""

import json
import re
from pathlib import Path

# 数据目录
DATA_DIR = Path(r"C:\Users\Administrator\Desktop\info")
OUTPUT_DIR = Path(r"d:\ranker_android-master\ranker_android\backend\database")

# ==================== CPU 模板 ====================
def get_cpu_template():
    """返回 CPU 规格模板（所有字段）"""
    return {
        "basic_info": {
            "id": None,
            "name": "",
            "brand": "",
            "series": "",
            "model": "",
            "part_number": "",
            "market": "",
            "generation": "",
            "bundled_cooler": ""
        },
        "architecture": {
            "architecture": "",
            "codename": "",
            "foundry": "",
            "process_node": "",
            "io_process_node": "",
            "transistor_count": None,
            "transistor_density": "",
            "die_size": ""
        },
        "core_config": {
            "total_cores": None,
            "total_threads": None,
            "performance_cores": None,
            "efficiency_cores": None,
            "performance_threads": None,
            "efficiency_threads": None
        },
        "clock_speed": {
            "base_clock": None,
            "boost_clock": None,
            "turbo_clock": None,
            "unlocked_multiplier": ""
        },
        "cache": {
            "l1_cache": "",
            "l2_cache": "",
            "l3_cache": ""
        },
        "memory": {
            "memory_type": "",
            "memory_channels": None,
            "max_memory_size": "",
            "max_memory_speed_ddr5": "",
            "max_memory_speed_ddr4": ""
        },
        "power": {
            "tdp": "",
            "ppt": "",
            "max_boost_power": ""
        },
        "interface": {
            "socket": "",
            "pcie_version": "",
            "pcie_lanes": None,
            "pcie_description": ""
        },
        "graphics": {
            "has_igpu": False,
            "igpu_name": "",
            "igpu_base_clock": "",
            "igpu_boost_clock": "",
            "igpu_execution_units": None,
            "igpu_shaders": None
        },
        "features": {
            "smp_cpus": None,
            "virtualization": "",
            "extensions": []
        },
        "benchmarks": {
            "geekbench6": {
                "single_core": None,
                "multi_core": None
            },
            "cinebench_r23": {
                "single_core": None,
                "multi_core": None
            },
            "cinebench_r24": {
                "single_core": None,
                "multi_core": None
            }
        },
        "ranking": {
            "overall_rank": None,
            "single_core_rank": None,
            "multi_core_rank": None,
            "value_score": None
        },
        "scores": {
            "single_core_score": None,
            "multi_core_score": None,
            "overall_score": None,
            "gaming_score": None,
            "workstation_score": None
        },
        "pricing": {
            "launch_price_usd": None,
            "current_price_usd": None,
            "price_performance_ratio": None
        },
        "platform": {
            "bus_speed": "",
            "multiplier": "",
            "package_type": ""
        },
        "physical": {
            "io_die_size": "",
            "t_case_max": ""
        },
        "thermal": {
            "max_operating_temp": ""
        },
        "software": {
            "official_links": [],
            "external_reviews": []
        }
    }

# ==================== GPU 模板 ====================
def get_gpu_template():
    """返回 GPU 规格模板（所有字段）"""
    return {
        "basic_info": {
            "id": None,
            "name": "",
            "brand": "",
            "codename": "",
            "series": "",
            "model": "",
            "release_date": "",
            "announced_date": "",
            "generation": "",
            "predecessor": "",
            "successor": "",
            "production_status": "",
            "driver_support": "",
            "launch_price_usd": None,
            "current_price_usd": None,
            "price_links": []
        },
        "architecture": {
            "architecture": "",
            "foundry": "",
            "process_type": "",
            "process_size": "",
            "transistor_count": None,
            "transistor_density": "",
            "die_size": "",
            "gpu_variant": ""
        },
        "core_config": {
            "shader_units": None,
            "texture_units": None,
            "render_output_units": None,
            "sm_count": None,
            "tensor_cores": None,
            "rt_cores": None
        },
        "cache": {
            "l1_cache": "",
            "l2_cache": "",
            "l3_cache": ""
        },
        "clock_speed": {
            "base_clock": "",
            "boost_clock": "",
            "memory_clock": ""
        },
        "memory": {
            "vram_size": "",
            "vram_type": "",
            "memory_bus_width": None,
            "memory_bandwidth": "",
            "memory_clock_effective": ""
        },
        "power": {
            "tdp": "",
            "suggested_psu": ""
        },
        "interface": {
            "bus_interface": "",
            "pcie_version": "",
            "pcie_lanes": None
        },
        "display": {
            "display_outputs": "",
            "max_resolution": "",
            "max_displays": None
        },
        "video": {
            "video_encode": "",
            "video_decode": "",
            "av1_encode": False,
            "av1_decode": False
        },
        "api_support": {
            "directx_version": "",
            "opengl_version": "",
            "vulkan_version": "",
            "opencl_version": "",
            "cuda_version": "",
            "shader_model": ""
        },
        "features": {
            "ray_tracing": False,
            "dlss": False,
            "fsr": False,
            "hdr_support": False,
            "vrr_support": False
        },
        "theoretical_performance": {
            "pixel_rate": "",
            "texture_rate": "",
            "fp16_performance": "",
            "fp32_performance": "",
            "fp64_performance": ""
        },
        "benchmarks": {
            "3dmark_time_spy": None,
            "3dmark_time_spy_extreme": None,
            "3dmark_port_royal": None,
            "3dmark_fire_strike": None,
            "3dmark_fire_strike_extreme": None,
            "3dmark_fire_strike_ultra": None,
            "gpubench": None
        },
        "ranking": {
            "overall_rank": None,
            "gaming_rank": None,
            "value_rank": None,
            "workstation_rank": None
        },
        "scores": {
            "gaming_score": None,
            "workstation_score": None,
            "value_score": None,
            "overall_score": None
        },
        "pricing": {
            "launch_price_usd": None,
            "current_price_usd": None,
            "price_performance_ratio": None
        },
        "platform": {
            "board_number": ""
        },
        "physical": {
            "slot_width": "",
            "length": "",
            "height": "",
            "width": "",
            "weight": ""
        },
        "thermal": {
            "max_operating_temp": ""
        },
        "software": {
            "official_links": [],
            "external_reviews": []
        }
    }

# ==================== CPU 数据转换 ====================
def parse_cores_threads(value):
    """解析 cores_threads 格式，如 '8 / 16'"""
    if not value:
        return None, None
    parts = value.split('/')
    if len(parts) == 2:
        return int(parts[0].strip()), int(parts[1].strip())
    return None, None

def parse_clock(value):
    """解析时钟频率，如 '3.0 GHz' -> 3.0"""
    if not value:
        return None
    match = re.search(r'([\d.]+)', value)
    if match:
        return float(match.group(1))
    return None

def parse_cache_size(value):
    """解析缓存大小"""
    if not value:
        return ""
    # 直接返回字符串，如 "128 MB"
    return value

def parse_tdp(value):
    """解析 TDP"""
    if not value:
        return ""
    return value

def parse_geekbench_score(value):
    """解析 Geekbench 分数"""
    if isinstance(value, (int, float)):
        return int(value)
    if isinstance(value, str):
        match = re.search(r'(\d+)', value)
        if match:
            return int(match.group(1))
    return None

def convert_cpu_data(all_cpus_path, techpowerup_cpu_path, output_path):
    """转换 CPU 数据"""
    # 读取 all_cpus.json (Geekbench 数据)
    with open(all_cpus_path, 'r', encoding='utf-8') as f:
        all_cpus = json.load(f)
    
    # 读取 techpowerup_cpu.json
    with open(techpowerup_cpu_path, 'r', encoding='utf-8') as f:
        techpowerup_cpus = json.load(f)
    
    # 创建名称到 TechPowerUp 数据的映射
    tp_cpu_map = {}
    for cpu in techpowerup_cpus:
        name = cpu.get('name', '').strip()
        if name:
            tp_cpu_map[name.lower()] = cpu
    
    # 合并数据
    result = []
    cpu_id = 1
    
    for cpu in all_cpus:
        template = get_cpu_template()
        name = cpu.get('name', '').strip()
        
        # 基础信息
        template['basic_info']['id'] = cpu_id
        template['basic_info']['name'] = name
        template['basic_info']['brand'] = cpu.get('brand', '')
        
        # 解析 specs 字段（JSON 字符串）
        specs = cpu.get('specs', {})
        if isinstance(specs, str):
            try:
                specs = json.loads(specs)
            except:
                specs = {}
        
        # Geekbench 分数
        template['benchmarks']['geekbench6']['single_core'] = parse_geekbench_score(specs.get('score'))
        template['benchmarks']['geekbench6']['multi_core'] = parse_geekbench_score(specs.get('multiscore'))
        
        # 分数（用于排序）
        template['scores']['single_core_score'] = template['benchmarks']['geekbench6']['single_core']
        template['scores']['multi_core_score'] = template['benchmarks']['geekbench6']['multi_core']
        template['scores']['overall_score'] = template['benchmarks']['geekbench6']['multi_core']
        
        # 尝试从 TechPowerUp 数据补充
        tp_data = tp_cpu_map.get(name.lower(), {})
        
        if tp_data:
            # 架构信息
            template['architecture']['codename'] = tp_data.get('codename', '')
            template['architecture']['process_node'] = tp_data.get('process', '')
            
            # 核心/线程
            cores, threads = parse_cores_threads(tp_data.get('cores_threads', ''))
            template['core_config']['total_cores'] = cores
            template['core_config']['total_threads'] = threads
            
            # 时钟
            template['clock_speed']['base_clock'] = parse_clock(tp_data.get('clock', ''))
            template['clock_speed']['boost_clock'] = parse_clock(tp_data.get('turbo_clock', ''))
            
            # 缓存
            template['cache']['l3_cache'] = parse_cache_size(tp_data.get('l3_cache', ''))
            
            # TDP
            template['power']['tdp'] = parse_tdp(tp_data.get('tdp', ''))
            
            # 接口
            template['interface']['socket'] = tp_data.get('socket', '')
        
        result.append(template)
        cpu_id += 1
    
    # 写入输出文件
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    
    print(f"CPU 数据转换完成: {len(result)} 条记录 -> {output_path}")
    return result

# ==================== GPU 数据转换 ====================
def parse_gpu_memory_size(value):
    """解析显存大小，如 0.128 GB -> '128 MB', 16 -> '16 GB'"""
    if value is None:
        return ""
    if isinstance(value, (int, float)):
        # 假设单位是 GB
        if value < 1:
            return f"{int(value * 1024)} MB"
        return f"{int(value)} GB"
    return str(value)

def parse_process_size(value):
    """解析工艺节点，如 800 -> '800 nm', 5 -> '5 nm'"""
    if value is None:
        return ""
    if isinstance(value, (int, float)):
        if value >= 100:
            return f"{int(value)} nm"
        return f"{int(value)} nm"
    return str(value)

def parse_clock_speed(value):
    """解析时钟速度，MHz 为单位"""
    if value is None:
        return ""
    if isinstance(value, (int, float)):
        return f"{int(value)} MHz"
    return str(value)

def parse_bandwidth(value):
    """解析带宽，GB/s 为单位"""
    if value is None:
        return ""
    if isinstance(value, (int, float)):
        return f"{value} GB/s"
    return str(value)

def convert_gpu_data(all_gpus_path, techpowerup_gpu_path, output_path):
    """转换 GPU 数据（优先使用 all-gpus.json，补充 techpowerup_gpu.json）"""
    # 读取 all-gpus.json（新数据源，更完整）
    with open(all_gpus_path, 'r', encoding='utf-8') as f:
        all_gpus = json.load(f)
    
    # 读取 techpowerup_gpu.json（旧数据源，用于补充）
    with open(techpowerup_gpu_path, 'r', encoding='utf-8') as f:
        techpowerup_gpus = json.load(f)
    
    # 创建名称到 TechPowerUp 数据的映射（用于补充）
    tp_gpu_map = {}
    for gpu in techpowerup_gpus:
        name = gpu.get('name', '').strip()
        if name:
            tp_gpu_map[name.lower()] = gpu
    
    # 合并数据
    result = []
    gpu_id = 1
    
    for gpu in all_gpus:
        template = get_gpu_template()
        name = gpu.get('name', '').strip()
        
        # ========== basic_info ==========
        template['basic_info']['id'] = gpu_id
        template['basic_info']['name'] = name
        template['basic_info']['brand'] = gpu.get('vendor', '').upper()
        template['basic_info']['codename'] = gpu.get('gpuName', '')
        template['basic_info']['series'] = gpu.get('generation', '')
        template['basic_info']['release_date'] = gpu.get('releaseDate', '')
        template['basic_info']['generation'] = gpu.get('generation', '')
        template['basic_info']['production_status'] = 'Active'  # 默认值
        
        # ========== architecture ==========
        template['architecture']['architecture'] = gpu.get('architecture', '')
        template['architecture']['foundry'] = gpu.get('foundry', '')
        template['architecture']['process_type'] = ''
        template['architecture']['process_size'] = parse_process_size(gpu.get('processSize'))
        template['architecture']['transistor_count'] = int(gpu.get('transistors', 0) * 1000) if gpu.get('transistors') else None  # 百万 -> 百万
        template['architecture']['transistor_density'] = f"{gpu.get('transistorDensity')} M/mm²" if gpu.get('transistorDensity') else ''
        template['architecture']['die_size'] = f"{gpu.get('dieSize')} mm²" if gpu.get('dieSize') else ''
        
        # ========== core_config ==========
        template['core_config']['shader_units'] = gpu.get('shaderUnits')
        template['core_config']['texture_units'] = gpu.get('tmus')
        template['core_config']['render_output_units'] = gpu.get('rops')
        template['core_config']['sm_count'] = gpu.get('smCount')
        template['core_config']['tensor_cores'] = gpu.get('tensorCores')
        template['core_config']['rt_cores'] = gpu.get('rtCores')
        
        # ========== clock_speed ==========
        template['clock_speed']['base_clock'] = parse_clock_speed(gpu.get('baseClock'))
        template['clock_speed']['boost_clock'] = parse_clock_speed(gpu.get('boostClock'))
        template['clock_speed']['memory_clock'] = parse_clock_speed(gpu.get('memoryClock'))
        
        # ========== memory ==========
        template['memory']['vram_size'] = parse_gpu_memory_size(gpu.get('memorySize'))
        template['memory']['vram_type'] = gpu.get('memoryType', '')
        template['memory']['memory_bus_width'] = gpu.get('memoryBus')
        template['memory']['memory_bandwidth'] = parse_bandwidth(gpu.get('memoryBandwidth'))
        
        # ========== power ==========
        template['power']['tdp'] = f"{gpu.get('tdp')} W" if gpu.get('tdp') else ''
        template['power']['suggested_psu'] = f"{gpu.get('suggestedPSU')} W" if gpu.get('suggestedPSU') else ''
        
        # ========== interface ==========
        template['interface']['bus_interface'] = gpu.get('busInterface', '')
        
        # ========== display ==========
        template['display']['display_outputs'] = gpu.get('displayOutputs', '')
        
        # ========== api_support ==========
        template['api_support']['directx_version'] = gpu.get('directX', '')
        template['api_support']['opengl_version'] = gpu.get('openGL', '')
        template['api_support']['vulkan_version'] = gpu.get('vulkan', '')
        template['api_support']['opencl_version'] = gpu.get('openCL', '')
        template['api_support']['cuda_version'] = gpu.get('cuda', '')
        
        # ========== theoretical_performance ==========
        template['theoretical_performance']['pixel_rate'] = f"{gpu.get('pixelRate')} GPixel/s" if gpu.get('pixelRate') else ''
        template['theoretical_performance']['texture_rate'] = f"{gpu.get('textureRate')} GTexel/s" if gpu.get('textureRate') else ''
        template['theoretical_performance']['fp16_performance'] = gpu.get('fp16')
        template['theoretical_performance']['fp32_performance'] = gpu.get('fp32')
        template['theoretical_performance']['fp64_performance'] = gpu.get('fp64')
        
        # ========== physical ==========
        template['physical']['slot_width'] = gpu.get('slot', '')
        template['physical']['length'] = f"{gpu.get('length')} mm" if gpu.get('length') else ''
        template['physical']['width'] = f"{gpu.get('width')} mm" if gpu.get('width') else ''
        template['physical']['height'] = f"{gpu.get('height')} mm" if gpu.get('height') else ''
        template['physical']['weight'] = f"{gpu.get('weight')} g" if gpu.get('weight') else ''
        
        # ========== features ==========
        template['features']['ray_tracing'] = gpu.get('rtCores', 0) > 0 if gpu.get('rtCores') is not None else False
        
        # ========== software ==========
        template['software']['official_links'] = [gpu.get('url', '')] if gpu.get('url') else []
        
        # ========== 尝试从 TechPowerUp 旧数据补充缺失字段 ==========
        tp_data = tp_gpu_map.get(name.lower(), {})
        
        if tp_data:
            # 补充制造商信息
            template['basic_info']['brand'] = template['basic_info']['brand'] or tp_data.get('brand', '')
            
            # 补充电源接口
            template['interface']['pcie_version'] = tp_data.get('pcie_version', '') or template['interface']['pcie_version']
        
        result.append(template)
        gpu_id += 1
    
    # 写入输出文件
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    
    print(f"GPU 数据转换完成: {len(result)} 条记录 -> {output_path}")
    return result

# ==================== 主函数 ====================
def main():
    print("=" * 60)
    print("数据转换脚本")
    print("=" * 60)
    
    # CPU 数据转换
    all_cpus_path = DATA_DIR / "all_cpus.json"
    techpowerup_cpu_path = DATA_DIR / "techpowerup_cpu.json"
    cpu_output_path = OUTPUT_DIR / "cpu_specs_data.json"
    
    if all_cpus_path.exists() and techpowerup_cpu_path.exists():
        print("\n[1/2] 转换 CPU 数据...")
        convert_cpu_data(all_cpus_path, techpowerup_cpu_path, cpu_output_path)
    else:
        print(f"\n[1/2] CPU 数据文件不存在:")
        print(f"  - {all_cpus_path}: {'存在' if all_cpus_path.exists() else '不存在'}")
        print(f"  - {techpowerup_cpu_path}: {'存在' if techpowerup_cpu_path.exists() else '不存在'}")
    
    # GPU 数据转换
    all_gpus_path = DATA_DIR / "RightNow-GPU-Database-main" / "RightNow-GPU-Database-main" / "data" / "all-gpus.json"
    techpowerup_gpu_path = DATA_DIR / "techpowerup_gpu.json"
    gpu_output_path = OUTPUT_DIR / "gpu_specs_data.json"
    
    if all_gpus_path.exists():
        print("\n[2/2] 转换 GPU 数据...")
        convert_gpu_data(all_gpus_path, techpowerup_gpu_path, gpu_output_path)
    else:
        print(f"\n[2/2] GPU 数据文件不存在:")
        print(f"  - {all_gpus_path}: {'存在' if all_gpus_path.exists() else '不存在'}")
        print(f"  - {techpowerup_gpu_path}: {'存在' if techpowerup_gpu_path.exists() else '不存在'}")
    
    print("\n" + "=" * 60)
    print("转换完成！")
    print("=" * 60)

if __name__ == "__main__":
    main()
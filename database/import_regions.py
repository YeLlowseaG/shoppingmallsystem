#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
地区数据导入脚本
将 regions.json 转换为 SQL INSERT 语句
"""

import json
import sys
import os

def escape_sql_string(s):
    """转义SQL字符串"""
    if s is None:
        return 'NULL'
    return "'" + s.replace("'", "''") + "'"

def import_regions(json_file, sql_file):
    """导入地区数据"""
    print(f"正在读取 {json_file}...")
    
    with open(json_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    regions = data.get('china', [])
    print(f"找到 {len(regions)} 个省份")
    
    sql_statements = []
    sql_statements.append("-- 地区数据导入SQL")
    sql_statements.append("-- 生成时间: " + str(os.popen('date').read().strip() if sys.platform != 'win32' else ''))
    sql_statements.append("")
    sql_statements.append("-- 清空现有数据（可选）")
    sql_statements.append("-- TRUNCATE TABLE `region`;")
    sql_statements.append("")
    sql_statements.append("-- 开始导入数据")
    sql_statements.append("")
    
    region_id = 1
    region_map = {}  # code -> id 映射
    
    # 第一级：省份
    for province in regions:
        province_code = province['code']
        province_name = province['name']
        
        province_id = region_id
        region_map[province_code] = province_id
        region_id += 1
        
        sql = f"INSERT INTO `region` (`id`, `code`, `name`, `parent_id`, `level`, `sort_order`, `status`) VALUES ({province_id}, {escape_sql_string(province_code)}, {escape_sql_string(province_name)}, NULL, 1, {province_id}, 1);"
        sql_statements.append(sql)
        
        # 第二级：城市
        cities = province.get('children', [])
        city_sort = 1
        for city in cities:
            city_code = city['code']
            city_name = city['name']
            
            city_id = region_id
            region_map[city_code] = city_id
            region_id += 1
            
            sql = f"INSERT INTO `region` (`id`, `code`, `name`, `parent_id`, `level`, `sort_order`, `status`) VALUES ({city_id}, {escape_sql_string(city_code)}, {escape_sql_string(city_name)}, {province_id}, 2, {city_sort}, 1);"
            sql_statements.append(sql)
            city_sort += 1
            
            # 第三级：区县
            districts = city.get('children', [])
            district_sort = 1
            for district in districts:
                district_code = district['code']
                district_name = district['name']
                
                district_id = region_id
                region_map[district_code] = district_id
                region_id += 1
                
                sql = f"INSERT INTO `region` (`id`, `code`, `name`, `parent_id`, `level`, `sort_order`, `status`) VALUES ({district_id}, {escape_sql_string(district_code)}, {escape_sql_string(district_name)}, {city_id}, 3, {district_sort}, 1);"
                sql_statements.append(sql)
                district_sort += 1
    
    # 写入SQL文件
    print(f"正在写入 {sql_file}...")
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_statements))
    
    print(f"完成！共生成 {region_id - 1} 条地区数据")
    print(f"SQL文件已保存到: {sql_file}")

if __name__ == '__main__':
    # 获取脚本所在目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    
    json_file = os.path.join(project_root, 'docs', 'regions.json')
    sql_file = os.path.join(script_dir, 'update-20251212-import-regions-data.sql')
    
    if not os.path.exists(json_file):
        print(f"错误: 找不到文件 {json_file}")
        sys.exit(1)
    
    import_regions(json_file, sql_file)









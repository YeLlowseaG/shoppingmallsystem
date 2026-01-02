# Error 140 问题分析报告

## 问题描述
调用聚水潭测试环境API时，所有接口都返回 `{"code":140,"msg":"参数传递错误"}`

## 已排查的问题

### ✅ 1. 网络连接
- curl可以正常连接聚水潭API
- SSL握手正常
- 响应时间正常（不超时）

### ✅ 2. 签名算法
签名算法按照聚水潭文档实现：
```
签名字符串 = appSecret + key1 + value1 + key2 + value2 + ... + appSecret
签名 = MD5(签名字符串).toUpperCase()
```

实际测试签名：
```
签名字符串: a804ba835d0a4c849336f4e2e7695ddfaccess_tokenf804693efc49416a84dcf0ca901e8622app_keyfb5302ac42e8496d9e764db70a3c5c24charsetutf-8methodshops.querytimestamp1767333001version2a804ba835d0a4c849336f4e2e7695ddf
签名: B3D2536704ABA5CC0A0C5D19A35BE50D
```

### ✅ 3. 参数格式
公共参数完全符合文档要求：
- access_token: f804693efc49416a84dcf0ca901e8622
- app_key: fb5302ac42e8496d9e764db70a3c5c24
- charset: utf-8
- method: shops.query
- timestamp: UNIX秒数
- version: 2
- sign: MD5签名（大写）

### ✅ 4. access_token有效期
根据聚水潭后台显示：
- 生成时间：2025-12-31 17:50:00
- 过期时间：2026-01-30 17:55:00
- 当前时间：2026-01-02（在有效期内）

### ✅ 5. 后端代码
- SSL配置已优化（移除自定义SSL，使用系统默认）
- HTTP请求工具类正确
- 参数编码正确（UTF-8 URL编码）

## 当前测试结果

### 测试1：最简单的shops.query接口
```bash
curl -X POST 'https://dev-api.jushuitan.com/api/open/query.aspx' \
  -d 'access_token=f804693efc49416a84dcf0ca901e8622' \
  -d 'app_key=fb5302ac42e8496d9e764db70a3c5c24' \
  -d 'charset=utf-8' \
  -d 'method=shops.query' \
  -d 'timestamp=1767333001' \
  -d 'version=2' \
  -d 'sign=B3D2536704ABA5CC0A0C5D19A35BE50D'
```

**结果**：
```json
{"code":140,"issuccess":false,"msg":"参数传递错误","requestId":"1a0c63d817673330991987300e3a8a"}
```

### 测试2：商品上传接口
同样的签名算法，同样返回Error 140。

## 可能的原因分析

### 1. ISV应用的特殊要求 ⭐️⭐️⭐️⭐️⭐️

**关键发现**：
- 授权接口使用：`/openWebIsv/auth/getInitToken`（ISV专用路径）
- 业务接口使用：`/api/open/query.aspx`（普通路径）

**可能的问题**：
- ISV应用可能需要使用不同的业务API路径
- ISV应用可能需要额外的参数（如partner_id、shop_id等）
- 测试环境的ISV应用可能有特殊限制

### 2. 测试环境的特殊要求 ⭐️⭐️⭐️⭐️

根据文档说明：
> 测试环境地址仅支持测试环境开放平台应用使用

**可能需要**：
- 在聚水潭测试环境后台完成特定的配置
- IP白名单设置
- 应用审核或激活步骤

### 3. access_token类型不匹配 ⭐️⭐️⭐️

**可能原因**：
- 这个token是ISV授权token，只能用于ISV专用接口
- 需要先通过ISV授权流程获取商户授权后的token
- 普通的app_key + access_token模式可能不适用于ISV应用

## 下一步建议

### 方案1：查阅ISV应用完整文档（推荐）⭐️⭐️⭐️⭐️⭐️

**需要确认**：
1. ISV应用的业务API调用路径是否和普通应用不同？
2. ISV应用是否需要额外参数（partner_id、shop_id等）？
3. 测试环境的ISV应用是否需要完成特定的配置步骤？

**文档位置**：
- https://openweb.jushuitan.com/doc?docId=110（测试环境说明）
- 查找"ISV应用开发指南"或"服务商应用文档"

### 方案2：联系聚水潭技术支持（最快）⭐️⭐️⭐️⭐️⭐️

**提供信息**：
- 应用名称：开发通信专业版
- 应用类型：ISV应用（服务商应用）
- 问题：access_token有效但所有API调用都返回Error 140
- 已测试接口：shops.query、jushuitan.itemsku.upload
- 签名算法：已按文档实现并验证
- 环境：测试环境

**问题**：
1. ISV应用在测试环境调用业务API的正确方式是什么？
2. 是否需要额外的ISV专用参数？
3. 测试环境是否有IP白名单或其他限制？

### 方案3：尝试不同的参数组合（探索性）⭐️⭐️

**可以尝试**：
1. 添加空的biz参数：`-d 'biz={}'`
2. 尝试不同的API路径（如果ISV有专用路径）
3. 查看是否需要partner_id或其他ISV标识参数

### 方案4：使用聚水潭官方SDK（如果有）⭐️⭐️⭐️

查看聚水潭是否提供Java SDK，直接使用官方SDK可以避免参数格式问题。

## 当前代码状态

### 已完成的功能
1. ✅ 商品同步日志表和查询界面
2. ✅ ERP配置管理界面
3. ✅ 签名算法实现（JushuitanSignUtil）
4. ✅ HTTP请求工具类（JushuitanHttpUtil）
5. ✅ 商品上传服务（JushuitanItemServiceImpl）
6. ✅ SSL连接优化

### 待解决的问题
1. ❌ Error 140 - 需要确认ISV应用的正确调用方式
2. ⏸️ 商品数据映射 - 需要先解决API调用问题
3. ⏸️ 订单推送功能 - 需要先解决API调用问题

## 技术支持联系方式

**聚水潭开放平台**：
- 官网：https://open.jushuitan.com/
- 文档中心：https://openweb.jushuitan.com/
- 在线客服：登录聚水潭后台查看
- 技术支持工单：（在聚水潭后台提交）

## 总结

**核心问题**：不是代码实现问题，而是ISV应用的调用方式可能和文档描述的不一致。

**解决路径**：
1. 最优：联系聚水潭技术支持，询问ISV应用在测试环境的正确调用方式
2. 次优：查阅完整的ISV应用开发文档
3. 备选：使用聚水潭官方SDK（如果提供）

**已投入时间**：3天

**建议**：不要继续盲目尝试参数组合，应该直接获取官方的ISV应用调用示例或联系技术支持。

---

**文档生成时间**：2026-01-02
**最后更新**：2026-01-02 13:45

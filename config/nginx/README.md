# Nginx配置说明

## 配置文件说明

- **nginx.conf**: Nginx主配置文件

## 配置功能

### 1. 前端静态文件服务

- 监听端口：80
- 静态文件目录：`/usr/share/nginx/html`（Linux）或 `D:/nginx/html`（Windows）
- 支持Vue Router的History模式（try_files配置）
- 静态资源缓存30天

### 2. 文件上传目录服务

- 路径：`/uploads/`
- 实际目录：`D:/uploads/`
- 只允许访问图片和文档文件
- 禁止访问可执行文件（php、jsp、asp等）
- 缓存30天

### 3. API反向代理

- 路径：`/api/`
- 后端服务：`http://localhost:8080/`
- 支持WebSocket（如果需要）
- 超时设置：60秒

### 4. Swagger文档代理（可选）

- Swagger UI：`/swagger-ui.html`
- API文档：`/api-docs`

## 部署说明

### Windows环境

1. 下载Nginx for Windows
2. 将配置文件复制到Nginx配置目录
3. 修改配置文件中的路径（Windows路径格式）
4. 启动Nginx：`nginx.exe`

### Linux环境

1. 安装Nginx：`yum install nginx` 或 `apt-get install nginx`
2. 将配置文件复制到 `/etc/nginx/nginx.conf` 或创建站点配置
3. 修改配置文件中的路径
4. 启动Nginx：`systemctl start nginx`

## 注意事项

1. **文件路径**：根据实际部署环境修改文件路径
   - Windows：`D:/uploads/`
   - Linux：`/data/uploads/`

2. **前端静态文件目录**：
   - 将前端构建后的`dist`目录内容复制到Nginx静态文件目录
   - 或修改配置指向实际的前端文件目录

3. **后端服务地址**：
   - 确保后端服务运行在`localhost:8080`
   - 或修改配置指向实际的后端服务地址

4. **文件权限**：
   - 确保Nginx有权限访问文件上传目录
   - Linux环境需要设置正确的文件权限

5. **防火墙**：
   - 确保80端口已开放
   - 如需HTTPS，配置SSL证书

## 常用命令

```bash
# 检查配置
nginx -t

# 重新加载配置
nginx -s reload

# 停止Nginx
nginx -s stop

# 启动Nginx
nginx
```


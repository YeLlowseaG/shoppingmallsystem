# 测试服务器部署指南

## 当前情况

已提交两个重要更新到 `feature/yellow-modules` 分支：

1. **Commit aa90361**: 修复支付宝回调接口仅支持POST导致URL验证失败的问题
2. **Commit ad97c00**: 添加支付宝签名详细调试日志

测试服务器（43.139.206.84:8081）需要部署这些更新才能看到调试日志。

## 部署步骤

### 1. SSH登录测试服务器

```bash
ssh user@43.139.206.84
```

### 2. 进入项目目录

```bash
cd /path/to/shoppingmallsystem/backend
```

### 3. 拉取最新代码

```bash
# 查看当前分支
git branch

# 如果不在 feature/yellow-modules 分支，切换到该分支
git checkout feature/yellow-modules

# 拉取最新代码
git pull origin feature/yellow-modules

# 确认已拉取到最新提交
git log --oneline -3
# 应该看到:
# ad97c00 debug: 添加支付宝签名详细调试日志
# aa90361 fix: 修复支付宝回调接口仅支持POST导致URL验证失败的问题
```

### 4. 重新编译项目

```bash
# 清理并重新打包（跳过测试）
mvn clean package -DskipTests
```

### 5. 重启服务

根据服务器的部署方式选择：

#### 方式A: 如果使用systemd服务

```bash
sudo systemctl restart shopping-mall-backend
# 查看服务状态
sudo systemctl status shopping-mall-backend
```

#### 方式B: 如果使用手动启动的Java进程

```bash
# 找到当前运行的进程
ps aux | grep shopping-mall

# 杀死旧进程（替换<PID>为实际进程ID）
kill <PID>

# 启动新进程
nohup java -jar target/shopping-mall-backend.jar --spring.profiles.active=test > logs/app.log 2>&1 &
```

#### 方式C: 如果使用Docker

```bash
# 停止并删除旧容器
docker stop shopping-mall-backend
docker rm shopping-mall-backend

# 重新构建镜像
docker build -t shopping-mall-backend:latest .

# 启动新容器
docker run -d --name shopping-mall-backend \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=test \
  shopping-mall-backend:latest
```

### 6. 验证部署

```bash
# 查看日志，确认服务启动成功
tail -f logs/app.log
# 或者（如果使用systemd）
journalctl -u shopping-mall-backend -f
```

查找以下启动成功标志：
```
Started ShoppingMallApplication in X.XXX seconds
```

### 7. 测试支付功能

部署完成后，创建新的支付订单，日志中应该会出现以下调试信息：

```
【签名调试】待签名字符串: app_id=...&biz_content=...
【签名调试】私钥前20字符: MIIEvgIBADANBgkqhki...
【签名调试】生成的签名前20字符: Q2fNNta/Iga/ivJ4PsH...
```

## 关键调试日志位置

新增的调试日志在以下文件中：
- `src/main/java/com/shoppingmall/payment/util/AlipayUtil.java:421-437`

日志会输出：
1. 待签名的完整字符串（用于与支付宝验证字符串对比）
2. 使用的私钥前20个字符（确认密钥正确）
3. 生成的签名前20个字符（用于调试）

## 问题排查

### 如果拉取代码失败

```bash
# 查看本地是否有未提交的修改
git status

# 如果有修改，暂存起来
git stash

# 再次拉取
git pull origin feature/yellow-modules

# 恢复暂存的修改（如果需要）
git stash pop
```

### 如果编译失败

```bash
# 检查Java版本（需要Java 17）
java -version

# 检查Maven版本
mvn -version

# 清理Maven本地缓存
mvn dependency:purge-local-repository
```

### 如果服务启动失败

```bash
# 查看详细错误日志
tail -n 100 logs/app.log

# 检查端口是否被占用
lsof -i :8081

# 检查数据库连接
mysql -h 43.139.206.84 -P 13306 -u shopping_mall -p shopping_mall_test
```

## 下一步

部署完成后，使用以下步骤测试支付功能：

1. 登录买家前端：http://43.139.206.84:3002/
2. 创建新订单
3. 选择支付宝支付
4. 查看后端日志中的【签名调试】信息
5. 将我们的"待签名字符串"与支付宝返回的验证字符串对比
6. 如果字符串一致，问题确定是密钥对不匹配
7. 需要Jie在支付宝开放平台更新正确的应用公钥

## 支付宝签名问题说明

当前错误：`invalid-signature: 验签出错`

**根本原因**：支付宝开放平台配置的"应用公钥"与我们数据库中的"应用私钥"不是一对。

**解决方案**：
1. 方案A：Jie在支付宝开放平台上传与当前私钥匹配的公钥
2. 方案B：重新生成新的RSA2密钥对，更新数据库私钥和平台公钥

**验证方法**：
部署此次更新后，通过日志对比签名字符串，可以100%确认是否为密钥不匹配问题。

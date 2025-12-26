# 支付宝SDK安装指南

## 问题说明

支付宝SDK (`com.alipay.sdk:alipay-sdk-java`) 不在公共Maven仓库中，无法直接从Maven Central或阿里云镜像下载。需要手动下载jar包并安装到本地Maven仓库。

## 解决方案

### 方案一：安装到本地Maven仓库（推荐）

#### 步骤1：下载支付宝SDK

1. 访问支付宝开放平台文档：https://opendocs.alipay.com/common/02kkv7
2. 下载Java版本的SDK jar包
3. 或者直接访问：https://opendocs.alipay.com/open/54/00y8k5
4. 下载 `alipay-sdk-java-4.38.195.ALL.jar` 文件

#### 步骤2：安装到本地Maven仓库

**重要**：`mvn install:install-file` 命令需要使用jar文件的**绝对路径**，或者在有POM文件的目录执行。

**方法一：在backend目录执行（推荐，最简单）**

**PowerShell命令（注意引号的使用）：**

```powershell
# 1. 将jar文件复制到backend目录（如果jar文件在项目根目录）
copy alipay-sdk-java-4.40.604.ALL.jar backend\

# 2. 进入backend目录
cd backend

# 3. 执行安装命令（PowerShell需要使用单引号或转义）
# 方式A：使用单引号包裹整个参数
mvn install:install-file '-Dfile=alipay-sdk-java-4.40.604.ALL.jar' '-DgroupId=com.alipay.sdk' '-DartifactId=alipay-sdk-java' '-Dversion=4.40.604.ALL' '-Dpackaging=jar'

# 方式B：使用反引号转义（推荐）
mvn install:install-file `-Dfile=alipay-sdk-java-4.40.604.ALL.jar `-DgroupId=com.alipay.sdk `-DartifactId=alipay-sdk-java `-Dversion=4.40.604.ALL `-Dpackaging=jar

# 方式C：使用双引号（如果路径没有空格）
mvn install:install-file -Dfile="alipay-sdk-java-4.40.604.ALL.jar" -DgroupId="com.alipay.sdk" -DartifactId="alipay-sdk-java" -Dversion="4.40.604.ALL" -Dpackaging="jar"

# 4. 安装完成后可以删除backend目录中的jar文件（可选）
del alipay-sdk-java-4.40.604.ALL.jar
```

**CMD命令（Windows命令提示符）：**

```cmd
# 1. 将jar文件复制到backend目录
copy alipay-sdk-java-4.40.604.ALL.jar backend\

# 2. 进入backend目录
cd backend

# 3. 执行安装命令（CMD不需要特殊处理）
mvn install:install-file -Dfile=alipay-sdk-java-4.40.604.ALL.jar -DgroupId=com.alipay.sdk -DartifactId=alipay-sdk-java -Dversion=4.40.604.ALL -Dpackaging=jar

# 4. 安装完成后可以删除backend目录中的jar文件（可选）
del alipay-sdk-java-4.40.604.ALL.jar
```

**方法二：使用-N参数跳过项目扫描**

```bash
# 在项目根目录执行，使用-N参数
mvn -N install:install-file -Dfile="D:\my project\java-project\shangdan\ShoppingMallSystem\alipay-sdk-java-4.40.604.ALL.jar" -DgroupId=com.alipay.sdk -DartifactId=alipay-sdk-java -Dversion=4.40.604.ALL -Dpackaging=jar
```

**方法三：使用绝对路径 + 在backend目录执行**

```bash
# 在backend目录执行，使用绝对路径指向项目根目录的jar文件
cd backend
mvn install:install-file -Dfile="D:\my project\java-project\shangdan\ShoppingMallSystem\alipay-sdk-java-4.40.604.ALL.jar" -DgroupId=com.alipay.sdk -DartifactId=alipay-sdk-java -Dversion=4.40.604.ALL -Dpackaging=jar
```

**方法四：使用临时目录（最可靠）**

```bash
# 1. 创建临时目录
mkdir temp-maven-install
cd temp-maven-install

# 2. 创建临时pom.xml
echo ^<?xml version="1.0" encoding="UTF-8"?^> > pom.xml
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"^> >> pom.xml
echo   ^<modelVersion^>4.0.0^</modelVersion^> >> pom.xml
echo   ^<groupId^>temp^</groupId^> >> pom.xml
echo   ^<artifactId^>temp^</artifactId^> >> pom.xml
echo   ^<version^>1.0^</version^> >> pom.xml
echo ^</project^> >> pom.xml

# 3. 复制jar文件到临时目录
copy ..\alipay-sdk-java-4.40.604.ALL.jar .

# 4. 执行安装
mvn install:install-file -Dfile=alipay-sdk-java-4.40.604.ALL.jar -DgroupId=com.alipay.sdk -DartifactId=alipay-sdk-java -Dversion=4.40.604.ALL -Dpackaging=jar

# 5. 返回上级目录并删除临时目录
cd ..
rmdir /s /q temp-maven-install
```

#### 步骤3：取消注释pom.xml中的依赖

安装完成后，在 `backend/pom.xml` 中取消注释支付宝SDK依赖：

```xml
<!-- 支付宝 SDK -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>${alipay.version}</version>
</dependency>
```

#### 步骤4：验证安装

```bash
cd backend
mvn dependency:tree | grep alipay
```

如果看到 `com.alipay.sdk:alipay-sdk-java:jar:4.38.195.ALL:compile`，说明安装成功。

### 方案二：使用本地jar包（临时方案）

如果不想安装到Maven仓库，可以将jar包放到项目的 `lib` 目录，然后在pom.xml中配置：

```xml
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>4.38.195.ALL</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/alipay-sdk-java-4.38.195.ALL.jar</systemPath>
</dependency>
```

**注意**：使用 `system` scope 的依赖在打包时不会自动包含，需要在打包插件中手动配置。

### 方案三：暂时注释依赖（当前方案）

如果暂时不需要支付宝功能（代码中显示功能还在开发中），可以暂时注释掉依赖，等需要时再安装。

当前 `pom.xml` 中已经注释掉了支付宝SDK依赖，项目可以正常编译运行。

## 验证安装

安装完成后，可以运行以下命令验证：

```bash
cd backend
mvn clean compile
```

如果没有报错，说明安装成功。

## 常见问题

### 1. 找不到jar文件

**错误信息**：
```
[ERROR] The specified file 'alipay-sdk-java-4.38.195.ALL.jar' does not exist
```

**解决方案**：
- 检查jar文件路径是否正确
- 使用绝对路径：`-Dfile=D:/path/to/alipay-sdk-java-4.38.195.ALL.jar`

### 2. 版本不匹配

**错误信息**：
```
Could not find artifact com.alipay.sdk:alipay-sdk-java:jar:4.38.195.ALL
```

**解决方案**：
- 确认安装的版本与pom.xml中配置的版本一致
- 检查本地仓库路径：`~/.m2/repository/com/alipay/sdk/alipay-sdk-java/4.38.195.ALL/`

### 3. 清除Maven缓存

如果安装后仍然找不到依赖，可以尝试清除Maven缓存：

```bash
# 删除本地仓库中的支付宝SDK
rm -rf ~/.m2/repository/com/alipay/sdk/

# 或者Windows
rd /s /q %USERPROFILE%\.m2\repository\com\alipay\sdk\

# 然后重新安装
```

## 相关文档

- 支付宝开放平台：https://open.alipay.com/
- 支付宝SDK文档：https://opendocs.alipay.com/common/02kkv7
- 支付宝Java SDK下载：https://opendocs.alipay.com/open/54/00y8k5

## 注意事项

1. **版本兼容性**：确保下载的SDK版本与项目要求的版本一致
2. **Java版本**：支付宝SDK需要Java 8+，项目使用Java 17，兼容性良好
3. **依赖冲突**：如果遇到依赖冲突，可以使用Maven的依赖排除功能
4. **生产环境**：生产环境部署时，确保服务器上也有对应的jar包或已安装到Maven仓库


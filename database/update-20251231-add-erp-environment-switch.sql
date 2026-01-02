-- 聚水潭ERP环境切换功能
-- 添加测试环境和生产环境配置切换
-- 日期：2025-12-31

-- 添加环境类型字段
ALTER TABLE `jushuitan_config`
ADD COLUMN `env_type` VARCHAR(20) NOT NULL DEFAULT 'production' COMMENT '环境类型：test=测试环境，production=生产环境' AFTER `api_url`;

-- 添加测试环境配置字段
ALTER TABLE `jushuitan_config`
ADD COLUMN `test_api_url` VARCHAR(200) DEFAULT 'https://dev-api.jushuitan.com/api/open/query.aspx' COMMENT '测试环境API地址' AFTER `env_type`,
ADD COLUMN `test_app_key` VARCHAR(100) DEFAULT 'b0b7d1db226d4216a3d58df9ffa2dde5' COMMENT '测试环境AppKey' AFTER `test_api_url`,
ADD COLUMN `test_app_secret` VARCHAR(200) DEFAULT '99c4cef262f34ca882975a7064de0b87' COMMENT '测试环境AppSecret' AFTER `test_app_key`;

-- 更新现有配置：将当前配置标记为生产环境，并设置测试环境默认值
UPDATE `jushuitan_config`
SET
  env_type = 'production',
  test_api_url = 'https://dev-api.jushuitan.com/api/open/query.aspx',
  test_app_key = 'b0b7d1db226d4216a3d58df9ffa2dde5',
  test_app_secret = '99c4cef262f34ca882975a7064de0b87'
WHERE id = 1;

-- 查看更新后的配置
SELECT id, env_type, api_url, app_key, test_api_url, test_app_key, enabled, auto_push_order
FROM `jushuitan_config`
WHERE id = 1;

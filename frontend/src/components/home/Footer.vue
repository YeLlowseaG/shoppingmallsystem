<template>
  <footer class="footer">
    <!-- 服务保障 - 已屏蔽 -->
    <!-- <div class="service-bar">
      <div class="container">
        <div class="service-item">
          <div class="icon-circle">正</div>
          <span>正品货源</span>
        </div>
        <div class="service-item">
          <span class="icon-text">✈</span>
          <span>海外直邮</span>
        </div>
        <div class="service-item">
          <span class="icon-text">👤</span>
          <span>本土客服</span>
        </div>
        <div class="service-item">
          <span class="icon-text">💰</span>
          <span>全球同价</span>
        </div>
        <el-button type="danger" class="contact-btn">查看详情</el-button>
      </div>
    </div> -->

    <!-- 底部信息 -->
    <div class="footer-info">
      <div class="container">
        <div class="footer-content">
          <!-- 左侧链接 -->
          <div class="links">
            <div class="link-group">
              <h4>购物指南</h4>
              <a href="#">体贴的售后服务</a>
              <a href="#">网站使用条款</a>
              <a href="#">网站免责声明</a>
              <a href="#">简单的购物流程</a>
            </div>
            <div class="link-group">
              <h4>新手上路</h4>
              <a href="#">顾客必读</a>
              <a href="#">会员等级折扣</a>
              <a href="#">订单的几种状态</a>
              <a href="#">积分奖励计划</a>
            </div>
            <div class="link-group">
              <h4>购物条款</h4>
              <a href="#">会员注册协议</a>
              <a href="#">隐私保护政策</a>
            </div>
            <div class="link-group">
              <h4>支付/配送方式</h4>
              <a href="#">支付方式</a>
              <a href="#">配送方式</a>
              <a href="#">订单何时出库？</a>
              <a href="#">网上支付小贴士</a>
            </div>
            <div class="link-group">
              <h4>备案号</h4>
              <a href="#">粤ICP备11098444号</a>
              <a href="#">粤深械网备202005070014</a>
              <a href="#">粤深食药监械经营备20151274号</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
// 暂时移除图标导入，如果图标不存在会导致页面空白
// import { LocationFilled, UserFilled, Wallet } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.footer {
  .service-bar {
    background: #e4393c;
    padding: 20px 0;

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 15px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .service-item {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #fff;

        .icon-circle {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          background: #fff;
          color: #e4393c;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 16px;
          font-weight: bold;
        }

        .icon {
          font-size: 24px;
          color: #fff;
        }

        .icon-text {
          font-size: 20px;
          color: #fff;
        }

        span {
          font-size: 14px;
          color: #fff;
        }
      }

      .contact-btn {
        padding: 12px 30px;
      }
    }
  }

  .footer-info {
    background: #333;
    color: #fff;
    padding: 40px 0 20px;

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 15px;
    }

    .footer-content {
      display: flex;
      justify-content: space-between;
      margin-bottom: 30px;

      .links {
        display: flex;
        gap: 40px;

        .link-group {
          h4 {
            font-size: 14px;
            font-weight: bold;
            margin-bottom: 15px;
            color: #fff;
          }

          a {
            display: block;
            color: #999;
            font-size: 12px;
            margin-bottom: 8px;
            text-decoration: none;
            transition: color 0.3s;

            &:hover {
              color: #e4393c;
            }
          }
        }
      }

    }
  }
}
</style>

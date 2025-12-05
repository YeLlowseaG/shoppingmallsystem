<template>
  <footer class="footer">
    <!-- 服务保障 -->
    <div class="service-bar">
      <div class="container">
        <div class="service-item">
          <el-icon class="icon"><Check /></el-icon>
          <span>正品保障</span>
        </div>
        <div class="service-item">
          <el-icon class="icon"><Location /></el-icon>
          <span>国内发货</span>
        </div>
        <div class="service-item">
          <el-icon class="icon"><Lock /></el-icon>
          <span>安全支付</span>
        </div>
        <div class="service-item">
          <el-icon class="icon"><Medal /></el-icon>
          <span>金牌服务</span>
        </div>
        <el-button type="danger" class="contact-btn">联系我们</el-button>
      </div>
    </div>

    <!-- 底部信息 -->
    <div class="footer-info">
      <div class="container">
        <div class="footer-content">
          <!-- 左侧链接 -->
          <div class="links">
            <div class="link-group">
              <h4>购物指南</h4>
              <a href="#">新手上路</a>
              <a href="#">会员介绍</a>
              <a href="#">购物流程</a>
              <a href="#">网站协议</a>
            </div>
            <div class="link-group">
              <h4>配送方式</h4>
              <a href="#">上门自提</a>
              <a href="#">快递运输</a>
              <a href="#">配送服务查询</a>
              <a href="#">配送费收取标准</a>
            </div>
            <div class="link-group">
              <h4>支付方式</h4>
              <a href="#">货到付款</a>
              <a href="#">在线支付</a>
              <a href="#">分期付款</a>
              <a href="#">邮局汇款</a>
            </div>
            <div class="link-group">
              <h4>售后服务</h4>
              <a href="#">售后政策</a>
              <a href="#">价格保护</a>
              <a href="#">退款说明</a>
              <a href="#">返修/退换货</a>
            </div>
            <div class="link-group">
              <h4>关于我们</h4>
              <a href="#">公司简介</a>
              <a href="#">加入我们</a>
              <a href="#">联系我们</a>
              <a href="#">关于我们</a>
            </div>
          </div>

          <!-- 右侧联系信息 -->
          <div class="contact">
            <h4>联系我们</h4>
            <p>服务热线：400-166-1683</p>
            <p>咨询热线：13049338552</p>
            <p>官方微信：扫描二维码关注我们</p>
          </div>
        </div>

        <!-- 版权信息 -->
        <div class="copyright">
          <p>© 2023JINGVO版权所有</p>
          <p>粤ICP备20123459号</p>
          <p>粤公网安备21231234号</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { Check, Location, Lock, Medal } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.footer {
  .service-bar {
    background: #f5f5f5;
    padding: 20px 0;
    border-top: 1px solid #eee;

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
        color: #666;

        .icon {
          font-size: 24px;
          color: #e4393c;
        }

        span {
          font-size: 14px;
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
        gap: 60px;

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

      .contact {
        h4 {
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 15px;
        }

        p {
          font-size: 13px;
          color: #999;
          margin-bottom: 8px;
        }
      }
    }

    .copyright {
      border-top: 1px solid #444;
      padding-top: 20px;
      text-align: center;

      p {
        font-size: 12px;
        color: #666;
        margin-bottom: 5px;
      }
    }
  }
}
</style>

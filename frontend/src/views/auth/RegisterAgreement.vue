<template>
  <div class="register-agreement">
    <div class="agreement-container">
      <h2>会员注册协议</h2>
      <div class="agreement-content">
        <p>尊敬的用户，欢迎您注册成为本网站用户。在注册前请您仔细阅读如下服务条款：</p>
        
        <h3>一、注册会员权限</h3>
        <p>只有注册会员才可以在商店上进行购物、订单查询等。请记住您的会员用户名。它是您在平台的唯一识别，您的任何投诉、问题、购买记录，均采用这个会员用户名处理。而且，您再次光临本站购物会有很大的方便，许多信息不必重新输入。</p>
        
        <h3>二、会员用户名唯一性</h3>
        <p>会员用户名可以是您便于记忆的任何代号比如网名。不过，在注册用户的时候，我们建议您务必输入您的真实信息，这样有利于准确发货、提供各项服务、必要时迅速与您联系。</p>
        
        <h3>三、信息安全保密承诺</h3>
        <p>拥有强大的后台保密程序，我们绝对保证您的注册信息的安全和保密性。</p>
        
        <h3>四、商品质量保证</h3>
        <p>我们保证所售商品的质量，不售假冒伪劣产品。如有质量问题，请联系客服。</p>
        
        <h3>五、发货和退换货政策</h3>
        <p>我们在收到您的汇款后，会及时发货。售出商品若有质量问题，除人为损坏外，未经穿着、洗涤无污损的商品，包换。</p>
        
        <h3>六、禁止转载商品资料声明</h3>
        <p>未经许可不得擅自转载、使用本站商品资料、图片，发现必究。</p>
      </div>
      
      <div class="agreement-actions">
        <el-button type="primary" @click="handleAgree">同意协议</el-button>
        <el-button @click="handleDisagree">不同意，返回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const handleAgree = () => {
  router.push('/register')
}

const handleDisagree = () => {
  router.back()
}
</script>

<style scoped>
.register-agreement {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40px 20px;
}

.agreement-container {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 28px;
}

.agreement-content {
  max-height: 500px;
  overflow-y: auto;
  margin-bottom: 30px;
  line-height: 1.8;
  color: #666;
}

.agreement-content h3 {
  color: #333;
  margin-top: 20px;
  margin-bottom: 10px;
}

.agreement-content p {
  margin-bottom: 15px;
}

.agreement-actions {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.agreement-actions .el-button {
  margin: 0 10px;
  min-width: 120px;
}
</style>





































































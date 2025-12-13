## 2025-12-13 - 修改支付记录页面操作按钮样式

### 修改内容
将支付记录页面（`/admin/finance/payment-record`）的操作按钮样式修改为与预存款交易记录页面一致。

### 修改文件

#### 前端
1. `admin-frontend/src/views/finance/PaymentRecord.vue` - 修改操作列按钮样式

### 具体修改

#### 1. 操作按钮样式调整
- 将"查看"按钮从 `type="primary" link size="small"` 改为 `type="primary" size="small"`（去掉 link 属性）
- 将按钮文字从"查看"改为"查看详情"
- 将"退款"按钮从 `type="danger" link size="small"` 改为 `type="danger" size="small"`（去掉 link 属性）
- 将操作列宽度从 150 调整为 280，与预存款交易记录页面保持一致

### 技术细节
- Element Plus 按钮：去掉 `link` 属性后，按钮显示为普通按钮样式而非文本链接样式
- 操作列宽度：从 150px 增加到 280px，为按钮提供更充足的显示空间
- 按钮文字：统一使用"查看详情"而非"查看"，提升用户体验

### 影响
- ✅ 操作按钮样式与预存款交易记录页面保持一致
- ✅ 按钮更加醒目，提升用户体验
- ✅ 操作列宽度增加，按钮显示更美观

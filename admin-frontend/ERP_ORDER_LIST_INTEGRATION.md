# 订单列表ERP功能集成说明

## 需要在 `admin-frontend/src/views/order/List.vue` 中进行以下修改：

### 1. 在 `<script>` 部分顶部添加 ERP API 导入

```typescript
import { pushOrderToErp, pullOrderLogistics } from '@/api/admin/erp'
```

### 2. 在表格中添加 ERP 状态列（在第 103 行后添加）

```vue
<!-- ERP同步状态列 -->
<el-table-column label="ERP状态" width="100" align="center">
  <template #default="{ row }">
    <el-tag v-if="row.erpSyncStatus === 1" type="success" size="small">已同步</el-tag>
    <el-tag v-else-if="row.erpSyncStatus === 2" type="danger" size="small">同步失败</el-tag>
    <el-tag v-else type="info" size="small">未同步</el-tag>
  </template>
</el-table-column>
```

### 3. 在操作列中添加 ERP 操作按钮（在第 131 行"备注"按钮后添加）

```vue
<!-- ERP操作按钮 -->
<el-dropdown trigger="click" size="small" style="margin-left: 5px;" v-if="row.status >= 1">
  <el-button size="small" type="success">
    ERP操作
    <el-icon class="el-icon--right"><arrow-down /></el-icon>
  </el-button>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item @click="handlePushToErp(row)" v-if="row.erpSyncStatus !== 1">
        推送到ERP
      </el-dropdown-item>
      <el-dropdown-item @click="handlePullLogistics(row)" v-if="row.erpSyncStatus === 1 && row.status === 1">
        拉取物流信息
      </el-dropdown-item>
      <el-dropdown-item @click="handleViewErpLogs(row)">
        查看同步日志
      </el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
```

### 4. 添加 ERP 操作方法（在 methods 或 setup 返回的对象中）

```typescript
// 推送订单到ERP
const handlePushToErp = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要将订单 ${row.orderNo} 推送到ERP系统吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await pushOrderToErp(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '推送成功')
      loadOrders() // 重新加载订单列表
    } else {
      ElMessage.error(res.message || '推送失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '推送失败')
    }
  }
}

// 从ERP拉取物流信息
const handlePullLogistics = async (row: any) => {
  try {
    const res = await pullOrderLogistics(row.id)
    if (res.code === 200) {
      ElMessage.success(res.message || '物流信息拉取成功')
      loadOrders() // 重新加载订单列表
    } else {
      ElMessage.error(res.message || '拉取失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '拉取失败')
  }
}

// 查看ERP同步日志
const handleViewErpLogs = (row: any) => {
  // 跳转到ERP同步日志页面，并传入订单ID作为筛选条件
  router.push({
    path: '/erp/order-sync',
    query: { orderId: row.id }
  })
}
```

### 5. 确保导入必要的组件（如果还没有）

```typescript
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
```

### 6. 修改操作列宽度

将第 104 行的操作列宽度从 380 改为 480：

```vue
<el-table-column label="操作" width="480" fixed="right">
```

## 注意事项

1. 确保后端已经在 Order 实体和 VO 中添加了 `erpSyncStatus`、`erpSyncTime`、`erpOrderId` 等字段
2. ERP 操作按钮只在订单已支付（status >= 1）时显示
3. "推送到ERP"按钮只在订单未同步时显示
4. "拉取物流信息"按钮只在订单已同步且未发货（status === 1）时显示
5. 所有用户都可以查看同步日志

## 完成后测试

1. 在订单列表查看 ERP 状态列是否正确显示
2. 点击"ERP操作"下拉菜单，测试各个功能按钮
3. 确认推送和拉取功能正常工作
4. 验证同步日志跳转是否正确

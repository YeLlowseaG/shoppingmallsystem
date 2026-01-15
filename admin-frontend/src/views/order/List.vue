<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="买家姓名">
          <el-input v-model="searchForm.buyerName" placeholder="请输入买家姓名" clearable />
        </el-form-item>
        <el-form-item label="买家用户名">
          <el-input v-model="searchForm.buyerUsername" placeholder="请输入买家用户名" clearable />
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="searchForm.recipientName" placeholder="请输入收货人姓名" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="全部" :value="undefined" />
            <el-option label="待付款" :value="0" />
            <el-option label="已付款未发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已退款" :value="5" />
            <el-option label="已退货" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="searchForm.startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            placeholder="选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单状态标签页 -->
      <div class="order-tabs">
        <div
          v-for="tab in orderTabs"
          :key="tab.value === undefined ? 'all' : tab.value"
          :class="['tab-item', { active: activeTab === tab.value }]"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>

      <!-- 订单列表 -->
      <el-table :data="orderList" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="handleView(row)" style="cursor: pointer;">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家姓名" width="100" />
        <el-table-column prop="buyerUsername" label="买家用户名" width="100" />
        <el-table-column prop="recipientName" label="收货人" width="100" />
        <el-table-column prop="description" label="订单描述" min-width="210" show-overflow-tooltip />
        <el-table-column prop="orderDate" label="下单日期" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.orderDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- ERP同步状态列 -->
        <el-table-column label="ERP状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.erpSyncStatus === 1" type="success" size="small">已同步</el-tag>
            <el-tag v-else-if="row.erpSyncStatus === 2" type="danger" size="small">同步失败</el-tag>
            <el-tag v-else type="info" size="small">未同步</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewLogistics(row)">物流信息</el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 2 || row.status === 3"
              type="danger"
              size="small"
              @click="handleRefund(row)"
            >
              退款
            </el-button>
            <el-button type="info" size="small" @click="handleRemark(row)">备注</el-button>
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
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="900px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单日期">{{ formatDateTime(currentOrder.orderDate) }}</el-descriptions-item>
        <el-descriptions-item label="买家姓名">{{ currentOrder.buyerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="买家用户名">{{ currentOrder.buyerUsername || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(currentOrder.status)">
            {{ currentOrder.statusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="商品总金额">¥{{ currentOrder.totalProductAmount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ currentOrder.shippingFee.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="商品数量">{{ currentOrder.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="订单备注" :span="2">{{ currentOrder.orderNotes || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 商品列表 -->
      <el-divider>商品信息</el-divider>
      <el-table :data="currentOrder?.items" border style="margin-top: 20px">
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.image)"
              :alt="row.name"
              fit="cover"
              style="width: 60px; height: 60px;"
              :preview-src-list="[getImageUrl(row.image)]"
              :initial-index="0"
              preview-teleported
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column label="商品名称" width="300">
          <template #default="{ row }">
            <div>{{ row.name }}</div>
            <div v-if="formatSpecText(row.specCombination)" class="sku-spec-text">
              {{ formatSpecText(row.specCombination) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="已退款/可退款" width="120">
          <template #default="{ row }">
            <div v-if="row.refundedQuantity !== undefined && row.availableRefundQuantity !== undefined">
              <div style="color: #e4393c; font-weight: bold;">已退：{{ row.refundedQuantity || 0 }}</div>
              <div style="color: #409eff;">可退：{{ row.availableRefundQuantity || 0 }}</div>
            </div>
            <div v-else>-</div>
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120">
          <template #default="{ row }">
            ¥{{ row.subtotal.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 操作按钮 -->
      <div style="margin-top: 20px; text-align: right;" v-if="currentOrder">
        <el-button
          v-if="currentOrder.status === 1 || currentOrder.status === 2 || currentOrder.status === 3"
          type="danger"
          @click="handleRefundFromDetail"
        >
          申请退款
        </el-button>
      </div>

      <!-- 退款记录 -->
      <el-divider v-if="refundList.length > 0">退款记录</el-divider>
      <div v-if="refundList.length > 0" style="margin-top: 20px;">
        <div v-for="refund in refundList" :key="refund.id" style="margin-bottom: 20px; padding: 15px; background: #f9f9f9; border: 1px solid #e5e5e5; border-radius: 4px;">
          <div style="margin-bottom: 15px;">
            <div style="display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 10px; font-size: 14px;">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="color: #666;">退款单号:</span>
                <span style="color: #333;">{{ refund.refundNo }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="color: #666;">退款金额:</span>
                <span style="color: #e4393c; font-weight: bold; font-size: 16px;">¥{{ refund.refundAmount.toFixed(2) }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="color: #666;">退款类型:</span>
                <span style="color: #333;">{{ refund.refundTypeText }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="color: #666;">退款状态:</span>
                <el-tag :type="getRefundStatusTagType(refund.refundStatus)" size="small">
                  {{ refund.refundStatusText }}
                </el-tag>
              </div>
            </div>
            <div style="display: flex; flex-wrap: wrap; gap: 20px; font-size: 14px;">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="color: #666;">退款时间:</span>
                <span style="color: #333;">{{ refund.refundTime ? formatDateTime(refund.refundTime) : '-' }}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="color: #666;">退款原因:</span>
                <span style="color: #333;">{{ refund.refundReason || '-' }}</span>
              </div>
            </div>
          </div>
          <div v-if="refund.refundItems && refund.refundItems.length > 0" style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #e5e5e5;">
            <div style="font-size: 14px; font-weight: bold; color: #333; margin-bottom: 10px;">退款明细:</div>
            <el-table :data="refund.refundItems" border size="small">
              <el-table-column prop="productCode" label="商品编码" width="120" />
              <el-table-column label="商品名称" min-width="250">
                <template #default="{ row }">
                  <div>{{ row.productName }}</div>
                  <div v-if="formatSpecText(row.specCombination)" style="margin-top: 5px; font-size: 12px; color: #999;">
                    规格：{{ formatSpecText(row.specCombination) }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="refundQuantity" label="退款数量" width="100" align="center" />
              <el-table-column prop="refundPrice" label="退款单价" width="120" align="right">
                <template #default="{ row }">
                  ¥{{ row.refundPrice.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="refundSubtotal" label="退款小计" width="120" align="right">
                <template #default="{ row }">
                  <span style="color: #e4393c; font-weight: bold;">¥{{ row.refundSubtotal.toFixed(2) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>

      <!-- 收货人信息 -->
      <el-divider>收货人信息</el-divider>
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="收货人姓名">{{ currentOrder.recipientInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.recipientInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="配送地区">{{ currentOrder.recipientInfo.region }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.recipientInfo.address }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ currentOrder.recipientInfo.shippingMethod }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.recipientInfo.paymentMethod }}</el-descriptions-item>
      </el-descriptions>

      <!-- 补单操作区域 -->
      <el-divider>支付补单</el-divider>
      <div v-if="currentOrder" style="padding: 20px; background: #f5f7fa; border-radius: 4px; margin-top: 20px;">
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 15px;"
        >
          <template #title>
            <div style="font-size: 14px; line-height: 1.6;">
              <div style="font-weight: bold; margin-bottom: 8px;">什么情况下需要补单？</div>
              <div style="margin-left: 0;">
                <div>• 用户已支付成功，但订单状态仍显示"待付款"</div>
                <div>• 支付回调丢失，导致订单状态未更新</div>
                <div>• 支付宝/微信支付成功，但系统未收到支付通知</div>
                <div style="color: #e6a23c; margin-top: 5px;">注意：补单功能会自动查询第三方支付平台，确认支付状态后更新订单状态</div>
              </div>
            </div>
          </template>
        </el-alert>
        <div style="text-align: right;">
          <el-button
            type="primary"
            :loading="syncPaymentLoading"
            :disabled="!currentOrder || currentOrder.status !== 0"
            @click="handleSyncPayment"
          >
            <el-icon><Refresh /></el-icon>
            执行补单
          </el-button>
        </div>
        <div v-if="currentOrder.status !== 0" style="margin-top: 10px; color: #909399; font-size: 12px; text-align: right;">
          提示：只有"待付款"状态的订单才能执行补单操作
        </div>
      </div>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="500px">
      <el-form :model="shipForm" :rules="shipRules" ref="shipFormRef" label-width="100px">
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-input v-model="shipForm.logisticsCompany" placeholder="请输入物流公司" />
        </el-form-item>
        <el-form-item label="物流单号" prop="logisticsNo">
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleShipSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 备注对话框 -->
    <el-dialog v-model="remarkDialogVisible" title="订单备注" width="500px">
      <el-form :model="remarkForm" ref="remarkFormRef" label-width="100px">
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="remarkForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRemarkSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog v-model="refundDialogVisible" title="订单退款" width="900px">
      <div v-if="currentOrder">
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <div>订单号：{{ currentOrder.orderNo }}</div>
          <div>订单金额：¥{{ currentOrder.totalProductAmount.toFixed(2) }}（不含运费）</div>
          <div style="color: #e4393c; margin-top: 5px;">注意：退款金额不包含运费，只退还商品金额</div>
        </el-alert>

        <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="120px">
          <el-form-item label="退款原因" prop="refundReason">
            <el-input
              v-model="refundForm.refundReason"
              type="textarea"
              :rows="3"
              placeholder="请输入退款原因"
            />
          </el-form-item>
        </el-form>

        <el-divider>选择退款商品</el-divider>
        <el-table 
          :data="refundItemList" 
          border 
          style="margin-top: 20px"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" :selectable="checkSelectable" />
          <el-table-column label="商品编码" prop="productCode" width="120" />
          <el-table-column label="商品名称" min-width="250">
            <template #default="{ row }">
              <div>{{ row.name }}</div>
              <div v-if="formatSpecText(row.specCombination)" class="sku-spec-text">
                {{ formatSpecText(row.specCombination) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100">
            <template #default="{ row }">
              ¥{{ row.price.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="订单数量" width="100" align="center">
            <template #default="{ row }">
              {{ row.quantity }}
            </template>
          </el-table-column>
          <el-table-column label="已退款" width="80" align="center">
            <template #default="{ row }">
              {{ row.refundedQuantity || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="可退款" width="80" align="center">
            <template #default="{ row }">
              <span style="color: #409eff;">{{ row.availableRefundQuantity || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="退款数量" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.refundQuantity"
                :min="1"
                :max="row.availableRefundQuantity || 0"
                :disabled="!row.selected"
                size="small"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="退款小计" width="120">
            <template #default="{ row }">
              <span v-if="row.selected && row.refundQuantity">
                ¥{{ (row.price * row.refundQuantity).toFixed(2) }}
              </span>
              <span v-else style="color: #ccc;">-</span>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 20px; text-align: right; font-size: 16px; font-weight: bold; color: #e4393c;">
          退款总金额：¥{{ totalRefundAmount.toFixed(2) }}
        </div>
      </div>
      <template #footer>
        <el-button @click="refundDialogVisible = false" :disabled="refundLoading">取消</el-button>
        <el-button type="danger" @click="handleRefundSubmit" :loading="refundLoading" :disabled="totalRefundAmount <= 0 || refundLoading">确认退款</el-button>
      </template>
    </el-dialog>

    <!-- 物流信息对话框 -->
    <el-dialog v-model="logisticsDialogVisible" title="物流信息" width="600px">
      <div v-if="currentLogisticsOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentLogisticsOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentLogisticsOrder.recipientName }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div v-if="currentLogisticsOrder.logistics">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="承运公司">{{ currentLogisticsOrder.logistics.carrier }}</el-descriptions-item>
            <el-descriptions-item label="发货日期">{{ currentLogisticsOrder.logistics.shipDate }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ currentLogisticsOrder.logistics.shipTime }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">
              <span>{{ currentLogisticsOrder.logistics.trackingNo }}</span>
              <el-button
                type="text"
                size="small"
                style="margin-left: 10px;"
                @click="handleCopyTrackingNo(currentLogisticsOrder.logistics!.trackingNo)"
              >
                复制
              </el-button>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else style="text-align: center; padding: 40px; color: #999;">
          暂无物流信息
        </div>
      </div>
      <template #footer>
        <el-button @click="logisticsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Picture, ArrowDown, Refresh } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils'
import {
  getOrderList,
  getOrderDetail,
  cancelOrder,
  shipOrder,
  addOrderRemark,
  refundOrder,
  getOrderRefundList,
  syncPaymentStatus
} from '@/api/admin/order'
import type { OrderListVO, OrderDetailVO, OrderRefundRequestDTO, OrderRefundVO } from '@/api/admin/order'
import { pushOrderToErp, pullOrderLogistics } from '@/api/admin/erp'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const orderList = ref<OrderListVO[]>([])

const searchForm = reactive({
  orderNo: '',
  buyerName: '',
  buyerUsername: '',
  recipientName: '',
  orderStatus: undefined as number | undefined,
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const shipDialogVisible = ref(false)
const remarkDialogVisible = ref(false)
const logisticsDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const refundLoading = ref(false)
const syncPaymentLoading = ref(false)
const currentOrder = ref<OrderDetailVO | null>(null)
const currentLogisticsOrder = ref<OrderListVO | null>(null)
const currentRefundOrder = ref<OrderListVO | null>(null)
const refundList = ref<OrderRefundVO[]>([])

const shipForm = reactive({
  logisticsCompany: '',
  logisticsNo: ''
})

const remarkForm = reactive({
  remark: ''
})

const refundForm = reactive({
  refundReason: ''
})

const refundItemList = ref<Array<{
  id: number
  productCode: string
  name: string
  specCombination?: string
  price: number
  quantity: number
  refundedQuantity?: number
  availableRefundQuantity?: number
  selected: boolean
  refundQuantity: number
}>>([])

const shipFormRef = ref<FormInstance>()
const remarkFormRef = ref<FormInstance>()
const refundFormRef = ref<FormInstance>()

const refundRules: FormRules = {
  refundReason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}

const shipRules: FormRules = {
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  logisticsNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

// 订单状态标签页配置
const orderTabs = [
  { label: '全部订单', value: undefined },
  { label: '待付款', value: 0 },
  { label: '已付款未发货', value: 1 },
  { label: '已发货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '已退款', value: 5 },
  { label: '已退货', value: 6 }
]

const activeTab = ref<number | undefined>(undefined)


// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    const response = await getOrderList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      buyerName: searchForm.buyerName || undefined,
      buyerUsername: searchForm.buyerUsername || undefined,
      recipientName: searchForm.recipientName || undefined,
      orderStatus: searchForm.orderStatus,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    orderList.value = response.records || []
    pagination.total = response.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (value: number | undefined) => {
  if (activeTab.value === value) {
    return // 如果点击的是当前标签，不执行任何操作
  }
  activeTab.value = value
  searchForm.orderStatus = value
  pagination.page = 1
  loadOrderList()
}

// 搜索
const handleSearch = () => {
  // 如果搜索时没有指定状态，使用当前tab的状态
  if (searchForm.orderStatus === undefined && activeTab.value !== undefined) {
    searchForm.orderStatus = activeTab.value
  }
  // 同步tab状态
  activeTab.value = searchForm.orderStatus
  pagination.page = 1
  loadOrderList()
}

// 重置
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.buyerName = ''
  searchForm.buyerUsername = ''
  searchForm.recipientName = ''
  searchForm.orderStatus = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  activeTab.value = undefined
  handleSearch()
}

// 查看详情
const handleView = async (row: OrderListVO) => {
  try {
    const order = await getOrderDetail(row.orderNo)
    currentOrder.value = order
    detailDialogVisible.value = true
    // 加载退款记录
    await loadRefundList(row.orderNo)
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 加载退款记录列表
const loadRefundList = async (orderNo: string) => {
  try {
    const refunds = await getOrderRefundList(orderNo)
    refundList.value = refunds || []
  } catch (error: any) {
    // 如果获取退款记录失败，不影响订单详情显示，只记录错误
    console.error('加载退款记录失败:', error)
    refundList.value = []
  }
}

// 获取退款状态标签类型
const getRefundStatusTagType = (status: number) => {
  switch (status) {
    case 3:
      return 'warning' // 退款中
    case 4:
      return 'success' // 退款成功
    case 5:
      return 'danger' // 退款失败
    default:
      return 'info'
  }
}

// 查看物流信息
const handleViewLogistics = (row: OrderListVO) => {
  currentLogisticsOrder.value = row
  logisticsDialogVisible.value = true
}

// 复制物流单号
const handleCopyTrackingNo = (trackingNo: string) => {
  navigator.clipboard.writeText(trackingNo).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 执行补单
const handleSyncPayment = async () => {
  if (!currentOrder.value) {
    ElMessage.error('订单信息不存在')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要对订单 ${currentOrder.value.orderNo} 执行补单操作吗？\n\n系统将查询第三方支付平台，确认支付状态后更新订单状态。`,
      '确认补单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    syncPaymentLoading.value = true
    try {
      await syncPaymentStatus(currentOrder.value.orderNo)
      ElMessage.success('补单成功，订单状态已更新')
      // 重新加载订单详情
      const order = await getOrderDetail(currentOrder.value.orderNo)
      currentOrder.value = order
      // 刷新订单列表
      loadOrderList()
    } finally {
      syncPaymentLoading.value = false
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '补单失败')
    }
  }
}

// 取消订单
const handleCancel = async (row: OrderListVO) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后库存将自动恢复。', '提示', {
      type: 'warning'
    })
    await cancelOrder(row.orderNo)
    ElMessage.success('取消成功')
    loadOrderList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 发货
const handleShip = (row: OrderListVO) => {
  currentOrder.value = { orderNo: row.orderNo } as any
  shipForm.logisticsCompany = ''
  shipForm.logisticsNo = ''
  shipDialogVisible.value = true
}

// 提交发货
const handleShipSubmit = async () => {
  if (!shipFormRef.value || !currentOrder.value) return

  await shipFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await shipOrder(
          currentOrder.value!.orderNo,
          shipForm.logisticsCompany,
          shipForm.logisticsNo
        )
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        loadOrderList()
      } catch (error: any) {
        ElMessage.error(error.message || '发货失败')
      }
    }
  })
}

// 备注
const handleRemark = async (row: OrderListVO) => {
  try {
    const order = await getOrderDetail(row.orderNo)
    currentOrder.value = order
    remarkForm.remark = order.orderNotes || ''
    remarkDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 提交备注
const handleRemarkSubmit = async () => {
  if (!currentOrder.value) return

  try {
    await addOrderRemark(currentOrder.value.orderNo, remarkForm.remark)
    ElMessage.success('备注添加成功')
    remarkDialogVisible.value = false
    loadOrderList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 退款（从列表）
const handleRefund = async (row: OrderListVO) => {
  try {
    const order = await getOrderDetail(row.orderNo)
    currentRefundOrder.value = row
    currentOrder.value = order
    initRefundDialog(order)
    refundDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  }
}

// 退款（从详情对话框）
const handleRefundFromDetail = async () => {
  if (!currentOrder.value) return
  initRefundDialog(currentOrder.value)
  refundDialogVisible.value = true
}

// 初始化退款对话框
const initRefundDialog = (order: OrderDetailVO) => {
  refundForm.refundReason = ''
  refundItemList.value = (order.items || []).map(item => ({
    id: item.id,
    productCode: item.productCode,
    name: item.name,
    specCombination: item.specCombination,
    price: item.price,
    quantity: item.quantity,
    refundedQuantity: item.refundedQuantity || 0,
    availableRefundQuantity: item.availableRefundQuantity || 0,
    selected: false,
    refundQuantity: 0
  }))
}

// 检查商品是否可选（可退款数量>0）
const checkSelectable = (row: any) => {
  return (row.availableRefundQuantity || 0) > 0
}

// 计算总退款金额
const totalRefundAmount = computed(() => {
  return refundItemList.value
    .filter(item => item.selected && item.refundQuantity > 0)
    .reduce((sum, item) => sum + (item.price * item.refundQuantity), 0)
})

// 监听表格选择变化
const handleSelectionChange = (selection: any[]) => {
  refundItemList.value.forEach(item => {
    item.selected = selection.some(sel => sel.id === item.id)
    if (!item.selected) {
      item.refundQuantity = 0
    } else if (item.refundQuantity === 0) {
      // 默认设置为可退款数量
      item.refundQuantity = item.availableRefundQuantity || 0
    }
  })
}

// 提交退款
const handleRefundSubmit = async () => {
  if (!refundFormRef.value || !currentOrder.value || refundLoading.value) return

  await refundFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 检查是否选择了退款商品
    const selectedItems = refundItemList.value.filter(item => item.selected && item.refundQuantity > 0)
    if (selectedItems.length === 0) {
      ElMessage.warning('请选择要退款的商品并设置退款数量')
      return
    }

    // 验证退款数量
    for (const item of selectedItems) {
      if (item.refundQuantity <= 0) {
        ElMessage.warning(`商品【${item.name}】的退款数量必须大于0`)
        return
      }
      if (item.refundQuantity > (item.availableRefundQuantity || 0)) {
        ElMessage.warning(`商品【${item.name}】的退款数量不能超过可退款数量`)
        return
      }
    }

    if (!currentOrder.value) {
      ElMessage.error('订单信息不存在')
      return
    }

    try {
      const refundDTO: OrderRefundRequestDTO = {
        orderNo: currentOrder.value.orderNo,
        refundReason: refundForm.refundReason,
        refundItems: selectedItems.map(item => ({
          orderItemId: item.id,
          refundQuantity: item.refundQuantity
        }))
      }

      await ElMessageBox.confirm(
        `确定要退款吗？退款金额：¥${totalRefundAmount.value.toFixed(2)}（不含运费）`,
        '确认退款',
        {
          type: 'warning',
          confirmButtonText: '确定退款',
          cancelButtonText: '取消'
        }
      )

      // 设置loading状态
      refundLoading.value = true
      try {
        const refundNo = await refundOrder(currentOrder.value.orderNo, refundDTO)
        ElMessage.success(`退款成功，退款单号：${refundNo}`)
        refundDialogVisible.value = false
        loadOrderList()
        // 如果详情对话框打开，重新加载订单详情和退款记录
        if (detailDialogVisible.value && currentOrder.value) {
          const order = await getOrderDetail(currentOrder.value.orderNo)
          currentOrder.value = order
          await loadRefundList(order.orderNo)
        }
      } finally {
        // 无论成功还是失败，都要重置loading状态
        refundLoading.value = false
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '退款失败')
      }
      // 如果是取消操作，不需要重置loading（因为loading还没设置）
      // 如果是其他错误，loading已经在finally中重置了
    }
  })
}

// 分页
const handleSizeChange = () => {
  loadOrderList()
}

const handlePageChange = () => {
  loadOrderList()
}

// 获取图片URL（处理相对路径）
const getImageUrl = (url: string | undefined): string => {
  if (!url) return ''
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径（以 / 开头），使用当前域名而不是API基础URL
  // 因为图片资源应该通过当前域名访问，而不是API服务器
  if (url.startsWith('/')) {
    // 使用 window.location.origin 获取当前域名
    return window.location.origin + url
  }
  return url
}

// 获取状态标签类型
const getStatusTagType = (status: number): string => {
  const typeMap: Record<number, string> = {
    0: 'danger', // 待付款
    1: 'warning', // 已付款未发货
    2: 'primary', // 已发货
    3: 'success', // 已完成
    4: 'info', // 已取消
    5: 'info', // 已退款
    6: 'info' // 已退货
  }
  return typeMap[status] || 'info'
}

// 将规格组合JSON转换为可读文本
const formatSpecText = (specCombination: string | undefined): string => {
  if (!specCombination) return ''
  try {
    const specs = JSON.parse(specCombination)
    return Object.entries(specs)
      .map(([key, value]) => `${key}:${value}`)
      .join(' / ')
  } catch (e) {
    return ''
  }
}

// ==================== ERP操作方法 ====================

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

    // 由于响应拦截器的逻辑，成功时返回的是 data 字段的内容
    const result = await pushOrderToErp(row.id)
    ElMessage.success(result || '推送成功')
    loadOrderList() // 重新加载订单列表
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
      loadOrderList() // 重新加载订单列表
    } else {
      ElMessage.error(res.message || '拉取失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '拉取失败')
  }
}


// 监听搜索表单中的订单状态变化，同步到tab
watch(() => searchForm.orderStatus, (newStatus) => {
  activeTab.value = newStatus
})

// 初始化
onMounted(() => {
  loadOrderList()
})
</script>

<style scoped lang="scss">
.order-management {
  .search-form {
    margin-bottom: 20px;
  }

  // 订单状态标签页样式
  .order-tabs {
    display: flex;
    gap: 0;
    border-bottom: 2px solid #e5e5e5;
    margin-bottom: 20px;

    .tab-item {
      padding: 12px 20px;
      font-size: 14px;
      color: #666;
      cursor: pointer;
      border-bottom: 2px solid transparent;
      margin-bottom: -2px;
      transition: all 0.3s;

      &:hover {
        color: #409eff;
      }

      &.active {
        color: #409eff;
        font-weight: bold;
        border-bottom-color: #409eff;
      }
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

// 图片占位符样式
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

// SKU规格文本样式
.sku-spec-text {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>


<template>
  <div class="scheduled-task-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>定时任务管理</span>
          <el-button type="primary" @click="handleRefresh">刷新</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="任务名称">
          <el-input v-model="queryForm.taskName" placeholder="请输入任务名称" clearable style="width: 200px" />
        </el-form-item>

        <el-form-item label="任务状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="运行中" :value="1" />
            <el-option label="已停止" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 任务列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" width="200" />
        <el-table-column prop="taskGroup" label="任务组" width="150" />
        <el-table-column prop="cronExpression" label="Cron表达式" width="180" />
        <el-table-column prop="beanName" label="Bean名称" width="200" show-overflow-tooltip />
        <el-table-column prop="methodName" label="方法名称" width="150" />
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecuteTime" label="上次执行时间" width="180" />
        <el-table-column prop="nextExecuteTime" label="下次执行时间" width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停止' : '启动' }}
            </el-button>
            <el-button link type="primary" @click="handleExecuteNow(row)">立即执行</el-button>
            <el-button link type="info" @click="handleViewLog(row)">查看日志</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

      <!-- 日志对话框 -->
    <el-dialog v-model="logVisible" title="任务执行日志" width="80%">
      <el-table :data="logData" v-loading="logLoading" border stripe>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" width="200" />
        <el-table-column prop="executeTypeDesc" label="执行类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.executeType === 1 ? 'info' : 'warning'">
              {{ row.executeTypeDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeStatusDesc" label="执行状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.executeStatus === 1 ? 'success' : 'danger'">
              {{ row.executeStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="duration" label="执行耗时(ms)" width="120" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
      </el-table>

      <!-- 日志分页 -->
      <el-pagination
        v-model:current-page="logQuery.pageNum"
        v-model:page-size="logQuery.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="logTotal"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadLogData"
        @current-change="loadLogData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScheduledTasks, toggleTaskStatus, executeTaskNow, getTaskLogs } from '@/api/admin/system'

const loading = ref(false)
const logLoading = ref(false)
const logVisible = ref(false)
const currentTaskId = ref<number | null>(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 20,
  taskName: undefined,
  status: undefined
})

const tableData = ref([])
const total = ref(0)

const logQuery = reactive({
  pageNum: 1,
  pageSize: 20,
  taskId: undefined
})

const logData = ref([])
const logTotal = ref(0)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getScheduledTasks(queryForm)
    tableData.value = data?.records || []
    total.value = data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.taskName = undefined
  queryForm.status = undefined
  queryForm.pageNum = 1
  loadData()
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.status === 1 ? '停止' : '启动'
    await ElMessageBox.confirm(
      `确定要${action}任务 "${row.taskName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await toggleTaskStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 立即执行
const handleExecuteNow = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要立即执行任务 "${row.taskName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await executeTaskNow(row.id)
    ElMessage.success('任务已提交执行')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '执行失败')
    }
  }
}

// 查看日志
const handleViewLog = (row: any) => {
  currentTaskId.value = row.id
  logQuery.taskId = row.id
  logQuery.pageNum = 1
  logVisible.value = true
  loadLogData()
}

// 加载日志数据
const loadLogData = async () => {
  if (!currentTaskId.value) return
  
  logLoading.value = true
  try {
    const data = await getTaskLogs(logQuery)
    logData.value = data?.records || []
    logTotal.value = data?.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载日志失败')
  } finally {
    logLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.scheduled-task-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>


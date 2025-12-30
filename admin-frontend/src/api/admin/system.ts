import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// ==================== 定时任务管理 ====================

/**
 * 定时任务查询DTO
 */
export interface ScheduledTaskQueryDTO {
  pageNum?: number
  pageSize?: number
  taskName?: string
  status?: number
}

/**
 * 定时任务VO
 */
export interface ScheduledTaskVO {
  id: number
  taskName: string
  taskGroup: string
  cronExpression: string
  beanName: string
  methodName: string
  status: number
  statusDesc?: string
  lastExecuteTime?: string
  nextExecuteTime?: string
  description?: string
}

/**
 * 分页查询定时任务列表
 */
export function getScheduledTasks(params: ScheduledTaskQueryDTO) {
  return request({
    url: '/api/admin/system/scheduled-task',
    method: 'get',
    params
  })
}

/**
 * 切换任务状态（启动/停止）
 */
export function toggleTaskStatus(taskId: number, status: number) {
  return request({
    url: `/api/admin/system/scheduled-task/${taskId}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 立即执行任务
 */
export function executeTaskNow(taskId: number) {
  return request({
    url: `/api/admin/system/scheduled-task/${taskId}/execute`,
    method: 'post'
  })
}

/**
 * 任务执行日志查询DTO
 */
export interface TaskLogQueryDTO {
  pageNum?: number
  pageSize?: number
  taskId?: number
}

/**
 * 任务执行日志VO
 */
export interface TaskLogVO {
  id: number
  taskId: number
  taskName: string
  taskGroup?: string
  beanName?: string
  methodName?: string
  executeStatus: number
  executeStatusDesc?: string
  startTime: string
  endTime?: string
  duration?: number
  errorMessage?: string
  executeType?: number
  executeTypeDesc?: string
  createTime?: string
}

/**
 * 分页查询任务执行日志
 */
export function getTaskLogs(params: TaskLogQueryDTO) {
  return request({
    url: '/api/admin/system/scheduled-task/log',
    method: 'get',
    params
  })
}


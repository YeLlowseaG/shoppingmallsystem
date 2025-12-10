import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 公告信息
 */
export interface Announcement {
  id: number
  title: string
  content: string // HTML内容
  publishDate: string // 发布日期
  updateTime?: string // 最后更新时间
  sort: number
  status: number
  createTime?: string
}

/**
 * 分页响应
 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

/**
 * 获取公告列表（分页）
 */
export const getAnnouncementList = (params: {
  pageNum?: number
  pageSize?: number
}): Promise<PageResult<Announcement>> => {
  return request.get('/api/common/announcement/list', { params })
}

/**
 * 根据ID获取公告详情
 */
export const getAnnouncementById = (id: number): Promise<Announcement> => {
  return request.get(`/api/common/announcement/${id}`)
}

/**
 * 获取上一篇公告
 */
export const getPrevAnnouncement = (id: number): Promise<Announcement | null> => {
  return request.get(`/api/common/announcement/${id}/prev`).then(data => data || null).catch(() => null)
}

/**
 * 获取下一篇公告
 */
export const getNextAnnouncement = (id: number): Promise<Announcement | null> => {
  return request.get(`/api/common/announcement/${id}/next`).then(data => data || null).catch(() => null)
}


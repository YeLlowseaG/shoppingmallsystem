/**
 * 公告管理API
 */

import request from '@/utils/request'

// 公告DTO
export interface AnnouncementDTO {
  id?: number
  title: string
  content: string
  images?: string[]
  publishDate: string
  sort?: number
  status?: number
}

// 公告VO
export interface AnnouncementVO {
  id: number
  title: string
  content: string
  images: string[]
  publishDate: string
  updateTime: string
  sort: number
  status: number
  createTime: string
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页查询公告列表
 */
export const getAnnouncementPage = (
  pageNum: number,
  pageSize: number,
  title?: string,
  status?: number
): Promise<PageResponse<AnnouncementVO>> => {
  return request.get('/api/admin/announcement/list', {
    params: { pageNum, pageSize, title, status }
  })
}

/**
 * 根据ID获取公告信息
 */
export const getAnnouncementById = (id: number): Promise<AnnouncementVO> => {
  return request.get(`/api/admin/announcement/${id}`)
}

/**
 * 创建公告
 */
export const createAnnouncement = (data: AnnouncementDTO): Promise<void> => {
  return request.post('/api/admin/announcement', data)
}

/**
 * 更新公告
 */
export const updateAnnouncement = (id: number, data: AnnouncementDTO): Promise<void> => {
  return request.put(`/api/admin/announcement/${id}`, data)
}

/**
 * 删除公告
 */
export const deleteAnnouncement = (id: number): Promise<void> => {
  return request.delete(`/api/admin/announcement/${id}`)
}

/**
 * 更新公告状态
 */
export const updateAnnouncementStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/announcement/${id}/status`, null, { params: { status } })
}


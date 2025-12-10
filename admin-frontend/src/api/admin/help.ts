/**
 * 帮助中心管理API
 */

import request from '@/utils/request'

// 帮助中心分类DTO
export interface HelpCategoryDTO {
  id?: number
  parentId: number
  name: string
  sort?: number
  status?: number
}

// 帮助中心分类VO
export interface HelpCategoryVO {
  id: number
  parentId: number
  name: string
  sort: number
  status: number
  createTime: string
  updateTime: string
  children?: HelpCategoryVO[]
}

// 帮助中心文章DTO
export interface HelpArticleDTO {
  id?: number
  categoryId?: number
  title: string
  content: string
  images?: string[]
  sort?: number
  status?: number
}

// 帮助中心文章VO
export interface HelpArticleVO {
  id: number
  categoryId: number
  categoryName?: string
  title: string
  content: string
  images: string[]
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// ==================== 分类管理 ====================

/**
 * 获取帮助中心分类树
 */
export const getHelpCategoryTree = (): Promise<HelpCategoryVO[]> => {
  return request.get('/api/admin/help/categories/tree')
}

/**
 * 根据ID获取分类信息
 */
export const getHelpCategoryById = (id: number): Promise<HelpCategoryVO> => {
  return request.get(`/api/admin/help/category/${id}`)
}

/**
 * 创建分类
 */
export const createHelpCategory = (data: HelpCategoryDTO): Promise<void> => {
  return request.post('/api/admin/help/category', data)
}

/**
 * 更新分类
 */
export const updateHelpCategory = (id: number, data: HelpCategoryDTO): Promise<void> => {
  return request.put(`/api/admin/help/category/${id}`, data)
}

/**
 * 删除分类
 */
export const deleteHelpCategory = (id: number): Promise<void> => {
  return request.delete(`/api/admin/help/category/${id}`)
}

/**
 * 更新分类状态
 */
export const updateHelpCategoryStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/help/category/${id}/status`, null, { params: { status } })
}

// ==================== 文章管理 ====================

/**
 * 分页查询文章列表
 */
export const getHelpArticlePage = (
  pageNum: number,
  pageSize: number,
  categoryId?: number,
  title?: string,
  status?: number
): Promise<PageResponse<HelpArticleVO>> => {
  return request.get('/api/admin/help/articles', {
    params: { pageNum, pageSize, categoryId, title, status }
  })
}

/**
 * 根据ID获取文章信息
 */
export const getHelpArticleById = (id: number): Promise<HelpArticleVO> => {
  return request.get(`/api/admin/help/article/${id}`)
}

/**
 * 创建文章
 */
export const createHelpArticle = (data: HelpArticleDTO): Promise<void> => {
  return request.post('/api/admin/help/article', data)
}

/**
 * 更新文章
 */
export const updateHelpArticle = (id: number, data: HelpArticleDTO): Promise<void> => {
  return request.put(`/api/admin/help/article/${id}`, data)
}

/**
 * 删除文章
 */
export const deleteHelpArticle = (id: number): Promise<void> => {
  return request.delete(`/api/admin/help/article/${id}`)
}

/**
 * 更新文章状态
 */
export const updateHelpArticleStatus = (id: number, status: number): Promise<void> => {
  return request.put(`/api/admin/help/article/${id}/status`, null, { params: { status } })
}


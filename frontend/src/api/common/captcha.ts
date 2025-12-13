import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 验证码响应
 */
export interface CaptchaResponse {
  captchaId: string
  captchaImage: string
}

/**
 * 生成验证码
 */
export const generateCaptcha = (): Promise<CaptchaResponse> => {
  return request.get('/api/common/captcha/generate')
}
































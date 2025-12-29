import dayjs from 'dayjs'

/**
 * 日期时间格式化
 */
export const formatDateTime = (date: Date | string | number, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  return dayjs(date).format(format)
}

/**
 * 日期格式化
 */
export const formatDate = (date: Date | string | number, format = 'YYYY-MM-DD'): string => {
  return dayjs(date).format(format)
}

/**
 * 金额格式化
 */
export const formatMoney = (amount: number): string => {
  return `¥${amount.toFixed(2)}`
}

/**
 * 文件大小格式化
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

/**
 * 手机号脱敏
 */
export const maskPhone = (phone: string): string => {
  if (!phone || phone.length < 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 邮箱脱敏
 */
export const maskEmail = (email: string): string => {
  if (!email) return email
  const [name, domain] = email.split('@')
  if (!name || !domain) return email
  const maskedName = name.length > 2 
    ? `${name.substring(0, 2)}***` 
    : `${name[0]}***`
  return `${maskedName}@${domain}`
}


























































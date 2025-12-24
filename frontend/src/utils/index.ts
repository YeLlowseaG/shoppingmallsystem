import dayjs from 'dayjs'

/**
 * 格式化日期时间
 */
export const formatDateTime = (date: string | Date | number, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 */
export const formatDate = (date: string | Date | number, format = 'YYYY-MM-DD'): string => {
  return dayjs(date).format(format)
}

/**
 * 格式化金额
 */
export const formatMoney = (amount: number, decimals = 2): string => {
  return amount.toFixed(decimals)
}

/**
 * 格式化文件大小
 */
export const formatFileSize = (size: number): string => {
  if (size < 1024) {
    return size + 'B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + 'KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + 'MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + 'GB'
  }
}

/**
 * 手机号脱敏
 */
export const maskPhone = (phone: string): string => {
  if (!phone || phone.length < 11) {
    return phone
  }
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

/**
 * 邮箱脱敏
 */
export const maskEmail = (email: string): string => {
  if (!email || !email.includes('@')) {
    return email
  }
  const [prefix, suffix] = email.split('@')
  if (prefix.length <= 2) {
    return prefix[0] + '***@' + suffix
  }
  return prefix.substring(0, 2) + '***@' + suffix
}














































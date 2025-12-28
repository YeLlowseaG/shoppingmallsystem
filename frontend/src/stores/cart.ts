import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCartItemCount } from '@/api/buyer/cart'

export interface CartItem {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  [key: string]: any
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])
  const totalCount = ref<number>(0)
  const totalAmount = ref<number>(0)

  // 添加商品到购物车
  const addItem = (item: CartItem) => {
    const existingItem = items.value.find(i => i.productId === item.productId)
    if (existingItem) {
      existingItem.quantity += item.quantity
    } else {
      items.value.push(item)
    }
    calculateTotal()
  }

  // 移除商品
  const removeItem = (productId: number) => {
    const index = items.value.findIndex(i => i.productId === productId)
    if (index > -1) {
      items.value.splice(index, 1)
      calculateTotal()
    }
  }

  // 更新商品数量
  const updateQuantity = (productId: number, quantity: number) => {
    const item = items.value.find(i => i.productId === productId)
    if (item) {
      item.quantity = quantity
      if (quantity <= 0) {
        removeItem(productId)
      } else {
        calculateTotal()
      }
    }
  }

  // 清空购物车
  const clearCart = () => {
    items.value = []
    calculateTotal()
  }

  // 计算总计
  const calculateTotal = () => {
    totalCount.value = items.value.reduce((sum, item) => sum + item.quantity, 0)
    totalAmount.value = items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  }

  // 从服务器更新购物车数量
  const updateCartCount = async () => {
    try {
      const count = await getCartItemCount()
      totalCount.value = count
    } catch (error) {
      console.error('更新购物车数量失败:', error)
    }
  }

  return {
    items,
    totalCount,
    totalAmount,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    calculateTotal,
    updateCartCount
  }
})























































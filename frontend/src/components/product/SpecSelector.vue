<template>
  <div class="spec-selector" v-if="specKeys.length > 0">
    <!-- 已选择规格显示 -->
    <div class="selected-specs" v-if="hasSelectedSpecs">
      <span class="selected-label">已选择：</span>
      <span class="selected-value">{{ selectedSpecsText }}</span>
    </div>

    <!-- 规格选择区域 -->
    <div 
      v-for="specKey in specKeys" 
      :key="specKey.id"
      class="spec-group"
    >
      <div class="spec-name">{{ specKey.specName }}：</div>
      <div class="spec-options">
        <div
          v-for="specValue in specKey.specValues"
          :key="specValue.id"
          class="spec-option"
          :class="{
            'selected': selectedSpecs[specKey.specName] === specValue.specValue,
            'disabled': isSpecValueDisabled(specKey.specName, specValue.specValue)
          }"
          @click="handleSpecValueClick(specKey.specName, specValue.specValue)"
        >
          <img 
            v-if="specValue.specImage" 
            :src="specValue.specImage" 
            :alt="specValue.specValue"
            class="spec-image"
          />
          <span class="spec-text">{{ specValue.specValue }}</span>
        </div>
      </div>
    </div>

    <!-- 价格和库存信息 -->
    <div class="sku-info" v-if="currentSku">
      <div class="price-info">
        <span class="current-price">¥{{ parseFloat(currentSku.price).toFixed(2) }}</span>
        <span class="stock-info" :class="{ 'low-stock': currentSku.stock <= currentSku.warningStock }">
          库存：{{ currentSku.stock }}
        </span>
      </div>
    </div>

    <!-- 无库存提示 -->
    <div class="no-stock-tip" v-if="hasSelectedAllSpecs && !currentSku">
      <span class="tip-text">该规格组合暂无库存</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { ProductSpecKeyVO, ProductSkuVO } from '@/api/buyer/sku'

// Props定义
interface Props {
  specKeys: ProductSpecKeyVO[]
  skuList: ProductSkuVO[]
  defaultSpecs?: Record<string, string>
}

const props = withDefaults(defineProps<Props>(), {
  defaultSpecs: () => ({})
})

// Emits定义
interface Emits {
  (e: 'spec-change', selectedSpecs: Record<string, string>, currentSku: ProductSkuVO | null): void
}

const emit = defineEmits<Emits>()

// 当前选择的规格
const selectedSpecs = ref<Record<string, string>>({ ...props.defaultSpecs })

// 计算属性：已选择的规格文本
const selectedSpecsText = computed(() => {
  return Object.entries(selectedSpecs.value)
    .map(([key, value]) => `${key}：${value}`)
    .join('，')
})

// 计算属性：是否有已选择的规格
const hasSelectedSpecs = computed(() => {
  return Object.keys(selectedSpecs.value).length > 0
})

// 计算属性：是否已选择所有规格
const hasSelectedAllSpecs = computed(() => {
  return props.specKeys.every(key => selectedSpecs.value[key.specName])
})

// 计算属性：当前选择的SKU
const currentSku = computed(() => {
  if (!hasSelectedAllSpecs.value) return null
  
  // 查找匹配的SKU（使用深度比较而非字符串比较）
  return props.skuList.find(sku => {
    try {
      const skuSpecs = JSON.parse(sku.specCombination)
      const selectedKeys = Object.keys(selectedSpecs.value)
      const skuKeys = Object.keys(skuSpecs)
      
      // 检查键数量是否相同
      if (selectedKeys.length !== skuKeys.length) return false
      
      // 检查每个规格是否匹配
      return selectedKeys.every(key => 
        skuSpecs[key] && skuSpecs[key] === selectedSpecs.value[key]
      )
    } catch {
      return false
    }
  }) || null
})

// 判断规格值是否禁用（无库存或不可选）
const isSpecValueDisabled = (specName: string, specValue: string): boolean => {
  // 创建临时的规格组合
  const tempSpecs = { ...selectedSpecs.value, [specName]: specValue }
  
  // 如果还没有选择完所有规格，检查是否有可用的SKU组合
  const unselectedKeys = props.specKeys.filter(key => !tempSpecs[key.specName])
  
  if (unselectedKeys.length === 0) {
    // 所有规格都已选择，直接检查SKU是否存在且有库存
    const sku = props.skuList.find(sku => {
      try {
        const skuSpecs = JSON.parse(sku.specCombination)
        return Object.keys(tempSpecs).every(key => 
          skuSpecs[key] && skuSpecs[key] === tempSpecs[key]
        )
      } catch {
        return false
      }
    })
    return !sku || sku.stock <= 0 || sku.status === 0
  } else {
    // 还有未选择的规格，检查是否有任何可能的组合有库存
    return !hasAvailableSkuForPartialSpecs(tempSpecs)
  }
}

// 检查部分规格选择是否有可用的SKU
const hasAvailableSkuForPartialSpecs = (partialSpecs: Record<string, string>): boolean => {
  return props.skuList.some(sku => {
    if (sku.stock <= 0 || sku.status === 0) return false
    
    try {
      const skuSpecs = JSON.parse(sku.specCombination)
      // 检查是否匹配已选择的规格
      return Object.entries(partialSpecs).every(([key, value]) => skuSpecs[key] === value)
    } catch {
      return false
    }
  })
}

// 处理规格值点击
const handleSpecValueClick = (specName: string, specValue: string) => {
  if (isSpecValueDisabled(specName, specValue)) {
    return
  }

  // 如果点击的是已选择的规格值，则取消选择
  if (selectedSpecs.value[specName] === specValue) {
    delete selectedSpecs.value[specName]
  } else {
    selectedSpecs.value[specName] = specValue
  }

  // 触发规格变化事件
  emit('spec-change', { ...selectedSpecs.value }, currentSku.value)
}

// 监听规格选择变化
watch(
  () => props.defaultSpecs,
  (newSpecs) => {
    selectedSpecs.value = { ...newSpecs }
  },
  { immediate: true }
)

// 监听当前SKU变化，向外部发射事件
watch(
  currentSku,
  (newSku) => {
    emit('spec-change', { ...selectedSpecs.value }, newSku)
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.spec-selector {
  .selected-specs {
    margin-bottom: 20px;
    padding: 12px;
    background-color: #f5f5f5;
    border-radius: 4px;
    font-size: 14px;

    .selected-label {
      color: #666;
    }

    .selected-value {
      color: #e4393c;
      font-weight: bold;
    }
  }

  .spec-group {
    margin-bottom: 20px;

    .spec-name {
      font-size: 14px;
      color: #333;
      font-weight: bold;
      margin-bottom: 12px;
    }

    .spec-options {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .spec-option {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 8px 16px;
        border: 1px solid #ddd;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;
        min-width: 60px;
        text-align: center;

        &:hover:not(.disabled) {
          border-color: #e4393c;
          background-color: #fff5f5;
        }

        &.selected {
          border-color: #e4393c;
          background-color: #e4393c;
          color: #fff;
        }

        &.disabled {
          background-color: #f5f5f5;
          color: #ccc;
          border-color: #e5e5e5;
          cursor: not-allowed;
          
          .spec-text {
            text-decoration: line-through;
          }
        }

        .spec-image {
          width: 40px;
          height: 40px;
          object-fit: cover;
          border-radius: 2px;
          margin-bottom: 4px;
        }

        .spec-text {
          font-size: 12px;
          line-height: 1.2;
        }
      }
    }
  }

  .sku-info {
    margin-top: 20px;
    padding: 15px;
    background-color: #f9f9f9;
    border-radius: 4px;

    .price-info {
      display: flex;
      align-items: center;
      gap: 20px;

      .current-price {
        font-size: 24px;
        font-weight: bold;
        color: #e4393c;
      }

      .stock-info {
        font-size: 14px;
        color: #52c41a;

        &.low-stock {
          color: #faad14;
        }
      }
    }
  }

  .no-stock-tip {
    margin-top: 20px;
    padding: 15px;
    background-color: #fff2f0;
    border: 1px solid #ffccc7;
    border-radius: 4px;
    text-align: center;

    .tip-text {
      color: #cf1322;
      font-size: 14px;
    }
  }
}
</style>
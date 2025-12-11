<template>
  <div class="rich-text-editor">
    <QuillEditor
      :key="editorKey"
      v-model:content="content"
      contentType="html"
      :options="editorOptions"
      :style="{ height: height }"
      @update:content="handleUpdate"
    />
    <div v-if="showTip" class="editor-tip">
      提示：支持富文本编辑，可以直接插入图片、设置文本格式等。图片可以通过工具栏的图片按钮插入。
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

/**
 * 富文本编辑器通用组件
 * 基于 Quill 编辑器封装，统一管理配置
 */

// Props
interface Props {
  modelValue: string
  height?: string
  placeholder?: string
  showTip?: boolean
  toolbar?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  height: '400px',
  placeholder: '请输入内容',
  showTip: true,
  toolbar: () => [
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'color': [] }, { 'background': [] }],
    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
    [{ 'align': [] }],
    ['link', 'image'],
    ['clean']
  ]
})

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 编辑器内容
const content = ref(props.modelValue)

// 编辑器配置
const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: props.toolbar
  },
  placeholder: props.placeholder
}

// 编辑器 key（用于强制刷新）
const editorKey = ref(0)

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue !== content.value) {
    content.value = newValue
  }
})

// 处理内容更新
const handleUpdate = (value: string) => {
  emit('update:modelValue', value)
}

// 暴露刷新方法（如果需要强制刷新编辑器）
const refresh = () => {
  editorKey.value++
}

defineExpose({
  refresh
})
</script>

<style scoped lang="scss">
.rich-text-editor {
  .editor-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
  }
}

// Quill 编辑器样式调整
:deep(.ql-toolbar) {
  border: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  background-color: #fafafa;
}

:deep(.ql-container) {
  border: 1px solid #dcdfe6;
  border-radius: 0 0 4px 4px;
  font-size: 14px;

  .ql-editor {
    min-height: 300px;

    &.ql-blank::before {
      color: #c0c4cc;
      font-style: normal;
    }
  }
}

:deep(.ql-snow .ql-tooltip) {
  z-index: 9999;
}
</style>

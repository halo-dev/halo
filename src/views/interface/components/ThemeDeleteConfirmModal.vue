<template>
  <a-modal v-model="modalVisible" :afterClose="onAfterClose" :closable="false" :width="416" destroyOnClose title="提示">
    <template slot="footer">
      <a-button @click="modalVisible = false">
        取消
      </a-button>
      <ReactiveButton
        :errored="deleteErrored"
        :loading="deleting"
        erroredText="删除失败"
        loadedText="删除成功"
        text="确定"
        @callback="handleDeleteCallback"
        @click="handleDelete()"
      ></ReactiveButton>
    </template>
    <p>确定删除【{{ theme.name }}】主题？</p>
    <a-checkbox v-model="deleteSettings">
      同时删除主题配置
    </a-checkbox>
  </a-modal>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'ThemeDeleteConfirmModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    theme: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      deleteErrored: false,
      deleting: false,
      deleteSettings: false
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    }
  },
  methods: {
    async handleDelete() {
      try {
        this.deleting = true
        await apiClient.theme.delete(this.theme.id, this.deleteSettings)
      } catch (e) {
        this.deleteErrored = false
        this.$log.error('Delete theme failed', e)
      } finally {
        setTimeout(() => {
          this.deleting = false
        }, 400)
      }
    },
    handleDeleteCallback() {
      if (this.deleteErrored) {
        this.deleteErrored = false
      } else {
        this.modalVisible = false
        this.$emit('success')
      }
    },
    onAfterClose() {
      this.deleteErrored = false
      this.deleting = false
      this.deleteSettings = false
      this.$emit('onAfterClose')
    }
  }
}
</script>

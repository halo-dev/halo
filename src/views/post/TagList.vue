<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }">
        <a-card title="添加标签">
          <a-form layout="horizontal">
            <a-form-item label="名称：" help="*页面上所显示的名称">
              <a-input v-model="tagToCreate.name" />
            </a-form-item>
            <a-form-item label="路径名称：" help="*这是文章路径上显示的名称，最好为英文">
              <a-input v-model="tagToCreate.slugName" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="createTag">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col
        :xl="14"
        :lg="14"
        :md="14"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }">
        <a-card title="所有标签">
          <a-tooltip placement="topLeft" v-for="tag in tags" :key="tag.id">
            <template slot="title">
              <span>{{ tag.postCount }} 篇文章</span>
            </template>
            <a-tag closable @close="deleteTag(tag.id)" color="green">{{ tag.name }}</a-tag>
          </a-tooltip>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import tagApi from '@/api/tag'
import { closeSync } from 'fs';
export default {
  data() {
    return {
      tags: [],
      tagToCreate: {},
      tagToUpdate: {}
    }
  },
  created() {
    this.loadTags()
  },
  methods: {
    loadTags() {
      tagApi.listAll(true).then(response => {
        this.tags = response.data.data
      })
    },
    createTag() {
      tagApi.create(this.tagToCreate).then(response => {
        this.loadTags()
      })
    },
    updateTag(tagId) {
      tagApi.update(tagId, this.tagToUpdate).then(response => {
        this.loadTags()
      })
    },
    deleteTag(tagId) {
      tagApi.delete(tagId).then(response => {
        this.$message.success('删除成功！')
        this.loadTags()
      })
    }
  }
}
</script>

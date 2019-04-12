<template>
  <page-view>
    <a-row
      :gutter="12"
      type="flex"
      align="middle"
    >
      <a-col
        class="attachment-item"
        v-for="attachment in attachments"
        :key="attachment.id"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24"
      >
        <a-card
          :bodyStyle="{ padding: '1rem' , width: '240px' }"
          hoverable
        >
          <img
            :src="attachment.thumbPath"
            :alt="attachment.name"
            slot="cover"
          >
          <a-card-meta>
            <template slot="description">{{ attachment.name }}</template>
          </a-card-meta>
        </a-card>
      </a-col>
    </a-row>
    <a-row
      type="flex"
      justify="end"
      :gutter="12"
    >
      <a-pagination
        v-model="pagination.page"
        :defaultPageSize="pagination.size"
        :total="pagination.total"
      ></a-pagination>
    </a-row>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import attachmentApi from '@/api/attachment'
export default {
  components: {
    PageView
  },
  data() {
    return {
      attachments: [],
      pagination: {
        page: 1,
        size: 16,
        sort: ''
      }
    }
  },
  created() {
    this.loadAttachments()
  },
  methods: {
    loadAttachments() {
      const pagination = Object.assign({}, this.pagination)
      pagination.page--
      attachmentApi.list(pagination).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    }
  }
}
</script>

<style lang="less" scoped>
.attachment-item {
  padding-bottom: 12px;
}
</style>

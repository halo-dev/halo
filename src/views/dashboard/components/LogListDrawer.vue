<template>
  <div>
    <a-drawer
      title="操作日志"
      :width="isMobile() ? '100%' : '480'"
      closable
      :visible="visible"
      destroyOnClose
      @close="onClose"
      :afterVisibleChange="handleAfterVisibleChanged"
    >
      <a-row type="flex" align="middle">
        <a-col :span="24">
          <a-list :loading="loading" :dataSource="formattedLogsDatas">
            <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
              <a-list-item-meta :description="item.createTime | timeAgo">
                <span slot="title">{{ item.type }}</span>
              </a-list-item-meta>
              <ellipsis :length="35" tooltip>{{ item.content }}</ellipsis>
            </a-list-item>
          </a-list>

          <div class="page-wrapper">
            <a-pagination
              class="pagination"
              :current="pagination.page"
              :total="pagination.total"
              :defaultPageSize="pagination.size"
              :pageSizeOptions="['50', '100', '150', '200']"
              showSizeChanger
              @showSizeChange="handlePaginationChange"
              @change="handlePaginationChange"
              showLessItems
            />
          </div>
        </a-col>
      </a-row>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-popconfirm title="你确定要清空所有操作日志？" okText="确定" @confirm="handleClearLogs" cancelText="取消">
          <a-button type="danger">清空操作日志</a-button>
        </a-popconfirm>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import logApi from '@/api/log'
export default {
  name: 'LogListDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      logTypes: logApi.logTypes,
      loading: true,
      logs: [],
      pagination: {
        page: 1,
        size: 50,
        sort: null,
        total: 1
      },
      logQueryParam: {
        page: 0,
        size: 50,
        sort: null
      }
    }
  },
  props: {
    visible: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  computed: {
    formattedLogsDatas() {
      return this.logs.map(log => {
        log.type = this.logTypes[log.type].text
        return log
      })
    }
  },
  methods: {
    handleListLogs() {
      this.loading = true
      this.logQueryParam.page = this.pagination.page - 1
      this.logQueryParam.size = this.pagination.size
      this.logQueryParam.sort = this.pagination.sort
      logApi
        .pageBy(this.logQueryParam)
        .then(response => {
          this.logs = response.data.data.content
          this.pagination.total = response.data.data.total
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleClearLogs() {
      logApi
        .clear()
        .then(() => {
          this.$message.success('清除成功！')
        })
        .finally(() => {
          this.handleListLogs()
        })
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.handleListLogs()
    },
    onClose() {
      this.$emit('close', false)
    },
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListLogs()
      }
    }
  }
}
</script>

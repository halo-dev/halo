<template>
  <div>
    <a-drawer
      title="操作日志"
      :width="isMobile()?'100%':'480'"
      closable
      :visible="visible"
      destroyOnClose
      @close="onClose"
    >
      <a-row
        type="flex"
        align="middle"
      >
        <a-col :span="24">
          <a-skeleton
            active
            :loading="loading"
            :paragraph="{rows: 18}"
          >
            <a-list :dataSource="formattedLogsDatas">
              <a-list-item
                slot="renderItem"
                slot-scope="item, index"
                :key="index"
              >
                <a-list-item-meta :description="item.createTime | timeAgo">
                  <span slot="title">{{ item.type }}</span>
                </a-list-item-meta>
                <ellipsis
                  :length="35"
                  tooltip
                >{{ item.content }}</ellipsis>
              </a-list-item>
            </a-list>
          </a-skeleton>

          <div class="page-wrapper">
            <a-pagination
              class="pagination"
              :current="pagination.page"
              :total="pagination.total"
              :defaultPageSize="pagination.size"
              :pageSizeOptions="['50', '100','150','200']"
              showSizeChanger
              @showSizeChange="handlePaginationChange"
              @change="handlePaginationChange"
            />
          </div>
        </a-col>
      </a-row>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-popconfirm
          title="你确定要清空所有操作日志？"
          okText="确定"
          @confirm="handleClearLogs"
          cancelText="取消"
        >
          <a-button type="danger">清空操作日志</a-button>
        </a-popconfirm>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import logApi from '@/api/log'
export default {
  name: 'LogListDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      logType: logApi.logType,
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
        log.type = this.logType[log.type].text
        return log
      })
    }
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
        this.loadLogs()
      }
    }
  },
  methods: {
    loadSkeleton() {
      this.loading = true
      setTimeout(() => {
        this.loading = false
      }, 500)
    },
    loadLogs() {
      this.logQueryParam.page = this.pagination.page - 1
      this.logQueryParam.size = this.pagination.size
      this.logQueryParam.sort = this.pagination.sort
      logApi.pageBy(this.logQueryParam).then(response => {
        this.logs = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    },
    handleClearLogs() {
      logApi.clear().then(response => {
        this.$message.success('清除成功！')
        this.loadLogs()
      })
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadLogs()
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>

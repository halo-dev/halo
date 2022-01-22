<template>
  <page-view>
    <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
      <div class="table-operator">
        <a-button type="danger" @click="handleClearActionLogs">清空操作日志</a-button>
      </div>
      <div class="mt-4">
        <a-table
          :columns="list.columns"
          :dataSource="list.data"
          :loading="list.loading"
          :pagination="false"
          :rowKey="log => log.id"
          :scrollToFirstRowOnChange="true"
        >
          <template #type="type">
            {{ type | typeConvert }}
          </template>
          <template #ipAddress="ipAddress">
            <div class="blur hover:blur-none transition-all">{{ ipAddress }}</div>
          </template>
          <template #createTime="createTime">
            <a-tooltip placement="top">
              <template slot="title">
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </template>
        </a-table>
        <div class="page-wrapper">
          <a-pagination
            :current="pagination.page"
            :defaultPageSize="pagination.size"
            :pageSizeOptions="['10', '20', '50', '100']"
            :total="pagination.total"
            class="pagination"
            showLessItems
            showSizeChanger
            @change="handlePageChange"
            @showSizeChange="handlePageSizeChange"
          />
        </div>
      </div>
    </a-card>
  </page-view>
</template>

<script>
// components
import { PageView } from '@/layouts'

// libs
import apiClient from '@/utils/api-client'
import { actionLogTypes } from '@/core/constant'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id'
  },
  {
    title: '类型',
    dataIndex: 'type',
    scopedSlots: { customRender: 'type' }
  },
  {
    title: '关键值',
    dataIndex: 'logKey'
  },
  {
    title: '内容',
    dataIndex: 'content'
  },
  {
    title: 'IP',
    dataIndex: 'ipAddress',
    scopedSlots: { customRender: 'ipAddress' }
  },
  {
    title: '操作时间',
    dataIndex: 'createTime',
    scopedSlots: { customRender: 'createTime' }
  }
]

export default {
  name: 'ActionLog',
  components: {
    PageView
  },
  data() {
    return {
      list: {
        columns,
        data: [],
        total: 0,
        loading: false,
        params: {
          page: 0,
          size: 50
        }
      }
    }
  },
  computed: {
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    }
  },
  created() {
    this.handleListActionLogs()
  },
  methods: {
    async handleListActionLogs() {
      try {
        this.list.loading = true

        const response = await apiClient.log.list(this.list.params)

        this.list.data = response.data.content
        this.list.total = response.data.total
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.list.loading = false
      }
    },

    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleListActionLogs()
    },

    handlePageSizeChange(current, size) {
      this.$log.debug(`Current: ${current}, PageSize: ${size}`)
      this.list.params.page = 0
      this.list.params.size = size
      this.handleListActionLogs()
    },

    handleClearActionLogs() {
      const _this = this
      _this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '是否确定要清空所有操作日志？',
        async onOk() {
          try {
            await apiClient.log.clear()
          } catch (e) {
            _this.$log.error('Failed to clear action logs.', e)
          } finally {
            await _this.handleListActionLogs()
          }
        }
      })
    }
  },
  filters: {
    typeConvert(key) {
      const type = actionLogTypes[key]
      return type ? type.text : key
    }
  }
}
</script>

<template>
  <div>
    <a-drawer
      :afterVisibleChange="handleAfterVisibleChanged"
      :visible="visible"
      :width="isMobile() ? '100%' : '480'"
      closable
      destroyOnClose
      title="操作日志"
      @close="onClose"
    >
      <a-row align="middle" type="flex">
        <a-col :span="24">
          <a-list :dataSource="formattedLogsDatas" :loading="loading">
            <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
              <a-list-item-meta :description="item.createTime | timeAgo">
                <span slot="title">{{ item.type }}</span>
              </a-list-item-meta>
              <ellipsis :length="35" tooltip>{{ item.content }}</ellipsis>
            </a-list-item>
          </a-list>

          <div class="page-wrapper">
            <a-pagination
              :current="pagination.page"
              :defaultPageSize="pagination.size"
              :pageSizeOptions="['50', '100', '150', '200']"
              :total="pagination.total"
              class="pagination"
              showLessItems
              showSizeChanger
              @change="handlePaginationChange"
              @showSizeChange="handlePaginationChange"
            />
          </div>
        </a-col>
      </a-row>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-popconfirm cancelText="取消" okText="确定" title="你确定要清空所有操作日志？" @confirm="handleClearLogs">
          <a-button type="danger">清空操作日志</a-button>
        </a-popconfirm>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

export default {
  name: 'LogListDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      logTypes: {
        BLOG_INITIALIZED: {
          value: 0,
          text: '博客初始化'
        },
        POST_PUBLISHED: {
          value: 5,
          text: '文章发布'
        },
        POST_EDITED: {
          value: 15,
          text: '文章修改'
        },
        POST_DELETED: {
          value: 20,
          text: '文章删除'
        },
        LOGGED_IN: {
          value: 25,
          text: '用户登录'
        },
        LOGGED_OUT: {
          value: 30,
          text: '注销登录'
        },
        LOGIN_FAILED: {
          value: 35,
          text: '登录失败'
        },
        PASSWORD_UPDATED: {
          value: 40,
          text: '修改密码'
        },
        PROFILE_UPDATED: {
          value: 45,
          text: '资料修改'
        },
        SHEET_PUBLISHED: {
          value: 50,
          text: '页面发布'
        },
        SHEET_EDITED: {
          value: 55,
          text: '页面修改'
        },
        SHEET_DELETED: {
          value: 60,
          text: '页面删除'
        },
        MFA_UPDATED: {
          value: 65,
          text: '两步验证'
        },
        LOGGED_PRE_CHECK: {
          value: 70,
          text: '登录验证'
        }
      },
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
      apiClient.log
        .list(this.logQueryParam)
        .then(response => {
          this.logs = response.data.content
          this.pagination.total = response.data.total
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleClearLogs() {
      apiClient.log
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

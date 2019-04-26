<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="关键词">
                    <a-input v-model="queryParam.keyword"/>
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="状态">
                    <a-select placeholder="请选择状态">
                      <a-select-option value="1">公开</a-select-option>
                      <a-select-option value="0">私密</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <span class="table-page-search-submitButtons">
                    <a-button
                      type="primary"
                      @click="loadJournals(true)"
                    >查询</a-button>
                    <a-button
                      style="margin-left: 8px;"
                      @click="resetParam"
                    >重置</a-button>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div class="table-operator">
            <a-button
              type="primary"
              icon="plus"
              @click="handleNew"
            >写日志</a-button>
          </div>
          <a-divider />
          <div style="margin-top:15px">
            <a-list
              itemLayout="vertical"
              :pagination="false"
              :dataSource="journals"
              :loading="listLoading"
            >
              <a-list-item
                slot="renderItem"
                slot-scope="item, index"
                :key="index"
              >
                <template
                  slot="actions"
                  v-for="{type, text} in actions"
                >
                  <span :key="type">
                    <a-icon
                      :type="type"
                      style="margin-right: 8px"
                    />
                    {{ text }}
                  </span>
                </template>
                <template slot="extra">
                  <a
                    href="javascript:void(0);"
                    @click="handleEdit(item)"
                  >编辑</a>
                  <a-divider type="vertical" />
                  <a href="javascript:void(0);">删除</a>
                </template>
                <a-list-item-meta :description="item.content">
                  <span slot="title">{{ item.createTime | moment }}</span>
                  <a-avatar
                    slot="avatar"
                    size="large"
                    src="https://gravatar.loli.net/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm"
                  />
                </a-list-item-meta>
              </a-list-item>
              <div class="page-wrapper">
                <a-pagination
                  class="pagination"
                  :total="pagination.total"
                  :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
                  showSizeChanger
                  @showSizeChange="onPaginationChange"
                  @change="onPaginationChange"
                />
              </div>
            </a-list>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      :title="title"
      v-model="visible"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="createOrUpdateJournal"
        >
          发布
        </a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <a-input
            type="textarea"
            :autosize="{ minRows: 8 }"
            v-model="journal.content"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import journalApi from '@/api/journal'

export default {
  data() {
    return {
      title: '发表',
      listLoading: false,
      visible: false,
      pagination: {
        page: 1,
        size: 10,
        sort: null
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null
      },
      actions: [{ type: 'like-o', text: '28031230' }, { type: 'message', text: '2' }],
      journals: [],
      journal: {}
    }
  },
  created() {
    this.loadJournals()
  },
  methods: {
    loadJournals(isSearch) {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      if (isSearch) {
        this.queryParam.page = 0
      }
      this.listLoading = true
      journalApi.query(this.queryParam).then(response => {
        this.journals = response.data.data.content
        this.pagination.total = response.data.data.total
        this.listLoading = false
      })
    },
    handleNew() {
      this.title = '新建'
      this.visible = true
      this.journal = {}
    },
    handleEdit(item) {
      this.title = '编辑'
      this.journal = item
      this.visible = true
    },
    createOrUpdateJournal() {
      if (this.journal.id) {
        journalApi.update(this.journal.id, this.journal).then(response => {
          this.$message.success('更新成功！')
          this.loadJournals()
        })
      } else {
        journalApi.create(this.journal).then(response => {
          this.$message.success('发表成功！')
          this.loadJournals()
        })
      }
      this.visible = false
    },
    onPaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadJournals()
    },
    resetParam() {
      this.queryParam.keyword = null
      this.loadJournals()
    }
  }
}
</script>
<style scoped>
.pagination {
  margin-top: 1rem;
}
</style>

<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="关键词">
                    <a-input v-model="queryParam.keyword" />
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
                <template slot="actions">
                  <span>
                    <a
                      href="javascript:void(0);"
                    >
                      <a-icon
                        type="like-o"
                        style="margin-right: 8px"
                      />{{ item.likes }}
                    </a>
                  </span>
                  <span>
                    <a
                      href="javascript:void(0);"
                      @click="handleCommentShow(item)"
                    >

                      <a-icon
                        type="message"
                        style="margin-right: 8px"
                      />{{ item.commentCount }}
                    </a>
                  </span>
                </template>
                <template slot="extra">
                  <a
                    href="javascript:void(0);"
                    @click="handleEdit(item)"
                  >编辑</a>
                  <a-divider type="vertical" />
                  <a-popconfirm
                    title="你确定要删除这条日志？"
                    @confirm="handleDelete(item.id)"
                    okText="确定"
                    cancelText="取消"
                  >
                    <a href="javascript:void(0);">删除</a>
                  </a-popconfirm>
                </template>
                <a-list-item-meta :description="item.content">
                  <span slot="title">{{ item.createTime | moment }}</span>
                  <a-avatar
                    slot="avatar"
                    size="large"
                    :src="user.avatar"
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

    <a-drawer
      title="评论列表"
      :width="isMobile()?'100%':'460'"
      closable
      :visible="commentVisiable"
      destroyOnClose
      @close="onCommentDrawerClose"
    >
      <a-row
        type="flex"
        align="middle"
      >
        <a-col :span="24">
          <blockquote>
            <a-comment>
              <template slot="actions">
                <span>
                  <a-icon type="like" />
                  <span style="padding-left: '8px';cursor: 'auto'">
                    {{ journal.likes }}
                  </span>
                </span>
                <span>
                  <a-icon type="message" />
                  <span style="padding-left: '8px';cursor: 'auto'">
                    {{ journal.commentCount }}
                  </span>
                </span>
              </template>
              <a-avatar
                :src="user.avatar"
                :alt="user.nickname"
                slot="avatar"
              />
              <p slot="content">{{ journal.content }}</p>

              <span slot="datetime">{{ journal.createTime | moment }}</span>
            </a-comment>
          </blockquote>
        </a-col>
        <a-divider />
        <a-col :span="24">
          <a-comment>
            <span slot="actions">回复</span>
            <a slot="author"> {{ user.nickname }} </a>
            <a-avatar
              slot="avatar"
              :src="user.avatar"
              :alt="user.nickname"
            />
            <p slot="content">{{ journal.content }}</p>
            <a-comment>
              <span slot="actions">回复</span>
              <a slot="author"> {{ user.nickname }} </a>
              <a-avatar
                slot="avatar"
                :src="user.avatar"
                :alt="user.nickname"
              />
              <p slot="content">{{ journal.content }}</p>
              <a-comment>
                <span slot="actions">回复</span>
                <a slot="author"> {{ user.nickname }} </a>
                <a-avatar
                  slot="avatar"
                  :src="user.avatar"
                  :alt="user.nickname"
                />
                <p slot="content">{{ journal.content }}</p>
              </a-comment>
              <a-comment>
                <span slot="actions">回复</span>
                <a slot="author"> {{ user.nickname }} </a>
                <a-avatar
                  slot="avatar"
                  :src="user.avatar"
                  :alt="user.nickname"
                />
                <p slot="content">{{ journal.content }}</p>
              </a-comment>
            </a-comment>
          </a-comment>
        </a-col>
      </a-row>
    </a-drawer>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import journalApi from '@/api/journal'
import userApi from '@/api/user'

export default {
  mixins: [mixin, mixinDevice],
  data() {
    return {
      title: '发表',
      listLoading: false,
      visible: false,
      commentVisiable: false,
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
      journals: [],
      journal: {},
      user: {}
    }
  },
  created() {
    this.loadJournals()
    this.loadUser()
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
    loadUser() {
      userApi.getProfile().then(response => {
        this.user = response.data.data
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
    handleDelete(id) {
      journalApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadJournals()
      })
    },
    handleCommentShow(journal) {
      this.journal = journal
      this.commentVisiable = true
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
    },
    onCommentDrawerClose() {
      this.commentVisiable = false
    }
  }
}
</script>
<style scoped>
.pagination {
  margin-top: 1rem;
}
</style>

<template>
  <page-view>
    <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="关键词：">
                <a-input v-model="list.params.keyword" @keyup.enter="handleQuery()" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="文章状态：">
                <a-select v-model="list.params.status" allowClear placeholder="请选择文章状态" @change="handleQuery()">
                  <a-select-option v-for="status in Object.keys(postStatuses)" :key="status" :value="status">
                    {{ postStatuses[status].text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="分类目录：">
                <a-select
                  v-model="list.params.categoryId"
                  :loading="categories.loading"
                  allowClear
                  placeholder="请选择分类"
                  @change="handleQuery()"
                >
                  <a-select-option v-for="category in categories.data" :key="category.id">
                    {{ category.name }}({{ category.postCount }})
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-space>
                  <a-button type="primary" @click="handleQuery()">查询</a-button>
                  <a-button @click="handleResetParam()">重置</a-button>
                </a-space>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <router-link :to="{ name: 'PostWrite' }">
          <a-button icon="plus" type="primary">写文章</a-button>
        </router-link>
        <a-dropdown v-show="list.params.status != null && list.params.status !== '' && !isMobile()">
          <template #overlay>
            <a-menu>
              <a-menu-item
                v-if="['DRAFT', 'RECYCLE'].includes(list.params.status)"
                key="1"
                @click="handleEditStatusMore(postStatuses.PUBLISHED.value)"
              >
                发布
              </a-menu-item>
              <a-menu-item
                v-if="['PUBLISHED', 'DRAFT', 'INTIMATE'].includes(list.params.status)"
                key="2"
                @click="handleEditStatusMore(postStatuses.RECYCLE.value)"
              >
                移到回收站
              </a-menu-item>
              <a-menu-item
                v-if="['RECYCLE', 'PUBLISHED', 'INTIMATE'].includes(list.params.status)"
                key="3"
                @click="handleEditStatusMore(postStatuses.DRAFT.value)"
              >
                草稿
              </a-menu-item>
              <a-menu-item v-if="['RECYCLE', 'DRAFT'].includes(list.params.status)" key="4" @click="handleDeleteMore">
                永久删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button class="ml-2">
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>
      <div class="mt-4">
        <!-- Mobile -->
        <a-list
          v-if="isMobile()"
          :dataSource="list.data"
          :loading="list.loading"
          :pagination="false"
          itemLayout="vertical"
          size="large"
        >
          <template #renderItem="item, index">
            <a-list-item :key="index">
              <template #actions>
                <span>
                  <a-icon type="eye" />
                  {{ item.visits }}
                </span>
                <span @click="handleShowPostComments(item)">
                  <a-icon type="message" />
                  {{ item.commentCount }}
                </span>
                <a-dropdown :trigger="['click']" placement="topLeft">
                  <span>
                    <a-icon type="bars" />
                  </span>

                  <template #overlay>
                    <a-menu>
                      <a-menu-item
                        v-if="['PUBLISHED', 'DRAFT', 'INTIMATE'].includes(item.status)"
                        @click="handleEditClick(item)"
                      >
                        编辑
                      </a-menu-item>
                      <a-menu-item v-else-if="item.status === 'RECYCLE'">
                        <a-popconfirm
                          :title="'你确定要发布【' + item.title + '】文章？'"
                          cancelText="取消"
                          okText="确定"
                          @confirm="handleEditStatusClick(item.id, 'PUBLISHED')"
                        >
                          还原
                        </a-popconfirm>
                      </a-menu-item>
                      <a-menu-item v-if="['PUBLISHED', 'DRAFT', 'INTIMATE'].includes(item.status)">
                        <a-popconfirm
                          :title="'你确定要将【' + item.title + '】文章移到回收站？'"
                          cancelText="取消"
                          okText="确定"
                          @confirm="handleEditStatusClick(item.id, 'RECYCLE')"
                        >
                          回收站
                        </a-popconfirm>
                      </a-menu-item>
                      <a-menu-item v-else-if="item.status === 'RECYCLE'">
                        <a-popconfirm
                          :title="'你确定要永久删除【' + item.title + '】文章？'"
                          cancelText="取消"
                          okText="确定"
                          @confirm="handleDeleteClick(item.id)"
                        >
                          删除
                        </a-popconfirm>
                      </a-menu-item>
                      <a-menu-item @click="handleShowPostSettings(item)">设置</a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </template>
              <template #extra>
                <a-badge :status="postStatuses[item.status].status" :text="item.status | statusText" />
              </template>
              <a-list-item-meta>
                <template #description>
                  {{ item.createTime | moment }}
                </template>
                <template #title>
                  <div
                    style="
                      max-width: 300px;
                      display: block;
                      white-space: nowrap;
                      overflow: hidden;
                      text-overflow: ellipsis;
                    "
                  >
                    <a-icon
                      v-if="item.topPriority !== 0"
                      style="margin-right: 3px"
                      theme="twoTone"
                      twoToneColor="red"
                      type="pushpin"
                    />
                    <a-tooltip v-if="item.inProgress" placement="top" title="当前有内容已保存，但还未发布。">
                      <a-icon
                        class="cursor-pointer"
                        style="margin-right: 3px"
                        theme="twoTone"
                        twoToneColor="#52c41a"
                        type="info-circle"
                      />
                    </a-tooltip>
                    <a-tooltip
                      v-if="['PUBLISHED', 'INTIMATE'].includes(item.status)"
                      :title="'点击访问【' + item.title + '】'"
                      placement="top"
                    >
                      <a :href="item.fullPath" class="no-underline" target="_blank">
                        {{ item.title }}
                      </a>
                    </a-tooltip>
                    <a-tooltip
                      v-else-if="item.status === 'DRAFT'"
                      :title="'点击预览【' + item.title + '】'"
                      placement="top"
                    >
                      <a class="no-underline" href="javascript:void(0);" @click="handlePreview(item.id)">
                        {{ item.title }}
                      </a>
                    </a-tooltip>
                    <a-button v-else class="!p-0" disabled type="link">
                      {{ item.title }}
                    </a-button>
                  </div>
                </template>
              </a-list-item-meta>

              <div v-if="item.summary" class="mb-3 block">
                <span> {{ item.summary }}... </span>
              </div>

              <div class="block">
                <a-tag
                  v-for="(category, categoryIndex) in item.categories"
                  :key="'category_' + categoryIndex"
                  color="blue"
                  style="margin-bottom: 8px"
                  @click="handleSelectCategory(category)"
                  >{{ category.name }}
                </a-tag>
              </div>
              <post-tag v-for="(tag, tagIndex) in item.tags" :key="tagIndex" :tag="tag" style="margin-bottom: 8px" />
            </a-list-item>
          </template>
        </a-list>

        <!-- Desktop -->
        <a-table
          v-else
          :columns="columns"
          :dataSource="list.data"
          :loading="list.loading"
          :pagination="false"
          :rowKey="post => post.id"
          :rowSelection="{
            selectedRowKeys: selectedRowKeys,
            onChange: onSelectionChange,
            getCheckboxProps: getCheckboxProps
          }"
          :scrollToFirstRowOnChange="true"
        >
          <template #postTitle="text, record">
            <a-icon
              v-if="record.topPriority !== 0"
              style="margin-right: 3px"
              theme="twoTone"
              twoToneColor="red"
              type="pushpin"
            />
            <a-tooltip v-if="record.inProgress" placement="top" title="当前有内容已保存，但还未发布。">
              <a-icon
                class="cursor-pointer"
                style="margin-right: 3px"
                theme="twoTone"
                twoToneColor="#52c41a"
                type="info-circle"
              />
            </a-tooltip>
            <a-tooltip
              v-if="['PUBLISHED', 'INTIMATE'].includes(record.status)"
              :title="'点击访问【' + text + '】'"
              placement="top"
            >
              <a :href="record.fullPath" class="no-underline" target="_blank">
                {{ text }}
              </a>
            </a-tooltip>
            <a-tooltip v-else-if="record.status === 'DRAFT'" :title="'点击预览【' + text + '】'" placement="top">
              <a class="no-underline" href="javascript:void(0);" @click="handlePreview(record.id)">
                {{ text }}
              </a>
            </a-tooltip>
            <a-button v-else class="!p-0" disabled type="link">
              {{ text }}
            </a-button>
          </template>

          <template #status="status">
            <a-badge :status="postStatuses[status].status" :text="status | statusText" />
          </template>

          <template #categories="categories">
            <a-tag
              v-for="(category, index) in categories"
              :key="index"
              color="blue"
              style="margin-bottom: 8px; cursor: pointer"
              @click="handleSelectCategory(category)"
            >
              {{ category.name }}
            </a-tag>
          </template>

          <template #tags="tags">
            <post-tag v-for="(tag, index) in tags" :key="index" :tag="tag" style="margin-bottom: 8px" />
          </template>

          <template #commentCount="text, record">
            <a-badge
              :count="record.commentCount"
              :numberStyle="{ backgroundColor: '#f38181' }"
              :overflowCount="999"
              :showZero="true"
              class="cursor-pointer"
              @click="handleShowPostComments(record)"
            />
          </template>

          <template #visits="visits">
            <a-badge
              :count="visits"
              :numberStyle="{ backgroundColor: '#00e0ff' }"
              :overflowCount="9999"
              :showZero="true"
              class="cursor-pointer"
            />
          </template>

          <template #createTime="createTime">
            <a-tooltip placement="top">
              <template #title>
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </template>

          <template #action="text, post">
            <a-button
              v-if="['PUBLISHED', 'DRAFT', 'INTIMATE'].includes(post.status)"
              class="!p-0"
              type="link"
              @click="handleEditClick(post)"
              >编辑
            </a-button>
            <a-popconfirm
              v-else-if="post.status === 'RECYCLE'"
              :title="'你确定要发布【' + post.title + '】文章？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleEditStatusClick(post.id, 'PUBLISHED')"
            >
              <a-button class="!p-0" type="link">还原</a-button>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a-popconfirm
              v-if="['PUBLISHED', 'DRAFT', 'INTIMATE'].includes(post.status)"
              :title="'你确定要将【' + post.title + '】文章移到回收站？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleEditStatusClick(post.id, 'RECYCLE')"
            >
              <a-button class="!p-0" type="link">回收站</a-button>
            </a-popconfirm>

            <a-popconfirm
              v-else-if="post.status === 'RECYCLE'"
              :title="'你确定要永久删除【' + post.title + '】文章？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleDeleteClick(post.id)"
            >
              <a-button class="!p-0" type="link">删除</a-button>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a-button class="!p-0" type="link" @click="handleShowPostSettings(post)">设置</a-button>
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

    <PostSettingModal
      :loading="postSettingLoading"
      :post="selectedPost"
      :savedCallback="onPostSavedCallback"
      :visible.sync="postSettingVisible"
      @onClose="selectedPost = {}"
    >
      <template #extraFooter>
        <a-button :disabled="selectPreviousButtonDisabled" @click="handleSelectPrevious"> 上一篇</a-button>
        <a-button :disabled="selectNextButtonDisabled" @click="handleSelectNext"> 下一篇</a-button>
      </template>
    </PostSettingModal>

    <TargetCommentDrawer
      :id="selectedPost.id"
      :description="selectedPost.summary"
      :target="`posts`"
      :title="selectedPost.title"
      :visible="postCommentVisible"
      @close="onPostCommentsClose"
    />
  </page-view>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { PageView } from '@/layouts'
import PostSettingModal from './components/PostSettingModal.vue'
import TargetCommentDrawer from '../comment/components/TargetCommentDrawer'
import apiClient from '@/utils/api-client'
import { postStatuses } from '@/core/constant'

const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    width: '200px',
    ellipsis: true,
    scopedSlots: { customRender: 'postTitle' }
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: '100px',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '分类',
    dataIndex: 'categories',
    scopedSlots: { customRender: 'categories' }
  },
  {
    title: '标签',
    dataIndex: 'tags',
    scopedSlots: { customRender: 'tags' }
  },
  {
    title: '评论',
    width: '70px',
    dataIndex: 'commentCount',
    scopedSlots: { customRender: 'commentCount' }
  },
  {
    title: '访问',
    width: '70px',
    dataIndex: 'visits',
    scopedSlots: { customRender: 'visits' }
  },
  {
    title: '发布时间',
    dataIndex: 'createTime',
    width: '170px',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    width: '180px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'PostList',
  components: {
    PageView,
    PostSettingModal,
    TargetCommentDrawer
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      columns,
      postStatuses,
      list: {
        data: [],
        loading: false,
        total: 0,
        hasPrevious: false,
        hasNext: false,
        params: {
          page: 0,
          size: 10,
          keyword: undefined,
          categoryId: undefined,
          status: undefined
        }
      },

      categories: {
        data: [],
        loading: false
      },

      selectedRowKeys: [],
      postSettingVisible: false,
      postSettingLoading: false,
      postCommentVisible: false,
      selectedPost: {}
    }
  },
  computed: {
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    },
    selectPreviousButtonDisabled() {
      const index = this.list.data.findIndex(post => post.id === this.selectedPost.id)
      return index === 0 && !this.list.hasPrevious
    },
    selectNextButtonDisabled() {
      const index = this.list.data.findIndex(post => post.id === this.selectedPost.id)
      return index === this.list.data.length - 1 && !this.list.hasNext
    }
  },
  beforeMount() {
    this.handleListCategories()
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.query.page) {
        vm.list.params.page = Number(to.query.page)
      }
      if (to.query.size) {
        vm.list.params.size = Number(to.query.size)
      }

      vm.list.params.sort = to.query.sort
      vm.list.params.keyword = to.query.keyword
      vm.list.params.categoryId = to.query.categoryId
      vm.list.params.status = to.query.status

      vm.handleListPosts()
    })
  },
  watch: {
    'list.params': {
      deep: true,
      handler: function (newVal) {
        if (newVal) {
          const params = JSON.parse(JSON.stringify(this.list.params))
          const path = this.$router.history.current.path
          this.$router.push({ path, query: params }).catch(err => err)
        }
      }
    }
  },
  methods: {
    /**
     * Fetch post data
     */
    async handleListPosts(enableLoading = true) {
      try {
        if (enableLoading) {
          this.list.loading = true
        }
        const response = await apiClient.post.list(this.list.params)

        this.list.data = response.data.content
        this.list.total = response.data.total
        this.list.hasPrevious = response.data.hasPrevious
        this.list.hasNext = response.data.hasNext
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.list.loading = false
      }
    },

    /**
     * Fetch categories data
     */
    async handleListCategories() {
      try {
        this.categories.loading = true

        const response = await apiClient.category.list({ sort: [], more: true })

        this.categories.data = response.data
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.categories.loading = false
      }
    },
    handleEditClick(post) {
      this.$router.push({ name: 'PostEdit', query: { postId: post.id } })
    },
    onSelectionChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
      this.$log.debug(`SelectedRowKeys: ${selectedRowKeys}`)
    },
    getCheckboxProps(post) {
      return {
        props: {
          disabled: this.list.params.status == null || this.list.params.status === '',
          name: post.title
        }
      }
    },

    /**
     * Handle page change
     */
    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleListPosts()
    },

    /**
     * Handle page size change
     */
    handlePageSizeChange(current, size) {
      this.$log.debug(`Current: ${current}, PageSize: ${size}`)
      this.list.params.page = 0
      this.list.params.size = size
      this.handleListPosts()
    },

    /**
     * Reset query params
     */
    handleResetParam() {
      this.list.params.keyword = undefined
      this.list.params.categoryId = undefined
      this.list.params.status = undefined
      this.handleClearRowKeys()
      this.handlePageChange(1)
      this.handleListCategories()
    },
    handleQuery() {
      this.handleClearRowKeys()
      this.handlePageChange(1)
    },
    handleSelectCategory(category) {
      this.list.params.categoryId = category.id
      this.handleQuery()
    },
    handleEditStatusClick(postId, status) {
      apiClient.post
        .updateStatusById(postId, status)
        .then(() => {
          this.$message.success('操作成功！')
        })
        .finally(() => {
          this.handleListPosts()
        })
    },
    handleDeleteClick(postId) {
      apiClient.post
        .delete(postId)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListPosts()
        })
    },
    handleEditStatusMore(status) {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      apiClient.post
        .updateStatusInBatch(this.selectedRowKeys, status)
        .then(() => {
          this.$log.debug(`postId: ${this.selectedRowKeys}, status: ${status}`)
          this.selectedRowKeys = []
        })
        .finally(() => {
          this.handleListPosts()
        })
    },
    handleDeleteMore() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      apiClient.post
        .deleteInBatch(this.selectedRowKeys)
        .then(() => {
          this.$log.debug(`delete: ${this.selectedRowKeys}`)
          this.selectedRowKeys = []
        })
        .finally(() => {
          this.handleListPosts()
        })
    },
    handleShowPostSettings(post) {
      this.postSettingVisible = true
      this.postSettingLoading = true
      apiClient.post
        .get(post.id)
        .then(response => {
          this.selectedPost = response.data
        })
        .finally(() => {
          this.postSettingLoading = false
        })
    },
    handleShowPostComments(post) {
      apiClient.post.get(post.id).then(response => {
        this.selectedPost = response.data
        this.postCommentVisible = true
      })
    },
    handlePreview(postId) {
      apiClient.post.getPreviewLinkById(postId).then(response => {
        window.open(response, '_blank')
      })
    },
    handleClearRowKeys() {
      this.selectedRowKeys = []
    },
    onPostSavedCallback() {
      this.handleListPosts(false)
    },
    onPostCommentsClose() {
      this.postCommentVisible = false
      this.selectedPost = {}
      setTimeout(() => {
        this.handleListPosts(false)
      }, 500)
    },

    /**
     * Select previous post
     */
    async handleSelectPrevious() {
      const index = this.list.data.findIndex(post => post.id === this.selectedPost.id)
      if (index > 0) {
        this.postSettingLoading = true
        const response = await apiClient.post.get(this.list.data[index - 1].id)
        this.selectedPost = response.data
        this.postSettingLoading = false
        return
      }
      if (index === 0 && this.list.hasPrevious) {
        this.list.params.page--
        await this.handleListPosts()
        this.postSettingLoading = true
        const response = await apiClient.post.get(this.list.data[this.list.data.length - 1].id)
        this.selectedPost = response.data
        this.postSettingLoading = false
      }
    },

    /**
     * Select next post
     */
    async handleSelectNext() {
      const index = this.list.data.findIndex(post => post.id === this.selectedPost.id)
      if (index < this.list.data.length - 1) {
        this.postSettingLoading = true
        const response = await apiClient.post.get(this.list.data[index + 1].id)
        this.selectedPost = response.data
        this.postSettingLoading = false
        return
      }
      if (index === this.list.data.length - 1 && this.list.hasNext) {
        this.list.params.page++
        await this.handleListPosts()

        this.postSettingLoading = true
        const response = await apiClient.post.get(this.list.data[0].id)
        this.selectedPost = response.data
        this.postSettingLoading = false
      }
    }
  },
  filters: {
    statusText(type) {
      return type ? postStatuses[type].text : ''
    }
  }
}
</script>

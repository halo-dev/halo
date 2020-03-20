<template>
  <div>
    <a-card
      :bordered="false"
      :bodyStyle="{ padding: '16px' }"
    >
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="关键词：">
                <a-input
                  v-model="queryParam.keyword"
                  @keyup.enter="handleQuery()"
                />
              </a-form-item>
            </a-col>
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="文章状态：">
                <a-select
                  v-model="queryParam.status"
                  placeholder="请选择文章状态"
                  @change="handleQuery()"
                >
                  <a-select-option
                    v-for="status in Object.keys(postStatus)"
                    :key="status"
                    :value="status"
                  >{{ postStatus[status].text }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="分类目录：">
                <a-select
                  v-model="queryParam.categoryId"
                  placeholder="请选择分类"
                  @change="handleQuery()"
                >
                  <a-select-option
                    v-for="category in categories"
                    :key="category.id"
                  >{{ category.name }} ({{ category.postCount }})</a-select-option>
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
                  @click="handleQuery()"
                >查询</a-button>
                <a-button
                  style="margin-left: 8px;"
                  @click="handleResetParam()"
                >重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <router-link :to="{name:'PostEdit'}">
          <a-button
            type="primary"
            icon="plus"
          >写文章</a-button>
        </router-link>
        <a-dropdown v-show="queryParam.status!=null && queryParam.status!='' && !isMobile()">
          <a-menu slot="overlay">
            <a-menu-item
              key="1"
              v-if="queryParam.status === 'DRAFT' || queryParam.status === 'RECYCLE'"
            >
              <a
                href="javascript:void(0);"
                @click="handleEditStatusMore(postStatus.PUBLISHED.value)"
              >
                <span>发布</span>
              </a>
            </a-menu-item>
            <a-menu-item
              key="2"
              v-if="queryParam.status === 'PUBLISHED' || queryParam.status ==='DRAFT' || queryParam.status === 'INTIMATE'"
            >
              <a
                href="javascript:void(0);"
                @click="handleEditStatusMore(postStatus.RECYCLE.value)"
              >
                <span>移到回收站</span>
              </a>
            </a-menu-item>
            <a-menu-item
              key="3"
              v-if="queryParam.status === 'RECYCLE' || queryParam.status === 'PUBLISHED' || queryParam.status === 'INTIMATE'"
            >
              <a
                href="javascript:void(0);"
                @click="handleEditStatusMore(postStatus.DRAFT.value)"
              >
                <span>草稿</span>
              </a>
            </a-menu-item>
            <a-menu-item
              key="4"
              v-if="queryParam.status === 'RECYCLE' || queryParam.status === 'DRAFT'"
            >
              <a
                href="javascript:void(0);"
                @click="handleDeleteMore"
              >
                <span>永久删除</span>
              </a>
            </a-menu-item>
          </a-menu>
          <a-button style="margin-left: 8px;">
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>
      <div style="margin-top:15px">

        <!-- Mobile -->
        <a-list
          v-if="isMobile()"
          itemLayout="vertical"
          size="large"
          :pagination="false"
          :dataSource="formattedPosts"
          :loading="postsLoading"
        >
          <a-list-item
            slot="renderItem"
            slot-scope="item, index"
            :key="index"
          >
            <template slot="actions">
              <span>
                <a-icon type="eye" />
                {{ item.visits }}
              </span>
              <span @click="handleShowPostComments(item)">
                <a-icon type="message" />
                {{ item.commentCount }}
              </span>
              <a-dropdown
                placement="topLeft"
                :trigger="['click']"
              >
                <span>
                  <a-icon type="bars" />
                </span>
                <a-menu slot="overlay">
                  <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'DRAFT' || item.status === 'INTIMATE'">
                    <a
                      href="javascript:;"
                      @click="handleEditClick(item)"
                    >编辑</a>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'RECYCLE'">
                    <a-popconfirm
                      :title="'你确定要发布【' + item.title + '】文章？'"
                      @confirm="handleEditStatusClick(item.id,'PUBLISHED')"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:;">还原</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'DRAFT' || item.status === 'INTIMATE'">
                    <a-popconfirm
                      :title="'你确定要将【' + item.title + '】文章移到回收站？'"
                      @confirm="handleEditStatusClick(item.id,'RECYCLE')"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:;">回收站</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item v-else-if="item.status === 'RECYCLE'">
                    <a-popconfirm
                      :title="'你确定要永久删除【' + item.title + '】文章？'"
                      @confirm="handleDeleteClick(item.id)"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:;">删除</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item>
                    <a
                      rel="noopener noreferrer"
                      href="javascript:void(0);"
                      @click="handleShowPostSettings(item)"
                    >设置</a>
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </template>
            <template slot="extra">
              <span>
                <a-badge
                  :status="item.statusProperty.status"
                  :text="item.statusProperty.text"
                />
              </span>
            </template>
            <a-list-item-meta>
              <template slot="description">
                {{ item.createTime | moment }}
              </template>
              <span
                slot="title"
                style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
              >
                <a-icon
                  type="pushpin"
                  v-if="item.topPriority!=0"
                  theme="twoTone"
                  twoToneColor="red"
                  style="margin-right: 3px;"
                />
                <a
                  v-if="item.status=='PUBLISHED' || item.status == 'INTIMATE'"
                  :href="item.fullPath"
                  target="_blank"
                  style="text-decoration: none;"
                >
                  <a-tooltip
                    placement="top"
                    :title="'点击访问【'+item.title+'】'"
                  >{{ item.title }}</a-tooltip>
                </a>
                <a
                  v-else-if="item.status=='DRAFT'"
                  href="javascript:void(0)"
                  style="text-decoration: none;"
                  @click="handlePreview(item.id)"
                >
                  <a-tooltip
                    placement="topLeft"
                    :title="'点击预览【'+item.title+'】'"
                  >{{ item.title }}</a-tooltip>
                </a>
                <a
                  v-else
                  href="javascript:void(0);"
                  style="text-decoration: none;"
                  disabled
                >
                  {{ item.title }}
                </a>
              </span>

            </a-list-item-meta>
            <span>
              {{ item.summary }}...
            </span>
            <br />
            <br />
            <a-tag
              v-for="(category,categoryIndex) in item.categories"
              :key="'category_'+categoryIndex"
              color="blue"
              style="margin-bottom: 8px"
            >{{ category.name }}</a-tag>
            <br />
            <a-tag
              v-for="(tag, tagIndex) in item.tags"
              :key="'tag_'+tagIndex"
              color="green"
              style="margin-bottom: 8px"
            >{{ tag.name }}</a-tag>

          </a-list-item>
        </a-list>

        <!-- Desktop -->
        <a-table
          v-else
          :rowKey="post => post.id"
          :rowSelection="{
            selectedRowKeys: selectedRowKeys,
            onChange: onSelectionChange,
            getCheckboxProps: getCheckboxProps
          }"
          :columns="columns"
          :dataSource="formattedPosts"
          :loading="postsLoading"
          :pagination="false"
          :scrollToFirstRowOnChange="true"
        >
          <span
            slot="postTitle"
            slot-scope="text,record"
          >
            <a-icon
              type="pushpin"
              v-if="record.topPriority!=0"
              theme="twoTone"
              twoToneColor="red"
              style="margin-right: 3px;"
            />
            <a
              v-if="record.status=='PUBLISHED' || record.status == 'INTIMATE'"
              :href="record.fullPath"
              target="_blank"
              style="text-decoration: none;"
            >
              <a-tooltip
                placement="top"
                :title="'点击访问【'+text+'】'"
              >{{ text }}</a-tooltip>
            </a>
            <a
              v-else-if="record.status=='DRAFT'"
              href="javascript:void(0)"
              style="text-decoration: none;"
              @click="handlePreview(record.id)"
            >
              <a-tooltip
                placement="topLeft"
                :title="'点击预览【'+text+'】'"
              >{{ text }}</a-tooltip>
            </a>
            <a
              v-else
              href="javascript:void(0);"
              style="text-decoration: none;"
              disabled
            >
              {{ text }}
            </a>
          </span>
          <span
            slot="status"
            slot-scope="statusProperty"
          >
            <a-badge
              :status="statusProperty.status"
              :text="statusProperty.text"
            />
          </span>

          <span
            slot="categories"
            slot-scope="categoriesOfPost"
          >
            <a-tag
              v-for="(category,index) in categoriesOfPost"
              :key="index"
              color="blue"
              style="margin-bottom: 8px"
            >{{ category.name }}</a-tag>
          </span>

          <span
            slot="tags"
            slot-scope="tags"
          >
            <a-tag
              v-for="(tag, index) in tags"
              :key="index"
              color="green"
              style="margin-bottom: 8px"
            >{{ tag.name }}</a-tag>
          </span>

          <span
            slot="commentCount"
            slot-scope="text,record"
            @click="handleShowPostComments(record)"
            style="cursor: pointer;"
          >
            <a-badge
              :count="record.commentCount"
              :numberStyle="{backgroundColor: '#f38181'} "
              :showZero="true"
              :overflowCount="999"
            />
          </span>

          <span
            slot="visits"
            slot-scope="visits"
          >
            <a-badge
              :count="visits"
              :numberStyle="{backgroundColor: '#00e0ff'} "
              :showZero="true"
              :overflowCount="9999"
            />
          </span>

          <span
            slot="createTime"
            slot-scope="createTime"
          >
            <a-tooltip placement="top">
              <template slot="title">
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </span>

          <span
            slot="action"
            slot-scope="text, post"
          >
            <a
              href="javascript:;"
              @click="handleEditClick(post)"
              v-if="post.status === 'PUBLISHED' || post.status === 'DRAFT' || post.status === 'INTIMATE'"
            >编辑</a>
            <a-popconfirm
              :title="'你确定要发布【' + post.title + '】文章？'"
              @confirm="handleEditStatusClick(post.id,'PUBLISHED')"
              okText="确定"
              cancelText="取消"
              v-else-if="post.status === 'RECYCLE'"
            >
              <a href="javascript:;">还原</a>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a-popconfirm
              :title="'你确定要将【' + post.title + '】文章移到回收站？'"
              @confirm="handleEditStatusClick(post.id,'RECYCLE')"
              okText="确定"
              cancelText="取消"
              v-if="post.status === 'PUBLISHED' || post.status === 'DRAFT' || post.status === 'INTIMATE'"
            >
              <a href="javascript:;">回收站</a>
            </a-popconfirm>

            <a-popconfirm
              :title="'你确定要永久删除【' + post.title + '】文章？'"
              @confirm="handleDeleteClick(post.id)"
              okText="确定"
              cancelText="取消"
              v-else-if="post.status === 'RECYCLE'"
            >
              <a href="javascript:;">删除</a>
            </a-popconfirm>

            <a-divider type="vertical" />

            <a
              href="javascript:;"
              @click="handleShowPostSettings(post)"
            >设置</a>
          </span>
        </a-table>
        <div class="page-wrapper">
          <a-pagination
            class="pagination"
            :current="pagination.page"
            :total="pagination.total"
            :defaultPageSize="pagination.size"
            :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
            showSizeChanger
            @showSizeChange="handlePaginationChange"
            @change="handlePaginationChange"
          />
        </div>
      </div>
    </a-card>

    <PostSettingDrawer
      :post="selectedPost"
      :tagIds="selectedTagIds"
      :categoryIds="selectedCategoryIds"
      :metas="selectedMetas"
      :needTitle="true"
      :saveDraftButton="false"
      :visible="postSettingVisible"
      @close="onPostSettingsClose"
      @onRefreshPost="onRefreshPostFromSetting"
      @onRefreshTagIds="onRefreshTagIdsFromSetting"
      @onRefreshCategoryIds="onRefreshCategoryIdsFromSetting"
      @onRefreshPostMetas="onRefreshPostMetasFromSetting"
    />

    <TargetCommentDrawer
      :visible="postCommentVisible"
      :title="selectedPost.title"
      :description="selectedPost.summary"
      :target="`posts`"
      :id="selectedPost.id"
      @close="onPostCommentsClose"
    />
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import PostSettingDrawer from './components/PostSettingDrawer'
import TargetCommentDrawer from '../comment/components/TargetCommentDrawer'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import TagSelect from './components/TagSelect'
import CategoryTree from './components/CategoryTree'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    width: '150px',
    ellipsis: true,
    scopedSlots: { customRender: 'postTitle' }
  },
  {
    title: '状态',
    className: 'status',
    dataIndex: 'statusProperty',
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
    AttachmentSelectDrawer,
    TagSelect,
    CategoryTree,
    PostSettingDrawer,
    TargetCommentDrawer
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      postStatus: postApi.postStatus,
      pagination: {
        page: 1,
        size: 10,
        sort: null,
        total: 1
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        categoryId: null,
        status: null
      },
      // 表头
      columns,
      selectedRowKeys: [],
      categories: [],
      selectedMetas: [
        {
          key: '',
          value: ''
        }
      ],
      posts: [],
      postsLoading: false,
      postSettingVisible: false,
      postCommentVisible: false,
      selectedPost: {},
      selectedTagIds: [],
      selectedCategoryIds: []
    }
  },
  computed: {
    formattedPosts() {
      return this.posts.map(post => {
        post.statusProperty = this.postStatus[post.status]
        return post
      })
    }
  },
  created() {
    this.loadPosts()
    this.loadCategories()
  },
  destroyed: function() {
    if (this.postSettingVisible) {
      this.postSettingVisible = false
    }
  },
  beforeRouteEnter(to, from, next) {
    next(vm => {
      vm.queryParam.page = to.query.page
      vm.queryParam.size = to.query.size
      vm.queryParam.sort = to.query.sort
      vm.queryParam.keyword = to.query.keyword
      vm.queryParam.categoryId = to.query.categoryId
      vm.queryParam.status = to.query.status
    })
  },
  beforeRouteLeave(to, from, next) {
    if (this.postSettingVisible) {
      this.postSettingVisible = false
    }
    next()
  },
  watch: {
    queryParam: {
      deep: true,
      handler: function(newVal, oldVal) {
        if (newVal) {
          const params = JSON.parse(JSON.stringify(this.queryParam))
          const path = this.$router.history.current.path
          this.$router.push({ path, query: params }).catch(err => err)
        }
      }
    }
  },
  methods: {
    loadPosts() {
      this.postsLoading = true
      // Set from pagination
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      postApi.query(this.queryParam).then(response => {
        this.posts = response.data.data.content
        this.pagination.total = response.data.data.total
        this.postsLoading = false
      })
    },
    loadCategories() {
      categoryApi.listAll(true).then(response => {
        this.categories = response.data.data
      })
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
          disabled: this.queryParam.status == null || this.queryParam.status === '',
          name: post.title
        }
      }
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadPosts()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.categoryId = null
      this.queryParam.status = null
      this.handleClearRowKeys()
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleQuery() {
      this.handleClearRowKeys()
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleEditStatusClick(postId, status) {
      postApi.updateStatus(postId, status).then(response => {
        this.$message.success('操作成功！')
        this.loadPosts()
      })
    },
    handleDeleteClick(postId) {
      postApi.delete(postId).then(response => {
        this.$message.success('删除成功！')
        this.loadPosts()
      })
    },
    handleEditStatusMore(status) {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      postApi.updateStatusInBatch(this.selectedRowKeys, status).then(response => {
        this.$log.debug(`postId: ${this.selectedRowKeys}, status: ${status}`)
        this.selectedRowKeys = []
        this.loadPosts()
      })
    },
    handleDeleteMore() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.info('请至少选择一项！')
        return
      }
      postApi.deleteInBatch(this.selectedRowKeys).then(response => {
        this.$log.debug(`delete: ${this.selectedRowKeys}`)
        this.selectedRowKeys = []
        this.loadPosts()
      })
    },
    handleShowPostSettings(post) {
      postApi.get(post.id).then(response => {
        this.selectedPost = response.data.data
        this.selectedTagIds = this.selectedPost.tagIds
        this.selectedCategoryIds = this.selectedPost.categoryIds
        this.selectedMetas = this.selectedPost.metas
        this.postSettingVisible = true
      })
    },
    handleShowPostComments(post) {
      postApi.get(post.id).then(response => {
        this.selectedPost = response.data.data
        this.postCommentVisible = true
      })
    },
    handlePreview(postId) {
      postApi.preview(postId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handleClearRowKeys() {
      this.selectedRowKeys = []
    },
    // 关闭文章设置抽屉
    onPostSettingsClose() {
      this.postSettingVisible = false
      this.selectedPost = {}
      setTimeout(() => {
        this.loadPosts()
      }, 500)
    },
    onPostCommentsClose() {
      this.postCommentVisible = false
      this.selectedPost = {}
      setTimeout(() => {
        this.loadPosts()
      }, 500)
    },
    onRefreshPostFromSetting(post) {
      this.selectedPost = post
    },
    onRefreshTagIdsFromSetting(tagIds) {
      this.selectedTagIds = tagIds
    },
    onRefreshCategoryIdsFromSetting(categoryIds) {
      this.selectedCategoryIds = categoryIds
    },
    onRefreshPostMetasFromSetting(metas) {
      this.selectedMetas = metas
    }
  }
}
</script>

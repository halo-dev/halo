<template>
  <div class="page-header-index-wide">
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
              <a-form-item label="文章状态">
                <a-select
                  v-model="queryParam.status"
                  placeholder="请选择文章状态"
                  @change="handleQuery"
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
              <a-form-item label="分类目录">
                <a-select
                  v-model="queryParam.categoryId"
                  placeholder="请选择分类"
                  @change="handleQuery"
                >
                  <a-select-option
                    v-for="category in categories"
                    :key="category.id"
                  >{{ category.name }}</a-select-option>
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
                  @click="handleQuery"
                >查询</a-button>
                <a-button
                  style="margin-left: 8px;"
                  @click="handleResetParam"
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
        <a-dropdown v-show="queryParam.status!=null && queryParam.status!=''">
          <a-menu slot="overlay">
            <a-menu-item
              key="1"
              v-if="queryParam.status === 'DRAFT'"
            >
              <a
                href="javascript:void(0);"
                @click="handlePublishMore"
              >
                <span>发布</span>
              </a>
            </a-menu-item>
            <a-menu-item
              key="2"
              v-if="queryParam.status === 'PUBLISHED' || queryParam.status ==='DRAFT'"
            >
              <a
                href="javascript:void(0);"
                @click="handleRecycleMore"
              >
                <span>移到回收站</span>
              </a>
            </a-menu-item>
            <a-menu-item
              key="3"
              v-if="queryParam.status === 'RECYCLE'"
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
        <a-table
          :rowKey="post => post.id"
          :rowSelection="{
            onChange: onSelectionChange,
            getCheckboxProps: getCheckboxProps
          }"
          :columns="columns"
          :dataSource="formattedPosts"
          :loading="postsLoading"
          :pagination="false"
        >
          <!-- ellipsis内嵌a标签后文本会被置空
           <ellipsis
            :length="25"
            tooltip
            slot="postTitle"
            slot-scope="text,record"
          >
          {{ text }}
          </ellipsis> -->
          <span
            slot="postTitle"
            slot-scope="text,record"
            style="max-width: 150px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
          >
            <a
              :href="options.blog_url+'/archives/'+record.url"
              target="_blank"
              style="text-decoration: none;"
            >
              <a-tooltip
                placement="topLeft"
                :title="'点击访问【'+text+'】'"
              >{{ text }}</a-tooltip>
            </a>
          </span>
          <span
            slot="status"
            slot-scope="statusProperty"
          >
            <a-badge :status="statusProperty.status" />
            {{ statusProperty.text }}
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
              v-if="post.status === 'PUBLISHED' || post.status === 'DRAFT'"
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
              v-if="post.status === 'PUBLISHED' || post.status === 'DRAFT'"
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
            :total="pagination.total"
            :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
            showSizeChanger
            @showSizeChange="handlePaginationChange"
            @change="handlePaginationChange"
          />
        </div>
      </div>
    </a-card>

    <PostSetting
      :post="selectedPost"
      :tagIds="selectedTagIds"
      :categoryIds="selectedCategoryIds"
      :needTitle="true"
      :saveDraftButton="false"
      :savePublishButton="false"
      :saveButton="true"
      v-model="postSettingVisible"
      @close="onPostSettingsClose"
      @onRefreshPost="onRefreshPostFromSetting"
      @onRefreshTagIds="onRefreshTagIdsFromSetting"
      @onRefreshCategoryIds="onRefreshCategoryIdsFromSetting"
    />
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import PostSetting from './components/PostSetting'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import TagSelect from './components/TagSelect'
import CategoryTree from './components/CategoryTree'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
import optionApi from '@/api/option'
const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    width: '150px',
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
    dataIndex: 'commentCount'
  },
  {
    title: '访问',
    width: '70px',
    dataIndex: 'visits'
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
    PostSetting
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      postStatus: postApi.postStatus,
      pagination: {
        current: 1,
        pageSize: 10,
        sort: null
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
      selectedRows: [],
      categories: [],
      posts: [],
      postsLoading: false,
      postSettingVisible: false,
      selectedPost: {},
      selectedTagIds: [],
      selectedCategoryIds: [],
      options: [],
      keys: ['blog_url']
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
    this.loadOptions()
    this.loadCategories()
  },
  methods: {
    loadPosts() {
      this.postsLoading = true
      // Set from pagination
      this.queryParam.page = this.pagination.current - 1
      this.queryParam.size = this.pagination.pageSize
      this.queryParam.sort = this.pagination.sort
      postApi.query(this.queryParam).then(response => {
        this.posts = response.data.data.content
        this.pagination.total = response.data.data.total
        this.postsLoading = false
      })
    },
    loadCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
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
          disabled: post.status === 'RECYCLE',
          name: post.title
        }
      }
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.current = page
      this.pagination.pageSize = pageSize
      this.loadPosts()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.categoryId = null
      this.queryParam.status = null
      this.loadPosts()
    },
    handleQuery() {
      this.queryParam.page = 0
      this.loadPosts()
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
    handlePublishMore() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.success('请至少选择一项！')
        return
      }
      for (let index = 0; index < this.selectedRowKeys.length; index++) {
        const element = this.selectedRowKeys[index]
        postApi.updateStatus(element, 'PUBLISHED').then(response => {
          this.$log.debug(`postId: ${element}, status: PUBLISHED`)
          this.selectedRowKeys = []
          this.loadPosts()
        })
      }
    },
    handleRecycleMore() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.success('请至少选择一项！')
        return
      }
      for (let index = 0; index < this.selectedRowKeys.length; index++) {
        const element = this.selectedRowKeys[index]
        postApi.updateStatus(element, 'RECYCLE').then(response => {
          this.$log.debug(`postId: ${element}, status: RECYCLE`)
          this.selectedRowKeys = []
          this.loadPosts()
        })
      }
    },
    handleDeleteMore() {
      if (this.selectedRowKeys.length <= 0) {
        this.$message.success('请至少选择一项！')
        return
      }
      for (let index = 0; index < this.selectedRowKeys.length; index++) {
        const element = this.selectedRowKeys[index]
        postApi.delete(element).then(response => {
          this.$log.debug(`delete: ${element}`)
          this.selectedRowKeys = []
          this.loadPosts()
        })
      }
    },
    handleShowPostSettings(post) {
      postApi.get(post.id).then(response => {
        this.selectedPost = response.data.data
        this.selectedTagIds = this.selectedPost.tagIds
        this.selectedCategoryIds = this.selectedPost.categoryIds
        this.postSettingVisible = true
      })
    },
    // 关闭文章设置抽屉
    onPostSettingsClose() {
      this.postSettingVisible = false
      this.selectedPost = {}
    },
    onRefreshPostFromSetting(post) {
      this.selectedPost = post
    },
    onRefreshTagIdsFromSetting(tagIds) {
      this.selectedTagIds = tagIds
    },
    onRefreshCategoryIdsFromSetting(categoryIds) {
      this.selectedCategoryIds = categoryIds
    }
  }
}
</script>
<style scoped>
</style>

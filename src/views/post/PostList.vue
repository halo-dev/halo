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
            class="post-title"
          >
            <a
              :href="options.blog_url+'/archives/'+record.url"
              target="_blank"
            >
              <a-tooltip placement="topLeft" :title="'点击预览 '+text">{{ text }}</a-tooltip>
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
            >{{ tag.name }}</a-tag>
          </span>

          <span
            slot="createTime"
            slot-scope="createTime"
          >{{ createTime | timeAgo }}</span>

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
              @click="handlePostSettingsDrawer(post)"
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

    <a-drawer
      title="文章设置"
      :width="isMobile()?'100%':'460'"
      placement="right"
      closable
      @close="onPostSettingsClose"
      :visible="postSettingVisible"
    >
      <div class="post-setting-drawer-content">
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">基本设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item label="文章标题：">
                <a-input v-model="selectedPost.title" />
              </a-form-item>
              <a-form-item
                label="文章路径："
                :help="options.blog_url+'/archives/' + (selectedPost.url ? selectedPost.url : '{auto_generate}')"
              >
                <a-input v-model="selectedPost.url" />
              </a-form-item>
              <a-form-item label="开启评论：">
                <a-radio-group
                  v-model="selectedPost.disallowComment"
                  :defaultValue="false"
                >
                  <a-radio :value="false">开启</a-radio>
                  <a-radio :value="true">关闭</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">分类目录</h3>
          <div class="post-setting-drawer-item">
            <category-tree
              v-model="selectedCategoryIds"
              :categories="categories"
            />
            <div>
              <a-form layout="vertical">
                <a-form-item v-if="categoryForm">
                  <category-select-tree
                    :categories="categories"
                    v-model="categoryToCreate.parentId"
                  />
                </a-form-item>
                <a-form-item v-if="categoryForm">
                  <a-input
                    placeholder="分类名称"
                    v-model="categoryToCreate.name"
                  />
                </a-form-item>
                <a-form-item v-if="categoryForm">
                  <a-input
                    placeholder="分类路径"
                    v-model="categoryToCreate.slugNames"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    style="marginRight: 8px"
                    v-if="categoryForm"
                    @click="handlerCreateCategory"
                  >保存</a-button>
                  <a-button
                    type="dashed"
                    style="marginRight: 8px"
                    v-if="!categoryForm"
                    @click="toggleCategoryForm"
                  >新增</a-button>
                  <a-button
                    v-if="categoryForm"
                    @click="toggleCategoryForm"
                  >取消</a-button>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">标签</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item>
                <TagSelect v-model="selectedTagIds" />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">摘要</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item>
                <a-input
                  type="textarea"
                  :autosize="{ minRows: 5 }"
                  v-model="selectedPost.summary"
                  placeholder="不填写则会自动生成"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">缩略图</h3>
          <div class="post-setting-drawer-item">
            <div class="post-thum">
              <img
                class="img"
                :src="selectedPost.thumbnail || '//i.loli.net/2019/05/05/5ccf007c0a01d.png'"
                @click="()=>this.thumDrawerVisible=true"
              >
              <a-button
                class="post-thum-remove"
                type="dashed"
                @click="handlerRemoveThumb"
              >移除</a-button>
            </div>
          </div>
        </div>
        <a-divider class="divider-transparent" />
      </div>
      <AttachmentSelectDrawer
        v-model="thumDrawerVisible"
        @listenToSelect="handleSelectPostThumb"
        :drawerWidth="460"
      />
      <div class="bottom-control">
        <a-button
          @click="handleSavePostSettingsClick"
          type="primary"
        >保存</a-button>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import categoryApi from '@/api/category'
import postApi from '@/api/post'
import optionApi from '@/api/option'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import TagSelect from './components/TagSelect'
import CategoryTree from './components/CategoryTree'
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
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '分类目录',
    dataIndex: 'categories',
    scopedSlots: { customRender: 'categories' }
  },
  {
    title: '标签',
    dataIndex: 'tags',
    scopedSlots: { customRender: 'tags' }
  },
  {
    title: '评论量',
    dataIndex: 'commentCount'
  },
  {
    title: '访问量',
    dataIndex: 'visits'
  },
  {
    title: '发布时间',
    dataIndex: 'createTime',
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
    CategoryTree
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
      thumDrawerVisible: false,
      selectedPost: {},
      selectedCategoryIds: [],
      selectedTagIds: [],
      categoryForm: false,
      categoryToCreate: {},
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
    this.loadCategories()
    this.loadPosts()
    this.loadOptions()
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
    // 打开文章设置抽屉
    handlePostSettingsDrawer(post) {
      this.postSettingVisible = true
      postApi.get(post.id).then(response => {
        const post = response.data.data
        this.selectedPost = post
        this.selectedTagIds = post.tagIds
        this.selectedCategoryIds = post.categoryIds
      })
    },
    handleSelectPostThumb(data) {
      this.selectedPost.thumbnail = data.path
      this.thumDrawerVisible = false
    },
    handlerRemoveThumb() {
      this.selectedPost.thumbnail = null
    },
    // 保存文章设置
    handleSavePostSettingsClick() {
      this.selectedPost.categoryIds = this.selectedCategoryIds
      this.selectedPost.tagIds = this.selectedTagIds
      postApi.update(this.selectedPost.id, this.selectedPost, false).then(response => {
        this.$log.debug('Updated post', response.data.data)
        this.loadPosts()
        this.$message.success('文章更新成功')
      })
    },
    toggleCategoryForm() {
      this.categoryForm = !this.categoryForm
    },
    handlerCreateCategory() {
      categoryApi.create(this.categoryToCreate).then(response => {
        this.loadCategories()
        this.categoryToCreate = {}
      })
    },
    // 关闭文章设置抽屉
    onPostSettingsClose() {
      this.postSettingVisible = false
      this.selectedPost = {}
      this.selectedTagIds = []
      this.selectedCategoryIds = []
    }
  }
}
</script>
<style scoped>
a {
  text-decoration: none;
}
.post-title {
  max-width: 150px;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

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
                  defaultValue="0"
                >
                  <a-select-option
                    v-for="status in Object.keys(postStatus)"
                    :key="status"
                    :value="status"
                  >
                    {{ postStatus[status].text }}
                  </a-select-option>
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
                  @click="loadPosts"
                >查询</a-button>
                <a-button
                  style="margin-left: 8px;"
                  @click="() => (queryParam = {})"
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
        <a-dropdown>
          <a-menu slot="overlay">
            <a-menu-item key="1">
              <a-icon type="delete" />移到回收站 </a-menu-item>
            <a-menu-item key="2">
              <a-icon type="delete" />永久删除 </a-menu-item>
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
          :pagination="pagination"
          :loading="postsLoading"
        >

          <span
            slot="status"
            slot-scope="statusProperty"
          >
            <a-badge :status="statusProperty.status" />{{ statusProperty.text }}
          </span>

          <span
            slot="categories"
            slot-scope="categoriesOfPost"
          >
            <a-tag
              v-for="(category,index) in categoriesOfPost"
              :key="index"
              color="blue"
            >
              {{ category.name }}
            </a-tag>
          </span>

          <span
            slot="tags"
            slot-scope="tags"
          >
            <a-tag
              v-for="(tag, index) in tags"
              :key="index"
              color="green"
            >
              {{ tag.name }}
            </a-tag>
          </span>

          <span
            slot="createTime"
            slot-scope="createTime"
          >
            {{ createTime | timeAgo }}
          </span>

          <span
            slot="updateTime"
            slot-scope="updateTime"
          >
            {{ updateTime | timeAgo }}
          </span>

          <span
            slot="action"
            slot-scope="text, record"
          >
            <a
              href="javascript:;"
              @click="editPost(record.id)"
            >编辑</a>
            <a-divider type="vertical" />
            <a
              href="javascript:;"
              @click="deletePost(record.id)"
            >删除</a>
          </span>
        </a-table>
      </div>
    </a-card>
  </div>
</template>

<script>
import categoryApi from '@/api/category'
import postApi from '@/api/post'

export default {
  name: 'PostList',
  components: {},
  data() {
    return {
      postStatus: postApi.postStatus,
      // 查询参数
      pagination: {},
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        categoryId: null,
        status: null
      },
      // 表头
      columns: [
        {
          title: 'ID',
          dataIndex: 'id'
        },
        {
          title: '标题',
          dataIndex: 'title'
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
          title: '创建时间',
          dataIndex: 'createTime',
          scopedSlots: { customRender: 'createTime' }
        },
        {
          title: '更新时间',
          dataIndex: 'updateTime',
          scopedSlots: { customRender: 'updateTime' }
        },
        {
          title: '操作',
          width: '150px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      selectedRowKeys: [],
      selectedRows: [],
      options: {},
      optionAlertShow: false,
      categories: [],
      posts: [],
      postsLoading: false
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
  },
  methods: {
    loadPosts() {
      this.postsLoading = true
      // Set from pagination
      this.queryParam.page = this.pagination.current
      this.queryParam.size = this.pagination.pageSize
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
    editPost(id) {
      this.$message.success('编辑')
    },
    deletePost(id) {
      this.$message.success('删除')
    },
    onSelectionChange(selectedRowKeys) {
      this.$log.debug(`SelectedRowKeys: ${selectedRowKeys}`)
    },
    getCheckboxProps(post) {
      return {
        props: {
          disabled: post.status === 'RECYCLE',
          name: post.title
        }
      }
    }
  }
}
</script>

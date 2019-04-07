<template>
  <div class="page-header-index-wide">
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="关键词">
                <a-input v-model="queryParam.id" placeholder />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="文章状态">
                <a-select v-model="queryParam.status" placeholder="请选择文章状态" defaultValue="0">
                  <a-select-option value="0">已发布</a-select-option>
                  <a-select-option value="1">草稿箱</a-select-option>
                  <a-select-option value="2">回收站</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="分类目录">
                <a-select v-model="queryParam.categoryId" placeholder="请选择分类">
                  <a-select-option v-for="category in categories" :key="category.id">{{ category.name }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px;" @click="() => (queryParam = {})">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <router-link :to="{name:'PostEdit'}">
          <a-button type="primary" icon="plus">写文章</a-button>
        </router-link>
        <a-dropdown>
          <a-menu slot="overlay">
            <a-menu-item key="1" v-if="postStatus==0 || postStatus==1"> <a-icon type="delete" />移到回收站 </a-menu-item>
            <a-menu-item key="1" v-else-if="postStatus==2"> <a-icon type="delete" />永久删除 </a-menu-item>
          </a-menu>
          <a-button style="margin-left: 8px;">
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>
      <div style="margin-top:15px">
        <a-table :columns="columns">
          <span slot="action" slot-scope="text, record">
            <a href="javascript:;" @click="editPost(record.id)">编辑</a>
            <a-divider type="vertical" />
            <a href="javascript:;" @click="deletePost(record.id)">删除</a>
          </span>
        </a-table>
      </div>
    </a-card>
  </div>
</template>

<script>
import categoryApi from '@/api/category'
export default {
  name: 'PostList',
  components: {},
  data() {
    return {
      mdl: {},
      // 查询参数
      queryParam: {},
      // 表头
      columns: [
        {
          title: '#',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: '标题',
          dataIndex: 'title'
        },
        {
          title: '分类目录',
          dataIndex: 'categories'
        },
        {
          title: '标签',
          dataIndex: 'tags'
        },
        {
          title: '评论量',
          dataIndex: 'comments'
        },
        {
          title: '访问量',
          dataIndex: 'views'
        },
        {
          title: '日期',
          dataIndex: 'createTime'
        },
        {
          title: '操作',
          dataIndex: 'action',
          width: '150px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      loadData: parameter => {},
      selectedRowKeys: [],
      selectedRows: [],
      options: {},
      optionAlertShow: false,
      categories: [],
      postStatus: 0
    }
  },
  created() {
    this.loadCategories()
  },
  methods: {
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
    }
  }
}
</script>

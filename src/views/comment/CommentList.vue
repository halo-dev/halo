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
                <a-select v-model="queryParam.status" placeholder="请选择" default-value="0">
                  <a-select-option value="0">已发布</a-select-option>
                  <a-select-option value="1">待审核</a-select-option>
                  <a-select-option value="2">回收站</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="12" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px;" @click="() => (queryParam = {})">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <a-dropdown>
          <a-menu slot="overlay">
            <a-menu-item key="1"> <a-icon type="delete" />回收站 </a-menu-item>
          </a-menu>
          <a-button
          >批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>
      <div style="margin-top:15px">
        <a-table :columns="columns" :dataSource="data">
          <a slot="name" slot-scope="text" href="javascript:;">{{ text }}</a>
          <span slot="customTitle"><a-icon type="smile-o" /> Name</span>
          <span slot="tags" slot-scope="tags">
            <a-tag v-for="tag in tags" color="blue" :key="tag">{{ tag }}</a-tag>
          </span>
          <span slot="action" slot-scope="text, record">
            <a href="javascript:;">Invite 一 {{ record.name }}</a>
            <a-divider type="vertical" />
            <a href="javascript:;">Delete</a>
            <a-divider type="vertical" />
            <a href="javascript:;" class="ant-dropdown-link"> More actions <a-icon type="down" /> </a>
          </span>
        </a-table>
      </div>
    </a-card>
  </div>
</template>

<script>
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
          title: '评论者',
          dataIndex: 'author'
        },
        {
          title: '内容',
          dataIndex: 'content'
        },
        {
          title: '评论页面',
          dataIndex: 'post.id'
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
      optionAlertShow: false
    }
  },
  created() {},
  methods: {}
}
</script>

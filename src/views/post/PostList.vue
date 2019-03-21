<template>
  <a-card :bordered="false">
    <div>
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="关键词">
                <a-input v-model="queryParam.id" placeholder/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="文章状态">
                <a-select v-model="queryParam.status" placeholder="请选择" default-value="0">
                  <a-select-option value="0">已发布</a-select-option>
                  <a-select-option value="1">草稿箱</a-select-option>
                  <a-select-option value="2">回收站</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="分类目录">
                <a-select v-model="queryParam.status" placeholder="请选择" default-value="0">
                  <a-select-option value="0">xx</a-select-option>
                  <a-select-option value="1">xx</a-select-option>
                  <a-select-option value="2">xx</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleEdit()">新建</a-button>
        <a-dropdown>
          <a-menu slot="overlay">
            <a-menu-item key="1">
              <a-icon type="delete"/>删除
            </a-menu-item>
          </a-menu>
          <a-button style="margin-left: 8px">批量操作
            <a-icon type="down"/>
          </a-button>
        </a-dropdown>
      </div>

      <a-table
        ref="table"
        size="default"
        rowKey="key"
        :columns="columns"
        :data="loadData"
      >
        <span slot="serial" slot-scope="text, record, index">{{ index + 1 }}</span>
        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical"/>
          </template>
          <a-dropdown>
            <a class="ant-dropdown-link">更多
              <a-icon type="down"/>
            </a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a href="javascript:;">详情</a>
              </a-menu-item>
              <a-menu-item v-if="$auth('table.disable')">
                <a href="javascript:;">禁用</a>
              </a-menu-item>
              <a-menu-item v-if="$auth('table.delete')">
                <a href="javascript:;">删除</a>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>
      </a-table>
    </div>
  </a-card>
</template>

<script>
export default {
  name: 'PostList',
  components: {
  },
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
      loadData: parameter => {
      },
      selectedRowKeys: [],
      selectedRows: [],
      options: {
      },
      optionAlertShow: false
    }
  },
  created() {
  },
  methods: {
  }
}
</script>

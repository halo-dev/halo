<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="internal">
              <span slot="tab">
                <a-icon type="pushpin"/>内置页面
              </span>
              <a-table
                :columns="internalColumns"
                :dataSource="internalPages"
                :pagination="false"
                :rowKey="page => page.id"
              >
                <span slot="action" slot-scope="text, record">
                  <a href="javascript:;" @click="viewPage(record.id)">查看</a>
                  <a-divider type="vertical"/>
                  <router-link :to="{name:'LinkList'}" v-if="record.id==1">
                    <a href="javascript:void(0);">编辑</a>
                  </router-link>
                  <router-link :to="{name:'GalleryList'}" v-if="record.id==2">
                    <a href="javascript:void(0);">编辑</a>
                  </router-link>
                  <router-link :to="{name:'JournalList'}" v-if="record.id==3">
                    <a href="javascript:void(0);">编辑</a>
                  </router-link>
                </span>
              </a-table>

              <!-- TODO 移动端展示 -->
              <a-collapse :bordered="false">
                <a-collapse-panel :header="item.name" v-for="(item,index) in internalPages" :key="index">
                  <p>12332112323</p>
                </a-collapse-panel>
              </a-collapse>
            </a-tab-pane>
            <a-tab-pane key="custom">
              <span slot="tab">
                <a-icon type="fork"/>内置页面
              </span>
              自定义页面
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
const internalColumns = [
  {
    title: '页面名称',
    dataIndex: 'name'
  },
  {
    title: '访问路径',
    dataIndex: 'url'
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]
const internalPages = [
  {
    id: '1',
    name: '友情链接',
    url: '/links'
  },
  {
    id: '2',
    name: '图库页面',
    url: '/galleries'
  },
  {
    id: '3',
    name: '日志页面',
    url: '/journals'
  }
]
export default {
  data() {
    return {
      internalColumns,
      internalPages
    }
  },
  methods: {
    editPage(id) {
      this.$message.success('编辑' + id)
    },
    viewPage(id) {
      this.$message.success('查看' + id)
    }
  }
}
</script>

<style>
.card-container {
  background: #f5f5f5;
}
.card-container > .ant-tabs-card > .ant-tabs-content {
  margin-top: -16px;
}

.card-container > .ant-tabs-card > .ant-tabs-content > .ant-tabs-tabpane {
  background: #fff;
  padding: 16px;
}

.card-container > .ant-tabs-card > .ant-tabs-bar {
  border-color: #fff;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab {
  border-color: transparent;
  background: transparent;
}

.card-container > .ant-tabs-card > .ant-tabs-bar .ant-tabs-tab-active {
  border-color: #fff;
  background: #fff;
}
.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}
</style>

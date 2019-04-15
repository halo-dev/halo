<template>
  <div class="page-header-index-wide">
    <div class="card-container">
      <a-tabs type="card">
        <a-tab-pane tab="资源文件备份" key="1">
          <a-table :columns="columns" :dataSource="ResourcesData">
            <span slot="action" slot-scope="text, record">
              <a href="javascript:;" @click="downResources('ResourcesData',record.id)">下载</a>
              <a-divider type="vertical"/>
              <a href="javascript:;" @click="sendResources('ResourcesData',record.id)">发送到邮箱</a>
              <a-divider type="vertical"/>
              <a href="javascript:;" @click="deleteResources('ResourcesData',record.id)">删除</a>
            </span>
          </a-table>
          <a-button type="primary" @click="backupData('ResourcesData')">备份</a-button>
        </a-tab-pane>
        <a-tab-pane tab="数据库备份" key="2">
          <a-table :columns="columns" :dataSource="DataBaseData">
            <span slot="action" slot-scope="text, record">
              <a href="javascript:;" @click="downResources('DataBaseData',record.id)">下载</a>
              <a-divider type="vertical"/>
              <a href="javascript:;" @click="sendResources('DataBaseData',record.id)">发送到邮箱</a>
              <a-divider type="vertical"/>
              <a href="javascript:;" @click="deleteResources('DataBaseData',record.id)">删除</a>
            </span>
          </a-table>
          <a-button type="primary" @click="backupData('DataBaseData')">备份</a-button>
        </a-tab-pane>
        <a-tab-pane tab="文章备份" key="3">
          <a-table :columns="columns" :dataSource="FileData">
            <span slot="action" slot-scope="text, record">
              <a href="javascript:;" @click="downResources('FileData',record.id)">下载</a>
              <a-divider type="vertical"/>
              <a href="javascript:;" @click="sendResources('FileData',record.id)">发送到邮箱</a>
              <a-divider type="vertical"/>
              <a href="javascript:;" @click="deleteResources('FileData',record.id)">删除</a>
            </span>
          </a-table>
          <a-button type="primary" @click="backupData('FileData')">备份</a-button>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      num: 0,
      columns: [
        {
          title: '文件名称',
          dataIndex: 'name'
        },
        {
          title: '日期',
          dataIndex: 'date'
        },
        {
          title: '文件大小',
          dataIndex: 'size'
        },
        {
          title: '文件类型',
          dataIndex: 'type'
        },
        {
          title: '操作',
          scopedSlots: { customRender: 'action' }
        }
      ],
      ResourcesData: [],
      FileData: []
    }
  },
  created() {
    ResourcesData = function() {}
  },
  methods: {
    // 下载
    downResources(type, id) {
      if (type == 'ResourcesData') {
        alert('资源文件下载' + id)
      } else if (type == 'DataBaseData') {
        alert('数据库文件下载' + id)
      } else {
        alert('文件下载' + id)
      }
    },
    // 发送到邮箱
    sendResources(type, id) {
      if (type == 'ResourcesData') {
        alert('资源文件发送到邮箱' + id)
      } else if (type == 'DataBaseData') {
        alert('数据库文件发送到邮箱' + id)
      } else {
        alert('文件发送到邮箱' + id)
      }
    },
    // 删除
    deleteResources(type, id) {
      if (type == 'ResourcesData') {
        alert('资源文件删除' + id)
      } else if (type == 'DataBaseData') {
        alert('数据库文件删除' + id)
      } else {
        alert('文件删除' + id)
      }
    },
    // 备份
    backupData(type) {
      if (type == 'ResourcesData') {
        alert('资源文件备份')
      } else if (type == 'DataBaseData') {
        alert('数据库文件备份')
      } else {
        alert('文件备份')
      }
    }
  }
}
</script>
<style>
.card-container {
  background: #f5f5f5;
  overflow: hidden;
  padding: 24px;
}
.card-container > .ant-tabs-card > .ant-tabs-content {
  /* height: 120px; */
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
</style>
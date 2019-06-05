<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs type="card">
            <a-tab-pane key="internal">
              <span slot="tab">
                <a-icon type="pushpin" />内置页面
              </span>

              <!-- TODO 移动端展示 -->
              <!-- <a-collapse
                :bordered="false"
                v-if="isMobile()"
              >
                <a-collapse-panel
                  v-for="(item,index) in internalSheets"
                  :key="index"
                >
                  <a
                    href="javascript:void(0);"
                    slot="header"
                  > {{ item.name }} </a>
                  <div>
                    访问路径：{{ item.url }}
                    操作：{{ item.url }}
                  </div>
                </a-collapse-panel>
              </a-collapse> -->

              <a-table
                :columns="internalColumns"
                :dataSource="internalSheets"
                :pagination="false"
                :rowKey="page => page.id"
              >
                <template
                  slot="status"
                  slot-scope="status"
                >
                  <span v-if="status">可用</span>
                  <span v-else>不可用
                    <a-tooltip
                      slot="action"
                      title="当前主题没有对应模板"
                    >
                      <a-icon type="info-circle-o" />
                    </a-tooltip>
                  </span>
                </template>
                <span
                  slot="action"
                  slot-scope="text, record"
                >
                  <router-link
                    :to="{name:'LinkList'}"
                    v-if="record.id==1"
                  >
                    <a href="javascript:void(0);">管理</a>
                  </router-link>
                  <router-link
                    :to="{name:'PhotoList'}"
                    v-if="record.id==2"
                  >
                    <a href="javascript:void(0);">管理</a>
                  </router-link>
                  <router-link
                    :to="{name:'JournalList'}"
                    v-if="record.id==3"
                  >
                    <a href="javascript:void(0);">管理</a>
                  </router-link>
                  <a-divider type="vertical" />
                  <a
                    :href="options.blog_url+record.url"
                    target="_blank"
                    v-if="record.status"
                  >访问</a>
                  <a
                    :href="options.blog_url+record.url"
                    target="_blank"
                    disabled
                    v-else
                  >访问</a>
                </span>
              </a-table>
            </a-tab-pane>
            <a-tab-pane key="custom">
              <span slot="tab">
                <a-icon type="fork" />自定义页面
              </span>
              <a-table
                :rowKey="sheet => sheet.id"
                :columns="customColumns"
                :dataSource="formattedSheets"
                :pagination="false"
              >
                <span
                  slot="status"
                  slot-scope="statusProperty"
                >
                  <a-badge :status="statusProperty.status" />
                  {{ statusProperty.text }}
                </span>

                <span
                  slot="updateTime"
                  slot-scope="updateTime"
                >{{ updateTime | timeAgo }}</span>

                <span
                  slot="action"
                  slot-scope="text, sheet"
                >
                  <a
                    href="javascript:;"
                    @click="handleEditClick(sheet)"
                    v-if="sheet.status === 'PUBLISHED' || sheet.status === 'DRAFT'"
                  >编辑</a>

                  <a-popconfirm
                    :title="'你确定要发布【' + sheet.title + '】？'"
                    @confirm="handleEditStatusClick(sheet.id,'PUBLISHED')"
                    okText="确定"
                    cancelText="取消"
                    v-else-if="sheet.status === 'RECYCLE'"
                  >
                    <a href="javascript:;">还原</a>
                  </a-popconfirm>

                  <a-divider type="vertical" />

                  <a-popconfirm
                    :title="'你确定要将【' + sheet.title + '】页面移到回收站？'"
                    @confirm="handleEditStatusClick(sheet.id,'RECYCLE')"
                    okText="确定"
                    cancelText="取消"
                    v-if="sheet.status === 'PUBLISHED' || sheet.status === 'DRAFT'"
                  >
                    <a href="javascript:;">回收站</a>
                  </a-popconfirm>

                  <a-popconfirm
                    :title="'你确定要永久删除【' + sheet.title + '】页面？'"
                    @confirm="handleDeleteClick(sheet.id)"
                    okText="确定"
                    cancelText="取消"
                    v-else-if="sheet.status === 'RECYCLE'"
                  >
                    <a href="javascript:;">删除</a>
                  </a-popconfirm>

                </span>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import sheetApi from '@/api/sheet'
import optionApi from '@/api/option'
const internalColumns = [
  {
    title: '页面名称',
    dataIndex: 'title'
  },
  {
    title: '访问路径',
    dataIndex: 'url'
  },
  {
    title: '状态',
    dataIndex: 'status',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]
const customColumns = [
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
    title: '评论量',
    dataIndex: 'commentCount'
  },
  {
    title: '访问量',
    dataIndex: 'visits'
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
]
export default {
  mixins: [mixin, mixinDevice],
  data() {
    return {
      sheetStatus: sheetApi.sheetStatus,
      internalColumns,
      customColumns,
      internalSheets: [],
      sheets: [],
      options: [],
      keys: ['blog_url']
    }
  },
  computed: {
    formattedSheets() {
      return this.sheets.map(sheet => {
        sheet.statusProperty = this.sheetStatus[sheet.status]
        return sheet
      })
    }
  },
  created() {
    this.loadSheets()
    this.loadInternalSheets()
    this.loadOptions()
  },
  methods: {
    loadSheets() {
      sheetApi.list().then(response => {
        this.sheets = response.data.data.content
      })
    },
    loadInternalSheets() {
      sheetApi.listInternal().then(response => {
        this.internalSheets = response.data.data
      })
    },
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
      })
    },
    handleEditClick(sheet) {
      this.$router.push({ name: 'SheetEdit', query: { sheetId: sheet.id } })
    },
    handleEditStatusClick(sheetId, status) {
      sheetApi.updateStatus(sheetId, status).then(response => {
        this.$message.success('操作成功！')
        this.loadSheets()
      })
    },
    handleDeleteClick(sheetId) {
      sheetApi.delete(sheetId).then(response => {
        this.$message.success('删除成功！')
        this.loadSheets()
      })
    }
  }
}
</script>

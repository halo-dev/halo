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

              <!-- Mobile -->

              <a-list
                v-if="isMobile()"
                itemLayout="vertical"
                size="large"
                :pagination="false"
                :dataSource="internalSheets"
              >
                <a-list-item
                  slot="renderItem"
                  slot-scope="item, index"
                  :key="index"
                >
                  <template slot="actions">
                    <span>
                      <router-link
                        :to="{name:'LinkList'}"
                        v-if="item.id==1"
                      >
                        <a-icon type="edit" />
                      </router-link>
                      <router-link
                        :to="{name:'PhotoList'}"
                        v-if="item.id==2"
                      >
                        <a-icon type="edit" />
                      </router-link>
                      <router-link
                        :to="{name:'JournalList'}"
                        v-if="item.id==3"
                      >
                        <a-icon type="edit" />
                      </router-link>
                    </span>
                  </template>
                  <template slot="extra">
                    <span v-if="item.status">可用</span>
                    <span v-else>不可用
                      <a-tooltip
                        slot="action"
                        title="当前主题没有对应模板"
                      >
                        <a-icon type="info-circle-o" />
                      </a-tooltip>
                    </span>
                  </template>
                  <a-list-item-meta>
                    <span
                      slot="title"
                      style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                    >
                      <a
                        :href="options.blog_url+item.url"
                        target="_blank"
                        v-if="item.status"
                      >{{ item.title }}</a>
                      <a
                        :href="options.blog_url+item.url"
                        target="_blank"
                        disabled
                        v-else
                      >{{ item.title }}</a>
                    </span>

                  </a-list-item-meta>
                </a-list-item>
              </a-list>

              <!-- Desktop -->
              <a-table
                v-else
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

              <!-- Mobile -->
              <a-list
                v-if="isMobile()"
                itemLayout="vertical"
                size="large"
                :pagination="false"
                :dataSource="formattedSheets"
                :loading="sheetsLoading"
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
                    <span>
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
                        <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'DRAFT'">
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
                        <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'DRAFT'">
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
                          <a-popconfirm
                            :title="'你确定要添加【' + item.title + '】到菜单？'"
                            @confirm="handleSheetToMenu(item)"
                            okText="确定"
                            cancelText="取消"
                          >
                            <a href="javascript:void(0);">添加到菜单</a>
                          </a-popconfirm>
                        </a-menu-item>
                        <a-menu-item>
                          <a
                            rel="noopener noreferrer"
                            href="javascript:void(0);"
                            @click="handleShowSheetSettings(item)"
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
                        v-if="item.status=='PUBLISHED'"
                        :href="options.blog_url+'/archives/'+item.url"
                        target="_blank"
                        style="text-decoration: none;"
                      >
                        <a-tooltip
                          placement="top"
                          :title="'点击访问【'+item.title+'】'"
                        >{{ item.title }}</a-tooltip>
                      </a>
                      <a
                        v-else-if="item.status == 'INTIMATE'"
                        :href="options.blog_url+'/archives/'+item.url+'/password'"
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

                </a-list-item>
              </a-list>

              <!-- Desktop -->
              <a-table
                v-else
                :rowKey="sheet => sheet.id"
                :columns="customColumns"
                :dataSource="formattedSheets"
                :pagination="false"
                :loading="sheetsLoading"
              >
                <span
                  slot="sheetTitle"
                  slot-scope="text,record"
                  style="max-width: 150px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                >
                  <a
                    v-if="record.status=='PUBLISHED'"
                    :href="options.blog_url+'/s/'+record.url"
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
                  slot="commentCount"
                  slot-scope="commentCount"
                >
                  <a-badge
                    :count="commentCount"
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
                  <a-divider type="vertical" />
                  <a-dropdown :trigger="['click']">
                    <a
                      href="javascript:void(0);"
                      class="ant-dropdown-link"
                    >更多</a>
                    <a-menu slot="overlay">
                      <a-menu-item key="1">
                        <a
                          href="javascript:void(0);"
                          @click="handleShowSheetSettings(sheet)"
                        >设置</a>
                      </a-menu-item>
                      <a-menu-item key="2">
                        <a-popconfirm
                          :title="'你确定要添加【' + sheet.title + '】到菜单？'"
                          @confirm="handleSheetToMenu(sheet)"
                          okText="确定"
                          cancelText="取消"
                        >
                          <a href="javascript:void(0);">添加到菜单</a>
                        </a-popconfirm>
                      </a-menu-item>
                    </a-menu>
                  </a-dropdown>
                </span>
              </a-table>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>

    <SheetSetting
      :sheet="selectedSheet"
      :visible="sheetSettingVisible"
      :needTitle="true"
      @close="onSheetSettingsClose"
      @onRefreshSheet="onRefreshSheetFromSetting"
    />

  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { mapGetters } from 'vuex'
import SheetSetting from './components/SheetSetting'
import sheetApi from '@/api/sheet'
import menuApi from '@/api/menu'

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
    dataIndex: 'title',
    scopedSlots: { customRender: 'sheetTitle' }
  },
  {
    title: '状态',
    className: 'status',
    dataIndex: 'statusProperty',
    scopedSlots: { customRender: 'status' }
  },
  {
    title: '评论量',
    dataIndex: 'commentCount',
    scopedSlots: { customRender: 'commentCount' }
  },
  {
    title: '访问量',
    dataIndex: 'visits',
    scopedSlots: { customRender: 'visits' }
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
  mixins: [mixin, mixinDevice],
  components: {
    SheetSetting
  },
  data() {
    return {
      sheetsLoading: false,
      sheetStatus: sheetApi.sheetStatus,
      internalColumns,
      customColumns,
      selectedSheet: {},
      sheetSettingVisible: false,
      internalSheets: [],
      sheets: [],
      menu: {}
    }
  },
  computed: {
    formattedSheets() {
      return this.sheets.map(sheet => {
        sheet.statusProperty = this.sheetStatus[sheet.status]
        return sheet
      })
    },
    ...mapGetters(['options'])
  },
  created() {
    this.loadSheets()
    this.loadInternalSheets()
  },
  destroyed: function() {
    if (this.sheetSettingVisible) {
      this.sheetSettingVisible = false
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.sheetSettingVisible) {
      this.sheetSettingVisible = false
    }
    next()
  },
  methods: {
    loadSheets() {
      this.sheetsLoading = true
      sheetApi.list().then(response => {
        this.sheets = response.data.data.content
        this.sheetsLoading = false
      })
    },
    loadInternalSheets() {
      sheetApi.listInternal().then(response => {
        this.internalSheets = response.data.data
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
    },
    handleSheetToMenu(sheet) {
      this.menu['name'] = sheet.title
      this.menu['url'] = `/s/${sheet.url}`
      menuApi.create(this.menu).then(response => {
        this.$message.success('添加到菜单成功！')
        this.menu = {}
      })
    },
    handleShowSheetSettings(sheet) {
      sheetApi.get(sheet.id).then(response => {
        this.selectedSheet = response.data.data
        this.sheetSettingVisible = true
      })
    },
    handlePreview(sheetId) {
      sheetApi.preview(sheetId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    onSheetSettingsClose() {
      this.sheetSettingVisible = false
      this.selectedSheet = {}
      this.loadSheets()
    },
    onRefreshSheetFromSetting(sheet) {
      this.selectedSheet = sheet
    }
  }
}
</script>

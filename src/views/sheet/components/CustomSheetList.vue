<template>
  <div>
    <!-- Mobile -->
    <a-list
      v-if="isMobile()"
      itemLayout="vertical"
      size="large"
      :pagination="false"
      :dataSource="formattedSheets"
      :loading="loading"
    >
      <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
        <template slot="actions">
          <span>
            <a-icon type="eye" />
            {{ item.visits }}
          </span>
          <span @click="handleShowSheetComments(item)">
            <a-icon type="message" />
            {{ item.commentCount }}
          </span>
          <a-dropdown placement="topLeft" :trigger="['click']">
            <span>
              <a-icon type="bars" />
            </span>
            <a-menu slot="overlay">
              <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'DRAFT'">
                <a href="javascript:;" @click="handleEditClick(item)">编辑</a>
              </a-menu-item>
              <a-menu-item v-else-if="item.status === 'RECYCLE'">
                <a-popconfirm
                  :title="'你确定要发布【' + item.title + '】页面？'"
                  @confirm="handleEditStatusClick(item.id, 'PUBLISHED')"
                  okText="确定"
                  cancelText="取消"
                >
                  <a href="javascript:;">还原</a>
                </a-popconfirm>
              </a-menu-item>
              <a-menu-item v-if="item.status === 'PUBLISHED' || item.status === 'DRAFT'">
                <a-popconfirm
                  :title="'你确定要将【' + item.title + '】页面移到回收站？'"
                  @confirm="handleEditStatusClick(item.id, 'RECYCLE')"
                  okText="确定"
                  cancelText="取消"
                >
                  <a href="javascript:;">回收站</a>
                </a-popconfirm>
              </a-menu-item>
              <a-menu-item v-else-if="item.status === 'RECYCLE'">
                <a-popconfirm
                  :title="'你确定要永久删除【' + item.title + '】页面？'"
                  @confirm="handleDeleteClick(item.id)"
                  okText="确定"
                  cancelText="取消"
                >
                  <a href="javascript:;">删除</a>
                </a-popconfirm>
              </a-menu-item>
              <a-menu-item>
                <a rel="noopener noreferrer" href="javascript:void(0);" @click="handleShowSheetSettings(item)">设置</a>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </template>
        <template slot="extra">
          <span>
            <a-badge :status="item.statusProperty.status" :text="item.statusProperty.text" />
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
            <a v-if="item.status == 'PUBLISHED'" :href="item.fullPath" target="_blank" class="no-underline">
              <a-tooltip placement="top" :title="'点击访问【' + item.title + '】'">{{ item.title }}</a-tooltip>
            </a>
            <a
              v-else-if="item.status == 'DRAFT'"
              href="javascript:void(0)"
              class="no-underline"
              @click="handlePreview(item.id)"
            >
              <a-tooltip placement="topLeft" :title="'点击预览【' + item.title + '】'">{{ item.title }}</a-tooltip>
            </a>
            <a v-else href="javascript:void(0);" class="no-underline" disabled>
              {{ item.title }}
            </a>
          </span>
        </a-list-item-meta>
        <span> {{ item.summary }}... </span>
      </a-list-item>
    </a-list>

    <!-- Desktop -->
    <a-table
      v-else
      :rowKey="sheet => sheet.id"
      :columns="customColumns"
      :dataSource="formattedSheets"
      :pagination="false"
      :loading="loading"
      :scrollToFirstRowOnChange="true"
    >
      <span slot="sheetTitle" slot-scope="text, record">
        <a v-if="record.status == 'PUBLISHED'" :href="record.fullPath" target="_blank" class="no-underline">
          <a-tooltip placement="top" :title="'点击访问【' + text + '】'">{{ text }}</a-tooltip>
        </a>
        <a
          v-else-if="record.status == 'DRAFT'"
          href="javascript:void(0)"
          class="no-underline"
          @click="handlePreview(record.id)"
        >
          <a-tooltip placement="topLeft" :title="'点击预览【' + text + '】'">{{ text }}</a-tooltip>
        </a>
        <a v-else href="javascript:void(0);" class="no-underline" disabled>
          {{ text }}
        </a>
      </span>

      <span slot="status" slot-scope="statusProperty">
        <a-badge :status="statusProperty.status" :text="statusProperty.text" />
      </span>

      <span
        slot="commentCount"
        slot-scope="text, record"
        @click="handleShowSheetComments(record)"
        class="cursor-pointer"
      >
        <a-badge
          :count="record.commentCount"
          :numberStyle="{ backgroundColor: '#f38181' }"
          :showZero="true"
          :overflowCount="999"
        />
      </span>

      <span slot="visits" slot-scope="visits">
        <a-badge :count="visits" :numberStyle="{ backgroundColor: '#00e0ff' }" :showZero="true" :overflowCount="9999" />
      </span>

      <span slot="createTime" slot-scope="createTime">
        <a-tooltip placement="top">
          <template slot="title">
            {{ createTime | moment }}
          </template>
          {{ createTime | timeAgo }}
        </a-tooltip>
      </span>

      <span slot="action" slot-scope="text, sheet">
        <a
          href="javascript:;"
          @click="handleEditClick(sheet)"
          v-if="sheet.status === 'PUBLISHED' || sheet.status === 'DRAFT'"
          >编辑</a
        >

        <a-popconfirm
          :title="'你确定要发布【' + sheet.title + '】？'"
          @confirm="handleEditStatusClick(sheet.id, 'PUBLISHED')"
          okText="确定"
          cancelText="取消"
          v-else-if="sheet.status === 'RECYCLE'"
        >
          <a href="javascript:;">还原</a>
        </a-popconfirm>

        <a-divider type="vertical" />

        <a-popconfirm
          :title="'你确定要将【' + sheet.title + '】页面移到回收站？'"
          @confirm="handleEditStatusClick(sheet.id, 'RECYCLE')"
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
        <a href="javascript:void(0);" @click="handleShowSheetSettings(sheet)">设置</a>
      </span>
    </a-table>
    <div class="page-wrapper">
      <a-pagination
        class="pagination"
        :current="pagination.page"
        :total="pagination.total"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
        showSizeChanger
        @showSizeChange="handlePaginationChange"
        @change="handlePaginationChange"
        showLessItems
      />
    </div>
    <SheetSettingDrawer
      :sheet="selectedSheet"
      :metas="selectedMetas"
      :visible="sheetSettingVisible"
      :needTitle="true"
      :saveDraftButton="false"
      @close="onSheetSettingsClose"
      @onRefreshSheet="onRefreshSheetFromSetting"
      @onRefreshSheetMetas="onRefreshSheetMetasFromSetting"
    />

    <TargetCommentDrawer
      :visible="sheetCommentVisible"
      :title="selectedSheet.title"
      :description="selectedSheet.summary"
      :target="`sheets`"
      :id="selectedSheet.id"
      @close="onSheetCommentsClose"
    />
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import SheetSettingDrawer from './SheetSettingDrawer'
import TargetCommentDrawer from '../../comment/components/TargetCommentDrawer'
import sheetApi from '@/api/sheet'

const customColumns = [
  {
    title: '标题',
    dataIndex: 'title',
    ellipsis: true,
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
  name: 'CustomSheetList',
  mixins: [mixin, mixinDevice],
  components: {
    SheetSettingDrawer,
    TargetCommentDrawer
  },
  data() {
    return {
      pagination: {
        page: 1,
        size: 10,
        sort: null,
        total: 1
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        categoryId: null,
        status: null
      },
      loading: false,
      sheetStatus: sheetApi.sheetStatus,
      customColumns,
      selectedSheet: {},
      selectedMetas: [],
      sheetSettingVisible: false,
      sheetCommentVisible: false,
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
    }
  },
  created() {
    this.handleListSheets()
  },
  destroyed() {
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
    handleListSheets(enableLoading = true) {
      if (enableLoading) {
        this.loading = true
      }
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      sheetApi
        .list(this.queryParam)
        .then(response => {
          this.sheets = response.data.data.content
          this.pagination.total = response.data.data.total
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleEditClick(sheet) {
      this.$router.push({ name: 'SheetEdit', query: { sheetId: sheet.id } })
    },
    handleEditStatusClick(sheetId, status) {
      sheetApi
        .updateStatus(sheetId, status)
        .then(() => {
          this.$message.success('操作成功！')
        })
        .finally(() => {
          this.handleListSheets()
        })
    },
    handleDeleteClick(sheetId) {
      sheetApi
        .delete(sheetId)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListSheets()
        })
    },
    handleShowSheetSettings(sheet) {
      sheetApi.get(sheet.id).then(response => {
        this.selectedSheet = response.data.data
        this.selectedMetas = this.selectedSheet.metas
        this.sheetSettingVisible = true
      })
    },
    handleShowSheetComments(sheet) {
      sheetApi.get(sheet.id).then(response => {
        this.selectedSheet = response.data.data
        this.sheetCommentVisible = true
      })
    },
    handlePreview(sheetId) {
      sheetApi.preview(sheetId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.handleListSheets()
    },
    onSheetSettingsClose() {
      this.sheetSettingVisible = false
      this.selectedSheet = {}
      setTimeout(() => {
        this.handleListSheets(false)
      }, 500)
    },
    onSheetCommentsClose() {
      this.sheetCommentVisible = false
      this.selectedSheet = {}
      setTimeout(() => {
        this.handleListSheets(false)
      }, 500)
    },
    onRefreshSheetFromSetting(sheet) {
      this.selectedSheet = sheet
    },
    onRefreshSheetMetasFromSetting(metas) {
      this.selectedMetas = metas
    }
  }
}
</script>

<template>
  <div>
    <!-- Mobile -->
    <a-list
      v-if="isMobile()"
      itemLayout="vertical"
      size="large"
      :pagination="false"
      :dataSource="internalSheets"
      :loading="loading"
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
              :href="item.url"
              target="_blank"
              v-if="item.status"
            >{{ item.title }}</a>
            <a
              :href="item.url"
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
      :loading="loading"
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
          :href="record.url"
          target="_blank"
          v-if="record.status"
        >访问</a>
        <a
          :href="record.url"
          target="_blank"
          disabled
          v-else
        >访问</a>
      </span>
    </a-table>
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import sheetApi from '@/api/sheet'
const internalColumns = [
  {
    title: '页面名称',
    dataIndex: 'title'
  },
  {
    title: '访问地址',
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
export default {
  name: 'InternalSheetList',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      internalColumns,
      internalSheets: [],
      loading: false
    }
  },
  created() {
    this.loadInternalSheets()
  },
  methods: {
    loadInternalSheets() {
      this.loading = true
      sheetApi.listInternal().then(response => {
        this.internalSheets = response.data.data
        this.loading = false
      })
    }
  }
}
</script>

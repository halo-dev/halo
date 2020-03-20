<template>
  <div>
    <!-- Mobile -->
    <a-list
      v-if="isMobile()"
      itemLayout="vertical"
      size="large"
      :pagination="false"
      :dataSource="independentSheets"
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
              :to="{name:item.routeName}"
            >
              <a-icon type="edit" />
            </router-link>
          </span>
        </template>
        <template slot="extra">
          <span v-if="item.available">可用</span>
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
              :href="item.fullPath"
              target="_blank"
              v-if="item.available"
            >{{ item.title }}</a>
            <a
              :href="item.fullPath"
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
      :columns="independentColumns"
      :dataSource="independentSheets"
      :pagination="false"
      :rowKey="sheet => sheet.id"
      :loading="loading"
    >
      <template
        slot="available"
        slot-scope="available"
      >
        <span v-if="available">可用</span>
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
          :to="{name:record.routeName}"
        >
          <a href="javascript:void(0);">管理</a>
        </router-link>
        <a-divider type="vertical" />
        <a
          :href="record.fullPath"
          target="_blank"
          v-if="record.available"
        >访问</a>
        <a
          :href="record.fullPath"
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
const independentColumns = [
  {
    title: '页面名称',
    dataIndex: 'title'
  },
  {
    title: '访问地址',
    dataIndex: 'fullPath'
  },
  {
    title: '状态',
    dataIndex: 'available',
    scopedSlots: { customRender: 'available' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'IndependentSheetList',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      independentColumns,
      independentSheets: [],
      loading: false
    }
  },
  created() {
    this.loadIndependentSheets()
  },
  methods: {
    loadIndependentSheets() {
      this.loading = true
      sheetApi.listIndependent().then(response => {
        this.independentSheets = response.data.data
        this.loading = false
      })
    }
  }
}
</script>

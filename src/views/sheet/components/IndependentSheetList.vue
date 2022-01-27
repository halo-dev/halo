<template>
  <div>
    <!-- Mobile -->
    <a-list
      v-if="isMobile()"
      :dataSource="independentSheets"
      :loading="loading"
      :pagination="false"
      itemLayout="vertical"
      size="large"
    >
      <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
        <template slot="actions">
          <span>
            <router-link :to="{ name: item.routeName }">
              <a-icon type="edit" />
            </router-link>
          </span>
        </template>
        <template slot="extra">
          <span v-if="item.available">可用</span>
          <span v-else
            >不可用
            <a-tooltip slot="action" title="当前主题没有对应模板">
              <a-icon type="info-circle-o" />
            </a-tooltip>
          </span>
        </template>
        <a-list-item-meta>
          <span
            slot="title"
            style="max-width: 300px; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis"
          >
            <a v-if="item.available" :href="item.fullPath" target="_blank">{{ item.title }}</a>
            <a v-else :href="item.fullPath" disabled target="_blank">{{ item.title }}</a>
          </span>
        </a-list-item-meta>
      </a-list-item>
    </a-list>

    <!-- Desktop -->
    <a-table
      v-else
      :columns="independentColumns"
      :dataSource="independentSheets"
      :loading="loading"
      :pagination="false"
      :rowKey="sheet => sheet.id"
    >
      <template slot="available" slot-scope="available">
        <span v-if="available">可用</span>
        <span v-else>
          不可用
          <a-tooltip slot="action" title="当前主题没有对应模板">
            <a-icon type="info-circle-o" />
          </a-tooltip>
        </span>
      </template>
      <span slot="action" slot-scope="text, record">
        <router-link :to="{ name: record.routeName }">
          <a-button class="!p-0" type="link">管理</a-button>
        </router-link>
        <a-divider type="vertical" />
        <a v-if="record.available" :href="record.fullPath" target="_blank">访问</a>
        <a v-else :href="record.fullPath" disabled target="_blank">访问</a>
      </span>
    </a-table>
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

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
    this.handleListIndependentSheets()
  },
  methods: {
    async handleListIndependentSheets() {
      try {
        const { data } = await apiClient.sheet.listIndependents()

        this.independentSheets = data
      } catch (e) {
        this.$log.error(e)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

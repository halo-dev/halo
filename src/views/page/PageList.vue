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

              <!-- TODO 移动端展示 -->
              <a-collapse :bordered="false" v-if="isMobile()">
                <a-collapse-panel v-for="(item,index) in internalPages" :key="index">
                  <a href="javascript:void(0);" slot="header"> {{ item.name }} </a>
                  <div>
                    访问路径：{{ item.url }}
                    操作：{{ item.url }}
                  </div>
                </a-collapse-panel>
              </a-collapse>

              <a-table
                :columns="internalColumns"
                :dataSource="internalPages"
                :pagination="false"
                :rowKey="page => page.id"
                v-else
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
import { mixin, mixinDevice } from '@/utils/mixin.js'
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
  mixins: [mixin, mixinDevice],
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
<template>
  <div class="option-tab-wrapper">
    <a-card
      :bordered="false"
      :bodyStyle="{ padding: 0 }"
    >
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="关键词">
                <a-input
                  v-model="queryParam.keyword"
                  @keyup.enter="handleQuery()"
                />
              </a-form-item>
            </a-col>
            <a-col
              :md="6"
              :sm="24"
            >
              <a-form-item label="类型">
                <a-select
                  v-model="queryParam.type"
                  placeholder="请选择类型"
                  @change="handleQuery()"
                >
                  <a-select-option
                    v-for="item in Object.keys(optionType)"
                    :key="item"
                    :value="item"
                  >{{ optionType[item].text }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col
              :md="12"
              :sm="24"
            >
              <span class="table-page-search-submitButtons">
                <a-button
                  type="primary"
                  @click="handleQuery()"
                >查询</a-button>
                <a-button
                  style="margin-left: 8px;"
                  @click="handleResetParam()"
                >重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="table-operator">
        <a-button
          type="primary"
          icon="plus"
        >新增</a-button>
      </div>
      <div style="margin-top:15px">
        <a-table
          :rowKey="option => option.id"
          :columns="columns"
          :dataSource="formattedDatas"
          :loading="loading"
          :pagination="false"
        >
          <ellipsis
            :length="50"
            tooltip
            slot="key"
            slot-scope="key"
          >
            {{ key }}
          </ellipsis>
          <ellipsis
            :length="50"
            tooltip
            slot="value"
            slot-scope="value"
          >
            {{ value }}
          </ellipsis>
          <span
            slot="type"
            slot-scope="typeProperty"
          >
            {{ typeProperty.text }}
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
            slot="updateTime"
            slot-scope="updateTime"
          >
            <a-tooltip placement="top">
              <template slot="title">
                {{ updateTime | moment }}
              </template>
              {{ updateTime | timeAgo }}
            </a-tooltip>
          </span>
          <span
            slot="action"
          >
            <a href="javascript:void(0);">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm
              :title="'你确定要永久删除该变量？'"
              okText="确定"
              cancelText="取消"
            >
              <a href="javascript:;">删除</a>
            </a-popconfirm>
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
          />
        </div>
      </div>
    </a-card>
  </div>
</template>
<script>
import optionApi from '@/api/option'
const columns = [
  {
    title: 'Key',
    dataIndex: 'key',
    scopedSlots: { customRender: 'key' }
  },
  {
    title: 'Value',
    dataIndex: 'value',
    scopedSlots: { customRender: 'value' }
  },
  {
    title: '类型',
    dataIndex: 'typeProperty',
    width: '100px',
    scopedSlots: { customRender: 'type' }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: '200px',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: '200px',
    scopedSlots: { customRender: 'updateTime' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '180px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'OptionsList',
  data() {
    return {
      optionType: optionApi.type,
      columns: columns,
      pagination: {
        page: 1,
        size: 10,
        sort: null
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        status: null
      },
      loading: false,
      options: []
    }
  },
  computed: {
    formattedDatas() {
      return this.options.map(option => {
        option.typeProperty = this.optionType[option.type]
        return option
      })
    }
  },
  created() {
    this.loadOptions()
  },
  methods: {
    loadOptions() {
      this.loading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      optionApi.query(this.queryParam).then(response => {
        this.options = response.data.data.content
        this.pagination.total = response.data.data.total
        this.loading = false
      })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.pagination.size)
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadOptions()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.type = null
      this.handlePaginationChange(1, this.pagination.size)
    }
  }
}
</script>

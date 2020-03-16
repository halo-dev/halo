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
              <a-form-item label="关键词：">
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
              <a-form-item label="类型：">
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
          @click="formVisible=true"
        >新增</a-button>
      </div>
      <div style="margin-top:15px">
        <a-table
          :rowKey="option => option.id"
          :columns="columns"
          :dataSource="formattedDatas"
          :loading="loading"
          :pagination="false"
          :scrollToFirstRowOnChange="true"
        >
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
            slot-scope="text, record"
          >
            <a
              href="javascript:void(0);"
              @click="handleEditOption(record)"
            >编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm
              :title="'你确定要永久删除该变量？'"
              okText="确定"
              cancelText="取消"
              @confirm="handleDeleteOption(record.id)"
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
    <a-modal
      v-model="formVisible"
      :title="formTitle"
      :afterClose="onFormClose"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="createOrUpdateOption()"
        >保存</a-button>
      </template>
      <a-alert
        v-if="optionToStage.type === optionType.INTERNAL.value"
        message="注意：在不知道系统变量的具体用途时，请不要随意修改！"
        banner
        closable
      />
      <a-form layout="vertical">
        <a-form-item label="Key：">
          <a-input v-model="optionToStage.key" />
        </a-form-item>
        <a-form-item label="Value：">
          <a-input
            type="textarea"
            :autoSize="{ minRows: 5 }"
            v-model="optionToStage.value"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script>
import optionApi from '@/api/option'
import { mapActions } from 'vuex'
const columns = [
  {
    title: 'Key',
    dataIndex: 'key',
    ellipsis: true,
    scopedSlots: { customRender: 'key' }
  },
  {
    title: 'Value',
    dataIndex: 'value',
    ellipsis: true,
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
    width: '120px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'OptionsList',
  data() {
    return {
      optionType: optionApi.type,
      columns: columns,
      formVisible: false,
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
        status: null
      },
      optionToStage: {},
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
    },
    formTitle() {
      return this.optionToStage.id ? '编辑' : '新增'
    }
  },
  created() {
    this.loadOptionsList()
  },
  methods: {
    ...mapActions(['loadOptions']),
    loadOptionsList() {
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
    handleDeleteOption(id) {
      optionApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadOptionsList()
        this.loadOptions()
      })
    },
    handleEditOption(option) {
      this.optionToStage = option
      this.formVisible = true
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadOptionsList()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.type = null
      this.handlePaginationChange(1, this.pagination.size)
    },
    onFormClose() {
      this.formVisible = false
      this.optionToStage = {}
    },
    createOrUpdateOption() {
      if (!this.optionToStage.key) {
        this.$notification['error']({
          message: '提示',
          description: 'Key 不能为空！'
        })
        return
      }
      if (!this.optionToStage.value) {
        this.$notification['error']({
          message: '提示',
          description: 'Value 不能为空！'
        })
        return
      }
      if (this.optionToStage.id) {
        optionApi.update(this.optionToStage.id, this.optionToStage).then(response => {
          this.$message.success('更新成功！')
          this.loadOptionsList()
          this.loadOptions()
          this.optionToStage = {}
          this.formVisible = false
        })
      } else {
        this.optionToStage.type = this.optionType.CUSTOM.value
        optionApi.create(this.optionToStage).then(response => {
          this.$message.success('保存成功！')
          this.loadOptionsList()
          this.loadOptions()
          this.optionToStage = {}
          this.formVisible = false
        })
      }
    }
  }
}
</script>

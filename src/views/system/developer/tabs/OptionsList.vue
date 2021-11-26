<template>
  <div class="option-tab-wrapper">
    <a-card :bodyStyle="{ padding: 0 }" :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="关键词：">
                <a-input v-model="queryParam.keyword" @keyup.enter="handleQuery()" />
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item label="类型：">
                <a-select v-model="queryParam.type" allowClear placeholder="请选择类型" @change="handleQuery()">
                  <a-select-option v-for="item in Object.keys(optionType)" :key="item" :value="item"
                    >{{ optionType[item].text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="12" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-space>
                  <a-button type="primary" @click="handleQuery()">查询</a-button>
                  <a-button @click="handleResetParam()">重置</a-button>
                </a-space>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="table-operator">
        <a-button icon="plus" type="primary" @click="handleOpenFormModal">新增</a-button>
      </div>
      <div class="mt-4">
        <a-table
          :columns="columns"
          :dataSource="formattedData"
          :loading="loading"
          :pagination="false"
          :rowKey="option => option.id"
          :scrollToFirstRowOnChange="true"
        >
          <span slot="type" slot-scope="typeProperty">
            {{ typeProperty.text }}
          </span>
          <span slot="createTime" slot-scope="createTime">
            <a-tooltip placement="top">
              <template slot="title">
                {{ createTime | moment }}
              </template>
              {{ createTime | timeAgo }}
            </a-tooltip>
          </span>
          <span slot="updateTime" slot-scope="updateTime">
            <a-tooltip placement="top">
              <template slot="title">
                {{ updateTime | moment }}
              </template>
              {{ updateTime | timeAgo }}
            </a-tooltip>
          </span>
          <span slot="action" slot-scope="text, record">
            <a href="javascript:void(0);" @click="handleOpenEditFormModal(record)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm
              :title="'你确定要永久删除该变量？'"
              cancelText="取消"
              okText="确定"
              @confirm="handleDeleteOption(record.id)"
            >
              <a href="javascript:void(0);">删除</a>
            </a-popconfirm>
          </span>
        </a-table>
        <div class="page-wrapper">
          <a-pagination
            :current="pagination.page"
            :defaultPageSize="pagination.size"
            :pageSizeOptions="['10', '20', '50', '100']"
            :total="pagination.total"
            class="pagination"
            showLessItems
            showSizeChanger
            @change="handlePaginationChange"
            @showSizeChange="handlePaginationChange"
          />
        </div>
      </div>
    </a-card>
    <a-modal v-model="form.visible" :afterClose="onFormClose" :title="formTitle">
      <template slot="footer">
        <ReactiveButton
          :errored="form.saveErrored"
          :loading="form.saving"
          erroredText="保存失败"
          loadedText="保存成功"
          text="保存"
          @callback="handleSaveOrUpdateCallback"
          @click="handleSaveOrUpdate"
        ></ReactiveButton>
      </template>
      <a-alert
        v-if="form.model.type === optionType.INTERNAL.value"
        banner
        closable
        message="注意：在不知道系统变量的具体用途时，请不要随意修改！"
      />
      <a-form-model ref="optionForm" :model="form.model" :rules="form.rules" layout="vertical">
        <a-form-model-item label="Key：" prop="key">
          <a-input ref="keyInput" v-model="form.model.key" />
        </a-form-model-item>
        <a-form-model-item label="Value：" prop="value">
          <a-input v-model="form.model.value" :autoSize="{ minRows: 5 }" type="textarea" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </div>
</template>
<script>
import apiClient from '@/utils/api-client'
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
      optionType: {
        INTERNAL: {
          value: 'INTERNAL',
          text: '系统'
        },
        CUSTOM: {
          value: 'CUSTOM',
          text: '自定义'
        }
      },
      columns: columns,
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
        type: null
      },
      loading: false,
      options: [],

      form: {
        visible: false,
        model: {},
        rules: {
          key: [{ required: true, message: '* Key 不能为空', trigger: ['change'] }],
          value: [{ required: true, message: '* Value 不能为空', trigger: ['change'] }]
        },
        saving: false,
        saveErrored: false
      }
    }
  },
  computed: {
    formattedData() {
      return this.options.map(option => {
        option.typeProperty = this.optionType[option.type]
        return option
      })
    },
    formTitle() {
      return this.form.model.id ? '编辑' : '新增'
    }
  },
  beforeMount() {
    this.handleListOptions()
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    handleListOptions() {
      this.loading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      apiClient.option
        .listAsView(this.queryParam)
        .then(response => {
          this.options = response.data.content
          this.pagination.total = response.data.total
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleDeleteOption(id) {
      apiClient.option
        .delete(id)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListOptions()
          this.refreshOptionsCache()
        })
    },
    handleOpenFormModal() {
      this.form.visible = true
      this.$nextTick(() => {
        this.$refs.keyInput.focus()
      })
    },
    handleOpenEditFormModal(option) {
      this.form.model = option
      this.form.visible = true
      this.$nextTick(() => {
        this.$refs.keyInput.focus()
      })
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.handleListOptions()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.type = null
      this.handlePaginationChange(1, this.pagination.size)
    },
    onFormClose() {
      this.form.visible = false
      this.form.model = {}
    },
    handleSaveOrUpdate() {
      const _this = this
      _this.$refs.optionForm.validate(valid => {
        if (valid) {
          _this.form.saving = true
          if (_this.form.model.id) {
            apiClient.option
              .update(_this.form.model.id, _this.form.model)
              .catch(() => {
                _this.form.saveErrored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          } else {
            _this.form.model.type = _this.optionType.CUSTOM.value
            apiClient.option
              .create(_this.form.model)
              .catch(() => {
                _this.form.saveErrored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          }
        }
      })
    },
    handleSaveOrUpdateCallback() {
      if (this.form.saveErrored) {
        this.form.saveErrored = false
      } else {
        this.form.model = {}
        this.form.visible = false
        this.handleListOptions()
        this.refreshOptionsCache()
      }
    }
  }
}
</script>

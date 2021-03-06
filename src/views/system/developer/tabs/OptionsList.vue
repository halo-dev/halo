<template>
  <div class="option-tab-wrapper">
    <a-card :bordered="false" :bodyStyle="{ padding: 0 }">
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
                <a-select v-model="queryParam.type" placeholder="请选择类型" @change="handleQuery()" allowClear>
                  <a-select-option v-for="item in Object.keys(optionType)" :key="item" :value="item">{{
                    optionType[item].text
                  }}</a-select-option>
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
        <a-button type="primary" icon="plus" @click="handleOpenFormModal">新增</a-button>
      </div>
      <div class="mt-4">
        <a-table
          :rowKey="option => option.id"
          :columns="columns"
          :dataSource="formattedDatas"
          :loading="loading"
          :pagination="false"
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
            showLessItems
          />
        </div>
      </div>
    </a-card>
    <a-modal v-model="form.visible" :title="formTitle" :afterClose="onFormClose">
      <template slot="footer">
        <ReactiveButton
          @click="handleSaveOrUpdate"
          @callback="handleSaveOrUpdateCallback"
          :loading="form.saving"
          :errored="form.saveErrored"
          text="保存"
          loadedText="保存成功"
          erroredText="保存失败"
        ></ReactiveButton>
      </template>
      <a-alert
        v-if="form.model.type === optionType.INTERNAL.value"
        message="注意：在不知道系统变量的具体用途时，请不要随意修改！"
        banner
        closable
      />
      <a-form-model ref="optionForm" :model="form.model" :rules="form.rules" layout="vertical">
        <a-form-model-item prop="key" label="Key：">
          <a-input ref="keyInput" v-model="form.model.key" />
        </a-form-model-item>
        <a-form-model-item prop="value" label="Value：">
          <a-input type="textarea" :autoSize="{ minRows: 5 }" v-model="form.model.value" />
        </a-form-model-item>
      </a-form-model>
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
    formattedDatas() {
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
    this.hanldeListOptions()
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    hanldeListOptions() {
      this.loading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      optionApi
        .query(this.queryParam)
        .then(response => {
          this.options = response.data.data.content
          this.pagination.total = response.data.data.total
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.pagination.size)
    },
    handleDeleteOption(id) {
      optionApi
        .delete(id)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.hanldeListOptions()
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
      this.hanldeListOptions()
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
            optionApi
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
            optionApi
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
        this.hanldeListOptions()
        this.refreshOptionsCache()
      }
    }
  }
}
</script>

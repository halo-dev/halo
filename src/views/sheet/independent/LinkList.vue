<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :xl="10" :lg="10" :md="10" :sm="24" :xs="24" class="pb-3">
        <a-card :title="title" :bodyStyle="{ padding: '16px' }">
          <a-form-model ref="linkForm" :model="form.model" :rules="form.rules" layout="horizontal">
            <a-form-model-item label="网站名称：" prop="name">
              <a-input v-model="form.model.name" />
            </a-form-model-item>
            <a-form-model-item label="网站地址：" help="* 需要加上 http://" prop="url">
              <a-input v-model="form.model.url">
                <!-- <a
                  href="javascript:void(0);"
                  slot="addonAfter"
                  @click="handleParseUrl"
                >
                  <a-icon type="sync" />
                </a> -->
              </a-input>
            </a-form-model-item>
            <a-form-model-item label="Logo：" prop="logo">
              <a-input v-model="form.model.logo" />
            </a-form-model-item>
            <a-form-model-item label="分组：" prop="team">
              <a-auto-complete :dataSource="computedTeams" v-model="form.model.team" allowClear />
            </a-form-model-item>
            <a-form-model-item label="排序编号：" prop="priority">
              <a-input-number :min="0" v-model="form.model.priority" style="width:100%" />
            </a-form-model-item>
            <a-form-model-item label="描述：" prop="description">
              <a-input type="textarea" :autoSize="{ minRows: 5 }" v-model="form.model.description" />
            </a-form-model-item>
            <a-form-model-item>
              <ReactiveButton
                v-if="!isUpdateMode"
                type="primary"
                @click="handleCreateOrUpdateLink"
                @callback="handleSavedCallback"
                :loading="form.saving"
                :errored="form.errored"
                text="保存"
                loadedText="保存成功"
                erroredText="保存失败"
              ></ReactiveButton>
              <a-button-group v-else>
                <ReactiveButton
                  type="primary"
                  @click="handleCreateOrUpdateLink"
                  @callback="handleSavedCallback"
                  :loading="form.saving"
                  :errored="form.errored"
                  text="更新"
                  loadedText="更新成功"
                  erroredText="更新失败"
                ></ReactiveButton>
                <a-button type="dashed" @click="form.model = {}" v-if="isUpdateMode">返回添加</a-button>
              </a-button-group>
            </a-form-model-item>
          </a-form-model>
        </a-card>
      </a-col>
      <a-col :xl="14" :lg="14" :md="14" :sm="24" :xs="24" class="pb-3">
        <a-card title="所有友情链接" :bodyStyle="{ padding: '16px' }">
          <!-- Mobile -->
          <a-list
            v-if="isMobile()"
            itemLayout="vertical"
            size="large"
            :dataSource="table.data"
            :loading="table.loading"
          >
            <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
              <template slot="actions">
                <a-dropdown placement="topLeft" :trigger="['click']">
                  <span>
                    <a-icon type="bars" />
                  </span>
                  <a-menu slot="overlay">
                    <a-menu-item>
                      <a href="javascript:void(0);" @click="form.model = item">编辑</a>
                    </a-menu-item>
                    <a-menu-item>
                      <a-popconfirm
                        :title="'你确定要删除【' + item.name + '】链接？'"
                        @confirm="handleDeleteLink(item.id)"
                        okText="确定"
                        cancelText="取消"
                      >
                        <a href="javascript:;">删除</a>
                      </a-popconfirm>
                    </a-menu-item>
                  </a-menu>
                </a-dropdown>
              </template>
              <template slot="extra">
                <span>
                  {{ item.team }}
                </span>
              </template>
              <a-list-item-meta>
                <template slot="description">
                  {{ item.description }}
                </template>
                <span
                  slot="title"
                  style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                >
                  {{ item.name }}
                </span>
              </a-list-item-meta>
              <a :href="item.url" target="_blank">{{ item.url }}</a>
            </a-list-item>
          </a-list>
          <!-- Desktop -->
          <a-table
            v-else
            :columns="table.columns"
            :dataSource="table.data"
            :loading="table.loading"
            :rowKey="link => link.id"
            :scrollToFirstRowOnChange="true"
          >
            <template slot="url" slot-scope="text">
              <a target="_blank" :href="text">{{ text }}</a>
            </template>
            <ellipsis :length="15" tooltip slot="name" slot-scope="text">{{ text }}</ellipsis>
            <span slot="action" slot-scope="text, record">
              <a href="javascript:void(0);" @click="form.model = record">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm
                :title="'你确定要删除【' + record.name + '】链接？'"
                @confirm="handleDeleteLink(record.id)"
                okText="确定"
                cancelText="取消"
              >
                <a href="javascript:;">删除</a>
              </a-popconfirm>
            </span>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
    <div style="position: fixed;bottom: 30px;right: 30px;">
      <a-button
        type="primary"
        shape="circle"
        icon="setting"
        size="large"
        @click="optionsModal.visible = true"
      ></a-button>
    </div>
    <a-modal v-model="optionsModal.visible" title="页面设置" :afterClose="() => (optionsModal.visible = false)">
      <template slot="footer">
        <a-button key="submit" type="primary" @click="handleSaveOptions()">保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item label="页面标题：" help="* 需要主题进行适配">
          <a-input v-model="optionsModal.data.links_title" />
        </a-form-item>
      </a-form>
    </a-modal>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import optionApi from '@/api/option'
import linkApi from '@/api/link'
const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '网址',
    dataIndex: 'url',
    ellipsis: true,
    scopedSlots: { customRender: 'url' }
  },
  {
    title: '分组',
    ellipsis: true,
    dataIndex: 'team'
  },
  {
    title: '排序',
    dataIndex: 'priority'
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  mixins: [mixin, mixinDevice],
  components: {
    PageView
  },
  data() {
    return {
      table: {
        columns,
        data: [],
        loading: false
      },
      form: {
        model: {},
        saving: false,
        errored: false,
        rules: {
          name: [
            { required: true, message: '* 友情链接名称不能为空', trigger: ['change'] },
            { max: 255, message: '* 友情链接名称的字符长度不能超过 255', trigger: ['change'] }
          ],
          url: [
            { required: true, message: '* 友情链接地址不能为空', trigger: ['change'] },
            { max: 1023, message: '* 友情链接地址的字符长度不能超过 1023', trigger: ['change'] },
            { type: 'url', message: '* 友情链接地址格式有误', trigger: ['change'] }
          ],
          logo: [{ max: 1023, message: '* 友情链接 Logo 的字符长度不能超过 1023', trigger: ['change'] }],
          description: [{ max: 255, message: '* 友情链接描述的字符长度不能超过 255', trigger: ['change'] }],
          team: [{ max: 255, message: '* 友情链接分组的字符长度 255', trigger: ['change'] }]
        }
      },
      optionsModal: {
        visible: false,
        data: []
      },
      teams: []
    }
  },
  computed: {
    title() {
      if (this.isUpdateMode) {
        return '修改友情链接'
      }
      return '添加友情链接'
    },
    isUpdateMode() {
      return !!this.form.model.id
    },
    computedTeams() {
      return this.teams.filter(item => {
        return item !== ''
      })
    }
  },
  created() {
    this.handleListLinks()
    this.handleListLinkTeams()
    this.handleListOptions()
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    handleListLinks() {
      this.table.loading = true
      linkApi
        .listAll()
        .then(response => {
          this.table.data = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.table.loading = false
          }, 200)
        })
    },
    handleListLinkTeams() {
      linkApi.listTeams().then(response => {
        this.teams = response.data.data
      })
    },
    handleListOptions() {
      optionApi.listAll().then(response => {
        this.optionsModal.data = response.data.data
      })
    },
    handleDeleteLink(id) {
      linkApi
        .delete(id)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListLinks()
          this.handleListLinkTeams()
        })
    },
    handleParseUrl() {
      linkApi.getByParse(this.form.model.url).then(response => {
        this.form.model = response.data.data
      })
    },
    handleCreateOrUpdateLink() {
      const _this = this
      _this.$refs.linkForm.validate(valid => {
        if (valid) {
          _this.form.saving = true
          if (_this.isUpdateMode) {
            linkApi
              .update(_this.form.model.id, _this.form.model)
              .catch(() => {
                this.form.errored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          } else {
            linkApi
              .create(_this.form.model)
              .catch(() => {
                this.form.errored = true
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
    handleSavedCallback() {
      if (this.form.errored) {
        this.form.errored = false
      } else {
        this.form.model = {}
        this.handleListLinks()
        this.handleListLinkTeams()
      }
    },
    handleSaveOptions() {
      optionApi
        .save(this.optionsModal.data)
        .then(() => {
          this.$message.success('保存成功！')
          this.optionsModal.visible = false
        })
        .finally(() => {
          this.handleListOptions()
          this.refreshOptionsCache()
        })
    }
  }
}
</script>

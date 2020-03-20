<template>
  <div>
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card
          :title="title"
          :bodyStyle="{ padding: '16px' }"
        >
          <a-form layout="horizontal">
            <a-form-item label="网站名称：">
              <a-input v-model="link.name" />
            </a-form-item>
            <a-form-item
              label="网站地址："
              help="* 需要加上 http://"
            >
              <a-input v-model="link.url">
                <!-- <a
                  href="javascript:void(0);"
                  slot="addonAfter"
                  @click="handleParseUrl"
                >
                  <a-icon type="sync" />
                </a> -->
              </a-input>
            </a-form-item>
            <a-form-item label="Logo：">
              <a-input v-model="link.logo" />
            </a-form-item>
            <a-form-item label="分组：">
              <a-auto-complete
                :dataSource="teams"
                v-model="link.team"
                allowClear
              />
            </a-form-item>
            <a-form-item label="排序编号：">
              <a-input
                type="number"
                v-model="link.priority"
              />
            </a-form-item>
            <a-form-item label="描述：">
              <a-input
                type="textarea"
                :autoSize="{ minRows: 5 }"
                v-model="link.description"
              />
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                @click="handleSaveClick"
                v-if="formType==='create'"
              >保存</a-button>
              <a-button-group v-else>
                <a-button
                  type="primary"
                  @click="handleSaveClick"
                >更新</a-button>
                <a-button
                  type="dashed"
                  @click="handleAddLink"
                  v-if="formType==='update'"
                >返回添加</a-button>
              </a-button-group>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col
        :xl="14"
        :lg="14"
        :md="14"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card
          title="所有友情链接"
          :bodyStyle="{ padding: '16px' }"
        >
          <!-- Mobile -->
          <a-list
            v-if="isMobile()"
            itemLayout="vertical"
            size="large"
            :dataSource="links"
            :loading="loading"
          >
            <a-list-item
              slot="renderItem"
              slot-scope="item, index"
              :key="index"
            >
              <template slot="actions">
                <a-dropdown
                  placement="topLeft"
                  :trigger="['click']"
                >
                  <span>
                    <a-icon type="bars" />
                  </span>
                  <a-menu slot="overlay">
                    <a-menu-item>
                      <a
                        href="javascript:;"
                        @click="handleEditLink(item.id)"
                      >编辑</a>
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
              <a
                :href="item.url"
                target="_blank"
              >{{ item.url }}</a>
            </a-list-item>
          </a-list>
          <!-- Desktop -->
          <a-table
            v-else
            :columns="columns"
            :dataSource="links"
            :loading="loading"
            :rowKey="link => link.id"
            :scrollToFirstRowOnChange="true"
          >
            <template
              slot="url"
              slot-scope="text"
            >
              <a
                target="_blank"
                :href="text"
              >{{ text }}</a>
            </template>
            <ellipsis
              :length="15"
              tooltip
              slot="name"
              slot-scope="text"
            >{{ text }}</ellipsis>
            <span
              slot="action"
              slot-scope="text, record"
            >
              <a
                href="javascript:;"
                @click="handleEditLink(record.id)"
              >编辑</a>
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
        @click="optionFormVisible=true"
      ></a-button>
    </div>
    <a-modal
      v-model="optionFormVisible"
      title="页面设置"
      :afterClose="onOptionFormClose"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleSaveOptions()"
        >保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item label="页面标题：" help="* 需要主题进行适配">
          <a-input v-model="options.links_title" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/utils/mixin.js'
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
  data() {
    return {
      formType: 'create',
      optionFormVisible: false,
      data: [],
      loading: false,
      columns,
      links: [],
      link: {},
      teams: [],
      options: []
    }
  },
  computed: {
    title() {
      if (this.link.id) {
        return '修改友情链接'
      }
      return '添加友情链接'
    }
  },
  created() {
    this.loadLinks()
    this.loadTeams()
    this.loadFormOptions()
  },
  methods: {
    ...mapActions(['loadOptions']),
    loadLinks() {
      this.loading = true
      linkApi.listAll().then(response => {
        this.links = response.data.data
        this.loading = false
      })
    },
    loadTeams() {
      linkApi.listTeams().then(response => {
        this.teams = response.data.data
      })
    },
    loadFormOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    handleSaveClick() {
      this.createOrUpdateLink()
    },
    handleAddLink() {
      this.formType = 'create'
      this.link = {}
    },
    handleEditLink(id) {
      linkApi.get(id).then(response => {
        this.link = response.data.data
        this.formType = 'update'
      })
    },
    handleDeleteLink(id) {
      linkApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadLinks()
        this.loadTeams()
      })
    },
    handleParseUrl() {
      linkApi.getByParse(this.link.url).then(response => {
        this.link = response.data.data
      })
    },
    createOrUpdateLink() {
      if (!this.link.name) {
        this.$notification['error']({
          message: '提示',
          description: '网站名称不能为空！'
        })
        return
      }
      if (!this.link.url) {
        this.$notification['error']({
          message: '提示',
          description: '网站地址不能为空！'
        })
        return
      }
      if (this.link.id) {
        linkApi.update(this.link.id, this.link).then(response => {
          this.$message.success('更新成功！')
          this.loadLinks()
          this.loadTeams()
        })
      } else {
        linkApi.create(this.link).then(response => {
          this.$message.success('保存成功！')
          this.loadLinks()
          this.loadTeams()
        })
      }
      this.handleAddLink()
    },
    handleSaveOptions() {
      optionApi.save(this.options).then(response => {
        this.loadFormOptions()
        this.loadOptions()
        this.$message.success('保存成功！')
        this.optionFormVisible = false
      })
    },
    onOptionFormClose() {
      this.optionFormVisible = false
    }
  }
}
</script>

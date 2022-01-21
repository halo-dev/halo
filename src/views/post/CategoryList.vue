<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :lg="10" :md="10" :sm="24" :xl="10" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }" :title="title">
          <a-form-model ref="categoryForm" :model="form.model" :rules="form.rules" layout="horizontal">
            <a-form-model-item help="* 页面上所显示的名称" label="名称：" prop="name">
              <a-input v-model="form.model.name" />
            </a-form-model-item>
            <a-form-model-item help="* 一般为单个分类页面的标识，最好为英文" label="别名：" prop="slug">
              <a-input v-model="form.model.slug" />
            </a-form-model-item>
            <a-form-model-item label="上级目录：" prop="parentId">
              <category-select-tree v-model="form.model.parentId" :categories="table.data" />
            </a-form-model-item>
            <a-form-model-item help="* 在分类页面可展示，需要主题支持" label="封面图：" prop="thumbnail">
              <AttachmentInput v-model="form.model.thumbnail" title="选择封面图" />
            </a-form-model-item>
            <a-form-model-item help="* 分类密码" label="密码：" prop="password">
              <a-input-password v-model="form.model.password" autocomplete="new-password" />
            </a-form-model-item>
            <a-form-model-item help="* 分类描述，需要主题支持" label="描述：" prop="description">
              <a-input v-model="form.model.description" :autoSize="{ minRows: 3 }" type="textarea" />
            </a-form-model-item>
            <a-form-model-item>
              <ReactiveButton
                v-if="!isUpdateMode"
                :errored="form.errored"
                :loading="form.saving"
                erroredText="保存失败"
                loadedText="保存成功"
                text="保存"
                type="primary"
                @callback="handleSavedCallback"
                @click="handleCreateOrUpdateCategory"
              ></ReactiveButton>
              <a-button-group v-else>
                <ReactiveButton
                  :errored="form.errored"
                  :loading="form.saving"
                  erroredText="更新失败"
                  loadedText="更新成功"
                  text="更新"
                  type="primary"
                  @callback="handleSavedCallback"
                  @click="handleCreateOrUpdateCategory"
                ></ReactiveButton>
                <a-button type="dashed" @click="form.model = {}">返回添加</a-button>
              </a-button-group>
            </a-form-model-item>
          </a-form-model>
        </a-card>
      </a-col>
      <a-col :lg="14" :md="14" :sm="24" :xl="14" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }" title="分类列表">
          <!-- Mobile -->
          <a-list
            v-if="isMobile()"
            :dataSource="table.data"
            :loading="table.loading"
            :pagination="false"
            itemLayout="vertical"
            size="large"
          >
            <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
              <template slot="actions">
                <span>
                  <a-icon type="form" />
                  {{ item.postCount }}
                </span>
                <a-dropdown :trigger="['click']" placement="topLeft">
                  <span>
                    <a-icon type="bars" />
                  </span>
                  <a-menu slot="overlay">
                    <a-menu-item>
                      <a href="javascript:void(0);" @click="form.model = item">编辑</a>
                    </a-menu-item>
                    <a-menu-item>
                      <a-popconfirm
                        :title="'你确定要删除【' + item.name + '】分类？'"
                        cancelText="取消"
                        okText="确定"
                        @confirm="handleDeleteCategory(item.id)"
                      >
                        <a href="javascript:void(0);">删除</a>
                      </a-popconfirm>
                    </a-menu-item>
                  </a-menu>
                </a-dropdown>
              </template>
              <a-list-item-meta>
                <template slot="description">
                  {{ item.slug }}
                </template>
                <span
                  slot="title"
                  style="
                    max-width: 300px;
                    display: block;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                >
                  {{ item.name }}{{ item.password ? '（加密）' : '' }}
                </span></a-list-item-meta
              >
              x
              <span>
                {{ item.description }}
              </span>
            </a-list-item>
          </a-list>
          <!-- Desktop -->
          <a-table
            v-else
            :columns="table.columns"
            :dataSource="table.data"
            :loading="table.loading"
            :rowKey="record => record.id"
            :scrollToFirstRowOnChange="true"
          >
            <span slot="name" slot-scope="text, record" class="cursor-pointer">
              {{ record.name }}{{ record.password ? '（加密）' : '' }}
            </span>
            <span
              slot="postCount"
              slot-scope="text, record"
              class="cursor-pointer"
              @click="handleQueryCategoryPosts(record)"
            >
              <a-badge
                :count="record.postCount"
                :numberStyle="{ backgroundColor: '#00e0ff' }"
                :overflowCount="9999"
                :showZero="true"
              />
            </span>
            <span slot="action" slot-scope="text, record">
              <a href="javascript:void(0);" @click="form.model = record">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm
                :title="'你确定要删除【' + record.name + '】分类？'"
                cancelText="取消"
                okText="确定"
                @confirm="handleDeleteCategory(record.id)"
              >
                <a href="javascript:void(0);">删除</a>
              </a-popconfirm>
            </span>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import CategorySelectTree from './components/CategorySelectTree'
import apiClient from '@/utils/api-client'

const columns = [
  {
    title: '名称',
    ellipsis: true,
    dataIndex: 'name',
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '别名',
    ellipsis: true,
    dataIndex: 'slug'
  },
  {
    title: '文章数',
    dataIndex: 'postCount',
    scopedSlots: { customRender: 'postCount' }
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' }
  }
]

export default {
  components: { PageView, CategorySelectTree },
  mixins: [mixin, mixinDevice],
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
            { required: true, message: '* 分类名称不能为空', trigger: ['change'] },
            { max: 255, message: '* 分类名称的字符长度不能超过 255', trigger: ['change'] }
          ],
          slug: [{ max: 255, message: '* 分类别名的字符长度不能超过 255', trigger: ['change'] }],
          thumbnail: [{ max: 1023, message: '* 封面图链接的字符长度不能超过 1023', trigger: ['change'] }],
          description: [{ max: 100, message: '* 分类描述的字符长度不能超过 100', trigger: ['change'] }]
        }
      }
    }
  },
  computed: {
    title() {
      if (this.isUpdateMode) {
        return '修改分类'
      }
      return '添加分类'
    },
    isUpdateMode() {
      return !!this.form.model.id
    }
  },
  created() {
    this.handleListCategories()
  },
  methods: {
    handleListCategories() {
      this.table.loading = true
      apiClient.category
        .list({ sort: [], more: true })
        .then(response => {
          this.table.data = response.data
        })
        .finally(() => {
          this.table.loading = false
        })
    },
    handleDeleteCategory(id) {
      apiClient.category
        .delete(id)
        .then(() => {
          this.$message.success('删除成功！')
          this.form.model = {}
        })
        .finally(() => {
          this.handleListCategories()
        })
    },

    /**
     * Create or update a category.
     */
    handleCreateOrUpdateCategory() {
      const _this = this
      _this.$refs.categoryForm.validate(valid => {
        if (valid) {
          _this.form.saving = true
          if (_this.isUpdateMode) {
            apiClient.category
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
            apiClient.category
              .create(this.form.model)
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
        const _this = this
        _this.form.model = {}
        _this.handleListCategories()
      }
    },
    handleQueryCategoryPosts(category) {
      this.$router.push({ name: 'PostList', query: { categoryId: category.id } })
    }
  }
}
</script>

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
          <a-form-model
            ref="categoryForm"
            :model="categoryToCreate"
            :rules="categoryRules"
            layout="horizontal"
          >
            <a-form-model-item
              label="名称："
              help="* 页面上所显示的名称"
              prop="name"
            >
              <a-input v-model="categoryToCreate.name" />
            </a-form-model-item>
            <a-form-model-item
              label="别名："
              help="* 一般为单个分类页面的标识，最好为英文"
              prop="slug"
            >
              <a-input v-model="categoryToCreate.slug" />
            </a-form-model-item>
            <a-form-model-item
              label="上级目录："
              prop="parentId"
            >
              <category-select-tree
                :categories="categories"
                v-model="categoryToCreate.parentId"
              />
            </a-form-model-item>
            <a-form-model-item
              label="封面图："
              help="* 在分类页面可展示，需要主题支持"
              prop="thumbnail"
            >
              <a-input v-model="categoryToCreate.thumbnail">
                <a
                  href="javascript:void(0);"
                  slot="addonAfter"
                  @click="thumbnailDrawerVisible = true"
                >
                  <a-icon type="picture" />
                </a>
              </a-input>
            </a-form-model-item>
            <a-form-model-item
              label="描述："
              help="* 分类描述，部分主题可显示"
              prop="description"
            >
              <a-input
                type="textarea"
                v-model="categoryToCreate.description"
                :autoSize="{ minRows: 3 }"
              />
            </a-form-model-item>
            <a-form-model-item>
              <a-button
                type="primary"
                @click="handleSaveClick"
                v-if="!isUpdateForm"
              >保存</a-button>
              <a-button-group v-else>
                <a-button
                  type="primary"
                  @click="handleSaveClick"
                >更新</a-button>
                <a-button
                  type="dashed"
                  @click="categoryToCreate = {}"
                >返回添加</a-button>
              </a-button-group>
            </a-form-model-item>
          </a-form-model>
        </a-card>
      </a-col>
      <a-col
        :xl="14"
        :lg="14"
        :md="14"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '1rem' }"
      >
        <a-card
          title="分类列表"
          :bodyStyle="{ padding: '16px' }"
        >
          <!-- Mobile -->
          <a-list
            v-if="isMobile()"
            itemLayout="vertical"
            size="large"
            :pagination="false"
            :dataSource="categories"
            :loading="loading"
          >
            <a-list-item
              slot="renderItem"
              slot-scope="item, index"
              :key="index"
            >
              <template slot="actions">
                <span>
                  <a-icon type="form" />
                  {{ item.postCount }}
                </span>
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
                        href="javascript:void(0);"
                        @click="handleEditCategory(item)"
                      >编辑</a>
                    </a-menu-item>
                    <a-menu-item>
                      <a-popconfirm
                        :title="'你确定要添加【' + item.name + '】到菜单？'"
                        @confirm="handleCategoryToMenu(item)"
                        okText="确定"
                        cancelText="取消"
                      >
                        <a href="javascript:void(0);">添加到菜单</a>
                      </a-popconfirm>
                    </a-menu-item>
                    <a-menu-item>
                      <a-popconfirm
                        :title="'你确定要删除【' + item.name + '】分类？'"
                        @confirm="handleDeleteCategory(item.id)"
                        okText="确定"
                        cancelText="取消"
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
                  style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                >
                  {{ item.name }}
                </span>

              </a-list-item-meta>
              <span>
                {{ item.description }}
              </span>
            </a-list-item>
          </a-list>
          <!-- Desktop -->
          <a-table
            v-else
            :columns="columns"
            :dataSource="categories"
            :rowKey="record => record.id"
            :loading="loading"
            :scrollToFirstRowOnChange="true"
          >
            <span
              slot="postCount"
              slot-scope="text,record"
              style="cursor: pointer;"
              @click="handleQueryCategoryPosts(record)"
            >
              <a-badge
                :count="record.postCount"
                :numberStyle="{backgroundColor: '#00e0ff'} "
                :showZero="true"
                :overflowCount="9999"
              />
            </span>
            <span
              slot="action"
              slot-scope="text, record"
            >
              <a
                href="javascript:;"
                @click="handleEditCategory(record)"
              >编辑</a>
              <a-divider type="vertical" />
              <a-dropdown :trigger="['click']">
                <a
                  href="javascript:void(0);"
                  class="ant-dropdown-link"
                >更多</a>
                <a-menu slot="overlay">
                  <a-menu-item key="1">
                    <a-popconfirm
                      :title="'你确定要添加【' + record.name + '】到菜单？'"
                      @confirm="handleCategoryToMenu(record)"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:void(0);">添加到菜单</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item key="2">
                    <a-popconfirm
                      :title="'你确定要删除【' + record.name + '】分类？'"
                      @confirm="handleDeleteCategory(record.id)"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:void(0);">删除</a>
                    </a-popconfirm>
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </span>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="thumbnailDrawerVisible"
      @listenToSelect="handleSelectThumbnail"
      title="选择封面图"
    />
  </div>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import CategorySelectTree from './components/CategorySelectTree'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import categoryApi from '@/api/category'
import menuApi from '@/api/menu'

const columns = [
  {
    title: '名称',
    ellipsis: true,
    dataIndex: 'name'
  },
  {
    title: '别名',
    ellipsis: true,
    dataIndex: 'slug'
  },
  {
    title: '描述',
    ellipsis: true,
    dataIndex: 'description'
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
  components: { CategorySelectTree, AttachmentSelectDrawer },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      categories: [],
      categoryToCreate: {},
      thumbnailDrawerVisible: false,
      loading: false,
      columns,
      categoryRules: {
        name: [
          { required: true, message: '* 分类名称不能为空', trigger: ['change', 'blur'] },
          { max: 255, message: '* 分类名称的字符长度不能超过 255', trigger: ['change', 'blur'] }
        ],
        slug: [{ max: 255, message: '* 分类别名的字符长度不能超过 255', trigger: ['change', 'blur'] }],
        thumbnail: [{ max: 1023, message: '* 封面图链接的字符长度不能超过 1023', trigger: ['change', 'blur'] }],
        description: [{ max: 100, message: '* 分类描述的字符长度不能超过 100', trigger: ['change', 'blur'] }]
      }
    }
  },
  computed: {
    title() {
      if (this.categoryToCreate.id) {
        return '修改分类'
      }
      return '添加分类'
    },
    isUpdateForm() {
      return this.categoryToCreate.id
    }
  },
  created() {
    this.loadCategories()
  },
  methods: {
    loadCategories() {
      this.loading = true
      categoryApi
        .listAll(true)
        .then(response => {
          this.categories = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleSaveClick() {
      this.createOrUpdateCategory()
    },
    handleEditCategory(category) {
      this.categoryToCreate = category
    },
    handleDeleteCategory(id) {
      categoryApi
        .delete(id)
        .then(response => {
          this.$message.success('删除成功！')
          this.categoryToCreate = {}
        })
        .finally(() => {
          this.loadCategories()
        })
    },
    createOrUpdateCategory() {
      this.$refs.categoryForm.validate(valid => {
        if (valid) {
          if (this.categoryToCreate.id) {
            categoryApi
              .update(this.categoryToCreate.id, this.categoryToCreate)
              .then(response => {
                this.$message.success('更新成功！')
                this.categoryToCreate = {}
              })
              .finally(() => {
                this.loadCategories()
              })
          } else {
            categoryApi
              .create(this.categoryToCreate)
              .then(response => {
                this.$message.success('保存成功！')
                this.categoryToCreate = {}
              })
              .finally(() => {
                this.loadCategories()
              })
          }
        }
      })
    },
    handleCategoryToMenu(category) {
      const menu = {
        name: category.name,
        url: `${category.fullPath}`
      }
      menuApi.create(menu).then(response => {
        this.$message.success('添加到菜单成功！')
      })
    },
    handleSelectThumbnail(data) {
      this.$set(this.categoryToCreate, 'thumbnail', encodeURI(data.path))
      this.thumbnailDrawerVisible = false
    },
    handleQueryCategoryPosts(category) {
      this.$router.push({ name: 'PostList', query: { categoryId: category.id } })
    }
  }
}
</script>

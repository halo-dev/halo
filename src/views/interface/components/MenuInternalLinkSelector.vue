<template>
  <a-modal v-model="visible" title="从系统预设链接添加菜单" :width="1024" :bodyStyle="{ padding: '0 24px 24px' }">
    <template slot="footer">
      <a-button @click="handleCancel">
        取消
      </a-button>
      <ReactiveButton
        @click="handleCreateBatch"
        @callback="handleCreateBatchCallback"
        :loading="saving"
        :errored="saveErrored"
        text="添加"
        loadedText="添加成功"
        erroredText="添加失败"
        :disabled="menus && menus.length <= 0"
      ></ReactiveButton>
    </template>
    <a-row :gutter="24">
      <a-col :span="12">
        <a-spin :spinning="loading">
          <div class="custom-tab-wrapper">
            <a-tabs default-active-key="1">
              <a-tab-pane key="1" tab="分类目录" force-render>
                <a-list item-layout="horizontal">
                  <a-list-item v-for="(category, index) in categories" :key="index">
                    <a-list-item-meta>
                      <span slot="title">{{ category.name }}</span>
                      <span slot="description">{{ category.fullPath }}</span>
                    </a-list-item-meta>
                    <template slot="actions">
                      <a href="javascript:void(0);" class="text-base">
                        <a-icon type="plus-circle" @click="handleInsertPre(category.name, category.fullPath)" />
                      </a>
                    </template>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane key="2" tab="标签">
                <a-list item-layout="horizontal">
                  <a-list-item v-for="(tag, index) in tags" :key="index">
                    <a-list-item-meta>
                      <span slot="title">{{ tag.name }}</span>
                      <span slot="description">{{ tag.fullPath }}</span>
                    </a-list-item-meta>
                    <template slot="actions">
                      <a href="javascript:void(0);" class="text-base">
                        <a-icon type="plus-circle" @click="handleInsertPre(tag.name, tag.fullPath)" />
                      </a>
                    </template>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane key="3" tab="独立页面">
                <a-list item-layout="horizontal">
                  <a-list-item v-for="(item, index) in sheet.independents" :key="index">
                    <a-list-item-meta>
                      <span slot="title">{{ item.title }}</span>
                      <span slot="description">{{ item.fullPath }}</span>
                    </a-list-item-meta>
                    <template slot="actions">
                      <a href="javascript:void(0);" class="text-base">
                        <a-icon type="plus-circle" @click="handleInsertPre(item.title, item.fullPath)" />
                      </a>
                    </template>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
              <a-tab-pane key="4" tab="自定义页面">
                <a-list item-layout="horizontal">
                  <a-list-item v-for="(item, index) in sheet.customs.data" :key="index">
                    <a-list-item-meta>
                      <span slot="title">{{ item.title }}</span>
                      <span slot="description">{{ item.fullPath }}</span>
                    </a-list-item-meta>
                    <template slot="actions">
                      <a href="javascript:void(0);" class="text-base">
                        <a-icon type="plus-circle" @click="handleInsertPre(item.title, item.fullPath)" />
                      </a>
                    </template>
                  </a-list-item>
                </a-list>
                <div class="page-wrapper">
                  <a-pagination
                    class="pagination"
                    :current="sheet.customs.pagination.page"
                    :total="sheet.customs.pagination.total"
                    :defaultPageSize="sheet.customs.pagination.size"
                    :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
                    showSizeChanger
                    @showSizeChange="handleSheetPaginationChange"
                    @change="handleSheetPaginationChange"
                    showLessItems
                  />
                </div>
              </a-tab-pane>
              <a-tab-pane key="5" tab="其他">
                <a-list item-layout="horizontal">
                  <a-list-item v-for="(item, index) in otherInternalLinks" :key="index">
                    <a-list-item-meta>
                      <span slot="title">{{ item.name }}</span>
                      <span slot="description">{{ item.url }}</span>
                    </a-list-item-meta>
                    <template slot="actions">
                      <a href="javascript:void(0);" class="text-base">
                        <a-icon type="plus-circle" @click="handleInsertPre(item.name, item.url)" />
                      </a>
                    </template>
                  </a-list-item>
                </a-list>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-spin>
      </a-col>
      <a-col :span="12">
        <div class="custom-tab-wrapper">
          <a-tabs default-active-key="1">
            <a-tab-pane key="1" tab="备选" force-render>
              <a-list item-layout="horizontal">
                <a-list-item v-for="(menu, index) in menus" :key="index">
                  <a-list-item-meta>
                    <span slot="title">{{ menu.name }}</span>
                    <span slot="description">{{ menu.url }}</span>
                  </a-list-item-meta>
                  <template slot="actions">
                    <a href="javascript:void(0);" class="text-base" @click="handleRemovePre(index)">
                      <a-icon type="close-circle" />
                    </a>
                  </template>
                </a-list-item>
              </a-list>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </a-modal>
</template>
<script>
import categoryApi from '@/api/category'
import tagApi from '@/api/tag'
import menuApi from '@/api/menu'
import sheetApi from '@/api/sheet'
import optionApi from '@/api/option'

export default {
  name: 'MenuInternalLinkSelector',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    team: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      options: {},
      categories: [],
      tags: [],
      menus: [],
      sheet: {
        independents: [],
        customs: {
          data: [],
          pagination: {
            page: 1,
            size: 10,
            sort: null,
            total: 1
          },
          queryParam: {
            page: 0,
            size: 10,
            sort: null
          }
        }
      },
      loading: false,
      saving: false,
      saveErrored: false
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('input', value)
      }
    },
    otherInternalLinks() {
      const options = this.options
      const pathSuffix = this.options.path_suffix ? this.options.path_suffix : ''
      return [
        {
          name: '分类目录',
          url: `${options.blog_url}/${options.categories_prefix}${pathSuffix}`
        },
        {
          name: '标签',
          url: `${options.blog_url}/${options.tags_prefix}${pathSuffix}`
        },
        {
          name: '文章归档',
          url: `${options.blog_url}/${options.archives_prefix}${pathSuffix}`
        },
        {
          name: 'RSS',
          url: `${options.blog_url}/atom.xml`
        },
        {
          name: '网站地图',
          url: `${options.blog_url}/sitemap.xml`
        },
        {
          name: '网站地图',
          url: `${options.blog_url}/sitemap.html`
        }
      ]
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.handleFetchAll()
        this.handleListSheets()
      }
    }
  },
  methods: {
    handleFetchAll() {
      this.loading = true
      Promise.all([optionApi.listAll(), categoryApi.listAll(true), tagApi.listAll(true), sheetApi.listIndependent()])
        .then(response => {
          this.options = response[0].data.data
          this.categories = response[1].data.data
          this.tags = response[2].data.data
          this.sheet.independents = response[3].data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 200)
        })
    },
    handleListSheets() {
      this.sheet.customs.queryParam.page = this.sheet.customs.pagination.page - 1
      this.sheet.customs.queryParam.size = this.sheet.customs.pagination.size
      this.sheet.customs.queryParam.sort = this.sheet.customs.pagination.sort
      sheetApi.list(this.sheet.customs.queryParam).then(response => {
        this.sheet.customs.data = response.data.data.content
        this.sheet.customs.pagination.total = response.data.data.total
      })
    },
    handleSheetPaginationChange(page, pageSize) {
      this.sheet.customs.pagination.page = page
      this.sheet.customs.pagination.size = pageSize
      this.handleListSheets()
    },
    handleInsertPre(name, url) {
      this.menus.push({
        name: name,
        url: url,
        team: this.team
      })
    },
    handleRemovePre(index) {
      this.menus.splice(index, 1)
    },
    handleCancel() {
      this.menus = []
      this.visible = false
      this.$emit('reload')
    },
    handleCreateBatch() {
      this.saving = true
      menuApi
        .createBatch(this.menus)
        .catch(() => {
          this.saveErrored = false
        })
        .finally(() => {
          setTimeout(() => {
            this.saving = false
          }, 400)
        })
    },
    handleCreateBatchCallback() {
      if (this.saveErrored) {
        this.saveErrored = false
      } else {
        this.handleCancel()
      }
    }
  }
}
</script>

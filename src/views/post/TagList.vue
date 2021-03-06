<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :xl="10" :lg="10" :md="10" :sm="24" :xs="24" class="pb-3">
        <a-card :title="title" :bodyStyle="{ padding: '16px' }">
          <a-form-model ref="tagForm" :model="form.model" :rules="form.rules" layout="horizontal">
            <a-form-model-item label="名称：" help="* 页面上所显示的名称" prop="name">
              <a-input v-model="form.model.name" />
            </a-form-model-item>
            <a-form-model-item label="别名：" help="* 一般为单个标签页面的标识，最好为英文" prop="slug">
              <a-input v-model="form.model.slug" />
            </a-form-model-item>
            <a-form-model-item label="封面图：" help="* 在标签页面可展示，需要主题支持" prop="thumbnail">
              <a-input v-model="form.model.thumbnail">
                <a href="javascript:void(0);" slot="addonAfter" @click="thumbnailDrawer.visible = true">
                  <a-icon type="picture" />
                </a>
              </a-input>
            </a-form-model-item>
            <a-form-model-item>
              <ReactiveButton
                v-if="!isUpdateMode"
                type="primary"
                @click="handleCreateOrUpdateTag"
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
                  @click="handleCreateOrUpdateTag"
                  @callback="handleSavedCallback"
                  :loading="form.saving"
                  :errored="form.errored"
                  text="更新"
                  loadedText="更新成功"
                  erroredText="更新失败"
                ></ReactiveButton>
                <a-button type="dashed" @click="form.model = {}">返回添加</a-button>
              </a-button-group>
              <a-popconfirm
                :title="'你确定要删除【' + form.model.name + '】标签？'"
                @confirm="handleDeleteTag(form.model.id)"
                okText="确定"
                cancelText="取消"
                v-if="isUpdateMode"
              >
                <a-button type="danger" class="float-right">删除</a-button>
              </a-popconfirm>
            </a-form-model-item>
          </a-form-model>
        </a-card>
      </a-col>
      <a-col :xl="14" :lg="14" :md="14" :sm="24" :xs="24" class="pb-3">
        <a-card title="所有标签" :bodyStyle="{ padding: '16px' }">
          <a-spin :spinning="list.loading">
            <a-empty v-if="list.data.length == 0" />
            <a-tooltip placement="topLeft" v-for="tag in list.data" :key="tag.id" v-else>
              <template slot="title">
                <span>{{ tag.postCount }} 篇文章</span>
              </template>
              <a-tag color="blue" style="margin-bottom: 8px;cursor:pointer;" @click="form.model = tag">{{
                tag.name
              }}</a-tag>
            </a-tooltip>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="thumbnailDrawer.visible"
      @listenToSelect="handleSelectThumbnail"
      title="选择封面图"
    />
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import tagApi from '@/api/tag'

export default {
  components: { PageView },
  data() {
    return {
      list: {
        data: [],
        loading: false
      },
      form: {
        model: {},
        saving: false,
        errored: false,
        rules: {
          name: [
            { required: true, message: '* 标签名称不能为空', trigger: ['change'] },
            { max: 255, message: '* 标签名称的字符长度不能超过 255', trigger: ['change'] }
          ],
          slug: [{ max: 255, message: '* 标签别名的字符长度不能超过 255', trigger: ['change'] }],
          thumbnail: [{ max: 1023, message: '* 封面图链接的字符长度不能超过 1023', trigger: ['change'] }]
        }
      },
      thumbnailDrawer: {
        visible: false
      }
    }
  },
  computed: {
    title() {
      if (this.isUpdateMode) {
        return '修改标签'
      }
      return '添加标签'
    },
    isUpdateMode() {
      return !!this.form.model.id
    }
  },
  created() {
    this.handleListTags()
  },
  methods: {
    handleListTags() {
      this.list.loading = true
      tagApi
        .listAll(true)
        .then(response => {
          this.list.data = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.list.loading = false
          }, 200)
        })
    },
    handleDeleteTag(tagId) {
      tagApi.delete(tagId).finally(() => {
        this.form.model = {}
        this.handleListTags()
      })
    },
    handleCreateOrUpdateTag() {
      const _this = this
      _this.$refs.tagForm.validate(valid => {
        if (valid) {
          this.form.saving = true
          if (_this.isUpdateMode) {
            tagApi
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
            tagApi
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
      const _this = this
      if (_this.form.errored) {
        _this.form.errored = false
      } else {
        _this.form.model = {}
        _this.handleListTags()
      }
    },
    handleSelectThumbnail(data) {
      this.$set(this.form.model, 'thumbnail', encodeURI(data.path))
      this.thumbnailDrawer.visible = false
    }
  }
}
</script>

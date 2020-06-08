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
            ref="tagForm"
            :model="tagToCreate"
            :rules="tagRules"
            layout="horizontal"
          >
            <a-form-model-item
              label="名称："
              help="* 页面上所显示的名称"
              prop="name"
            >
              <a-input v-model="tagToCreate.name" />
            </a-form-model-item>
            <a-form-model-item
              label="别名："
              help="* 一般为单个标签页面的标识，最好为英文"
              prop="slug"
            >
              <a-input v-model="tagToCreate.slug" />
            </a-form-model-item>
            <a-form-model-item
              label="封面图："
              help="* 在标签页面可展示，需要主题支持"
              prop="thumbnail"
            >
              <a-input v-model="tagToCreate.thumbnail">
                <a
                  href="javascript:void(0);"
                  slot="addonAfter"
                  @click="thumbnailDrawerVisible = true"
                >
                  <a-icon type="picture" />
                </a>
              </a-input>
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
                  @click="tagToCreate = {}"
                >返回添加</a-button>
              </a-button-group>
              <a-popconfirm
                :title="'你确定要删除【' + tagToCreate.name + '】标签？'"
                @confirm="handleDeleteTag(tagToCreate.id)"
                okText="确定"
                cancelText="取消"
                v-if="isUpdateForm"
              >
                <a-button
                  type="danger"
                  style="float:right"
                >删除</a-button>
              </a-popconfirm>
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
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card
          title="所有标签"
          :bodyStyle="{ padding: '16px' }"
        >
          <a-empty v-if="tags.length==0" />
          <a-tooltip
            placement="topLeft"
            v-for="tag in tags"
            :key="tag.id"
            v-else
          >
            <template slot="title">
              <span>{{ tag.postCount }} 篇文章</span>
            </template>
            <a-tag
              color="blue"
              style="margin-bottom: 8px;cursor:pointer;"
              @click="handleEditTag(tag)"
            >{{ tag.name }}</a-tag>
          </a-tooltip>
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
import tagApi from '@/api/tag'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'

export default {
  components: { AttachmentSelectDrawer },
  data() {
    return {
      tags: [],
      tagToCreate: {},
      thumbnailDrawerVisible: false,
      tagRules: {
        name: [
          { required: true, message: '* 标签名称不能为空', trigger: ['change', 'blur'] },
          { max: 255, message: '* 标签名称的字符长度不能超过 255', trigger: ['change', 'blur'] }
        ],
        slug: [{ max: 255, message: '* 标签别名的字符长度不能超过 255', trigger: ['change', 'blur'] }],
        thumbnail: [{ max: 1023, message: '* 封面图链接的字符长度不能超过 1023', trigger: ['change', 'blur'] }]
      }
    }
  },
  computed: {
    title() {
      if (this.tagToCreate.id) {
        return '修改标签'
      }
      return '添加标签'
    },
    isUpdateForm() {
      return this.tagToCreate.id
    }
  },
  created() {
    this.loadTags()
  },
  methods: {
    loadTags() {
      tagApi.listAll(true).then(response => {
        this.tags = response.data.data
      })
    },
    handleSaveClick() {
      this.createOrUpdateTag()
    },
    handleEditTag(tag) {
      this.tagToCreate = tag
    },
    handleDeleteTag(tagId) {
      tagApi
        .delete(tagId)
        .then(response => {
          this.$message.success('删除成功！')
          this.tagToCreate = {}
        })
        .finally(() => {
          this.loadTags()
        })
    },
    createOrUpdateTag() {
      this.$refs.tagForm.validate(valid => {
        if (valid) {
          if (this.tagToCreate.id) {
            tagApi.update(this.tagToCreate.id, this.tagToCreate).then(response => {
              this.$message.success('更新成功！')
              this.tagToCreate = {}
            })
          } else {
            tagApi
              .create(this.tagToCreate)
              .then(response => {
                this.$message.success('保存成功！')
                this.tagToCreate = {}
              })
              .finally(() => {
                this.loadTags()
              })
          }
        }
      })
    },
    handleSelectThumbnail(data) {
      this.$set(this.tagToCreate, 'thumbnail', encodeURI(data.path))
      this.thumbnailDrawerVisible = false
    }
  }
}
</script>

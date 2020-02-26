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
            <a-form-item
              label="名称："
              help="* 页面上所显示的名称"
            >
              <a-input v-model="tagToCreate.name" />
            </a-form-item>
            <a-form-item
              label="别名"
              help="* 一般为单个标签页面的标识，最好为英文"
            >
              <a-input v-model="tagToCreate.slug" />
            </a-form-item>
            <a-form-item
              label="封面图"
              help="* 在标签页面可展示，需要主题支持"
            >
              <a-input v-model="tagToCreate.thumbnail">
                <a
                  href="javascript:void(0);"
                  slot="addonAfter"
                  @click="()=>this.thumbnailDrawerVisible = true"
                >
                  <a-icon type="picture" />
                </a>
              </a-input>
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
                  @click="handleAddTag"
                  v-if="formType==='update'"
                >返回添加</a-button>
              </a-button-group>
              <a-popconfirm
                :title="'你确定要删除【' + tagToCreate.name + '】标签？'"
                @confirm="handleDeleteTag(tagToCreate.id)"
                okText="确定"
                cancelText="取消"
                v-if="formType==='update'"
              >
                <a-button
                  type="danger"
                  style="float:right"
                >删除</a-button>
              </a-popconfirm>
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
              style="margin-bottom: 8px"
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
      formType: 'create',
      tags: [],
      tagToCreate: {},
      thumbnailDrawerVisible: false
    }
  },
  computed: {
    title() {
      if (this.tagToCreate.id) {
        return '修改标签'
      }
      return '添加标签'
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
    handleAddTag() {
      this.formType = 'create'
      this.tagToCreate = {}
    },
    handleEditTag(tag) {
      this.tagToCreate = tag
      this.formType = 'update'
    },
    handleDeleteTag(tagId) {
      tagApi.delete(tagId).then(response => {
        this.$message.success('删除成功！')
        this.loadTags()
        this.handleAddTag()
      })
    },
    createOrUpdateTag() {
      if (!this.tagToCreate.name) {
        this.$notification['error']({
          message: '提示',
          description: '标签名称不能为空！'
        })
        return
      }
      if (this.tagToCreate.id) {
        tagApi.update(this.tagToCreate.id, this.tagToCreate).then(response => {
          this.$message.success('更新成功！')
          this.loadTags()
          this.tagToCreate = {}
        })
      } else {
        tagApi.create(this.tagToCreate).then(response => {
          this.$message.success('保存成功！')
          this.loadTags()
          this.tagToCreate = {}
        })
      }
      this.handleAddTag()
    },
    handleSelectThumbnail(data) {
      this.$set(this.tagToCreate, 'thumbnail', encodeURI(data.path))
      this.thumbnailDrawerVisible = false
    }
  }
}
</script>

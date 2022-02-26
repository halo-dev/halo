<template>
  <a-modal v-model="modalVisible" :afterClose="onClosed" :maskClosable="false" :width="680" destroyOnClose>
    <template #title>
      {{ form.model.id ? '修改' : '添加' }}图片
      <a-icon v-if="loading" type="loading" />
    </template>

    <a-form-model
      ref="photoForm"
      :label-col="{ span: 4 }"
      :model="form.model"
      :rules="form.rules"
      :wrapper-col="{ span: 20 }"
      labelAlign="left"
    >
      <a-form-model-item label="图片地址：" prop="url">
        <a-space direction="vertical">
          <img
            :src="form.model.url || '/images/placeholder.jpg'"
            class="w-1/2 cursor-pointer"
            style="border-radius: 4px"
            @click="attachmentSelectModal.visible = true"
          />
          <a-input v-model="form.model.url" allow-clear placeholder="点击封面图选择图片，或者输入外部链接" />
        </a-space>
      </a-form-model-item>
      <a-form-model-item label="缩略图地址：" prop="thumbnail">
        <a-input v-model="form.model.thumbnail" />
      </a-form-model-item>
      <a-form-model-item label="图片名称：" prop="name">
        <a-input v-model="form.model.name" />
      </a-form-model-item>
      <a-form-model-item label="拍摄日期：" prop="takeTime">
        <a-date-picker
          :defaultValue="takeTimeDefaultValue"
          format="YYYY-MM-DD HH:mm:ss"
          showTime
          style="width: 100%"
          @change="onTakeTimeChange"
          @ok="onTakeTimeChange"
        />
      </a-form-model-item>
      <a-form-model-item label="拍摄地点：" prop="location">
        <a-input v-model="form.model.location" />
      </a-form-model-item>
      <a-form-model-item label="分组：" prop="team">
        <a-auto-complete v-model="form.model.team" :dataSource="teams" allowClear style="width: 100%" />
      </a-form-model-item>
      <a-form-model-item label="描述：" prop="description">
        <a-input v-model="form.model.description" />
      </a-form-model-item>
    </a-form-model>

    <template #footer>
      <slot name="extraFooter" />
      <ReactiveButton
        :errored="form.saveErrored"
        :loading="form.saving"
        erroredText="保存失败"
        loadedText="保存成功"
        text="保存"
        @callback="handleSaveCallback"
        @click="handleSave"
      ></ReactiveButton>
      <a-button :disabled="loading" @click="modalVisible = false"> 关闭</a-button>
    </template>

    <AttachmentSelectModal
      :multiSelect="false"
      :visible.sync="attachmentSelectModal.visible"
      @confirm="handleAttachmentSelected"
    />
  </a-modal>
</template>
<script>
import { datetimeFormat } from '@/utils/datetime'
import apiClient from '@/utils/api-client'

export default {
  name: 'PhotoFormModal',
  props: {
    visible: {
      type: Boolean,
      default: true
    },
    loading: {
      type: Boolean,
      default: false
    },
    photo: {
      type: Object,
      default: () => {}
    },
    teams: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      form: {
        model: {},
        rules: {
          url: [{ required: true, message: '* 图片地址不能为空', trigger: ['change'] }],
          thumbnail: [{ required: true, message: '* 缩略图地址不能为空', trigger: ['change'] }],
          name: [{ required: true, message: '* 图片名称不能为空', trigger: ['change'] }]
        },
        saving: false,
        saveErrored: false
      },
      attachmentSelectModal: {
        visible: false
      }
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    takeTimeDefaultValue() {
      if (this.form.model.takeTime) {
        const date = new Date(this.form.model.takeTime)
        return datetimeFormat(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return datetimeFormat(new Date(), 'YYYY-MM-DD HH:mm:ss')
    }
  },
  watch: {
    modalVisible(value) {
      if (value) {
        this.form.model = Object.assign({}, this.photo)
      }
    },
    photo: {
      deep: true,
      handler(value) {
        this.form.model = Object.assign({}, value)
      }
    }
  },
  methods: {
    onClosed() {},

    handleAttachmentSelected({ raw }) {
      if (raw.length) {
        const { path, thumbPath, name } = raw[0]
        this.$set(this.form.model, 'url', encodeURI(path))
        this.$set(this.form.model, 'thumbnail', encodeURI(thumbPath))
        this.$set(this.form.model, 'name', name)
      }
      this.attachmentSelectModal.visible = false
    },

    onTakeTimeChange(value) {
      this.form.model.takeTime = value.valueOf()
    },

    handleSave() {
      const _this = this
      _this.$refs.photoForm.validate(async valid => {
        if (valid) {
          _this.form.saving = true

          try {
            if (_this.form.model.id) {
              await apiClient.photo.update(_this.form.model.id, _this.form.model)
            } else {
              const { data } = await apiClient.photo.create(_this.form.model)
              this.form.model = data
            }
          } catch (e) {
            _this.form.saveErrored = true
            this.$log.error('Failed to save this photo', e)
          } finally {
            setTimeout(() => {
              _this.form.saving = false
            }, 400)
          }
        }
      })
    },

    handleSaveCallback() {
      if (this.form.saveErrored) {
        this.form.saveErrored = false
      } else {
        this.$emit('succeed', this.form.model)
      }
    }
  }
}
</script>

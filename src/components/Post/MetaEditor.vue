<template>
  <div>
    <a-form>
      <a-form-item v-for="(meta, index) in presetMetas" :key="index">
        <a-row :gutter="5">
          <a-col :span="12">
            <a-input v-model="meta.key" :disabled="true"><i slot="addonBefore">K</i></a-input>
          </a-col>
          <a-col :span="12">
            <a-input v-model="meta.value">
              <i slot="addonBefore">V</i>
            </a-input>
          </a-col>
        </a-row>
      </a-form-item>
    </a-form>
    <a-form>
      <a-form-item v-for="(meta, index) in customMetas" :key="index">
        <a-row :gutter="5">
          <a-col :span="12">
            <a-input v-model="meta.key"><i slot="addonBefore">K</i></a-input>
          </a-col>
          <a-col :span="12">
            <a-input v-model="meta.value">
              <i slot="addonBefore">V</i>
              <a slot="addonAfter" href="javascript:void(0);" @click.prevent="handleRemove(index)">
                <a-icon type="close" />
              </a>
            </a-input>
          </a-col>
        </a-row>
      </a-form-item>
      <a-form-item>
        <a-button type="dashed" @click="handleAdd">新增</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'MetaEditor',
  props: {
    target: {
      type: String,
      default: 'post',
      validator: function(value) {
        return ['post', 'sheet'].indexOf(value) !== -1
      }
    },
    targetId: {
      type: Number,
      default: null
    },
    metas: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      presetFields: [],
      presetMetas: [],
      customMetas: []
    }
  },
  watch: {
    presetMetas: {
      handler() {
        this.handleChange()
      },
      deep: true
    },
    customMetas: {
      handler() {
        this.handleChange()
      },
      deep: true
    },
    targetId() {
      this.handleGenerateMetas()
    }
  },
  created() {
    this.handleListPresetMetasField()
  },
  methods: {
    /**
     * Fetch preset metas fields
     *
     * @returns {Promise<void>}
     */
    async handleListPresetMetasField() {
      try {
        const response = await apiClient.theme.getActivatedTheme()
        this.presetFields = response.data[`${this.target}MetaField`] || []

        this.handleGenerateMetas()
      } catch (e) {
        this.$log.error(e)
      }
    },

    /**
     * Generate preset and custom metas
     */
    handleGenerateMetas() {
      this.presetMetas = this.presetFields.map(field => {
        const meta = this.metas.find(meta => meta.key === field)
        return meta ? { key: field, value: meta.value } : { key: field, value: '' }
      })

      this.customMetas = this.metas
        .filter(meta => this.presetFields.indexOf(meta.key) === -1)
        .map(meta => {
          return {
            key: meta.key,
            value: meta.value
          }
        })
    },

    /**
     * Add a new custom meta
     */
    handleAdd() {
      this.customMetas.push({
        key: '',
        value: ''
      })
    },

    /**
     * Remove custom meta
     *
     * @param index
     */
    handleRemove(index) {
      this.customMetas.splice(index, 1)
    },

    /**
     * Handle change
     */
    handleChange() {
      this.$emit('update:metas', this.presetMetas.concat(this.customMetas))
    }
  }
}
</script>

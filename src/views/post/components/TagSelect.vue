<template>
  <a-select
    v-model="selectedTagNames"
    :token-separators="[',', '|']"
    allowClear
    class="w-full"
    mode="tags"
    placeholder="选择或输入标签"
    @change="handleChange"
  >
    <a-select-option v-for="tag in tags" :key="tag.id" :value="tag.name">{{ tag.name }}</a-select-option>
  </a-select>
</template>

<script>
import apiClient from '@/utils/api-client'
import axios from 'axios'

export default {
  name: 'TagSelect',
  model: {
    prop: 'tagIds',
    event: 'change'
  },
  props: {
    tagIds: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  data() {
    return {
      tags: [],
      selectedTagNames: []
    }
  },
  created() {
    this.handleListTags()
  },
  watch: {
    tags(newValue) {
      if (newValue) {
        this.selectedTagNames = this.tagIds.map(tagId => this.tagIdMap[tagId].name)
      }
    },
    tagIds: {
      handler(newValue) {
        if (!this.tags.length) {
          return
        }
        this.selectedTagNames = newValue.map(tagId => this.tagIdMap[tagId].name)
      },
      deep: true
    }
  },
  computed: {
    tagIdMap() {
      const tagIdMap = {}
      this.tags.forEach(tag => {
        tagIdMap[tag.id] = tag
      })
      return tagIdMap
    },
    tagNameMap() {
      const tagNameMap = {}
      this.tags.forEach(tag => {
        tagNameMap[tag.name] = tag
      })
      return tagNameMap
    }
  },
  methods: {
    handleListTags(callback) {
      apiClient.tag.list({ sort: 'name,asc', more: true }).then(response => {
        this.tags = response.data
        if (callback) {
          callback()
        }
      })
    },
    handleChange() {
      const tagNamesToCreate = this.selectedTagNames.filter(tagName => !this.tagNameMap[tagName])

      this.$log.debug('Tag names to create', tagNamesToCreate)

      if (!tagNamesToCreate.length) {
        const tagIds = this.selectedTagNames.map(tagName => this.tagNameMap[tagName].id)
        this.$emit('change', tagIds)
        return
      }

      const createPromises = tagNamesToCreate.map(tagName => apiClient.tag.create({ name: tagName }))

      axios.all(createPromises).then(
        axios.spread(() => {
          this.handleListTags(() => {
            this.$log.debug('Tag name map', this.tagNameMap)
            // Get all tag id
            const tagIds = this.selectedTagNames.map(tagName => this.tagNameMap[tagName].id)
            this.$emit('change', tagIds)
          })
        })
      )
    }
  }
}
</script>

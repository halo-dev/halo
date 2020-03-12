<template>
  <div>
    <a-select
      v-model="selectedTagNames"
      style="width: 100%"
      allowClear
      mode="tags"
      placeholder="选择或输入标签"
      @change="handleChange"
    >
      <a-select-option
        v-for="tag in tags"
        :key="tag.id"
        :value="tag.name"
      >{{ tag.name }}</a-select-option>
    </a-select>
  </div>
</template>

<script>
import tagApi from '@/api/tag'
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
    this.loadTags()
  },
  watch: {
    tags(newValue, oldValue) {
      // 解决tags未赋上值就使用导致的取值报错问题
      if (newValue) {
        this.selectedTagNames = this.tagIds.map(tagId => this.tagIdMap[tagId].name)
      }
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
    loadTags(callback) {
      tagApi.listAll(true).then(response => {
        this.tags = response.data.data
        if (callback) {
          callback()
        }
      })
    },
    handleChange() {
      this.$log.debug('Changed')
      const tagNamesToCreate = this.selectedTagNames.filter(tagName => !this.tagNameMap[tagName])

      this.$log.debug('Tag names to create', tagNamesToCreate)

      if (tagNamesToCreate === []) {
        const tagIds = this.selectedTagNames.map(tagName => this.tagNameMap[tagName].id)
        // If empty
        this.$emit('change', tagIds)
        return
      }

      const createPromises = tagNamesToCreate.map(tagName => tagApi.createWithName(tagName))

      axios.all(createPromises).then(
        axios.spread(() => {
          this.loadTags(() => {
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

<template>
  <a-list item-layout="horizontal">
    <draggable
      :list="list"
      :value="value"
      class="item-container"
      handle=".mover"
      tag="div"
      v-bind="{
        animation: 300,
        group: 'description',
        ghostClass: 'ghost',
        chosenClass: 'chosen',
        dragClass: 'drag',
        emptyInsertThreshold: 20
      }"
      @end="isDragging = false"
      @input="emitter"
      @start="isDragging = true"
    >
      <transition-group>
        <div v-for="item in realValue" :key="item.id">
          <a-list-item class="menu-item">
            <a-list-item-meta>
              <span slot="title" class="inline-block font-bold title">
                <a-icon class="cursor-move mover mr-1" type="bars" />
                {{ item.name }}{{ item.hasPassword ? '（加密）' : '' }}
              </span>
              <span slot="description" class="inline-block">
                <a :href="item.fullPath" class="ant-anchor-link-title" target="_blank"> {{ item.fullPath }} </a>
              </span>
            </a-list-item-meta>
            <template #actions>
              <a-button class="!p-0" type="link" @click="handleSelect(item)">新增</a-button>
              <a-button class="!p-0" type="link" @click="handleEdit(item)">编辑</a-button>
              <a-button class="!p-0" type="link" @click="handleDelete(item)">删除</a-button>
            </template>
          </a-list-item>
          <div class="a-list-nested" style="margin-left: 44px">
            <CategoryTreeNode
              :list="item.children"
              @edit="handleEdit"
              @reload="$emit('reload')"
              @select="handleSelect"
            />
          </div>
        </div>
      </transition-group>
    </draggable>
  </a-list>
</template>
<script>
// components
import draggable from 'vuedraggable'
import apiClient from '@/utils/api-client'

export default {
  name: 'CategoryTreeNode',
  components: {
    draggable
  },
  props: {
    value: {
      required: false,
      type: Array,
      default: null
    },
    list: {
      required: false,
      type: Array,
      default: null
    }
  },
  computed: {
    realValue() {
      return this.value ? this.value : this.list
    }
  },
  data() {
    return {
      isDragging: false
    }
  },
  methods: {
    emitter(value) {
      this.$emit('input', value)
    },
    handleDelete(item) {
      const _this = this
      _this.$confirm({
        title: '提示',
        content: `确定要删除名为${item.name}的分类？`,
        async onOk() {
          try {
            await apiClient.category.delete(item.id)
            _this.$emit('reload')
          } catch (e) {
            _this.$log.error('Fail to delete category', e)
          }
        }
      })
    },
    handleEdit(item) {
      this.$emit('edit', item)
    },
    handleSelect(item) {
      this.$emit('select', item)
    }
  }
}
</script>

<style scoped>
.ghost {
  opacity: 0.8;
  @apply bg-gray-200;
}

.chosen {
  opacity: 0.8;
  @apply bg-gray-200;
  padding: 0 5px;
}

.drag {
  @apply bg-white;
  padding: 0 5px;
}

::v-deep .ant-list-item-action {
  display: none;
}

::v-deep .menu-item:hover .ant-list-item-action {
  display: block;
}
</style>

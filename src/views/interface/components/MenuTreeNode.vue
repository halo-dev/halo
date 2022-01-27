<template>
  <a-list item-layout="horizontal">
    <draggable
      :list="list"
      :value="value"
      class="item-container"
      handle=".mover"
      tag="div"
      v-bind="dragOptions"
      @end="isDragging = false"
      @input="emitter"
      @start="isDragging = true"
    >
      <transition-group>
        <div v-for="item in realValue" :key="item.id">
          <a-list-item class="menu-item">
            <a-list-item-meta>
              <span slot="title" class="inline-block font-bold title">
                <a-icon class="cursor-pointer mover" type="bars" />
                {{ item.name }}
                <a-tooltip v-if="item.target === '_blank'" title="外部链接">
                  <a-icon type="link" />
                </a-tooltip>
                {{ item.formVisible ? '（正在编辑）' : '' }}
              </span>
              <span slot="description" class="inline-block">
                <a :href="item.url" class="ant-anchor-link-title" target="_blank"> {{ item.url }} </a>
              </span>
            </a-list-item-meta>
            <template slot="actions">
              <a-button v-if="!item.formVisible" class="!p-0" type="link" @click="handleOpenEditForm(item)">
                编辑
              </a-button>
              <a-button v-else class="!p-0" type="link" @click="handleCloseCreateMenuForm(item)">取消编辑</a-button>
            </template>
            <template slot="actions">
              <a-button class="!p-0" type="link" @click="handleDelete(item.id)">删除</a-button>
            </template>
            <template v-if="excludedTeams && excludedTeams.length > 0" slot="actions">
              <a-dropdown :trigger="['click']">
                <a class="ant-dropdown-link" @click="e => e.preventDefault()">
                  更多
                  <a-icon type="down" />
                </a>
                <a-menu slot="overlay">
                  <a-sub-menu title="移动到分组">
                    <a-menu-item v-for="(team, index) in excludedTeams" :key="index" @click="handleMoveMenu(item, team)"
                      >{{ team === '' ? '未分组' : team }}
                    </a-menu-item>
                  </a-sub-menu>
                </a-menu>
              </a-dropdown>
            </template>
          </a-list-item>
          <MenuForm
            v-if="item.formVisible"
            :menu="item"
            @cancel="handleCloseCreateMenuForm(item)"
            @succeed="handleUpdateMenuSucceed(item)"
          />
          <div class="a-list-nested" style="margin-left: 44px">
            <MenuTreeNode :excludedTeams="excludedTeams" :list="item.children" @reload="onReloadEmit" />
          </div>
        </div>
      </transition-group>
    </draggable>
  </a-list>
</template>
<script>
// components
import draggable from 'vuedraggable'
import MenuForm from './MenuForm'

// apis
import apiClient from '@/utils/api-client'
import { deepClone } from '@/utils/util'

export default {
  name: 'MenuTreeNode',
  components: {
    draggable,
    MenuForm
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
    },
    excludedTeams: {
      required: false,
      type: Array,
      default: null
    }
  },
  data() {
    return {
      isDragging: false
    }
  },
  computed: {
    dragOptions() {
      return {
        animation: 300,
        group: 'description',
        ghostClass: 'ghost',
        chosenClass: 'chosen',
        dragClass: 'drag',
        emptyInsertThreshold: 20
      }
    },
    realValue() {
      return this.value ? this.value : this.list
    }
  },
  methods: {
    emitter(value) {
      this.$emit('input', value)
    },
    handleDelete(id) {
      const _this = this
      _this.$confirm({
        title: '提示',
        content: '确定要删除当前菜单？',
        onOk() {
          apiClient.menu.delete(id).finally(() => {
            _this.onReloadEmit()
          })
        }
      })
    },
    handleOpenEditForm(item) {
      this.$set(item, 'formVisible', true)
    },
    handleUpdateMenuSucceed(item) {
      this.handleCloseCreateMenuForm(item)
      // this.$emit('reload')
    },
    handleCloseCreateMenuForm(item) {
      this.$set(item, 'formVisible', false)
    },
    async handleMoveMenu(item, team) {
      const menu = deepClone(item)
      menu.team = team
      menu.parentId = 0
      menu.priority = 0

      const toFlatList = data => {
        if (!data || data.length === 0) return []

        return data.reduce((prev, current) => {
          const children = current.children.length > 0 ? toFlatList(current.children) : []
          current.team = team
          return [...prev, current, ...children]
        }, [])
      }

      const flatList = [menu, ...toFlatList(menu.children)]

      this.$log.debug('menu list as flat list:', flatList)

      try {
        await apiClient.menu.updateInBatch(flatList)
        this.$emit('reload')
      } catch (e) {
        this.$log.error('Fail to update menu in batch', e)
      }
    },
    onReloadEmit() {
      this.$emit('reload')
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

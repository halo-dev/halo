<template>
  <a-list item-layout="horizontal">
    <draggable
      v-bind="dragOptions"
      tag="div"
      class="item-container"
      :list="list"
      :value="value"
      @input="emitter"
      @start="isDragging = true"
      @end="isDragging = false"
      handle=".title"
    >
      <transition-group>
        <div :key="item.id" v-for="item in realValue">
          <a-list-item class="cursor-pointer menu-item">
            <a-list-item-meta>
              <span slot="title" class="inline-block font-bold cursor-move title"
                >{{ item.name }}
                <a-tooltip title="外部链接" v-if="item.target === '_blank'">
                  <a-icon type="link" />
                </a-tooltip>
                {{ item.formVisible ? '（正在编辑）' : '' }}
              </span>
              <span slot="description" class="inline-block">
                <a :href="item.url" target="_blank" class="ant-anchor-link-title"> {{ item.url }} </a>
              </span>
            </a-list-item-meta>
            <template slot="actions">
              <a v-if="!item.formVisible" href="javascript:void(0);" @click="handleOpenEditForm(item)">
                编辑
              </a>
              <a v-else href="javascript:void(0);" @click="handleCloseCreateMenuForm(item)">
                取消编辑
              </a>
            </template>
            <template slot="actions">
              <a href="javascript:void(0);" @click="handleDelete(item.id)">删除</a>
            </template>
            <template slot="actions" v-if="excludedTeams && excludedTeams.length > 0">
              <a-dropdown :trigger="['click']">
                <a class="ant-dropdown-link" @click="e => e.preventDefault()">
                  更多
                  <a-icon type="down" />
                </a>
                <a-menu slot="overlay">
                  <a-sub-menu title="移动到分组">
                    <a-menu-item
                      v-for="(team, index) in excludedTeams"
                      :key="index"
                      @click="handleMoveMenu(item, team)"
                      >{{ team === '' ? '未分组' : team }}</a-menu-item
                    >
                  </a-sub-menu>
                  <a-sub-menu title="复制到分组">
                    <a-menu-item
                      v-for="(team, index) in excludedTeams"
                      :key="index"
                      @click="handleCopyMenu(item, team)"
                      >{{ team === '' ? '未分组' : team }}</a-menu-item
                    >
                  </a-sub-menu>
                </a-menu>
              </a-dropdown>
            </template>
          </a-list-item>
          <MenuForm
            v-if="item.formVisible"
            :menu="item"
            @succeed="handleUpdateMenuSucceed(item)"
            @cancel="handleCloseCreateMenuForm(item)"
          />
          <div class="a-list-nested" style="margin-left: 44px;">
            <MenuTreeNode :list="item.children" :excludedTeams="excludedTeams" @reload="onReloadEmit" />
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
import menuApi from '@/api/menu'
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
          menuApi.delete(id).finally(() => {
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
    handleCopyMenu(item, team) {
      const menu = deepClone(item)
      menu.team = team
      menu.parentId = 0
      menu.priority = 0
      menu.id = null
      menuApi.create(menu).then(() => {
        this.$emit('reload')
      })
    },
    handleMoveMenu(item, team) {
      const menu = deepClone(item)
      menu.team = team
      menu.parentId = 0
      menu.priority = 0
      menuApi.update(menu.id, menu).then(() => {
        this.$emit('reload')
      })
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

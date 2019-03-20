<template>
  <div>
    <a-tabs>
      <a-tab-pane v-for="(v, i) in icons" :tab="v.title" :key="i">
        <ul>
          <li v-for="(icon, key) in v.icons" :key="`${v.title}-${key}`" :class="{ 'active': selectedIcon==icon }">
            <a-icon :type="icon" :style="{ fontSize: '36px' }" @click="handleSelectedIcon(icon)" />
          </li>
        </ul>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
import icons from './icons'

export default {
  name: 'IconSelect',
  data () {
    return {
      selectedIcon: '',
      icons
    }
  },
  methods: {
    handleSelectedIcon (icon) {
      this.selectedIcon = icon
      this.$emit('change', icon)
    }
  }
}
</script>

<style lang="less" scoped>
  ul{
    list-style: none;
    padding: 0;
    overflow-y: scroll;
    height: 250px;
    li{
      display: inline-block;
      padding:5px;
      margin:5px;
      &:hover {
        cursor: pointer;
      }
      &.active{
        box-shadow: 0px 0px 5px 2px #888888;
      }
    }
  }
</style>

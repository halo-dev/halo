<template>
  <fragment>
    <a-tree-node v-for="(item,index) in list" :title="item.name" :key="index">
      <theme-file :list="item.node"></theme-file>
    </a-tree-node>
  </fragment>
  <!-- <ul>
    <li v-for="(item,index) in list " :key="index">
      <p>{{item.name}}</p>
      <theme-file :list="item.node"></theme-file>
    </li>
  </ul> -->
</template>
<script>
import { Fragment } from 'vue-fragment'
export default {
  components: { Fragment },
  name: 'ThemeFile',
  props: {
    list: Array
  }
}
</script>

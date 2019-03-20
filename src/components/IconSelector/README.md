IconSelector
====

> 图标选择组件，常用于为某一个数据设定一个图标时使用
> eg: 设定菜单列表时，为每个菜单设定一个图标

该组件由 [@Saraka](https://github.com/saraka-tsukai) 封装



### 使用方式

```vue
<template>
	<div>
       <icon-selector @change="handleIconChange"/>
    </div>
</template>

<script>
import IconSelector from '@/components/IconSelector'

export default {
  name: 'YourView',
  components: {
    IconSelector
  },
  data () {
    return {
    }
  },
  methods: {
    handleIconChange (icon) {
      console.log('change Icon', icon)
    }
  }
}
</script>
```



### 事件


| 名称   | 说明                       | 类型   | 默认值 |
| ------ | -------------------------- | ------ | ------ |
| change | 当改变了 `icon` 选中项触发 | String | -      |

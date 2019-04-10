# FooterToolbar 底部工具栏

固定在底部的工具栏。



## 何时使用

固定在内容区域的底部，不随滚动条移动，常用于长页面的数据搜集和提交工作。



引用方式：

```javascript
import FooterToolBar from '@/components/FooterToolbar'

export default {
    components: {
        FooterToolBar
    }
}
```



## 代码演示

```html
<footer-tool-bar>
    <a-button type="primary" @click="validate" :loading="loading">提交</a-button>
</footer-tool-bar>
```
或
```html
<footer-tool-bar extra="扩展信息提示">
    <a-button type="primary" @click="validate" :loading="loading">提交</a-button>
</footer-tool-bar>
```


## API

参数 | 说明 | 类型 | 默认值
----|------|-----|------
children (slot) | 工具栏内容，向右对齐 | - | -
extra | 额外信息，向左对齐 | String, Object | -


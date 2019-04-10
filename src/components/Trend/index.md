# Trend 趋势标记

趋势符号，标记上升和下降趋势。通常用绿色代表“好”，红色代表“不好”，股票涨跌场景除外。



引用方式：

```javascript
import Trend from '@/components/Trend'

export default {
    components: {
        Trend
    }
}
```



## 代码演示  [demo](https://pro.loacg.com/test/home)

```html
<trend flag="up">5%</trend>
```
或
```html
<trend flag="up">
    <span slot="term">工资</span>
    5%
</trend>
```
或
```html
<trend flag="up" term="工资">5%</trend>
```


## API

| 参数      | 说明                                      | 类型         | 默认值 |
|----------|------------------------------------------|-------------|-------|
| flag | 上升下降标识：`up|down` | string | - |
| reverseColor | 颜色反转 | Boolean | false |


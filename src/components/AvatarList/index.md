# AvatarList 用户头像列表


一组用户头像，常用在项目/团队成员列表。可通过设置 `size` 属性来指定头像大小。



引用方式：

```javascript
import AvatarList from '@/components/AvatarList'
const AvatarListItem = AvatarList.AvatarItem

export default {
    components: {
        AvatarList,
        AvatarListItem
    }
}
```



## 代码演示  [demo](https://pro.loacg.com/test/home)

```html
<avatar-list size="mini">
    <avatar-list-item tips="Jake" src="https://gw.alipayobjects.com/zos/rmsportal/zOsKZmFRdUtvpqCImOVY.png" />
    <avatar-list-item tips="Andy" src="https://gw.alipayobjects.com/zos/rmsportal/sfjbOqnsXXJgNCjCzDBL.png" />
    <avatar-list-item tips="Niko" src="https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png" />
</avatar-list>
```
或
```html
<avatar-list :max-length="3">
    <avatar-list-item tips="Jake" src="https://gw.alipayobjects.com/zos/rmsportal/zOsKZmFRdUtvpqCImOVY.png" />
    <avatar-list-item tips="Andy" src="https://gw.alipayobjects.com/zos/rmsportal/sfjbOqnsXXJgNCjCzDBL.png" />
    <avatar-list-item tips="Niko" src="https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png" />
    <avatar-list-item tips="Niko" src="https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png" />
    <avatar-list-item tips="Niko" src="https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png" />
    <avatar-list-item tips="Niko" src="https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png" />
    <avatar-list-item tips="Niko" src="https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png" />
</avatar-list>
```



## API

### AvatarList

| 参数               | 说明       | 类型                                 | 默认值       |
| ---------------- | -------- | ---------------------------------- | --------- |
| size             | 头像大小     | `large`、`small` 、`mini`, `default` | `default` |
| maxLength        | 要显示的最大项目 | number                             | -         |
| excessItemsStyle | 多余的项目风格  | CSSProperties                      | -         |

### AvatarList.Item

| 参数   | 说明     | 类型        | 默认值 |
| ---- | ------ | --------- | --- |
| tips | 头像展示文案 | string | -   |
| src  | 头像图片连接 | string    | -   |


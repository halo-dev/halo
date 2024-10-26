# 自定义 FormKit 输入组件

## 原由

目前在 Console 端的所有表单都使用了 FormKit，但 FormKit 内置的 Input 组件并不满足所有的需求，因此需要自定义一些 Input 组件。此外，为了插件和主题能够更加方便的使用系统内的一些数据，所以同样需要自定义一些带数据的选择组件。

## 使用方式

目前已提供以下类型：

- `code`: 代码编辑器
  - 参数
    1. language: 目前支持 `yaml`, `html`, `css`, `javascript`, `json`
    2. height: 编辑器高度，如：`100px`
- `attachment`: 附件选择
  - 参数
    1. accepts：允许上传的文件类型，如：`image/*`
- `repeater`: 定义一个对象集合，可以让使用者可视化的操作集合。
  - 参数
    1. min: 最小数量，默认为 `0`
    2. max: 最大数量，默认为 `Infinity`，即无限制。
    3. addLabel: 添加按钮的文本，默认为 `添加`
    4. addButton: 是否显示添加按钮，默认为 `true`
    5. upControl: 是否显示上移按钮，默认为 `true`
    6. downControl: 是否显示下移按钮，默认为 `true`
    7. insertControl: 是否显示插入按钮，默认为 `true`
    8. removeControl: 是否显示删除按钮，默认为 `true`
- `list`: 动态列表，定义一个数组列表。
  - 参数
    1. itemType: 列表项的数据类型，用于初始化数据类型，可选参数 `string`, `number`, `boolean`, `object`，默认为 `string`
    2. min: 最小数量，默认为 `0`
    3. max: 最大数量，默认为 `Infinity`，即无限制。
    4. addLabel: 添加按钮的文本，默认为 `添加`
    5. addButton: 是否显示添加按钮，默认为 `true`
    6. upControl: 是否显示上移按钮，默认为 `true`
    7. downControl: 是否显示下移按钮，默认为 `true`
    8. insertControl: 是否显示插入按钮，默认为 `true`
    9. removeControl: 是否显示删除按钮，默认为 `true`
- `menuCheckbox`：选择一组菜单
- `menuRadio`：选择一个菜单
- `menuSelect`: 通用菜单选择组件，支持单选、多选、排序
- `menuItemSelect`：选择菜单项
- `postSelect`：选择文章
- `singlePageSelect`：选择自定义页面
- `categorySelect`：选择分类
  - 参数
    1. multiple: 是否多选，默认为 `false`
- `categoryCheckbox`：选择多个分类
- `tagSelect`：选择标签
  - 参数
    1. multiple: 是否多选，默认为 `false`
- `tagCheckbox`：选择多个标签
- `verificationForm`: 远程验证一组数据是否符合某个规则
  - 参数
    1. action: 对目标数据进行验证的接口地址
    2. label: 验证按钮文本
    3. buttonAttrs: 验证按钮的额外属性
- `secret`: 用于选择或者管理密钥（Secret）
  - 参数
    1. requiredKey：用于确认所需密钥的字段名称
- `select`: 自定义的选择器组件，用于在备选项中选择一个或多个选项
  - 参数
    1. `options`：静态数据源。当 `action` 或 `remote` 存在时，此参数无效。
    2. `action`：远程动态数据源的接口地址。
    3. `requestOption`： 动态数据源的请求参数，可以通过此参数来指定如何获取数据，适配不同的接口。当 `action` 存在时，此参数有效。
    4. `remote`：标识当前是否由用户自定义的远程数据源。
    5. `remoteOption`：当 `remote` 为 `true` 时，此配置项必须存在，用于为 Select 组件提供处理搜索及查询键值对的方法。
    6. `remoteOptimize`：是否开启远程数据源优化，默认为 `true`。开启后，将会对远程数据源进行优化，减少请求次数。仅在动态数据源下有效。
    7. `allowCreate`：是否允许创建新选项，默认为 `false`。仅在静态数据源下有效，需要同时开启 `searchable`。
    8. `clearable`：是否允许清空选项，默认为 `false`。
    9. `multiple`：是否多选，默认为 `false`。
    10. `maxCount`：多选时最大可选数量，默认为 `Infinity`。仅在多选时有效。
    11. `sortable`：是否支持拖动排序，默认为 `false`。仅在多选时有效。
    12. `searchable`：是否支持搜索内容，默认为 `false`。
    13. `autoSelect`：当 value 不存在时，是否自动选择第一个选项，默认为 `true`。仅在单选时有效。

在 Vue 单组件中使用：

```vue
<script lang="ts" setup>
const postName = ref("");
</script>

<template>
  <FormKit
    v-model="postName"
    placeholder="请选择文章"
    label="文章"
    type="postSelect"
    validation="required"
  />
</template>
```

在 FormKit Schema 中使用（插件 / 主题设置表单定义）：

```yaml
- $formkit: menuRadio
  name: menus
  label: 底部菜单组
```

### select

select 是一个选择器类型的输入组件，使用者可以从一批待选数据中选择一个或多个选项。它支持单选、多选操作，并且支持静态数据及远程动态数据加载等多种方式。

#### 在 Vue SFC 中以组件形式使用

静态数据源：

```vue
<script lang="ts" setup></script>
<template>
  <FormKit
    type="select"
    label="What country makes the best food?"
    name="countries"
    placeholder="Select a country"
    allow-create
    clearable
    sortable
    multiple
    searchable
    :options="[
      { label: 'China', value: 'China' },
      { label: 'USA', value: 'USA' },
      { label: 'Japan', value: 'Japan' },
      { label: 'Korea', value: 'Korea' },
      { label: 'France', value: 'France' },
      { label: 'Italy', value: 'Italy' },
      { label: 'Germany', value: 'Germany' },
      { label: 'UK', value: 'UK' },
      { label: 'Canada', value: 'Canada' },
      { label: 'Australia', value: 'Australia' },
    ]"
    help="Don’t worry, you can’t get this one wrong."
  />
</template>
```

动态数据源：

```vue
<script lang="ts" setup>
const ANONYMOUSUSER_NAME = "anonymousUser";
const DELETEDUSER_NAME = "ghost";

const handleSelectPostAuthorRemote = {
  search: async ({ keyword, page, size }) => {
    const { data } = await consoleApiClient.user.listUsers({
      page,
      size,
      keyword,
      fieldSelector: [
        `name!=${ANONYMOUSUSER_NAME}`,
        `name!=${DELETEDUSER_NAME}`,
      ],
    });
    return {
      options: data.items.map((item) => ({
        label: item.user.spec.displayName,
        value: item.user.metadata.name,
      })),
      total: data.total,
      page: data.page,
      size: data.size,
    };
  },

  findOptionsByValues: () => {
    return [];
  },
};
</script>
<template>
  <FormKit
    type="select"
    label="The author of the post is?"
    name="post_author"
    placeholder="Select a user"
    searchable
    remote
    :remote-option="handleSelectPostAuthorRemote"
  />
</template>
```

#### 在 FormKit Schema 中使用

静态数据源：

```yaml
- $formkit: select
  name: countries
  label: What country makes the best food?
  sortable: true
  multiple: true
  clearable: true
  placeholder: Select a country
  options:
    - label: China
      value: cn
    - label: France
      value: fr
    - label: Germany
      value: de
    - label: Spain
      value: es
    - label: Italy
      value: ie
    - label: Greece
      value: gr
```

远程动态数据源：

支持远程动态数据源，通过 `action` 和 `requestOption` 参数来指定如何获取数据。

请求的接口将会自动拼接 `page`、`size` 与 `keyword` 参数，其中 `keyword` 为搜索关键词。

```yaml
- $formkit: select
  name: postName
  label: Choose an post
  clearable: true
  action: /apis/api.console.halo.run/v1alpha1/posts
  requestOption:
    method: GET
    pageField: page
    sizeField: size
    totalField: total
    itemsField: items
    labelField: post.spec.title
    valueField: post.metadata.name
    fieldSelectorKey: metadata.name
```

> [!NOTE]
> 当远程数据具有分页时，可能会出现默认选项不在第一页的情况，此时 Select 组件将会发送另一个查询请求，以获取默认选项的数据。此接口会携带如下参数 `fieldSelector: ${requestOption.fieldSelectorKey}=(value1,value2,value3)`。

> 其中，value1, value2, value3 为默认选项的值。返回值与查询一致，通过 `requestOption` 解析。

### list

list 是一个数组类型的输入组件，可以让使用者可视化的操作数组。它支持动态添加、删除、上移、下移、插入数组项等操作。

在 Vue SFC 中以组件形式使用：

```vue
<script lang="ts" setup>
const users = ref([]);
</script>

<template>
  <FormKit
    :min="1"
    :max="3"
    type="list"
    label="Users"
    add-label="Add User"
    item-type="string"
  >
    <template #default="{ index }">
      <FormKit
        type="text"
        :index="index"
        validation="required"
      />
    </template>
  </FormKit>
</template>
```

在 FormKit Schema 中使用：

```yaml
- $formkit: list
  name: users
  label: Users
  addLabel: Add User
  min: 1
  max: 3
  itemType: string
  children:
    - $formkit: text
      index: "$index"
      validation: required
```

> [!NOTE]
> `list` 组件有且只有一个子节点，并且必须为子节点传递 `index` 属性。若想提供多个字段，则建议使用 `group` 组件包裹。

最终得到的数据类似于：

```json
{
  "users": [
    "Jack",
    "John"
  ]
}
```

### Repeater

Repeater 是一个集合类型的输入组件，可以让使用者可视化的操作集合。

在 Vue SFC 中以组件形式使用：

```vue
<script lang="ts" setup>
const users = ref([]);
</script>

<template>
  <FormKit
    v-model="users"
    :min="1"
    :max="3"
    addLabel="Add User"
    type="repeater"
    label="Users"
  >
    <FormKit
      type="text"
      label="Full Name"
      name="full_name"
      validation="required"
    />
    <FormKit
      type="email"
      label="Email"
      name="email"
      validation="required|email"
    />
  </FormKit>
</template>
```

在 FormKit Schema 中使用：

```yaml
- $formkit: repeater
  name: users
  label: Users
  addLabel: Add User
  min: 1
  max: 3
  items:
    - $formkit: text
      name: full_name
      label: Full Name
      validation: required
    - $formkit: email
      name: email
      label: Email
      validation: required|email
```

最终得到的数据类似于：

```json
[
  {
    "full_name": "Jack",
    "email": "jack@example.com"
  },
  {
    "full_name": "John",
    "email": "john@example.com"
  }
]
```

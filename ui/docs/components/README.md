# Console 组件介绍

目前 Console 的组件包含基础组件（`@halo-dev/components`）和 Console 端的业务组件，这两种组件都可以在插件中使用。

## 业务组件

### AnnotationsForm

此组件用于为自定义模型设置 annotations 数据，同时支持自定义 key / value 和自定义表单，表单定义方式可以参考：<https://docs.halo.run/developer-guide/annotations-form>

使用方式：

```vue
<script setup lang="ts">
const formState = ref({
  metadata: {
    annotations: {}
  }
})

const annotationsFormRef = ref();

async function handleSubmit () {
  annotationsFormRef.value?.handleSubmit();

  await nextTick();

  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } =
    annotationsFormRef.value || {};

  // AnnotationsForm 中的表单校验失败时，不提交数据
  if (customFormInvalid || specFormInvalid) {
    return;
  }

  // 合并数据，此对象即可最终设置给模型的 metadata.annotations
  const annotations = {
    ...annotations,
    ...customAnnotations,
  }
}
</script>

<template>
  <AnnotationsForm
    ref="annotationsFormRef"
    :value="formState.metadata.annotations"
    kind="Post"
    group="content.halo.run"
  />
</template>
```

其中，kind 和 group 为必填项，分别表示模型的 kind 和 group。

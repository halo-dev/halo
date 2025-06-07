# 仪表盘扩展点

## 概述

仪表盘扩展点允许插件为 Halo 的控制台仪表盘添加自定义小部件和快速操作项。通过这些扩展点，插件可以：

- 创建自定义的仪表盘小部件来展示特定数据或功能
- 为快速操作小部件添加自定义操作项

## console:dashboard:widgets:create

此扩展点用于创建自定义的仪表盘小部件。

### 定义方式

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import MyCustomWidget from "./components/MyCustomWidget.vue";

export default definePlugin({
  extensionPoints: {
    "console:dashboard:widgets:create": () => {
      return [
        {
          id: "my-custom-widget",
          component: markRaw(MyCustomWidget),
          group: "my-plugin",
          configFormKitSchema: [
            {
              $formkit: "text",
              name: "title",
              label: "标题",
              value: "默认标题",
            },
            {
              $formkit: "number",
              name: "refresh_interval",
              label: "刷新间隔（秒）",
              value: 30,
              min: 10,
            },
          ],
          defaultConfig: {
            title: "我的自定义小部件",
            refresh_interval: 30,
          },
          defaultSize: {
            w: 6,
            h: 8,
            minW: 3,
            minH: 4,
            maxW: 12,
            maxH: 16,
          },
          permissions: ["plugin:my-plugin:view"],
        },
      ];
    },
  },
});
```

### DashboardWidgetDefinition 类型

```ts
export interface DashboardWidgetDefinition {
  id: string;                                    // 小部件唯一标识符
  component: Raw<Component>;                     // 小部件 Vue 组件
  group: string;                                 // 小部件分组，用于在小部件库中分类显示
  configFormKitSchema?:
    | Record<string, unknown>[]
    | (() => Promise<Record<string, unknown>[]>)
    | (() => Record<string, unknown>[]);         // 配置表单 FormKit 定义，支持异步函数
  defaultConfig?: Record<string, unknown>;       // 默认配置
  defaultSize: {                                 // 默认尺寸
    w: number;                                   // 宽度（网格单位），根据不同屏幕尺寸，网格单位不同，可参考：{ lg: 12, md: 12, sm: 6, xs: 4 }
    h: number;                                   // 高度（网格单位）
    minW?: number;                               // 最小宽度
    minH?: number;                               // 最小高度
    maxW?: number;                               // 最大宽度
    maxH?: number;                               // 最大高度
  };
  permissions?: string[];                        // 访问权限
}
```

### 小部件组件开发

```vue
<template>
  <WidgetCard v-bind="$attrs" :body-class="['!p-0']">
    <template #title>
      <div class="inline-flex items-center gap-2">
        <div class="text-base font-medium flex-1">
          {{ config?.title || "默认标题" }}
        </div>
        <IconSettings
          v-if="editMode"
          class="hover:text-gray-600 cursor-pointer"
          @click="showConfigModal = true"
        />
      </div>
    </template>
    
    <!-- 小部件内容 -->
    <div class="p-4">
      <div v-if="previewMode" class="text-center text-gray-500">
        预览模式
      </div>
      <div v-else>
        <!-- 实际小部件内容 -->
        <p>刷新间隔：{{ config?.refresh_interval || 30 }}秒</p>
      </div>
    </div>
  </WidgetCard>
</template>

<script lang="ts" setup>
import { IconSettings } from "@halo-dev/components";
import { ref } from "vue";

const props = defineProps<{
  editMode?: boolean;        // 是否为编辑模式
  previewMode?: boolean;     // 是否为预览模式
  config?: Record<string, unknown>; // 小部件配置
}>();

const emit = defineEmits<{
  // 
  (e: "update:config", config: Record<string, unknown>): void;
}>();

</script>
```

**小部件组件的属性与事件：**

| 属性          | 类型                    | 说明           |
|---------------|-------------------------|--------------|
| `editMode`    | boolean                 | 是否为编辑模式 |
| `previewMode` | boolean                 | 是否为预览模式 |
| `config`      | Record<string, unknown> | 小部件配置     |

| 事件            | 说明               |
|-----------------|------------------|
| `update:config` | 小部件配置更新事件 |

**WidgetCard 组件的属性与插槽：**

| 属性          | 类型                    | 说明           |
|---------------|-------------------------|--------------|
| `title`    | string                 | 小部件标题 |
| `bodyClass` | string[]                 | 小部件内容区域样式     |

| 插槽      | 说明         |
|-----------|------------|
| `title`   | 小部件标题   |
| `default` | 小部件内容   |
| `actions` | 小部件操作项 |

**重要说明：**

- 小部件组件必须使用 `WidgetCard` 作为根组件，此组件已经在全局注册，不需要导入
- 支持 `editMode` 和 `previewMode` 两种模式，当仪表盘处于编辑页面时，`editMode` 为 `true`，在小组件选择列表时，`previewMode` 为 `true`。可以根据这两个属性来控制小部件的显示内容
- `update:config` 事件通常不需要实现，已经在内部实现了打开配置表单的功能，此事件用于自行实现配置表单
- 使用 `markRaw()` 包装组件以避免响应式转换

## console:dashboard:widgets:internal:quick-action:item:create

此扩展点用于为快速操作小部件添加自定义操作项。

### 定义方式

```ts
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import { IconPlug } from "@halo-dev/components";
import { useRouter } from "vue-router";

export default definePlugin({
  extensionPoints: {
    "console:dashboard:widgets:internal:quick-action:item:create": () => {
      return [
        {
          id: "my-plugin-action",
          icon: markRaw(IconPlug),
          title: "我的插件操作",
          action: () => {
            // do something
          },
          permissions: ["plugin:my-plugin:manage"],
        },
      ];
    },
  },
});
```

### 自定义组件操作项

你也可以提供自定义组件而不是标准的操作项：

```ts
import CustomActionItem from "./components/CustomActionItem.vue";

export default definePlugin({
  extensionPoints: {
    "console:dashboard:widgets:internal:quick-action:item:create": () => {
      return [
        {
          id: "custom-action",
          component: markRaw(CustomActionItem),
          permissions: ["plugin:my-plugin:view"],
        },
      ];
    },
  },
});
```

自定义组件：

```vue
<template>
  <div class="group relative cursor-pointer rounded-lg bg-blue-50 p-4 transition-all hover:bg-blue-100">
    <div class="flex items-center gap-3">
      <component :is="item.icon" class="text-blue-600" />
      <div>
        <h3 class="text-sm font-semibold text-blue-900">
          {{ item.title }}
        </h3>
        <p class="text-xs text-blue-700">自定义操作描述</p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardWidgetQuickActionItem } from "@halo-dev/console-shared";

defineProps<{
  item: DashboardWidgetQuickActionItem;
}>();
</script>
```

### DashboardWidgetQuickActionItem 类型

```ts
interface DashboardWidgetQuickActionBaseItem {
  id: string;                    // 操作项唯一标识符
  permissions?: string[];        // 访问权限
}

interface DashboardWidgetQuickActionComponentItem
  extends DashboardWidgetQuickActionBaseItem {
  component: Raw<Component>;     // 自定义组件
  icon?: Raw<Component>;         // 图标（可选）
  title?: string;                // 标题（可选）
  action?: () => void;           // 点击操作（可选）
}

interface DashboardWidgetQuickActionStandardItem
  extends DashboardWidgetQuickActionBaseItem {
  component?: never;             // 不使用自定义组件
  icon: Raw<Component>;          // 图标（必需）
  title: string;                 // 标题（必需）
  action: () => void;            // 点击操作（必需）
}

export type DashboardWidgetQuickActionItem =
  | DashboardWidgetQuickActionComponentItem
  | DashboardWidgetQuickActionStandardItem;
```

## 权限控制

两个扩展点都支持权限控制：

- **小部件权限**：通过 `permissions` 字段控制小部件的显示
- **操作项权限**：通过 `permissions` 字段控制快速操作项的显示

权限检查会自动进行，用户只能看到有权限访问的小部件和操作项。

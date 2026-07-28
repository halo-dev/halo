# 编辑器扩展的 AI 元数据

`@halo-dev/richtext-editor` 允许 Tiptap 的 Node、Mark 和 Extension
声明供 AI 消费的组件说明。元数据只描述最终编辑器中已有组件的用法，不会对组件功能有任何影响。

## 声明新组件

下面的数学公式节点说明了适用场景、属性和生成所需的外部能力：

```ts
import { Node } from "@halo-dev/richtext-editor";

export const MathBlock = Node.create({
  name: "mathBlock",
  group: "block",
  atom: true,

  addAttributes() {
    return {
      formula: {
        default: "",
      },
    };
  },

  parseHTML() {
    return [{ tag: 'div[data-type="math-block"]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return ["div", { ...HTMLAttributes, "data-type": "math-block" }];
  },

  // 添加供 AI 理解能力的 元数据
  addHaloEditorMetadata() {
    return {
      ai: {
        description: "A display mathematical formula.",
        exposure: "available",
        useWhen: ["Presenting a standalone mathematical expression."],
        attributeGuidance: {
          formula: {
            description: "Formula source written in LaTeX.",
            format: "LaTeX",
            examples: ["E = mc^2"],
          },
        },
        generation: {
          mode: "requires-capability",
          requiredCapabilities: ["math-to-html"],
        },
        examples: [
          '<div data-type="math-block" formula="E = mc^2"></div>',
        ],
      },
    };
  },
});
```

Capability 名称是开放字符串。Halo 只把它放入 Manifest，不负责查找或执行对应工具，由最终使用者扩展工具。

## 扩展现有组件

元数据与 Tiptap 的 `.extend()` 继承链一起自动合并。插件只需返回自己的局部补丁，
不需要调用 `this.parent`，也不需要手动合并 Halo 已有说明：

```ts
import { ExtensionCodeBlock } from "@halo-dev/richtext-editor";

export const HighlightedCodeBlock = ExtensionCodeBlock.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      highlightTheme: {
        default: null,
      },
    };
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        attributeGuidance: {
          highlightTheme: {
            description: "Syntax-highlighting theme.",
            allowedValues: ["github-light", "github-dark"],
            omitWhen: ["The editor default theme should be used."],
          },
        },
      },
    };
  },
});
```

最终 Manifest 会同时包含原 code block 的描述和 `highlightTheme`。

## 为全局属性贡献说明

Plain Extension 不会成为 Manifest 组件，但可向明确命名的 Node 或 Mark
贡献元数据。这适合 `addGlobalAttributes()`：

```ts
import { Extension } from "@halo-dev/richtext-editor";

export const Tone = Extension.create({
  name: "tone",

  addGlobalAttributes() {
    return [
      {
        types: ["paragraph", "heading"],
        attributes: {
          tone: {
            default: null,
          },
        },
      },
    ];
  },

  addHaloEditorMetadata() {
    return {
      contributions: [
        {
          targets: [
            { kind: "node", name: "paragraph" },
            { kind: "node", name: "heading" },
          ],
          metadata: {
            ai: {
              attributeGuidance: {
                tone: {
                  description: "Writing tone for this block.",
                  allowedValues: ["neutral", "friendly", "formal"],
                },
              },
            },
          },
        },
      ],
    };
  },
});
```

贡献只应用于最终 schema 中存在的目标。多个贡献冲突时，根据组件定义中的 priority 决定，
较高者优先；priority 相同时，后注册者优先。

## 组件自身的结构说明

组件只声明自己的父级和数量关系，不跨组件声明子节点：

```ts
addHaloEditorMetadata() {
  return {
    ai: {
      description: "An optional caption belonging to a figure.",
    },
    structure: {
      allowedParents: ["figure"],
      minPerParent: 0,
      maxPerParent: 1,
    },
  };
}
```

这表示当前组件只能位于 `figure` 下，在每个 `figure` 中可省略且最多出现一次。

## 禁用与兼容

- `ai: false` 明确建议 AI 不主动使用该组件，但组件的 schema 信息仍会出现在
  Manifest 中供 AI 理解。
- 声明抛错、字段无效、属性或父节点不存在时，生成器保留其他有效数据；
  开发环境给出警告，生产环境不会因元数据阻止编辑器运行。
- 未知字段会被丢弃。请勿把 system prompt、可执行回调或敏感信息放入元数据。

每段文本最多 1,000 个字符；说明数组和 aliases 最多 10 项；
`allowedValues` 和属性示例最多 32 项；组件 HTML 示例最多 3 个且每个不超过
4 KiB；单组件 AI 元数据最多 16 KiB；单个 Manifest 的 AI 元数据最多
128 KiB。

## 生成 Manifest

在 Editor 创建完成后同步生成最终快照：

```ts
import {
  createHaloEditorManifest,
  type HaloEditorManifest,
  type VueEditor,
} from "@halo-dev/richtext-editor";

function editorManifest(editor: VueEditor): HaloEditorManifest {
  return createHaloEditorManifest(editor);
}
```

Manifest 包含编辑器中全部的 Node 和 Mark、规范化元数据、`version: 1`
以及稳定的 `signature`。AI 插件自行决定怎样把它加入模型上下文，以及是否根据
其中的建议进行额外校验。

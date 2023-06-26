# 评论来源显示拓展点

在 Console 中，评论管理列表的评论来源默认仅支持显示来自文章和页面的评论，如果其他插件中的业务模块也使用了评论，那么就可以通过该拓展点来扩展评论来源的显示。

## 定义方式

假设以文章为例：

```ts
import { definePlugin } from "@halo-dev/console-shared";
import type { CommentSubjectRefResult } from "@halo-dev/console-shared";
import type { Extension } from "@halo-dev/api-client";
import type { Post } from "./types";

export default definePlugin({
  components: {},
  extensionPoints: {
    "comment:subject-ref:create": () => {
      return [
        {
          kind: "Post",
          group: "post.halo.run",
          resolve: (subject: Extension): CommentSubjectRefResult => {
            const post = subject as Post;
            return {
              label: "文章",
              title: post.spec.title,
              externalUrl: post.status.permalink,
              route: {
                name: "PostEditor",
                params: {
                  name: post.metadata.name
                }
              },
            };
          },
        },
      ];
    },
  },
});
```

类型定义如下：

```ts
type CommentSubjectRefProvider = {
  kind: string;               // 自定义模型的类型
  group: string;              // 自定义模型的分组
  resolve: (subject: Extension) => CommentSubjectRefResult;
}

interface CommentSubjectRefResult {
  label: string;              // 来源名称（类型）
  title: string;              // 来源标题
  route?: RouteLocationRaw;   // Console 的路由，可以设置为来源的详情或者编辑页面
  externalUrl?: string;       // 访问地址，可以设置为前台资源的访问地址
}
```

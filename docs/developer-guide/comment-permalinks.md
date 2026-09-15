# 评论与回复永久链接

Halo 2.27 为评论和回复提供统一的前台定位协议。Console、通知和评论组件可以使用同一链接定位目标，无需知道它位于列表的第几页。

## 链接格式

```text
/archives/hello-halo#halo-comment=comment-a
/archives/hello-halo#halo-comment=comment-a&reply=reply-b
```

`halo-comment` 是根评论的 `metadata.name`，`reply` 是目标回复的 `metadata.name`。回复引用了另一条回复时，链接仍指向这条新回复，不使用 `spec.quoteReply` 的值。名称按 URL 参数值编码，消费者可以使用 `new URLSearchParams(location.hash.slice(1))` 解码一次。

链接以 `CommentSubject.getSubjectDisplay()` 提供的前台 URL 为基础，保留路径和查询参数，替换原有 fragment。文章和页面直接沿用 `status.permalink`：默认返回相对链接，不额外拼接域名；启用现有 `halo.use-absolute-permalink` 配置后，保留 subject 生成的绝对链接。仅设置站点外部访问地址不会将 API 的 permalink 改为绝对链接。链接与分页、排序、置顶无关；subject URL 变化后，新读取的链接随之变化，旧 URL 的重定向仍由 subject 自身负责。

## 获取 permalink

下列展示对象增加可选的顶层 `permalink`：

|              对象               |          使用场景           |
|-------------------------------|-------------------------|
| `ListedComment`、`ListedReply` | Console 评论和回复列表         |
| `CommentVo`、`ReplyVo`         | 公共 API 和主题查询            |
| `CommentWithReplyVo`          | 带回复的评论列表，评论及嵌入的回复各自携带链接 |

字段在读取时生成，不写入原始 Comment/Reply 扩展资源。subject、扩展点或有效前台 URL 缺失时，原有展示数据仍返回，`permalink` 为 null 或缺省。消费者不要用 API 地址、当前页面或站点首页拼造替代链接；可以隐藏复制链接入口。Console 的现有前台入口仍回退到 subject 前台地址。

## 直接读取目标

```http
GET /apis/api.halo.run/v1alpha1/comments/{name}
GET /apis/api.halo.run/v1alpha1/comments/{name}/reply/{replyName}
```

第二个接口的 operation ID 为 `GetCommentReply`，返回单个 `ReplyVo`。它按名称定位，不依赖回复列表位置，并验证回复属于路径中的根评论。

回复详情要求根评论和回复都对当前访问者可见且未删除。匿名访问要求两者均已审核、非私密；登录用户沿用现有评论所有者、私密评论线程所有者和评论查看权限规则。链接不授予权限。目标缺失、不可见、被删除或归属错误时返回 404；其他服务错误保持错误响应。

回复详情采用公共展示数据脱敏规则，清除私有身份、邮箱和 IP 信息，保留公开展示信息及统计。其 200 和 404 响应均包含：

```http
Cache-Control: no-store, private
Vary: Cookie, Authorization
```

## 评论组件适配

1. 初始加载及地址变化时识别 `halo-comment` 和可选 `reply`。普通页面锚点不进入评论详情；无根评论、空值或无效参数不发起目标查询。
2. 按名称读取根评论，并对比 `spec.subjectRef` 的 group、kind、name 与当前挂载的 subject。版本变化不改变 subject 身份；归属不一致时不展示目标。
3. 若指定回复，使用上述 Core 回复详情接口。直接展示根评论和目标回复，不逐页查找。
4. 组件负责自身内容内的滚动、高亮和焦点定位，包括 Shadow DOM。异步响应不得覆盖后来选择的目标，切换 subject 或地址时应取消或忽略过期请求。
5. 为不可见、已删除和归属错误的目标提供“不可用”状态及返回列表入口。网络或服务失败提供重试，不绕过后端可见性检查。
6. 返回列表时更新 URL；浏览器前进、后退恢复对应详情或列表。通过 History API 修改地址后，组件需同步自身状态。
7. 分享入口直接使用 Core 返回的 `permalink`。没有该字段时保留时间等展示内容，省略复制入口。

此协议占用页面 fragment。独立前端、不同的前台 URL 映射以及 hash router 由集成方自行适配，本次不提供 URL 覆盖配置或第二套定位协议。

## 通知与发布顺序

新评论通知提供可选 `commentUrl`，新回复通知提供可选 `replyUrl`。通知发布器使用现有的外部链接处理器补全域名，保证邮件中的地址可用；它与 API 的相对 permalink 定位同一目标。默认 HTML 和纯文本模板在链接可用时增加精确目标入口；原有 `postUrl`、`pageUrl`、`commentSubjectUrl` 仍表示 subject 地址，可继续用于标题链接和自定义模板。通知订阅身份与发送时机不变，历史已渲染通知和已发送邮件不重写。

先发布 Halo 2.27，再发布适配后的官方评论组件。组件插件最低 Halo 版本提升到 `>=2.27.0`，独立 npm 组件同样要求 Halo 2.27.0 或更高版本。组件改用 Core 回复详情 API 并移除插件内重复接口，不保留旧 Core 的运行时接口回退。组件代码、浏览器详情验收和发布在组件仓库单独完成；旧组件仍可打开 subject 页面，精确详情需要兼容组件支持。

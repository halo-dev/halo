# CHANGELOG

# 1.4.0-beta.1

## Feature

- 支持静态存储重命名和修改文件内容。#819
- 所有附件列表均支持右键复制图片链接。halo-dev/halo-admin#180
- 开发者选项中的实时日志支持自动滚动到最新的日志。
- 在线下载主题支持选择分支和 release。#515 #592 #835

## Change

- 为部分表单添加表单验证。
- 发布文章时采用实际点击发布按钮时的时间。halo-dev/halo-admin#160
- 添加 renderer meta 标签，让部分双核浏览器强制使用新一代内核，而不是 IE 内核导致页面无法正常渲染。halo-dev/halo-admin#207
- 减弱所有动画效果。halo-dev/halo-admin#213
- 移除部分操作的吐司提示，改为直接在按钮上显示操作结果。halo-dev/halo-admin#216
- 优化大量不合理的代码。halo-dev/halo-admin#213 halo-dev/halo-admin#215
- 移除 fastjson 依赖。#871
- 重构主题目录扫描，允许当前没有激活中的主题。#869
- 移除在开发者选项中重启应用的功能。#917
- 移除 Token 不存在时抛出的异常。#962

## Fixed

- 修复取消全局绝对路径导致加密文章无法正确查看的问题。#785 #854
- 修复 token 无法正确失效的问题。halo-dev/halo-admin#129
- 修复附件不存在时调用删除接口抛异常的问题。#951
- 修复 content api 中查询单篇文章或页面时，没有发出浏览量增加事件的问题。#981

# 1.3.2

## Feature

- 主题设置选项支持 switch 类型。#735
- 后台登陆支持二步验证，需要在个人资料中设置。#745
- 云存储支持华为云。#756

## Change

- 优化初始化页面的表单验证。halo-dev/halo-admin#116
- 优化文章发布体验。halo-dev/halo-admin#125
- 优化仪表盘的操作记录列表展示。halo-dev/halo-admin#128
- 升级 UI 组件。halo-dev/halo-admin#128

## Fixed

- 修复评论邮件中页面地址不正确的问题。#749
- 修复初始化页面的数据导入无法正常显示的问题。halo-dev/halo-admin#128

# 1.3.1

# Fixed

- 修复自定义页面设置中的地址预览出现 undefined 的问题。
- 升级 fastjson 版本为 `1.2.67`。

# 1.3.0

## Feature

- 支持设置文章链接风格（默认，日期型，年月型，ID型）。#563
- 支持设置文章后缀（可实现伪静态）。#563
- 新增磁盘缓存方式。#494
- 支持设置全局路径类型（绝对路径，相对路径）。
- 支持主题设置最低兼容版本。#544
- 支持分类目录和标签设置封面图。#574
- 归档页面支持分页。#608
- 支持数据导入导出功能，方便 H2 与 MySQL 之间无缝迁移。需要注意的是，为了防止误操作，仅仅在博客初始化的时候才能导入数据。#687
- 支持主题开发者自定义邮件发送模板。#691
- 支持分类订阅，`/feed/categories/{slugName}.xml` or `/atom/categories/{slugName}.xml`。#595
- 支持在主题中自定义邮件发送模板。#691

## Change

- 修改邮件发送的文案，防止被服务商判定为广告邮件导致封号。#568
- 取消后台更新 admin 的功能。
- admin 的大部分资源使用 jsdelivr cdn 代理。
- 修改初始化数据，新增更多页面类型。#600
- 优化启动日志，高亮访问地址。#634
- 优化上传文件过程中，内存消耗过大的问题。#659 @bestsort
- 部分核心依赖升级。
- 升级 Markdown 解析器版本。#695
- 在初始化页面中，移除 0.x 迁移的功能，替换为数据导入功能。#633
- 将文章设置的密码框放置于高级设置中。

## Fixed

- 修复附件偶发上传失败的问题。#581
- 修复全站备份失败的问题。
- 修复更新主题不触发缓存更新的问题。#553
- 修复后台开发者选项中实时日志顺序有误的问题。#556
- 修复邮件发送的部分问题。#584
- 修复 sm.ms 无法上传的问题。#609
- 修复 RSS 包含某些特殊符号时，无法正常访问的问题。#641
- 修复某些 SQL 语句在 MySQL 中无法正常执行的问题。
- 评论框 XSS 修复。#677
- 修复文章设置中，标签选中后无法触发保存的问题。
- 修复文章数量显示不准确的问题。#705
- 修复在 iOS 浏览器中，菜单闪烁的问题。

## 升级步骤

1. 由于这个版本修改了大量的主题 API，导致无法兼容旧版本主题，所以升级前请确保你正在使用的主题已经适配了 1.3.0。
2. 备份数据：`cp -r ~/.halo ~/.halo.bak`。
3. 重命名（备份）旧运行包：`mv halo-latest.jar halo-latest.jar.bak`。
4. 下载新运行包：`wget https://dl.halo.run/release/halo-1.3.0.jar -O halo-latest.jar`
5. 重启：`service halo restart`。
6. 更新主题。

## 注意事项

1. 请务必确保你正在使用的主题已经适配了 1.3.0 再进行更新。目前在 `https://github.com/halo-dev` 下的主题均已适配 1.3.0。
2. 更新前不要忘了备份数据，不管你是以什么方式部署的，都请备份 `~/.halo`，当然，如果你使用 docker 部署，并修改了映射路径的话，就备份你的映射路径。
3. 如果有使用 CDN 全站加速，请更新完毕后，刷新全站缓存，并清空浏览器缓存。

## 后期计划

我们计划这个版本发布之后，在短期内都不会再进行较大功能的开发，我们将在后面很长一段时间内做好这些事情：

- 基础建设（主题和评论模块）。
- 完善文档（主要为开发文档）。
- 对已有功能进行改进（编辑器等）和 bug 修复。
- 重构部分代码。
- 安全性。
- 运行包体积优化。
- 资源消耗。

所以，未来我们可能会频繁的发布小版本（至少比以前频繁），勿怪。主要是防止一味地更新功能，而没有照顾到其他更应该照顾的地方。如有其他功能的需要，请去 Github 提 issue 以做记录，我们等到 Halo 更加成熟稳定之后，再做打算。

# 1.2.0

## Feature
- 支持自定义后台管理页面的地址，详细操作参见下方注意事项。
- 图库支持分页查询。#361
- 支持博客备份。
- 云存储支持选择 http 协议，防止在输入自定义域名的时候忘记加上 http 协议。
- 编辑文章时，如发生浏览器误关闭/刷新，提示保存文章，不直接关闭或刷新，防止正在编辑的文章丢失。
- 支持查看某一篇文章的评论。
- 新增开发者选项页面，详细操作参见下方注意事项。
- 支持文章/自定义页面设置 meta 信息。
- 支持设置 RSS 输出类型（全文/摘要）。
- 支持批量删除附件。
- 编辑器支持 mermaid 语法，需要注意的是，在博文中显示需要添加对应的 js 插件。
- 文章支持设置自定义模板。
- 日志支持 Markdown 渲染。
- 云存储支持设置上传目录。
- 自定义页面支持设置摘要。

## Change
- 移除 CDN 加速的设置选项。

## Fixed
- 修复文章路径包含特殊字符时，访问文章 404 的问题。
- 修复文章路径包含中文时，预览地址不正确的问题。
- 修复上传 ico 后缀文件错误的问题。
- 修复附件字段 `media_type` 字符长度过短导致的问题。#356
- 修复文章过长保存失败的问题。#373
- 修复操作日志未保存 ip 地址的问题。
- 修复 TOC 不支持 h4 标题以上的问题。@xebcxc
- 修复大量隐性问题。

## 其他

### 升级注意
1. 如果你之前更新过 `1.2.0-beta.x`，更新到此版本需要先去数据库清空 `flyway_schema_history` 表，然后再进行升级操作。
2. 更新完毕后请在关于 Halo 页面查看版本号，如 Admin 版本不是最新，请手动点击右上角更新 Admin。
3. 如果有使用 CDN 全站加速，请更新完毕后，刷新全站缓存，并清空浏览器缓存。
4. 如果 Github 中的安装包下载太慢，请到 <https://dl.halo.run> 下载。
5. 此次更新修改了附件设置的内容，你可能还需要按照表单重新设置一下。
6. 更新教程：<https://halo.run/guide/install/install-with-linux.html#%E6%9B%B4%E6%96%B0-halo>

### 如何自定义后台管理地址

第一步，使用编辑器（vim）打开 `~/.halo/application.yaml`

第二步，添加如下代码到根节点

```yaml
halo:
  adminPath: <-SUB_PATH->
```

`<-SUB_PATH->` 为你想要修改的子路径（默认为 `admin`），仅支持一级，前后无 `/`。

如：

```yaml
halo:
  adminPath: manage
```

那么这时候你的后台地址为：`博客地址/manage`。

反例：

```yaml
halo:
  adminPath: /manage # 不支持
```

```yaml
halo:
  adminPath: manage/admin # 不支持
```

### 如何开启开发者选项

> 注意，开发者选项包含大量危险性操作，假设你不清楚其中的一些东西，请不要随意修改，否则后果自负。在修改前，建议备份数据。

开启教程：

第一步，登录到后台。

第二步，连续点击左上角 `Halo Dashboard` 十次，即可开启开发者选项。

关闭教程：

第一步，进入 `系统 -> 小工具 -> 开发者选项`。

第二步，点击 `设置`，关闭开发者选项的按钮并保存。

# 1.1.1
## New features
- 支持友情链接排序。@mrdong916

## Fixed
- 修复安全漏洞。#311

# 1.1.0
## New features
- 编辑器支持图片上传功能，包括截图粘贴上传（可以把刀拿开了么？🌚）。 @guqing 
- 文章编辑支持自定义发布时间（刀拿开了么？🌚）。@guqing 
- 支持选择分类或者页面添加到菜单。
- 新增百度云 BOS 云存储。@secondarycoder
- 新增腾讯云 COS 云存储。@secondarycoder
- 适配 SM.MS API V2（如需使用，请到 SM.MS 注册账号获取 Secret Token）。
- 主题支持选择文件更新。
- 主题设置选项支持颜色选择器。
- 主题设置选项支持附件选择器。
- 支持自定义评论插件地址。
- 支持在关于 Halo 页面检测是否有更新。
- 支持所有输入框的验证，以减少大量系统的逻辑异常日志。
- 支持菜单分组，即支持多菜单。
- 文章设置支持设置首页文章排序规则。
- 支持设置文章置顶。
- 主题设置支持 预览模式，实现边设置边预览（仅对当前激活的主题有效）。
- 编辑主题支持选择主题进行编辑。
- 后台新增首屏加载动画。
- 后台布局补充更多选项。
- 支持在后台登录页重置密码，呼出方式：快捷键 SHIFT+ALT+H。#208
- 文章和页面编辑支持预览。
- 支持设置 CDN 加速地址，以加速博客静态资源的访问。
- 支持文章加密，即私密文章。

## Changes
- 文章列表改为从创建时间排序。
- 从附件库选择或复制附件链接进行编码处理，否则可能会导致附件访问不到的情况。
- 编辑器从 [mavonEditor](https://github.com/hinesboy/mavonEditor) 改为 [halo-editor](https://github.com/halo-dev/halo-editor)，基于 mavonEditor 开发，非常感谢 @hinesboy 做出的贡献。
- 抽离 PostSetting 组件，解决打开掉帧的问题。
- 废弃自动保存文章的功能，由于该功能导致了大量的性能消耗，且可能会导致很多错误，所以暂时废弃。代替方案为：支持 Ctrl+S(windows or linux)，Command+S(macOS)快捷键保存为草稿，以防止正在编辑的文章被丢失。
- 后台发表文章之后跳转到文章列表。
- 重构上传组件，支持设定同时上传数与同时并行上传数，减少出错率。
- 重构附件上传生成缩略图的代码，修复内存占用导致的异常。@JohnNiang 
- 优化后台部分 UI。

## Fixed
- 修复文章选择缩略图显示异常的问题（没有处理路径转码）。
- 修复第一次安装报【该博客还没初始化】的错误提示。
- 修复文章选择缩略图显示异常的问题。
- 修复多级菜单删除父菜单，会导致子菜单无法显示的问题。
- 修复删除又拍云附件失败的问题。@Darkcolth
- 修复迁移服务器之后不会恢复默认主题的问题。@jinqilin721
- 修复文章路径中包含 & 字符会导致站点地图出现错误。 #264 @JohnNiang 
- 修复编辑主题模板的报错。
- 修复文章路径带英文逗号不能访问的问题。#280
- 修复文章标签和分类无法修改的问题。#279

## 升级注意
- 因为支持了更好的设置选项组件，请在更新 Halo 之后，同时更新主题。
- 更新完毕后请在关于 Halo 页面查看版本号，是否都为 1.1.0，如 Admin 版本有异常，请手动点击右上角更新 Admin。
- 如果有使用 CDN 全站加速，请更新完毕后，刷新全站缓存。

# 1.1.0-beta.3
## New features
- 后台新增首屏加载动画。
- 后台布局补充更多选项。
- 支持在后台登录页重置密码，呼出方式：SHIFT+ALT+H。#208
- 文章和页面编辑支持预览。
- 支持设置 CDN 加速地址，以加速博客静态资源的访问。
- 支持文章加密，即私密文章。

## Changes
- 后台文章发表之后跳转到文章列表。

## Fixed
- 修复日志管理的部分问题。
- 修复文章路径带英文逗号不能访问的问题。#280
- 修复文章标签和分类无法修改的问题。#279
- 修复 Docker 的构建问题。

# 1.1.0-beta.2
## New features
- 文章设置支持设置首页文章排序规则。
- 支持设置文章置顶。
- 主题设置支持 预览模式，实现边设置边预览（仅对当前激活的主题有效）。
- 编辑主题支持选择主题进行编辑。
## Changes
- 移除无用代码和依赖，优化打包体积。
- 优化文章列表的评论数和点击量的样式。
## Fixed
- 修复编辑主题模板的报错。

# 1.1.0-beta.1
## New features
- 编辑器支持图片上传功能，包括截图粘贴上传（可以把刀拿开了么？🌚）。 @guqing 
- 文章编辑支持自定义发布时间（刀拿开了么？🌚）。@guqing 
- 支持选择分类或者页面添加到菜单。
- 新增百度云 BOS 云存储。@secondarycoder
- 新增腾讯云 COS 云存储。@secondarycoder
- 适配 SM.MS API V2（如需使用，请到 SM.MS 注册账号获取 Secret Token）。
- 主题支持选择文件更新。
- 主题设置选项支持颜色选择器。
- 主题设置选项支持附件选择器。
- 支持自定义评论插件地址。
- 支持在关于 Halo 页面检测是否有更新。
- 支持所有输入框的验证，以减少大量系统的逻辑异常日志。
- 支持菜单分组，即支持多菜单。

## Changes
- 文章列表改为从创建时间排序。
- 从附件库选择或复制附件链接进行编码处理，否则可能会导致附件访问不到的情况。
- 编辑器从 [mavonEditor](https://github.com/hinesboy/mavonEditor) 改为 [halo-editor](https://github.com/halo-dev/halo-editor)，基于 mavonEditor 开发，非常感谢 @hinesboy 做出的贡献。
- 抽离 PostSetting 组件，解决打开掉帧的问题。
- 废弃自动保存文章的功能，由于该功能导致了大量的性能消耗，且可能会导致很多错误，所以暂时废弃。代替方案为：支持 Ctrl+S(windows or linux)，Command+S(macOS)快捷键保存为草稿，以防止正在编辑的文章被丢失。

## Fixed
- 修复文章选择缩略图显示异常的问题（没有处理路径转码）。
- 修复第一次安装报【该博客还没初始化】的错误提示。
- 修复文章选择缩略图显示异常的问题。
- 修复多级菜单删除父菜单，会导致子菜单无法显示的问题。
- 修复删除又拍云附件失败的问题。@Darkcolth
- 修复迁移服务器之后不会恢复默认主题的问题。@jinqilin721
- 修复文章路径中包含 & 字符会导致站点地图出现错误。 #264 @JohnNiang 

# 1.0.3
## New features
- 首次安装自动设置头像为 [Gravatar](http://cn.gravatar.com) 头像。
- 主题列表默认将已启用的主题放在第一位。
- 关于页面支持复制环境信息。
- 增加附加视频预览功能，并对不支持预览的缩略图做不支持处理。@guqing
- 支持登录页面设置 API 地址。@johnniang
- 支持选择头像的时候使用 Gravatar。
- 支持在文章列表选择文章进行设置。
- 新增文章/页面列表点击标题预览的特性。@guqing
- 完善默认 404/500 页面内容。
- 支持添加页面 head 部分代码。
- 支持映射自定义静态资源，需要手动在 `~/.halo/static` 中添加文件。

## Changes
- 修改拼写错误，Gavatar -> Gravatar
- 将文章默认标题改为时间戳。@johnniang
- 移除站点验证的设置选项（百度，Google，360，必应的站点验证）。
- 更换 Markdown 解析器 [commonmark-java](https://github.com/atlassian/commonmark-java) 为 [flexmark](https://github.com/vsch/flexmark-java)。

## Fixed
- 修复附件链接包含中文时，图片无法正常显示的问题。
- 修复回复页面的评论会失败的问题。
- 修复页面评论列表的标题列无法显示的问题。@johnniang
- 修复编辑文章时，标签无法回显的问题。@guqing
- 修复从旧版本迁移到 v1.x 时分类和标签丢失的问题。
- 修复自动输出摘要导致页面样式错乱的问题。
- 修复 Swagger 文档鉴权失效的问题。@johnniang
- 修复评论模块头像显示不正常的问题，需要同时更新主题。

## 升级注意
- 由于此次更新删除了百度，Google，360，必应的站点验证设置选项，所以更新前请备份你之前的设置，更新之后你可以在博客设置中的其他设置里面，将原先的内容放在自定义 head 中。
- 此次更新修改了 Gravatar 的拼写错误，更新之后请重新在博客设置里面设置默认头像。
- 请在更新 Halo 之后，同时更新主题。
- 更新完毕后请在关于 Halo 页面查看版本号，是否都为 1.0.3，如 Admin 版本有异常，请手动更新 Admin。
- 如果有使用 CDN 全站加速，请更新完毕后，刷新全站缓存。

# 1.0.2
## New features
- 支持打包成 War，部署到外部容器。
- 支持自定义博客的 head 信息。
- 提供在线更新 [halo-admin](https://github.com/halo-dev/halo-admin) 的接口。@johnniang

## Fixed
- 修复 404 请求被重定向至 /404。#186 
- 修复导入 Markdown 文档时，标签判断完没有加break，导致标签会继续走分类的逻辑。@wuzhi1234
- 修复导入 Markdown 文件时，slug 为空时初始化赋值。@wuzhi1234

# 1.0.2-beta.1
- 主要修复在 `Windows` 上无法删除主题的 bug 以及其他小 bug；
- 脱敏日志（密码）；
- 更新 Lombok 插件。

# 1.0.1
## Changes
- 修改 `Content api` 的参数 `api_token` 为 `api_access_key`。

## Fixed
- 修复文章无评论时一直转圈圈的问题。
- 修复使用 MySQL 启动时索引创建失败的问题。

# 1.0.0
## 🎉 Halo v1.0 发布啦。

## Features

1. 拥有使用 Vue 开发的后台管理，体验升级，但是并不需要独立部署，启动 Halo 即可。
2. 拥有 Restful 风格的 Content api，你可以用于开发单页面主题，微信小程序等。
3. 拥有 Restful 风格的 Admin api，你可以用于开发桌面管理客户端，管理 App(已有) 等。
4. 拥有使用 Flutter 开发的管理端 App，支持 Android 和 iOS，随时随地发表你的想法！感谢@雨季不再来。
5. 拥有独立的评论插件，使用 Vue 开发，只需在页面引入构建好的 JS 文件即可，完美地和主题相结合。
6. 支持多主题。另外，还支持在线下载主题以及更新主题。
7. 支持在线修改主题文件内容，无需在本地修改然后上传。
8. 十分友好的主题开发体验，支持自定义配置。(主题开发文档正在开发中)。
9. 功能强大的附件管理，同时支持本地上传，又拍云/七牛云/阿里云等云存储，另外，还支持 SM.MS 图床(非常感谢 SM.MS，请大家善用该服务哦)。
10. 自带友情链接管理，图库管理(给爱摄影的小伙伴们)。
11. 支持自定义页面。
12. 支持 Markdown 文档导入，顺带解析 FrontMatter。
13. 支持日志功能，类似于 QQ 空间的说说，亦或者微博。同时支持微信发布日志（后续计划）。
14. 还有…

# 0.4.4
## Fixed
- 修复导出博客数据的问题。

## Tips
> 此版本为 0.x 最后一个版本，不支持直接升级到 v1.0 版本。如需从 0.x 升级为 v1.0，请参考 https://halo.run/archives/install-migrate-from-044

# 0.4.3
## New features
- 支持备份全站数据，包括设置项，文章，分类，标签，评论等。

## Tips
> 重要版本，请尽快升级。

# 0.4.2
## Changes
- 修改rss/sitemap的渲染方式，新增html形式的sitemap以及atom格式的feed。
- 重构 Service 层的代码。 by @JohnNiang 
- 重构 Api 的代码。 by @JohnNiang 
- 文章缩略图统一改为绝对路径。

## Fixed
- 修复附件删除失败的问题
- 修复图库删除图片导致页面报错的问题。#97
- 修复 Material 主题文章页面链接错误的问题。

# 0.4.1
## New features
- 支持文章加密

## Changes
- 更换 Markdown 编辑器为 [EasyMDE](https://github.com/Ionaru/easy-markdown-editor)，这是 [SimpleMDE](https://github.com/sparksuite/simplemde-markdown-editor/) 的一个分支，在继续维护。

## Fixed
- 修复使用 MySQL 导致时区不正确的问题。
- 修复 Docker 自动构建的镜像，主题无法使用的问题。
- 修复搜索框的 XSS 漏洞。
- 修复编辑器自动保存的问题。
- 修复使用oss时，选择图片路径不正确的问题

# 0.4.0
## New features
- 支持后台管理界面的盒子布局背景自定义。
- 支持文章备份带上元数据，也就是说，在某天你要抛弃 Halo 的时候，你可以导出文章直接放到 Hexo 等博客，并且带有文章分类信息，标签信息，发布时间，更新时间，文章标题等数据。
- Material 主题支持文章搜索，MathJax，以及代码高亮。
- 发布新主题 [Pinghsu](https://github.com/ruibaby/pinghsu-halo)，非常感谢该主题的作者 [chakhsu](https://github.com/chakhsu) 制作出如此优秀的主题。需要注意的是，使用该主题，必须升级到当前版本。

## Changes
- 升级 AdminLTE 的版本。

## Fixed
- 修复若干Bug。

# 0.3.0
## :tada:2019,Happy New Year!

## New features
- Docker 部署支持自定义 `H2Database` 数据库用户名和密码。

## Changes
- 后台管理的 pjax 插件更换为 MoOx Pjax，体验更加顺畅。

## Fixed
- 修复若干Bug。

> 祝大家新年快乐。

# 0.2.2
## Fixed
- 修复后台菜单管理的问题。

# 0.2.1
## Fixed
- 修复后台菜单管理的排序问题。
- 修复安全问题。

# 0.2.0
## New features
- 支持 Markdown 文档(Hexo/Jekyll)导入

## Changes
- 修改 `static` 目录的文件结构。

## Fixed
- 修复多文件上传失败的问题，以及图片选择框翻页之后无法选择的问题。

## 注意：
> 本次更新修改了静态资源路径，更新到该版本需要到数据库执行下面两条 SQL 语句，如果某些静态资源还是无法访问，重启 Halo 即可。

```sql
UPDATE HALO_POST SET POST_THUMBNAIL = replace(POST_THUMBNAIL, '/static/images/thumbnail', '/static/halo-frontend/images/thumbnail')

UPDATE HALO_COMMENT SET COMMENT_CONTENT = replace(COMMENT_CONTENT, '/static/plugins/OwO', '/static/halo-common/OwO')
```

# 0.1.1
## New features
- 新增后台管理切换页面的过渡动画。
- 支持又拍云/七牛云图片上传（感谢[@ywms](https://github.com/ywms)）。
- Markdown 编辑器支持数学公式渲染。
- 支持 Docker Compose 部署，真正意义上的一键部署。自动配置好 Nginx 反向代理，SSL证书。

## Changes
- 仪表盘最新文章/最新评论的时间格式改为几...前，如：1天前，36分钟前。
- Markdown 文章渲染改为由后端渲染，使用的库为 [commonmark-java](https://github.com/atlassian/commonmark-java)。
- 支持文章修改发布时间。
- 取消自动备份功能。

## Fixed
- 修复安装主题之后不关闭弹窗的问题。
- 修复使用 MySQL 时，报时区错误的问题。
- 修复使用 Docker 部署时，时间不正常的问题。
- 修复修改标签/分类目录时，文章信息没有刷新缓存的问题。

# 0.1
## New features
- 支持Docker部署。
- 新增Api Token验证，防止接口被恶意调用。
- 支持自定义页面选择指定模板渲染，模板文件名格式`page_xxx.ftl`。

## Changes
- 后台管理页面代码结构优化。
- 更换数据库连接池为性能更好的[HikariCP](https://github.com/brettwooldridge/HikariCP)，更新的时候需要修改配置文件。

## Fixed
- 解决文章备份失败的问题。

## 注意
因为更换了数据库连接池，所以需要修改配置文件（老版本升级，新部署不需要），否则会启动失败。

```yaml
spring:
  datasource:
-    type: com.alibaba.druid.pool.DruidDataSource
+    type: com.zaxxer.hikari.HikariDataSource
```

# 0.0.9
## New features
- 评论支持换行显示。

## Changes
- 弃用`Apache common Lang3`的相关方法，使用`hutool`代替。
- 后台管理主题列表中的主题名改为仅首字母大写，如`ANATOLE`改为`Anatole`。
- 新增自动备份的开关。
- 移除所有第三方评论系统，不再支持。
- 因为`Bootcdn`不再提供服务，所以更换CDN源，致敬`Bootcdn`。
- 后台样式优化，缩减代码。
- 重写评论模块，支持打字特效。
- 封装JS常用方法，缩减大量代码。
- 废除自动保存文章功能，由编辑器的自动保存替代。
- 更改主题设置页面的代码结构，封装`theme_option_marco`，方便调用。

## Fixed
- 修复网站名称为空时，页面的错误。

# 0.0.8
## New features
- 发布文章的时候，没有缩略图会自动添加一张。
- 支持i18n，并带有英文语言包，后台可自行切换语言。
- 新增各大搜索平台验证代码的入口，需要主题支持。
- 全局美化checkbox和radio。

## Changes
- 后台Favicon更新。
- 更改附件目录为用户目录下的`halo/upload`，需要将原来的附件目录`upload`移动到用户目录下的`halo`文件夹。
- 更换编辑器，由editor.md更换为simplemde，支持图片拖动上传。
- 下载的主题不需要再更改为指定文件夹名才能上传。

## Fixed
- 修复评论框在某些主题下样式错乱的问题。
- 修复编辑文章的时候，分类目录不回显的问题。
- 修复Material主题第一次使用样式混乱的问题。

## 注意
因为支持了i18n，所以更新的时候需要修改`application.yaml`配置文件。
```yaml
server:
  port: 8090
  use-forward-headers: true
  undertow:
    io-threads: 2
    worker-threads: 36
    buffer-size: 1024
    directBuffers: true
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource

    # H2database 配置
    driver-class-name: org.h2.Driver
    url: jdbc:h2:file:~/halo/halo
    username: admin
    password: 123456

    #MySql配置
#    driver-class-name: com.mysql.jdbc.Driver
#    url: jdbc:mysql://127.0.0.1:3306/halodb?characterEncoding=utf8&useSSL=false
#    username: root
#    password: 123456

  h2:
    console:
      settings:
        web-allow-others: true
      path: /h2-console
      enabled: true
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  freemarker:
    allow-request-override: false
    cache: false
    check-template-location: true
    charset: utf-8
    content-type: text/html
    expose-request-attributes: false
    expose-session-attributes: false
    expose-spring-macro-helpers: true
    suffix: .ftl
    settings:
      auto_import: /spring.ftl as spring
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

  # 多语言资源文件路径
  messages:
    basename: i18n/messages
logging:
  file: ./logs/log.log
```
如上代码，修改的地方有：
1. expose-spring-macro-helpers: false（原），现为true。
2. 在`freemarker`添加了`settings:auto_import: /spring.ftl as spring`（注意按照上面的格式）。
3. 在`spring`节点添加了`messages:basename: i18n/messages`（注意按照上面的格式）。

### 修改方法
1. 将原有的`application.yaml`备份（重命名）。
2. 复制新的`application.yaml`文件到`resources`下。
3. 按照原来的配置文件修改`application.yaml`文件，需要修改端口号，数据库配置等。

# 0.0.7
## New features
- 新增可选是否需要审核评论的选项。
- 新增一键脚本部署方式。
- 新增NexT主题和Story主题。
- 新增Anatole主题的博客标题可设置大小写的选项。
- 新增使用文章标签作为文章页面关键字的特性。
- 新增配置Favicon的选项，不需要单个主题进行配置。
- 新增评论分页特性。
- 新增主题在线安装和在线更新的功能（需要安装Git）。

## Changes
- 使用枚举方式替换大量重复字符串。
- 更改Anatole社交账号的填写方式，现在只需要填写相关账号。
- 暂时下线文章自动保存的功能，但是可以使用Ctrl+S进行保存。

## Fixed
- 修复文章页面会出现横向滚动条的问题。
- 修复自定义页面不显示评论条数的问题。
- 修复后台评论管理样式混乱的问题。
- 修复Markdown编辑器全屏样式混乱的问题。
- 修复Material主题不可以显示自定义缩略图的问题。
- 修复保存文章失败的问题。
- 修复删除主题再上传相同主题时，会出现又删除上传的主题的问题。

# 0.0.6
## New features
- 使用Ehcache缓存。
- Anatole可在主题设置中添加自定义css。
- 新增Anatole主题的表格样式。
- 单个文章可设置是否可以评论。
- 新增评论的时候可以选择表情。

## Changes
- 删除Editor.md插件中一些无用的资源。
- 规范后台界面的部分命名。
- 压缩后台的代码。
- 删除Anatole主题中无用的js文件。
- 优化后台操作体验。
- 修改评论的展示方式，改变为盖楼（嵌套）的方式。

## Fixed
- 修复文章状态不为发布的时候也可以通过链接访问的问题。
- 修复使用MySQL初始化博客失败的问题。
- 修复前台标签下可现实草稿文章的问题。
- 修复附件的大小和尺寸显示不正确的问题。

# 0.0.5
## New features
- 使用[Hutool](https://github.com/looly/hutool)的encode方法来防止xss攻击。
- 新增备份功能，支持备份resources目录，数据库，以及导出文章。并且可以提供下载和发送到邮箱。
- 新增自动备份功能，每天凌晨一点会自动备份一次，超过10个备份将删除之前的备份并新建一个备份。
- 新增评论之后保存评论者信息。
- Anatole主题支持设置Google浏览器状态栏颜色。
- 新增API接口，可能会考虑做小程序或者单页面应用，提供一个可能。
- 支持评论框显示头像，自动根据邮箱显示Gravatar头像。
- 后台登录支持保存登录名。

## Changes
- 优化后台登录逻辑，登录失败超过5次，将10分钟不能登录。
- 后台管理页面支持高亮菜单。
- 压缩了Anatole主题的资源文件。
- 修改上传附件时候的压缩方式，这种方式更加完美，平均压缩之后只有几k到十几k。

## Fixed
- 修复后台favicon获取不到的问题，会导致每刷新一次就获取一次，拖慢速度。
- 修复后台登录的xss漏洞。
- 修复上传主题之后会产生`__MACOSX`目录的问题。
- 修复附件的大小和尺寸显示不正确的问题。

# 0.0.4
更新汇总:

1. 修复第一次安装完成启动后首页报错的bug
2. 新增文章访问统计
3. 根据文章标题自动填充固定链接
4. 修复附件重名的问题
5. 添加发表文章项面直接访问附件页面的入口
6. 更新AdminLET为最新版本( 2.4.3 )
7. 更改评论的逻辑,需要通过审核之后才会显示在文章
8. 新增编辑文章时自动保存为草稿,防止丢失正在编辑的文章
9. 新增自动构建脚本
10. 更换web容器为undertow

# 0.0.3
完整包：[halo-0.0.3.zip](https://pan.baidu.com/s/1kqNKwSlveqC4gS6TIF5mGQ)
更新包：[halo-0.0.3-update.zip](https://pan.baidu.com/s/1S6CNlFaZ5hvNEqQ2a50mNg)

# 0.0.2
完整包：[halo-0.0.2.zip](http://io.ryanc.cc/index.php?share/file&user=1&sid=ecShv8rB)
更新包：[halo-0.0.2-update.zip](http://io.ryanc.cc/index.php?share/file&user=1&sid=a2zx3v6I)

> 注意：为了防止配置文件被覆盖，更新包里面的配置文件被改成了application.template.yaml。如果更新之后只能进入安装页面的话，请手动在数据表`HALO_OPTIONS`里面添加`is_install`字段，值为`true`。

# 0.0.1
[网盘下载地址](http://io.ryanc.cc/index.php?share/file&user=1&sid=YUHisTCV)
注：第一次发布版本，所以把依赖jar包也压缩进去了，以后发布的版本会有两个，一个是完整版（带有依赖），一个是增量版（不带有依赖，直接上传覆盖就可以更新）。
安装教程请看：[https://halo-doc.ryanc.cc](https://halo-doc.ryanc.cc)和[https://ryanc.cc](https://ryanc.cc)
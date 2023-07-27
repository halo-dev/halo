# 备份和恢复 Proposal

## Motivation

目前，Halo 2.x 支持多种数据库：H2、MySQL、MariaDB、Microsoft SQL Server、Oracle 和
PostgreSQL，虽然数据库有备份和恢复的功能，但是仍然缺少应用级别的备份和恢复功能。Halo
的数据不仅限于数据库中的数据，还包含工作目录下的数据，例如主题、插件和日志等。

## Goals

- 全站备份，包括数据库中的数据和工作目录的数据。
- 全站恢复，包括恢复数据库中的数据和工作目录的数据。
- 用户可控制备份文件存储的时间。
- 对于工作目录的数据，用户可选择性备份和恢复。
- 用户可指定备份权限到任意用户。

## Non-Goals

- 仅备份部分自定义资源。
- 仅备份和恢复文章 Markdown。
- 定时备份。
- 加密备份文件。
- 备份文件自动上传至对象存储。

## Use Cases

- 从某种数据库（例如：H2）迁移至另外的数据库（例如：MySQL），不会因为 SQL 的兼容性而影响迁移。
- 定时完整备份 Halo，并存储至对象存储，一旦发生意外可随时恢复。

## Requirements

- 仅支持 2.8.x 及以上的 Halo。
- 恢复的数据的 creationTimestamp 可能会被当前时间覆盖。

## Draft

恢复数据之前需要完整备份当前 Halo，以便恢复过程中发生错误导致无法回滚。

备份文件将存储在 `${halo.work-dir}/backups/halo-full-backup-2023.07.03-17:52:59.zip`。

备份整站可能需要大量的时间，所以我们需要创建自定义模型（Backup）用于保存用户创建备份的请求，并异步执行备份操作，最终将结果反馈至自定义模型数据中。

Backup 模型样例如下：

- 备份成功样例

```yaml
apiVersion: migration.halo.run/v1alpha1
kind: Backup
metadata:
  name: halo-full-backup-xyz
  creationTimestamp: 2023.07.04-10:25:30
spec:
  format: zip
  autoDeleteWhen: 2023.07.10-00:00:00Z
status:
  phase: Succeeded
  startTimestamp: 2023.07.04-10:25:31
  completionTimestamp: 2023.07.04-10:26:30
  filename: halo-full-backup-2023-07-04-10-25-30.zip
  size: 1024 # data unit: bytes
```

- 备份失败样例

```yaml
apiVersion: migration.halo.run/v1alpha1
kind: Backup
metadata:
  name: halo-full-backup-xyz
  creationTimestamp: 2023.07.04-10:25:30
spec:
  compressionFormat: zip | 7z | tar | tar.gz # 压缩格式
status:
  startTimestamp: 2023.07.04-10:25:31
  # Pending: 刚刚创建好 Backup 资源，等待 Reconciler reconcile。
  # Running: Reconciler 正在备份 Halo。
  # Succeeded: Reconciler 成功执行备份 Halo 操作。
  # Failed: 备份 Halo 失败。
  phase: Failed
  failureReason: DatabaseConnectionReset | UnsupportedCompression # 机器可识别的信息
  failureMessage: The database connection reset. # 人类可阅读的信息
```

同时，BackupReconciler 将负责备份操作，并更新 Backup 数据。

请求示例如下：

```text
POST /apis/migration.halo.run/v1alpha1/backups
Content-Type: application/json
```

### 备份

准备好所有的备份内容后，需要计算摘要并保存，以便后期恢复校验备份文件完整性使用。

#### 数据库备份和恢复

因为 Halo 的 [Extension 设计](https://github.com/halo-dev/rfcs/tree/main/extension)，所以 Halo 的在数据库中的数据备份相对比较简单，只需要简单备份
ExtensionStore 即可。恢复同理。

#### 工作目录备份和恢复

Halo 工作目录样例如下所示：

```text
├── application.yaml
├── attachments
│   └── upload
│       └── image_2023-06-09_16-24-41.png
├── db
│   └── halo-next.mv.db
├── indices
│   └── posts
│       ├── _a.cfe
│       ├── _a.cfs
│       ├── _a.si
│       ├── segments_h
│       └── write.lock
├── keys
│   ├── id_rsa
│   └── id_rsa.pub
├── logs
│   ├── halo.log
│   ├── halo.log.2023-06-01.0.gz
│   ├── halo.log.2023-06-02.0.gz
│   ├── halo.log.2023-06-05.0.gz
│   └── halo.log.2023-06-26.0.gz
├── plugins
│   ├── PluginCommentWidget-1.5.0.jar
│   ├── PluginFeed-1.1.1.jar
│   ├── PluginSearchWidget-1.0.0.jar
│   ├── PluginSitemap-1.0.2.jar
│   └── configs
└── themes
    ├── theme-earth
    │   ├── README.md
    │   ├── settings.yaml
    │   ├── templates
    │   │   ├── archives.html
    │   │   ├── assets
    │   │   │   ├── dist
    │   │   │   │   ├── main.iife.js
    │   │   │   │   └── style.css
    │   │   │   └── images
    │   │   │       ├── default-avatar.svg
    │   │   │       └── default-background.png
    │   │   ├── author.html
    │   │   ├── category.html
    │   │   ├── error
    │   │   │   └── error.html
    │   │   ├── index.html
    │   │   ├── links.html
    │   │   ├── modules
    │   │   │   ├── category-filter.html
    │   │   │   ├── category-tree.html
    │   │   │   ├── featured-post-card.html
    │   │   │   ├── footer.html
    │   │   │   ├── header.html
    │   │   │   ├── hero.html
    │   │   │   ├── layout.html
    │   │   │   ├── post-card.html
    │   │   │   ├── sidebar.html
    │   │   │   ├── tag-filter.html
    │   │   │   └── widgets
    │   │   │       ├── categories.html
    │   │   │       ├── latest-comments.html
    │   │   │       ├── popular-posts.html
    │   │   │       ├── profile.html
    │   │   │       └── tags.html
    │   │   ├── page.html
    │   │   ├── post.html
    │   │   ├── tag.html
    │   │   └── tags.html
    │   └── theme.yaml
```

备份时需要过滤 `db`、backups` 和 `indices` 目录。

#### 备份文件结构

备份文件主要包含自定义资源（`extensions.data`）和工作目录（`workdir.data`）的数据。

- `extensions.data`

前期可考虑使用 JSON 来存储所有的 ExtensionStore 数据。

- `workdir.data`

对工作目录进行 `ZIP` 压缩。

- config.yaml（备份配置）

主要用于描述 `extensions.data` 和 `workdir.data` 压缩格式，后续可扩展备份与恢复相关的配置。例如：

```yaml
compressions:
  extensions: json | others
  workdir: zip | others
```

前期可不实现该功能。

### 恢复

用户通过上传备份文件的方式进行恢复。当且仅当博客未初始化阶段才能进行恢复操作，否则可能会造成数据不一致。

请求示例如下：

```text
POST /apis/migration.halo.run/v1alpha1/restorations
Content-Type: multipart/form-data; boundary="boundary"

'''
--boundary
Content-Disposition: form-data; name="backupfile"; filename="halo-full-backup.zip"
Content-Type: application/zip
'''
```

恢复步骤如下：

1. 解压缩备份文件。
2. 校验备份文件的完整性。
2. 恢复所有 ExtensionStore。
3. 覆盖当前工作目录。
4. 备份完成。

> 需要注意内存占用问题。

## TBDs

- 数据备份期间可能会存在数据的创建、更新和删除。

我们将忽略这些数据变化。

- 是否支持在初始化博客后恢复数据？

支持。不过可能会覆盖掉已有的数据。

# Halo 后端代码结构分析

## 一、项目整体结构
Halo 后端基于 Spring Boot 开发，项目核心目录结构如下：
src/
└── main/
├── java/run/halo/app/
│ ├── controller/ # 控制器层，处理 HTTP 请求
│ ├── service/ # 业务逻辑层，处理核心业务
│ ├── repository/ # 数据访问层，操作数据库
│ ├── entity/ # 数据实体类
│ └── Application.java # 项目启动类
└── resources/
├── application.yml # 配置文件
└── static/ # 静态资源

## 二、文章相关接口位置
文章模块的核心代码主要在以下路径：
- 控制器层：`src/main/java/run/halo/app/controller/content/PostController.java`
  包含文章的增删改查接口，如 `getPostBySlug`（获取文章详情）、`listPosts`（获取文章列表）
- 业务层：`src/main/java/run/halo/app/service/post/PostService.java`
  包含文章的业务逻辑，如数据校验、权限控制、数据组装
- 数据层：`src/main/java/run/halo/app/repository/PostRepository.java`
  直接操作数据库，执行文章数据的CRUD

## 三、后续计划
1.  定位文章详情接口的返回逻辑，找到文章内容字段的处理位置
2.  为后续AI摘要接口的开发做准备，确认接口返回格式，方便后续注入摘要数据
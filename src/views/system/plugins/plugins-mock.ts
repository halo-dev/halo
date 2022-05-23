import alibabacloud from "@/assets/logo-mock/alibabacloud.svg";
import amazonaws from "@/assets/logo-mock/amazonaws.svg";
import w3c from "@/assets/logo-mock/w3c.svg";
import halo from "@/assets/logo-mock/halo.png";
import wechat from "@/assets/logo-mock/wechat.svg";
import github from "@/assets/logo-mock/github.svg";
import redis from "@/assets/logo-mock/redis.svg";
import elasticsearch from "@/assets/logo-mock/elasticsearch.svg";

export const plugins = [
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "阿里云 OSS",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "attachment",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: alibabacloud,
      pluginClass: "run.halo.plugins.OSS",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-oss",
      shortDescription: "this is a test plugin",
      description: "阿里云 OSS 的上传选项，可同步文件以及上传",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "Amazon S3",
      enabled: false,
      labels: {
        "extensions.halo.run/category": "attachment",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: amazonaws,
      pluginClass: "run.halo.plugins.s3",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-s3",
      shortDescription: "this is a test plugin",
      description: "Amazon S3 对象存储的上传选项，可同步文件以及上传",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "OpenGraph Generator",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "common",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "Ryan Wang",
      logo: w3c,
      pluginClass: "run.halo.plugins.opengraph-gen",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-opengraph-gen",
      shortDescription: "this is a test plugin",
      description:
        "自动为文章页面添加 OpenGraph 标签，并支持配置生成 OpenGraph 图片",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "富文本编辑器",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "editor",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: halo,
      pluginClass: "run.halo.plugins.richtext-editor",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-richtext-editor",
      shortDescription: "this is a test plugin",
      description: "Halo 官方提供的富文本编辑器",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "友情链接",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "page",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: halo,
      pluginClass: "run.halo.plugins.links",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-links",
      shortDescription: "this is a test plugin",
      description: "友情链接管理模块",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "图库",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "page",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: halo,
      pluginClass: "run.halo.plugins.photos",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-photos",
      shortDescription: "this is a test plugin",
      description: "图库管理模块",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "社区",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "page",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: halo,
      pluginClass: "run.halo.plugins.forum",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-forum",
      shortDescription: "this is a test plugin",
      description: "提供一个社区/论坛应有的所有模块",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "微信公众号管理",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "common",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: wechat,
      pluginClass: "run.halo.plugins.wechat-channel",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-wechatchannel",
      shortDescription: "this is a test plugin",
      description: "托管微信公众号的管理，支持同步文章和管理媒体",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "GitHub OAuth",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "auth",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: github,
      pluginClass: "run.halo.plugins.github-oauth",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-github-oauth",
      shortDescription: "this is a test plugin",
      description: "允许用户通过 GitHub 账号登录",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "Redis Cache",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "cache",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: redis,
      pluginClass: "run.halo.plugins.redis-cache",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-redis-cache",
      shortDescription: "this is a test plugin",
      description: "支持使用 Redis 作为缓存",
      license: "GPL V3",
    },
  },
  {
    apiVersion: "v1",
    kind: "Plugin",
    metadata: {
      name: "ElasticSearch",
      enabled: true,
      labels: {
        "extensions.halo.run/category": "search-engine",
      },
    },
    spec: {
      version: "1.0.0",
      requires: ">=2.0.0",
      author: "halo-dev",
      logo: elasticsearch,
      pluginClass: "run.halo.plugins.elasticsearch",
      pluginDependencies: {},
      homepage: "https://github.com/halo-dev/halo-plugin-elasticsearch",
      shortDescription: "this is a test plugin",
      description: "接入 ElasticSearch 作为全文搜索系统",
      license: "GPL V3",
    },
  },
];

name: Bug 反馈
description: 提交 Bug 反馈
labels: [bug]
body:
  - type: markdown
    id: preface
    attributes:
      value: |
        感谢你花时间填写此错误报告！在开始之前，我们非常推荐阅读一遍[《开源最佳实践》](https://github.com/LinuxSuRen/open-source-best-practice)，这会在很大程度上提高我们彼此的效率。
  - type: markdown
    id: environment
    attributes:
      value: "## 环境信息"
  - type: input
    id: version
    validations:
      required: true
    attributes:
      label: "是什么版本出现了此问题？"
  - type: dropdown
    id: database
    validations:
      required: true
    attributes:
      label: "使用的什么数据库？"
      options:
        - H2
        - PostgreSQL
        - MySQL 5.7
        - MySQL 8.x
        - MariaDB
        - Other
  - type: dropdown
    id: deployment-method
    validations:
      required: true
    attributes:
      label: "使用的哪种方式部署？"
      options:
        - Docker
        - Docker Compose
        - Fat Jar
  - type: input
    id: site-url
    attributes:
      label: "在线站点地址"
      description: "如果可以的话，请提供你的站点地址。这可能会帮助我们更好的定位问题。"
      placeholder: "ex. https://halo.run"
    validations:
      required: false
  - type: markdown
    id: details
    attributes:
      value: "## 详细信息"
  - type: textarea
    id: what-happened
    attributes:
      label: "发生了什么？"
      description: "最好还告诉我们，你预计会发生什么。"
    validations:
      required: true
  - type: textarea
    id: logs
    attributes:
      label: "相关日志输出"
      description: "请复制并粘贴任何相关的日志输出。 这将自动格式化为代码，因此无需反引号。"
      render: shell
  - type: textarea
    id: additional-information
    attributes:
      label: "附加信息"
      description: "如果你还有其他需要提供的信息，可以在这里填写（可以提供截图、视频等）。"

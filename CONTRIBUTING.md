# 开源参与指南

欢迎关注并有想法参与 Halo 的开发，以下是关于如何参与到 Halo 项目的指南，仅供参考。

## 发现 Issue

所有的代码尽可能都有依据（Issue），不是凭空产生。

### 寻找一个 Good First Issue

> 这个步骤非常适合首次贡献者。

在 [halo-dev](https://github.com/halo-dev) 组织下，有非常多的仓库。每个仓库下都有可能包含一些“首次贡献者”友好的 Issue，主要是为了给贡献者提供一个友好的体验。 该类 Issue
一般会用 `good-first-issue` 标签标记。标签 `good-first-issue` 表示该 Issue 不需要对 Halo 有深入的理解也能够参与。

请点击：[good-first-issue](https://github.com/issues?q=org%3Ahalo-dev+is%3Aopen+is%3Aissue+label%3A%22good+first+issue%22+no%3Aassignee+)
查看关于 Halo 的 Good First Issue。

### 认领 Issue

若对任何一个 Issue 感兴趣，请尝试在 Issue 进行回复，讨论解决 Issue 的思路。确定后可直接通过 `/assign` 或者 `/assign @GitHub 用户名` 认领这个
Issue。这样可避免两位贡献者在同一个问题上花时间。

## 代码贡献步骤

1. Fork 此仓库

   点击 Halo 仓库主页右上角的 `Fork` 按钮即可。

2. Clone 仓库到本地

   ```bash
   git clone https://github.com/{YOUR_USERNAME}/halo --recursive
   # 或者 git clone git@github.com:{YOUR_USERNAME}/halo.git --recursive 
   ```

3. 添加主仓库

   添加主仓库方便未来同步主仓库最新的 commits 以及创建新的分支。

    ```bash
    git remote add upstream https://github.com/halo-dev/halo.git
    # 或者 git remote add upstream git@github.com:halo-dev/halo.git
    git fetch upstream master
    ```

6. 创建新的开发分支

   我们需要从主仓库的主分支创建一个新的开发分支。

    ```bash
    git checkout upstream/master
    git checkout -b {BRANCH_NAME}
    ```

7. 提交代码

    ```bash
    git add .
    git commit -s -m "Fix a bug king"
    git push origin {BRANCH_NAME}
    ```

8. 合并主分支

   在提交 Pull Request 之前，尽量保证当前分支和主分支的代码尽可能同步，这时需要我们手动操作。示例：

    ```bash
    git fetch upstream/master
    git merge upstream/master
    git push origin {BRANCH_NAME}
    ```

## Pull Request

进入此阶段说明已经完成了代码的编写，测试和自测，并且准备好接受 Code Review。

### 创建 Pull Request

回到自己的仓库页面，选择 `New pull request` 按钮，创建 `Pull request` 到原仓库的 `master` 分支。
然后等待我们 Review 即可，如有 `Change Request`，再本地修改之后再次提交即可。

提交 Pull Request 的注意事项：

- 提交 Pull Request 请充分自测。
- 每个 Pull Request 尽量只解决一个 Issue，特殊情况除外。
- 应尽可能多的添加单元测试，其他测试（集成测试和 E2E 测试）可看情况添加。
- 不论需要解决的 Issue 发生在哪个版本，提交 Pull Request 的时候，请将主仓库的主分支设置为 `master`。例如：即使某个 Bug 于 Halo 1.4.x 被发现，但是提交 Pull Request 仍只针对
  `master` 分支，等待 Pull Request 合并之后，我们会通过 `/cherrypick release-1.4` 或者 `/cherry-pick release-1.4` 指令将此 Pull Request
  的修改应用到 `release-1.4` 和 `release-1.5` 分支上。

### 更新 commits

Code Review 阶段可能需要 Pull Request 作者重新修改代码，请直接在当前分支 commit 并 push 即可，无需关闭并重新提交 Pull Request。示例：

```bash
git add .
git commit -s -m "Refactor some code according code review"
git push origin bug/king
```

同时，若已经进入 Code Review 阶段，请不要强制推送 commits 到当前分支。否则 Reviewers 需要从头开始 Code Review。

### 开发规范

请参考 [https://docs.halo.run/developer-guide/core/code-style](https://docs.halo.run/developer-guide/core/code-style)
，请确保所有代码格式化之后再提交。

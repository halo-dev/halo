> 欢迎你参与 Halo 的开发，下面是参与代码贡献的指南，以供参考。

### 代码贡献步骤

#### 0. 提交 issue

任何新功能或者功能改进建议都先提交 issue 讨论一下再进行开发，bug 修复可以直接提交 pull request。

#### 1. Fork 此仓库

点击右上角的 `fork` 按钮即可。

#### 2. Clone 仓库到本地

```bash
git clone https://github.com/{YOUR_USERNAME}/halo

git submodule init

git submodule update
```

#### 3. 创建新的开发分支

```bash
git checkout -b {BRANCH_NAME}
```

#### 4. 提交代码

```bash
git push origin {BRANCH_NAME}
```

#### 5. 提交 pull request

回到自己的仓库页面，选择 `New pull request` 按钮，创建 `Pull request` 到原仓库的 `master` 分支。

然后等待我们 Review 即可，如有 `Change Request`，再本地修改之后再次提交即可。

#### 6. 更新主仓库代码到自己的仓库

```bash
git remote add upstream git@github.com:halo-dev/halo.git

git pull upstream master

git push
```

### 开发规范

请参考 [https://docs.halo.run/developer-guide/core/code-style](https://docs.halo.run/developer-guide/core/code-style)，请确保所有代码格式化之后再提交。

### Usage of Cherry Pick Script

We can use the cherry pick script to cherry-pick commits in pull request as follows:

```bash
GITHUB_USER={your_github_user} hack/cherry_pick_pull.sh upstream/{target_branch} {pull_request_number}
```

> This script is from <https://github.com/kubernetes/kubernetes/blob/master/hack/cherry_pick_pull.sh>.


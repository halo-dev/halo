\# 服务器部署文档



\## 服务器配置

\- 云服务商：腾讯云轻量应用服务器

\- 系统：Ubuntu 22.04

\- CPU：2核

\- 内存：2GB

\- 硬盘：50GB

\- 公网IP：111.229.210.162



\## 部署步骤



\### 1. 连接服务器

使用腾讯云控制台 OrcaTerm 连接服务器，用户名：ubuntu



\### 2. 确认 Docker 已安装

```bash

docker --version

```



\### 3. 启动 Halo 容器

```bash

sudo docker run -d --name halo -p 8090:8090 -v \~/.halo2:/root/.halo2 halohub/halo:2.20

```



\### 4. 开放防火墙端口

在腾讯云控制台 → 防火墙 → 添加规则：

\- 协议：TCP

\- 端口：8090

\- 来源：0.0.0.0/0



\### 5. 系统初始化

浏览器访问 http://111.229.210.162:8090/system/setup 完成初始化设置



\## 访问地址

\- 博客前台：http://111.229.210.162:8090

\- 管理后台：http://111.229.210.162:8090/console



\## 常用命令

\- 查看容器状态：sudo docker ps

\- 停止 Halo：sudo docker stop halo

\- 启动 Halo：sudo docker start halo

\- 查看日志：sudo docker logs halo


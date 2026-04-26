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


## Nginx 反向代理配置

### 安装 Nginx
```bash
sudo apt update
sudo apt install nginx -y
```

### 配置反向代理
```bash
sudo tee /etc/nginx/sites-available/halo > /dev/null << 'EOF'
server {
    listen 80;
    server_name 111.229.210.162;
    location / {
        proxy_pass http://127.0.0.1:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF
sudo ln -s /etc/nginx/sites-available/halo /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 访问地址（更新）
- 博客前台：http://111.229.210.162
- 管理后台：http://111.229.210.162/console


# 使用 Ubuntu 22.04 作为基础镜像
FROM ubuntu:22.04

# 设置环境变量，以避免在安装软件包时的交互式提示
ENV DEBIAN_FRONTEND=noninteractive

# 更新包列表并安装 OpenJDK 21 和其他必需的工具
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    curl \
    unzip \
    && apt-get clean

# 设置 JAVA_HOME 环境变量
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

# 设置工作目录
WORKDIR /workspace/halo

CMD ["tail","-f","/dev/null"]

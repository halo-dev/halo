#!/bin/bash
APP_NAME=halo-latest.jar

usage() {
    echo "用法: sh halo.sh [start(启动)|stop(停止)|restart(重启)|status(状态)]"
    exit 1
}

is_exist(){
  pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `
  if [ -z "${pid}" ]; then
   return 1
  else
    return 0
  fi
}

start(){
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} 正在运行。 pid=${pid} ."
  else
    nohup java -server -Xms256m -Xmx512m -jar $APP_NAME > /dev/null 2>&1 &
    echo "${APP_NAME}启动成功，请查看日志确保运行正常。"
    fi
}

stop(){
  is_exist
  if [ $? -eq "0" ]; then
    kill -9 $pid
    echo "${pid} 进程已被杀死，程序停止运行"
  else
    echo "${APP_NAME} 没有运行。"
  fi
}

status(){
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} 正在运行。Pid is ${pid}"
  else
    echo "${APP_NAME} 没有运行。"
  fi
}

restart(){
  stop
  start
}

case "$1" in
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac

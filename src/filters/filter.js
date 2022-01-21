import Vue from 'vue'

import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

import { timeAgo } from '@/utils/datetime'

dayjs.locale('zh-cn')

Vue.filter('moment', function (dataStr, pattern = 'YYYY-MM-DD HH:mm') {
  return dayjs(dataStr).format(pattern)
})

Vue.filter('moment_post_date', function (dataStr, pattern = '/YYYY/MM/') {
  return dayjs(dataStr).format(pattern)
})

Vue.filter('moment_post_year', function (dataStr, pattern = '/YYYY/') {
  return dayjs(dataStr).format(pattern)
})

Vue.filter('moment_post_day', function (dataStr, pattern = '/YYYY/MM/DD/') {
  return dayjs(dataStr).format(pattern)
})

Vue.filter('timeAgo', timeAgo)

Vue.filter('fileSizeFormat', function (value) {
  if (!value) {
    return '0 Bytes'
  }
  const unitArr = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const srcsize = parseFloat(value)
  let index = Math.floor(Math.log(srcsize) / Math.log(1024))
  let size = srcsize / Math.pow(1024, index)
  size = size.toFixed(2)
  return size + ' ' + unitArr[index]
})

Vue.filter('dayTime', function (value) {
  const days = Math.floor(value / 86400)
  const hours = Math.floor((value % 86400) / 3600)
  const minutes = Math.floor(((value % 86400) % 3600) / 60)
  const seconds = Math.floor(((value % 86400) % 3600) % 60)
  return days + 'd ' + hours + 'h ' + minutes + 'm ' + seconds + 's'
})

import service from '@/utils/service'

const baseUrl = '/api/admin/actuator'

const actuatorApi = {}

actuatorApi.logfile = () => {
  return service({
    url: `${baseUrl}/logfile`,
    method: 'get'
  })
}

actuatorApi.env = () => {
  return service({
    url: `${baseUrl}/env`,
    method: 'get'
  })
}

actuatorApi.getSystemCpuCount = () => {
  return service({
    url: `${baseUrl}/metrics/system.cpu.count`,
    method: 'get'
  })
}

actuatorApi.getSystemCpuUsage = () => {
  return service({
    url: `${baseUrl}/metrics/system.cpu.usage`,
    method: 'get'
  })
}

actuatorApi.getProcessUptime = () => {
  return service({
    url: `${baseUrl}/metrics/process.uptime`,
    method: 'get'
  })
}

actuatorApi.getProcessStartTime = () => {
  return service({
    url: `${baseUrl}/metrics/process.start.time`,
    method: 'get'
  })
}

actuatorApi.getProcessCpuUsage = () => {
  return service({
    url: `${baseUrl}/metrics/process.cpu.usage`,
    method: 'get'
  })
}

actuatorApi.getJvmMemoryMax = () => {
  return service({
    url: `${baseUrl}/metrics/jvm.memory.max`,
    method: 'get'
  })
}

actuatorApi.getJvmMemoryCommitted = () => {
  return service({
    url: `${baseUrl}/metrics/jvm.memory.committed`,
    method: 'get'
  })
}

actuatorApi.getJvmMemoryUsed = () => {
  return service({
    url: `${baseUrl}/metrics/jvm.memory.used`,
    method: 'get'
  })
}

actuatorApi.getJvmGcPause = () => {
  return service({
    url: `${baseUrl}/metrics/jvm.gc.pause`,
    method: 'get'
  })
}

export default actuatorApi

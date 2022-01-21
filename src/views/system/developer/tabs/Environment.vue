<template>
  <div>
    <a-row :gutter="12">
      <a-col :lg="12" :md="24" :sm="24" :xl="12" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="false" hoverable title="服务器">
          <table class="w-full">
            <tbody class="ant-table-tbody">
              <tr>
                <td>系统</td>
                <td>{{ systemProperties['os.name'].value }} {{ systemProperties['os.version'].value }}</td>
              </tr>
              <tr>
                <td>平台</td>
                <td>{{ systemProperties['os.arch'].value }}</td>
              </tr>
              <tr>
                <td>语言</td>
                <td>{{ systemProperties['user.language'].value }}</td>
              </tr>
              <tr>
                <td>时区</td>
                <td>{{ systemProperties['user.timezone'].value }}</td>
              </tr>
              <tr>
                <td>当前用户</td>
                <td>{{ systemProperties['user.name'].value }}</td>
              </tr>
              <tr>
                <td>用户目录</td>
                <td>{{ systemProperties['user.home'].value }}</td>
              </tr>
            </tbody>
          </table>
        </a-card>
        <a-divider dashed />
      </a-col>
      <a-col :lg="12" :md="24" :sm="24" :xl="12" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="false" hoverable title="使用情况">
          <table class="w-full">
            <tbody class="ant-table-tbody">
              <tr>
                <td>CPU 核心数</td>
                <td>{{ system.cpu.count }} 个</td>
              </tr>
              <tr>
                <td>CPU 使用率</td>
                <td>{{ system.cpu.usage }} %</td>
              </tr>
              <tr>
                <td>JVM 最大可用内存</td>
                <td>{{ jvm.memory.max | fileSizeFormat }}</td>
              </tr>
              <tr>
                <td>JVM 可用内存</td>
                <td>{{ jvm.memory.committed | fileSizeFormat }}</td>
              </tr>
              <tr>
                <td>JVM 已用内存</td>
                <td>{{ jvm.memory.used | fileSizeFormat }}</td>
              </tr>
              <tr>
                <td>GC 次数</td>
                <td>{{ jvm.gc.pause.count }} 次</td>
              </tr>
            </tbody>
          </table>
        </a-card>
        <a-divider dashed />
      </a-col>
      <a-col :lg="24" :md="24" :sm="24" :xl="24" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="false" hoverable title="环境">
          <table class="w-full">
            <tbody class="ant-table-tbody">
              <tr>
                <td>Java 名称</td>
                <td>{{ systemProperties['java.vm.name'].value }}</td>
              </tr>
              <tr>
                <td>Java 版本</td>
                <td>{{ systemProperties['java.version'].value }}</td>
              </tr>
              <tr>
                <td>Java Home</td>
                <td>
                  <ellipsis :length="isMobile() ? 50 : 256" tooltip>
                    {{ systemProperties['java.home'].value }}
                  </ellipsis>
                </td>
              </tr>
            </tbody>
          </table>
        </a-card>
        <a-divider dashed />
      </a-col>
      <a-col :lg="24" :md="24" :sm="24" :xl="24" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="false" hoverable title="应用">
          <table class="w-full">
            <tbody class="ant-table-tbody">
              <tr>
                <td>端口</td>
                <td>{{ propertiesSourcesMap['server.ports']['local.server.port'].value }}</td>
              </tr>
              <tr>
                <td>PID</td>
                <td>{{ systemProperties['PID'].value }}</td>
              </tr>
              <tr>
                <td>启动时间</td>
                <td>{{ system.process.startTime | moment }}</td>
              </tr>
              <tr>
                <td>已启动时间</td>
                <td>{{ system.process.uptime | dayTime }}</td>
              </tr>
              <tr>
                <td>启动目录</td>
                <td>
                  <ellipsis :length="isMobile() ? 50 : 256" tooltip>
                    {{ systemProperties['user.dir'].value }}
                  </ellipsis>
                </td>
              </tr>
              <tr>
                <td>日志目录</td>
                <td>
                  <ellipsis :length="isMobile() ? 50 : 256" tooltip>
                    {{ systemProperties['LOG_FILE'].value }}
                  </ellipsis>
                </td>
              </tr>
            </tbody>
          </table>
        </a-card>
      </a-col>
    </a-row>
    <div style="position: fixed; bottom: 30px; right: 30px">
      <a-button icon="sync" shape="circle" size="large" type="primary" @click="handleRefresh"></a-button>
    </div>
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

export default {
  name: 'Environment',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      propertiesSourcesMap: {},
      systemProperties: [],
      interval: null,
      system: {
        cpu: {
          count: 0,
          usage: 0
        },
        process: {
          cpuUsage: 0,
          uptime: 0,
          startTime: 0
        }
      },
      jvm: {
        memory: {
          max: 0,
          committed: 0,
          used: 0
        },
        gc: {
          pause: {
            count: 0
          }
        }
      }
    }
  },
  created() {
    this.loadEnv()
    this.loadSystemInfo()
    this.loadJvmInfo()
  },
  methods: {
    loadEnv() {
      apiClient.actuator.getEnv().then(response => {
        const propertiesSources = response.propertySources
        propertiesSources.forEach(item => {
          this.propertiesSourcesMap[item.name] = item.properties
        })
        this.systemProperties = this.propertiesSourcesMap['systemProperties']
      })
    },
    loadSystemInfo() {
      apiClient.actuator.getSystemCpuCount().then(response => {
        this.system.cpu.count = response.measurements[0].value
      })
      apiClient.actuator.getSystemCpuUsage().then(response => {
        this.system.cpu.usage = Number(response.measurements[0].value * 100).toFixed(2)
      })
      apiClient.actuator.getProcessUptime().then(response => {
        this.system.process.uptime = response.measurements[0].value
      })
      apiClient.actuator.getProcessStartTime().then(response => {
        this.system.process.startTime = response.measurements[0].value * 1000
      })
      apiClient.actuator.getProcessCpuUsage().then(response => {
        this.system.process.cpuUsage = response.measurements[0].value
      })
    },
    loadJvmInfo() {
      apiClient.actuator.getJvmMemoryMax().then(response => {
        this.jvm.memory.max = response.measurements[0].value
      })
      apiClient.actuator.getJvmMemoryCommitted().then(response => {
        this.jvm.memory.committed = response.measurements[0].value
      })
      apiClient.actuator.getJvmMemoryUsed().then(response => {
        this.jvm.memory.used = response.measurements[0].value
      })
      apiClient.actuator.getJvmGcPause().then(response => {
        this.jvm.gc.pause.count = response.measurements[0].value
      })
    },
    handleRefresh() {
      this.loadSystemInfo()
      this.loadJvmInfo()
    }
  }
}
</script>

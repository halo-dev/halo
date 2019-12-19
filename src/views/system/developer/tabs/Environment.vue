<template>
  <div>
    <a-row :gutter="12">
      <a-col
        :xl="12"
        :lg="12"
        :md="24"
        :sm="24"
        :xs="24"
        :style="{ marginBottom: '12px' }"
      >
        <a-card
          title="服务器"
          :bordered="false"
          hoverable
          :bodyStyle="{ padding: 0 }"
        >
          <table style="width:100%">
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
      <a-col
        :xl="12"
        :lg="12"
        :md="24"
        :sm="24"
        :xs="24"
        :style="{ marginBottom: '12px' }"
      >
        <a-card
          title="使用情况"
          :bordered="false"
          hoverable
          :bodyStyle="{ padding: 0 }"
        >
          <table style="width:100%">
            <tbody class="ant-table-tbody">
              <tr>
                <td>CPU 数量</td>
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
      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
        :style="{ marginBottom: '12px' }"
      >
        <a-card
          title="环境"
          :bordered="false"
          hoverable
          :bodyStyle="{ padding: 0 }"
        >
          <table style="width:100%">
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
                  <ellipsis
                    :length="isMobile() ? 50 : 256"
                    tooltip
                  >
                    {{ systemProperties['java.home'].value }}
                  </ellipsis>
                </td>
              </tr>
            </tbody>
          </table>
        </a-card>
        <a-divider dashed />
      </a-col>
      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
        :style="{ marginBottom: '12px' }"
      >
        <a-card
          title="应用"
          :bordered="false"
          hoverable
          :bodyStyle="{ padding: 0 }"
        >
          <table style="width:100%">
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
                <td>{{ system.process.uptime | dayTime }} </td>
              </tr>
              <tr>
                <td>启动目录</td>
                <td>
                  <ellipsis
                    :length="isMobile() ? 50 : 256"
                    tooltip
                  >
                    {{ systemProperties['user.dir'].value }}
                  </ellipsis>
                </td>
              </tr>
              <tr>
                <td>日志目录</td>
                <td>
                  <ellipsis
                    :length="isMobile() ? 50 : 256"
                    tooltip
                  >
                    {{ systemProperties['LOG_FILE'].value }}
                  </ellipsis>
                </td>
              </tr>
            </tbody>
          </table>
        </a-card>
      </a-col>
    </a-row>
    <div style="position: fixed;bottom: 30px;right: 30px;">
      <a-button
        type="primary"
        shape="circle"
        icon="sync"
        size="large"
        @click="handleRefresh"
      ></a-button>
    </div>
  </div>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import actuatorApi from '@/api/actuator'
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
      actuatorApi.env().then(response => {
        const propertiesSources = response.data.propertySources
        propertiesSources.forEach(item => {
          this.propertiesSourcesMap[item.name] = item.properties
        })
        this.systemProperties = this.propertiesSourcesMap['systemProperties']
      })
    },
    loadSystemInfo() {
      actuatorApi.getSystemCpuCount().then(response => {
        this.system.cpu.count = response.data.measurements[0].value
      })
      actuatorApi.getSystemCpuUsage().then(response => {
        this.system.cpu.usage = Number(response.data.measurements[0].value * 100).toFixed(2)
      })
      actuatorApi.getProcessUptime().then(response => {
        this.system.process.uptime = response.data.measurements[0].value
      })
      actuatorApi.getProcessStartTime().then(response => {
        this.system.process.startTime = response.data.measurements[0].value * 1000
      })
      actuatorApi.getProcessCpuUsage().then(response => {
        this.system.process.cpuUsage = response.data.measurements[0].value
      })
    },
    loadJvmInfo() {
      actuatorApi.getJvmMemoryMax().then(response => {
        this.jvm.memory.max = response.data.measurements[0].value
      })
      actuatorApi.getJvmMemoryCommitted().then(response => {
        this.jvm.memory.committed = response.data.measurements[0].value
      })
      actuatorApi.getJvmMemoryUsed().then(response => {
        this.jvm.memory.used = response.data.measurements[0].value
      })
      actuatorApi.getJvmGcPause().then(response => {
        this.jvm.gc.pause.count = response.data.measurements[0].value
      })
    },
    handleRefresh() {
      this.loadSystemInfo()
      this.loadJvmInfo()
    }
  }
}
</script>

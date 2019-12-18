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
                <td>{{ systemProperties.properties['os.name'].value }} {{ systemProperties.properties['os.version'].value }}</td>
              </tr>
              <tr>
                <td>平台</td>
                <td>{{ systemProperties.properties['os.arch'].value }}</td>
              </tr>
              <tr>
                <td>语言</td>
                <td>{{ systemProperties.properties['user.language'].value }}</td>
              </tr>
              <tr>
                <td>时区</td>
                <td>{{ systemProperties.properties['user.timezone'].value }}</td>
              </tr>
              <tr>
                <td>当前用户</td>
                <td>{{ systemProperties.properties['user.name'].value }}</td>
              </tr>
              <tr>
                <td>用户目录</td>
                <td>{{ systemProperties.properties['user.home'].value }}</td>
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
                <td>{{ systemProperties.properties['java.vm.name'].value }}</td>
              </tr>
              <tr>
                <td>Java 版本</td>
                <td>{{ systemProperties.properties['java.version'].value }}</td>
              </tr>
              <tr>
                <td>Java Home</td>
                <td>
                  <ellipsis
                    :length="isMobile() ? 50 : 256"
                    tooltip
                  >
                    {{ systemProperties.properties['java.home'].value }}
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
                <td>{{ propertiesSourcesMap['server.ports'].properties['local.server.port'].value }}</td>
              </tr>
              <tr>
                <td>PID</td>
                <td>{{ systemProperties.properties['PID'].value }}</td>
              </tr>
              <tr>
                <td>启动模式</td>
                <td>{{ systemProperties.properties['spring.profiles.active'].value }}</td>
              </tr>
              <tr>
                <td>启动时间</td>
                <td>{{ system.process.startTime | moment }}</td>
              </tr>
              <tr>
                <td>已启动时间</td>
                <td>{{ system.process.uptime }} 秒</td>
              </tr>
              <tr>
                <td>启动目录</td>
                <td>
                  <ellipsis
                    :length="isMobile() ? 50 : 256"
                    tooltip
                  >
                    {{ systemProperties.properties['user.dir'].value }}
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
                    {{ systemProperties.properties['LOG_FILE'].value }}
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
import { mapGetters } from 'vuex'
import axios from 'axios'
import actuatorApi from '@/api/actuator'
export default {
  name: 'Environment',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      propertiesSourcesMap: {},
      systemProperties: {},
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
  computed: {
    ...mapGetters(['options'])
  },
  methods: {
    loadEnv() {
      actuatorApi.env().then(response => {
        const propertiesSources = response.data.propertySources
        propertiesSources.forEach(item => {
          this.propertiesSourcesMap[item.name] = item
          this.systemProperties = this.propertiesSourcesMap['systemProperties']
        })
      })
    },
    loadSystemInfo() {
      axios
        .all([
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/system.cpu.count'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/system.cpu.usage'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/process.uptime'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/process.start.time'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/process.cpu.usage')
        ])
        .then(response => {
          this.system.cpu.count = response[0].data.measurements[0].value
          this.system.cpu.usage = Number(response[1].data.measurements[0].value * 100).toFixed(2)
          this.system.process.uptime = response[2].data.measurements[0].value
          this.system.process.startTime = response[3].data.measurements[0].value * 1000
          this.system.process.cpuUsage = response[4].data.measurements[0].value
        })
        .catch(response => {
          this.$message.error('获取服务器系统信息失败！')
        })
    },
    loadJvmInfo() {
      axios
        .all([
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/jvm.memory.max'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/jvm.memory.committed'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/jvm.memory.used'),
          axios.get(this.options.blog_url + '/api/admin/actuator/metrics/jvm.gc.pause')
        ])
        .then(r => {
          this.jvm.memory.max = r[0].data.measurements[0].value
          this.jvm.memory.committed = r[1].data.measurements[0].value
          this.jvm.memory.used = r[2].data.measurements[0].value
          this.jvm.gc.pause.count = r[3].data.measurements[0].value
        })
        .catch(r => {
          console.error(r)
          this.$message.error('获取 JVM 信息失败！')
        })
    },
    handleRefresh() {
      this.loadSystemInfo()
      this.loadJvmInfo()
    }
  }
}
</script>

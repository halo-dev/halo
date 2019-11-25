<template>
  <div>
    <a-row :gutter="12">
      <a-col
        :xl="24"
        :lg="24"
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
                <td>{{ systemProperties.properties['java.home'].value }}</td>
              </tr>
            </tbody>
          </table>
        </a-card>
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
                <td>PID</td>
                <td>{{ systemProperties.properties['PID'].value }}</td>
              </tr>
              <tr>
                <td>启动目录</td>
                <td>{{ systemProperties.properties['user.dir'].value }}</td>
              </tr>
              <tr>
                <td>启动模式</td>
                <td>{{ systemProperties.properties['spring.profiles.active'].value }}</td>
              </tr>
              <tr>
                <td>日志目录</td>
                <td>{{ systemProperties.properties['LOG_FILE'].value }}</td>
              </tr>
            </tbody>
          </table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script>
import actuatorApi from '@/api/actuator'
export default {
  name: 'Environment',
  data() {
    return {
      systemProperties: {}
    }
  },
  created() {
    this.loadEnv()
  },
  methods: {
    loadEnv() {
      actuatorApi.env().then(response => {
        this.systemProperties = response.data.propertySources[2]
      })
    }
  }
}
</script>

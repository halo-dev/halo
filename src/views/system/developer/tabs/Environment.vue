<template>
  <div>
    运行环境 {{ env }}
  </div>
</template>
<script>
import actuatorApi from '@/api/actuator'
export default {
  name: 'Environment',
  data() {
    return {
      env: {}
    }
  },
  created() {
    this.loadEnv()
  },
  methods: {
    loadEnv() {
      actuatorApi.env().then(response => {
        this.env = response.data
      })
    }
  }
}
</script>

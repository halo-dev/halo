<template>
  <div class="logo">
    <a
      href="javascript:void(0);"
      @click="onLogoClick()"
    >
      <h1 class="logo-title">Halo</h1>
      <h1
        class="logo-sub-title"
        style="padding-left: 10px;"
      >Dashboard</h1>
    </a>
  </div>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'
import optionApi from '@/api/option'
export default {
  name: 'Logo',
  data() {
    return {
      clickCount: 0,
      optionsToCreate: {
        developer_mode: true
      }
    }
  },
  computed: {
    ...mapGetters(['options'])
  },
  methods: {
    ...mapActions(['loadOptions']),
    onLogoClick() {
      this.clickCount++
      if (this.clickCount === 10) {
        optionApi.save(this.optionsToCreate).then(response => {
          this.loadOptions()
          this.$message.success(`开发者选项已启用！`)
          this.clickCount = 0
          this.$router.push({ name: 'ToolList' })
        })
      } else if (this.clickCount >= 5) {
        if (this.options.developer_mode) {
          this.$message.info(`当前已启用开发者选项！`)
          this.clickCount = 0
        } else {
          this.$message.info(`再点击 ${10 - this.clickCount} 次即可启用开发者选项！`)
        }
      }
    }
  }
}
</script>

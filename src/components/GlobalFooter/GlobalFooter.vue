<template>
  <div class="footer">
    <div class="copyright">
      Proudly power by 
      <router-link :to="{ name:'About' }">
        <a href="javascript:void(0);">Halo</a>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GlobalFooter',
  data() {
    return {
    }
  }
}
</script>

<style lang="less" scoped>
  .footer {
    padding: 0 16px;
    margin: 48px 0 24px;
    text-align: center;
    .copyright {
      color: rgba(0, 0, 0, .45);
      font-size: 14px;
    }
  }
</style>

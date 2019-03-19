<template>
  <div class="globalFooter">
    <div class="copyright">
      Proudly power by
      <a href="https://github.com/halo-dev/halo" target="_blank">Halo v1.0</a>
    </div>
  </div>
</template>

<script>
export default {
  name: "GlobalFooter",
  data() {
    return {};
  }
};
</script>

<style lang="less" scoped>
.globalFooter {
  margin: 48px 0 24px 0;
  padding: 0 16px;
  text-align: center;
  .copyright {
    color: @text-color-secondary;
    font-size: @font-size-base;

    a {
      color: @text-color-secondary;
      transition: all 0.3s;

      &:not(:last-child) {
        margin-right: 40px;
      }

      &:hover {
        color: @text-color;
      }
    }
  }
}
</style>

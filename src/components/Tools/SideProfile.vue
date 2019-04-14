<template>
  <div class="side-profile">
    <router-link :to="{ name:'Dashboard' }">
      <a-avatar class="avatar" size="large" src="https://gravatar.loli.net/avatar/?s=256&d=mm"/>
    </router-link>
  </div>
</template>

<script>

export default {
  name: 'SideProfile',
  props: {
    title: {
      type: String,
      default: 'Halo Dashboard',
      required: false
    },
    showTitle: {
      type: Boolean,
      default: true,
      required: false
    }
  }
}
</script>

<style scope>
.sider .side-profile {
  left: 0;
  width: 100%;
  height: 72px;
  line-height: 72px;
  padding-left: 24px;
  -webkit-transition: all 0.3s;
  transition: all 0.3s;
  overflow: hidden;
  z-index: 9;
}
</style>

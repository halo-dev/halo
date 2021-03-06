<template>
  <div class="footer text-center" style="padding: 0 16px;margin: 48px 0 0;">
    <div class="copyright" style="color: rgba(0, 0, 0, 0.45);font-size: 14px;">
      Proudly power by
      <router-link :to="{ name: 'About' }">
        Halo
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GlobalFooter'
}
</script>

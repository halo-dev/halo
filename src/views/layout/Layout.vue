<template>
  <div>
    <div class="nav">
      Nav
      <ul>
        <li>
          <router-link :to="{ name: 'dashboard' }">Dashboard</router-link>
        </li>
        <li>
          <router-link :to="{ name: 'posts-list' }">Posts</router-link>
        </li>
      </ul>
    </div>
    <div class="container">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {};
</script>

<style scoped></style>

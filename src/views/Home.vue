<template>
  <div class="home">
    <img
      alt="Vue logo"
      src="../assets/halo.png"
    />
    <HelloWorld msg="Welcome to Halo Admin" />
  </div>
</template>

<script>
// @ is an alias to /src
import HelloWorld from "@/components/HelloWorld.vue";

export default {
  name: "home",
  components: {
    HelloWorld
  }
};
</script>

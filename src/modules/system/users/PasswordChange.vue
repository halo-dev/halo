<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
</script>
<template>
  <FormKit id="password-form" :actions="false" type="form">
    <FormKit label="原密码" type="password"></FormKit>
    <FormKit label="新密码" type="password"></FormKit>
    <FormKit label="确认密码" type="password"></FormKit>
  </FormKit>

  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary">修改密码</VButton>
    </div>
  </div>
</template>

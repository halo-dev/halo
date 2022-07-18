<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import { inject } from "vue";
import type { Ref } from "vue";
import type { User } from "@halo-dev/api-client";

const user = inject<Ref<User>>("user");
</script>
<template>
  <FormKit v-if="user" id="user-form" :actions="false" type="form">
    <FormKit
      v-model="user.metadata.name"
      label="用户名"
      type="text"
      validation="required"
    ></FormKit>
    <FormKit
      v-model="user.spec.displayName"
      label="显示名称"
      type="text"
      validation="required"
    ></FormKit>
    <FormKit
      v-model="user.spec.email"
      label="电子邮箱"
      type="email"
      validation="required"
    ></FormKit>
    <FormKit v-model="user.spec.bio" label="描述" type="textarea"></FormKit>
  </FormKit>

  <div class="pt-5">
    <div class="flex justify-start">
      <VButton type="secondary">保存</VButton>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { VAvatar } from "./index";
import { VSpace } from "../space";
</script>
<template>
  <Story title="Avatar">
    <VSpace direction="column">
      <VSpace>
        <VAvatar size="md" alt="Halo" src="https://halo.run/logo" />
        <VAvatar size="md" alt="Ryan Wang" src="https://ryanc.cc/avatar" />
        <VAvatar size="md" alt="John Niang" src="https://johnniang.me/avatar" />
        <VAvatar size="md" alt="guqing" src="https://guqing.xyz/avatar" />
      </VSpace>
      <VSpace>
        <VAvatar size="md" alt="Halo" />
        <VAvatar size="md" alt="Ryan Wang" />
        <VAvatar size="md" alt="John Niang" />
        <VAvatar size="md" alt="guqing" />
      </VSpace>
      <VSpace>
        <VAvatar size="md" circle alt="Halo" />
        <VAvatar size="md" circle alt="Ryan Wang" />
        <VAvatar size="md" circle alt="John Niang" />
        <VAvatar size="md" circle alt="guqing" />
      </VSpace>
      <VSpace>
        <VAvatar size="xs" circle alt="Halo" src="https://halo.run/logo" />
        <VAvatar
          size="sm"
          circle
          alt="Ryan Wang"
          src="https://ryanc.cc/avatar"
        />
        <VAvatar
          size="md"
          circle
          alt="John Niang"
          src="https://johnniang.me/avatar"
        />
        <VAvatar
          size="lg"
          circle
          alt="guqing"
          src="https://guqing.xyz/avatar"
        />
      </VSpace>
    </VSpace>
  </Story>
</template>

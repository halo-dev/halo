<template>
  <Story title="Button">
    <Variant title="Type">
      <template #default>
        <VSpace>
          <VButton type="primary"> primary </VButton>
          <VButton type="secondary"> secondary </VButton>
          <VButton type="danger"> danger </VButton>
          <VButton type="default"> default </VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Size">
      <template #default>
        <VSpace>
          <VButton type="secondary" size="lg"> Large </VButton>
          <VButton type="secondary"> default </VButton>
          <VButton type="secondary" size="sm"> sm </VButton>
          <VButton type="secondary" size="xs"> xs </VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Circle">
      <template #default>
        <VSpace>
          <VButton type="secondary" size="lg" circle> lg </VButton>
          <VButton type="secondary" circle> d </VButton>
          <VButton type="secondary" size="sm" circle> sm </VButton>
          <VButton type="secondary" size="xs" circle> xs </VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Block">
      <template #default>
        <VSpace direction="column" class="w-full">
          <VButton type="primary" block> primary </VButton>
          <VButton type="secondary" block> secondary </VButton>
          <VButton type="danger" block> danger </VButton>
          <VButton type="default" block> default </VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Disabled" :init-state="disabledState">
      <template #default="{ state }">
        <VSpace>
          <VButton type="primary" :disabled="state.disabled"> primary </VButton>
          <VButton type="secondary" :disabled="state.disabled">
            secondary
          </VButton>
          <VButton type="danger" :disabled="state.disabled"> danger </VButton>
          <VButton type="default" :disabled="state.disabled"> default </VButton>
        </VSpace>
      </template>
      <template #controls="{ state }">
        <HstCheckbox v-model="state.disabled" title="Disabled" />
      </template>
    </Variant>
    <Variant title="Loading" :init-state="loadingState">
      <template #default="{ state }">
        <VSpace>
          <VButton type="primary" :loading="state.loading"> Primary </VButton>
          <VButton type="secondary" :loading="state.loading">
            Secondary
          </VButton>
          <VButton type="danger" :loading="state.loading"> Danger </VButton>
          <VButton type="default" :loading="state.loading"> Default </VButton>
          <VButton size="lg" type="secondary" :loading="state.loading">
            Large
          </VButton>
          <VButton type="secondary" :loading="state.loading">Default</VButton>
          <VButton size="sm" type="secondary" :loading="state.loading">
            sm
          </VButton>
          <VButton size="xs" type="secondary" :loading="state.loading">
            xs
          </VButton>
        </VSpace>
      </template>
      <template #controls="{ state }">
        <HstCheckbox v-model="state.loading" title="Loading" />
      </template>
    </Variant>
    <Variant title="Icon" :init-state="iconState">
      <template #default="{ state }">
        <VSpace>
          <VButton
            size="lg"
            type="primary"
            :loading="state.loading"
            :disabled="state.disabled"
          >
            <template #icon>
              <IconSettings class="w-full h-full" />
            </template>
            Large
          </VButton>
          <VButton
            type="secondary"
            :loading="state.loading"
            :disabled="state.disabled"
          >
            <template #icon>
              <IconSettings class="w-full h-full" />
            </template>
            Default
          </VButton>
          <VButton
            size="sm"
            type="danger"
            :loading="state.loading"
            :disabled="state.disabled"
          >
            <template #icon>
              <IconSettings class="w-full h-full" />
            </template>
            sm
          </VButton>
          <VButton
            size="xs"
            type="default"
            :loading="state.loading"
            :disabled="state.disabled"
          >
            <template #icon>
              <IconSettings class="w-full h-full" />
            </template>
            xs
          </VButton>
        </VSpace>
      </template>
      <template #controls="{ state }">
        <HstCheckbox v-model="state.loading" title="Loading" />
        <HstCheckbox v-model="state.disabled" title="Disabled" />
      </template>
    </Variant>
  </Story>
</template>
<script lang="ts" setup>
import { VButton } from "./index";
import { VSpace } from "../space";
import { IconSettings } from "@/core/icons";

function loadingState() {
  return {
    loading: true,
  };
}

function disabledState() {
  return {
    disabled: true,
  };
}

function iconState() {
  return {
    loading: false,
    disabled: false,
  };
}
</script>

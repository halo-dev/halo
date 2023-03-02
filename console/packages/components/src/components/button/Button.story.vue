<template>
  <Story title="Button">
    <Variant title="Type">
      <template #default>
        <VSpace>
          <VButton type="primary"> primary</VButton>
          <VButton type="secondary"> secondary</VButton>
          <VButton type="danger"> danger</VButton>
          <VButton type="default"> default</VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Size">
      <template #default>
        <VSpace>
          <VButton size="lg" type="secondary"> Large</VButton>
          <VButton type="secondary"> default</VButton>
          <VButton size="sm" type="secondary"> sm</VButton>
          <VButton size="xs" type="secondary"> xs</VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Circle">
      <template #default>
        <VSpace>
          <VButton circle size="lg" type="secondary"> lg</VButton>
          <VButton circle type="secondary"> d</VButton>
          <VButton circle size="sm" type="secondary"> sm</VButton>
          <VButton circle size="xs" type="secondary"> xs</VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Block">
      <template #default>
        <VSpace class="w-full" direction="column">
          <VButton block type="primary"> primary</VButton>
          <VButton block type="secondary"> secondary</VButton>
          <VButton block type="danger"> danger</VButton>
          <VButton block type="default"> default</VButton>
        </VSpace>
      </template>
    </Variant>
    <Variant :init-state="disabledState" title="Disabled">
      <template #default="{ state }">
        <VSpace>
          <VButton :disabled="state.disabled" type="primary"> primary</VButton>
          <VButton :disabled="state.disabled" type="secondary">
            secondary
          </VButton>
          <VButton :disabled="state.disabled" type="danger"> danger</VButton>
          <VButton :disabled="state.disabled" type="default"> default</VButton>
        </VSpace>
      </template>
      <template #controls="{ state }">
        <HstCheckbox v-model="state.disabled" title="Disabled" />
      </template>
    </Variant>
    <Variant :init-state="loadingState" title="Loading">
      <template #default="{ state }">
        <VSpace>
          <VButton :loading="state.loading" type="primary"> Primary</VButton>
          <VButton :loading="state.loading" type="secondary">
            Secondary
          </VButton>
          <VButton :loading="state.loading" type="danger"> Danger</VButton>
          <VButton :loading="state.loading" type="default"> Default</VButton>
          <VButton :loading="state.loading" size="lg" type="secondary">
            Large
          </VButton>
          <VButton :loading="state.loading" type="secondary">Default</VButton>
          <VButton :loading="state.loading" size="sm" type="secondary">
            sm
          </VButton>
          <VButton :loading="state.loading" size="xs" type="secondary">
            xs
          </VButton>
        </VSpace>
      </template>
      <template #controls="{ state }">
        <HstCheckbox v-model="state.loading" title="Loading" />
      </template>
    </Variant>
    <Variant :init-state="iconState" title="Icon">
      <template #default="{ state }">
        <VSpace>
          <VButton
            :disabled="state.disabled"
            :loading="state.loading"
            size="lg"
            type="primary"
          >
            <template #icon>
              <IconSettings class="h-full w-full" />
            </template>
            Large
          </VButton>
          <VButton
            :disabled="state.disabled"
            :loading="state.loading"
            type="secondary"
          >
            <template #icon>
              <IconSettings class="h-full w-full" />
            </template>
            Default
          </VButton>
          <VButton
            :disabled="state.disabled"
            :loading="state.loading"
            size="sm"
            type="danger"
          >
            <template #icon>
              <IconSettings class="h-full w-full" />
            </template>
            sm
          </VButton>
          <VButton
            :disabled="state.disabled"
            :loading="state.loading"
            size="xs"
            type="default"
          >
            <template #icon>
              <IconSettings class="h-full w-full" />
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
import { IconSettings } from "../../icons/icons";

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

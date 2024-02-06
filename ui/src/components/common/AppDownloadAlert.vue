<script lang="ts" setup>
import { VAlert } from "@halo-dev/components";
</script>

<template>
  <VAlert :title="$t('core.common.text.tip')" :closable="false">
    <template #description>
      <p>{{ $t("core.components.app_download_alert.description") }}</p>
      <ul class="mt-1.5 list-inside list-decimal space-y-1">
        <i18n-t
          keypath="core.components.app_download_alert.sources.app_store"
          tag="li"
        >
          <template #url>
            <a
              href="https://halo.run/store/apps"
              target="_blank"
              class="underline-offset-2 hover:text-gray-900 hover:underline"
            >
              https://halo.run/store/apps
            </a>
          </template>
        </i18n-t>
        <i18n-t
          keypath="core.components.app_download_alert.sources.github"
          tag="li"
        >
          <template #url>
            <a
              href="https://github.com/halo-sigs/awesome-halo"
              target="_blank"
              class="underline-offset-2 hover:text-gray-900 hover:underline"
            >
              https://github.com/halo-sigs/awesome-halo
            </a>
          </template>
        </i18n-t>
      </ul>
    </template>
  </VAlert>
</template>

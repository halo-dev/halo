<script lang="ts" setup>
import { VButton, VModal } from "@halo-dev/components";
import type { Plugin } from "@halo-dev/api-client";
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import { ref } from "vue";

withDefaults(defineProps<{ plugin: Plugin }>(), {});

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref();
</script>

<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :title="$t('core.plugin.conditions_modal.title')"
    :width="900"
    layer-closable
    @close="emit('close')"
  >
    <table class="min-w-full divide-y divide-gray-100">
      <thead class="bg-gray-50">
        <tr>
          <th
            class="px-4 py-3 text-left text-sm font-semibold text-gray-900 sm:w-96"
            scope="col"
          >
            {{ $t("core.plugin.conditions_modal.fields.type") }}
          </th>
          <th
            scope="col"
            class="px-4 py-3 text-left text-sm font-semibold text-gray-900"
          >
            {{ $t("core.plugin.conditions_modal.fields.status") }}
          </th>
          <th
            scope="col"
            class="px-4 py-3 text-left text-sm font-semibold text-gray-900"
          >
            {{ $t("core.plugin.conditions_modal.fields.reason") }}
          </th>
          <th
            scope="col"
            class="px-4 py-3 text-left text-sm font-semibold text-gray-900"
          >
            {{ $t("core.plugin.conditions_modal.fields.message") }}
          </th>
          <th
            scope="col"
            class="px-4 py-3 text-left text-sm font-semibold text-gray-900"
          >
            {{ $t("core.plugin.conditions_modal.fields.last_transition_time") }}
          </th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-100 bg-white">
        <tr
          v-for="(condition, index) in plugin?.status?.conditions"
          :key="index"
        >
          <td class="whitespace-nowrap px-4 py-3 text-sm text-gray-900">
            {{ condition.type }}
          </td>
          <td class="whitespace-nowrap px-4 py-3 text-sm text-gray-500">
            {{ condition.status }}
          </td>
          <td class="whitespace-nowrap px-4 py-3 text-sm text-gray-500">
            {{ condition.reason || "-" }}
          </td>
          <td class="whitespace-nowrap px-4 py-3 text-sm text-gray-500">
            {{ condition.message || "-" }}
          </td>
          <td
            v-tooltip="formatDatetime(condition.lastTransitionTime)"
            class="whitespace-nowrap px-4 py-3 text-sm text-gray-500"
          >
            {{ relativeTimeTo(condition.lastTransitionTime) }}
          </td>
        </tr>
      </tbody>
    </table>

    <template #footer>
      <VButton @click="modal.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>

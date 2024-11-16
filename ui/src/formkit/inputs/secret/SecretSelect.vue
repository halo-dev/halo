<script lang="ts" setup>
import { secretAnnotations } from "@/constants/annotations";
import type { FormKitFrameworkContext } from "@formkit/core";
import { coreApiClient, type Secret } from "@halo-dev/api-client";
import {
  IconArrowRight,
  IconCheckboxCircle,
  IconClose,
  IconSettings,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { onClickOutside } from "@vueuse/core";
import Fuse from "fuse.js";
import { computed, ref, watch, type PropType } from "vue";
import SecretCreationModal from "./components/SecretCreationModal.vue";
import SecretEditModal from "./components/SecretEditModal.vue";
import SecretListModal from "./components/SecretListModal.vue";
import { Q_KEY, useSecretsFetch } from "./composables/use-secrets-fetch";

const queryClient = useQueryClient();

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const requiredKey = computed(() => props.context.requiredKey);

const selectedSecret = ref<Secret>();
const dropdownVisible = ref(false);
const text = ref("");
const wrapperRef = ref<HTMLElement>();

const { data } = useSecretsFetch();

onClickOutside(wrapperRef, () => {
  dropdownVisible.value = false;
});

// search
let fuse: Fuse<Secret> | undefined = undefined;

watch(
  () => data.value,
  () => {
    fuse = new Fuse(data.value?.items || [], {
      keys: ["metadata.name", "metadata.stringData"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  },
  {
    immediate: true,
  }
);

const searchResults = computed(() => {
  if (!text.value) {
    return data.value?.items;
  }

  return fuse?.search(text.value).map((item) => item.item) || [];
});

watch(
  () => searchResults.value,
  (value) => {
    if (value?.length && text.value) {
      selectedSecret.value = value[0];
      scrollToSelected();
    } else {
      selectedSecret.value = undefined;
    }
  }
);

const handleKeydown = (e: KeyboardEvent) => {
  if (!searchResults.value) return;

  if (e.key === "ArrowDown") {
    e.preventDefault();

    const index = searchResults.value.findIndex(
      (secret) => secret.metadata.name === selectedSecret.value?.metadata.name
    );
    if (index < searchResults.value.length - 1) {
      selectedSecret.value = searchResults.value[index + 1];
    }

    scrollToSelected();
  }
  if (e.key === "ArrowUp") {
    e.preventDefault();

    const index = searchResults.value.findIndex(
      (secret) => secret.metadata.name === selectedSecret.value?.metadata.name
    );
    if (index > 0) {
      selectedSecret.value = searchResults.value[index - 1];
    } else {
      selectedSecret.value = undefined;
    }

    scrollToSelected();
  }

  if (e.key === "Enter") {
    if (!selectedSecret.value && text.value) {
      e.preventDefault();
      handleCreateSecret();
      return;
    }

    if (selectedSecret.value) {
      handleSelect(selectedSecret.value);
      text.value = "";

      e.preventDefault();
    }
  }
};

const scrollToSelected = () => {
  const selectedNodeName = selectedSecret.value
    ? selectedSecret.value?.metadata.name
    : "secret-create";

  const selectedNode = document.getElementById(selectedNodeName);

  if (selectedNode) {
    selectedNode.scrollIntoView({
      behavior: "smooth",
      block: "nearest",
      inline: "start",
    });
  }
};

// Check required key and edit secret
function hasRequiredKey(secret: Secret) {
  return !!secret.stringData?.[requiredKey.value as string];
}
const secretToUpdate = ref<Secret>();
const secretEditModalVisible = ref(false);
const secretCreationModalVisible = ref(false);

const handleSelect = (secret?: Secret) => {
  if (!secret || secret.metadata.name === props.context._value) {
    props.context.node.input("");
    dropdownVisible.value = false;
    return;
  }

  props.context.node.input(secret.metadata.name);

  text.value = "";

  dropdownVisible.value = false;

  // Check required key and open edit modal
  if (!hasRequiredKey(secret)) {
    const stringDataToUpdate = {
      ...secret.stringData,
      [requiredKey.value ? (requiredKey.value as string) : ""]: "",
    };
    secretToUpdate.value = {
      ...secret,
      stringData: stringDataToUpdate,
    };
    secretEditModalVisible.value = true;
  }
};

const onTextInput = (e: Event) => {
  text.value = (e.target as HTMLInputElement).value;
};

const secretListModalVisible = ref(false);

// Create new secret
async function handleCreateSecret() {
  if (!requiredKey.value) {
    secretCreationModalVisible.value = true;
    return;
  }

  const { data: newSecret } = await coreApiClient.secret.createSecret({
    secret: {
      metadata: {
        generateName: "secret-",
        name: "",
      },
      kind: "Secret",
      apiVersion: "v1alpha1",
      type: "Opaque",
      stringData: {
        [requiredKey.value as string]: text.value,
      },
    },
  });

  queryClient.invalidateQueries({ queryKey: Q_KEY() });

  handleSelect(newSecret);

  text.value = "";

  dropdownVisible.value = false;
}
</script>

<template>
  <SecretListModal
    v-if="secretListModalVisible"
    @close="secretListModalVisible = false"
  />
  <SecretEditModal
    v-if="secretEditModalVisible && secretToUpdate"
    :secret="secretToUpdate"
    @close="secretEditModalVisible = false"
  />
  <SecretCreationModal
    v-if="secretCreationModalVisible"
    :form-state="{
      stringDataArray: [{ key: requiredKey ? requiredKey  as string : '', value: text || '' }],
    }"
    @close="secretCreationModalVisible = false"
  />
  <div
    ref="wrapperRef"
    class="flex h-full w-full items-center border border-gray-300 transition-all"
    :class="[
      { 'pointer-events-none': context.disabled },
      { 'border-primary shadow-sm': dropdownVisible },
    ]"
    style="border-radius: inherit"
    @keydown="handleKeydown"
  >
    <div
      class="flex w-full min-w-0 flex-1 shrink flex-wrap items-center"
      @click="dropdownVisible = true"
    >
      <div v-if="context._value" class="flex px-3 text-sm text-black">
        {{ context._value }}
      </div>
      <input
        v-else
        :value="text"
        :class="context.classes.input"
        type="text"
        :placeholder="$t('core.formkit.secret.placeholder')"
        @input="onTextInput"
        @focus="dropdownVisible = true"
      />
    </div>

    <div class="inline-flex h-full flex-none items-center gap-2">
      <div v-if="context._value" class="cursor-pointer" @click="handleSelect()">
        <IconClose
          class="rotate-90 text-sm text-gray-500 hover:text-gray-700"
        />
      </div>
      <div class="inline-flex h-full items-center">
        <div
          class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
          @click="dropdownVisible = !dropdownVisible"
        >
          <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
        </div>
        <div
          class="group flex h-full cursor-pointer items-center rounded-r-base border-l px-3 transition-all hover:bg-gray-100"
          @click="secretListModalVisible = true"
        >
          <IconSettings class="h-4 w-4 text-gray-500 hover:text-gray-700" />
        </div>
      </div>
    </div>

    <div
      v-if="dropdownVisible"
      class="absolute bottom-auto right-0 top-full z-10 mt-1 max-h-96 w-full overflow-auto rounded bg-white shadow-lg ring-1 ring-gray-100"
    >
      <ul class="p-1">
        <li
          v-if="text.trim()"
          id="secret-create"
          class="group flex cursor-pointer items-center justify-between rounded p-2"
          :class="{
            'bg-gray-100': selectedSecret === undefined,
          }"
          @click="handleCreateSecret"
        >
          <span class="text-xs text-gray-700 group-hover:text-gray-900">
            {{ $t("core.formkit.secret.creation_label") }}
          </span>
        </li>
        <li
          v-for="secret in searchResults"
          :id="secret.metadata.name"
          :key="secret.metadata.name"
          class="group flex cursor-pointer items-center rounded p-2 hover:bg-gray-100"
          :class="{
            'bg-gray-100':
              selectedSecret?.metadata.name === secret.metadata.name,
          }"
          @click="handleSelect(secret)"
        >
          <div
            class="inline-flex min-w-0 flex-1 shrink items-center space-x-2 overflow-hidden text-sm"
          >
            <span class="flex-none"> {{ secret.metadata.name }}</span>
            <span
              class="line-clamp-1 min-w-0 flex-1 shrink break-words text-xs text-gray-500"
            >
              {{
                hasRequiredKey(secret)
                  ? secret.metadata.annotations?.[secretAnnotations.DESCRIPTION]
                  : $t("core.formkit.secret.required_key_missing_label")
              }}
            </span>
          </div>
          <IconCheckboxCircle
            v-if="context._value === secret.metadata.name"
            class="flex-none text-primary"
          />
        </li>
      </ul>
    </div>
  </div>
</template>

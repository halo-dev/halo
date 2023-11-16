<script lang="ts" setup>
import {
  IconUserSettings,
  VDescription,
  VDescriptionItem,
  VTag,
} from "@halo-dev/components";
import type { Ref } from "vue";
import { inject } from "vue";
import type { DetailedUser } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";

const user = inject<Ref<DetailedUser | undefined>>("user");
</script>
<template>
  <div class="border-t border-gray-100">
    <VDescription>
      <VDescriptionItem
        :label="$t('core.user.detail.fields.display_name')"
        :content="user?.user.spec.displayName"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.username')"
        :content="user?.user.metadata.name"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.email')"
        :content="user?.user.spec.email || $t('core.common.text.none')"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.roles')"
        class="!px-2"
      >
        <VTag
          v-for="(role, index) in user?.roles"
          :key="index"
          @click="
            $router.push({
              name: 'RoleDetail',
              params: { name: role.metadata.name },
            })
          "
        >
          <template #leftIcon>
            <IconUserSettings />
          </template>
          {{
            role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
            role.metadata.name
          }}
        </VTag>
      </VDescriptionItem>
      <VDescriptionItem
        :label="$t('core.user.detail.fields.bio')"
        :content="user?.user.spec?.bio || $t('core.common.text.none')"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.user.detail.fields.creation_time')"
        :content="formatDatetime(user?.user.metadata?.creationTimestamp)"
        class="!px-2"
      />
    </VDescription>
  </div>
</template>

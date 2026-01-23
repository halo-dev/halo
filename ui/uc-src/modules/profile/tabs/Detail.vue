<script lang="ts" setup>
import { rbacAnnotations } from "@/constants/annotations";
import {
  IconShieldUser,
  VAlert,
  VButton,
  VDescription,
  VDescriptionItem,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { stores, utils } from "@halo-dev/ui-shared";
import { storeToRefs } from "pinia";
import { ref } from "vue";
import RiVerifiedBadgeLine from "~icons/ri/verified-badge-line";
import EmailVerifyModal from "../components/EmailVerifyModal.vue";

const { currentUser } = storeToRefs(stores.currentUser());

// verify email
const emailVerifyModal = ref(false);
</script>
<template>
  <div class="border-t border-gray-100">
    <VDescription>
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.display_name')"
        :content="currentUser?.user.spec.displayName"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.username')"
        :content="currentUser?.user.metadata.name"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.email')"
        class="!px-2"
      >
        <div v-if="currentUser" class="w-full xl:w-1/2">
          <VAlert
            v-if="!currentUser.user.spec.email"
            :title="$t('core.uc_profile.detail.email_not_set.title')"
            :description="
              $t('core.uc_profile.detail.email_not_set.description')
            "
            type="warning"
            :closable="false"
          >
            <template #actions>
              <VButton size="sm" @click="emailVerifyModal = true">
                {{ $t("core.common.buttons.setting") }}
              </VButton>
            </template>
          </VAlert>

          <div v-else>
            <div class="flex items-center space-x-2">
              <span>{{ currentUser.user.spec.email }}</span>
              <RiVerifiedBadgeLine
                v-if="currentUser.user.spec.emailVerified"
                v-tooltip="$t('core.uc_profile.detail.email_verified.tooltip')"
                class="text-xs text-blue-600"
              />
            </div>
            <div v-if="!currentUser.user.spec.emailVerified" class="mt-3">
              <VAlert
                :title="$t('core.uc_profile.detail.email_not_verified.title')"
                :description="
                  $t('core.uc_profile.detail.email_not_verified.description')
                "
                type="warning"
                :closable="false"
              >
                <template #actions>
                  <VButton size="sm" @click="emailVerifyModal = true">
                    {{ $t("core.common.buttons.verify") }}
                  </VButton>
                </template>
              </VAlert>
            </div>
          </div>
        </div>
      </VDescriptionItem>
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.roles')"
        class="!px-2"
      >
        <VSpace>
          <VTag v-for="role in currentUser?.roles" :key="role.metadata.name">
            <template #leftIcon>
              <IconShieldUser />
            </template>
            {{
              role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
              role.metadata.name
            }}
          </VTag>
        </VSpace>
      </VDescriptionItem>
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.bio')"
        :content="currentUser?.user.spec?.bio || $t('core.common.text.none')"
        class="!px-2"
      />
      <VDescriptionItem
        :label="$t('core.uc_profile.detail.fields.creation_time')"
        :content="
          utils.date.format(currentUser?.user.metadata?.creationTimestamp)
        "
        class="!px-2"
      />
    </VDescription>

    <EmailVerifyModal
      v-if="emailVerifyModal"
      @close="emailVerifyModal = false"
    />
  </div>
</template>

<script lang="ts" setup>
import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import {
  IconToolsFill,
  VButton,
  VCard,
  VEmpty,
  VEntity,
  VEntityField,
  VPageHeader,
} from "@halo-dev/components";
import { computed } from "vue";
import type { RouteRecordRaw } from "vue-router";
import { useRouter } from "vue-router";

const router = useRouter();
const roleStore = useRoleStore();

const { uiPermissions } = roleStore.permissions;

function isRouteValid(route?: RouteRecordRaw) {
  if (!route) return false;
  const { meta } = route;
  if (!meta?.menu) return false;
  return (
    !meta.permissions || hasPermission(uiPermissions, meta.permissions, true)
  );
}

const routes = computed(() => {
  const matchedRoute = router.currentRoute.value.matched[0];

  return router
    .getRoutes()
    .find((route) => route.name === matchedRoute.name)
    ?.children.filter((route) => route.path !== "")
    .filter((route) => isRouteValid(route));
});
</script>

<template>
  <VPageHeader :title="$t('core.tool.title')">
    <template #icon>
      <IconToolsFill class="mr-2 self-center" />
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <VEmpty
        v-if="!routes?.length"
        :title="$t('core.tool.empty.title')"
        :message="$t('core.tool.empty.message')"
      ></VEmpty>
      <ul
        v-else
        class="box-border h-full w-full divide-y divide-gray-100"
        role="list"
      >
        <li v-for="route in routes" :key="route.name">
          <VEntity>
            <template #start>
              <VEntityField>
                <template #description>
                  <component
                    :is="route.meta?.menu?.icon"
                    v-if="route.meta?.menu?.icon"
                    class="text-lg"
                  />
                  <IconToolsFill v-else class="text-lg" />
                </template>
              </VEntityField>
              <VEntityField
                :route="{ name: route.name }"
                :title="route.meta?.menu?.name"
                :description="route.meta?.description"
              ></VEntityField>
            </template>

            <template #end>
              <VEntityField>
                <template #description>
                  <VButton
                    size="sm"
                    @click="$router.push({ name: route.name })"
                  >
                    {{ $t("core.common.buttons.access") }}
                  </VButton>
                </template>
              </VEntityField>
            </template>
          </VEntity>
        </li>
      </ul>
    </VCard>
  </div>
</template>

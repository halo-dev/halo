<script lang="ts" setup>
import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import {
  IconToolsFill,
  VButton,
  VCard,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VPageHeader,
} from "@halo-dev/components";
import { onMounted, ref } from "vue";
import type { RouteRecordRaw } from "vue-router";
import { useRouter } from "vue-router";

const router = useRouter();
const roleStore = useRoleStore();

const { uiPermissions } = roleStore.permissions;
const routes = ref<RouteRecordRaw[]>([]);

async function isRouteValid(route?: RouteRecordRaw) {
  if (!route) return false;
  const { meta } = route;
  if (!meta?.menu) return false;

  // If permissions doesn't exist or is empty
  if (!meta.permissions) return true;

  // Check if permissions is a function
  if (typeof meta.permissions === "function") {
    try {
      return await meta.permissions(uiPermissions);
    } catch (e) {
      console.error(
        `Error checking permissions for route ${String(route.name)}:`,
        e
      );
      return false;
    }
  }

  // Default behavior for array of permissions
  return hasPermission(uiPermissions, meta.permissions as string[], true);
}

// Use async function to set routes
const fetchRoutes = async () => {
  const matchedRoute = router.currentRoute.value.matched[0];
  const childRoutes =
    router
      .getRoutes()
      .find((route) => route.name === matchedRoute.name)
      ?.children.filter((route) => route.path !== "") || [];

  const validRoutes: RouteRecordRaw[] = [];
  for (const route of childRoutes) {
    if (await isRouteValid(route)) {
      validRoutes.push(route);
    }
  }

  routes.value = validRoutes;
};

// Fetch routes on component mount
onMounted(() => {
  fetchRoutes();
});
</script>

<template>
  <VPageHeader :title="$t('core.tool.title')">
    <template #icon>
      <IconToolsFill />
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <VEmpty
        v-if="!routes?.length"
        :title="$t('core.tool.empty.title')"
        :message="$t('core.tool.empty.message')"
      ></VEmpty>
      <VEntityContainer v-else>
        <VEntity v-for="route in routes" :key="route.name">
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
                <VButton size="sm" @click="$router.push({ name: route.name })">
                  {{ $t("core.common.buttons.access") }}
                </VButton>
              </template>
            </VEntityField>
          </template>
        </VEntity>
      </VEntityContainer>
    </VCard>
  </div>
</template>

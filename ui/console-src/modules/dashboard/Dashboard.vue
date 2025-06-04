<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import {
  IconDashboard,
  IconSettings,
  VButton,
  VPageHeader,
} from "@halo-dev/components";
import { ref } from "vue";
import { useDashboardWidgetsFetch } from "./composables/use-dashboard-widgets-fetch";
import "./styles/dashboard.css";

const { currentUserHasPermission } = usePermission();

const currentBreakpoint = ref();

function breakpointChangedEvent(breakpoint: string) {
  currentBreakpoint.value = breakpoint;
}

const { layouts, layout } = useDashboardWidgetsFetch(currentBreakpoint);
</script>
<template>
  <VPageHeader :title="$t('core.dashboard.title')">
    <template #icon>
      <IconDashboard class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        type="secondary"
        @click="$router.push({ name: 'DashboardDesigner' })"
      >
        <template #icon>
          <IconSettings class="h-full w-full" />
        </template>
        {{ $t("core.dashboard.actions.setting") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="dashboard m-4">
    <grid-layout
      v-model:layout="layout"
      :responsive-layouts="layouts"
      :col-num="12"
      :is-draggable="false"
      :is-resizable="false"
      :margin="[10, 10]"
      :responsive="true"
      :row-height="30"
      :use-css-transforms="true"
      :vertical-compact="true"
      @breakpoint-changed="breakpointChangedEvent"
    >
      <template v-for="item in layout">
        <grid-item
          v-if="currentUserHasPermission(item.permissions)"
          :key="item.i"
          :h="item.h"
          :i="item.i"
          :w="item.w"
          :x="item.x"
          :y="item.y"
          :min-w="item.minW"
          :min-h="item.minH"
          :max-w="item.maxW"
          :max-h="item.maxH"
        >
          <component :is="item.componentName" :config="item.config" />
        </grid-item>
      </template>
    </grid-layout>
  </div>
</template>

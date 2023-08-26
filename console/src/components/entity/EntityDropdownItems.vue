<script setup lang="ts" generic="T">
import type { EntityDropdownItem } from "@halo-dev/console-shared";
import { VDropdown } from "@halo-dev/components";

const props = withDefaults(
  defineProps<{
    dropdownItems: EntityDropdownItem<T>[];
    item?: T;
  }>(),
  {
    item: undefined,
  }
);

function action(dropdownItem: EntityDropdownItem<T>) {
  if (!dropdownItem.action) {
    return;
  }
  dropdownItem.action(props.item);
}
</script>

<template>
  <template v-for="(dropdownItem, index) in dropdownItems">
    <template v-if="dropdownItem.visible">
      <VDropdown
        v-if="dropdownItem.children?.length"
        :key="`dropdown-children-items-${index}`"
        v-permission="dropdownItem.permissions"
        :triggers="['click']"
      >
        <component
          :is="dropdownItem.component"
          v-bind="dropdownItem.props"
          @click="action(dropdownItem)"
        >
          {{ dropdownItem.label }}
        </component>
        <template #popper>
          <template v-for="(childItem, childIndex) in dropdownItem.children">
            <component
              :is="childItem.component"
              v-if="childItem.visible"
              v-bind="childItem.props"
              :key="`dropdown-child-item-${childIndex}`"
              v-permission="childItem.permissions"
              @click="action(childItem)"
            >
              {{ childItem.label }}
            </component>
          </template>
        </template>
      </VDropdown>
      <component
        :is="dropdownItem.component"
        v-else
        v-bind="dropdownItem.props"
        :key="`dropdown-item-${index}`"
        v-permission="dropdownItem.permissions"
        @click="action(dropdownItem)"
      >
        {{ dropdownItem.label }}
      </component>
    </template>
  </template>
</template>

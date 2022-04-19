<template>
  <FilledLayout>
    <div class="container p-3">
      <section class="box border-2 rounded p-2 mb-3">
        <h1 class="text-xl font-bold mb-2">Button</h1>
        <h2 class="mb-1">Type:</h2>
        <div class="mb-3">
          <VButton class="mr-2" type="primary">Primary</VButton>
          <VButton class="mr-2" type="secondary">Secondary</VButton>
          <VButton class="mr-2" type="danger">Danger</VButton>
          <VButton type="default">Default</VButton>
        </div>
        <h2 class="mb-1">Size:</h2>
        <div class="mb-3">
          <VButton class="mr-2" size="lg" type="secondary"> Large</VButton>
          <VButton class="mr-2" type="secondary"> Default</VButton>
          <VButton class="mr-2" size="sm" type="secondary"> sm</VButton>
          <VButton size="xs" type="secondary"> xs</VButton>
        </div>
        <h2 class="mb-1">Circle:</h2>
        <div class="mb-3">
          <VButton circle class="mr-2" size="lg" type="secondary"> lg</VButton>
          <VButton circle class="mr-2" type="secondary"> d</VButton>
          <VButton circle class="mr-2" size="sm" type="secondary"> sm</VButton>
          <VButton circle size="xs" type="secondary"> xs</VButton>
        </div>
        <h2 class="mb-1">Block:</h2>
        <div class="mb-3">
          <VButton block class="mb-2" type="primary"> Primary</VButton>
          <VButton block class="mb-2" type="secondary"> Secondary</VButton>
          <VButton block type="danger"> Danger</VButton>
        </div>
        <h2 class="mb-1">Disabled:</h2>
        <div>
          <VButton class="mr-2" disabled type="primary"> Primary</VButton>
          <VButton class="mr-2" disabled type="secondary"> Secondary</VButton>
          <VButton disabled type="danger"> Danger</VButton>
        </div>
      </section>
      <section class="box border-2 rounded p-2 mb-3">
        <h1 class="text-xl font-bold mb-2">Input</h1>
        <h2 class="mb-1">Size:</h2>
        <div class="mb-3">
          <VInput
            class="mb-2"
            v-model="inputValue"
            placeholder="请输入邮箱"
            size="lg"
          />
          <VInput
            class="mb-2"
            v-model="inputValue"
            placeholder="请输入邮箱"
            size="md"
          />
          <VInput
            class="mb-2"
            v-model="inputValue"
            placeholder="请输入邮箱"
            size="sm"
          />
          <VInput v-model="inputValue" placeholder="请输入邮箱" size="xs" />
        </div>
        <!--        <h2 class="mb-1">With Icon:</h2>-->
        <!--        <div class="mb-3">-->
        <!--          <VInput v-model="inputValue" class="mb-2" size="md">-->
        <!--            <template #prefix>-->
        <!--              <IconDashboard />-->
        <!--            </template>-->
        <!--            <template #suffix>-->
        <!--              <IconDashboard />-->
        <!--            </template>-->
        <!--          </VInput>-->
        <!--        </div>-->
        <h2 class="mb-1">Disabled:</h2>
        <div class="mb-3">
          <VInput v-model="inputValue" disabled />
        </div>
      </section>
      <section class="box border-2 rounded p-2">
        <h1 class="text-xl font-bold mb-2">Select</h1>
        <h2 class="mb-1">Size:</h2>
        <div class="mb-3">
          <VSelect v-model="selectValue">
            <VOption
              v-for="(option, index) in selectData"
              :value="option.value"
              :key="index"
            >
              {{ option.label }}
            </VOption>
          </VSelect>
        </div>
        <h2 class="mb-1">Disabled:</h2>
        <div class="mb-3">
          <VSelect v-model="selectValue" disabled>
            <VOption
              v-for="(option, index) in selectData"
              :value="option.value"
              :key="index"
            >
              {{ option.label }}
            </VOption>
          </VSelect>
        </div>
      </section>
    </div>
  </FilledLayout>
</template>

<script lang="ts" setup>
import { FilledLayout } from "../layouts";
import { VButton } from "@/components/base/button";
import { VInput } from "@/components/base/input";
import { VSelect, VOption } from "@/components/base/select";
import { ref } from "vue";

const inputValue = ref();
const selectValue = ref();

const selectData = [
  {
    value: "1",
    label: "1",
  },
  {
    value: "2",
    label: "2",
  },
];
</script>

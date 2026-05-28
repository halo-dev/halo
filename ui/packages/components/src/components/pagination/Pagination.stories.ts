import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { computed, shallowRef } from "vue";
import { VPagination } from ".";
import { VCard } from "../card";

const meta: Meta<typeof VPagination> = {
  title: "Components/Pagination",
  component: VPagination,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VCard, VPagination },
    setup() {
      const page = shallowRef(args.page);
      const size = shallowRef(args.size);
      const rangeText = computed(() => {
        const start = (page.value - 1) * size.value + 1;
        const end = Math.min(page.value * size.value, args.total);
        return `${start}-${end}`;
      });

      return { args, page, rangeText, size };
    },
    template: `
      <div class="max-w-3xl">
        <VCard title="文章列表">
          <div class="divide-y divide-gray-100">
            <div v-for="index in 4" :key="index" class="flex items-center justify-between py-3">
              <div>
                <div class="text-sm font-medium text-gray-900">示例文章 {{ index + (page - 1) * size }}</div>
                <div class="mt-1 text-xs text-gray-500">展示范围 {{ rangeText }}，共 {{ args.total }} 条</div>
              </div>
              <div class="text-xs text-gray-400">已发布</div>
            </div>
          </div>
          <template #footer>
            <VPagination
              v-model:page="page"
              v-model:size="size"
              :total="args.total"
              :size-options="args.sizeOptions"
              :show-total="args.showTotal"
            />
          </template>
        </VCard>
      </div>
    `,
  }),
  argTypes: {
    showTotal: {
      control: { type: "boolean" },
    },
  },
  args: {
    page: 1,
    size: 10,
    total: 128,
    sizeOptions: [10, 20, 50],
    showTotal: true,
  },
};

export default meta;
type Story = StoryObj<typeof VPagination>;

export const Default: Story = {};

export const EmptyTotal: Story = {
  args: {
    total: 0,
  },
};

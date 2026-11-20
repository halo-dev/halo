import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { computed, ref } from "vue";
import { VDatePickerDropdown } from ".";
import { IconCalendar } from "../../icons/icons";

const meta = {
  title: "Components/DatePickerDropdown",
  component: VDatePickerDropdown,
  tags: ["autodocs"],
  args: {
    modelValue: "2026-11-19",
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VDatePickerDropdown, IconCalendar },
    setup() {
      const value = ref(args.modelValue);
      const displayValue = computed(() => {
        if (!value.value) {
          return undefined;
        }

        if (args.format) {
          return args.format(value.value);
        }

        return args.mode === "datetime"
          ? value.value.replace("T", " ")
          : value.value;
      });
      return { args, value, displayValue };
    },
    template: `
      <div class="h-96 p-4">
        <div class="w-64">
          <VDatePickerDropdown v-bind="args" v-model="value">
            <template #trigger="{ open }">
              <button
                type="button"
                class="flex h-9 w-full items-center justify-between gap-2 rounded-base border border-gray-300 bg-white px-3 text-sm text-gray-900 transition-colors hover:border-gray-400 focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary"
                :class="open ? 'border-primary' : ''"
              >
                <span class="truncate" :class="displayValue ? '' : 'text-gray-400'">
                  {{ displayValue ?? "Select date" }}
                </span>
                <IconCalendar class="h-4 w-4 shrink-0 text-gray-400" />
              </button>
            </template>
          </VDatePickerDropdown>
        </div>
      </div>
    `,
  }),
} satisfies Meta<typeof VDatePickerDropdown>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const DateTime: Story = {
  args: {
    mode: "datetime",
    modelValue: "2026-11-19T09:30",
  },
};

export const WithoutValue: Story = {
  args: {
    modelValue: undefined,
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};

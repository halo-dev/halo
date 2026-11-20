import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { computed, ref } from "vue";
import { VDateRangePickerDropdown } from ".";
import { IconCalendar } from "../../icons/icons";
import type { DateRangeValue } from "./types";

const meta = {
  title: "Components/DateRangePickerDropdown",
  component: VDateRangePickerDropdown,
  tags: ["autodocs"],
  args: {
    modelValue: { start: "2026-11-05", end: "2026-11-19" },
    defaultPickerValue: "2026-11-01",
  },
  render: (args) => ({
    components: { VDateRangePickerDropdown, IconCalendar },
    setup() {
      const value = ref<DateRangeValue | undefined>(args.modelValue);
      const displayValue = computed(() => {
        if (!value.value?.start && !value.value?.end) {
          return undefined;
        }

        const formatValue = (part?: string) => {
          if (!part) {
            return "…";
          }

          return args.mode === "datetime-range" ? part.replace("T", " ") : part;
        };

        return `${formatValue(value.value?.start)} ~ ${formatValue(
          value.value?.end
        )}`;
      });
      return { args, value, displayValue };
    },
    template: `
      <div class="h-96 p-4">
        <div class="w-80">
          <VDateRangePickerDropdown v-bind="args" v-model="value">
            <template #trigger="{ open }">
              <button
                type="button"
                class="flex h-9 w-full items-center justify-between gap-2 rounded-base border border-gray-300 bg-white px-3 text-sm text-gray-900 transition-colors hover:border-gray-400 focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary"
                :class="open ? 'border-primary' : ''"
              >
                <span class="truncate" :class="displayValue ? '' : 'text-gray-400'">
                  {{ displayValue ?? "Select date range" }}
                </span>
                <IconCalendar class="h-4 w-4 shrink-0 text-gray-400" />
              </button>
            </template>
          </VDateRangePickerDropdown>
        </div>
      </div>
    `,
  }),
} satisfies Meta<typeof VDateRangePickerDropdown>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const DateTimeRange: Story = {
  args: {
    mode: "datetime-range",
    modelValue: {
      start: "2026-11-05T08:00",
      end: "2026-11-19T18:00",
    },
  },
};

export const WithoutValue: Story = {
  args: {
    modelValue: undefined,
  },
};

export const WithPresets: Story = {
  args: {
    presets: [
      {
        label: "Today",
        value: () => ({ start: "2026-11-19", end: "2026-11-19" }),
      },
      {
        label: "Last 7 days",
        value: { start: "2026-11-13", end: "2026-11-19" },
      },
      {
        label: "Last 30 days",
        value: { start: "2026-10-21", end: "2026-11-19" },
      },
    ],
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};

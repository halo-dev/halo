import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import {
  VFilterDropdown,
  VFilterDropdownGroup,
  type FilterDropdownOption,
} from ".";

const statusOptions: FilterDropdownOption[] = [
  { label: "Published", value: "published" },
  { label: "Draft", value: "draft" },
  { label: "Scheduled", value: "scheduled" },
];

const authorOptions: FilterDropdownOption[] = [
  { label: "ruibaby", value: "ruibaby", description: "Halo maintainer" },
  { label: "Copilot", value: "copilot", description: "GitHub" },
  { label: "1lck", value: "1lck", description: "Lichenkang" },
];

const meta = {
  title: "Components/FilterDropdownGroup",
  component: VFilterDropdownGroup,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VFilterDropdown, VFilterDropdownGroup },
    setup() {
      const status = ref<string>();
      const author = ref<string>();
      return { args, status, author, statusOptions, authorOptions };
    },
    template: `
      <div class="flex h-80 items-start gap-5 p-4">
        <VFilterDropdownGroup v-bind="args">
          <VFilterDropdown
            v-model="status"
            label="Status"
            clear-label="All"
            show-clear-option
            :options="statusOptions"
          />
          <VFilterDropdown
            v-model="author"
            label="Author"
            clear-label="All"
            searchable
            show-clear-option
            :options="authorOptions"
          />
        </VFilterDropdownGroup>
      </div>
    `,
  }),
} satisfies Meta<typeof VFilterDropdownGroup>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

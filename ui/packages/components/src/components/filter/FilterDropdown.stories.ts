import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { onMounted, ref } from "vue";
import { IconFolder, IconUserLine } from "@/icons/icons";
import { VFilterDropdown, type FilterDropdownOption } from ".";
import { VAvatar } from "../avatar";

const meta = {
  title: "Components/FilterDropdown",
  component: VFilterDropdown,
  tags: ["autodocs"],
  args: {
    label: "Reviews",
    title: "Filter by review status",
    options: [
      { label: "No reviews", value: "none" },
      { label: "Review required", value: "required" },
      { label: "Approved review", value: "approved" },
      { label: "Changes requested", value: "changes-requested" },
      { label: "Reviewed by you", value: "reviewed-by-you" },
      { label: "Not reviewed by you", value: "not-reviewed-by-you" },
      { label: "Awaiting review from you", value: "awaiting-review" },
    ],
  },
  render: (args) => ({
    components: { VFilterDropdown },
    setup() {
      const value = ref(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="flex h-80 items-start p-4">
        <VFilterDropdown v-bind="args" v-model="value" />
      </div>
    `,
  }),
} satisfies Meta<typeof VFilterDropdown>;

export default meta;
type Story = StoryObj<typeof meta>;

const authors: FilterDropdownOption[] = [
  {
    label: "ruibaby",
    value: "ruibaby",
    description: "Halo maintainer",
    icon: IconUserLine,
  },
  {
    label: "Copilot",
    value: "copilot",
    description: "GitHub",
    icon: IconUserLine,
  },
  {
    label: "1lck",
    value: "1lck",
    description: "Lichenkang",
    icon: IconUserLine,
  },
  {
    label: "AceCandy",
    value: "acecandy",
    description: "AceCandy",
    icon: IconUserLine,
  },
];

export const Default: Story = {};

export const Outline: Story = {
  args: {
    variant: "outline",
  },
};

export const WithClearOption: Story = {
  args: {
    showClearOption: true,
    clearLabel: "All",
  },
};

export const Searchable: Story = {
  args: {
    label: "Author",
    title: "Filter by author",
    searchable: true,
    searchPlaceholder: "Filter authors",
    options: authors,
  },
};

export const Grouped: Story = {
  args: {
    label: "Project",
    title: "Filter by project",
    searchable: true,
    options: [
      {
        label: "Recent",
        options: [
          { label: "Halo", value: "halo", description: "halo-dev/halo" },
          {
            label: "Console",
            value: "console",
            description: "halo-dev/console",
          },
        ],
      },
      {
        label: "All projects",
        options: [
          {
            label: "Website",
            value: "website",
            description: "halo-dev/website",
          },
          {
            label: "Plugins",
            value: "plugins",
            description: "halo-dev/plugin",
          },
        ],
      },
    ],
  },
};

export const Tree: Story = {
  args: {
    label: "Category",
    title: "Filter by category",
    searchable: true,
    options: [
      {
        label: "Frontend",
        icon: IconFolder,
        children: [
          { label: "Vue", value: "vue", description: "frontend/vue" },
          { label: "React", value: "react", description: "frontend/react" },
        ],
      },
      {
        label: "Backend",
        icon: IconFolder,
        children: [
          { label: "Java", value: "java", description: "backend/java" },
          { label: "Go", value: "go", description: "backend/go" },
        ],
      },
    ],
  },
};

export const CustomOption: Story = {
  args: {
    label: "Author",
    title: "Filter by author",
    searchable: true,
    options: authors,
    panelWidth: "18rem",
  },
  render: (args) => ({
    components: { VFilterDropdown, VAvatar },
    setup() {
      const value = ref(args.modelValue);
      return { args, value };
    },
    template: `
      <div class="flex h-80 items-start p-4">
        <VFilterDropdown v-bind="args" v-model="value">
          <template #option="{ option }">
            <VAvatar size="sm" circle aria-hidden="true" :alt="option.label" :src="'https://api.dicebear.com/9.x/bottts/svg?seed=' + option.value" />
            <div class="flex min-w-0 flex-1 flex-col">
              <span class="truncate text-sm text-gray-700">{{ option.label }}</span>
              <span class="truncate text-xs text-gray-500">{{ option.description }}</span>
            </div>
          </template>
        </VFilterDropdown>
      </div>
    `,
  }),
};

const allUsers: FilterDropdownOption[] = Array.from(
  { length: 40 },
  (_, index) => {
    const number = index + 1;
    return {
      label: `User ${number}`,
      value: `user-${number}`,
      description: `user-${number}@example.com`,
    };
  }
);

export const RemoteSearchAndPagination: Story = {
  args: {
    label: "Author",
    title: "Filter by author",
    searchable: true,
    searchPlaceholder: "Filter authors",
    remoteSearch: true,
    options: [],
  },
  render: (args) => ({
    components: { VFilterDropdown },
    setup() {
      const PAGE_SIZE = 8;
      const value = ref(args.modelValue);
      const options = ref<FilterDropdownOption[]>([]);
      const loading = ref(false);
      const hasMore = ref(true);

      let keyword = "";
      let page = 0;
      let timer: ReturnType<typeof setTimeout> | undefined;

      function load(reset = false) {
        if (reset) {
          page = 0;
          options.value = [];
          hasMore.value = true;
        }

        if (!hasMore.value) {
          return;
        }

        loading.value = true;

        if (timer) {
          clearTimeout(timer);
        }

        timer = setTimeout(() => {
          const matched = allUsers.filter((option) =>
            `${option.label} ${option.description}`
              .toLowerCase()
              .includes(keyword.toLowerCase())
          );
          const start = page * PAGE_SIZE;
          const items = matched.slice(start, start + PAGE_SIZE);

          options.value = start === 0 ? items : [...options.value, ...items];
          hasMore.value = start + items.length < matched.length;
          page += 1;
          loading.value = false;
        }, 400);
      }

      function onSearch(value: string) {
        keyword = value;
        load(true);
      }

      onMounted(() => load(true));

      return { args, value, options, loading, hasMore, onSearch, load };
    },
    template: `
      <div class="flex h-96 items-start p-4">
        <VFilterDropdown
          v-bind="args"
          v-model="value"
          :options="options"
          :loading="loading"
          :has-more="hasMore"
          @search="onSearch"
          @load-more="load()"
        />
      </div>
    `,
  }),
};

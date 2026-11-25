import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VAccordion, VAccordionItem } from ".";
import { IconSettings } from "../../icons/icons";

const meta: Meta<typeof VAccordion> = {
  title: "Components/Accordion",
  component: VAccordion,
  tags: ["autodocs"],
  render: (args) => ({
    components: { VAccordion, VAccordionItem },
    setup() {
      return { args };
    },
    template: `
      <VAccordion v-bind="args">
        <VAccordionItem value="general" title="通用设置">
          <p class="text-sm text-gray-600">
            在这里配置通用选项。
          </p>
        </VAccordionItem>
        <VAccordionItem value="publishing" title="发布设置">
          <p class="text-sm text-gray-600">
            在这里配置发布选项。
          </p>
        </VAccordionItem>
        <VAccordionItem value="advanced" title="高级设置">
          <p class="text-sm text-gray-600">
            在这里配置高级选项。
          </p>
        </VAccordionItem>
      </VAccordion>
    `,
  }),
  args: {
    type: "single",
    collapsible: true,
    disabled: false,
    unmountOnHide: false,
    variant: "default",
    chevronPosition: "right",
    disableChevronRotation: false,
    transitionDuration: 200,
  },
  argTypes: {
    type: {
      control: { type: "inline-radio" },
      options: ["single", "multiple"],
    },
    collapsible: {
      control: { type: "boolean" },
    },
    disabled: {
      control: { type: "boolean" },
    },
    unmountOnHide: {
      control: { type: "boolean" },
    },
    variant: {
      control: { type: "inline-radio" },
      options: ["default", "bordered", "filled", "separated", "plain"],
    },
    chevronPosition: {
      control: { type: "inline-radio" },
      options: ["left", "right"],
    },
    disableChevronRotation: {
      control: { type: "boolean" },
    },
    transitionDuration: {
      control: { type: "range", min: 0, max: 1000, step: 50 },
    },
    order: {
      control: { type: "select" },
      options: [undefined, 2, 3, 4, 5, 6],
    },
  },
};

export default meta;
type Story = StoryObj<typeof VAccordion>;

export const Default: Story = {};

export const Bordered: Story = {
  args: {
    variant: "bordered",
    defaultValue: "general",
  },
};

export const Filled: Story = {
  args: {
    variant: "filled",
    defaultValue: "general",
  },
};

export const Separated: Story = {
  args: {
    variant: "separated",
    defaultValue: "general",
  },
};

export const Multiple: Story = {
  args: {
    type: "multiple",
    defaultValue: ["general", "publishing"],
  },
};

export const ChevronLeft: Story = {
  args: {
    variant: "bordered",
    chevronPosition: "left",
    defaultValue: "general",
  },
};

export const LeadingIcon: Story = {
  args: {
    variant: "bordered",
    defaultValue: "general",
  },
  render: (args) => ({
    components: { VAccordion, VAccordionItem },
    setup() {
      return { args, IconSettings };
    },
    template: `
      <VAccordion v-bind="args">
        <VAccordionItem value="general" title="通用设置" :leading-icon="IconSettings">
          <p class="text-sm text-gray-600">
            左侧可以放置一个图标。
          </p>
        </VAccordionItem>
        <VAccordionItem value="publishing" title="发布设置" :leading-icon="IconSettings">
          <p class="text-sm text-gray-600">
            触发器、标签、图标和箭头都支持自定义。
          </p>
        </VAccordionItem>
      </VAccordion>
    `,
  }),
};

export const CustomChevron: Story = {
  args: {
    variant: "bordered",
    defaultValue: "general",
  },
  render: (args) => ({
    components: { VAccordion, VAccordionItem },
    setup() {
      return { args };
    },
    template: `
      <VAccordion v-bind="args">
        <template #chevron>
          <span class="text-xs font-semibold text-primary">+</span>
        </template>
        <VAccordionItem value="general" title="通用设置">
          <p class="text-sm text-gray-600">
            在根组件定义一次箭头，所有子项都会复用。
          </p>
        </VAccordionItem>
        <VAccordionItem value="publishing" title="发布设置">
          <p class="text-sm text-gray-600">
            也可以在每个 VAccordionItem 上覆盖。
          </p>
        </VAccordionItem>
      </VAccordion>
    `,
  }),
};

export const NoRotation: Story = {
  args: {
    disableChevronRotation: true,
  },
};

export const NoTransition: Story = {
  args: {
    transitionDuration: 0,
  },
};

export const Plain: Story = {
  args: {
    variant: "plain",
  },
  render: (args) => ({
    components: { VAccordion, VAccordionItem },
    setup() {
      const open = ref<string | string[] | undefined>("custom");

      return { args, open };
    },
    template: `
      <div class="max-w-xl rounded-base border border-[#eaecf0] bg-white p-4">
        <VAccordion v-model="open" v-bind="args">
          <VAccordionItem value="custom" header-as="div">
            <template #trigger="{ open: isOpen }">
              <span class="font-medium">
                {{ isOpen ? "自定义字段" : "添加自定义字段" }}
              </span>
            </template>
            <div class="space-y-2">
              <p class="text-sm text-gray-600">
                触发器可以渲染任意内容，收起后内容仍保持挂载。
              </p>
              <input
                class="block h-9 w-full rounded-md border border-gray-300 px-3 text-sm text-gray-900"
                placeholder="自定义字段值"
              />
            </div>
          </VAccordionItem>
        </VAccordion>
      </div>
    `,
  }),
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};

/**
 * `order` controls the heading level wrapping each trigger (`4` renders
 * `h4`). Individual items can override it with their own `headerAs` prop.
 */
export const HeaderOrder: Story = {
  args: {
    order: 4,
    variant: "bordered",
    defaultValue: "general",
  },
};

/**
 * Side-by-side comparison of every layout. Each accordion has its first item
 * open so the active-state styling is visible.
 */
export const AllVariants: Story = {
  parameters: {
    controls: { disable: true },
  },
  render: () => ({
    components: { VAccordion, VAccordionItem },
    setup() {
      return {
        variants: ["default", "bordered", "filled", "separated"],
      };
    },
    template: `
      <div class="grid gap-6 md:grid-cols-2">
        <div v-for="variant in variants" :key="variant" class="space-y-2">
          <div class="text-xs font-medium uppercase tracking-wide text-gray-500">
            {{ variant }}
          </div>
          <VAccordion
            :variant="variant"
            :default-value="'first'"
            type="single"
            collapsible
          >
            <VAccordionItem value="first" title="通用设置">
              <p class="text-sm text-gray-600">在这里配置通用选项。</p>
            </VAccordionItem>
            <VAccordionItem value="second" title="发布设置">
              <p class="text-sm text-gray-600">在这里配置发布选项。</p>
            </VAccordionItem>
            <VAccordionItem value="third" title="高级设置">
              <p class="text-sm text-gray-600">在这里配置高级选项。</p>
            </VAccordionItem>
          </VAccordion>
        </div>
      </div>
    `,
  }),
};

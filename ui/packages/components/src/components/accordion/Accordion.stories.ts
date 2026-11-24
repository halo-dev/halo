import type { Meta, StoryObj } from "@storybook/vue3-vite";
import { ref } from "vue";
import { VAccordion, VAccordionItem } from ".";

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
    variant: "bordered",
  },
  argTypes: {
    type: {
      control: { type: "inline-radio" },
      options: ["single", "multiple"],
    },
    variant: {
      control: { type: "inline-radio" },
      options: ["bordered", "plain"],
    },
  },
};

export default meta;
type Story = StoryObj<typeof VAccordion>;

export const Default: Story = {};

export const Multiple: Story = {
  args: {
    type: "multiple",
    defaultValue: ["general", "publishing"],
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

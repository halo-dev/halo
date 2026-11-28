import type { FormKitFrameworkContext } from "@formkit/core";
import { utils } from "@halo-dev/ui-shared";
import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it } from "vite-plus/test";
import type { Component } from "vue";
import GroupSettingsButton from "../GroupSettingsButton.vue";
import PolicySettingsButton from "../PolicySettingsButton.vue";

const cases: Array<{
  name: string;
  component: Component;
  label: string;
}> = [
  {
    name: "PolicySettingsButton",
    component: PolicySettingsButton,
    label: "core.attachment.policies_modal.title",
  },
  {
    name: "GroupSettingsButton",
    component: GroupSettingsButton,
    label: "core.attachment.groups_modal.title",
  },
];

function mountSuffixButton(component: Component, disabled = false) {
  return mount(component, {
    props: {
      context: {
        _value: undefined,
        disabled,
        node: { input: () => {}, context: {} },
      } as unknown as FormKitFrameworkContext,
    },
    global: {
      mocks: {
        $t: (key: string) => key,
      },
      directives: {
        tooltip: {},
      },
    },
  });
}

describe.each(cases)("$name", ({ component, label }) => {
  beforeEach(() => {
    utils.permission.setUserPermissions(["*", "system:attachments:manage"]);
  });

  it("renders the suffix as a full-height button", () => {
    const wrapper = mountSuffixButton(component);
    const button = wrapper.get("button");

    expect(button.attributes("type")).toBe("button");
    expect(button.attributes("aria-label")).toBe(label);
    expect(button.classes()).toContain("self-stretch");
    expect(button.classes()).not.toContain("h-full");
  });

  it("disables the suffix with the field", () => {
    const wrapper = mountSuffixButton(component, true);

    expect(wrapper.get("button").attributes()).toHaveProperty("disabled");
  });
});

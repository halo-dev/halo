import { describe, expect, it } from "vite-plus/test";
import {
  isSelectOptionMatched,
  mapItemsToSelectOptions,
} from "../option-utils";

describe("select option utils", () => {
  it("maps action response items with icon and description fields", () => {
    const options = mapItemsToSelectOptions(
      [
        {
          metadata: {
            name: "plugin-a",
          },
          spec: {
            description: "Manage plugin settings",
            displayName: "Plugin A",
          },
          status: {
            logo: "/plugins/plugin-a/assets/logo.svg",
          },
        },
      ],
      {
        descriptionField: "spec.description",
        iconField: "status.logo",
        labelField: "spec.displayName",
        valueField: "metadata.name",
      }
    );

    expect(options).toEqual([
      {
        description: "Manage plugin settings",
        icon: "/plugins/plugin-a/assets/logo.svg",
        label: "Plugin A",
        value: "plugin-a",
      },
    ]);
  });

  it("keeps action response mapping compatible without metadata fields", () => {
    const options = mapItemsToSelectOptions(
      [
        {
          label: "Theme",
          value: "theme",
        },
      ],
      {}
    );

    expect(options).toEqual([
      {
        label: "Theme",
        value: "theme",
      },
    ]);
  });

  it("matches local search by label and description but not icon source", () => {
    const option = {
      description: "Controls the dashboard actions",
      icon: "/assets/shortcut.svg",
      label: "Quick action",
      value: "quick-action",
    };

    expect(isSelectOptionMatched(option, "quick")).toBe(true);
    expect(isSelectOptionMatched(option, "dashboard")).toBe(true);
    expect(isSelectOptionMatched(option, "shortcut")).toBe(false);
  });
});

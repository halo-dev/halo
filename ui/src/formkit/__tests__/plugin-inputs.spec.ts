import type { FormKitLibrary, FormKitTypeDefinition } from "@formkit/core";
import type { PluginModule } from "@halo-dev/ui-shared";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { collectPluginFormKitInputs } from "../plugin-inputs";

function createInputDefinition(): FormKitTypeDefinition {
  return {
    type: "input",
    schema: [],
  };
}

function createPluginSource(name: string, module: PluginModule) {
  return {
    name,
    module,
  };
}

describe("collectPluginFormKitInputs", () => {
  let warnSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    warnSpy = vi.spyOn(console, "warn").mockImplementation(() => undefined);
  });

  afterEach(() => {
    warnSpy.mockRestore();
  });

  it("collects valid plugin FormKit inputs", () => {
    const pluginInput = createInputDefinition();
    const result = collectPluginFormKitInputs(
      [
        createPluginSource("plugin-a", {
          formkit: {
            inputs: {
              pluginInput,
            },
          },
        }),
      ],
      {}
    );

    expect(result).toEqual({
      pluginInput,
    });
    expect(warnSpy).not.toHaveBeenCalled();
  });

  it("keeps built-in inputs when plugin input names conflict", () => {
    const builtinInput = createInputDefinition();
    const pluginInput = createInputDefinition();
    const result = collectPluginFormKitInputs(
      [
        createPluginSource("plugin-a", {
          formkit: {
            inputs: {
              select: pluginInput,
            },
          },
        }),
      ],
      {
        select: builtinInput,
      }
    );

    expect(result).toEqual({});
    expect(warnSpy).toHaveBeenCalledWith(
      expect.stringContaining("conflicts with Halo built-in inputs")
    );
  });

  it("keeps the first plugin input when plugin input names conflict", () => {
    const firstInput = createInputDefinition();
    const secondInput = createInputDefinition();
    const result = collectPluginFormKitInputs(
      [
        createPluginSource("plugin-a", {
          formkit: {
            inputs: {
              sharedInput: firstInput,
            },
          },
        }),
        createPluginSource("plugin-b", {
          formkit: {
            inputs: {
              sharedInput: secondInput,
            },
          },
        }),
      ],
      {}
    );

    expect(result).toEqual({
      sharedInput: firstInput,
    });
    expect(warnSpy).toHaveBeenCalledWith(
      expect.stringContaining('conflicts with plugin "plugin-a"')
    );
  });

  it("skips invalid plugin FormKit input shapes", () => {
    const result = collectPluginFormKitInputs(
      [
        createPluginSource("plugin-a", {
          formkit: "invalid",
        } as unknown as PluginModule),
        createPluginSource("plugin-b", {
          formkit: {
            inputs: [],
          },
        } as unknown as PluginModule),
        createPluginSource("plugin-c", {
          formkit: {
            inputs: {
              invalidInput: {
                type: "input",
              },
            },
          },
        } as unknown as PluginModule),
      ],
      {} as FormKitLibrary
    );

    expect(result).toEqual({});
    expect(warnSpy).toHaveBeenCalledTimes(3);
  });
});

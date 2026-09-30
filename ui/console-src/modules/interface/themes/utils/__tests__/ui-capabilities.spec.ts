import type { PluginModule } from "@halo-dev/ui-shared";
import { describe, expect, it } from "vite-plus/test";
import {
  collectRouteDeclarations,
  hasDeclaredCapabilities,
  summarizePluginModule,
} from "../ui-capabilities";

describe("ui capabilities helpers", () => {
  it("collects standalone and appended route declarations including children", () => {
    const declarations = collectRouteDeclarations([
      { path: "/foo", name: "Foo" },
      {
        parentName: "Tools",
        route: {
          path: "bar",
          name: "Bar",
          children: [{ path: "baz", name: "Baz" }],
        },
      },
    ]);
    expect(declarations).toEqual([
      { name: "Foo", path: "/foo", parentName: undefined },
      { name: "Bar", path: "bar", parentName: "Tools" },
      { name: "Baz", path: "baz", parentName: undefined },
    ]);
  });

  it("returns empty declarations for missing routes", () => {
    expect(collectRouteDeclarations(undefined)).toEqual([]);
  });

  it("summarizes a module without invoking extension callbacks", () => {
    const extensionCallback = () => {
      throw new Error("must not be called");
    };
    const module: PluginModule = {
      routes: [{ path: "/foo", name: "Foo" }],
      ucRoutes: [{ parentName: "UcRoot", route: { path: "uc", name: "Uc" } }],
      components: { FooCard: {} },
      extensionPoints: { "editor:create": extensionCallback },
      formkit: { inputs: { foo: {} as never } },
    };
    const summary = summarizePluginModule(module);
    expect(summary.consoleRoutes).toHaveLength(1);
    expect(summary.ucRoutes[0].parentName).toBe("UcRoot");
    expect(summary.components).toEqual(["FooCard"]);
    expect(summary.extensionPoints).toEqual(["editor:create"]);
    expect(summary.formkitInputs).toEqual(["foo"]);
    expect(hasDeclaredCapabilities(summary)).toBe(true);
  });

  it("reports an empty module as having no declared capabilities", () => {
    expect(hasDeclaredCapabilities(summarizePluginModule(undefined))).toBe(
      false
    );
  });
});

import crypto from "node:crypto";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import {
  createGlobalBridgeSource,
  selectHostRuntimeSnapshotFile,
  setupLibraryExternal,
} from "../library-external";

describe("ESM shared runtime", () => {
  afterEach(() => {
    delete (globalThis as Record<string, unknown>).TestSharedRuntime;
  });

  it("generates static named and default exports from a global", () => {
    const source = createGlobalBridgeSource({
      exports: ["create", "default"],
      runtime: { bridge: "axios", global: "axios" },
    });

    expect(source).toContain('globalThis["axios"]');
    expect(source).toContain(
      'export const create = __haloSharedRuntime["create"]'
    );
    expect(source).toContain("export default __haloSharedRuntime.default");
    expect(source).toContain("TODO(Halo 3)");
    expect(source).not.toMatch(/\beval\(|new Function/);
  });

  it("preserves runtime identity through a generated bridge", async () => {
    const identity = () => "same";
    (globalThis as Record<string, unknown>).TestSharedRuntime = { identity };
    const source = createGlobalBridgeSource({
      exports: ["identity"],
      runtime: { bridge: "test", global: "TestSharedRuntime" },
    });

    const bridge = await import(
      `data:text/javascript;base64,${Buffer.from(source).toString("base64")}#${crypto.randomUUID()}`
    );

    expect(bridge.identity).toBe(identity);
  });

  it("fails before evaluating a bridge with unavailable named exports", async () => {
    (globalThis as Record<string, unknown>).TestSharedRuntime = {};
    const source = createGlobalBridgeSource({
      exports: ["missing"],
      runtime: { bridge: "test", global: "TestSharedRuntime" },
    });

    await expect(
      import(
        `data:text/javascript;base64,${Buffer.from(source).toString("base64")}#${crypto.randomUUID()}`
      )
    ).rejects.toThrow("missing export(s): missing");
  });

  it("selects the latest eligible sparse host snapshot", () => {
    expect(
      selectHostRuntimeSnapshotFile("2.27.0", [
        "halo-2.26.0.json",
        "halo-3.0.0.json",
      ])
    ).toBe("halo-2.26.0.json");
  });

  it("injects all ten development mappings before module entries", () => {
    const plugin = getRuntimePlugin("serve", 3456);
    const transform = plugin.transformIndexHtml;
    if (!transform || typeof transform === "function") {
      throw new Error("Expected object transformIndexHtml hook");
    }
    const tags = transform.handler("", {} as never) as Array<{
      attrs: { type: string };
      children: string;
      injectTo: string;
    }>;
    const importMap = JSON.parse(tags[0].children);

    expect(tags[0]).toMatchObject({
      attrs: { type: "importmap" },
      injectTo: "head-prepend",
    });
    expect(Object.keys(importMap.imports)).toHaveLength(10);
    expect(importMap.imports.vue).toMatch(
      /^http:\/\/localhost:3456\/ui-assets\/esm-runtime\/vue\.[a-f0-9]{8}\.mjs$/
    );
    expect(importMap.imports["@formkit/core"]).toContain("formkit-core.");
  });

  it("uses root-relative production bridge URLs and emits hashed assets", () => {
    const plugin = getRuntimePlugin("build");
    const emitted: Array<{ fileName?: string; source?: string | Uint8Array }> =
      [];
    if (typeof plugin.buildStart !== "function") {
      throw new Error("Expected buildStart hook");
    }
    plugin.buildStart.call({
      emitFile(file: { fileName?: string; source?: string | Uint8Array }) {
        emitted.push(file);
        return "asset";
      },
    } as never);

    expect(emitted).toHaveLength(10);
    expect(emitted.map((asset) => asset.fileName)).toContainEqual(
      expect.stringMatching(/^ui-assets\/esm-runtime\/vue\.[a-f0-9]{8}\.mjs$/)
    );
  });

  it("serves development bridges with module MIME and CORS headers", () => {
    const plugin = getRuntimePlugin("serve");
    let middleware:
      | ((
          request: { url?: string },
          response: {
            statusCode: number;
            setHeader: ReturnType<typeof vi.fn>;
            end: ReturnType<typeof vi.fn>;
          },
          next: () => void
        ) => void)
      | undefined;
    if (typeof plugin.configureServer !== "function") {
      throw new Error("Expected configureServer hook");
    }
    plugin.configureServer({
      middlewares: {
        use(handler: typeof middleware) {
          middleware = handler;
        },
      },
    } as never);
    const response = {
      statusCode: 0,
      setHeader: vi.fn(),
      end: vi.fn(),
    };
    const next = vi.fn();
    const vueUrl = getImportMap(plugin).imports.vue as string;

    middleware?.({ url: new URL(vueUrl).pathname }, response, next);

    expect(response.statusCode).toBe(200);
    expect(response.setHeader).toHaveBeenCalledWith(
      "Content-Type",
      "text/javascript; charset=utf-8"
    );
    expect(response.setHeader).toHaveBeenCalledWith(
      "Access-Control-Allow-Origin",
      "*"
    );
    expect(response.end).toHaveBeenCalledWith(
      expect.stringContaining("export")
    );
    expect(next).not.toHaveBeenCalled();
  });
});

function getRuntimePlugin(command: "serve" | "build", port?: number) {
  const plugin = setupLibraryExternal(command, port).find(
    (candidate) => candidate && candidate.name === "halo:esm-shared-runtime"
  );
  if (!plugin || Array.isArray(plugin)) {
    throw new Error("ESM runtime plugin not found");
  }
  return plugin;
}

function getImportMap(plugin: ReturnType<typeof getRuntimePlugin>) {
  const transform = plugin.transformIndexHtml;
  if (!transform || typeof transform === "function") {
    throw new Error("Expected object transformIndexHtml hook");
  }
  const tags = transform.handler("", {} as never) as Array<{
    children: string;
  }>;
  return JSON.parse(tags[0].children) as {
    imports: Record<string, unknown>;
  };
}

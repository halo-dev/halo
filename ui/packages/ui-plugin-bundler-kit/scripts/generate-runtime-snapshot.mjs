import fs from "node:fs";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { TextDecoder, TextEncoder } from "node:util";
import vm from "node:vm";
import { resolveModulePath } from "exsolve";

const packageRoot = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  ".."
);
const projectRoot = path.resolve(packageRoot, "../..");
const packageJson = JSON.parse(
  fs.readFileSync(path.join(packageRoot, "package.json"), "utf8")
);
const haloVersion = packageJson.version;
if (!/^\d+\.\d+\.\d+$/.test(haloVersion)) {
  throw new Error(
    `Bundler-kit version ${haloVersion} is not a stable Halo snapshot version.`
  );
}
const snapshotsDir = path.join(packageRoot, "src/runtime-snapshots");
const outputFile = path.join(snapshotsDir, `halo-${haloVersion}.json`);
const indexFile = path.join(snapshotsDir, "index.ts");
const check = process.argv.includes("--check");

const definitions = {
  vue: ["vue", "Vue", "singleton"],
  "vue-router": ["vue-router", "VueRouter", "singleton"],
  pinia: ["pinia", "Pinia", "singleton"],
  axios: ["axios", "axios", "shared"],
  "@formkit/vue": ["formkit-vue", "FormKitVue", "singleton"],
  "@formkit/core": ["formkit-core", "FormKitCore", "singleton"],
  "@halo-dev/ui-shared": ["ui-shared", "HaloUiShared", "shared"],
  "@halo-dev/components": ["components", "HaloComponents", "shared"],
  "@halo-dev/api-client": ["api-client", "HaloApiClient", "shared"],
  "@halo-dev/richtext-editor": ["richtext-editor", "RichTextEditor", "shared"],
};

const browserRuntimeRoots = new Set([
  "vue",
  "vue-router",
  "pinia",
  "axios",
  "@halo-dev/ui-shared",
  "@halo-dev/components",
  "@halo-dev/api-client",
  "@halo-dev/richtext-editor",
]);
const browserRuntimeGlobals = loadBrowserRuntimeGlobals();

const packages = {};
for (const [root, [bridge, global, identity]] of Object.entries(definitions)) {
  const entry = fs.realpathSync(
    resolveModulePath(root, {
      from: path.join(projectRoot, "package.json"),
      conditions: ["node", "import"],
    })
  );
  const owningRoot = findOwningPackageRoot(root, entry);
  const dependencyPackageJson = JSON.parse(
    fs.readFileSync(path.join(owningRoot, "package.json"), "utf8")
  );
  const runtimeModule = await import(pathToFileURL(entry).href);
  const moduleExports = Object.keys(runtimeModule).filter(
    (name) => /^[$A-Z_a-z][$\w]*$/.test(name) && name !== "__esModule"
  );
  const browserGlobal = browserRuntimeRoots.has(root)
    ? browserRuntimeGlobals[global]
    : runtimeModule;
  if (!browserGlobal) {
    throw new Error(`Browser runtime did not expose ${global} for ${root}.`);
  }
  const missingExports = moduleExports.filter(
    (name) =>
      name !== "default" &&
      !Object.prototype.hasOwnProperty.call(browserGlobal, name)
  );
  if (missingExports.length > 0) {
    throw new Error(
      `Browser runtime ${global} is missing ${root} export(s): ${missingExports.join(", ")}.`
    );
  }
  packages[root] = {
    version: dependencyPackageJson.version,
    exports: moduleExports.sort(),
    runtime: { bridge, global, identity },
  };
}

const generatedSnapshot = `${JSON.stringify(
  { haloVersion, packages },
  null,
  2
)}\n`;
const snapshotFiles = [
  ...new Set([...listSnapshotFiles(), path.basename(outputFile)]),
].sort(compareSnapshotFile);
const generatedIndex = generateIndex(snapshotFiles);

if (check) {
  checkFile(outputFile, generatedSnapshot);
  checkFile(indexFile, generatedIndex);
} else {
  fs.mkdirSync(snapshotsDir, { recursive: true });
  fs.writeFileSync(outputFile, generatedSnapshot);
  fs.writeFileSync(indexFile, generatedIndex);
}

function loadBrowserRuntimeGlobals() {
  class NodeStub {}
  class ElementStub extends NodeStub {}
  class HTMLElementStub extends ElementStub {}
  class SVGElementStub extends ElementStub {}
  const createElement = () =>
    Object.assign(new HTMLElementStub(), {
      style: {},
      classList: { add() {}, remove() {} },
      appendChild() {},
      removeChild() {},
      setAttribute() {},
      getAttribute() {
        return null;
      },
      querySelector() {
        return null;
      },
      querySelectorAll() {
        return [];
      },
    });
  const document = {
    createElement,
    createElementNS: createElement,
    createTextNode: () => new NodeStub(),
    querySelector: () => null,
    querySelectorAll: () => [],
    documentElement: createElement(),
    head: createElement(),
    body: createElement(),
    addEventListener() {},
    removeEventListener() {},
  };
  const context = {
    console,
    setTimeout,
    clearTimeout,
    setInterval,
    clearInterval,
    URL,
    URLSearchParams,
    TextDecoder,
    TextEncoder,
    location: new URL("http://localhost/"),
    navigator: { userAgent: "node" },
    document,
    Node: NodeStub,
    Element: ElementStub,
    HTMLElement: HTMLElementStub,
    SVGElement: SVGElementStub,
    MutationObserver: class {
      observe() {}
      disconnect() {}
    },
    getComputedStyle: () => ({}),
    addEventListener() {},
    removeEventListener() {},
  };
  context.window = context;
  context.self = context;
  context.globalThis = context;
  vm.createContext(context);

  for (const runtimeFile of [
    "vue/dist/vue.global.prod.js",
    "vue-router/dist/vue-router.global.prod.js",
    "pinia/dist/pinia.iife.prod.js",
    "axios/dist/axios.min.js",
    "vue-demi/lib/index.iife.js",
    "@vueuse/shared/dist/index.iife.min.js",
    "@vueuse/core/dist/index.iife.min.js",
    "@vueuse/components/dist/index.iife.min.js",
    "@vueuse/router/dist/index.iife.min.js",
    "@halo-dev/api-client/dist/index.iife.js",
    "@halo-dev/ui-shared/dist/index.iife.js",
    "@halo-dev/components/dist/index.iife.js",
    "@halo-dev/richtext-editor/dist/index.iife.js",
  ]) {
    const runtimePath = path.join(projectRoot, "node_modules", runtimeFile);
    vm.runInContext(fs.readFileSync(runtimePath, "utf8"), context, {
      filename: runtimePath,
    });
  }
  return context;
}

function listSnapshotFiles() {
  if (!fs.existsSync(snapshotsDir)) {
    return [];
  }
  return fs
    .readdirSync(snapshotsDir)
    .filter((name) => /^halo-\d+\.\d+\.\d+\.json$/.test(name));
}

function compareSnapshotFile(left, right) {
  return extractVersion(left).localeCompare(extractVersion(right), undefined, {
    numeric: true,
  });
}

function extractVersion(filename) {
  return filename.slice("halo-".length, -".json".length);
}

function generateIndex(files) {
  const imports = files.map((filename) => {
    const version = extractVersion(filename);
    return `import halo_${version.replaceAll(".", "_")} from "./${filename}";`;
  });
  const values = files.map(
    (filename) => `halo_${extractVersion(filename).replaceAll(".", "_")}`
  );
  return [
    "// Generated by scripts/generate-runtime-snapshot.mjs. Do not edit manually.",
    ...imports,
    "",
    `export const rawHaloHostRuntimeSnapshots = [${values.join(", ")}];`,
    "",
  ].join("\n");
}

function checkFile(file, expected) {
  const current = fs.existsSync(file) ? fs.readFileSync(file, "utf8") : "";
  if (current !== expected) {
    throw new Error(
      `Host runtime snapshot is stale. Run: pnpm --filter @halo-dev/ui-plugin-bundler-kit snapshot:generate`
    );
  }
}

function findOwningPackageRoot(expectedName, resolvedEntry) {
  let current = path.dirname(resolvedEntry);
  while (true) {
    const packageJsonPath = path.join(current, "package.json");
    if (fs.existsSync(packageJsonPath)) {
      const candidatePackageJson = JSON.parse(
        fs.readFileSync(packageJsonPath, "utf8")
      );
      if (candidatePackageJson.name === expectedName) {
        return current;
      }
    }
    const parent = path.dirname(current);
    if (parent === current) {
      throw new Error(`Cannot locate package root for ${expectedName}.`);
    }
    current = parent;
  }
}

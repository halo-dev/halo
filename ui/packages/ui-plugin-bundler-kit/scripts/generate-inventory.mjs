import fs from "node:fs";
import path from "node:path";
import { pathToFileURL } from "node:url";

const packageRoot = path.resolve(import.meta.dirname, "..");
const projectRoot = path.resolve(packageRoot, "../..");
const outputFile = path.join(packageRoot, "src/inventories/halo-2.26.0.json");
const check = process.argv.includes("--check");

const definitions = {
  vue: [">=3.2.0 <4", "vue", "Vue", "singleton"],
  "vue-router": [">=4 <6", "vue-router", "VueRouter", "singleton"],
  pinia: [">=2 <4", "pinia", "Pinia", "singleton"],
  axios: [">=1 <2", "axios", "axios", "shared"],
  "@formkit/vue": [">=1 <3", "formkit-vue", "FormKitVue", "singleton"],
  "@formkit/core": [">=1 <3", "formkit-core", "FormKitCore", "singleton"],
  "@halo-dev/ui-shared": [">=2 <3", "ui-shared", "HaloUiShared", "shared"],
  "@halo-dev/components": [">=2 <3", "components", "HaloComponents", "shared"],
  "@halo-dev/api-client": [">=2 <3", "api-client", "HaloApiClient", "shared"],
  "@halo-dev/richtext-editor": [
    ">=2 <3",
    "richtext-editor",
    "RichTextEditor",
    "shared",
  ],
};

const projectUrl = pathToFileURL(path.join(projectRoot, "package.json")).href;
const packages = {};
for (const [root, [range, bridge, global, identity]] of Object.entries(
  definitions
)) {
  const entry = fs.realpathSync(
    new URL(import.meta.resolve(root, projectUrl)).pathname
  );
  const owningRoot = findOwningPackageRoot(root, entry);
  const packageJson = JSON.parse(
    fs.readFileSync(path.join(owningRoot, "package.json"), "utf8")
  );
  const runtimeModule = await import(pathToFileURL(entry).href);
  packages[root] = {
    version: packageJson.version,
    range,
    exports: Object.keys(runtimeModule)
      .filter((name) => /^[$A-Z_a-z][$\w]*$/.test(name))
      .sort(),
    runtime: { bridge, global, identity },
  };
}

const generated = `${JSON.stringify(
  { haloVersion: "2.26.0", packages },
  null,
  2
)}\n`;
if (check) {
  const current = fs.existsSync(outputFile)
    ? fs.readFileSync(outputFile, "utf8")
    : "";
  if (current !== generated) {
    throw new Error(
      `Inventory is stale. Run: pnpm --filter @halo-dev/ui-plugin-bundler-kit inventory:generate`
    );
  }
} else {
  fs.mkdirSync(path.dirname(outputFile), { recursive: true });
  fs.writeFileSync(outputFile, generated);
}

function findOwningPackageRoot(expectedName, resolvedEntry) {
  let current = path.dirname(resolvedEntry);
  while (true) {
    const packageJsonPath = path.join(current, "package.json");
    if (fs.existsSync(packageJsonPath)) {
      const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, "utf8"));
      if (packageJson.name === expectedName) {
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

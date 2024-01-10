import type { PluginModule } from "packages/shared/dist";

const modules = Object.values(
  import.meta.glob("./**/module.ts", {
    eager: true,
    import: "default",
  })
) as PluginModule[];

export default modules;

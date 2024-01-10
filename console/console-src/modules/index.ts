import type { PluginModule } from "@halo-dev/console-shared";

const modules = Object.values(
  import.meta.glob("./**/module.ts", {
    eager: true,
    import: "default",
  })
) as PluginModule[];

export default modules;

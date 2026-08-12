import { rsbuildConfig as createRsbuildConfig } from "./rsbuild";
import { viteConfig as createViteConfig } from "./vite";

export { HaloUIPluginBundlerKit } from "./legacy";

/**
 * @deprecated Import from `@halo-dev/ui-plugin-bundler-kit/rsbuild` instead.
 */
export const rsbuildConfig = createRsbuildConfig;

/**
 * @deprecated Import from `@halo-dev/ui-plugin-bundler-kit/vite` instead.
 */
export const viteConfig = createViteConfig;

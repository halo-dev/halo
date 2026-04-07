import type { Plugin } from "vite-plus";

export function devPlugin({ port }: { port: number }): Plugin {
  const DEV_SERVER_ORIGIN = `http://localhost:${port}`;

  return {
    name: "vite-dev-absolute-urls",
    apply: "serve",
    transformIndexHtml: {
      order: "post" as const,
      handler: (html: string) =>
        html.replace(/ (src|href)="(\/.+?)"/g, ` $1="${DEV_SERVER_ORIGIN}$2"`),
    },
  };
}

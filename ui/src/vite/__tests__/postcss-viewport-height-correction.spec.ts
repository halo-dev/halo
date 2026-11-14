import { createRequire } from "node:module";
import postcss from "postcss";
import { describe, expect, it } from "vite-plus/test";

const require = createRequire(import.meta.url);
const viewportHeightCorrection = require("../postcss-viewport-height-correction.cjs");

describe("postcss viewport height correction", () => {
  it("preserves the source and importance of cloned declarations", async () => {
    const result = await postcss([viewportHeightCorrection]).process(
      ".modal { height: 100vh !important; }",
      { from: "fixture.css" }
    );
    const declarations = result.root.nodes[0].nodes;

    expect(declarations).toHaveLength(2);
    expect(declarations[1]).toMatchObject({
      important: true,
      value: "calc(var(--vh, 1vh) * 100)",
    });
    expect(declarations[1].source?.input.file).toBe(
      declarations[0].source?.input.file
    );
  });
});

import { describe, expect, it } from "vite-plus/test";
import { AttachmentUtils } from "../attachment";

describe("AttachmentUtils", () => {
  describe("handleThumbnailError", () => {
    it("should fall back to the original URL and allow falling back again after src changes", () => {
      const utils = new AttachmentUtils();
      let src =
        "/apis/api.storage.halo.run/v1alpha1/thumbnails/-/via-uri?uri=a";
      let writes = 0;
      const target = {
        get src() {
          return src;
        },
        set src(value: string) {
          writes++;
          src = value;
        },
        getAttribute: (name: string) => (name === "src" ? src : null),
      };
      const event = { target } as unknown as Event;

      utils.handleThumbnailError(event, "https://external.com/a.png");

      expect(src).toBe("https://external.com/a.png");
      expect(writes).toBe(1);

      // The original image also failed: keep it and do not write again.
      utils.handleThumbnailError(event, "https://external.com/a.png");

      expect(src).toBe("https://external.com/a.png");
      expect(writes).toBe(1);

      // The same element was reused for another image.
      src = "/apis/api.storage.halo.run/v1alpha1/thumbnails/-/via-uri?uri=b";
      utils.handleThumbnailError(event, "https://external.com/b.png");

      expect(src).toBe("https://external.com/b.png");
      expect(writes).toBe(2);
    });

    it("should ignore events without an image target", () => {
      const utils = new AttachmentUtils();

      expect(() =>
        utils.handleThumbnailError(
          { target: null } as unknown as Event,
          "https://external.com/a.png"
        )
      ).not.toThrow();
    });
  });
});

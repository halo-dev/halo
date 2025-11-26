import { Extension, type Extensions } from "@/tiptap";

export interface StarterKitOptions {}

export const StarterKit = Extension.create<StarterKitOptions>({
  name: "halo-starter-kit",
  addExtensions() {
    const extensions: Extensions = [];

    return extensions;
  },
});

import TiptapLink from "@tiptap/extension-link";
import type { LinkOptions } from "@tiptap/extension-link";
import type { ExtensionOptions } from "@/types";

const Link = TiptapLink.extend<ExtensionOptions & LinkOptions>();

export default Link;

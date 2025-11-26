import { i18n } from "@/locales";
import { Placeholder } from "@tiptap/extensions";

export const ExtensionPlaceholder = Placeholder.configure({
  placeholder: i18n.global.t("editor.extensions.commands_menu.placeholder"),
});

import type { Ref } from "vue";
import { inject } from "vue";
import type {
  Type,
  useDialogOptions,
  useDialogUserOptions,
} from "@/components/dialog/interface";
import { DialogProviderProvideKey } from "@/components/dialog/interface";

interface useDialogReturn {
  success: (options: useDialogUserOptions) => void;
  info: (options: useDialogUserOptions) => void;
  warning: (options: useDialogUserOptions) => void;
  error: (options: useDialogUserOptions) => void;
}

export function useDialog(): useDialogReturn {
  const dialogOptions = inject<Ref<useDialogOptions>>(DialogProviderProvideKey);

  if (!dialogOptions) {
    throw new Error("DialogProvider is not mounted");
  }

  const createDialog = (type: Type) => (options: useDialogUserOptions) => {
    dialogOptions.value = { ...dialogOptions.value, ...options };
    dialogOptions.value.type = type;
    dialogOptions.value.visible = true;
  };

  return {
    success: createDialog("success"),
    info: createDialog("info"),
    warning: createDialog("warning"),
    error: createDialog("error"),
  };
}

import { slugify } from "transliteration";
import { watch, type Ref } from "vue";
import ShortUniqueId from "short-unique-id";
import { FormType } from "@/types/slug";
import { useGlobalInfoStore } from "@/stores/global-info";
import { randomUUID } from "@/utils/id";
const uid = new ShortUniqueId();
const Strategy = {
  generateByTitle: (value: string) => {
    if (!value) return "";
    return slugify(value, { trim: true });
  },
  shortUUID: (value: string) => {
    if (!value) return "";
    return uid.randomUUID(8);
  },
  UUID: (value: string) => {
    if (!value) return "";
    return randomUUID();
  },
  timestamp: (value: string) => {
    if (!value) return "";
    return new Date().getTime().toString();
  },
};

const onceList = ["shortUUID", "UUID", "timestamp"];

export default function useSlugify(
  source: Ref<string>,
  target: Ref<string>,
  auto: Ref<boolean>,
  formType: FormType
) {
  const handleGenerateSlug = (forceUpdate = false, formType: FormType) => {
    const globalInfoStore = useGlobalInfoStore();
    const mode = globalInfoStore.globalInfo?.postSlugGenerationStrategy;

    if (!mode) {
      return;
    }
    if (formType != FormType.POST) {
      target.value = Strategy["generateByTitle"](source.value);
      return;
    }
    if (forceUpdate) {
      target.value = Strategy[mode](source.value);
      return;
    }
    if (onceList.includes(mode) && target.value) return;
    target.value = Strategy[mode](source.value);
  };

  watch(
    () => source.value,
    () => {
      if (auto.value) {
        handleGenerateSlug(false, formType);
      }
    },
    {
      immediate: true,
    }
  );

  return {
    handleGenerateSlug,
  };
}

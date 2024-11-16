import { useGlobalInfoStore } from "@/stores/global-info";
import { FormType } from "@/types/slug";
import { randomUUID } from "@/utils/id";
import ShortUniqueId from "short-unique-id";
import { slugify } from "transliteration";
import { computed, watch, type Ref } from "vue";

const uid = new ShortUniqueId();

type SlugStrategy = (value?: string) => string;

const strategies: Record<string, SlugStrategy> = {
  generateByTitle: (value?: string) => slugify(value || "", { trim: true }),
  shortUUID: () => uid.randomUUID(8),
  UUID: () => randomUUID(),
  timestamp: () => new Date().getTime().toString(),
};

const onceStrategies = new Set(["shortUUID", "UUID", "timestamp"]);

export default function useSlugify(
  source: Ref<string>,
  target: Ref<string>,
  auto: Ref<boolean>,
  formType: FormType
) {
  const globalInfoStore = useGlobalInfoStore();

  const currentStrategy = computed(
    () =>
      globalInfoStore.globalInfo?.postSlugGenerationStrategy ||
      "generateByTitle"
  );

  const generateSlug = (value: string): string => {
    const strategy =
      formType === FormType.POST
        ? strategies[currentStrategy.value]
        : strategies.generateByTitle;

    return strategy(value);
  };

  const handleGenerateSlug = (forceUpdate = false) => {
    if (
      !forceUpdate &&
      onceStrategies.has(currentStrategy.value) &&
      target.value
    ) {
      return;
    }

    target.value = generateSlug(source.value);
  };

  watch(
    source,
    () => {
      if (auto.value) {
        handleGenerateSlug(true);
      }
    },
    { immediate: true }
  );

  return {
    handleGenerateSlug,
  };
}

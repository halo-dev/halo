<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { computed, ref, watch } from "vue";
import type { MenuItem, Post, SinglePage } from "@halo-dev/api-client";
import { v4 as uuid } from "uuid";
import { apiClient } from "@/utils/api-client";
import { reset } from "@formkit/core";
import cloneDeep from "lodash.clonedeep";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";
import { usePostTag } from "@/modules/contents/posts/tags/composables/use-post-tag";
import { setFocus } from "@/formkit/utils/focus";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    menuItem: MenuItem | null;
  }>(),
  {
    visible: false,
    menuItem: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", menuItem: MenuItem): void;
}>();

const initialFormState: MenuItem = {
  spec: {
    displayName: "",
    href: "",
    children: [],
    priority: 0,
  },
  apiVersion: "v1alpha1",
  kind: "MenuItem",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<MenuItem>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const handleSaveMenuItem = async () => {
  try {
    saving.value = true;

    const menuItemSource = menuItemSources.find(
      (source) => source.value === selectedMenuItemSource.value
    );

    if (menuItemSource) {
      const { ref } = menuItemSource;
      if (ref) {
        formState.value.spec[ref] = {
          version: "content.halo.run/v1alpha1",
          kind: menuItemSource.kind,
          name: selectedRef.value as string,
        };
      }
    }

    if (isUpdateMode.value) {
      const { data } =
        await apiClient.extension.menuItem.updatev1alpha1MenuItem({
          name: formState.value.metadata.name,
          menuItem: formState.value,
        });
      onVisibleChange(false);
      emit("saved", data);
    } else {
      const { data } =
        await apiClient.extension.menuItem.createv1alpha1MenuItem({
          menuItem: formState.value,
        });
      onVisibleChange(false);
      emit("saved", data);
    }
  } catch (e) {
    console.error("Failed to create menu item", e);
  } finally {
    saving.value = false;
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  formState.value.metadata.name = uuid();
  reset("menuitem-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("displayNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.menuItem,
  (menuItem) => {
    if (menuItem) {
      formState.value = cloneDeep(menuItem);

      // Set Ref related
      const { postRef, categoryRef, tagRef, singlePageRef } =
        formState.value.spec;

      if (postRef) {
        selectedMenuItemSource.value = "post";
        selectedRef.value = postRef.name;
      }

      if (categoryRef) {
        selectedMenuItemSource.value = "category";
        selectedRef.value = categoryRef.name;
      }

      if (tagRef) {
        selectedMenuItemSource.value = "tag";
        selectedRef.value = tagRef.name;
      }

      if (singlePageRef) {
        selectedMenuItemSource.value = "singlePage";
        selectedRef.value = singlePageRef.name;
      }
    } else {
      handleResetForm();
    }
  }
);

// MenuItem Ref
interface MenuItemSource {
  label: string;
  value: string;
  ref?: "postRef" | "categoryRef" | "tagRef" | "singlePageRef";
  kind?: "Post" | "Category" | "Tag" | "SinglePage";
}

const menuItemSources: MenuItemSource[] = [
  {
    label: "自定义链接",
    value: "custom",
  },
  {
    label: "文章",
    value: "post",
    ref: "postRef",
    kind: "Post",
  },
  {
    label: "自定义页面",
    value: "singlePage",
    ref: "singlePageRef",
    kind: "SinglePage",
  },
  {
    label: "分类",
    value: "category",
    ref: "categoryRef",
    kind: "Post",
  },
  {
    label: "标签",
    value: "tag",
    ref: "tagRef",
    kind: "Tag",
  },
];

const selectedMenuItemSource = ref<string>(menuItemSources[0].value);

const { categories, handleFetchCategories } = usePostCategory();
const { tags, handleFetchTags } = usePostTag();
const posts = ref<Post[]>([] as Post[]);
const singlePages = ref<SinglePage[]>([] as SinglePage[]);

const postMap = computed(() => {
  return [
    { label: "请选择文章", value: undefined },
    ...posts.value.map((post) => {
      return {
        label: post.spec.title,
        value: post.metadata.name,
      };
    }),
  ];
});

const singlePageMap = computed(() => {
  return [
    {
      label: "请选择自定义页面",
      value: undefined,
    },
    ...singlePages.value.map((singlePage) => {
      return {
        label: singlePage.spec.title,
        value: singlePage.metadata.name,
      };
    }),
  ];
});

const categoryMap = computed(() => {
  return [
    {
      label: "请选择分类",
      value: undefined,
    },
    ...categories.value.map((category) => {
      return {
        label: category.spec.displayName,
        value: category.metadata.name,
      };
    }),
  ];
});

const tagMap = computed(() => {
  return [
    {
      label: "请选择标签",
      value: undefined,
    },
    ...tags.value.map((tag) => {
      return {
        label: tag.spec.displayName,
        value: tag.metadata.name,
      };
    }),
  ];
});

const selectedRef = ref<string>("");

const handleFetchPosts = async () => {
  const { data } =
    await apiClient.extension.post.listcontentHaloRunV1alpha1Post({
      page: 0,
      size: 0,
    });
  posts.value = data.items;
};

const handleFetchSinglePages = async () => {
  const { data } =
    await apiClient.extension.singlePage.listcontentHaloRunV1alpha1SinglePage({
      page: 0,
      size: 0,
    });
  singlePages.value = data.items;
};

const onMenuItemSourceChange = () => {
  selectedRef.value = "";
};

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      handleFetchCategories();
      handleFetchTags();
      handleFetchPosts();
      handleFetchSinglePages();
    }
  }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="编辑菜单项"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="menuitem-form"
      name="menuitem-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveMenuItem"
    >
      <FormKit
        v-model="selectedMenuItemSource"
        :options="menuItemSources"
        :disabled="isUpdateMode"
        label="类型"
        type="select"
        @change="onMenuItemSourceChange"
      >
      </FormKit>

      <FormKit
        v-if="selectedMenuItemSource === 'custom'"
        id="displayNameInput"
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        name="displayName"
        validation="required"
      ></FormKit>
      <FormKit
        v-if="selectedMenuItemSource === 'custom'"
        v-model="formState.spec.href"
        label="链接地址"
        type="text"
        name="href"
        validation="required"
      ></FormKit>

      <FormKit
        v-if="selectedMenuItemSource === 'post'"
        v-model="selectedRef"
        label="文章"
        type="select"
        :options="postMap"
        validation="required"
      ></FormKit>

      <FormKit
        v-if="selectedMenuItemSource === 'singlePage'"
        v-model="selectedRef"
        label="自定义页面"
        type="select"
        :options="singlePageMap"
        validation="required"
      ></FormKit>

      <FormKit
        v-if="selectedMenuItemSource === 'tag'"
        v-model="selectedRef"
        label="标签"
        type="select"
        :options="tagMap"
        validation="required"
      ></FormKit>

      <FormKit
        v-if="selectedMenuItemSource === 'category'"
        v-model="selectedRef"
        label="分类"
        type="select"
        :options="categoryMap"
        validation="required"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          type="secondary"
          @submit="$formkit.submit('menuitem-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

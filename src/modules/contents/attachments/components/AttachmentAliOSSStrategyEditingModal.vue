<script lang="ts" setup>
import { VAlert, VButton, VModal, VSpace } from "@halo-dev/components";

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:visible", "close"]);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="600"
    title="阿里云 OSS 存储策略编辑"
    @update:visible="onVisibleChange"
  >
    <VAlert class="mb-4" title="提示">
      <template #description>
        <p class="my-3 text-sm text-gray-600">
          阿里云 OSS 产品文档：<a
            href="https://help.aliyun.com/product/31815.html"
            target="_blank"
            >https://help.aliyun.com/product/31815.html</a
          >
        </p>
      </template>
    </VAlert>
    <FormKit id="alioss-strategy-form" type="form">
      <FormKit label="名称" type="text" validation="required"></FormKit>
      <FormKit label="Bucket" type="text" validation="required"></FormKit>
      <FormKit label="EndPoint" type="text" validation="required"></FormKit>
      <FormKit label="Access Key" type="text" validation="required"></FormKit>
      <FormKit
        label="Access Secret"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        label="上传目录"
        placeholder="如不填写，则默认上传到根目录"
        type="text"
      ></FormKit>
      <FormKit
        :options="[
          { label: 'HTTPS', value: 'https' },
          { label: 'HTTP', value: 'http' },
        ]"
        label="绑定域名协议"
        type="select"
      ></FormKit>
      <FormKit
        label="绑定域名"
        placeholder="如不设置，那么将使用 Bucket + EndPoint 作为域名"
        type="text"
      ></FormKit>
      <FormKit
        :options="[
          { label: '服务端上传', value: 'server' },
          { label: '客户端上传', value: 'client' },
        ]"
        label="上传方式"
        type="select"
      ></FormKit>
      <FormKit
        help="使用半角逗号分隔"
        label="允许上传的文件类型"
        type="textarea"
        value="jpg,png,gif"
      ></FormKit>
    </FormKit>

    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          @click="$formkit.submit('alioss-strategy-form')"
        >
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

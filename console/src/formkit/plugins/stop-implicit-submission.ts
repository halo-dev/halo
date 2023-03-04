import router from "@/router";
import type { FormKitNode } from "@formkit/core";
import { onMounted } from "vue";
import { useRoute } from "vue-router";
function handleKeydown(e: KeyboardEvent) {
  if (
    e.key == "Enter" &&
    e.altKey == false &&
    e.ctrlKey == false &&
    e.metaKey == false
  ) {
    e.stopImmediatePropagation();
    e.preventDefault();
    e.stopPropagation();
  }
}

// 以下表单键入enter引起表单提交，
//https://html.spec.whatwg.org/multipage/form-control-infrastructure.html#implicit-submission
const implicitSubmissionType = [
  "text",
  "search",
  "url",
  "email",
  "password",
  "date",
  "month",
  "week",
  "time",
  "datetime-local",
  "number",
];

const FormKeydownEventControllerMap = new Map<string, AbortController>();

const clearFormKeydownEventByPath = (fullPath: string) => {
  if (FormKeydownEventControllerMap.size) {
    FormKeydownEventControllerMap.forEach((controller, path) => {
      if (!fullPath.includes(path)) {
        controller.abort();
        FormKeydownEventControllerMap.delete(path);
      }
    });
  }
};

//https://developer.mozilla.org/zh-CN/docs/Web/API/EventTarget/addEventListener#%E5%8F%82%E6%95%B0
//使用AbortSignal来取消事件监听
const inputPreventFn = (node: FormKitNode) => {
  const id = node.props.id ?? "";
  if (node.type == "group") {
    onMounted(() => {
      const { path } = useRoute();
      let controller = FormKeydownEventControllerMap.get(path);
      if (
        node.children.length == 1 &&
        node.props.type == "form" &&
        implicitSubmissionType.includes(node.children[0].props.type)
      ) {
        if (!controller) {
          controller = new AbortController();
          FormKeydownEventControllerMap.set(path, controller);
        }
        const rootForm = document.getElementById(id);
        rootForm?.addEventListener("keydown", handleKeydown, {
          signal: controller.signal,
        });
      }
    });
  }
};

// 进入下一个页面，清除上一个页面的事件。
// 可以做个测试后面。https://segmentfault.com/q/1010000042671015
// map里面的key是path，这里传fullPath，当有多级路由的时候避免清掉上一级的事件。
router.beforeEach(({ fullPath }) => {
  clearFormKeydownEventByPath(fullPath);
});

export default inputPreventFn;

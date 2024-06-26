import type { FormKitNode } from "@formkit/core";
import { undefine } from "@formkit/utils";

export const lists = function (node: FormKitNode) {
  node._c.sync = true;
  node.on("created", listFeature.bind(null, node));
};

const fn = (node: FormKitNode): object | string | boolean | number => {
  switch (node.props.itemType.toLocaleLowerCase()) {
    case "object":
      return {};
    case "boolean":
      return false;
    case "number":
      return 0;
    default:
      return "";
  }
};

function createValue(num: number, node: FormKitNode) {
  return new Array(num).fill("").map(() => fn(node));
}

function listFeature(node: FormKitNode) {
  node.props.removeControl = node.props.removeControl ?? true;
  node.props.upControl = node.props.upControl ?? true;
  node.props.downControl = node.props.downControl ?? true;
  node.props.insertControl = node.props.insertControl ?? true;
  node.props.addButton = node.props.addButton ?? true;
  node.props.addLabel = node.props.addLabel ?? false;
  node.props.addAttrs = node.props.addAttrs ?? {};
  node.props.min = node.props.min ? Number(node.props.min) : 0;
  node.props.max = node.props.max ? Number(node.props.max) : Infinity;
  node.props.itemType = node.props.itemType ?? "string";
  if (node.props.min > node.props.max) {
    throw Error("list: min must be less than max");
  }

  if ("disabled" in node.props) {
    node.props.disabled = undefine(node.props.disabled);
  }

  if (Array.isArray(node.value)) {
    if (node.value.length < node.props.min) {
      const value = createValue(node.props.min - node.value.length, node);
      node.input(node.value.concat(value), false);
    } else {
      if (node.value.length > node.props.max) {
        node.input(node.value.slice(0, node.props.max), false);
      }
    }
  } else {
    node.input(createValue(node.props.min, node), false);
  }

  if (node.context) {
    const fns = node.context.fns;
    fns.createShift = (index: number, offset: number) => () => {
      const value = node._value as unknown[];
      value.splice(index + offset, 0, value.splice(index, 1)[0]),
        node.input(value, false);
    };
    fns.createInsert = (index: number) => () => {
      const value = node._value as unknown[];
      value.splice(index + 1, 0, fn(node)), node.input(value, false);
    };
    fns.createAppend = () => () => {
      const value = node._value as unknown[];
      console.log(fn(node));
      value.push(fn(node)), node.input(value, false);
    };
    fns.createRemover = (index: number) => () => {
      const value = node._value as unknown[];
      value.splice(index, 1), node.input(value, false);
    };
  }
}

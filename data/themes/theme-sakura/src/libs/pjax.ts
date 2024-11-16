/**
 * PJAX 刷新。
 * 对于 PJAX，最困难的问题在于不同脚本的刷新，页面切换之后，尽管大部分 PJAX 插件会提供自动执行脚本的功能。
 * 但此功能实际上需要各个网站定制化，因此需要将脚本分类，在 PJAX 的角度上大致能将脚本分为三类
 * 1、例如看板娘等全局页面都会存在的脚本，仅仅需要加载一次。
 * 2、例如网站访问量、AD 等每个页面都存在，并且 pjax 刷新的时候都需要重新加载的脚本。
 * 3、例如评论组件等仅在部分页面存在，不使用不加载的脚本。
 *
 * 对于上述脚本，每一类需要有每一类的处理方式。
 * 1、第一类脚本，通常需要将其放在非 PJAX 刷新区域之外，之后便不需要进行任何处理。只需保证在 PJAX 加载
 * 刷新区时，不重复加载即可。
 * 2、第二类脚本，例如 AD，如果用户从首页切换到某个目录，此时由于 PJAX 的原因，AD 并不会在第二个页面加载，
 * 此时的做法可以是重新调用目标 JS 的初始化，但此类脚本通常不包含初始化方法且种类繁多，处理极其麻烦。因此
 * 比较好的处理方式是重新加载此脚本的 script。如使用 replaceChild 重新替换脚本。
 * 3、第三类脚本，例如评论组件加载，当用户从首页进入到某个文章，评论组件并不会重新在此文章页面渲染，而且，
 * 通常文章会控制是否使用评论。此类脚本与第二类很相似，但不同点在于，此类组件可选并且只加载一次【全体】，
 * 而每个需要此插件的页面则只需重新执行初始化方法即可。因而此种方式也更为麻烦，因为不同的脚本具有不同的初始化
 * 方式。计划通过 Zero 的刷新事件来同步更新所有内联代码块的刷新功能。
 *
 * 脱离 Jquery，使用 https://github.com/MoOx/pjax
 */
import Pjax from "pjax";
import { sakura } from "../main";
import NProgress from "nprogress";
import { Util } from "../utils/util";

NProgress.configure({ trickle: false });
const pjax = new Pjax({
  elements: "a[data-pjax]",
  selectors: ["head title", ".wrapper", ".pjax"],
  switches: {
    ".wrapper": Pjax.switches.innerHTML,
  },
  analytics: false,
  cacheBust: false,
  debug: import.meta.env.MODE === "development" ? true : false,
});

// @ts-ignore
// 修复由于 pjax 导致的 Request Context 无法被拦截获取信息的问题。
pjax.doRequest = function (
  location,
  options: Pjax.IOptions,
  callback: (requestText: string | null, request: XMLHttpRequest, href: string, options?: Pjax.IOptions) => void
) {
  options = options || {};
  var queryString;
  var requestOptions = options.requestOptions || {};
  var requestMethod = (requestOptions.requestMethod || "GET").toUpperCase();
  var requestParams = requestOptions.requestParams || null;
  var formData = requestOptions.formData || null;
  var requestPayload = null;
  var request = new XMLHttpRequest();
  var timeout = options.timeout || 0;

  request.onreadystatechange = function () {
    if (request.readyState === 4) {
      if (request.status === 200) {
        callback(request.responseText, request, location, options);
      } else if (request.status !== 0) {
        callback(null, request, location, options);
      }
    }
  };

  request.onerror = function (e) {
    console.log(e);
    callback(null, request, location, options);
  };

  request.ontimeout = function () {
    callback(null, request, location, options);
  };

  if (requestParams && requestParams.length) {
    queryString = requestParams
      .map(function (param: { name: string; value: string }) {
        return param.name + "=" + param.value;
      })
      .join("&");

    switch (requestMethod) {
      case "GET":
        location = location.split("?")[0];

        location += "?" + queryString;
        break;

      case "POST":
        requestPayload = queryString;
        break;
    }
  } else if (formData) {
    requestPayload = formData;
  }

  if (options.cacheBust) {
    location = updateQueryString(location, "t", Date.now());
  }

  request.open(requestMethod, location, true);
  request.timeout = timeout;
  request.setRequestHeader("X-Requested-With", "XMLHttpRequest");
  request.setRequestHeader("X-PJAX", "true");
  request.setRequestHeader("X-PJAX-Selectors", JSON.stringify(options.selectors));
  request.setRequestHeader("accept", "text/html, application/json, text/plain, */*");
  request.withCredentials = true;

  // 发送 POST 表单
  if (requestPayload && requestMethod === "POST" && !formData) {
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
  }

  request.send(requestPayload);

  return request;
};

const updateQueryString = function (uri: string, key: string, value: number) {
  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
  var separator = uri.indexOf("?") !== -1 ? "&" : "?";
  if (uri.match(re)) {
    return uri.replace(re, "$1" + key + "=" + value + "$2");
  } else {
    return uri + separator + key + "=" + value;
  }
};

// 挂载 pjax 实例至全局
sakura.mountGlobalProperty("pjax", pjax);

// 使用代理的事件
const originalAddEventListener = EventTarget.prototype.addEventListener;
EventTarget.prototype.addEventListener = function (type, listener: any, options) {
  // 检查是否是 'DOMContentLoaded' 事件类型
  if (type === "DOMContentLoaded") {
    if (listener) {
      window.addEventListener(
        "pjax:success",
        () => {
          Util.retry(() => listener(), 10, 100);
        },
        {
          once: true,
        }
      );
      return;
    }
  }
  // 其他方法调用原始的 addEventListener 方法
  originalAddEventListener.call(this, type, listener, options);
};

window.addEventListener("pjax:send", () => {
  NProgress.start();
});

window.addEventListener("pjax:complete", () => {
  NProgress.done();
});

window.addEventListener("pjax:error", (event: any) => {
  const request = event.request as XMLHttpRequest;
  if (request.status === 404 || request.status === 500) {
    window.location.href = request.responseURL;
  }
});

window.addEventListener("pjax:success", () => {
  // 第二种脚本处理。对添加了 id=pjax 或者 data-pjax 的 script，重新添加到文档树
  let pjaxDoms = document.querySelectorAll("script[data-pjax]") as NodeListOf<HTMLScriptElement>;
  pjaxDoms.forEach((element) => {
    let code: string = element.text || element.textContent || element.innerHTML || "";
    let parent: ParentNode | null = element.parentNode;
    if (parent === null) {
      return;
    }
    parent.removeChild(element);
    let script: HTMLElementTagNameMap["script"] = document.createElement("script");
    if (element.id) {
      script.id = element.id;
    }
    if (element.className) {
      script.className = element.className;
    }
    if (element.type) {
      script.type = element.type;
    }
    if (element.src) {
      script.src = element.src;
      script.async = false;
    }
    if (element.dataset.pjax !== undefined) {
      script.dataset.pjax = "";
    }
    if (code !== "") {
      script.appendChild(document.createTextNode(code));
    }
    parent.appendChild(script);
  });
  // 第三种脚本处理方式，执行 sakura 的 refresh 方法来触发监听事件。
  sakura.refresh();
});

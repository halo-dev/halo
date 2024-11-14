import { Util } from "./utils/util";
import "./module/index";
import "./css/main.css";
import "@purge-icons/generated";
import Toast from "./utils/toast";
import i18next, { type TOptions } from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import Backend from "i18next-chained-backend";
import LocalStorageBackend from "i18next-localstorage-backend";
// @ts-ignore
import locI18next from "loc-i18next";
import { I18nFormat } from "./utils/i18nFormat";

/* 核心启动，通常不建议也不应当由用户调用，只能由启动代码使用  */
interface Sakura {
  [key: string]: any;
  getThemeConfig<T extends Number | String | Boolean | Array<ThemeConfig>>(
    group: String,
    key: String,
    type: new (...args: any) => T
  ): T | undefined;
  refresh(): void;
  registerDocumentFunction(documentFunction: DocumentFunction): void;
  translate(key: String, defaultValue: string, options?: TOptions): string;
  getPageConfig(key: string): any | undefined;
  mountGlobalProperty(key: string, value: any): void;
}

declare var Sakura: {
  prototype: Sakura;
  new (config?: object): Sakura;
};

export interface ThemeConfig {
  isEmpty(): Boolean;
  getValue<T extends Number | String | Boolean | ThemeConfig[]>(
    key: String,
    type: new (...args: any) => T
  ): T | undefined;
}

export class ThemeConfigImpl implements ThemeConfig {
  private schemas?: any;

  constructor(schemas?: any) {
    this.schemas = schemas;
  }

  isEmpty(): Boolean {
    return !this.schemas;
  }

  getValue<T extends Number | String | Boolean | ThemeConfig[]>(
    key: String,
    type: new (...args: any) => T
  ): T | undefined {
    if (this.isEmpty()) {
      return undefined;
    }
    if (!(key.toString() in this.schemas)) {
      return undefined;
    }
    return new type(this.schemas[key.toString()]);
  }
}

interface DocumentFunction {
  isRefresh: Boolean;

  target: any;

  name: String;

  method: Function;

  execute(): void;
}

class SakuraDocumentFunctionImpl implements DocumentFunction {
  isRefresh: Boolean;

  target: any;

  name: String;

  method: Function;

  execCount: Number = 0;

  constructor(target: any, name: String, method: Function, isRefresh: Boolean) {
    this.target = target;
    this.name = name;
    this.method = method;
    this.isRefresh = isRefresh;
  }

  execute(): void {
    if (!this.isRefresh) {
      if (this.execCount.valueOf() > 0) {
        return;
      }
    }
    this.method.call(this.target);
    this.execCount = this.execCount.valueOf() + 1;
  }
}

interface DocumentFunctionFactory {
  registerDocumentFunction(documentFunction: DocumentFunction): void;

  getDocumentFunction(name: String): DocumentFunction | undefined;

  getDocumentFunctions(): Set<DocumentFunction>;

  geDocumentFunctionCount(): Number;
}

class SakuraDocumentFunctionFactory implements DocumentFunctionFactory {
  private documentFunctions: Set<DocumentFunction>;

  constructor() {
    this.documentFunctions = new Set();
  }

  geDocumentFunctionCount(): Number {
    return this.documentFunctions.size;
  }

  getDocumentFunctions(): Set<DocumentFunction> {
    return this.documentFunctions;
  }

  registerDocumentFunction(documentFunction: DocumentFunction): void {
    this.documentFunctions.add(documentFunction);
  }

  getDocumentFunction(name: String): DocumentFunction | undefined {
    if (!name || !this.documentFunctions) {
      return undefined;
    }
    for (const documentFunction of this.documentFunctions) {
      if (documentFunction.name === name) {
        return documentFunction;
      }
    }
    return undefined;
  }
}

export class SakuraApp implements Sakura {
  private readonly config?: any;

  private themeconfigs: Map<String, ThemeConfig>;

  private currPageData: Map<String, any> = new Map();

  private documentFunctionFactory: DocumentFunctionFactory = new SakuraDocumentFunctionFactory();

  private events: Map<String, Event> = new Map();

  public readonly REFRESH_EVENT_NAME: String = "sakura:refresh";

  constructor(config?: any) {
    this.config = config;
    this.themeconfigs = new Map();
    this.refreshThemeConfig();
  }

  translate(key: String, defaultValue: string, options?: TOptions): string {
    if (sakura.$t) {
      return sakura.$t(key, { defaultValue: defaultValue, ...options });
    }
    return defaultValue;
  }

  private refreshThemeConfig() {
    if (!this.config) {
      return;
    }
    try {
      Object.keys(this.config).forEach((key) => {
        let themeConfig = new ThemeConfigImpl(this.config[key]);
        this.themeconfigs.set(key, themeConfig);
      });
    } catch (error) {
      console.error("解析 themeConfig 失败：", error);
    }
  }

  getThemeConfig<T extends Number | String | Boolean | ThemeConfig[]>(
    group: String,
    key: String,
    type: new (...args: any) => T
  ): T | undefined {
    let themeConfig = this.themeconfigs.get(group);
    if (!themeConfig) {
      return undefined;
    }

    return themeConfig.getValue(key, type);
  }

  getPageConfig(string: string): any | undefined {
    if (this.currPageData.has(string)) {
      return this.currPageData.get(string);
    }
    return undefined;
  }

  /**
   * 页面变化时，刷新 Sakura 所需更新的公共状态。
   *
   * <P>
   * 特别的，为了减少公共 JS 的大小，不建议在此方法内调用状态可变的功能刷新方法。例如可由用户开启或关闭的功能。
   * 此类开放功能可由内联代码使用 window 监听事件 "sakura:refresh" 来进行刷新,也可以放在 `lib` 目录下，之后
   * 在主题端使用 `script` 标签引入。
   * <P>
   */
  public refresh(): void {
    // 初始化刷新前置
    this.prepareRefresh();
    // 初始化事件广播器
    this.initEventMulticaster();
    // 挂载全局方法
    this.mountGlobalFunction();
    // 注册路由
    this.registerRoute();
    // 获取 Dom 函数工厂
    this.obtainFunctionFactory();
    // 注册预设 Dom 函数
    this.registerDomProcessors();
    // 处理所有 DocumentFunction
    this.finishDocumentFunction();
    // 结束刷新
    this.finishRefresh();
  }

  /**
   * 挂载全局属性至 sakura 中
   */
  public mountGlobalProperty(key: string, value: any) {
    const descriptor = Object.getOwnPropertyDescriptor(sakura, `$${key}`);
    if (descriptor) {
      return;
    }
    Object.defineProperty(sakura, `$${key}`, {
      value: value,
      writable: false,
      enumerable: false,
      configurable: false,
    });
    // TODO 触发属性挂载完成事件
  }

  protected mountGlobalFunction() {
    // 注册sakura toast 组件
    const isToast = sakura.getThemeConfig("toast", "open_toast", Boolean)?.valueOf();
    if (isToast && !Object.getOwnPropertyDescriptor(sakura, "$toast")) {
      Object.defineProperty(sakura, "$toast", {
        value: new Toast({
          width: sakura.getThemeConfig("toast", "toast_width", Number)?.valueOf(),
          height: sakura.getThemeConfig("toast", "toast_height", Number)?.valueOf(),
          top: sakura.getThemeConfig("toast", "toast_top", String)?.valueOf(),
          background: sakura.getThemeConfig("general", "theme_skin", String)?.valueOf(),
          color: sakura.getThemeConfig("toast", "toast_color", String)?.valueOf(),
          fontSize: sakura.getThemeConfig("toast", "toast_font_size", Number)?.valueOf(),
        }),
        writable: true,
        configurable: true,
        enumerable: false,
      });
    }

    // 挂载 i18n
    if (!Object.getOwnPropertyDescriptor(sakura, "$t")) {
      i18next
        .use(LanguageDetector)
        .use(Backend)
        .init({
          backend: {
            backends: [
              LocalStorageBackend,
              {
                type: "backend",
                read<Namespace>(
                  // @ts-ignore
                  language: LocaleCode,
                  // @ts-ignore
                  namespace: Namespace,
                  callback: (errorValue: unknown, translations: null | [Namespace]) => void
                ) {
                  import(`./languages/${language}.json`)
                    .then((resources) => {
                      callback(null, resources.default);
                    })
                    .catch((error) => {
                      callback(error, null);
                    });
                },
                init: () => {},
              },
            ],
            backendOptions: [
              {
                prefix: "i18next_sakura_",
                defaultVersion: sakura.getPageConfig("version"),
              },
            ],
          },
          debug: import.meta.env.MODE === "development" ? true : false,
          lowerCaseLng: true,
          cleanCode: true,
          interpolation: {
            format: function (value, format, lng, edit) {
              const params = edit?.params;
              switch (format) {
                case "datetimeFormat":
                  let options = params?.options;
                  if (!options) {
                    switch (sakura.getThemeConfig("general", "date_format", String)?.valueOf()) {
                      case "time":
                        options = {
                          hour: "numeric",
                          minute: "numeric",
                          second: "numeric",
                        };
                        break;
                      case "datetime":
                        options = {
                          year: "numeric",
                          month: "2-digit",
                          day: "2-digit",
                          hour: "numeric",
                          minute: "numeric",
                          second: "numeric",
                        };
                        break;
                      case "date":
                      default:
                        options = {
                          year: "numeric",
                          month: "2-digit",
                          day: "2-digit",
                        };
                        break;
                    }

                    if (sakura.getThemeConfig("general", "hour12", Boolean)?.valueOf()) {
                      options.hour12 = true;
                    }
                  }
                  return I18nFormat.DateTimeFormat(value, lng, options, params?.separator);
                case "relativeTimeFormat":
                  return I18nFormat.RelativeTimeFormat(value, lng);
                default:
                  return value;
              }
            },
            escapeValue: false,
          },
        })
        .then((t) => {
          let i18nReadyEvent = this.events.get("sakura:i18n");
          if (!i18nReadyEvent) {
            i18nReadyEvent = new CustomEvent("sakura:i18n");
            this.events.set("sakura:i18n", i18nReadyEvent);
          }
          window.dispatchEvent(i18nReadyEvent);
          Object.defineProperty(sakura, "$t", {
            value: t,
            writable: false,
            configurable: false,
            enumerable: false,
          });
          const localize = locI18next.init(i18next, {
            selectorAttr: "data-i18n",
            targetAttr: "i18n-target",
            optionsAttr: "i18n-options",
            useOptionsAttr: true,
            parseDefaultValueFromContent: true,
            document: window.document,
          });
          localize("[data-i18n]");
          Object.defineProperty(sakura, "$localize", {
            value: localize,
            writable: false,
            configurable: false,
            enumerable: false,
          });
        });
    } else {
      sakura.$localize("[data-i18n]");
    }
  }

  /**
   * 注册 documentFunction 函数，该类函数通常为动态加载。
   *
   * @param name 函数名
   * @param method 函数
   */
  public registerDocumentFunction(documentFunction: DocumentFunction): void {
    this.obtainFunctionFactory();
    this.documentFunctionFactory.registerDocumentFunction(documentFunction);
    documentFunction.execute();
  }

  protected finishDocumentFunction(): void {
    const functions = this.documentFunctionFactory.getDocumentFunctions();
    for (const documentFunction of functions) {
      documentFunction.execute();
    }
  }

  protected registerDomProcessors(): void {
    let initFuncitons = getInitDocumentFunctions();
    for (const documentFunction of initFuncitons) {
      const personObj = documentFunction as {
        target: any;
        propertyKey: string;
        method: Function;
        isRefresh: Boolean;
      };
      this.documentFunctionFactory.registerDocumentFunction(
        new SakuraDocumentFunctionImpl(personObj.target, personObj.propertyKey, personObj.method, personObj.isRefresh)
      );
    }
    initFuncitons.clear();
    if (this.getThemeConfig("advanced", "log", Boolean)?.valueOf()) {
      console.log("共获取预设 documentFunction " + functions.size + " 个");
    }
  }

  protected obtainFunctionFactory(): DocumentFunctionFactory {
    if (!this.documentFunctionFactory) {
      this.documentFunctionFactory = new SakuraDocumentFunctionFactory();
    }
    return this.documentFunctionFactory;
  }

  protected prepareRefresh(): void {
    this.refreshMetadata();

    if (this.getThemeConfig("advanced", "log", Boolean)?.valueOf()) {
      console.log("Sakura Refreshing");
    }
  }

  protected refreshMetadata() {
    try {
      this.currPageData = Util.jsonToMap<string, any>(pageData);
    } catch (error) {
      console.error("解析 pageData 失败：", error);
    }
  }

  protected async registerRoute() {
    const _templateId = this.currPageData.get("_templateId");
    if (!_templateId) {
      return;
    }
    // 也可以通过 `./page/index` 这类具体名称处理，优点是不需要加上 min.js，缺点是需要特殊处理
    const modulePath = `./page/${_templateId}.min.js`;
    await import(modulePath);
  }

  protected initEventMulticaster(): void {
    let refreshEvent = this.events.get(this.REFRESH_EVENT_NAME);
    if (!refreshEvent) {
      refreshEvent = new CustomEvent(this.REFRESH_EVENT_NAME.toString(), {
        detail: {
          pageData: this.currPageData,
        },
      });
      this.events.set(this.REFRESH_EVENT_NAME, refreshEvent);
    }
  }

  protected finishRefresh(): void {
    let refreshEvent = this.events.get(this.REFRESH_EVENT_NAME) as Event;
    window.dispatchEvent(refreshEvent);
    if (this.getThemeConfig("advanced", "log", Boolean)?.valueOf()) {
      console.log("finish Refreshing");
    }
    console.log("%c Github %c", "background:#24272A; color:#ffffff", "", "https://github.com/LIlGG/halo-theme-Sakura");
  }
}

// 全局配置文件变量，由主题提供
declare const config: any;
// 当前可变属性变量，由主题提供
declare const pageData: any;

export var sakura: Sakura = new SakuraApp(config);

window.sakura = sakura;
sakura.refresh();

var functions: Set<object>;

function getInitDocumentFunctions(): Set<object> {
  return functions;
}

export function documentFunction(isRefresh: Boolean = true) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    if (!sakura) {
      // 初始化阶段下将函数缓存起来，在刷新阶段再次运行。
      if (!functions) {
        functions = new Set();
      }
      const jsonObj: object = {
        target: target,
        propertyKey: propertyKey,
        method: descriptor.value,
        isRefresh: isRefresh,
      };
      functions.add(jsonObj);
      return;
    }
    sakura.registerDocumentFunction(new SakuraDocumentFunctionImpl(target, propertyKey, descriptor.value, isRefresh));
  };
}

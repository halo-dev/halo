/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "7535");
/******/ })
/************************************************************************/
/******/ ({

/***/ "0439":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");
var bind = __webpack_require__("51f3");
var Axios = __webpack_require__("9b45");
var defaults = __webpack_require__("5ce4");

/**
 * Create an instance of Axios
 *
 * @param {Object} defaultConfig The default config for the instance
 * @return {Axios} A new instance of Axios
 */
function createInstance(defaultConfig) {
  var context = new Axios(defaultConfig);
  var instance = bind(Axios.prototype.request, context);

  // Copy axios.prototype to instance
  utils.extend(instance, Axios.prototype, context);

  // Copy context to instance
  utils.extend(instance, context);

  return instance;
}

// Create the default instance to be exported
var axios = createInstance(defaults);

// Expose Axios class to allow class inheritance
axios.Axios = Axios;

// Factory for creating new instances
axios.create = function create(instanceConfig) {
  return createInstance(utils.merge(defaults, instanceConfig));
};

// Expose Cancel & CancelToken
axios.Cancel = __webpack_require__("9f80");
axios.CancelToken = __webpack_require__("d475");
axios.isCancel = __webpack_require__("3673");

// Expose all/spread
axios.all = function all(promises) {
  return Promise.all(promises);
};
axios.spread = __webpack_require__("9e99");

module.exports = axios;

// Allow use of default import syntax in TypeScript
module.exports.default = axios;


/***/ }),

/***/ "04ac":
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.2 / 15.2.3.5 Object.create(O [, Properties])
var anObject = __webpack_require__("4d65");
var dPs = __webpack_require__("9208");
var enumBugKeys = __webpack_require__("2ba0");
var IE_PROTO = __webpack_require__("5ee9")('IE_PROTO');
var Empty = function () { /* empty */ };
var PROTOTYPE = 'prototype';

// Create object with fake `null` prototype: use iframe Object with cleared prototype
var createDict = function () {
  // Thrash, waste and sodomy: IE GC bug
  var iframe = __webpack_require__("bce2")('iframe');
  var i = enumBugKeys.length;
  var lt = '<';
  var gt = '>';
  var iframeDocument;
  iframe.style.display = 'none';
  __webpack_require__("5b58").appendChild(iframe);
  iframe.src = 'javascript:'; // eslint-disable-line no-script-url
  // createDict = iframe.contentWindow.Object;
  // html.removeChild(iframe);
  iframeDocument = iframe.contentWindow.document;
  iframeDocument.open();
  iframeDocument.write(lt + 'script' + gt + 'document.F=Object' + lt + '/script' + gt);
  iframeDocument.close();
  createDict = iframeDocument.F;
  while (i--) delete createDict[PROTOTYPE][enumBugKeys[i]];
  return createDict();
};

module.exports = Object.create || function create(O, Properties) {
  var result;
  if (O !== null) {
    Empty[PROTOTYPE] = anObject(O);
    result = new Empty();
    Empty[PROTOTYPE] = null;
    // add "__proto__" for Object.getPrototypeOf polyfill
    result[IE_PROTO] = O;
  } else result = createDict();
  return Properties === undefined ? result : dPs(result, Properties);
};


/***/ }),

/***/ "0709":
/***/ (function(module, exports, __webpack_require__) {

// optional / simple context binding
var aFunction = __webpack_require__("9102");
module.exports = function (fn, that, length) {
  aFunction(fn);
  if (that === undefined) return fn;
  switch (length) {
    case 1: return function (a) {
      return fn.call(that, a);
    };
    case 2: return function (a, b) {
      return fn.call(that, a, b);
    };
    case 3: return function (a, b, c) {
      return fn.call(that, a, b, c);
    };
  }
  return function (/* ...args */) {
    return fn.apply(that, arguments);
  };
};


/***/ }),

/***/ "0af5":
/***/ (function(module, exports, __webpack_require__) {

var ctx = __webpack_require__("0709");
var invoke = __webpack_require__("be04");
var html = __webpack_require__("5b58");
var cel = __webpack_require__("bce2");
var global = __webpack_require__("4839");
var process = global.process;
var setTask = global.setImmediate;
var clearTask = global.clearImmediate;
var MessageChannel = global.MessageChannel;
var Dispatch = global.Dispatch;
var counter = 0;
var queue = {};
var ONREADYSTATECHANGE = 'onreadystatechange';
var defer, channel, port;
var run = function () {
  var id = +this;
  // eslint-disable-next-line no-prototype-builtins
  if (queue.hasOwnProperty(id)) {
    var fn = queue[id];
    delete queue[id];
    fn();
  }
};
var listener = function (event) {
  run.call(event.data);
};
// Node.js 0.9+ & IE10+ has setImmediate, otherwise:
if (!setTask || !clearTask) {
  setTask = function setImmediate(fn) {
    var args = [];
    var i = 1;
    while (arguments.length > i) args.push(arguments[i++]);
    queue[++counter] = function () {
      // eslint-disable-next-line no-new-func
      invoke(typeof fn == 'function' ? fn : Function(fn), args);
    };
    defer(counter);
    return counter;
  };
  clearTask = function clearImmediate(id) {
    delete queue[id];
  };
  // Node.js 0.8-
  if (__webpack_require__("9b6d")(process) == 'process') {
    defer = function (id) {
      process.nextTick(ctx(run, id, 1));
    };
  // Sphere (JS game engine) Dispatch API
  } else if (Dispatch && Dispatch.now) {
    defer = function (id) {
      Dispatch.now(ctx(run, id, 1));
    };
  // Browsers with MessageChannel, includes WebWorkers
  } else if (MessageChannel) {
    channel = new MessageChannel();
    port = channel.port2;
    channel.port1.onmessage = listener;
    defer = ctx(port.postMessage, port, 1);
  // Browsers with postMessage, skip WebWorkers
  // IE8 has postMessage, but it's sync & typeof its postMessage is 'object'
  } else if (global.addEventListener && typeof postMessage == 'function' && !global.importScripts) {
    defer = function (id) {
      global.postMessage(id + '', '*');
    };
    global.addEventListener('message', listener, false);
  // IE8-
  } else if (ONREADYSTATECHANGE in cel('script')) {
    defer = function (id) {
      html.appendChild(cel('script'))[ONREADYSTATECHANGE] = function () {
        html.removeChild(this);
        run.call(id);
      };
    };
  // Rest old browsers
  } else {
    defer = function (id) {
      setTimeout(ctx(run, id, 1), 0);
    };
  }
}
module.exports = {
  set: setTask,
  clear: clearTask
};


/***/ }),

/***/ "0d0d":
/***/ (function(module, exports, __webpack_require__) {

// false -> Array#indexOf
// true  -> Array#includes
var toIObject = __webpack_require__("54a3");
var toLength = __webpack_require__("33f2");
var toAbsoluteIndex = __webpack_require__("8da8");
module.exports = function (IS_INCLUDES) {
  return function ($this, el, fromIndex) {
    var O = toIObject($this);
    var length = toLength(O.length);
    var index = toAbsoluteIndex(fromIndex, length);
    var value;
    // Array#includes uses SameValueZero equality algorithm
    // eslint-disable-next-line no-self-compare
    if (IS_INCLUDES && el != el) while (length > index) {
      value = O[index++];
      // eslint-disable-next-line no-self-compare
      if (value != value) return true;
    // Array#indexOf ignores holes, Array#includes - not
    } else for (;length > index; index++) if (IS_INCLUDES || index in O) {
      if (O[index] === el) return IS_INCLUDES || index || 0;
    } return !IS_INCLUDES && -1;
  };
};


/***/ }),

/***/ "1145":
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var LIBRARY = __webpack_require__("4725");
var $export = __webpack_require__("2d2c");
var redefine = __webpack_require__("7f00");
var hide = __webpack_require__("c84b");
var Iterators = __webpack_require__("f03e");
var $iterCreate = __webpack_require__("648e");
var setToStringTag = __webpack_require__("c67d");
var getPrototypeOf = __webpack_require__("d6e1");
var ITERATOR = __webpack_require__("f3ae")('iterator');
var BUGGY = !([].keys && 'next' in [].keys()); // Safari has buggy iterators w/o `next`
var FF_ITERATOR = '@@iterator';
var KEYS = 'keys';
var VALUES = 'values';

var returnThis = function () { return this; };

module.exports = function (Base, NAME, Constructor, next, DEFAULT, IS_SET, FORCED) {
  $iterCreate(Constructor, NAME, next);
  var getMethod = function (kind) {
    if (!BUGGY && kind in proto) return proto[kind];
    switch (kind) {
      case KEYS: return function keys() { return new Constructor(this, kind); };
      case VALUES: return function values() { return new Constructor(this, kind); };
    } return function entries() { return new Constructor(this, kind); };
  };
  var TAG = NAME + ' Iterator';
  var DEF_VALUES = DEFAULT == VALUES;
  var VALUES_BUG = false;
  var proto = Base.prototype;
  var $native = proto[ITERATOR] || proto[FF_ITERATOR] || DEFAULT && proto[DEFAULT];
  var $default = $native || getMethod(DEFAULT);
  var $entries = DEFAULT ? !DEF_VALUES ? $default : getMethod('entries') : undefined;
  var $anyNative = NAME == 'Array' ? proto.entries || $native : $native;
  var methods, key, IteratorPrototype;
  // Fix native
  if ($anyNative) {
    IteratorPrototype = getPrototypeOf($anyNative.call(new Base()));
    if (IteratorPrototype !== Object.prototype && IteratorPrototype.next) {
      // Set @@toStringTag to native iterators
      setToStringTag(IteratorPrototype, TAG, true);
      // fix for some old engines
      if (!LIBRARY && typeof IteratorPrototype[ITERATOR] != 'function') hide(IteratorPrototype, ITERATOR, returnThis);
    }
  }
  // fix Array#{values, @@iterator}.name in V8 / FF
  if (DEF_VALUES && $native && $native.name !== VALUES) {
    VALUES_BUG = true;
    $default = function values() { return $native.call(this); };
  }
  // Define iterator
  if ((!LIBRARY || FORCED) && (BUGGY || VALUES_BUG || !proto[ITERATOR])) {
    hide(proto, ITERATOR, $default);
  }
  // Plug for library
  Iterators[NAME] = $default;
  Iterators[TAG] = returnThis;
  if (DEFAULT) {
    methods = {
      values: DEF_VALUES ? $default : getMethod(VALUES),
      keys: IS_SET ? $default : getMethod(KEYS),
      entries: $entries
    };
    if (FORCED) for (key in methods) {
      if (!(key in proto)) redefine(proto, key, methods[key]);
    } else $export($export.P + $export.F * (BUGGY || VALUES_BUG), NAME, methods);
  }
  return methods;
};


/***/ }),

/***/ "13c4":
/***/ (function(module, exports, __webpack_require__) {

var core = __webpack_require__("1dff");
var global = __webpack_require__("4839");
var SHARED = '__core-js_shared__';
var store = global[SHARED] || (global[SHARED] = {});

(module.exports = function (key, value) {
  return store[key] || (store[key] = value !== undefined ? value : {});
})('versions', []).push({
  version: core.version,
  mode: __webpack_require__("4725") ? 'pure' : 'global',
  copyright: '© 2019 Denis Pushkarev (zloirock.ru)'
});


/***/ }),

/***/ "1417":
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__("cfb8");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add CSS to Shadow Root
var add = __webpack_require__("565c").default
module.exports.__inject__ = function (shadowRoot) {
  add("7992e080", content, shadowRoot)
};

/***/ }),

/***/ "181f":
/***/ (function(module, exports, __webpack_require__) {

// call something on iterator step with safe closing on error
var anObject = __webpack_require__("4d65");
module.exports = function (iterator, fn, value, entries) {
  try {
    return entries ? fn(anObject(value)[0], value[1]) : fn(value);
  // 7.4.6 IteratorClose(iterator, completion)
  } catch (e) {
    var ret = iterator['return'];
    if (ret !== undefined) anObject(ret.call(iterator));
    throw e;
  }
};


/***/ }),

/***/ "1b62":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * Update an Error with the specified config, error code, and response.
 *
 * @param {Error} error The error to update.
 * @param {Object} config The config.
 * @param {string} [code] The error code (for example, 'ECONNABORTED').
 * @param {Object} [request] The request.
 * @param {Object} [response] The response.
 * @returns {Error} The error.
 */
module.exports = function enhanceError(error, config, code, request, response) {
  error.config = config;
  if (code) {
    error.code = code;
  }
  error.request = request;
  error.response = response;
  return error;
};


/***/ }),

/***/ "1dff":
/***/ (function(module, exports) {

var core = module.exports = { version: '2.6.5' };
if (typeof __e == 'number') __e = core; // eslint-disable-line no-undef


/***/ }),

/***/ "1f51":
/***/ (function(module, exports, __webpack_require__) {

// 7.1.1 ToPrimitive(input [, PreferredType])
var isObject = __webpack_require__("b429");
// instead of the ES6 spec version, we didn't implement @@toPrimitive case
// and the second argument - flag - preferred type is a string
module.exports = function (it, S) {
  if (!isObject(it)) return it;
  var fn, val;
  if (S && typeof (fn = it.toString) == 'function' && !isObject(val = fn.call(it))) return val;
  if (typeof (fn = it.valueOf) == 'function' && !isObject(val = fn.call(it))) return val;
  if (!S && typeof (fn = it.toString) == 'function' && !isObject(val = fn.call(it))) return val;
  throw TypeError("Can't convert object to primitive value");
};


/***/ }),

/***/ "201d":
/***/ (function(module, exports) {

module.exports = function (exec) {
  try {
    return !!exec();
  } catch (e) {
    return true;
  }
};


/***/ }),

/***/ "240e":
/***/ (function(module, exports, __webpack_require__) {

// fallback for non-array-like ES3 and non-enumerable old V8 strings
var cof = __webpack_require__("9b6d");
// eslint-disable-next-line no-prototype-builtins
module.exports = Object('z').propertyIsEnumerable(0) ? Object : function (it) {
  return cof(it) == 'String' ? it.split('') : Object(it);
};


/***/ }),

/***/ "27be":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var createError = __webpack_require__("3494");

/**
 * Resolve or reject a Promise based on response status.
 *
 * @param {Function} resolve A function that resolves the promise.
 * @param {Function} reject A function that rejects the promise.
 * @param {object} response The response.
 */
module.exports = function settle(resolve, reject, response) {
  var validateStatus = response.config.validateStatus;
  // Note: status is not exposed by XDomainRequest
  if (!response.status || !validateStatus || validateStatus(response.status)) {
    resolve(response);
  } else {
    reject(createError(
      'Request failed with status code ' + response.status,
      response.config,
      null,
      response.request,
      response
    ));
  }
};


/***/ }),

/***/ "2b11":
/***/ (function(module, exports) {

// 7.2.1 RequireObjectCoercible(argument)
module.exports = function (it) {
  if (it == undefined) throw TypeError("Can't call method on  " + it);
  return it;
};


/***/ }),

/***/ "2ba0":
/***/ (function(module, exports) {

// IE 8- don't enum bug keys
module.exports = (
  'constructor,hasOwnProperty,isPrototypeOf,propertyIsEnumerable,toLocaleString,toString,valueOf'
).split(',');


/***/ }),

/***/ "2be5":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * Determines whether the specified URL is absolute
 *
 * @param {string} url The URL to test
 * @returns {boolean} True if the specified URL is absolute, otherwise false
 */
module.exports = function isAbsoluteURL(url) {
  // A URL is considered absolute if it begins with "<scheme>://" or "//" (protocol-relative URL).
  // RFC 3986 defines scheme name as a sequence of characters beginning with a letter and followed
  // by any combination of letters, digits, plus, period, or hyphen.
  return /^([a-z][a-z\d\+\-\.]*:)?\/\//i.test(url);
};


/***/ }),

/***/ "2bef":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("690e")(false);
// imports


// module
exports.push([module.i, ".comment-author-wrapper .comment-author-time{font-size:12px;color:#808283}.comment-action{padding-right:10px;-webkit-transition:color .3s;transition:color .3s;font-size:12px;color:rgba(0,0,0,.45);cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}", ""]);

// exports


/***/ }),

/***/ "2d2c":
/***/ (function(module, exports, __webpack_require__) {

var global = __webpack_require__("4839");
var core = __webpack_require__("1dff");
var hide = __webpack_require__("c84b");
var redefine = __webpack_require__("7f00");
var ctx = __webpack_require__("0709");
var PROTOTYPE = 'prototype';

var $export = function (type, name, source) {
  var IS_FORCED = type & $export.F;
  var IS_GLOBAL = type & $export.G;
  var IS_STATIC = type & $export.S;
  var IS_PROTO = type & $export.P;
  var IS_BIND = type & $export.B;
  var target = IS_GLOBAL ? global : IS_STATIC ? global[name] || (global[name] = {}) : (global[name] || {})[PROTOTYPE];
  var exports = IS_GLOBAL ? core : core[name] || (core[name] = {});
  var expProto = exports[PROTOTYPE] || (exports[PROTOTYPE] = {});
  var key, own, out, exp;
  if (IS_GLOBAL) source = name;
  for (key in source) {
    // contains in native
    own = !IS_FORCED && target && target[key] !== undefined;
    // export native or passed
    out = (own ? target : source)[key];
    // bind timers to global for call from export context
    exp = IS_BIND && own ? ctx(out, global) : IS_PROTO && typeof out == 'function' ? ctx(Function.call, out) : out;
    // extend global
    if (target) redefine(target, key, out, type & $export.U);
    // export
    if (exports[key] != out) hide(exports, key, exp);
    if (IS_PROTO && expProto[key] != out) expProto[key] = out;
  }
};
global.core = core;
// type bitmap
$export.F = 1;   // forced
$export.G = 2;   // global
$export.S = 4;   // static
$export.P = 8;   // proto
$export.B = 16;  // bind
$export.W = 32;  // wrap
$export.U = 64;  // safe
$export.R = 128; // real proto method for `library`
module.exports = $export;


/***/ }),

/***/ "3301":
/***/ (function(module, exports) {

var hasOwnProperty = {}.hasOwnProperty;
module.exports = function (it, key) {
  return hasOwnProperty.call(it, key);
};


/***/ }),

/***/ "3332":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var bind = __webpack_require__("51f3");
var isBuffer = __webpack_require__("50aa");

/*global toString:true*/

// utils is a library of generic helper functions non-specific to axios

var toString = Object.prototype.toString;

/**
 * Determine if a value is an Array
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is an Array, otherwise false
 */
function isArray(val) {
  return toString.call(val) === '[object Array]';
}

/**
 * Determine if a value is an ArrayBuffer
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is an ArrayBuffer, otherwise false
 */
function isArrayBuffer(val) {
  return toString.call(val) === '[object ArrayBuffer]';
}

/**
 * Determine if a value is a FormData
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is an FormData, otherwise false
 */
function isFormData(val) {
  return (typeof FormData !== 'undefined') && (val instanceof FormData);
}

/**
 * Determine if a value is a view on an ArrayBuffer
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a view on an ArrayBuffer, otherwise false
 */
function isArrayBufferView(val) {
  var result;
  if ((typeof ArrayBuffer !== 'undefined') && (ArrayBuffer.isView)) {
    result = ArrayBuffer.isView(val);
  } else {
    result = (val) && (val.buffer) && (val.buffer instanceof ArrayBuffer);
  }
  return result;
}

/**
 * Determine if a value is a String
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a String, otherwise false
 */
function isString(val) {
  return typeof val === 'string';
}

/**
 * Determine if a value is a Number
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a Number, otherwise false
 */
function isNumber(val) {
  return typeof val === 'number';
}

/**
 * Determine if a value is undefined
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if the value is undefined, otherwise false
 */
function isUndefined(val) {
  return typeof val === 'undefined';
}

/**
 * Determine if a value is an Object
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is an Object, otherwise false
 */
function isObject(val) {
  return val !== null && typeof val === 'object';
}

/**
 * Determine if a value is a Date
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a Date, otherwise false
 */
function isDate(val) {
  return toString.call(val) === '[object Date]';
}

/**
 * Determine if a value is a File
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a File, otherwise false
 */
function isFile(val) {
  return toString.call(val) === '[object File]';
}

/**
 * Determine if a value is a Blob
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a Blob, otherwise false
 */
function isBlob(val) {
  return toString.call(val) === '[object Blob]';
}

/**
 * Determine if a value is a Function
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a Function, otherwise false
 */
function isFunction(val) {
  return toString.call(val) === '[object Function]';
}

/**
 * Determine if a value is a Stream
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a Stream, otherwise false
 */
function isStream(val) {
  return isObject(val) && isFunction(val.pipe);
}

/**
 * Determine if a value is a URLSearchParams object
 *
 * @param {Object} val The value to test
 * @returns {boolean} True if value is a URLSearchParams object, otherwise false
 */
function isURLSearchParams(val) {
  return typeof URLSearchParams !== 'undefined' && val instanceof URLSearchParams;
}

/**
 * Trim excess whitespace off the beginning and end of a string
 *
 * @param {String} str The String to trim
 * @returns {String} The String freed of excess whitespace
 */
function trim(str) {
  return str.replace(/^\s*/, '').replace(/\s*$/, '');
}

/**
 * Determine if we're running in a standard browser environment
 *
 * This allows axios to run in a web worker, and react-native.
 * Both environments support XMLHttpRequest, but not fully standard globals.
 *
 * web workers:
 *  typeof window -> undefined
 *  typeof document -> undefined
 *
 * react-native:
 *  navigator.product -> 'ReactNative'
 */
function isStandardBrowserEnv() {
  if (typeof navigator !== 'undefined' && navigator.product === 'ReactNative') {
    return false;
  }
  return (
    typeof window !== 'undefined' &&
    typeof document !== 'undefined'
  );
}

/**
 * Iterate over an Array or an Object invoking a function for each item.
 *
 * If `obj` is an Array callback will be called passing
 * the value, index, and complete array for each item.
 *
 * If 'obj' is an Object callback will be called passing
 * the value, key, and complete object for each property.
 *
 * @param {Object|Array} obj The object to iterate
 * @param {Function} fn The callback to invoke for each item
 */
function forEach(obj, fn) {
  // Don't bother if no value provided
  if (obj === null || typeof obj === 'undefined') {
    return;
  }

  // Force an array if not already something iterable
  if (typeof obj !== 'object') {
    /*eslint no-param-reassign:0*/
    obj = [obj];
  }

  if (isArray(obj)) {
    // Iterate over array values
    for (var i = 0, l = obj.length; i < l; i++) {
      fn.call(null, obj[i], i, obj);
    }
  } else {
    // Iterate over object keys
    for (var key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        fn.call(null, obj[key], key, obj);
      }
    }
  }
}

/**
 * Accepts varargs expecting each argument to be an object, then
 * immutably merges the properties of each object and returns result.
 *
 * When multiple objects contain the same key the later object in
 * the arguments list will take precedence.
 *
 * Example:
 *
 * ```js
 * var result = merge({foo: 123}, {foo: 456});
 * console.log(result.foo); // outputs 456
 * ```
 *
 * @param {Object} obj1 Object to merge
 * @returns {Object} Result of all merge properties
 */
function merge(/* obj1, obj2, obj3, ... */) {
  var result = {};
  function assignValue(val, key) {
    if (typeof result[key] === 'object' && typeof val === 'object') {
      result[key] = merge(result[key], val);
    } else {
      result[key] = val;
    }
  }

  for (var i = 0, l = arguments.length; i < l; i++) {
    forEach(arguments[i], assignValue);
  }
  return result;
}

/**
 * Extends object a by mutably adding to it the properties of object b.
 *
 * @param {Object} a The object to be extended
 * @param {Object} b The object to copy properties from
 * @param {Object} thisArg The object to bind function to
 * @return {Object} The resulting value of object a
 */
function extend(a, b, thisArg) {
  forEach(b, function assignValue(val, key) {
    if (thisArg && typeof val === 'function') {
      a[key] = bind(val, thisArg);
    } else {
      a[key] = val;
    }
  });
  return a;
}

module.exports = {
  isArray: isArray,
  isArrayBuffer: isArrayBuffer,
  isBuffer: isBuffer,
  isFormData: isFormData,
  isArrayBufferView: isArrayBufferView,
  isString: isString,
  isNumber: isNumber,
  isObject: isObject,
  isUndefined: isUndefined,
  isDate: isDate,
  isFile: isFile,
  isBlob: isBlob,
  isFunction: isFunction,
  isStream: isStream,
  isURLSearchParams: isURLSearchParams,
  isStandardBrowserEnv: isStandardBrowserEnv,
  forEach: forEach,
  merge: merge,
  extend: extend,
  trim: trim
};


/***/ }),

/***/ "33f2":
/***/ (function(module, exports, __webpack_require__) {

// 7.1.15 ToLength
var toInteger = __webpack_require__("ae63");
var min = Math.min;
module.exports = function (it) {
  return it > 0 ? min(toInteger(it), 0x1fffffffffffff) : 0; // pow(2, 53) - 1 == 9007199254740991
};


/***/ }),

/***/ "3494":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var enhanceError = __webpack_require__("1b62");

/**
 * Create an Error with the specified message, config, error code, request and response.
 *
 * @param {string} message The error message.
 * @param {Object} config The config.
 * @param {string} [code] The error code (for example, 'ECONNABORTED').
 * @param {Object} [request] The request.
 * @param {Object} [response] The response.
 * @returns {Error} The created error.
 */
module.exports = function createError(message, config, code, request, response) {
  var error = new Error(message);
  return enhanceError(error, config, code, request, response);
};


/***/ }),

/***/ "35c6":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");
var transformData = __webpack_require__("79c0");
var isCancel = __webpack_require__("3673");
var defaults = __webpack_require__("5ce4");
var isAbsoluteURL = __webpack_require__("2be5");
var combineURLs = __webpack_require__("45d2");

/**
 * Throws a `Cancel` if cancellation has been requested.
 */
function throwIfCancellationRequested(config) {
  if (config.cancelToken) {
    config.cancelToken.throwIfRequested();
  }
}

/**
 * Dispatch a request to the server using the configured adapter.
 *
 * @param {object} config The config that is to be used for the request
 * @returns {Promise} The Promise to be fulfilled
 */
module.exports = function dispatchRequest(config) {
  throwIfCancellationRequested(config);

  // Support baseURL config
  if (config.baseURL && !isAbsoluteURL(config.url)) {
    config.url = combineURLs(config.baseURL, config.url);
  }

  // Ensure headers exist
  config.headers = config.headers || {};

  // Transform request data
  config.data = transformData(
    config.data,
    config.headers,
    config.transformRequest
  );

  // Flatten headers
  config.headers = utils.merge(
    config.headers.common || {},
    config.headers[config.method] || {},
    config.headers || {}
  );

  utils.forEach(
    ['delete', 'get', 'head', 'post', 'put', 'patch', 'common'],
    function cleanHeaderConfig(method) {
      delete config.headers[method];
    }
  );

  var adapter = config.adapter || defaults.adapter;

  return adapter(config).then(function onAdapterResolution(response) {
    throwIfCancellationRequested(config);

    // Transform response data
    response.data = transformData(
      response.data,
      response.headers,
      config.transformResponse
    );

    return response;
  }, function onAdapterRejection(reason) {
    if (!isCancel(reason)) {
      throwIfCancellationRequested(config);

      // Transform response data
      if (reason && reason.response) {
        reason.response.data = transformData(
          reason.response.data,
          reason.response.headers,
          config.transformResponse
        );
      }
    }

    return Promise.reject(reason);
  });
};


/***/ }),

/***/ "3673":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function isCancel(value) {
  return !!(value && value.__CANCEL__);
};


/***/ }),

/***/ "3747":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

module.exports = (
  utils.isStandardBrowserEnv() ?

  // Standard browser envs support document.cookie
  (function standardBrowserEnv() {
    return {
      write: function write(name, value, expires, path, domain, secure) {
        var cookie = [];
        cookie.push(name + '=' + encodeURIComponent(value));

        if (utils.isNumber(expires)) {
          cookie.push('expires=' + new Date(expires).toGMTString());
        }

        if (utils.isString(path)) {
          cookie.push('path=' + path);
        }

        if (utils.isString(domain)) {
          cookie.push('domain=' + domain);
        }

        if (secure === true) {
          cookie.push('secure');
        }

        document.cookie = cookie.join('; ');
      },

      read: function read(name) {
        var match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'));
        return (match ? decodeURIComponent(match[3]) : null);
      },

      remove: function remove(name) {
        this.write(name, '', Date.now() - 86400000);
      }
    };
  })() :

  // Non standard browser env (web workers, react-native) lack needed support.
  (function nonStandardBrowserEnv() {
    return {
      write: function write() {},
      read: function read() { return null; },
      remove: function remove() {}
    };
  })()
);


/***/ }),

/***/ "38bc":
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_RESULT__;/* NProgress, (c) 2013, 2014 Rico Sta. Cruz - http://ricostacruz.com/nprogress
 * @license MIT */

;(function(root, factory) {

  if (true) {
    !(__WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.call(exports, __webpack_require__, exports, module)) :
				__WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
  } else {}

})(this, function() {
  var NProgress = {};

  NProgress.version = '0.2.0';

  var Settings = NProgress.settings = {
    minimum: 0.08,
    easing: 'ease',
    positionUsing: '',
    speed: 200,
    trickle: true,
    trickleRate: 0.02,
    trickleSpeed: 800,
    showSpinner: true,
    barSelector: '[role="bar"]',
    spinnerSelector: '[role="spinner"]',
    parent: 'body',
    template: '<div class="bar" role="bar"><div class="peg"></div></div><div class="spinner" role="spinner"><div class="spinner-icon"></div></div>'
  };

  /**
   * Updates configuration.
   *
   *     NProgress.configure({
   *       minimum: 0.1
   *     });
   */
  NProgress.configure = function(options) {
    var key, value;
    for (key in options) {
      value = options[key];
      if (value !== undefined && options.hasOwnProperty(key)) Settings[key] = value;
    }

    return this;
  };

  /**
   * Last number.
   */

  NProgress.status = null;

  /**
   * Sets the progress bar status, where `n` is a number from `0.0` to `1.0`.
   *
   *     NProgress.set(0.4);
   *     NProgress.set(1.0);
   */

  NProgress.set = function(n) {
    var started = NProgress.isStarted();

    n = clamp(n, Settings.minimum, 1);
    NProgress.status = (n === 1 ? null : n);

    var progress = NProgress.render(!started),
        bar      = progress.querySelector(Settings.barSelector),
        speed    = Settings.speed,
        ease     = Settings.easing;

    progress.offsetWidth; /* Repaint */

    queue(function(next) {
      // Set positionUsing if it hasn't already been set
      if (Settings.positionUsing === '') Settings.positionUsing = NProgress.getPositioningCSS();

      // Add transition
      css(bar, barPositionCSS(n, speed, ease));

      if (n === 1) {
        // Fade out
        css(progress, { 
          transition: 'none', 
          opacity: 1 
        });
        progress.offsetWidth; /* Repaint */

        setTimeout(function() {
          css(progress, { 
            transition: 'all ' + speed + 'ms linear', 
            opacity: 0 
          });
          setTimeout(function() {
            NProgress.remove();
            next();
          }, speed);
        }, speed);
      } else {
        setTimeout(next, speed);
      }
    });

    return this;
  };

  NProgress.isStarted = function() {
    return typeof NProgress.status === 'number';
  };

  /**
   * Shows the progress bar.
   * This is the same as setting the status to 0%, except that it doesn't go backwards.
   *
   *     NProgress.start();
   *
   */
  NProgress.start = function() {
    if (!NProgress.status) NProgress.set(0);

    var work = function() {
      setTimeout(function() {
        if (!NProgress.status) return;
        NProgress.trickle();
        work();
      }, Settings.trickleSpeed);
    };

    if (Settings.trickle) work();

    return this;
  };

  /**
   * Hides the progress bar.
   * This is the *sort of* the same as setting the status to 100%, with the
   * difference being `done()` makes some placebo effect of some realistic motion.
   *
   *     NProgress.done();
   *
   * If `true` is passed, it will show the progress bar even if its hidden.
   *
   *     NProgress.done(true);
   */

  NProgress.done = function(force) {
    if (!force && !NProgress.status) return this;

    return NProgress.inc(0.3 + 0.5 * Math.random()).set(1);
  };

  /**
   * Increments by a random amount.
   */

  NProgress.inc = function(amount) {
    var n = NProgress.status;

    if (!n) {
      return NProgress.start();
    } else {
      if (typeof amount !== 'number') {
        amount = (1 - n) * clamp(Math.random() * n, 0.1, 0.95);
      }

      n = clamp(n + amount, 0, 0.994);
      return NProgress.set(n);
    }
  };

  NProgress.trickle = function() {
    return NProgress.inc(Math.random() * Settings.trickleRate);
  };

  /**
   * Waits for all supplied jQuery promises and
   * increases the progress as the promises resolve.
   *
   * @param $promise jQUery Promise
   */
  (function() {
    var initial = 0, current = 0;

    NProgress.promise = function($promise) {
      if (!$promise || $promise.state() === "resolved") {
        return this;
      }

      if (current === 0) {
        NProgress.start();
      }

      initial++;
      current++;

      $promise.always(function() {
        current--;
        if (current === 0) {
            initial = 0;
            NProgress.done();
        } else {
            NProgress.set((initial - current) / initial);
        }
      });

      return this;
    };

  })();

  /**
   * (Internal) renders the progress bar markup based on the `template`
   * setting.
   */

  NProgress.render = function(fromStart) {
    if (NProgress.isRendered()) return document.getElementById('nprogress');

    addClass(document.documentElement, 'nprogress-busy');
    
    var progress = document.createElement('div');
    progress.id = 'nprogress';
    progress.innerHTML = Settings.template;

    var bar      = progress.querySelector(Settings.barSelector),
        perc     = fromStart ? '-100' : toBarPerc(NProgress.status || 0),
        parent   = document.querySelector(Settings.parent),
        spinner;
    
    css(bar, {
      transition: 'all 0 linear',
      transform: 'translate3d(' + perc + '%,0,0)'
    });

    if (!Settings.showSpinner) {
      spinner = progress.querySelector(Settings.spinnerSelector);
      spinner && removeElement(spinner);
    }

    if (parent != document.body) {
      addClass(parent, 'nprogress-custom-parent');
    }

    parent.appendChild(progress);
    return progress;
  };

  /**
   * Removes the element. Opposite of render().
   */

  NProgress.remove = function() {
    removeClass(document.documentElement, 'nprogress-busy');
    removeClass(document.querySelector(Settings.parent), 'nprogress-custom-parent');
    var progress = document.getElementById('nprogress');
    progress && removeElement(progress);
  };

  /**
   * Checks if the progress bar is rendered.
   */

  NProgress.isRendered = function() {
    return !!document.getElementById('nprogress');
  };

  /**
   * Determine which positioning CSS rule to use.
   */

  NProgress.getPositioningCSS = function() {
    // Sniff on document.body.style
    var bodyStyle = document.body.style;

    // Sniff prefixes
    var vendorPrefix = ('WebkitTransform' in bodyStyle) ? 'Webkit' :
                       ('MozTransform' in bodyStyle) ? 'Moz' :
                       ('msTransform' in bodyStyle) ? 'ms' :
                       ('OTransform' in bodyStyle) ? 'O' : '';

    if (vendorPrefix + 'Perspective' in bodyStyle) {
      // Modern browsers with 3D support, e.g. Webkit, IE10
      return 'translate3d';
    } else if (vendorPrefix + 'Transform' in bodyStyle) {
      // Browsers without 3D support, e.g. IE9
      return 'translate';
    } else {
      // Browsers without translate() support, e.g. IE7-8
      return 'margin';
    }
  };

  /**
   * Helpers
   */

  function clamp(n, min, max) {
    if (n < min) return min;
    if (n > max) return max;
    return n;
  }

  /**
   * (Internal) converts a percentage (`0..1`) to a bar translateX
   * percentage (`-100%..0%`).
   */

  function toBarPerc(n) {
    return (-1 + n) * 100;
  }


  /**
   * (Internal) returns the correct CSS for changing the bar's
   * position given an n percentage, and speed and ease from Settings
   */

  function barPositionCSS(n, speed, ease) {
    var barCSS;

    if (Settings.positionUsing === 'translate3d') {
      barCSS = { transform: 'translate3d('+toBarPerc(n)+'%,0,0)' };
    } else if (Settings.positionUsing === 'translate') {
      barCSS = { transform: 'translate('+toBarPerc(n)+'%,0)' };
    } else {
      barCSS = { 'margin-left': toBarPerc(n)+'%' };
    }

    barCSS.transition = 'all '+speed+'ms '+ease;

    return barCSS;
  }

  /**
   * (Internal) Queues a function to be executed.
   */

  var queue = (function() {
    var pending = [];
    
    function next() {
      var fn = pending.shift();
      if (fn) {
        fn(next);
      }
    }

    return function(fn) {
      pending.push(fn);
      if (pending.length == 1) next();
    };
  })();

  /**
   * (Internal) Applies css properties to an element, similar to the jQuery 
   * css method.
   *
   * While this helper does assist with vendor prefixed property names, it 
   * does not perform any manipulation of values prior to setting styles.
   */

  var css = (function() {
    var cssPrefixes = [ 'Webkit', 'O', 'Moz', 'ms' ],
        cssProps    = {};

    function camelCase(string) {
      return string.replace(/^-ms-/, 'ms-').replace(/-([\da-z])/gi, function(match, letter) {
        return letter.toUpperCase();
      });
    }

    function getVendorProp(name) {
      var style = document.body.style;
      if (name in style) return name;

      var i = cssPrefixes.length,
          capName = name.charAt(0).toUpperCase() + name.slice(1),
          vendorName;
      while (i--) {
        vendorName = cssPrefixes[i] + capName;
        if (vendorName in style) return vendorName;
      }

      return name;
    }

    function getStyleProp(name) {
      name = camelCase(name);
      return cssProps[name] || (cssProps[name] = getVendorProp(name));
    }

    function applyCss(element, prop, value) {
      prop = getStyleProp(prop);
      element.style[prop] = value;
    }

    return function(element, properties) {
      var args = arguments,
          prop, 
          value;

      if (args.length == 2) {
        for (prop in properties) {
          value = properties[prop];
          if (value !== undefined && properties.hasOwnProperty(prop)) applyCss(element, prop, value);
        }
      } else {
        applyCss(element, args[1], args[2]);
      }
    }
  })();

  /**
   * (Internal) Determines if an element or space separated list of class names contains a class name.
   */

  function hasClass(element, name) {
    var list = typeof element == 'string' ? element : classList(element);
    return list.indexOf(' ' + name + ' ') >= 0;
  }

  /**
   * (Internal) Adds a class to an element.
   */

  function addClass(element, name) {
    var oldList = classList(element),
        newList = oldList + name;

    if (hasClass(oldList, name)) return; 

    // Trim the opening space.
    element.className = newList.substring(1);
  }

  /**
   * (Internal) Removes a class from an element.
   */

  function removeClass(element, name) {
    var oldList = classList(element),
        newList;

    if (!hasClass(element, name)) return;

    // Replace the class name.
    newList = oldList.replace(' ' + name + ' ', ' ');

    // Trim the opening and closing spaces.
    element.className = newList.substring(1, newList.length - 1);
  }

  /**
   * (Internal) Gets a space separated list of the class names on the element. 
   * The list is wrapped with a single space on each end to facilitate finding 
   * matches within the list.
   */

  function classList(element) {
    return (' ' + (element.className || '') + ' ').replace(/\s+/gi, ' ');
  }

  /**
   * (Internal) Removes an element from the DOM.
   */

  function removeElement(element) {
    element && element.parentNode && element.parentNode.removeChild(element);
  }

  return NProgress;
});



/***/ }),

/***/ "3a0a":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

module.exports = function normalizeHeaderName(headers, normalizedName) {
  utils.forEach(headers, function processHeader(value, name) {
    if (name !== normalizedName && name.toUpperCase() === normalizedName.toUpperCase()) {
      headers[normalizedName] = value;
      delete headers[name];
    }
  });
};


/***/ }),

/***/ "3a0f":
/***/ (function(module, exports, __webpack_require__) {

// Thank's IE8 for his funny defineProperty
module.exports = !__webpack_require__("201d")(function () {
  return Object.defineProperty({}, 'a', { get: function () { return 7; } }).a != 7;
});


/***/ }),

/***/ "3efa":
/***/ (function(module, exports, __webpack_require__) {

"use strict";

// 25.4.1.5 NewPromiseCapability(C)
var aFunction = __webpack_require__("9102");

function PromiseCapability(C) {
  var resolve, reject;
  this.promise = new C(function ($$resolve, $$reject) {
    if (resolve !== undefined || reject !== undefined) throw TypeError('Bad Promise constructor');
    resolve = $$resolve;
    reject = $$reject;
  });
  this.resolve = aFunction(resolve);
  this.reject = aFunction(reject);
}

module.exports.f = function (C) {
  return new PromiseCapability(C);
};


/***/ }),

/***/ "45d2":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * Creates a new URL by combining the specified URLs
 *
 * @param {string} baseURL The base URL
 * @param {string} relativeURL The relative URL
 * @returns {string} The combined URL
 */
module.exports = function combineURLs(baseURL, relativeURL) {
  return relativeURL
    ? baseURL.replace(/\/+$/, '') + '/' + relativeURL.replace(/^\/+/, '')
    : baseURL;
};


/***/ }),

/***/ "4725":
/***/ (function(module, exports) {

module.exports = false;


/***/ }),

/***/ "4839":
/***/ (function(module, exports) {

// https://github.com/zloirock/core-js/issues/86#issuecomment-115759028
var global = module.exports = typeof window != 'undefined' && window.Math == Math
  ? window : typeof self != 'undefined' && self.Math == Math ? self
  // eslint-disable-next-line no-new-func
  : Function('return this')();
if (typeof __g == 'number') __g = global; // eslint-disable-line no-undef


/***/ }),

/***/ "4c39":
/***/ (function(module, exports) {

// shim for using process in browser
var process = module.exports = {};

// cached from whatever global is present so that test runners that stub it
// don't break things.  But we need to wrap it in a try catch in case it is
// wrapped in strict mode code which doesn't define any globals.  It's inside a
// function because try/catches deoptimize in certain engines.

var cachedSetTimeout;
var cachedClearTimeout;

function defaultSetTimout() {
    throw new Error('setTimeout has not been defined');
}
function defaultClearTimeout () {
    throw new Error('clearTimeout has not been defined');
}
(function () {
    try {
        if (typeof setTimeout === 'function') {
            cachedSetTimeout = setTimeout;
        } else {
            cachedSetTimeout = defaultSetTimout;
        }
    } catch (e) {
        cachedSetTimeout = defaultSetTimout;
    }
    try {
        if (typeof clearTimeout === 'function') {
            cachedClearTimeout = clearTimeout;
        } else {
            cachedClearTimeout = defaultClearTimeout;
        }
    } catch (e) {
        cachedClearTimeout = defaultClearTimeout;
    }
} ())
function runTimeout(fun) {
    if (cachedSetTimeout === setTimeout) {
        //normal enviroments in sane situations
        return setTimeout(fun, 0);
    }
    // if setTimeout wasn't available but was latter defined
    if ((cachedSetTimeout === defaultSetTimout || !cachedSetTimeout) && setTimeout) {
        cachedSetTimeout = setTimeout;
        return setTimeout(fun, 0);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedSetTimeout(fun, 0);
    } catch(e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't trust the global object when called normally
            return cachedSetTimeout.call(null, fun, 0);
        } catch(e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error
            return cachedSetTimeout.call(this, fun, 0);
        }
    }


}
function runClearTimeout(marker) {
    if (cachedClearTimeout === clearTimeout) {
        //normal enviroments in sane situations
        return clearTimeout(marker);
    }
    // if clearTimeout wasn't available but was latter defined
    if ((cachedClearTimeout === defaultClearTimeout || !cachedClearTimeout) && clearTimeout) {
        cachedClearTimeout = clearTimeout;
        return clearTimeout(marker);
    }
    try {
        // when when somebody has screwed with setTimeout but no I.E. maddness
        return cachedClearTimeout(marker);
    } catch (e){
        try {
            // When we are in I.E. but the script has been evaled so I.E. doesn't  trust the global object when called normally
            return cachedClearTimeout.call(null, marker);
        } catch (e){
            // same as above but when it's a version of I.E. that must have the global object for 'this', hopfully our context correct otherwise it will throw a global error.
            // Some versions of I.E. have different rules for clearTimeout vs setTimeout
            return cachedClearTimeout.call(this, marker);
        }
    }



}
var queue = [];
var draining = false;
var currentQueue;
var queueIndex = -1;

function cleanUpNextTick() {
    if (!draining || !currentQueue) {
        return;
    }
    draining = false;
    if (currentQueue.length) {
        queue = currentQueue.concat(queue);
    } else {
        queueIndex = -1;
    }
    if (queue.length) {
        drainQueue();
    }
}

function drainQueue() {
    if (draining) {
        return;
    }
    var timeout = runTimeout(cleanUpNextTick);
    draining = true;

    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        while (++queueIndex < len) {
            if (currentQueue) {
                currentQueue[queueIndex].run();
            }
        }
        queueIndex = -1;
        len = queue.length;
    }
    currentQueue = null;
    draining = false;
    runClearTimeout(timeout);
}

process.nextTick = function (fun) {
    var args = new Array(arguments.length - 1);
    if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
            args[i - 1] = arguments[i];
        }
    }
    queue.push(new Item(fun, args));
    if (queue.length === 1 && !draining) {
        runTimeout(drainQueue);
    }
};

// v8 likes predictible objects
function Item(fun, array) {
    this.fun = fun;
    this.array = array;
}
Item.prototype.run = function () {
    this.fun.apply(null, this.array);
};
process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues
process.versions = {};

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;
process.prependListener = noop;
process.prependOnceListener = noop;

process.listeners = function (name) { return [] }

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };


/***/ }),

/***/ "4d65":
/***/ (function(module, exports, __webpack_require__) {

var isObject = __webpack_require__("b429");
module.exports = function (it) {
  if (!isObject(it)) throw TypeError(it + ' is not an object!');
  return it;
};


/***/ }),

/***/ "4fd1":
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var global = __webpack_require__("4839");
var dP = __webpack_require__("694f");
var DESCRIPTORS = __webpack_require__("3a0f");
var SPECIES = __webpack_require__("f3ae")('species');

module.exports = function (KEY) {
  var C = global[KEY];
  if (DESCRIPTORS && C && !C[SPECIES]) dP.f(C, SPECIES, {
    configurable: true,
    get: function () { return this; }
  });
};


/***/ }),

/***/ "508e":
/***/ (function(module, exports) {

module.exports = function (it, Constructor, name, forbiddenField) {
  if (!(it instanceof Constructor) || (forbiddenField !== undefined && forbiddenField in it)) {
    throw TypeError(name + ': incorrect invocation!');
  } return it;
};


/***/ }),

/***/ "50aa":
/***/ (function(module, exports) {

/*!
 * Determine if an object is a Buffer
 *
 * @author   Feross Aboukhadijeh <https://feross.org>
 * @license  MIT
 */

// The _isBuffer check is for Safari 5-7 support, because it's missing
// Object.prototype.constructor. Remove this eventually
module.exports = function (obj) {
  return obj != null && (isBuffer(obj) || isSlowBuffer(obj) || !!obj._isBuffer)
}

function isBuffer (obj) {
  return !!obj.constructor && typeof obj.constructor.isBuffer === 'function' && obj.constructor.isBuffer(obj)
}

// For Node v0.10 support. Remove this eventually.
function isSlowBuffer (obj) {
  return typeof obj.readFloatLE === 'function' && typeof obj.slice === 'function' && isBuffer(obj.slice(0, 0))
}


/***/ }),

/***/ "51f3":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function bind(fn, thisArg) {
  return function wrap() {
    var args = new Array(arguments.length);
    for (var i = 0; i < args.length; i++) {
      args[i] = arguments[i];
    }
    return fn.apply(thisArg, args);
  };
};


/***/ }),

/***/ "53da":
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var LIBRARY = __webpack_require__("4725");
var global = __webpack_require__("4839");
var ctx = __webpack_require__("0709");
var classof = __webpack_require__("56f8");
var $export = __webpack_require__("2d2c");
var isObject = __webpack_require__("b429");
var aFunction = __webpack_require__("9102");
var anInstance = __webpack_require__("508e");
var forOf = __webpack_require__("f10a");
var speciesConstructor = __webpack_require__("d4f6");
var task = __webpack_require__("0af5").set;
var microtask = __webpack_require__("e6ca")();
var newPromiseCapabilityModule = __webpack_require__("3efa");
var perform = __webpack_require__("c8bb");
var userAgent = __webpack_require__("dad2");
var promiseResolve = __webpack_require__("d28d");
var PROMISE = 'Promise';
var TypeError = global.TypeError;
var process = global.process;
var versions = process && process.versions;
var v8 = versions && versions.v8 || '';
var $Promise = global[PROMISE];
var isNode = classof(process) == 'process';
var empty = function () { /* empty */ };
var Internal, newGenericPromiseCapability, OwnPromiseCapability, Wrapper;
var newPromiseCapability = newGenericPromiseCapability = newPromiseCapabilityModule.f;

var USE_NATIVE = !!function () {
  try {
    // correct subclassing with @@species support
    var promise = $Promise.resolve(1);
    var FakePromise = (promise.constructor = {})[__webpack_require__("f3ae")('species')] = function (exec) {
      exec(empty, empty);
    };
    // unhandled rejections tracking support, NodeJS Promise without it fails @@species test
    return (isNode || typeof PromiseRejectionEvent == 'function')
      && promise.then(empty) instanceof FakePromise
      // v8 6.6 (Node 10 and Chrome 66) have a bug with resolving custom thenables
      // https://bugs.chromium.org/p/chromium/issues/detail?id=830565
      // we can't detect it synchronously, so just check versions
      && v8.indexOf('6.6') !== 0
      && userAgent.indexOf('Chrome/66') === -1;
  } catch (e) { /* empty */ }
}();

// helpers
var isThenable = function (it) {
  var then;
  return isObject(it) && typeof (then = it.then) == 'function' ? then : false;
};
var notify = function (promise, isReject) {
  if (promise._n) return;
  promise._n = true;
  var chain = promise._c;
  microtask(function () {
    var value = promise._v;
    var ok = promise._s == 1;
    var i = 0;
    var run = function (reaction) {
      var handler = ok ? reaction.ok : reaction.fail;
      var resolve = reaction.resolve;
      var reject = reaction.reject;
      var domain = reaction.domain;
      var result, then, exited;
      try {
        if (handler) {
          if (!ok) {
            if (promise._h == 2) onHandleUnhandled(promise);
            promise._h = 1;
          }
          if (handler === true) result = value;
          else {
            if (domain) domain.enter();
            result = handler(value); // may throw
            if (domain) {
              domain.exit();
              exited = true;
            }
          }
          if (result === reaction.promise) {
            reject(TypeError('Promise-chain cycle'));
          } else if (then = isThenable(result)) {
            then.call(result, resolve, reject);
          } else resolve(result);
        } else reject(value);
      } catch (e) {
        if (domain && !exited) domain.exit();
        reject(e);
      }
    };
    while (chain.length > i) run(chain[i++]); // variable length - can't use forEach
    promise._c = [];
    promise._n = false;
    if (isReject && !promise._h) onUnhandled(promise);
  });
};
var onUnhandled = function (promise) {
  task.call(global, function () {
    var value = promise._v;
    var unhandled = isUnhandled(promise);
    var result, handler, console;
    if (unhandled) {
      result = perform(function () {
        if (isNode) {
          process.emit('unhandledRejection', value, promise);
        } else if (handler = global.onunhandledrejection) {
          handler({ promise: promise, reason: value });
        } else if ((console = global.console) && console.error) {
          console.error('Unhandled promise rejection', value);
        }
      });
      // Browsers should not trigger `rejectionHandled` event if it was handled here, NodeJS - should
      promise._h = isNode || isUnhandled(promise) ? 2 : 1;
    } promise._a = undefined;
    if (unhandled && result.e) throw result.v;
  });
};
var isUnhandled = function (promise) {
  return promise._h !== 1 && (promise._a || promise._c).length === 0;
};
var onHandleUnhandled = function (promise) {
  task.call(global, function () {
    var handler;
    if (isNode) {
      process.emit('rejectionHandled', promise);
    } else if (handler = global.onrejectionhandled) {
      handler({ promise: promise, reason: promise._v });
    }
  });
};
var $reject = function (value) {
  var promise = this;
  if (promise._d) return;
  promise._d = true;
  promise = promise._w || promise; // unwrap
  promise._v = value;
  promise._s = 2;
  if (!promise._a) promise._a = promise._c.slice();
  notify(promise, true);
};
var $resolve = function (value) {
  var promise = this;
  var then;
  if (promise._d) return;
  promise._d = true;
  promise = promise._w || promise; // unwrap
  try {
    if (promise === value) throw TypeError("Promise can't be resolved itself");
    if (then = isThenable(value)) {
      microtask(function () {
        var wrapper = { _w: promise, _d: false }; // wrap
        try {
          then.call(value, ctx($resolve, wrapper, 1), ctx($reject, wrapper, 1));
        } catch (e) {
          $reject.call(wrapper, e);
        }
      });
    } else {
      promise._v = value;
      promise._s = 1;
      notify(promise, false);
    }
  } catch (e) {
    $reject.call({ _w: promise, _d: false }, e); // wrap
  }
};

// constructor polyfill
if (!USE_NATIVE) {
  // 25.4.3.1 Promise(executor)
  $Promise = function Promise(executor) {
    anInstance(this, $Promise, PROMISE, '_h');
    aFunction(executor);
    Internal.call(this);
    try {
      executor(ctx($resolve, this, 1), ctx($reject, this, 1));
    } catch (err) {
      $reject.call(this, err);
    }
  };
  // eslint-disable-next-line no-unused-vars
  Internal = function Promise(executor) {
    this._c = [];             // <- awaiting reactions
    this._a = undefined;      // <- checked in isUnhandled reactions
    this._s = 0;              // <- state
    this._d = false;          // <- done
    this._v = undefined;      // <- value
    this._h = 0;              // <- rejection state, 0 - default, 1 - handled, 2 - unhandled
    this._n = false;          // <- notify
  };
  Internal.prototype = __webpack_require__("f043")($Promise.prototype, {
    // 25.4.5.3 Promise.prototype.then(onFulfilled, onRejected)
    then: function then(onFulfilled, onRejected) {
      var reaction = newPromiseCapability(speciesConstructor(this, $Promise));
      reaction.ok = typeof onFulfilled == 'function' ? onFulfilled : true;
      reaction.fail = typeof onRejected == 'function' && onRejected;
      reaction.domain = isNode ? process.domain : undefined;
      this._c.push(reaction);
      if (this._a) this._a.push(reaction);
      if (this._s) notify(this, false);
      return reaction.promise;
    },
    // 25.4.5.1 Promise.prototype.catch(onRejected)
    'catch': function (onRejected) {
      return this.then(undefined, onRejected);
    }
  });
  OwnPromiseCapability = function () {
    var promise = new Internal();
    this.promise = promise;
    this.resolve = ctx($resolve, promise, 1);
    this.reject = ctx($reject, promise, 1);
  };
  newPromiseCapabilityModule.f = newPromiseCapability = function (C) {
    return C === $Promise || C === Wrapper
      ? new OwnPromiseCapability(C)
      : newGenericPromiseCapability(C);
  };
}

$export($export.G + $export.W + $export.F * !USE_NATIVE, { Promise: $Promise });
__webpack_require__("c67d")($Promise, PROMISE);
__webpack_require__("4fd1")(PROMISE);
Wrapper = __webpack_require__("1dff")[PROMISE];

// statics
$export($export.S + $export.F * !USE_NATIVE, PROMISE, {
  // 25.4.4.5 Promise.reject(r)
  reject: function reject(r) {
    var capability = newPromiseCapability(this);
    var $$reject = capability.reject;
    $$reject(r);
    return capability.promise;
  }
});
$export($export.S + $export.F * (LIBRARY || !USE_NATIVE), PROMISE, {
  // 25.4.4.6 Promise.resolve(x)
  resolve: function resolve(x) {
    return promiseResolve(LIBRARY && this === Wrapper ? $Promise : this, x);
  }
});
$export($export.S + $export.F * !(USE_NATIVE && __webpack_require__("fbd3")(function (iter) {
  $Promise.all(iter)['catch'](empty);
})), PROMISE, {
  // 25.4.4.1 Promise.all(iterable)
  all: function all(iterable) {
    var C = this;
    var capability = newPromiseCapability(C);
    var resolve = capability.resolve;
    var reject = capability.reject;
    var result = perform(function () {
      var values = [];
      var index = 0;
      var remaining = 1;
      forOf(iterable, false, function (promise) {
        var $index = index++;
        var alreadyCalled = false;
        values.push(undefined);
        remaining++;
        C.resolve(promise).then(function (value) {
          if (alreadyCalled) return;
          alreadyCalled = true;
          values[$index] = value;
          --remaining || resolve(values);
        }, reject);
      });
      --remaining || resolve(values);
    });
    if (result.e) reject(result.v);
    return capability.promise;
  },
  // 25.4.4.4 Promise.race(iterable)
  race: function race(iterable) {
    var C = this;
    var capability = newPromiseCapability(C);
    var reject = capability.reject;
    var result = perform(function () {
      forOf(iterable, false, function (promise) {
        C.resolve(promise).then(capability.resolve, reject);
      });
    });
    if (result.e) reject(result.v);
    return capability.promise;
  }
});


/***/ }),

/***/ "54a3":
/***/ (function(module, exports, __webpack_require__) {

// to indexed object, toObject with fallback for non-array-like ES3 strings
var IObject = __webpack_require__("240e");
var defined = __webpack_require__("2b11");
module.exports = function (it) {
  return IObject(defined(it));
};


/***/ }),

/***/ "565c":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);

// CONCATENATED MODULE: ./node_modules/_vue-style-loader@4.1.2@vue-style-loader/lib/listToStyles.js
/**
 * Translates the list format produced by css-loader into something
 * easier to manipulate.
 */
function listToStyles (parentId, list) {
  var styles = []
  var newStyles = {}
  for (var i = 0; i < list.length; i++) {
    var item = list[i]
    var id = item[0]
    var css = item[1]
    var media = item[2]
    var sourceMap = item[3]
    var part = {
      id: parentId + ':' + i,
      css: css,
      media: media,
      sourceMap: sourceMap
    }
    if (!newStyles[id]) {
      styles.push(newStyles[id] = { id: id, parts: [part] })
    } else {
      newStyles[id].parts.push(part)
    }
  }
  return styles
}

// CONCATENATED MODULE: ./node_modules/_vue-style-loader@4.1.2@vue-style-loader/lib/addStylesShadow.js
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "default", function() { return addStylesToShadowDOM; });


function addStylesToShadowDOM (parentId, list, shadowRoot) {
  var styles = listToStyles(parentId, list)
  addStyles(styles, shadowRoot)
}

/*
type StyleObject = {
  id: number;
  parts: Array<StyleObjectPart>
}

type StyleObjectPart = {
  css: string;
  media: string;
  sourceMap: ?string
}
*/

function addStyles (styles /* Array<StyleObject> */, shadowRoot) {
  const injectedStyles =
    shadowRoot._injectedStyles ||
    (shadowRoot._injectedStyles = {})
  for (var i = 0; i < styles.length; i++) {
    var item = styles[i]
    var style = injectedStyles[item.id]
    if (!style) {
      for (var j = 0; j < item.parts.length; j++) {
        addStyle(item.parts[j], shadowRoot)
      }
      injectedStyles[item.id] = true
    }
  }
}

function createStyleElement (shadowRoot) {
  var styleElement = document.createElement('style')
  styleElement.type = 'text/css'
  shadowRoot.appendChild(styleElement)
  return styleElement
}

function addStyle (obj /* StyleObjectPart */, shadowRoot) {
  var styleElement = createStyleElement(shadowRoot)
  var css = obj.css
  var media = obj.media
  var sourceMap = obj.sourceMap

  if (media) {
    styleElement.setAttribute('media', media)
  }

  if (sourceMap) {
    // https://developer.chrome.com/devtools/docs/javascript-debugging
    // this makes source maps inside style tags work properly in Chrome
    css += '\n/*# sourceURL=' + sourceMap.sources[0] + ' */'
    // http://stackoverflow.com/a/26603875
    css += '\n/*# sourceMappingURL=data:application/json;base64,' + btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap)))) + ' */'
  }

  if (styleElement.styleSheet) {
    styleElement.styleSheet.cssText = css
  } else {
    while (styleElement.firstChild) {
      styleElement.removeChild(styleElement.firstChild)
    }
    styleElement.appendChild(document.createTextNode(css))
  }
}


/***/ }),

/***/ "56f8":
/***/ (function(module, exports, __webpack_require__) {

// getting tag from 19.1.3.6 Object.prototype.toString()
var cof = __webpack_require__("9b6d");
var TAG = __webpack_require__("f3ae")('toStringTag');
// ES3 wrong here
var ARG = cof(function () { return arguments; }()) == 'Arguments';

// fallback for IE11 Script Access Denied error
var tryGet = function (it, key) {
  try {
    return it[key];
  } catch (e) { /* empty */ }
};

module.exports = function (it) {
  var O, T, B;
  return it === undefined ? 'Undefined' : it === null ? 'Null'
    // @@toStringTag case
    : typeof (T = tryGet(O = Object(it), TAG)) == 'string' ? T
    // builtinTag case
    : ARG ? cof(O)
    // ES3 arguments fallback
    : (B = cof(O)) == 'Object' && typeof O.callee == 'function' ? 'Arguments' : B;
};


/***/ }),

/***/ "577d":
/***/ (function(module, exports, __webpack_require__) {

// check on default Array iterator
var Iterators = __webpack_require__("f03e");
var ITERATOR = __webpack_require__("f3ae")('iterator');
var ArrayProto = Array.prototype;

module.exports = function (it) {
  return it !== undefined && (Iterators.Array === it || ArrayProto[ITERATOR] === it);
};


/***/ }),

/***/ "5b58":
/***/ (function(module, exports, __webpack_require__) {

var document = __webpack_require__("4839").document;
module.exports = document && document.documentElement;


/***/ }),

/***/ "5c07":
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var addToUnscopables = __webpack_require__("68fb");
var step = __webpack_require__("b725");
var Iterators = __webpack_require__("f03e");
var toIObject = __webpack_require__("54a3");

// 22.1.3.4 Array.prototype.entries()
// 22.1.3.13 Array.prototype.keys()
// 22.1.3.29 Array.prototype.values()
// 22.1.3.30 Array.prototype[@@iterator]()
module.exports = __webpack_require__("1145")(Array, 'Array', function (iterated, kind) {
  this._t = toIObject(iterated); // target
  this._i = 0;                   // next index
  this._k = kind;                // kind
// 22.1.5.2.1 %ArrayIteratorPrototype%.next()
}, function () {
  var O = this._t;
  var kind = this._k;
  var index = this._i++;
  if (!O || index >= O.length) {
    this._t = undefined;
    return step(1);
  }
  if (kind == 'keys') return step(0, index);
  if (kind == 'values') return step(0, O[index]);
  return step(0, [index, O[index]]);
}, 'values');

// argumentsList[@@iterator] is %ArrayProto_values% (9.4.4.6, 9.4.4.7)
Iterators.Arguments = Iterators.Array;

addToUnscopables('keys');
addToUnscopables('values');
addToUnscopables('entries');


/***/ }),

/***/ "5ce4":
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(process) {

var utils = __webpack_require__("3332");
var normalizeHeaderName = __webpack_require__("3a0a");

var DEFAULT_CONTENT_TYPE = {
  'Content-Type': 'application/x-www-form-urlencoded'
};

function setContentTypeIfUnset(headers, value) {
  if (!utils.isUndefined(headers) && utils.isUndefined(headers['Content-Type'])) {
    headers['Content-Type'] = value;
  }
}

function getDefaultAdapter() {
  var adapter;
  if (typeof XMLHttpRequest !== 'undefined') {
    // For browsers use XHR adapter
    adapter = __webpack_require__("76fc");
  } else if (typeof process !== 'undefined') {
    // For node use HTTP adapter
    adapter = __webpack_require__("76fc");
  }
  return adapter;
}

var defaults = {
  adapter: getDefaultAdapter(),

  transformRequest: [function transformRequest(data, headers) {
    normalizeHeaderName(headers, 'Content-Type');
    if (utils.isFormData(data) ||
      utils.isArrayBuffer(data) ||
      utils.isBuffer(data) ||
      utils.isStream(data) ||
      utils.isFile(data) ||
      utils.isBlob(data)
    ) {
      return data;
    }
    if (utils.isArrayBufferView(data)) {
      return data.buffer;
    }
    if (utils.isURLSearchParams(data)) {
      setContentTypeIfUnset(headers, 'application/x-www-form-urlencoded;charset=utf-8');
      return data.toString();
    }
    if (utils.isObject(data)) {
      setContentTypeIfUnset(headers, 'application/json;charset=utf-8');
      return JSON.stringify(data);
    }
    return data;
  }],

  transformResponse: [function transformResponse(data) {
    /*eslint no-param-reassign:0*/
    if (typeof data === 'string') {
      try {
        data = JSON.parse(data);
      } catch (e) { /* Ignore */ }
    }
    return data;
  }],

  /**
   * A timeout in milliseconds to abort a request. If set to 0 (default) a
   * timeout is not created.
   */
  timeout: 0,

  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',

  maxContentLength: -1,

  validateStatus: function validateStatus(status) {
    return status >= 200 && status < 300;
  }
};

defaults.headers = {
  common: {
    'Accept': 'application/json, text/plain, */*'
  }
};

utils.forEach(['delete', 'get', 'head'], function forEachMethodNoData(method) {
  defaults.headers[method] = {};
});

utils.forEach(['post', 'put', 'patch'], function forEachMethodWithData(method) {
  defaults.headers[method] = utils.merge(DEFAULT_CONTENT_TYPE);
});

module.exports = defaults;

/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__("4c39")))

/***/ }),

/***/ "5ee9":
/***/ (function(module, exports, __webpack_require__) {

var shared = __webpack_require__("13c4")('keys');
var uid = __webpack_require__("75ff");
module.exports = function (key) {
  return shared[key] || (shared[key] = uid(key));
};


/***/ }),

/***/ "612f":
/***/ (function(module, exports, __webpack_require__) {

var $iterators = __webpack_require__("5c07");
var getKeys = __webpack_require__("d753");
var redefine = __webpack_require__("7f00");
var global = __webpack_require__("4839");
var hide = __webpack_require__("c84b");
var Iterators = __webpack_require__("f03e");
var wks = __webpack_require__("f3ae");
var ITERATOR = wks('iterator');
var TO_STRING_TAG = wks('toStringTag');
var ArrayValues = Iterators.Array;

var DOMIterables = {
  CSSRuleList: true, // TODO: Not spec compliant, should be false.
  CSSStyleDeclaration: false,
  CSSValueList: false,
  ClientRectList: false,
  DOMRectList: false,
  DOMStringList: false,
  DOMTokenList: true,
  DataTransferItemList: false,
  FileList: false,
  HTMLAllCollection: false,
  HTMLCollection: false,
  HTMLFormElement: false,
  HTMLSelectElement: false,
  MediaList: true, // TODO: Not spec compliant, should be false.
  MimeTypeArray: false,
  NamedNodeMap: false,
  NodeList: true,
  PaintRequestList: false,
  Plugin: false,
  PluginArray: false,
  SVGLengthList: false,
  SVGNumberList: false,
  SVGPathSegList: false,
  SVGPointList: false,
  SVGStringList: false,
  SVGTransformList: false,
  SourceBufferList: false,
  StyleSheetList: true, // TODO: Not spec compliant, should be false.
  TextTrackCueList: false,
  TextTrackList: false,
  TouchList: false
};

for (var collections = getKeys(DOMIterables), i = 0; i < collections.length; i++) {
  var NAME = collections[i];
  var explicit = DOMIterables[NAME];
  var Collection = global[NAME];
  var proto = Collection && Collection.prototype;
  var key;
  if (proto) {
    if (!proto[ITERATOR]) hide(proto, ITERATOR, ArrayValues);
    if (!proto[TO_STRING_TAG]) hide(proto, TO_STRING_TAG, NAME);
    Iterators[NAME] = ArrayValues;
    if (explicit) for (key in $iterators) if (!proto[key]) redefine(proto, key, $iterators[key], true);
  }
}


/***/ }),

/***/ "648e":
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var create = __webpack_require__("04ac");
var descriptor = __webpack_require__("b915");
var setToStringTag = __webpack_require__("c67d");
var IteratorPrototype = {};

// 25.1.2.1.1 %IteratorPrototype%[@@iterator]()
__webpack_require__("c84b")(IteratorPrototype, __webpack_require__("f3ae")('iterator'), function () { return this; });

module.exports = function (Constructor, NAME, next) {
  Constructor.prototype = create(IteratorPrototype, { next: descriptor(1, next) });
  setToStringTag(Constructor, NAME + ' Iterator');
};


/***/ }),

/***/ "66fa":
/***/ (function(module, exports) {

var g;

// This works in non-strict mode
g = (function() {
	return this;
})();

try {
	// This works if eval is allowed (see CSP)
	g = g || new Function("return this")();
} catch (e) {
	// This works if the window reference is available
	if (typeof window === "object") g = window;
}

// g can still be undefined, but nothing to do about it...
// We return undefined, instead of nothing here, so it's
// easier to handle this case. if(!global) { ...}

module.exports = g;


/***/ }),

/***/ "68fb":
/***/ (function(module, exports, __webpack_require__) {

// 22.1.3.31 Array.prototype[@@unscopables]
var UNSCOPABLES = __webpack_require__("f3ae")('unscopables');
var ArrayProto = Array.prototype;
if (ArrayProto[UNSCOPABLES] == undefined) __webpack_require__("c84b")(ArrayProto, UNSCOPABLES, {});
module.exports = function (key) {
  ArrayProto[UNSCOPABLES][key] = true;
};


/***/ }),

/***/ "690e":
/***/ (function(module, exports) {

/*
	MIT License http://www.opensource.org/licenses/mit-license.php
	Author Tobias Koppers @sokra
*/
// css base code, injected by the css-loader
module.exports = function(useSourceMap) {
	var list = [];

	// return the list of modules as css string
	list.toString = function toString() {
		return this.map(function (item) {
			var content = cssWithMappingToString(item, useSourceMap);
			if(item[2]) {
				return "@media " + item[2] + "{" + content + "}";
			} else {
				return content;
			}
		}).join("");
	};

	// import a list of modules into the list
	list.i = function(modules, mediaQuery) {
		if(typeof modules === "string")
			modules = [[null, modules, ""]];
		var alreadyImportedModules = {};
		for(var i = 0; i < this.length; i++) {
			var id = this[i][0];
			if(typeof id === "number")
				alreadyImportedModules[id] = true;
		}
		for(i = 0; i < modules.length; i++) {
			var item = modules[i];
			// skip already imported module
			// this implementation is not 100% perfect for weird media query combinations
			//  when a module is imported multiple times with different media queries.
			//  I hope this will never occur (Hey this way we have smaller bundles)
			if(typeof item[0] !== "number" || !alreadyImportedModules[item[0]]) {
				if(mediaQuery && !item[2]) {
					item[2] = mediaQuery;
				} else if(mediaQuery) {
					item[2] = "(" + item[2] + ") and (" + mediaQuery + ")";
				}
				list.push(item);
			}
		}
	};
	return list;
};

function cssWithMappingToString(item, useSourceMap) {
	var content = item[1] || '';
	var cssMapping = item[3];
	if (!cssMapping) {
		return content;
	}

	if (useSourceMap && typeof btoa === 'function') {
		var sourceMapping = toComment(cssMapping);
		var sourceURLs = cssMapping.sources.map(function (source) {
			return '/*# sourceURL=' + cssMapping.sourceRoot + source + ' */'
		});

		return [content].concat(sourceURLs).concat([sourceMapping]).join('\n');
	}

	return [content].join('\n');
}

// Adapted from convert-source-map (MIT)
function toComment(sourceMap) {
	// eslint-disable-next-line no-undef
	var base64 = btoa(unescape(encodeURIComponent(JSON.stringify(sourceMap))));
	var data = 'sourceMappingURL=data:application/json;charset=utf-8;base64,' + base64;

	return '/*# ' + data + ' */';
}


/***/ }),

/***/ "694f":
/***/ (function(module, exports, __webpack_require__) {

var anObject = __webpack_require__("4d65");
var IE8_DOM_DEFINE = __webpack_require__("8003");
var toPrimitive = __webpack_require__("1f51");
var dP = Object.defineProperty;

exports.f = __webpack_require__("3a0f") ? Object.defineProperty : function defineProperty(O, P, Attributes) {
  anObject(O);
  P = toPrimitive(P, true);
  anObject(Attributes);
  if (IE8_DOM_DEFINE) try {
    return dP(O, P, Attributes);
  } catch (e) { /* empty */ }
  if ('get' in Attributes || 'set' in Attributes) throw TypeError('Accessors not supported!');
  if ('value' in Attributes) O[P] = Attributes.value;
  return O;
};


/***/ }),

/***/ "6ccd":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_6_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_6_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_3_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Pagination_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__("7b4a");
/* harmony import */ var _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_6_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_6_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_3_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Pagination_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_6_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_6_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_3_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Pagination_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_6_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_6_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_3_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Pagination_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_6_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_6_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_3_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Pagination_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_6_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_6_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_6_oneOf_1_3_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Pagination_vue_vue_type_style_index_0_lang_css___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ "6f8f":
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__("13c4")('native-function-to-string', Function.toString);


/***/ }),

/***/ "70e7":
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__("8dcd");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add CSS to Shadow Root
var add = __webpack_require__("565c").default
module.exports.__inject__ = function (shadowRoot) {
  add("6527aeed", content, shadowRoot)
};

/***/ }),

/***/ "7535":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);

// CONCATENATED MODULE: ./node_modules/_@vue_cli-service@3.7.0@@vue/cli-service/lib/commands/build/setPublicPath.js
// This file is imported into lib/wc client bundles.

if (typeof window !== 'undefined') {
  if (Object({"NODE_ENV":"production","BASE_URL":"/"}).NEED_CURRENTSCRIPT_POLYFILL) {
    __webpack_require__("e67d")
  }

  var i
  if ((i = window.document.currentScript) && (i = i.src.match(/(.+\/)[^/]+\.js(\?.*)?$/))) {
    __webpack_require__.p = i[1] // eslint-disable-line
  }
}

// Indicate to webpack that this file can be concatenated
/* harmony default export */ var setPublicPath = (null);

// EXTERNAL MODULE: external "Vue"
var external_Vue_ = __webpack_require__("8bbf");
var external_Vue_default = /*#__PURE__*/__webpack_require__.n(external_Vue_);

// CONCATENATED MODULE: ./node_modules/_@vue_web-component-wrapper@1.2.0@@vue/web-component-wrapper/dist/vue-wc-wrapper.js
const camelizeRE = /-(\w)/g;
const camelize = str => {
  return str.replace(camelizeRE, (_, c) => c ? c.toUpperCase() : '')
};

const hyphenateRE = /\B([A-Z])/g;
const hyphenate = str => {
  return str.replace(hyphenateRE, '-$1').toLowerCase()
};

function getInitialProps (propsList) {
  const res = {};
  propsList.forEach(key => {
    res[key] = undefined;
  });
  return res
}

function injectHook (options, key, hook) {
  options[key] = [].concat(options[key] || []);
  options[key].unshift(hook);
}

function callHooks (vm, hook) {
  if (vm) {
    const hooks = vm.$options[hook] || [];
    hooks.forEach(hook => {
      hook.call(vm);
    });
  }
}

function createCustomEvent (name, args) {
  return new CustomEvent(name, {
    bubbles: false,
    cancelable: false,
    detail: args
  })
}

const isBoolean = val => /function Boolean/.test(String(val));
const isNumber = val => /function Number/.test(String(val));

function convertAttributeValue (value, name, { type } = {}) {
  if (isBoolean(type)) {
    if (value === 'true' || value === 'false') {
      return value === 'true'
    }
    if (value === '' || value === name) {
      return true
    }
    return value != null
  } else if (isNumber(type)) {
    const parsed = parseFloat(value, 10);
    return isNaN(parsed) ? value : parsed
  } else {
    return value
  }
}

function toVNodes (h, children) {
  const res = [];
  for (let i = 0, l = children.length; i < l; i++) {
    res.push(toVNode(h, children[i]));
  }
  return res
}

function toVNode (h, node) {
  if (node.nodeType === 3) {
    return node.data.trim() ? node.data : null
  } else if (node.nodeType === 1) {
    const data = {
      attrs: getAttributes(node),
      domProps: {
        innerHTML: node.innerHTML
      }
    };
    if (data.attrs.slot) {
      data.slot = data.attrs.slot;
      delete data.attrs.slot;
    }
    return h(node.tagName, data)
  } else {
    return null
  }
}

function getAttributes (node) {
  const res = {};
  for (let i = 0, l = node.attributes.length; i < l; i++) {
    const attr = node.attributes[i];
    res[attr.nodeName] = attr.nodeValue;
  }
  return res
}

function wrap (Vue, Component) {
  const isAsync = typeof Component === 'function' && !Component.cid;
  let isInitialized = false;
  let hyphenatedPropsList;
  let camelizedPropsList;
  let camelizedPropsMap;

  function initialize (Component) {
    if (isInitialized) return

    const options = typeof Component === 'function'
      ? Component.options
      : Component;

    // extract props info
    const propsList = Array.isArray(options.props)
      ? options.props
      : Object.keys(options.props || {});
    hyphenatedPropsList = propsList.map(hyphenate);
    camelizedPropsList = propsList.map(camelize);
    const originalPropsAsObject = Array.isArray(options.props) ? {} : options.props || {};
    camelizedPropsMap = camelizedPropsList.reduce((map, key, i) => {
      map[key] = originalPropsAsObject[propsList[i]];
      return map
    }, {});

    // proxy $emit to native DOM events
    injectHook(options, 'beforeCreate', function () {
      const emit = this.$emit;
      this.$emit = (name, ...args) => {
        this.$root.$options.customElement.dispatchEvent(createCustomEvent(name, args));
        return emit.call(this, name, ...args)
      };
    });

    injectHook(options, 'created', function () {
      // sync default props values to wrapper on created
      camelizedPropsList.forEach(key => {
        this.$root.props[key] = this[key];
      });
    });

    // proxy props as Element properties
    camelizedPropsList.forEach(key => {
      Object.defineProperty(CustomElement.prototype, key, {
        get () {
          return this._wrapper.props[key]
        },
        set (newVal) {
          this._wrapper.props[key] = newVal;
        },
        enumerable: false,
        configurable: true
      });
    });

    isInitialized = true;
  }

  function syncAttribute (el, key) {
    const camelized = camelize(key);
    const value = el.hasAttribute(key) ? el.getAttribute(key) : undefined;
    el._wrapper.props[camelized] = convertAttributeValue(
      value,
      key,
      camelizedPropsMap[camelized]
    );
  }

  class CustomElement extends HTMLElement {
    constructor () {
      super();
      this.attachShadow({ mode: 'open' });

      const wrapper = this._wrapper = new Vue({
        name: 'shadow-root',
        customElement: this,
        shadowRoot: this.shadowRoot,
        data () {
          return {
            props: {},
            slotChildren: []
          }
        },
        render (h) {
          return h(Component, {
            ref: 'inner',
            props: this.props
          }, this.slotChildren)
        }
      });

      // Use MutationObserver to react to future attribute & slot content change
      const observer = new MutationObserver(mutations => {
        let hasChildrenChange = false;
        for (let i = 0; i < mutations.length; i++) {
          const m = mutations[i];
          if (isInitialized && m.type === 'attributes' && m.target === this) {
            syncAttribute(this, m.attributeName);
          } else {
            hasChildrenChange = true;
          }
        }
        if (hasChildrenChange) {
          wrapper.slotChildren = Object.freeze(toVNodes(
            wrapper.$createElement,
            this.childNodes
          ));
        }
      });
      observer.observe(this, {
        childList: true,
        subtree: true,
        characterData: true,
        attributes: true
      });
    }

    get vueComponent () {
      return this._wrapper.$refs.inner
    }

    connectedCallback () {
      const wrapper = this._wrapper;
      if (!wrapper._isMounted) {
        // initialize attributes
        const syncInitialAttributes = () => {
          wrapper.props = getInitialProps(camelizedPropsList);
          hyphenatedPropsList.forEach(key => {
            syncAttribute(this, key);
          });
        };

        if (isInitialized) {
          syncInitialAttributes();
        } else {
          // async & unresolved
          Component().then(resolved => {
            if (resolved.__esModule || resolved[Symbol.toStringTag] === 'Module') {
              resolved = resolved.default;
            }
            initialize(resolved);
            syncInitialAttributes();
          });
        }
        // initialize children
        wrapper.slotChildren = Object.freeze(toVNodes(
          wrapper.$createElement,
          this.childNodes
        ));
        wrapper.$mount();
        this.shadowRoot.appendChild(wrapper.$el);
      } else {
        callHooks(this.vueComponent, 'activated');
      }
    }

    disconnectedCallback () {
      callHooks(this.vueComponent, 'deactivated');
    }
  }

  if (!isAsync) {
    initialize(Component);
  }

  return CustomElement
}

/* harmony default export */ var vue_wc_wrapper = (wrap);

// EXTERNAL MODULE: ./node_modules/_css-loader@1.0.1@css-loader/lib/css-base.js
var css_base = __webpack_require__("690e");

// EXTERNAL MODULE: ./node_modules/_vue-style-loader@4.1.2@vue-style-loader/lib/addStylesShadow.js + 1 modules
var addStylesShadow = __webpack_require__("565c");

// CONCATENATED MODULE: ./node_modules/_vue-loader@15.7.0@vue-loader/lib/runtime/componentNormalizer.js
/* globals __VUE_SSR_CONTEXT__ */

// IMPORTANT: Do NOT use ES2015 features in this file (except for modules).
// This module is a runtime utility for cleaner component module output and will
// be included in the final webpack user bundle.

function normalizeComponent (
  scriptExports,
  render,
  staticRenderFns,
  functionalTemplate,
  injectStyles,
  scopeId,
  moduleIdentifier, /* server only */
  shadowMode /* vue-cli only */
) {
  // Vue.extend constructor export interop
  var options = typeof scriptExports === 'function'
    ? scriptExports.options
    : scriptExports

  // render functions
  if (render) {
    options.render = render
    options.staticRenderFns = staticRenderFns
    options._compiled = true
  }

  // functional template
  if (functionalTemplate) {
    options.functional = true
  }

  // scopedId
  if (scopeId) {
    options._scopeId = 'data-v-' + scopeId
  }

  var hook
  if (moduleIdentifier) { // server build
    hook = function (context) {
      // 2.3 injection
      context =
        context || // cached call
        (this.$vnode && this.$vnode.ssrContext) || // stateful
        (this.parent && this.parent.$vnode && this.parent.$vnode.ssrContext) // functional
      // 2.2 with runInNewContext: true
      if (!context && typeof __VUE_SSR_CONTEXT__ !== 'undefined') {
        context = __VUE_SSR_CONTEXT__
      }
      // inject component styles
      if (injectStyles) {
        injectStyles.call(this, context)
      }
      // register component module identifier for async chunk inferrence
      if (context && context._registeredComponents) {
        context._registeredComponents.add(moduleIdentifier)
      }
    }
    // used by ssr in case component is cached and beforeCreate
    // never gets called
    options._ssrRegister = hook
  } else if (injectStyles) {
    hook = shadowMode
      ? function () { injectStyles.call(this, this.$root.$options.shadowRoot) }
      : injectStyles
  }

  if (hook) {
    if (options.functional) {
      // for template-only hot-reload because in that case the render fn doesn't
      // go through the normalizer
      options._injectStyles = hook
      // register for functioal component in vue file
      var originalRender = options.render
      options.render = function renderWithStyleInjection (h, context) {
        hook.call(context)
        return originalRender(h, context)
      }
    } else {
      // inject component registration as beforeCreate hook
      var existing = options.beforeCreate
      options.beforeCreate = existing
        ? [].concat(existing, hook)
        : [hook]
    }
  }

  return {
    exports: scriptExports,
    options: options
  }
}

// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js?{"cacheDirectory":"node_modules/.cache/vue-loader","cacheIdentifier":"0baef6aa-vue-loader-template"}!./node_modules/_vue-loader@15.7.0@vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/Comment.vue?vue&type=template&id=244575d4&shadow
var render = function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"comment-wrapper"},[_c('div',{staticClass:"divider text-center",attrs:{"data-content":"撰写评论"}}),_c('comment-node',[_c('figure',{staticClass:"avatar avatar-lg",attrs:{"slot":"comment-icon"},slot:"comment-icon"},[_c('img',{attrs:{"src":_vm.avatar,"alt":"Annoymouse"}})]),_c('div',{staticClass:"panel",attrs:{"slot":"comment-content"},slot:"comment-content"},[(_vm.replyComment)?_c('div',{staticClass:"panel-header"},[_vm._v("\n        回复: "+_vm._s(_vm.replyComment.author)+"\n        "),_c('blockquote',{staticClass:"blockquote",domProps:{"innerHTML":_vm._s(_vm.compiledReplyCommentContent)}}),_c('button',{staticClass:"btn",on:{"click":function($event){_vm.replyComment = null}}},[_c('i',{staticClass:"icon icon-cross"}),_vm._v("\n          取消\n        ")])]):_vm._e(),_c('div',{staticClass:"panel-nav"},[_c('ul',{staticClass:"tab"},[_c('li',{staticClass:"tab-item"},[_c('a',{class:{active: _vm.editActivated},on:{"click":_vm.editActivate}},[_vm._v("编辑")])]),_c('li',{staticClass:"tab-item"},[_c('a',{class:{active: _vm.previewActivated},on:{"click":_vm.previewActivate}},[_vm._v("预览")])])])]),_c('div',{staticClass:"panel-body"},[_c('form',{directives:[{name:"show",rawName:"v-show",value:(_vm.editActivated),expression:"editActivated"}]},[_c('div',{staticClass:"columns input-wrapper"},[_c('input',{directives:[{name:"model",rawName:"v-model",value:(_vm.comment.author),expression:"comment.author"}],staticClass:"form-input col-4 col-sm-12 halo-input",attrs:{"placeholder":"*昵称"},domProps:{"value":(_vm.comment.author)},on:{"input":function($event){if($event.target.composing){ return; }_vm.$set(_vm.comment, "author", $event.target.value)}}}),_c('input',{directives:[{name:"model",rawName:"v-model",value:(_vm.comment.email),expression:"comment.email"}],staticClass:"form-input col-4 col-sm-12 halo-input",attrs:{"type":"email","placeholder":"*邮箱","pattern":"[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,14}$"},domProps:{"value":(_vm.comment.email)},on:{"input":function($event){if($event.target.composing){ return; }_vm.$set(_vm.comment, "email", $event.target.value)}}}),_c('input',{directives:[{name:"model",rawName:"v-model",value:(_vm.comment.authorUrl),expression:"comment.authorUrl"}],staticClass:"form-input col-4 col-sm-12 halo-input",attrs:{"placeholder":"个人网址"},domProps:{"value":(_vm.comment.authorUrl)},on:{"input":function($event){if($event.target.composing){ return; }_vm.$set(_vm.comment, "authorUrl", $event.target.value)}}})]),_c('textarea',{directives:[{name:"model",rawName:"v-model",value:(_vm.comment.content),expression:"comment.content"}],staticClass:"form-input comment-textarea halo-input",attrs:{"name":"comment-editor","id":"comment-editor","rows":"6"},domProps:{"value":(_vm.comment.content)},on:{"input":function($event){if($event.target.composing){ return; }_vm.$set(_vm.comment, "content", $event.target.value)}}}),_c('p',{staticClass:"form-input-hint comment-textarea-tip"},[_vm._v("Markdown Support")])]),_c('div',{directives:[{name:"show",rawName:"v-show",value:(_vm.previewActivated),expression:"previewActivated"}]},[_c('div',{staticClass:"markdown-content",domProps:{"innerHTML":_vm._s(_vm.compileContent)}})])]),_c('div',{staticClass:"panel-footer"},[_vm._l((_vm.errors),function(error,index){return _c('div',{key:index,staticClass:"toast toast-error"},[_c('button',{staticClass:"btn btn-clear float-right",on:{"click":function($event){_vm.fieldError = null}}}),_vm._v("\n          "+_vm._s(error)+"\n        ")])}),(_vm.tip)?_c('div',{staticClass:"toast toast-success"},[_c('button',{staticClass:"btn btn-clear float-right",on:{"click":function($event){_vm.tip = null}}}),_vm._v("\n          "+_vm._s(_vm.tip)+"\n        ")]):_vm._e(),_c('button',{staticClass:"halo-Button",on:{"click":_vm.handleComment}},[_vm._v("提交")])],2)])]),_c('div',{staticClass:"divider text-center",attrs:{"data-content":"评论详情"}}),_vm._l((_vm.comments),function(comment,index){return _c('comment-tree',{key:index,attrs:{"comment":comment},on:{"reply":_vm.handleReplyClick}})}),(!_vm.havingComment)?_c('div',{staticClass:"empty"},[_vm._m(0),_c('p',{staticClass:"empty-title h5"},[_vm._v("当前还没有人留言")]),_c('p',{staticClass:"empty-subtitle"},[_vm._v("有什么想对作者说的么?")]),_vm._m(1)]):_c('pagination',{attrs:{"size":_vm.pagination.rpp,"total":_vm.pagination.total},on:{"change":_vm.handlePaginationChange},model:{value:(_vm.pagination.page),callback:function ($$v) {_vm.$set(_vm.pagination, "page", $$v)},expression:"pagination.page"}})],2)}
var staticRenderFns = [function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"empty-icon"},[_c('i',{staticClass:"icon icon-3x icon-people"})])},function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',{staticClass:"empty-action"},[_c('button',{staticClass:"halo-Button"},[_vm._v("请留言")])])}]


// CONCATENATED MODULE: ./src/components/Comment.vue?vue&type=template&id=244575d4&shadow

// EXTERNAL MODULE: ./node_modules/_core-js@2.6.5@core-js/modules/web.dom.iterable.js
var web_dom_iterable = __webpack_require__("612f");

// EXTERNAL MODULE: ./node_modules/_core-js@2.6.5@core-js/modules/es6.promise.js
var es6_promise = __webpack_require__("53da");

// EXTERNAL MODULE: ./node_modules/_axios@0.18.0@axios/index.js
var _axios_0_18_0_axios = __webpack_require__("7f43");
var _axios_0_18_0_axios_default = /*#__PURE__*/__webpack_require__.n(_axios_0_18_0_axios);

// EXTERNAL MODULE: ./node_modules/_nprogress@0.2.0@nprogress/nprogress.js
var nprogress = __webpack_require__("38bc");
var nprogress_default = /*#__PURE__*/__webpack_require__.n(nprogress);

// EXTERNAL MODULE: ./node_modules/_nprogress@0.2.0@nprogress/nprogress.css
var _nprogress_0_2_0_nprogress_nprogress = __webpack_require__("70e7");

// CONCATENATED MODULE: ./src/utils/service.js



 // import Vue from "vue";

var service = _axios_0_18_0_axios_default.a.create({
  baseURL:  true ? '' : undefined,
  timeout: 5000,
  withCredentials: true
});
service.interceptors.request.use(config => {
  nprogress_default.a.start();
  return config;
}, error => {
  nprogress_default.a.remove();
  return Promise.reject(error);
});
service.interceptors.response.use(response => {
  nprogress_default.a.done();
  return response;
}, error => {
  nprogress_default.a.done();

  if (_axios_0_18_0_axios_default.a.isCancel(error)) {
    // Vue.$log.debug("Cancelled uploading by user.");
    return Promise.reject(error);
  } // Vue.$log.error("Response failed", error);


  var response = error.response; // const status = response ? response.status : -1;
  // Vue.$log.error("Server response status", status);

  var data = response ? response.data : null;

  if (data) {
    // Business response
    // Vue.$log.error("Business response status", data.status);
    if (data.status === 400) {// TODO handle 400 status error
    } else if (data.status === 401) {// TODO Handle 401 status error
    } else if (data.status === 403) {// TODO handle 403 status error
    } else if (data.status === 404) {// TODO handle 404 status error
    } else if (data.status === 500) {// TODO handle 500 status error
    }
  } else {// TODO Server unavailable
    }

  return Promise.reject(error);
});
/* harmony default export */ var utils_service = (service);
// CONCATENATED MODULE: ./src/apis/comment.js

var baseUrl = '/api/content';
var commentApi = {};
/**
 * Lists comment.
 * @param {String} target
 * @param {Object} view
 */

function listComment(target, targetId, view, pagination) {
  return utils_service({
    url: `${baseUrl}/${target}/${targetId}/comments/${view}`,
    params: pagination,
    method: 'get'
  });
}
/**
 * Creates a comment.
 * @param {String} target
 * @param {Object} comment
 */


function createComment(target, comment) {
  return utils_service({
    url: `${baseUrl}/${target}/comments`,
    method: 'post',
    data: comment
  });
} // List api


commentApi.listPostComment = function (postId, pagination) {
  var view = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 'list_view';
  return listComment('posts', postId, view, pagination);
};

commentApi.listSheetComment = function (postId, pagination) {
  var view = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 'list_view';
  return listComment('sheets', postId, view, pagination);
};

commentApi.listJournalComment = function (postId, pagination) {
  var view = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 'list_view';
  return listComment('journals', postId, view, pagination);
}; // Creation api


commentApi.createPostComment = comment => {
  return createComment('posts', comment);
};

commentApi.createSheetComment = comment => {
  return createComment('sheets', comment);
};

commentApi.createJournalComment = comment => {
  return createComment('journals', comment);
};

commentApi.createComment = (comment, type) => {
  if (type == 'sheet') {
    return commentApi.createSheetComment(comment);
  }

  if (type == 'journal') {
    return commentApi.createJournalComment(comment);
  }

  return commentApi.createPostComment(comment);
};

/* harmony default export */ var apis_comment = (commentApi);
// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js?{"cacheDirectory":"node_modules/.cache/vue-loader","cacheIdentifier":"0baef6aa-vue-loader-template"}!./node_modules/_vue-loader@15.7.0@vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/CommentTree.vue?vue&type=template&id=3a44dc66&
var CommentTreevue_type_template_id_3a44dc66_render = function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[(_vm.comment)?_c('comment-node',[_c('figure',{staticClass:"avatar avatar-lg",attrs:{"slot":"comment-icon"},slot:"comment-icon"},[_c('img',{attrs:{"src":_vm.avatar,"alt":_vm.comment.author}})]),_c('p',{staticClass:"comment-author-wrapper",attrs:{"slot":"comment-title"},slot:"comment-title"},[_c('span',{staticClass:"comment-author"},[_vm._v(_vm._s(_vm.comment.author))]),_c('span',{staticClass:"comment-author-time m-2"},[_vm._v(_vm._s(_vm.createTimeAgo)+" ago")])]),_c('p',{attrs:{"slot":"comment-content"},domProps:{"innerHTML":_vm._s(_vm.compileContent)},slot:"comment-content"}),_c('template',{slot:"comment-action-bottom"},[_c('p',[_c('button',{staticClass:"btn btn-sm btn-link",on:{"click":_vm.handleReplyClick}},[_vm._v("回复")])])]),(_vm.comment.children)?_vm._l((_vm.comment.children),function(child,index){return _c('comment-tree',{key:index,attrs:{"slot":"comment-inner","comment":child},on:{"reply":_vm.handleSubReply},slot:"comment-inner"})}):_vm._e()],2):_vm._e()],1)}
var CommentTreevue_type_template_id_3a44dc66_staticRenderFns = []


// CONCATENATED MODULE: ./src/components/CommentTree.vue?vue&type=template&id=3a44dc66&

// CONCATENATED MODULE: ./src/utils/time.js
function pluralize(time, label) {
  if (time <= 1) {
    return time + ' ' + label;
  }

  return time + ' ' + label + 's';
}
/**
 * time ago
 * @param {*} time
 */

function timeAgo(time) {
  var between = (Date.now() - Number(time)) / 1000;

  if (between < 3600) {
    return pluralize(~~(between / 60), ' minute');
  } else if (between < 86400) {
    return pluralize(~~(between / 3600), ' hour');
  } else {
    return pluralize(~~(between / 86400), ' day');
  }
}
// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js?{"cacheDirectory":"node_modules/.cache/vue-loader","cacheIdentifier":"0baef6aa-vue-loader-template"}!./node_modules/_vue-loader@15.7.0@vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/CommentNode.vue?vue&type=template&id=18c54e3e&
var CommentNodevue_type_template_id_18c54e3e_render = function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('div',{staticClass:"tile"},[_c('div',{staticClass:"tile-icon"},[_vm._t("comment-icon")],2),_c('div',{staticClass:"tile-content"},[_c('div',{staticClass:"tile-title"},[_vm._t("comment-title")],2),_c('div',{staticClass:"tile-subtitle"},[_vm._t("comment-content")],2),_vm._t("comment-action-bottom"),_vm._t("comment-inner")],2),_c('div',{staticClass:"tile-action"},[_vm._t("comment-action")],2)])])}
var CommentNodevue_type_template_id_18c54e3e_staticRenderFns = []


// CONCATENATED MODULE: ./src/components/CommentNode.vue?vue&type=template&id=18c54e3e&

// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--12-0!./node_modules/_thread-loader@2.1.2@thread-loader/dist/cjs.js!./node_modules/_babel-loader@8.0.5@babel-loader/lib!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/CommentNode.vue?vue&type=script&lang=js&
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
/* harmony default export */ var CommentNodevue_type_script_lang_js_ = ({
  name: 'CommentNode'
});
// CONCATENATED MODULE: ./src/components/CommentNode.vue?vue&type=script&lang=js&
 /* harmony default export */ var components_CommentNodevue_type_script_lang_js_ = (CommentNodevue_type_script_lang_js_); 
// CONCATENATED MODULE: ./src/components/CommentNode.vue



function injectStyles (context) {
  
  
}

/* normalize component */

var component = normalizeComponent(
  components_CommentNodevue_type_script_lang_js_,
  CommentNodevue_type_template_id_18c54e3e_render,
  CommentNodevue_type_template_id_18c54e3e_staticRenderFns,
  false,
  injectStyles,
  null,
  null
  ,true
)

/* harmony default export */ var CommentNode = (component.exports);
// EXTERNAL MODULE: ./node_modules/_marked@0.6.2@marked/lib/marked.js
var marked = __webpack_require__("ae4d");
var marked_default = /*#__PURE__*/__webpack_require__.n(marked);

// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--12-0!./node_modules/_thread-loader@2.1.2@thread-loader/dist/cjs.js!./node_modules/_babel-loader@8.0.5@babel-loader/lib!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/CommentTree.vue?vue&type=script&lang=js&
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//



/* harmony default export */ var CommentTreevue_type_script_lang_js_ = ({
  name: 'CommentTree',
  components: {
    CommentNode: CommentNode
  },
  props: {
    comment: {
      type: Object,
      required: false,
      default: null
    }
  },
  computed: {
    avatar() {
      return `//gravatar.loli.net/avatar/${this.comment.gavatarMd5}/?s=256&d=mp`;
    },

    createTimeAgo() {
      return timeAgo(this.comment.createTime);
    },

    compileContent() {
      return marked_default()(this.comment.content, {
        sanitize: true
      });
    }

  },
  methods: {
    handleReplyClick() {
      this.$emit('reply', this.comment);
    },

    handleSubReply(comment) {
      this.$emit('reply', comment);
    }

  }
});
// CONCATENATED MODULE: ./src/components/CommentTree.vue?vue&type=script&lang=js&
 /* harmony default export */ var components_CommentTreevue_type_script_lang_js_ = (CommentTreevue_type_script_lang_js_); 
// CONCATENATED MODULE: ./src/components/CommentTree.vue



function CommentTree_injectStyles (context) {
  
  var style0 = __webpack_require__("8c5d")
if (style0.__inject__) style0.__inject__(context)

}

/* normalize component */

var CommentTree_component = normalizeComponent(
  components_CommentTreevue_type_script_lang_js_,
  CommentTreevue_type_template_id_3a44dc66_render,
  CommentTreevue_type_template_id_3a44dc66_staticRenderFns,
  false,
  CommentTree_injectStyles,
  null,
  null
  ,true
)

/* harmony default export */ var CommentTree = (CommentTree_component.exports);
// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js?{"cacheDirectory":"node_modules/.cache/vue-loader","cacheIdentifier":"0baef6aa-vue-loader-template"}!./node_modules/_vue-loader@15.7.0@vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/Pagination.vue?vue&type=template&id=361767eb&
var Paginationvue_type_template_id_361767eb_render = function () {var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;return _c('div',[_c('ul',{staticClass:"pagination"},[_c('li',{staticClass:"page-item",class:{ disabled: !_vm.hasPrev }},[_c('a',{attrs:{"tabindex":"-1","href":"javascript:void(0)"},on:{"click":_vm.handlePrevClick}},[_vm._v("Previous")])]),(_vm.firstPage != null)?_c('li',{staticClass:"page-item",class:{ active: _vm.page === _vm.firstPage}},[_c('a',{attrs:{"href":"javascript:void(0)"},on:{"click":function($event){return _vm.handlePageItemClick(_vm.firstPage)}}},[_vm._v(_vm._s(_vm.firstPage + 1))])]):_vm._e(),_c('li',{directives:[{name:"show",rawName:"v-show",value:(_vm.hasMorePrev),expression:"hasMorePrev"}],staticClass:"page-item"},[_c('span',[_vm._v("...")])]),_vm._l((_vm.middlePages),function(middlePage){return _c('li',{key:middlePage,staticClass:"page-item",class:{ active: middlePage === _vm.page }},[_c('a',{attrs:{"href":"javascript:void(0)"},on:{"click":function($event){return _vm.handlePageItemClick(middlePage)}}},[_vm._v("\n        "+_vm._s(middlePage + 1)+"\n      ")])])}),_c('li',{directives:[{name:"show",rawName:"v-show",value:(_vm.hasMoreNext),expression:"hasMoreNext"}],staticClass:"page-item"},[_c('span',[_vm._v("...")])]),(_vm.lastPage)?_c('li',{staticClass:"page-item",class:{ active: _vm.page === _vm.lastPage}},[_c('a',{attrs:{"href":"javascript:void(0)"},on:{"click":function($event){return _vm.handlePageItemClick(_vm.lastPage)}}},[_vm._v("\n        "+_vm._s(_vm.lastPage + 1)+"\n      ")])]):_vm._e(),_c('li',{staticClass:"page-item",class:{ disabled: !_vm.hasNext }},[_c('a',{attrs:{"href":"javascript:void(0)"},on:{"click":_vm.handleNextClick}},[_vm._v("Next")])])],2)])}
var Paginationvue_type_template_id_361767eb_staticRenderFns = []


// CONCATENATED MODULE: ./src/components/Pagination.vue?vue&type=template&id=361767eb&

// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--12-0!./node_modules/_thread-loader@2.1.2@thread-loader/dist/cjs.js!./node_modules/_babel-loader@8.0.5@babel-loader/lib!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/Pagination.vue?vue&type=script&lang=js&
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
/* harmony default export */ var Paginationvue_type_script_lang_js_ = ({
  name: 'Pagination',
  model: {
    prop: 'page',
    event: 'change'
  },
  props: {
    page: {
      type: Number,
      required: false,
      default: 0
    },
    size: {
      type: Number,
      required: false,
      default: 10
    },
    total: {
      type: Number,
      required: false,
      default: 0
    }
  },

  data() {
    return {
      middleSize: 5
    };
  },

  computed: {
    pages() {
      return Math.ceil(this.total / this.size);
    },

    hasNext() {
      return this.page < this.pages - 1;
    },

    hasPrev() {
      return this.page > 0;
    },

    firstPage() {
      if (this.pages === 0) {
        return null;
      }

      return 0;
    },

    hasMorePrev() {
      if (this.firstPage === null) {
        return false;
      }

      return this.page - 2 > this.firstPage;
    },

    hasMoreNext() {
      if (this.lastPage === null) {
        return false;
      }

      return this.page + 2 < this.lastPage;
    },

    middlePages() {
      if (this.pages <= 2) {
        return [];
      }

      if (this.pages <= 2 + this.middleSize) {
        return this.range(1, this.lastPage);
      }

      var halfMiddleSize = Math.floor(this.middleSize / 2);
      var left = this.page - halfMiddleSize;
      var right = this.page + halfMiddleSize;

      if (this.page <= this.firstPage + halfMiddleSize + 1) {
        left = this.firstPage + 1;
        right = left + this.middleSize - 1;
      } else if (this.page >= this.lastPage - halfMiddleSize - 1) {
        right = this.lastPage - 1;
        left = right - this.middleSize + 1;
      }

      return this.range(left, right + 1);
    },

    lastPage() {
      if (this.pages === 0 || this.pages === 1) {
        return 0;
      }

      return this.pages - 1;
    }

  },
  methods: {
    handleNextClick() {
      if (this.hasNext) {
        this.$emit('change', this.page + 1);
      }
    },

    handlePrevClick() {
      if (this.hasPrev) {
        this.$emit('change', this.page - 1);
      }
    },

    handlePageItemClick(page) {
      this.$emit('change', page);
    },

    range(left, right) {
      if (left >= right) {
        return [];
      }

      var result = [];

      for (var i = left; i < right; i++) {
        result.push(i);
      }

      return result;
    }

  }
});
// CONCATENATED MODULE: ./src/components/Pagination.vue?vue&type=script&lang=js&
 /* harmony default export */ var components_Paginationvue_type_script_lang_js_ = (Paginationvue_type_script_lang_js_); 
// CONCATENATED MODULE: ./src/components/Pagination.vue



function Pagination_injectStyles (context) {
  
  var style0 = __webpack_require__("6ccd")
if (style0.__inject__) style0.__inject__(context)

}

/* normalize component */

var Pagination_component = normalizeComponent(
  components_Paginationvue_type_script_lang_js_,
  Paginationvue_type_template_id_361767eb_render,
  Paginationvue_type_template_id_361767eb_staticRenderFns,
  false,
  Pagination_injectStyles,
  null,
  null
  ,true
)

/* harmony default export */ var Pagination = (Pagination_component.exports);
// CONCATENATED MODULE: ./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--12-0!./node_modules/_thread-loader@2.1.2@thread-loader/dist/cjs.js!./node_modules/_babel-loader@8.0.5@babel-loader/lib!./node_modules/_cache-loader@2.0.1@cache-loader/dist/cjs.js??ref--0-0!./node_modules/_vue-loader@15.7.0@vue-loader/lib??vue-loader-options!./src/components/Comment.vue?vue&type=script&lang=js&shadow

//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//





/* harmony default export */ var Commentvue_type_script_lang_js_shadow = ({
  name: 'Comment',
  components: {
    CommentTree: CommentTree,
    CommentNode: CommentNode,
    Pagination: Pagination
  },
  props: {
    id: {
      type: Number,
      required: false,
      default: 0
    },
    type: {
      type: String,
      required: false,
      default: 'post',
      validator: function validator(value) {
        // The value must match one of these strings
        return ['post', 'sheet', 'journal'].indexOf(value) !== -1;
      }
    }
  },

  data() {
    return {
      comments: [],
      pagination: {
        page: 0,
        size: 10,
        total: 0
      },
      editActivated: true,
      previewActivated: false,
      replyComment: null,
      comment: {
        author: null,
        authorUrl: null,
        content: '# Hello World!',
        email: null,
        parentId: 0,
        postId: this.id
      },
      fieldError: null,
      tip: null
    };
  },

  computed: {
    avatar() {
      return `//gravatar.loli.net/avatar/${this.comment.gavatarMd5}/?s=256&d=mp`;
    },

    compileContent() {
      return marked_default()(this.comment.content, {
        sanitize: true
      });
    },

    compiledReplyCommentContent() {
      if (!this.replyComment) {
        return null;
      }

      return marked_default()(this.replyComment.content, {
        sanitize: true
      });
    },

    havingComment() {
      return this.comments && this.comments.length > 0;
    },

    errors() {
      var errors = [];

      if (this.fieldError) {
        Object.keys(this.fieldError).forEach(key => {
          errors.push(this.fieldError[key]);
        });
      }

      return errors;
    }

  },

  created() {
    this.loadComments(); // Load user data

    this.comment.author = localStorage.getItem('author');
    this.comment.email = localStorage.getItem('email');
    this.comment.authorUrl = localStorage.getItem('authorUrl');
    this.comment.gavatarMd5 = localStorage.getItem('avatar');
  },

  methods: {
    editActivate() {
      this.editActivated = true;
      this.previewActivated = false;
    },

    previewActivate() {
      this.editActivated = false;
      this.previewActivated = true;
    },

    loadComments() {
      apis_comment.listPostComment(this.id, this.pagination, 'tree_view').then(response => {
        this.comments = response.data.data.content;
        this.pagination.total = response.data.data.total;
        this.pagination.rpp = response.data.data.rpp;
      });
    },

    handlePaginationChange() {
      this.loadComments();
    },

    handleReplyClick(comment) {
      this.replyComment = comment;
    },

    handleComment() {
      if (this.replyComment) {
        this.comment.parentId = this.replyComment.id;
      }

      this.tip = null;
      apis_comment.createComment(this.comment, this.type).then(response => {
        this.fieldError = null;
        this.replyComment = null;
        this.loadComments(); // Set local storage

        localStorage.setItem('author', this.comment.author);
        localStorage.setItem('email', this.comment.email);
        localStorage.setItem('authorUrl', this.comment.authorUrl);

        if (response && response.data && response.data.data) {
          var createdComment = response.data.data;
          localStorage.setItem('avatar', createdComment.gavatarMd5);

          if (createdComment.status === 'AUDITING') {
            this.tip = '您的评论已经投递至博主，待博主审核后进行展示。';
          } else if (createdComment.status === 'PUBLISHED') {
            this.tip = '您的评论已经创建成功！';
          }
        }
      }).catch(error => {
        this.fieldError = {};
        var response = error.response;

        if (response && response.data) {
          if (this.isObject(response.data.data)) {
            this.fieldError = response.data.data;
          }

          this.fieldError.message = response.data.message;
        } else {
          this.fieldError.message = '服务器响应失败';
        }
      });
    },

    isObject(value) {
      return value && typeof value === 'object' && value.constructor === Object;
    }

  }
});
// CONCATENATED MODULE: ./src/components/Comment.vue?vue&type=script&lang=js&shadow
 /* harmony default export */ var components_Commentvue_type_script_lang_js_shadow = (Commentvue_type_script_lang_js_shadow); 
// CONCATENATED MODULE: ./src/components/Comment.vue?shadow



function Commentshadow_injectStyles (context) {
  
  var style0 = __webpack_require__("a85a")
if (style0.__inject__) style0.__inject__(context)

}

/* normalize component */

var Commentshadow_component = normalizeComponent(
  components_Commentvue_type_script_lang_js_shadow,
  render,
  staticRenderFns,
  false,
  Commentshadow_injectStyles,
  null,
  null
  ,true
)

/* harmony default export */ var Commentshadow = (Commentshadow_component.exports);
// CONCATENATED MODULE: ./node_modules/_@vue_cli-service@3.7.0@@vue/cli-service/lib/commands/build/entry-wc.js




// runtime shared by every component chunk





window.customElements.define('halo-comment', vue_wc_wrapper(external_Vue_default.a, Commentshadow))

/***/ }),

/***/ "75ff":
/***/ (function(module, exports) {

var id = 0;
var px = Math.random();
module.exports = function (key) {
  return 'Symbol('.concat(key === undefined ? '' : key, ')_', (++id + px).toString(36));
};


/***/ }),

/***/ "76fc":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");
var settle = __webpack_require__("27be");
var buildURL = __webpack_require__("929b");
var parseHeaders = __webpack_require__("e555");
var isURLSameOrigin = __webpack_require__("dd61");
var createError = __webpack_require__("3494");
var btoa = (typeof window !== 'undefined' && window.btoa && window.btoa.bind(window)) || __webpack_require__("b032");

module.exports = function xhrAdapter(config) {
  return new Promise(function dispatchXhrRequest(resolve, reject) {
    var requestData = config.data;
    var requestHeaders = config.headers;

    if (utils.isFormData(requestData)) {
      delete requestHeaders['Content-Type']; // Let the browser set it
    }

    var request = new XMLHttpRequest();
    var loadEvent = 'onreadystatechange';
    var xDomain = false;

    // For IE 8/9 CORS support
    // Only supports POST and GET calls and doesn't returns the response headers.
    // DON'T do this for testing b/c XMLHttpRequest is mocked, not XDomainRequest.
    if ( true &&
        typeof window !== 'undefined' &&
        window.XDomainRequest && !('withCredentials' in request) &&
        !isURLSameOrigin(config.url)) {
      request = new window.XDomainRequest();
      loadEvent = 'onload';
      xDomain = true;
      request.onprogress = function handleProgress() {};
      request.ontimeout = function handleTimeout() {};
    }

    // HTTP basic authentication
    if (config.auth) {
      var username = config.auth.username || '';
      var password = config.auth.password || '';
      requestHeaders.Authorization = 'Basic ' + btoa(username + ':' + password);
    }

    request.open(config.method.toUpperCase(), buildURL(config.url, config.params, config.paramsSerializer), true);

    // Set the request timeout in MS
    request.timeout = config.timeout;

    // Listen for ready state
    request[loadEvent] = function handleLoad() {
      if (!request || (request.readyState !== 4 && !xDomain)) {
        return;
      }

      // The request errored out and we didn't get a response, this will be
      // handled by onerror instead
      // With one exception: request that using file: protocol, most browsers
      // will return status as 0 even though it's a successful request
      if (request.status === 0 && !(request.responseURL && request.responseURL.indexOf('file:') === 0)) {
        return;
      }

      // Prepare the response
      var responseHeaders = 'getAllResponseHeaders' in request ? parseHeaders(request.getAllResponseHeaders()) : null;
      var responseData = !config.responseType || config.responseType === 'text' ? request.responseText : request.response;
      var response = {
        data: responseData,
        // IE sends 1223 instead of 204 (https://github.com/axios/axios/issues/201)
        status: request.status === 1223 ? 204 : request.status,
        statusText: request.status === 1223 ? 'No Content' : request.statusText,
        headers: responseHeaders,
        config: config,
        request: request
      };

      settle(resolve, reject, response);

      // Clean up request
      request = null;
    };

    // Handle low level network errors
    request.onerror = function handleError() {
      // Real errors are hidden from us by the browser
      // onerror should only fire if it's a network error
      reject(createError('Network Error', config, null, request));

      // Clean up request
      request = null;
    };

    // Handle timeout
    request.ontimeout = function handleTimeout() {
      reject(createError('timeout of ' + config.timeout + 'ms exceeded', config, 'ECONNABORTED',
        request));

      // Clean up request
      request = null;
    };

    // Add xsrf header
    // This is only done if running in a standard browser environment.
    // Specifically not if we're in a web worker, or react-native.
    if (utils.isStandardBrowserEnv()) {
      var cookies = __webpack_require__("3747");

      // Add xsrf header
      var xsrfValue = (config.withCredentials || isURLSameOrigin(config.url)) && config.xsrfCookieName ?
          cookies.read(config.xsrfCookieName) :
          undefined;

      if (xsrfValue) {
        requestHeaders[config.xsrfHeaderName] = xsrfValue;
      }
    }

    // Add headers to the request
    if ('setRequestHeader' in request) {
      utils.forEach(requestHeaders, function setRequestHeader(val, key) {
        if (typeof requestData === 'undefined' && key.toLowerCase() === 'content-type') {
          // Remove Content-Type if data is undefined
          delete requestHeaders[key];
        } else {
          // Otherwise add header to the request
          request.setRequestHeader(key, val);
        }
      });
    }

    // Add withCredentials to request if needed
    if (config.withCredentials) {
      request.withCredentials = true;
    }

    // Add responseType to request if needed
    if (config.responseType) {
      try {
        request.responseType = config.responseType;
      } catch (e) {
        // Expected DOMException thrown by browsers not compatible XMLHttpRequest Level 2.
        // But, this can be suppressed for 'json' type as it can be parsed by default 'transformResponse' function.
        if (config.responseType !== 'json') {
          throw e;
        }
      }
    }

    // Handle progress if needed
    if (typeof config.onDownloadProgress === 'function') {
      request.addEventListener('progress', config.onDownloadProgress);
    }

    // Not all browsers support upload events
    if (typeof config.onUploadProgress === 'function' && request.upload) {
      request.upload.addEventListener('progress', config.onUploadProgress);
    }

    if (config.cancelToken) {
      // Handle cancellation
      config.cancelToken.promise.then(function onCanceled(cancel) {
        if (!request) {
          return;
        }

        request.abort();
        reject(cancel);
        // Clean up request
        request = null;
      });
    }

    if (requestData === undefined) {
      requestData = null;
    }

    // Send the request
    request.send(requestData);
  });
};


/***/ }),

/***/ "79c0":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

/**
 * Transform the data for a request or a response
 *
 * @param {Object|String} data The data to be transformed
 * @param {Array} headers The headers for the request or response
 * @param {Array|Function} fns A single function or Array of functions
 * @returns {*} The resulting transformed data
 */
module.exports = function transformData(data, headers, fns) {
  /*eslint no-param-reassign:0*/
  utils.forEach(fns, function transform(fn) {
    data = fn(data, headers);
  });

  return data;
};


/***/ }),

/***/ "7b4a":
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__("e552");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add CSS to Shadow Root
var add = __webpack_require__("565c").default
module.exports.__inject__ = function (shadowRoot) {
  add("ec20d4b6", content, shadowRoot)
};

/***/ }),

/***/ "7cbd":
/***/ (function(module, exports, __webpack_require__) {

var has = __webpack_require__("3301");
var toIObject = __webpack_require__("54a3");
var arrayIndexOf = __webpack_require__("0d0d")(false);
var IE_PROTO = __webpack_require__("5ee9")('IE_PROTO');

module.exports = function (object, names) {
  var O = toIObject(object);
  var i = 0;
  var result = [];
  var key;
  for (key in O) if (key != IE_PROTO) has(O, key) && result.push(key);
  // Don't enum bug & hidden keys
  while (names.length > i) if (has(O, key = names[i++])) {
    ~arrayIndexOf(result, key) || result.push(key);
  }
  return result;
};


/***/ }),

/***/ "7f00":
/***/ (function(module, exports, __webpack_require__) {

var global = __webpack_require__("4839");
var hide = __webpack_require__("c84b");
var has = __webpack_require__("3301");
var SRC = __webpack_require__("75ff")('src');
var $toString = __webpack_require__("6f8f");
var TO_STRING = 'toString';
var TPL = ('' + $toString).split(TO_STRING);

__webpack_require__("1dff").inspectSource = function (it) {
  return $toString.call(it);
};

(module.exports = function (O, key, val, safe) {
  var isFunction = typeof val == 'function';
  if (isFunction) has(val, 'name') || hide(val, 'name', key);
  if (O[key] === val) return;
  if (isFunction) has(val, SRC) || hide(val, SRC, O[key] ? '' + O[key] : TPL.join(String(key)));
  if (O === global) {
    O[key] = val;
  } else if (!safe) {
    delete O[key];
    hide(O, key, val);
  } else if (O[key]) {
    O[key] = val;
  } else {
    hide(O, key, val);
  }
// add fake Function#toString for correct work wrapped methods / constructors with methods like LoDash isNative
})(Function.prototype, TO_STRING, function toString() {
  return typeof this == 'function' && this[SRC] || $toString.call(this);
});


/***/ }),

/***/ "7f43":
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__("0439");

/***/ }),

/***/ "8003":
/***/ (function(module, exports, __webpack_require__) {

module.exports = !__webpack_require__("3a0f") && !__webpack_require__("201d")(function () {
  return Object.defineProperty(__webpack_require__("bce2")('div'), 'a', { get: function () { return 7; } }).a != 7;
});


/***/ }),

/***/ "8bbf":
/***/ (function(module, exports) {

module.exports = Vue;

/***/ }),

/***/ "8c5d":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_CommentTree_vue_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__("f1de");
/* harmony import */ var _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_CommentTree_vue_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_CommentTree_vue_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_CommentTree_vue_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_0__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_CommentTree_vue_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_CommentTree_vue_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ "8da8":
/***/ (function(module, exports, __webpack_require__) {

var toInteger = __webpack_require__("ae63");
var max = Math.max;
var min = Math.min;
module.exports = function (index, length) {
  index = toInteger(index);
  return index < 0 ? max(index + length, 0) : min(index, length);
};


/***/ }),

/***/ "8dcd":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("690e")(false);
// imports


// module
exports.push([module.i, "#nprogress{pointer-events:none}#nprogress .bar{background:#29d;position:fixed;z-index:1031;top:0;left:0;width:100%;height:2px}#nprogress .peg{display:block;position:absolute;right:0;width:100px;height:100%;-webkit-box-shadow:0 0 10px #29d,0 0 5px #29d;box-shadow:0 0 10px #29d,0 0 5px #29d;opacity:1;-webkit-transform:rotate(3deg) translateY(-4px);transform:rotate(3deg) translateY(-4px)}#nprogress .spinner{display:block;position:fixed;z-index:1031;top:15px;right:15px}#nprogress .spinner-icon{width:18px;height:18px;-webkit-box-sizing:border-box;box-sizing:border-box;border:2px solid transparent;border-top-color:#29d;border-left-color:#29d;border-radius:50%;-webkit-animation:nprogress-spinner .4s linear infinite;animation:nprogress-spinner .4s linear infinite}.nprogress-custom-parent{overflow:hidden;position:relative}.nprogress-custom-parent #nprogress .bar,.nprogress-custom-parent #nprogress .spinner{position:absolute}@-webkit-keyframes nprogress-spinner{0%{-webkit-transform:rotate(0deg)}to{-webkit-transform:rotate(1turn)}}@keyframes nprogress-spinner{0%{-webkit-transform:rotate(0deg);transform:rotate(0deg)}to{-webkit-transform:rotate(1turn);transform:rotate(1turn)}}", ""]);

// exports


/***/ }),

/***/ "9102":
/***/ (function(module, exports) {

module.exports = function (it) {
  if (typeof it != 'function') throw TypeError(it + ' is not a function!');
  return it;
};


/***/ }),

/***/ "9208":
/***/ (function(module, exports, __webpack_require__) {

var dP = __webpack_require__("694f");
var anObject = __webpack_require__("4d65");
var getKeys = __webpack_require__("d753");

module.exports = __webpack_require__("3a0f") ? Object.defineProperties : function defineProperties(O, Properties) {
  anObject(O);
  var keys = getKeys(Properties);
  var length = keys.length;
  var i = 0;
  var P;
  while (length > i) dP.f(O, P = keys[i++], Properties[P]);
  return O;
};


/***/ }),

/***/ "929b":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

function encode(val) {
  return encodeURIComponent(val).
    replace(/%40/gi, '@').
    replace(/%3A/gi, ':').
    replace(/%24/g, '$').
    replace(/%2C/gi, ',').
    replace(/%20/g, '+').
    replace(/%5B/gi, '[').
    replace(/%5D/gi, ']');
}

/**
 * Build a URL by appending params to the end
 *
 * @param {string} url The base of the url (e.g., http://www.google.com)
 * @param {object} [params] The params to be appended
 * @returns {string} The formatted url
 */
module.exports = function buildURL(url, params, paramsSerializer) {
  /*eslint no-param-reassign:0*/
  if (!params) {
    return url;
  }

  var serializedParams;
  if (paramsSerializer) {
    serializedParams = paramsSerializer(params);
  } else if (utils.isURLSearchParams(params)) {
    serializedParams = params.toString();
  } else {
    var parts = [];

    utils.forEach(params, function serialize(val, key) {
      if (val === null || typeof val === 'undefined') {
        return;
      }

      if (utils.isArray(val)) {
        key = key + '[]';
      } else {
        val = [val];
      }

      utils.forEach(val, function parseValue(v) {
        if (utils.isDate(v)) {
          v = v.toISOString();
        } else if (utils.isObject(v)) {
          v = JSON.stringify(v);
        }
        parts.push(encode(key) + '=' + encode(v));
      });
    });

    serializedParams = parts.join('&');
  }

  if (serializedParams) {
    url += (url.indexOf('?') === -1 ? '?' : '&') + serializedParams;
  }

  return url;
};


/***/ }),

/***/ "9b45":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var defaults = __webpack_require__("5ce4");
var utils = __webpack_require__("3332");
var InterceptorManager = __webpack_require__("eb90");
var dispatchRequest = __webpack_require__("35c6");

/**
 * Create a new instance of Axios
 *
 * @param {Object} instanceConfig The default config for the instance
 */
function Axios(instanceConfig) {
  this.defaults = instanceConfig;
  this.interceptors = {
    request: new InterceptorManager(),
    response: new InterceptorManager()
  };
}

/**
 * Dispatch a request
 *
 * @param {Object} config The config specific for this request (merged with this.defaults)
 */
Axios.prototype.request = function request(config) {
  /*eslint no-param-reassign:0*/
  // Allow for axios('example/url'[, config]) a la fetch API
  if (typeof config === 'string') {
    config = utils.merge({
      url: arguments[0]
    }, arguments[1]);
  }

  config = utils.merge(defaults, {method: 'get'}, this.defaults, config);
  config.method = config.method.toLowerCase();

  // Hook up interceptors middleware
  var chain = [dispatchRequest, undefined];
  var promise = Promise.resolve(config);

  this.interceptors.request.forEach(function unshiftRequestInterceptors(interceptor) {
    chain.unshift(interceptor.fulfilled, interceptor.rejected);
  });

  this.interceptors.response.forEach(function pushResponseInterceptors(interceptor) {
    chain.push(interceptor.fulfilled, interceptor.rejected);
  });

  while (chain.length) {
    promise = promise.then(chain.shift(), chain.shift());
  }

  return promise;
};

// Provide aliases for supported request methods
utils.forEach(['delete', 'get', 'head', 'options'], function forEachMethodNoData(method) {
  /*eslint func-names:0*/
  Axios.prototype[method] = function(url, config) {
    return this.request(utils.merge(config || {}, {
      method: method,
      url: url
    }));
  };
});

utils.forEach(['post', 'put', 'patch'], function forEachMethodWithData(method) {
  /*eslint func-names:0*/
  Axios.prototype[method] = function(url, data, config) {
    return this.request(utils.merge(config || {}, {
      method: method,
      url: url,
      data: data
    }));
  };
});

module.exports = Axios;


/***/ }),

/***/ "9b6d":
/***/ (function(module, exports) {

var toString = {}.toString;

module.exports = function (it) {
  return toString.call(it).slice(8, -1);
};


/***/ }),

/***/ "9e99":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * Syntactic sugar for invoking a function and expanding an array for arguments.
 *
 * Common use case would be to use `Function.prototype.apply`.
 *
 *  ```js
 *  function f(x, y, z) {}
 *  var args = [1, 2, 3];
 *  f.apply(null, args);
 *  ```
 *
 * With `spread` this example can be re-written.
 *
 *  ```js
 *  spread(function(x, y, z) {})([1, 2, 3]);
 *  ```
 *
 * @param {Function} callback
 * @returns {Function}
 */
module.exports = function spread(callback) {
  return function wrap(arr) {
    return callback.apply(null, arr);
  };
};


/***/ }),

/***/ "9f80":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * A `Cancel` is an object that is thrown when an operation is canceled.
 *
 * @class
 * @param {string=} message The message.
 */
function Cancel(message) {
  this.message = message;
}

Cancel.prototype.toString = function toString() {
  return 'Cancel' + (this.message ? ': ' + this.message : '');
};

Cancel.prototype.__CANCEL__ = true;

module.exports = Cancel;


/***/ }),

/***/ "a5de":
/***/ (function(module, exports, __webpack_require__) {

var classof = __webpack_require__("56f8");
var ITERATOR = __webpack_require__("f3ae")('iterator');
var Iterators = __webpack_require__("f03e");
module.exports = __webpack_require__("1dff").getIteratorMethod = function (it) {
  if (it != undefined) return it[ITERATOR]
    || it['@@iterator']
    || Iterators[classof(it)];
};


/***/ }),

/***/ "a85a":
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Comment_vue_vue_type_style_index_0_lang_scss_shadow__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__("1417");
/* harmony import */ var _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Comment_vue_vue_type_style_index_0_lang_scss_shadow__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Comment_vue_vue_type_style_index_0_lang_scss_shadow__WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Comment_vue_vue_type_style_index_0_lang_scss_shadow__WEBPACK_IMPORTED_MODULE_0__) if(__WEBPACK_IMPORT_KEY__ !== 'default') (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Comment_vue_vue_type_style_index_0_lang_scss_shadow__WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_node_modules_vue_style_loader_4_1_2_vue_style_loader_index_js_ref_8_oneOf_1_0_node_modules_css_loader_1_0_1_css_loader_index_js_ref_8_oneOf_1_1_node_modules_vue_loader_15_7_0_vue_loader_lib_loaders_stylePostLoader_js_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_2_node_modules_postcss_loader_3_0_0_postcss_loader_src_index_js_ref_8_oneOf_1_3_node_modules_sass_loader_7_1_0_sass_loader_lib_loader_js_ref_8_oneOf_1_4_node_modules_cache_loader_2_0_1_cache_loader_dist_cjs_js_ref_0_0_node_modules_vue_loader_15_7_0_vue_loader_lib_index_js_vue_loader_options_Comment_vue_vue_type_style_index_0_lang_scss_shadow__WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ "aa91":
/***/ (function(module, exports, __webpack_require__) {

// 7.1.13 ToObject(argument)
var defined = __webpack_require__("2b11");
module.exports = function (it) {
  return Object(defined(it));
};


/***/ }),

/***/ "ae4d":
/***/ (function(module, exports, __webpack_require__) {

/* WEBPACK VAR INJECTION */(function(global) {/**
 * marked - a markdown parser
 * Copyright (c) 2011-2018, Christopher Jeffrey. (MIT Licensed)
 * https://github.com/markedjs/marked
 */

;(function(root) {
'use strict';

/**
 * Block-Level Grammar
 */

var block = {
  newline: /^\n+/,
  code: /^( {4}[^\n]+\n*)+/,
  fences: noop,
  hr: /^ {0,3}((?:- *){3,}|(?:_ *){3,}|(?:\* *){3,})(?:\n+|$)/,
  heading: /^ *(#{1,6}) *([^\n]+?) *(?:#+ *)?(?:\n+|$)/,
  nptable: noop,
  blockquote: /^( {0,3}> ?(paragraph|[^\n]*)(?:\n|$))+/,
  list: /^( {0,3})(bull) [\s\S]+?(?:hr|def|\n{2,}(?! )(?!\1bull )\n*|\s*$)/,
  html: '^ {0,3}(?:' // optional indentation
    + '<(script|pre|style)[\\s>][\\s\\S]*?(?:</\\1>[^\\n]*\\n+|$)' // (1)
    + '|comment[^\\n]*(\\n+|$)' // (2)
    + '|<\\?[\\s\\S]*?\\?>\\n*' // (3)
    + '|<![A-Z][\\s\\S]*?>\\n*' // (4)
    + '|<!\\[CDATA\\[[\\s\\S]*?\\]\\]>\\n*' // (5)
    + '|</?(tag)(?: +|\\n|/?>)[\\s\\S]*?(?:\\n{2,}|$)' // (6)
    + '|<(?!script|pre|style)([a-z][\\w-]*)(?:attribute)*? */?>(?=[ \\t]*(?:\\n|$))[\\s\\S]*?(?:\\n{2,}|$)' // (7) open tag
    + '|</(?!script|pre|style)[a-z][\\w-]*\\s*>(?=[ \\t]*(?:\\n|$))[\\s\\S]*?(?:\\n{2,}|$)' // (7) closing tag
    + ')',
  def: /^ {0,3}\[(label)\]: *\n? *<?([^\s>]+)>?(?:(?: +\n? *| *\n *)(title))? *(?:\n+|$)/,
  table: noop,
  lheading: /^([^\n]+)\n *(=|-){2,} *(?:\n+|$)/,
  paragraph: /^([^\n]+(?:\n(?!hr|heading|lheading| {0,3}>|<\/?(?:tag)(?: +|\n|\/?>)|<(?:script|pre|style|!--))[^\n]+)*)/,
  text: /^[^\n]+/
};

block._label = /(?!\s*\])(?:\\[\[\]]|[^\[\]])+/;
block._title = /(?:"(?:\\"?|[^"\\])*"|'[^'\n]*(?:\n[^'\n]+)*\n?'|\([^()]*\))/;
block.def = edit(block.def)
  .replace('label', block._label)
  .replace('title', block._title)
  .getRegex();

block.bullet = /(?:[*+-]|\d{1,9}\.)/;
block.item = /^( *)(bull) ?[^\n]*(?:\n(?!\1bull ?)[^\n]*)*/;
block.item = edit(block.item, 'gm')
  .replace(/bull/g, block.bullet)
  .getRegex();

block.list = edit(block.list)
  .replace(/bull/g, block.bullet)
  .replace('hr', '\\n+(?=\\1?(?:(?:- *){3,}|(?:_ *){3,}|(?:\\* *){3,})(?:\\n+|$))')
  .replace('def', '\\n+(?=' + block.def.source + ')')
  .getRegex();

block._tag = 'address|article|aside|base|basefont|blockquote|body|caption'
  + '|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption'
  + '|figure|footer|form|frame|frameset|h[1-6]|head|header|hr|html|iframe'
  + '|legend|li|link|main|menu|menuitem|meta|nav|noframes|ol|optgroup|option'
  + '|p|param|section|source|summary|table|tbody|td|tfoot|th|thead|title|tr'
  + '|track|ul';
block._comment = /<!--(?!-?>)[\s\S]*?-->/;
block.html = edit(block.html, 'i')
  .replace('comment', block._comment)
  .replace('tag', block._tag)
  .replace('attribute', / +[a-zA-Z:_][\w.:-]*(?: *= *"[^"\n]*"| *= *'[^'\n]*'| *= *[^\s"'=<>`]+)?/)
  .getRegex();

block.paragraph = edit(block.paragraph)
  .replace('hr', block.hr)
  .replace('heading', block.heading)
  .replace('lheading', block.lheading)
  .replace('tag', block._tag) // pars can be interrupted by type (6) html blocks
  .getRegex();

block.blockquote = edit(block.blockquote)
  .replace('paragraph', block.paragraph)
  .getRegex();

/**
 * Normal Block Grammar
 */

block.normal = merge({}, block);

/**
 * GFM Block Grammar
 */

block.gfm = merge({}, block.normal, {
  fences: /^ {0,3}(`{3,}|~{3,})([^`\n]*)\n(?:|([\s\S]*?)\n)(?: {0,3}\1[~`]* *(?:\n+|$)|$)/,
  paragraph: /^/,
  heading: /^ *(#{1,6}) +([^\n]+?) *#* *(?:\n+|$)/
});

block.gfm.paragraph = edit(block.paragraph)
  .replace('(?!', '(?!'
    + block.gfm.fences.source.replace('\\1', '\\2') + '|'
    + block.list.source.replace('\\1', '\\3') + '|')
  .getRegex();

/**
 * GFM + Tables Block Grammar
 */

block.tables = merge({}, block.gfm, {
  nptable: /^ *([^|\n ].*\|.*)\n *([-:]+ *\|[-| :]*)(?:\n((?:.*[^>\n ].*(?:\n|$))*)\n*|$)/,
  table: /^ *\|(.+)\n *\|?( *[-:]+[-| :]*)(?:\n((?: *[^>\n ].*(?:\n|$))*)\n*|$)/
});

/**
 * Pedantic grammar
 */

block.pedantic = merge({}, block.normal, {
  html: edit(
    '^ *(?:comment *(?:\\n|\\s*$)'
    + '|<(tag)[\\s\\S]+?</\\1> *(?:\\n{2,}|\\s*$)' // closed tag
    + '|<tag(?:"[^"]*"|\'[^\']*\'|\\s[^\'"/>\\s]*)*?/?> *(?:\\n{2,}|\\s*$))')
    .replace('comment', block._comment)
    .replace(/tag/g, '(?!(?:'
      + 'a|em|strong|small|s|cite|q|dfn|abbr|data|time|code|var|samp|kbd|sub'
      + '|sup|i|b|u|mark|ruby|rt|rp|bdi|bdo|span|br|wbr|ins|del|img)'
      + '\\b)\\w+(?!:|[^\\w\\s@]*@)\\b')
    .getRegex(),
  def: /^ *\[([^\]]+)\]: *<?([^\s>]+)>?(?: +(["(][^\n]+[")]))? *(?:\n+|$)/
});

/**
 * Block Lexer
 */

function Lexer(options) {
  this.tokens = [];
  this.tokens.links = Object.create(null);
  this.options = options || marked.defaults;
  this.rules = block.normal;

  if (this.options.pedantic) {
    this.rules = block.pedantic;
  } else if (this.options.gfm) {
    if (this.options.tables) {
      this.rules = block.tables;
    } else {
      this.rules = block.gfm;
    }
  }
}

/**
 * Expose Block Rules
 */

Lexer.rules = block;

/**
 * Static Lex Method
 */

Lexer.lex = function(src, options) {
  var lexer = new Lexer(options);
  return lexer.lex(src);
};

/**
 * Preprocessing
 */

Lexer.prototype.lex = function(src) {
  src = src
    .replace(/\r\n|\r/g, '\n')
    .replace(/\t/g, '    ')
    .replace(/\u00a0/g, ' ')
    .replace(/\u2424/g, '\n');

  return this.token(src, true);
};

/**
 * Lexing
 */

Lexer.prototype.token = function(src, top) {
  src = src.replace(/^ +$/gm, '');
  var next,
      loose,
      cap,
      bull,
      b,
      item,
      listStart,
      listItems,
      t,
      space,
      i,
      tag,
      l,
      isordered,
      istask,
      ischecked;

  while (src) {
    // newline
    if (cap = this.rules.newline.exec(src)) {
      src = src.substring(cap[0].length);
      if (cap[0].length > 1) {
        this.tokens.push({
          type: 'space'
        });
      }
    }

    // code
    if (cap = this.rules.code.exec(src)) {
      src = src.substring(cap[0].length);
      cap = cap[0].replace(/^ {4}/gm, '');
      this.tokens.push({
        type: 'code',
        text: !this.options.pedantic
          ? rtrim(cap, '\n')
          : cap
      });
      continue;
    }

    // fences (gfm)
    if (cap = this.rules.fences.exec(src)) {
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: 'code',
        lang: cap[2] ? cap[2].trim() : cap[2],
        text: cap[3] || ''
      });
      continue;
    }

    // heading
    if (cap = this.rules.heading.exec(src)) {
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: 'heading',
        depth: cap[1].length,
        text: cap[2]
      });
      continue;
    }

    // table no leading pipe (gfm)
    if (cap = this.rules.nptable.exec(src)) {
      item = {
        type: 'table',
        header: splitCells(cap[1].replace(/^ *| *\| *$/g, '')),
        align: cap[2].replace(/^ *|\| *$/g, '').split(/ *\| */),
        cells: cap[3] ? cap[3].replace(/\n$/, '').split('\n') : []
      };

      if (item.header.length === item.align.length) {
        src = src.substring(cap[0].length);

        for (i = 0; i < item.align.length; i++) {
          if (/^ *-+: *$/.test(item.align[i])) {
            item.align[i] = 'right';
          } else if (/^ *:-+: *$/.test(item.align[i])) {
            item.align[i] = 'center';
          } else if (/^ *:-+ *$/.test(item.align[i])) {
            item.align[i] = 'left';
          } else {
            item.align[i] = null;
          }
        }

        for (i = 0; i < item.cells.length; i++) {
          item.cells[i] = splitCells(item.cells[i], item.header.length);
        }

        this.tokens.push(item);

        continue;
      }
    }

    // hr
    if (cap = this.rules.hr.exec(src)) {
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: 'hr'
      });
      continue;
    }

    // blockquote
    if (cap = this.rules.blockquote.exec(src)) {
      src = src.substring(cap[0].length);

      this.tokens.push({
        type: 'blockquote_start'
      });

      cap = cap[0].replace(/^ *> ?/gm, '');

      // Pass `top` to keep the current
      // "toplevel" state. This is exactly
      // how markdown.pl works.
      this.token(cap, top);

      this.tokens.push({
        type: 'blockquote_end'
      });

      continue;
    }

    // list
    if (cap = this.rules.list.exec(src)) {
      src = src.substring(cap[0].length);
      bull = cap[2];
      isordered = bull.length > 1;

      listStart = {
        type: 'list_start',
        ordered: isordered,
        start: isordered ? +bull : '',
        loose: false
      };

      this.tokens.push(listStart);

      // Get each top-level item.
      cap = cap[0].match(this.rules.item);

      listItems = [];
      next = false;
      l = cap.length;
      i = 0;

      for (; i < l; i++) {
        item = cap[i];

        // Remove the list item's bullet
        // so it is seen as the next token.
        space = item.length;
        item = item.replace(/^ *([*+-]|\d+\.) */, '');

        // Outdent whatever the
        // list item contains. Hacky.
        if (~item.indexOf('\n ')) {
          space -= item.length;
          item = !this.options.pedantic
            ? item.replace(new RegExp('^ {1,' + space + '}', 'gm'), '')
            : item.replace(/^ {1,4}/gm, '');
        }

        // Determine whether the next list item belongs here.
        // Backpedal if it does not belong in this list.
        if (i !== l - 1) {
          b = block.bullet.exec(cap[i + 1])[0];
          if (bull.length > 1 ? b.length === 1
            : (b.length > 1 || (this.options.smartLists && b !== bull))) {
            src = cap.slice(i + 1).join('\n') + src;
            i = l - 1;
          }
        }

        // Determine whether item is loose or not.
        // Use: /(^|\n)(?! )[^\n]+\n\n(?!\s*$)/
        // for discount behavior.
        loose = next || /\n\n(?!\s*$)/.test(item);
        if (i !== l - 1) {
          next = item.charAt(item.length - 1) === '\n';
          if (!loose) loose = next;
        }

        if (loose) {
          listStart.loose = true;
        }

        // Check for task list items
        istask = /^\[[ xX]\] /.test(item);
        ischecked = undefined;
        if (istask) {
          ischecked = item[1] !== ' ';
          item = item.replace(/^\[[ xX]\] +/, '');
        }

        t = {
          type: 'list_item_start',
          task: istask,
          checked: ischecked,
          loose: loose
        };

        listItems.push(t);
        this.tokens.push(t);

        // Recurse.
        this.token(item, false);

        this.tokens.push({
          type: 'list_item_end'
        });
      }

      if (listStart.loose) {
        l = listItems.length;
        i = 0;
        for (; i < l; i++) {
          listItems[i].loose = true;
        }
      }

      this.tokens.push({
        type: 'list_end'
      });

      continue;
    }

    // html
    if (cap = this.rules.html.exec(src)) {
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: this.options.sanitize
          ? 'paragraph'
          : 'html',
        pre: !this.options.sanitizer
          && (cap[1] === 'pre' || cap[1] === 'script' || cap[1] === 'style'),
        text: cap[0]
      });
      continue;
    }

    // def
    if (top && (cap = this.rules.def.exec(src))) {
      src = src.substring(cap[0].length);
      if (cap[3]) cap[3] = cap[3].substring(1, cap[3].length - 1);
      tag = cap[1].toLowerCase().replace(/\s+/g, ' ');
      if (!this.tokens.links[tag]) {
        this.tokens.links[tag] = {
          href: cap[2],
          title: cap[3]
        };
      }
      continue;
    }

    // table (gfm)
    if (cap = this.rules.table.exec(src)) {
      item = {
        type: 'table',
        header: splitCells(cap[1].replace(/^ *| *\| *$/g, '')),
        align: cap[2].replace(/^ *|\| *$/g, '').split(/ *\| */),
        cells: cap[3] ? cap[3].replace(/\n$/, '').split('\n') : []
      };

      if (item.header.length === item.align.length) {
        src = src.substring(cap[0].length);

        for (i = 0; i < item.align.length; i++) {
          if (/^ *-+: *$/.test(item.align[i])) {
            item.align[i] = 'right';
          } else if (/^ *:-+: *$/.test(item.align[i])) {
            item.align[i] = 'center';
          } else if (/^ *:-+ *$/.test(item.align[i])) {
            item.align[i] = 'left';
          } else {
            item.align[i] = null;
          }
        }

        for (i = 0; i < item.cells.length; i++) {
          item.cells[i] = splitCells(
            item.cells[i].replace(/^ *\| *| *\| *$/g, ''),
            item.header.length);
        }

        this.tokens.push(item);

        continue;
      }
    }

    // lheading
    if (cap = this.rules.lheading.exec(src)) {
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: 'heading',
        depth: cap[2] === '=' ? 1 : 2,
        text: cap[1]
      });
      continue;
    }

    // top-level paragraph
    if (top && (cap = this.rules.paragraph.exec(src))) {
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: 'paragraph',
        text: cap[1].charAt(cap[1].length - 1) === '\n'
          ? cap[1].slice(0, -1)
          : cap[1]
      });
      continue;
    }

    // text
    if (cap = this.rules.text.exec(src)) {
      // Top-level should never reach here.
      src = src.substring(cap[0].length);
      this.tokens.push({
        type: 'text',
        text: cap[0]
      });
      continue;
    }

    if (src) {
      throw new Error('Infinite loop on byte: ' + src.charCodeAt(0));
    }
  }

  return this.tokens;
};

/**
 * Inline-Level Grammar
 */

var inline = {
  escape: /^\\([!"#$%&'()*+,\-./:;<=>?@\[\]\\^_`{|}~])/,
  autolink: /^<(scheme:[^\s\x00-\x1f<>]*|email)>/,
  url: noop,
  tag: '^comment'
    + '|^</[a-zA-Z][\\w:-]*\\s*>' // self-closing tag
    + '|^<[a-zA-Z][\\w-]*(?:attribute)*?\\s*/?>' // open tag
    + '|^<\\?[\\s\\S]*?\\?>' // processing instruction, e.g. <?php ?>
    + '|^<![a-zA-Z]+\\s[\\s\\S]*?>' // declaration, e.g. <!DOCTYPE html>
    + '|^<!\\[CDATA\\[[\\s\\S]*?\\]\\]>', // CDATA section
  link: /^!?\[(label)\]\(href(?:\s+(title))?\s*\)/,
  reflink: /^!?\[(label)\]\[(?!\s*\])((?:\\[\[\]]?|[^\[\]\\])+)\]/,
  nolink: /^!?\[(?!\s*\])((?:\[[^\[\]]*\]|\\[\[\]]|[^\[\]])*)\](?:\[\])?/,
  strong: /^__([^\s_])__(?!_)|^\*\*([^\s*])\*\*(?!\*)|^__([^\s][\s\S]*?[^\s])__(?!_)|^\*\*([^\s][\s\S]*?[^\s])\*\*(?!\*)/,
  em: /^_([^\s_])_(?!_)|^\*([^\s*"<\[])\*(?!\*)|^_([^\s][\s\S]*?[^\s_])_(?!_|[^\spunctuation])|^_([^\s_][\s\S]*?[^\s])_(?!_|[^\spunctuation])|^\*([^\s"<\[][\s\S]*?[^\s*])\*(?!\*)|^\*([^\s*"<\[][\s\S]*?[^\s])\*(?!\*)/,
  code: /^(`+)([^`]|[^`][\s\S]*?[^`])\1(?!`)/,
  br: /^( {2,}|\\)\n(?!\s*$)/,
  del: noop,
  text: /^(`+|[^`])(?:[\s\S]*?(?:(?=[\\<!\[`*]|\b_|$)|[^ ](?= {2,}\n))|(?= {2,}\n))/
};

// list of punctuation marks from common mark spec
// without ` and ] to workaround Rule 17 (inline code blocks/links)
inline._punctuation = '!"#$%&\'()*+,\\-./:;<=>?@\\[^_{|}~';
inline.em = edit(inline.em).replace(/punctuation/g, inline._punctuation).getRegex();

inline._escapes = /\\([!"#$%&'()*+,\-./:;<=>?@\[\]\\^_`{|}~])/g;

inline._scheme = /[a-zA-Z][a-zA-Z0-9+.-]{1,31}/;
inline._email = /[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+(@)[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+(?![-_])/;
inline.autolink = edit(inline.autolink)
  .replace('scheme', inline._scheme)
  .replace('email', inline._email)
  .getRegex();

inline._attribute = /\s+[a-zA-Z:_][\w.:-]*(?:\s*=\s*"[^"]*"|\s*=\s*'[^']*'|\s*=\s*[^\s"'=<>`]+)?/;

inline.tag = edit(inline.tag)
  .replace('comment', block._comment)
  .replace('attribute', inline._attribute)
  .getRegex();

inline._label = /(?:\[[^\[\]]*\]|\\[\[\]]?|`[^`]*`|`(?!`)|[^\[\]\\`])*?/;
inline._href = /\s*(<(?:\\[<>]?|[^\s<>\\])*>|[^\s\x00-\x1f]*)/;
inline._title = /"(?:\\"?|[^"\\])*"|'(?:\\'?|[^'\\])*'|\((?:\\\)?|[^)\\])*\)/;

inline.link = edit(inline.link)
  .replace('label', inline._label)
  .replace('href', inline._href)
  .replace('title', inline._title)
  .getRegex();

inline.reflink = edit(inline.reflink)
  .replace('label', inline._label)
  .getRegex();

/**
 * Normal Inline Grammar
 */

inline.normal = merge({}, inline);

/**
 * Pedantic Inline Grammar
 */

inline.pedantic = merge({}, inline.normal, {
  strong: /^__(?=\S)([\s\S]*?\S)__(?!_)|^\*\*(?=\S)([\s\S]*?\S)\*\*(?!\*)/,
  em: /^_(?=\S)([\s\S]*?\S)_(?!_)|^\*(?=\S)([\s\S]*?\S)\*(?!\*)/,
  link: edit(/^!?\[(label)\]\((.*?)\)/)
    .replace('label', inline._label)
    .getRegex(),
  reflink: edit(/^!?\[(label)\]\s*\[([^\]]*)\]/)
    .replace('label', inline._label)
    .getRegex()
});

/**
 * GFM Inline Grammar
 */

inline.gfm = merge({}, inline.normal, {
  escape: edit(inline.escape).replace('])', '~|])').getRegex(),
  _extended_email: /[A-Za-z0-9._+-]+(@)[a-zA-Z0-9-_]+(?:\.[a-zA-Z0-9-_]*[a-zA-Z0-9])+(?![-_])/,
  url: /^((?:ftp|https?):\/\/|www\.)(?:[a-zA-Z0-9\-]+\.?)+[^\s<]*|^email/,
  _backpedal: /(?:[^?!.,:;*_~()&]+|\([^)]*\)|&(?![a-zA-Z0-9]+;$)|[?!.,:;*_~)]+(?!$))+/,
  del: /^~+(?=\S)([\s\S]*?\S)~+/,
  text: /^(`+|[^`])(?:[\s\S]*?(?:(?=[\\<!\[`*~]|\b_|https?:\/\/|ftp:\/\/|www\.|$)|[^ ](?= {2,}\n)|[^a-zA-Z0-9.!#$%&'*+\/=?_`{\|}~-](?=[a-zA-Z0-9.!#$%&'*+\/=?_`{\|}~-]+@))|(?= {2,}\n|[a-zA-Z0-9.!#$%&'*+\/=?_`{\|}~-]+@))/
});

inline.gfm.url = edit(inline.gfm.url, 'i')
  .replace('email', inline.gfm._extended_email)
  .getRegex();
/**
 * GFM + Line Breaks Inline Grammar
 */

inline.breaks = merge({}, inline.gfm, {
  br: edit(inline.br).replace('{2,}', '*').getRegex(),
  text: edit(inline.gfm.text).replace(/\{2,\}/g, '*').getRegex()
});

/**
 * Inline Lexer & Compiler
 */

function InlineLexer(links, options) {
  this.options = options || marked.defaults;
  this.links = links;
  this.rules = inline.normal;
  this.renderer = this.options.renderer || new Renderer();
  this.renderer.options = this.options;

  if (!this.links) {
    throw new Error('Tokens array requires a `links` property.');
  }

  if (this.options.pedantic) {
    this.rules = inline.pedantic;
  } else if (this.options.gfm) {
    if (this.options.breaks) {
      this.rules = inline.breaks;
    } else {
      this.rules = inline.gfm;
    }
  }
}

/**
 * Expose Inline Rules
 */

InlineLexer.rules = inline;

/**
 * Static Lexing/Compiling Method
 */

InlineLexer.output = function(src, links, options) {
  var inline = new InlineLexer(links, options);
  return inline.output(src);
};

/**
 * Lexing/Compiling
 */

InlineLexer.prototype.output = function(src) {
  var out = '',
      link,
      text,
      href,
      title,
      cap,
      prevCapZero;

  while (src) {
    // escape
    if (cap = this.rules.escape.exec(src)) {
      src = src.substring(cap[0].length);
      out += escape(cap[1]);
      continue;
    }

    // tag
    if (cap = this.rules.tag.exec(src)) {
      if (!this.inLink && /^<a /i.test(cap[0])) {
        this.inLink = true;
      } else if (this.inLink && /^<\/a>/i.test(cap[0])) {
        this.inLink = false;
      }
      if (!this.inRawBlock && /^<(pre|code|kbd|script)(\s|>)/i.test(cap[0])) {
        this.inRawBlock = true;
      } else if (this.inRawBlock && /^<\/(pre|code|kbd|script)(\s|>)/i.test(cap[0])) {
        this.inRawBlock = false;
      }

      src = src.substring(cap[0].length);
      out += this.options.sanitize
        ? this.options.sanitizer
          ? this.options.sanitizer(cap[0])
          : escape(cap[0])
        : cap[0];
      continue;
    }

    // link
    if (cap = this.rules.link.exec(src)) {
      var lastParenIndex = findClosingBracket(cap[2], '()');
      if (lastParenIndex > -1) {
        var linkLen = cap[0].length - (cap[2].length - lastParenIndex) - (cap[3] || '').length;
        cap[2] = cap[2].substring(0, lastParenIndex);
        cap[0] = cap[0].substring(0, linkLen).trim();
        cap[3] = '';
      }
      src = src.substring(cap[0].length);
      this.inLink = true;
      href = cap[2];
      if (this.options.pedantic) {
        link = /^([^'"]*[^\s])\s+(['"])(.*)\2/.exec(href);

        if (link) {
          href = link[1];
          title = link[3];
        } else {
          title = '';
        }
      } else {
        title = cap[3] ? cap[3].slice(1, -1) : '';
      }
      href = href.trim().replace(/^<([\s\S]*)>$/, '$1');
      out += this.outputLink(cap, {
        href: InlineLexer.escapes(href),
        title: InlineLexer.escapes(title)
      });
      this.inLink = false;
      continue;
    }

    // reflink, nolink
    if ((cap = this.rules.reflink.exec(src))
        || (cap = this.rules.nolink.exec(src))) {
      src = src.substring(cap[0].length);
      link = (cap[2] || cap[1]).replace(/\s+/g, ' ');
      link = this.links[link.toLowerCase()];
      if (!link || !link.href) {
        out += cap[0].charAt(0);
        src = cap[0].substring(1) + src;
        continue;
      }
      this.inLink = true;
      out += this.outputLink(cap, link);
      this.inLink = false;
      continue;
    }

    // strong
    if (cap = this.rules.strong.exec(src)) {
      src = src.substring(cap[0].length);
      out += this.renderer.strong(this.output(cap[4] || cap[3] || cap[2] || cap[1]));
      continue;
    }

    // em
    if (cap = this.rules.em.exec(src)) {
      src = src.substring(cap[0].length);
      out += this.renderer.em(this.output(cap[6] || cap[5] || cap[4] || cap[3] || cap[2] || cap[1]));
      continue;
    }

    // code
    if (cap = this.rules.code.exec(src)) {
      src = src.substring(cap[0].length);
      out += this.renderer.codespan(escape(cap[2].trim(), true));
      continue;
    }

    // br
    if (cap = this.rules.br.exec(src)) {
      src = src.substring(cap[0].length);
      out += this.renderer.br();
      continue;
    }

    // del (gfm)
    if (cap = this.rules.del.exec(src)) {
      src = src.substring(cap[0].length);
      out += this.renderer.del(this.output(cap[1]));
      continue;
    }

    // autolink
    if (cap = this.rules.autolink.exec(src)) {
      src = src.substring(cap[0].length);
      if (cap[2] === '@') {
        text = escape(this.mangle(cap[1]));
        href = 'mailto:' + text;
      } else {
        text = escape(cap[1]);
        href = text;
      }
      out += this.renderer.link(href, null, text);
      continue;
    }

    // url (gfm)
    if (!this.inLink && (cap = this.rules.url.exec(src))) {
      if (cap[2] === '@') {
        text = escape(cap[0]);
        href = 'mailto:' + text;
      } else {
        // do extended autolink path validation
        do {
          prevCapZero = cap[0];
          cap[0] = this.rules._backpedal.exec(cap[0])[0];
        } while (prevCapZero !== cap[0]);
        text = escape(cap[0]);
        if (cap[1] === 'www.') {
          href = 'http://' + text;
        } else {
          href = text;
        }
      }
      src = src.substring(cap[0].length);
      out += this.renderer.link(href, null, text);
      continue;
    }

    // text
    if (cap = this.rules.text.exec(src)) {
      src = src.substring(cap[0].length);
      if (this.inRawBlock) {
        out += this.renderer.text(cap[0]);
      } else {
        out += this.renderer.text(escape(this.smartypants(cap[0])));
      }
      continue;
    }

    if (src) {
      throw new Error('Infinite loop on byte: ' + src.charCodeAt(0));
    }
  }

  return out;
};

InlineLexer.escapes = function(text) {
  return text ? text.replace(InlineLexer.rules._escapes, '$1') : text;
};

/**
 * Compile Link
 */

InlineLexer.prototype.outputLink = function(cap, link) {
  var href = link.href,
      title = link.title ? escape(link.title) : null;

  return cap[0].charAt(0) !== '!'
    ? this.renderer.link(href, title, this.output(cap[1]))
    : this.renderer.image(href, title, escape(cap[1]));
};

/**
 * Smartypants Transformations
 */

InlineLexer.prototype.smartypants = function(text) {
  if (!this.options.smartypants) return text;
  return text
    // em-dashes
    .replace(/---/g, '\u2014')
    // en-dashes
    .replace(/--/g, '\u2013')
    // opening singles
    .replace(/(^|[-\u2014/(\[{"\s])'/g, '$1\u2018')
    // closing singles & apostrophes
    .replace(/'/g, '\u2019')
    // opening doubles
    .replace(/(^|[-\u2014/(\[{\u2018\s])"/g, '$1\u201c')
    // closing doubles
    .replace(/"/g, '\u201d')
    // ellipses
    .replace(/\.{3}/g, '\u2026');
};

/**
 * Mangle Links
 */

InlineLexer.prototype.mangle = function(text) {
  if (!this.options.mangle) return text;
  var out = '',
      l = text.length,
      i = 0,
      ch;

  for (; i < l; i++) {
    ch = text.charCodeAt(i);
    if (Math.random() > 0.5) {
      ch = 'x' + ch.toString(16);
    }
    out += '&#' + ch + ';';
  }

  return out;
};

/**
 * Renderer
 */

function Renderer(options) {
  this.options = options || marked.defaults;
}

Renderer.prototype.code = function(code, infostring, escaped) {
  var lang = (infostring || '').match(/\S*/)[0];
  if (this.options.highlight) {
    var out = this.options.highlight(code, lang);
    if (out != null && out !== code) {
      escaped = true;
      code = out;
    }
  }

  if (!lang) {
    return '<pre><code>'
      + (escaped ? code : escape(code, true))
      + '</code></pre>';
  }

  return '<pre><code class="'
    + this.options.langPrefix
    + escape(lang, true)
    + '">'
    + (escaped ? code : escape(code, true))
    + '</code></pre>\n';
};

Renderer.prototype.blockquote = function(quote) {
  return '<blockquote>\n' + quote + '</blockquote>\n';
};

Renderer.prototype.html = function(html) {
  return html;
};

Renderer.prototype.heading = function(text, level, raw, slugger) {
  if (this.options.headerIds) {
    return '<h'
      + level
      + ' id="'
      + this.options.headerPrefix
      + slugger.slug(raw)
      + '">'
      + text
      + '</h'
      + level
      + '>\n';
  }
  // ignore IDs
  return '<h' + level + '>' + text + '</h' + level + '>\n';
};

Renderer.prototype.hr = function() {
  return this.options.xhtml ? '<hr/>\n' : '<hr>\n';
};

Renderer.prototype.list = function(body, ordered, start) {
  var type = ordered ? 'ol' : 'ul',
      startatt = (ordered && start !== 1) ? (' start="' + start + '"') : '';
  return '<' + type + startatt + '>\n' + body + '</' + type + '>\n';
};

Renderer.prototype.listitem = function(text) {
  return '<li>' + text + '</li>\n';
};

Renderer.prototype.checkbox = function(checked) {
  return '<input '
    + (checked ? 'checked="" ' : '')
    + 'disabled="" type="checkbox"'
    + (this.options.xhtml ? ' /' : '')
    + '> ';
};

Renderer.prototype.paragraph = function(text) {
  return '<p>' + text + '</p>\n';
};

Renderer.prototype.table = function(header, body) {
  if (body) body = '<tbody>' + body + '</tbody>';

  return '<table>\n'
    + '<thead>\n'
    + header
    + '</thead>\n'
    + body
    + '</table>\n';
};

Renderer.prototype.tablerow = function(content) {
  return '<tr>\n' + content + '</tr>\n';
};

Renderer.prototype.tablecell = function(content, flags) {
  var type = flags.header ? 'th' : 'td';
  var tag = flags.align
    ? '<' + type + ' align="' + flags.align + '">'
    : '<' + type + '>';
  return tag + content + '</' + type + '>\n';
};

// span level renderer
Renderer.prototype.strong = function(text) {
  return '<strong>' + text + '</strong>';
};

Renderer.prototype.em = function(text) {
  return '<em>' + text + '</em>';
};

Renderer.prototype.codespan = function(text) {
  return '<code>' + text + '</code>';
};

Renderer.prototype.br = function() {
  return this.options.xhtml ? '<br/>' : '<br>';
};

Renderer.prototype.del = function(text) {
  return '<del>' + text + '</del>';
};

Renderer.prototype.link = function(href, title, text) {
  href = cleanUrl(this.options.sanitize, this.options.baseUrl, href);
  if (href === null) {
    return text;
  }
  var out = '<a href="' + escape(href) + '"';
  if (title) {
    out += ' title="' + title + '"';
  }
  out += '>' + text + '</a>';
  return out;
};

Renderer.prototype.image = function(href, title, text) {
  href = cleanUrl(this.options.sanitize, this.options.baseUrl, href);
  if (href === null) {
    return text;
  }

  var out = '<img src="' + href + '" alt="' + text + '"';
  if (title) {
    out += ' title="' + title + '"';
  }
  out += this.options.xhtml ? '/>' : '>';
  return out;
};

Renderer.prototype.text = function(text) {
  return text;
};

/**
 * TextRenderer
 * returns only the textual part of the token
 */

function TextRenderer() {}

// no need for block level renderers

TextRenderer.prototype.strong =
TextRenderer.prototype.em =
TextRenderer.prototype.codespan =
TextRenderer.prototype.del =
TextRenderer.prototype.text = function (text) {
  return text;
};

TextRenderer.prototype.link =
TextRenderer.prototype.image = function(href, title, text) {
  return '' + text;
};

TextRenderer.prototype.br = function() {
  return '';
};

/**
 * Parsing & Compiling
 */

function Parser(options) {
  this.tokens = [];
  this.token = null;
  this.options = options || marked.defaults;
  this.options.renderer = this.options.renderer || new Renderer();
  this.renderer = this.options.renderer;
  this.renderer.options = this.options;
  this.slugger = new Slugger();
}

/**
 * Static Parse Method
 */

Parser.parse = function(src, options) {
  var parser = new Parser(options);
  return parser.parse(src);
};

/**
 * Parse Loop
 */

Parser.prototype.parse = function(src) {
  this.inline = new InlineLexer(src.links, this.options);
  // use an InlineLexer with a TextRenderer to extract pure text
  this.inlineText = new InlineLexer(
    src.links,
    merge({}, this.options, {renderer: new TextRenderer()})
  );
  this.tokens = src.reverse();

  var out = '';
  while (this.next()) {
    out += this.tok();
  }

  return out;
};

/**
 * Next Token
 */

Parser.prototype.next = function() {
  return this.token = this.tokens.pop();
};

/**
 * Preview Next Token
 */

Parser.prototype.peek = function() {
  return this.tokens[this.tokens.length - 1] || 0;
};

/**
 * Parse Text Tokens
 */

Parser.prototype.parseText = function() {
  var body = this.token.text;

  while (this.peek().type === 'text') {
    body += '\n' + this.next().text;
  }

  return this.inline.output(body);
};

/**
 * Parse Current Token
 */

Parser.prototype.tok = function() {
  switch (this.token.type) {
    case 'space': {
      return '';
    }
    case 'hr': {
      return this.renderer.hr();
    }
    case 'heading': {
      return this.renderer.heading(
        this.inline.output(this.token.text),
        this.token.depth,
        unescape(this.inlineText.output(this.token.text)),
        this.slugger);
    }
    case 'code': {
      return this.renderer.code(this.token.text,
        this.token.lang,
        this.token.escaped);
    }
    case 'table': {
      var header = '',
          body = '',
          i,
          row,
          cell,
          j;

      // header
      cell = '';
      for (i = 0; i < this.token.header.length; i++) {
        cell += this.renderer.tablecell(
          this.inline.output(this.token.header[i]),
          { header: true, align: this.token.align[i] }
        );
      }
      header += this.renderer.tablerow(cell);

      for (i = 0; i < this.token.cells.length; i++) {
        row = this.token.cells[i];

        cell = '';
        for (j = 0; j < row.length; j++) {
          cell += this.renderer.tablecell(
            this.inline.output(row[j]),
            { header: false, align: this.token.align[j] }
          );
        }

        body += this.renderer.tablerow(cell);
      }
      return this.renderer.table(header, body);
    }
    case 'blockquote_start': {
      body = '';

      while (this.next().type !== 'blockquote_end') {
        body += this.tok();
      }

      return this.renderer.blockquote(body);
    }
    case 'list_start': {
      body = '';
      var ordered = this.token.ordered,
          start = this.token.start;

      while (this.next().type !== 'list_end') {
        body += this.tok();
      }

      return this.renderer.list(body, ordered, start);
    }
    case 'list_item_start': {
      body = '';
      var loose = this.token.loose;
      var checked = this.token.checked;
      var task = this.token.task;

      if (this.token.task) {
        body += this.renderer.checkbox(checked);
      }

      while (this.next().type !== 'list_item_end') {
        body += !loose && this.token.type === 'text'
          ? this.parseText()
          : this.tok();
      }
      return this.renderer.listitem(body, task, checked);
    }
    case 'html': {
      // TODO parse inline content if parameter markdown=1
      return this.renderer.html(this.token.text);
    }
    case 'paragraph': {
      return this.renderer.paragraph(this.inline.output(this.token.text));
    }
    case 'text': {
      return this.renderer.paragraph(this.parseText());
    }
    default: {
      var errMsg = 'Token with "' + this.token.type + '" type was not found.';
      if (this.options.silent) {
        console.log(errMsg);
      } else {
        throw new Error(errMsg);
      }
    }
  }
};

/**
 * Slugger generates header id
 */

function Slugger () {
  this.seen = {};
}

/**
 * Convert string to unique id
 */

Slugger.prototype.slug = function (value) {
  var slug = value
    .toLowerCase()
    .trim()
    .replace(/[\u2000-\u206F\u2E00-\u2E7F\\'!"#$%&()*+,./:;<=>?@[\]^`{|}~]/g, '')
    .replace(/\s/g, '-');

  if (this.seen.hasOwnProperty(slug)) {
    var originalSlug = slug;
    do {
      this.seen[originalSlug]++;
      slug = originalSlug + '-' + this.seen[originalSlug];
    } while (this.seen.hasOwnProperty(slug));
  }
  this.seen[slug] = 0;

  return slug;
};

/**
 * Helpers
 */

function escape(html, encode) {
  if (encode) {
    if (escape.escapeTest.test(html)) {
      return html.replace(escape.escapeReplace, function (ch) { return escape.replacements[ch]; });
    }
  } else {
    if (escape.escapeTestNoEncode.test(html)) {
      return html.replace(escape.escapeReplaceNoEncode, function (ch) { return escape.replacements[ch]; });
    }
  }

  return html;
}

escape.escapeTest = /[&<>"']/;
escape.escapeReplace = /[&<>"']/g;
escape.replacements = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;'
};

escape.escapeTestNoEncode = /[<>"']|&(?!#?\w+;)/;
escape.escapeReplaceNoEncode = /[<>"']|&(?!#?\w+;)/g;

function unescape(html) {
  // explicitly match decimal, hex, and named HTML entities
  return html.replace(/&(#(?:\d+)|(?:#x[0-9A-Fa-f]+)|(?:\w+));?/ig, function(_, n) {
    n = n.toLowerCase();
    if (n === 'colon') return ':';
    if (n.charAt(0) === '#') {
      return n.charAt(1) === 'x'
        ? String.fromCharCode(parseInt(n.substring(2), 16))
        : String.fromCharCode(+n.substring(1));
    }
    return '';
  });
}

function edit(regex, opt) {
  regex = regex.source || regex;
  opt = opt || '';
  return {
    replace: function(name, val) {
      val = val.source || val;
      val = val.replace(/(^|[^\[])\^/g, '$1');
      regex = regex.replace(name, val);
      return this;
    },
    getRegex: function() {
      return new RegExp(regex, opt);
    }
  };
}

function cleanUrl(sanitize, base, href) {
  if (sanitize) {
    try {
      var prot = decodeURIComponent(unescape(href))
        .replace(/[^\w:]/g, '')
        .toLowerCase();
    } catch (e) {
      return null;
    }
    if (prot.indexOf('javascript:') === 0 || prot.indexOf('vbscript:') === 0 || prot.indexOf('data:') === 0) {
      return null;
    }
  }
  if (base && !originIndependentUrl.test(href)) {
    href = resolveUrl(base, href);
  }
  try {
    href = encodeURI(href).replace(/%25/g, '%');
  } catch (e) {
    return null;
  }
  return href;
}

function resolveUrl(base, href) {
  if (!baseUrls[' ' + base]) {
    // we can ignore everything in base after the last slash of its path component,
    // but we might need to add _that_
    // https://tools.ietf.org/html/rfc3986#section-3
    if (/^[^:]+:\/*[^/]*$/.test(base)) {
      baseUrls[' ' + base] = base + '/';
    } else {
      baseUrls[' ' + base] = rtrim(base, '/', true);
    }
  }
  base = baseUrls[' ' + base];

  if (href.slice(0, 2) === '//') {
    return base.replace(/:[\s\S]*/, ':') + href;
  } else if (href.charAt(0) === '/') {
    return base.replace(/(:\/*[^/]*)[\s\S]*/, '$1') + href;
  } else {
    return base + href;
  }
}
var baseUrls = {};
var originIndependentUrl = /^$|^[a-z][a-z0-9+.-]*:|^[?#]/i;

function noop() {}
noop.exec = noop;

function merge(obj) {
  var i = 1,
      target,
      key;

  for (; i < arguments.length; i++) {
    target = arguments[i];
    for (key in target) {
      if (Object.prototype.hasOwnProperty.call(target, key)) {
        obj[key] = target[key];
      }
    }
  }

  return obj;
}

function splitCells(tableRow, count) {
  // ensure that every cell-delimiting pipe has a space
  // before it to distinguish it from an escaped pipe
  var row = tableRow.replace(/\|/g, function (match, offset, str) {
        var escaped = false,
            curr = offset;
        while (--curr >= 0 && str[curr] === '\\') escaped = !escaped;
        if (escaped) {
          // odd number of slashes means | is escaped
          // so we leave it alone
          return '|';
        } else {
          // add space before unescaped |
          return ' |';
        }
      }),
      cells = row.split(/ \|/),
      i = 0;

  if (cells.length > count) {
    cells.splice(count);
  } else {
    while (cells.length < count) cells.push('');
  }

  for (; i < cells.length; i++) {
    // leading or trailing whitespace is ignored per the gfm spec
    cells[i] = cells[i].trim().replace(/\\\|/g, '|');
  }
  return cells;
}

// Remove trailing 'c's. Equivalent to str.replace(/c*$/, '').
// /c*$/ is vulnerable to REDOS.
// invert: Remove suffix of non-c chars instead. Default falsey.
function rtrim(str, c, invert) {
  if (str.length === 0) {
    return '';
  }

  // Length of suffix matching the invert condition.
  var suffLen = 0;

  // Step left until we fail to match the invert condition.
  while (suffLen < str.length) {
    var currChar = str.charAt(str.length - suffLen - 1);
    if (currChar === c && !invert) {
      suffLen++;
    } else if (currChar !== c && invert) {
      suffLen++;
    } else {
      break;
    }
  }

  return str.substr(0, str.length - suffLen);
}

function findClosingBracket(str, b) {
  if (str.indexOf(b[1]) === -1) {
    return -1;
  }
  var level = 0;
  for (var i = 0; i < str.length; i++) {
    if (str[i] === '\\') {
      i++;
    } else if (str[i] === b[0]) {
      level++;
    } else if (str[i] === b[1]) {
      level--;
      if (level < 0) {
        return i;
      }
    }
  }
  return -1;
}

/**
 * Marked
 */

function marked(src, opt, callback) {
  // throw error in case of non string input
  if (typeof src === 'undefined' || src === null) {
    throw new Error('marked(): input parameter is undefined or null');
  }
  if (typeof src !== 'string') {
    throw new Error('marked(): input parameter is of type '
      + Object.prototype.toString.call(src) + ', string expected');
  }

  if (callback || typeof opt === 'function') {
    if (!callback) {
      callback = opt;
      opt = null;
    }

    opt = merge({}, marked.defaults, opt || {});

    var highlight = opt.highlight,
        tokens,
        pending,
        i = 0;

    try {
      tokens = Lexer.lex(src, opt);
    } catch (e) {
      return callback(e);
    }

    pending = tokens.length;

    var done = function(err) {
      if (err) {
        opt.highlight = highlight;
        return callback(err);
      }

      var out;

      try {
        out = Parser.parse(tokens, opt);
      } catch (e) {
        err = e;
      }

      opt.highlight = highlight;

      return err
        ? callback(err)
        : callback(null, out);
    };

    if (!highlight || highlight.length < 3) {
      return done();
    }

    delete opt.highlight;

    if (!pending) return done();

    for (; i < tokens.length; i++) {
      (function(token) {
        if (token.type !== 'code') {
          return --pending || done();
        }
        return highlight(token.text, token.lang, function(err, code) {
          if (err) return done(err);
          if (code == null || code === token.text) {
            return --pending || done();
          }
          token.text = code;
          token.escaped = true;
          --pending || done();
        });
      })(tokens[i]);
    }

    return;
  }
  try {
    if (opt) opt = merge({}, marked.defaults, opt);
    return Parser.parse(Lexer.lex(src, opt), opt);
  } catch (e) {
    e.message += '\nPlease report this to https://github.com/markedjs/marked.';
    if ((opt || marked.defaults).silent) {
      return '<p>An error occurred:</p><pre>'
        + escape(e.message + '', true)
        + '</pre>';
    }
    throw e;
  }
}

/**
 * Options
 */

marked.options =
marked.setOptions = function(opt) {
  merge(marked.defaults, opt);
  return marked;
};

marked.getDefaults = function () {
  return {
    baseUrl: null,
    breaks: false,
    gfm: true,
    headerIds: true,
    headerPrefix: '',
    highlight: null,
    langPrefix: 'language-',
    mangle: true,
    pedantic: false,
    renderer: new Renderer(),
    sanitize: false,
    sanitizer: null,
    silent: false,
    smartLists: false,
    smartypants: false,
    tables: true,
    xhtml: false
  };
};

marked.defaults = marked.getDefaults();

/**
 * Expose
 */

marked.Parser = Parser;
marked.parser = Parser.parse;

marked.Renderer = Renderer;
marked.TextRenderer = TextRenderer;

marked.Lexer = Lexer;
marked.lexer = Lexer.lex;

marked.InlineLexer = InlineLexer;
marked.inlineLexer = InlineLexer.output;

marked.Slugger = Slugger;

marked.parse = marked;

if (true) {
  module.exports = marked;
} else {}
})(this || (typeof window !== 'undefined' ? window : global));

/* WEBPACK VAR INJECTION */}.call(this, __webpack_require__("66fa")))

/***/ }),

/***/ "ae63":
/***/ (function(module, exports) {

// 7.1.4 ToInteger
var ceil = Math.ceil;
var floor = Math.floor;
module.exports = function (it) {
  return isNaN(it = +it) ? 0 : (it > 0 ? floor : ceil)(it);
};


/***/ }),

/***/ "b032":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// btoa polyfill for IE<10 courtesy https://github.com/davidchambers/Base64.js

var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';

function E() {
  this.message = 'String contains an invalid character';
}
E.prototype = new Error;
E.prototype.code = 5;
E.prototype.name = 'InvalidCharacterError';

function btoa(input) {
  var str = String(input);
  var output = '';
  for (
    // initialize result and counter
    var block, charCode, idx = 0, map = chars;
    // if the next str index does not exist:
    //   change the mapping table to "="
    //   check if d has no fractional digits
    str.charAt(idx | 0) || (map = '=', idx % 1);
    // "8 - idx % 1 * 8" generates the sequence 2, 4, 6, 8
    output += map.charAt(63 & block >> 8 - idx % 1 * 8)
  ) {
    charCode = str.charCodeAt(idx += 3 / 4);
    if (charCode > 0xFF) {
      throw new E();
    }
    block = block << 8 | charCode;
  }
  return output;
}

module.exports = btoa;


/***/ }),

/***/ "b429":
/***/ (function(module, exports) {

module.exports = function (it) {
  return typeof it === 'object' ? it !== null : typeof it === 'function';
};


/***/ }),

/***/ "b725":
/***/ (function(module, exports) {

module.exports = function (done, value) {
  return { value: value, done: !!done };
};


/***/ }),

/***/ "b915":
/***/ (function(module, exports) {

module.exports = function (bitmap, value) {
  return {
    enumerable: !(bitmap & 1),
    configurable: !(bitmap & 2),
    writable: !(bitmap & 4),
    value: value
  };
};


/***/ }),

/***/ "bce2":
/***/ (function(module, exports, __webpack_require__) {

var isObject = __webpack_require__("b429");
var document = __webpack_require__("4839").document;
// typeof document.createElement is 'object' in old IE
var is = isObject(document) && isObject(document.createElement);
module.exports = function (it) {
  return is ? document.createElement(it) : {};
};


/***/ }),

/***/ "be04":
/***/ (function(module, exports) {

// fast apply, http://jsperf.lnkit.com/fast-apply/5
module.exports = function (fn, args, that) {
  var un = that === undefined;
  switch (args.length) {
    case 0: return un ? fn()
                      : fn.call(that);
    case 1: return un ? fn(args[0])
                      : fn.call(that, args[0]);
    case 2: return un ? fn(args[0], args[1])
                      : fn.call(that, args[0], args[1]);
    case 3: return un ? fn(args[0], args[1], args[2])
                      : fn.call(that, args[0], args[1], args[2]);
    case 4: return un ? fn(args[0], args[1], args[2], args[3])
                      : fn.call(that, args[0], args[1], args[2], args[3]);
  } return fn.apply(that, args);
};


/***/ }),

/***/ "c67d":
/***/ (function(module, exports, __webpack_require__) {

var def = __webpack_require__("694f").f;
var has = __webpack_require__("3301");
var TAG = __webpack_require__("f3ae")('toStringTag');

module.exports = function (it, tag, stat) {
  if (it && !has(it = stat ? it : it.prototype, TAG)) def(it, TAG, { configurable: true, value: tag });
};


/***/ }),

/***/ "c84b":
/***/ (function(module, exports, __webpack_require__) {

var dP = __webpack_require__("694f");
var createDesc = __webpack_require__("b915");
module.exports = __webpack_require__("3a0f") ? function (object, key, value) {
  return dP.f(object, key, createDesc(1, value));
} : function (object, key, value) {
  object[key] = value;
  return object;
};


/***/ }),

/***/ "c8bb":
/***/ (function(module, exports) {

module.exports = function (exec) {
  try {
    return { e: false, v: exec() };
  } catch (e) {
    return { e: true, v: e };
  }
};


/***/ }),

/***/ "cfb8":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("690e")(false);
// imports


// module
exports.push([module.i, "@charset \"UTF-8\";\n/*! Spectre.css v0.5.8 | MIT License | github.com/picturepan2/spectre */html{font-family:sans-serif;-ms-text-size-adjust:100%;-webkit-text-size-adjust:100%}body{margin:0}article,aside,footer,header,nav,section{display:block}h1{font-size:2em;margin:.67em 0}figcaption,figure,main{display:block}hr{-webkit-box-sizing:content-box;box-sizing:content-box;height:0;overflow:visible}a{background-color:transparent;-webkit-text-decoration-skip:objects}a:active,a:hover{outline-width:0}address{font-style:normal}b,strong{font-weight:inherit;font-weight:bolder}code,kbd,pre,samp{font-family:SF Mono,Segoe UI Mono,Roboto Mono,Menlo,Courier,monospace;font-size:1em}dfn{font-style:italic}small{font-size:80%;font-weight:400}sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline}sub{bottom:-.25em}sup{top:-.5em}audio,video{display:inline-block}audio:not([controls]){display:none;height:0}img{border-style:none}svg:not(:root){overflow:hidden}button,input,optgroup,select,textarea{font-family:inherit;font-size:inherit;line-height:inherit;margin:0}button,input{overflow:visible}button,select{text-transform:none}[type=reset],[type=submit],button,html [type=button]{-webkit-appearance:button}[type=button]::-moz-focus-inner,[type=reset]::-moz-focus-inner,[type=submit]::-moz-focus-inner,button::-moz-focus-inner{border-style:none;padding:0}fieldset{border:0;margin:0;padding:0}legend{-webkit-box-sizing:border-box;box-sizing:border-box;color:inherit;display:table;max-width:100%;padding:0;white-space:normal}progress{display:inline-block;vertical-align:baseline}textarea{overflow:auto}[type=checkbox],[type=radio]{-webkit-box-sizing:border-box;box-sizing:border-box;padding:0}[type=number]::-webkit-inner-spin-button,[type=number]::-webkit-outer-spin-button{height:auto}[type=search]{-webkit-appearance:textfield;outline-offset:-2px}[type=search]::-webkit-search-cancel-button,[type=search]::-webkit-search-decoration{-webkit-appearance:none}::-webkit-file-upload-button{-webkit-appearance:button;font:inherit}details,menu{display:block}summary{display:list-item;outline:none}canvas{display:inline-block}[hidden],template{display:none}*,:after,:before{-webkit-box-sizing:inherit;box-sizing:inherit}html{-webkit-box-sizing:border-box;box-sizing:border-box;font-size:20px;line-height:1.5;-webkit-tap-highlight-color:transparent}body{background:#fff;color:#3b4351;font-family:-apple-system,system-ui,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica Neue,sans-serif;font-size:.8rem;overflow-x:hidden;text-rendering:optimizeLegibility}a{color:#5755d9;outline:none;text-decoration:none}a:focus{-webkit-box-shadow:0 0 0 .1rem rgba(87,85,217,.2);box-shadow:0 0 0 .1rem rgba(87,85,217,.2)}a.active,a:active,a:focus,a:hover{color:#302ecd;text-decoration:underline}a:visited{color:#807fe2}h1,h2,h3,h4,h5,h6{color:inherit;font-weight:500;line-height:1.2;margin-bottom:.5em;margin-top:0}.h1,.h2,.h3,.h4,.h5,.h6{font-weight:500}.h1,h1{font-size:2rem}.h2,h2{font-size:1.6rem}.h3,h3{font-size:1.4rem}.h4,h4{font-size:1.2rem}.h5,h5{font-size:1rem}.h6,h6{font-size:.8rem}p{margin:0 0 1.2rem}a,ins,u{-webkit-text-decoration-skip:ink edges;text-decoration-skip:ink edges}abbr[title]{border-bottom:.05rem dotted;cursor:help;text-decoration:none}kbd{line-height:1.25;padding:.1rem .2rem;background:#303742;color:#fff;font-size:.7rem}kbd,mark{border-radius:.1rem}mark{background:#ffe9b3;color:#3b4351;border-bottom:.05rem solid #ffd367;padding:.05rem .1rem 0}blockquote{border-left:.1rem solid #dadee4;margin-left:0;padding:.4rem .8rem}blockquote p:last-child{margin-bottom:0}ol,ul{padding:0}ol,ol ol,ol ul,ul,ul ol,ul ul{margin:.8rem 0 .8rem .8rem}ol li,ul li{margin-top:.4rem}ul{list-style:disc inside}ul ul{list-style-type:circle}ol{list-style:decimal inside}ol ol{list-style-type:lower-alpha}dl dt{font-weight:700}dl dd{margin:.4rem 0 .8rem 0}.lang-zh,.lang-zh-hans,html:lang(zh),html:lang(zh-Hans){font-family:-apple-system,system-ui,BlinkMacSystemFont,Segoe UI,Roboto,PingFang SC,Hiragino Sans GB,Microsoft YaHei,Helvetica Neue,sans-serif}.lang-zh-hant,html:lang(zh-Hant){font-family:-apple-system,system-ui,BlinkMacSystemFont,Segoe UI,Roboto,PingFang TC,Hiragino Sans CNS,Microsoft JhengHei,Helvetica Neue,sans-serif}.lang-ja,html:lang(ja){font-family:-apple-system,system-ui,BlinkMacSystemFont,Segoe UI,Roboto,Hiragino Sans,Hiragino Kaku Gothic Pro,Yu Gothic,YuGothic,Meiryo,Helvetica Neue,sans-serif}.lang-ko,html:lang(ko){font-family:-apple-system,system-ui,BlinkMacSystemFont,Segoe UI,Roboto,Malgun Gothic,Helvetica Neue,sans-serif}.lang-cjk ins,.lang-cjk u,:lang(ja) ins,:lang(ja) u,:lang(zh) ins,:lang(zh) u{border-bottom:.05rem solid;text-decoration:none}.lang-cjk del+del,.lang-cjk del+s,.lang-cjk ins+ins,.lang-cjk ins+u,.lang-cjk s+del,.lang-cjk s+s,.lang-cjk u+ins,.lang-cjk u+u,:lang(ja) del+del,:lang(ja) del+s,:lang(ja) ins+ins,:lang(ja) ins+u,:lang(ja) s+del,:lang(ja) s+s,:lang(ja) u+ins,:lang(ja) u+u,:lang(zh) del+del,:lang(zh) del+s,:lang(zh) ins+ins,:lang(zh) ins+u,:lang(zh) s+del,:lang(zh) s+s,:lang(zh) u+ins,:lang(zh) u+u{margin-left:.125em}.table{border-collapse:collapse;border-spacing:0;width:100%;text-align:left}.table.table-striped tbody tr:nth-of-type(odd){background:#f7f8f9}.table.table-hover tbody tr:hover,.table.table-striped tbody tr.active,.table tbody tr.active{background:#eef0f3}.table.table-scroll{display:block;overflow-x:auto;padding-bottom:.75rem;white-space:nowrap}.table td,.table th{border-bottom:.05rem solid #dadee4;padding:.6rem .4rem}.table th{border-bottom-width:.1rem}.btn{-webkit-appearance:none;-moz-appearance:none;appearance:none;background:#fff;border:.05rem solid #5755d9;border-radius:.1rem;color:#5755d9;cursor:pointer;display:inline-block;font-size:.8rem;height:1.8rem;line-height:1.2rem;outline:none;padding:.25rem .4rem;text-align:center;text-decoration:none;-webkit-transition:background .2s,border .2s,color .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,color .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s,-webkit-box-shadow .2s;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;white-space:nowrap}.btn:focus{-webkit-box-shadow:0 0 0 .1rem rgba(87,85,217,.2);box-shadow:0 0 0 .1rem rgba(87,85,217,.2)}.btn:focus,.btn:hover{background:#f1f1fc;border-color:#4b48d6;text-decoration:none}.btn.active,.btn:active{background:#4b48d6;border-color:#3634d2;color:#fff;text-decoration:none}.btn.active.loading:after,.btn:active.loading:after{border-bottom-color:#fff;border-left-color:#fff}.btn.disabled,.btn:disabled,.btn[disabled]{cursor:default;opacity:.5;pointer-events:none}.btn.btn-primary{background:#5755d9;border-color:#4b48d6;color:#fff}.btn.btn-primary:focus,.btn.btn-primary:hover{background:#4240d4;border-color:#3634d2;color:#fff}.btn.btn-primary.active,.btn.btn-primary:active{background:#3a38d2;border-color:#302ecd;color:#fff}.btn.btn-primary.loading:after{border-bottom-color:#fff;border-left-color:#fff}.btn.btn-success{background:#32b643;border-color:#2faa3f;color:#fff}.btn.btn-success:focus{-webkit-box-shadow:0 0 0 .1rem rgba(50,182,67,.2);box-shadow:0 0 0 .1rem rgba(50,182,67,.2)}.btn.btn-success:focus,.btn.btn-success:hover{background:#30ae40;border-color:#2da23c;color:#fff}.btn.btn-success.active,.btn.btn-success:active{background:#2a9a39;border-color:#278e34;color:#fff}.btn.btn-success.loading:after{border-bottom-color:#fff;border-left-color:#fff}.btn.btn-error{background:#e85600;border-color:#d95000;color:#fff}.btn.btn-error:focus{-webkit-box-shadow:0 0 0 .1rem rgba(232,86,0,.2);box-shadow:0 0 0 .1rem rgba(232,86,0,.2)}.btn.btn-error:focus,.btn.btn-error:hover{background:#de5200;border-color:#cf4d00;color:#fff}.btn.btn-error.active,.btn.btn-error:active{background:#c44900;border-color:#b54300;color:#fff}.btn.btn-error.loading:after{border-bottom-color:#fff;border-left-color:#fff}.btn.btn-link{background:transparent;border-color:transparent;color:#5755d9}.btn.btn-link.active,.btn.btn-link:active,.btn.btn-link:focus,.btn.btn-link:hover{color:#302ecd}.btn.btn-sm{font-size:.7rem;height:1.4rem;padding:.05rem .3rem}.btn.btn-lg{font-size:.9rem;height:2rem;padding:.35rem .6rem}.btn.btn-block{display:block;width:100%}.btn.btn-action{width:1.8rem;padding-left:0;padding-right:0}.btn.btn-action.btn-sm{width:1.4rem}.btn.btn-action.btn-lg{width:2rem}.btn.btn-clear{background:transparent;border:0;color:currentColor;height:1rem;line-height:.8rem;margin-left:.2rem;margin-right:-2px;opacity:1;padding:.1rem;text-decoration:none;width:1rem}.btn.btn-clear:focus,.btn.btn-clear:hover{background:rgba(247,248,249,.5);opacity:.95}.btn.btn-clear:before{content:\"\\2715\"}.btn-group{display:-webkit-inline-box;display:-ms-inline-flexbox;display:inline-flex;-ms-flex-wrap:wrap;flex-wrap:wrap}.btn-group .btn{-webkit-box-flex:1;-ms-flex:1 0 auto;flex:1 0 auto}.btn-group .btn:first-child:not(:last-child){border-bottom-right-radius:0;border-top-right-radius:0}.btn-group .btn:not(:first-child):not(:last-child){border-radius:0;margin-left:-.05rem}.btn-group .btn:last-child:not(:first-child){border-bottom-left-radius:0;border-top-left-radius:0;margin-left:-.05rem}.btn-group .btn.active,.btn-group .btn:active,.btn-group .btn:focus,.btn-group .btn:hover{z-index:1}.btn-group.btn-group-block{display:-webkit-box;display:-ms-flexbox;display:flex}.btn-group.btn-group-block .btn{-webkit-box-flex:1;-ms-flex:1 0 0px;flex:1 0 0}.form-group:not(:last-child){margin-bottom:.4rem}fieldset,legend{margin-bottom:.8rem}legend{font-size:.9rem;font-weight:500}.form-label{display:block;line-height:1.2rem;padding:.3rem 0}.form-label.label-sm{font-size:.7rem;padding:.1rem 0}.form-label.label-lg{font-size:.9rem;padding:.4rem 0}.form-input{-webkit-appearance:none;-moz-appearance:none;appearance:none;background:#fff;background-image:none;border:.05rem solid #bcc3ce;border-radius:.1rem;color:#3b4351;display:block;font-size:.8rem;height:1.8rem;line-height:1.2rem;max-width:100%;outline:none;padding:.25rem .4rem;position:relative;-webkit-transition:background .2s,border .2s,color .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,color .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s,-webkit-box-shadow .2s;width:100%}.form-input:focus{-webkit-box-shadow:0 0 0 .1rem rgba(87,85,217,.2);box-shadow:0 0 0 .1rem rgba(87,85,217,.2);border-color:#5755d9}.form-input::-webkit-input-placeholder{color:#bcc3ce}.form-input::-moz-placeholder{color:#bcc3ce}.form-input:-ms-input-placeholder{color:#bcc3ce}.form-input::-ms-input-placeholder{color:#bcc3ce}.form-input::placeholder{color:#bcc3ce}.form-input.input-sm{font-size:.7rem;height:1.4rem;padding:.05rem .3rem}.form-input.input-lg{font-size:.9rem;height:2rem;padding:.35rem .6rem}.form-input.input-inline{display:inline-block;vertical-align:middle;width:auto}.form-input[type=file],textarea.form-input,textarea.form-input.input-lg,textarea.form-input.input-sm{height:auto}.form-input-hint{color:#bcc3ce;font-size:.7rem;margin-top:.2rem}.has-success .form-input-hint,.is-success+.form-input-hint{color:#32b643}.has-error .form-input-hint,.is-error+.form-input-hint{color:#e85600}.form-select{-webkit-appearance:none;-moz-appearance:none;appearance:none;border:.05rem solid #bcc3ce;border-radius:.1rem;color:inherit;font-size:.8rem;height:1.8rem;line-height:1.2rem;outline:none;padding:.25rem .4rem;vertical-align:middle;width:100%;background:#fff}.form-select:focus{-webkit-box-shadow:0 0 0 .1rem rgba(87,85,217,.2);box-shadow:0 0 0 .1rem rgba(87,85,217,.2);border-color:#5755d9}.form-select::-ms-expand{display:none}.form-select.select-sm{font-size:.7rem;height:1.4rem;padding:.05rem 1.1rem .05rem .3rem}.form-select.select-lg{font-size:.9rem;height:2rem;padding:.35rem 1.4rem .35rem .6rem}.form-select[multiple],.form-select[size]{height:auto;padding:.25rem .4rem}.form-select[multiple] option,.form-select[size] option{padding:.1rem .2rem}.form-select:not([multiple]):not([size]){background:#fff url(\"data:image/svg+xml;charset=utf8,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20viewBox='0%200%204%205'%3E%3Cpath%20fill='%23667189'%20d='M2%200L0%202h4zm0%205L0%203h4z'/%3E%3C/svg%3E\") no-repeat right .35rem center/.4rem .5rem;padding-right:1.2rem}.has-icon-left,.has-icon-right{position:relative}.has-icon-left .form-icon,.has-icon-right .form-icon{height:.8rem;margin:0 .25rem;position:absolute;top:50%;-webkit-transform:translateY(-50%);transform:translateY(-50%);width:.8rem;z-index:2}.has-icon-left .form-icon{left:.05rem}.has-icon-left .form-input{padding-left:1.3rem}.has-icon-right .form-icon{right:.05rem}.has-icon-right .form-input{padding-right:1.3rem}.form-checkbox,.form-radio,.form-switch{display:block;line-height:1.2rem;margin:.2rem 0;min-height:1.4rem;padding:.1rem .4rem .1rem 1.2rem;position:relative}.form-checkbox input,.form-radio input,.form-switch input{clip:rect(0,0,0,0);height:1px;margin:-1px;overflow:hidden;position:absolute;width:1px}.form-checkbox input:focus+.form-icon,.form-radio input:focus+.form-icon,.form-switch input:focus+.form-icon{-webkit-box-shadow:0 0 0 .1rem rgba(87,85,217,.2);box-shadow:0 0 0 .1rem rgba(87,85,217,.2);border-color:#5755d9}.form-checkbox input:checked+.form-icon,.form-radio input:checked+.form-icon,.form-switch input:checked+.form-icon{background:#5755d9;border-color:#5755d9}.form-checkbox .form-icon,.form-radio .form-icon,.form-switch .form-icon{border:.05rem solid #bcc3ce;cursor:pointer;display:inline-block;position:absolute;-webkit-transition:background .2s,border .2s,color .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,color .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s,-webkit-box-shadow .2s}.form-checkbox.input-sm,.form-radio.input-sm,.form-switch.input-sm{font-size:.7rem;margin:0}.form-checkbox.input-lg,.form-radio.input-lg,.form-switch.input-lg{font-size:.9rem;margin:.3rem 0}.form-checkbox .form-icon,.form-radio .form-icon{background:#fff;height:.8rem;left:0;top:.3rem;width:.8rem}.form-checkbox input:active+.form-icon,.form-radio input:active+.form-icon{background:#eef0f3}.form-checkbox .form-icon{border-radius:.1rem}.form-checkbox input:checked+.form-icon:before{background-clip:padding-box;border:.1rem solid #fff;border-left-width:0;border-top-width:0;content:\"\";height:9px;left:50%;margin-left:-3px;margin-top:-6px;position:absolute;top:50%;-webkit-transform:rotate(45deg);transform:rotate(45deg);width:6px}.form-checkbox input:indeterminate+.form-icon{background:#5755d9;border-color:#5755d9}.form-checkbox input:indeterminate+.form-icon:before{background:#fff;content:\"\";height:2px;left:50%;margin-left:-5px;margin-top:-1px;position:absolute;top:50%;width:10px}.form-radio .form-icon{border-radius:50%}.form-radio input:checked+.form-icon:before{background:#fff;border-radius:50%;content:\"\";height:6px;left:50%;position:absolute;top:50%;-webkit-transform:translate(-50%,-50%);transform:translate(-50%,-50%);width:6px}.form-switch{padding-left:2rem}.form-switch .form-icon{background:#bcc3ce;background-clip:padding-box;border-radius:.45rem;height:.9rem;left:0;top:.25rem;width:1.6rem}.form-switch .form-icon:before{background:#fff;border-radius:50%;content:\"\";display:block;height:.8rem;left:0;position:absolute;top:0;-webkit-transition:background .2s,border .2s,color .2s,left .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,color .2s,left .2s,-webkit-box-shadow .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s,left .2s;transition:background .2s,border .2s,box-shadow .2s,color .2s,left .2s,-webkit-box-shadow .2s;width:.8rem}.form-switch input:checked+.form-icon:before{left:14px}.form-switch input:active+.form-icon:before{background:#f7f8f9}.input-group{display:-webkit-box;display:-ms-flexbox;display:flex}.input-group .input-group-addon{background:#f7f8f9;border:.05rem solid #bcc3ce;border-radius:.1rem;line-height:1.2rem;padding:.25rem .4rem;white-space:nowrap}.input-group .input-group-addon.addon-sm{font-size:.7rem;padding:.05rem .3rem}.input-group .input-group-addon.addon-lg{font-size:.9rem;padding:.35rem .6rem}.input-group .form-input,.input-group .form-select{-webkit-box-flex:1;-ms-flex:1 1 auto;flex:1 1 auto;width:1%}.input-group .input-group-btn{z-index:1}.input-group .form-input:first-child:not(:last-child),.input-group .form-select:first-child:not(:last-child),.input-group .input-group-addon:first-child:not(:last-child),.input-group .input-group-btn:first-child:not(:last-child){border-bottom-right-radius:0;border-top-right-radius:0}.input-group .form-input:not(:first-child):not(:last-child),.input-group .form-select:not(:first-child):not(:last-child),.input-group .input-group-addon:not(:first-child):not(:last-child),.input-group .input-group-btn:not(:first-child):not(:last-child){border-radius:0;margin-left:-.05rem}.input-group .form-input:last-child:not(:first-child),.input-group .form-select:last-child:not(:first-child),.input-group .input-group-addon:last-child:not(:first-child),.input-group .input-group-btn:last-child:not(:first-child){border-bottom-left-radius:0;border-top-left-radius:0;margin-left:-.05rem}.input-group .form-input:focus,.input-group .form-select:focus,.input-group .input-group-addon:focus,.input-group .input-group-btn:focus{z-index:2}.input-group .form-select{width:auto}.input-group.input-inline{display:-webkit-inline-box;display:-ms-inline-flexbox;display:inline-flex}.form-input.is-success,.form-select.is-success,.has-success .form-input,.has-success .form-select{background:#f9fdfa;border-color:#32b643}.form-input.is-success:focus,.form-select.is-success:focus,.has-success .form-input:focus,.has-success .form-select:focus{-webkit-box-shadow:0 0 0 .1rem rgba(50,182,67,.2);box-shadow:0 0 0 .1rem rgba(50,182,67,.2)}.form-input.is-error,.form-select.is-error,.has-error .form-input,.has-error .form-select{background:#fffaf7;border-color:#e85600}.form-input.is-error:focus,.form-select.is-error:focus,.has-error .form-input:focus,.has-error .form-select:focus{-webkit-box-shadow:0 0 0 .1rem rgba(232,86,0,.2);box-shadow:0 0 0 .1rem rgba(232,86,0,.2)}.form-checkbox.is-error .form-icon,.form-radio.is-error .form-icon,.form-switch.is-error .form-icon,.has-error .form-checkbox .form-icon,.has-error .form-radio .form-icon,.has-error .form-switch .form-icon{border-color:#e85600}.form-checkbox.is-error input:checked+.form-icon,.form-radio.is-error input:checked+.form-icon,.form-switch.is-error input:checked+.form-icon,.has-error .form-checkbox input:checked+.form-icon,.has-error .form-radio input:checked+.form-icon,.has-error .form-switch input:checked+.form-icon{background:#e85600;border-color:#e85600}.form-checkbox.is-error input:focus+.form-icon,.form-radio.is-error input:focus+.form-icon,.form-switch.is-error input:focus+.form-icon,.has-error .form-checkbox input:focus+.form-icon,.has-error .form-radio input:focus+.form-icon,.has-error .form-switch input:focus+.form-icon{-webkit-box-shadow:0 0 0 .1rem rgba(232,86,0,.2);box-shadow:0 0 0 .1rem rgba(232,86,0,.2);border-color:#e85600}.form-checkbox.is-error input:indeterminate+.form-icon,.has-error .form-checkbox input:indeterminate+.form-icon{background:#e85600;border-color:#e85600}.form-input:not(:placeholder-shown):invalid{border-color:#e85600}.form-input:not(:placeholder-shown):invalid:focus{-webkit-box-shadow:0 0 0 .1rem rgba(232,86,0,.2);box-shadow:0 0 0 .1rem rgba(232,86,0,.2);background:#fffaf7}.form-input:not(:placeholder-shown):invalid+.form-input-hint{color:#e85600}.form-input.disabled,.form-input:disabled,.form-select.disabled,.form-select:disabled{background-color:#eef0f3;cursor:not-allowed;opacity:.5}.form-input[readonly]{background-color:#f7f8f9}input.disabled+.form-icon,input:disabled+.form-icon{background:#eef0f3;cursor:not-allowed;opacity:.5}.form-switch input.disabled+.form-icon:before,.form-switch input:disabled+.form-icon:before{background:#fff}.form-horizontal{padding:.4rem 0}.form-horizontal .form-group{display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-wrap:wrap;flex-wrap:wrap}.form-inline,.label{display:inline-block}.label{border-radius:.1rem;line-height:1.25;padding:.1rem .2rem;background:#eef0f3;color:#455060}.label.label-rounded{border-radius:5rem;padding-left:.4rem;padding-right:.4rem}.label.label-primary{background:#5755d9;color:#fff}.label.label-secondary{background:#f1f1fc;color:#5755d9}.label.label-success{background:#32b643;color:#fff}.label.label-warning{background:#ffb700;color:#fff}.label.label-error{background:#e85600;color:#fff}code{line-height:1.25;padding:.1rem .2rem;background:#fcf2f2;color:#d73e48;font-size:85%}.code,code{border-radius:.1rem}.code{color:#3b4351;position:relative}.code:before{color:#bcc3ce;content:attr(data-lang);font-size:.7rem;position:absolute;right:.4rem;top:.1rem}.code code{background:#f7f8f9;color:inherit;display:block;line-height:1.5;overflow-x:auto;padding:1rem;width:100%}.img-responsive{display:block;height:auto;max-width:100%}.img-fit-cover{-o-object-fit:cover;object-fit:cover}.img-fit-contain{-o-object-fit:contain;object-fit:contain}.video-responsive{display:block;overflow:hidden;padding:0;position:relative;width:100%}.video-responsive:before{content:\"\";display:block;padding-bottom:56.25%}.video-responsive embed,.video-responsive iframe,.video-responsive object{border:0;bottom:0;height:100%;left:0;position:absolute;right:0;top:0;width:100%}video.video-responsive{height:auto;max-width:100%}video.video-responsive:before{content:none}.video-responsive-4-3:before{padding-bottom:75%}.video-responsive-1-1:before{padding-bottom:100%}.figure{margin:0 0 .4rem 0}.figure .figure-caption{color:#66758c;margin-top:.4rem}.container{margin-left:auto;margin-right:auto;padding-left:.4rem;padding-right:.4rem;width:100%}.container.grid-xl{max-width:1296px}.container.grid-lg{max-width:976px}.container.grid-md{max-width:856px}.container.grid-sm{max-width:616px}.container.grid-xs{max-width:496px}.show-lg,.show-md,.show-sm,.show-xl,.show-xs{display:none!important}.columns{display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-wrap:wrap;flex-wrap:wrap;margin-left:-.4rem;margin-right:-.4rem}.columns.col-gapless{margin-left:0;margin-right:0}.columns.col-gapless>.column{padding-left:0;padding-right:0}.columns.col-oneline{-ms-flex-wrap:nowrap;flex-wrap:nowrap;overflow-x:auto}.column{-webkit-box-flex:1;-ms-flex:1;flex:1;max-width:100%;padding-left:.4rem;padding-right:.4rem}.column.col-1,.column.col-2,.column.col-3,.column.col-4,.column.col-5,.column.col-6,.column.col-7,.column.col-8,.column.col-9,.column.col-10,.column.col-11,.column.col-12,.column.col-auto{-webkit-box-flex:0;-ms-flex:none;flex:none}.col-12{width:100%}.col-11{width:91.66666667%}.col-10{width:83.33333333%}.col-9{width:75%}.col-8{width:66.66666667%}.col-7{width:58.33333333%}.col-6{width:50%}.col-5{width:41.66666667%}.col-4{width:33.33333333%}.col-3{width:25%}.col-2{width:16.66666667%}.col-1{width:8.33333333%}.col-auto{-webkit-box-flex:0;-ms-flex:0 0 auto;flex:0 0 auto;max-width:none;width:auto}.col-mx-auto{margin-right:auto}.col-ml-auto,.col-mx-auto{margin-left:auto}.col-mr-auto{margin-right:auto}@media (max-width:1280px){.col-xl-1,.col-xl-2,.col-xl-3,.col-xl-4,.col-xl-5,.col-xl-6,.col-xl-7,.col-xl-8,.col-xl-9,.col-xl-10,.col-xl-11,.col-xl-12,.col-xl-auto{-webkit-box-flex:0;-ms-flex:none;flex:none}.col-xl-12{width:100%}.col-xl-11{width:91.66666667%}.col-xl-10{width:83.33333333%}.col-xl-9{width:75%}.col-xl-8{width:66.66666667%}.col-xl-7{width:58.33333333%}.col-xl-6{width:50%}.col-xl-5{width:41.66666667%}.col-xl-4{width:33.33333333%}.col-xl-3{width:25%}.col-xl-2{width:16.66666667%}.col-xl-1{width:8.33333333%}.col-xl-auto{width:auto}.hide-xl{display:none!important}.show-xl{display:block!important}}@media (max-width:960px){.col-lg-1,.col-lg-2,.col-lg-3,.col-lg-4,.col-lg-5,.col-lg-6,.col-lg-7,.col-lg-8,.col-lg-9,.col-lg-10,.col-lg-11,.col-lg-12,.col-lg-auto{-webkit-box-flex:0;-ms-flex:none;flex:none}.col-lg-12{width:100%}.col-lg-11{width:91.66666667%}.col-lg-10{width:83.33333333%}.col-lg-9{width:75%}.col-lg-8{width:66.66666667%}.col-lg-7{width:58.33333333%}.col-lg-6{width:50%}.col-lg-5{width:41.66666667%}.col-lg-4{width:33.33333333%}.col-lg-3{width:25%}.col-lg-2{width:16.66666667%}.col-lg-1{width:8.33333333%}.col-lg-auto{width:auto}.hide-lg{display:none!important}.show-lg{display:block!important}}@media (max-width:840px){.col-md-1,.col-md-2,.col-md-3,.col-md-4,.col-md-5,.col-md-6,.col-md-7,.col-md-8,.col-md-9,.col-md-10,.col-md-11,.col-md-12,.col-md-auto{-webkit-box-flex:0;-ms-flex:none;flex:none}.col-md-12{width:100%}.col-md-11{width:91.66666667%}.col-md-10{width:83.33333333%}.col-md-9{width:75%}.col-md-8{width:66.66666667%}.col-md-7{width:58.33333333%}.col-md-6{width:50%}.col-md-5{width:41.66666667%}.col-md-4{width:33.33333333%}.col-md-3{width:25%}.col-md-2{width:16.66666667%}.col-md-1{width:8.33333333%}.col-md-auto{width:auto}.hide-md{display:none!important}.show-md{display:block!important}}@media (max-width:600px){.col-sm-1,.col-sm-2,.col-sm-3,.col-sm-4,.col-sm-5,.col-sm-6,.col-sm-7,.col-sm-8,.col-sm-9,.col-sm-10,.col-sm-11,.col-sm-12,.col-sm-auto{-webkit-box-flex:0;-ms-flex:none;flex:none}.col-sm-12{width:100%}.col-sm-11{width:91.66666667%}.col-sm-10{width:83.33333333%}.col-sm-9{width:75%}.col-sm-8{width:66.66666667%}.col-sm-7{width:58.33333333%}.col-sm-6{width:50%}.col-sm-5{width:41.66666667%}.col-sm-4{width:33.33333333%}.col-sm-3{width:25%}.col-sm-2{width:16.66666667%}.col-sm-1{width:8.33333333%}.col-sm-auto{width:auto}.hide-sm{display:none!important}.show-sm{display:block!important}}@media (max-width:480px){.col-xs-1,.col-xs-2,.col-xs-3,.col-xs-4,.col-xs-5,.col-xs-6,.col-xs-7,.col-xs-8,.col-xs-9,.col-xs-10,.col-xs-11,.col-xs-12,.col-xs-auto{-webkit-box-flex:0;-ms-flex:none;flex:none}.col-xs-12{width:100%}.col-xs-11{width:91.66666667%}.col-xs-10{width:83.33333333%}.col-xs-9{width:75%}.col-xs-8{width:66.66666667%}.col-xs-7{width:58.33333333%}.col-xs-6{width:50%}.col-xs-5{width:41.66666667%}.col-xs-4{width:33.33333333%}.col-xs-3{width:25%}.col-xs-2{width:16.66666667%}.col-xs-1{width:8.33333333%}.col-xs-auto{width:auto}.hide-xs{display:none!important}.show-xs{display:block!important}}.hero{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between;padding-bottom:4rem;padding-top:4rem}.hero.hero-sm{padding-bottom:2rem;padding-top:2rem}.hero.hero-lg{padding-bottom:8rem;padding-top:8rem}.hero .hero-body{padding:.4rem}.navbar{-webkit-box-align:stretch;-ms-flex-align:stretch;align-items:stretch;-ms-flex-wrap:wrap;flex-wrap:wrap;-webkit-box-pack:justify;-ms-flex-pack:justify;justify-content:space-between}.navbar,.navbar .navbar-section{display:-webkit-box;display:-ms-flexbox;display:flex}.navbar .navbar-section{-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-flex:1;-ms-flex:1 0 0px;flex:1 0 0}.navbar .navbar-section:not(:first-child):last-child{-webkit-box-pack:end;-ms-flex-pack:end;justify-content:flex-end}.navbar .navbar-center{-webkit-box-align:center;-ms-flex-align:center;align-items:center;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-flex:0;-ms-flex:0 0 auto;flex:0 0 auto}.navbar .navbar-brand{font-size:.9rem;text-decoration:none}.accordion[open] .accordion-header .icon,.accordion input:checked~.accordion-header .icon{-webkit-transform:rotate(90deg);transform:rotate(90deg)}.accordion[open] .accordion-body,.accordion input:checked~.accordion-body{max-height:50rem}.accordion .accordion-header{display:block;padding:.2rem .4rem}.accordion .accordion-header .icon{-webkit-transition:-webkit-transform .25s;transition:-webkit-transform .25s;transition:transform .25s;transition:transform .25s,-webkit-transform .25s}.accordion .accordion-body{margin-bottom:.4rem;max-height:0;overflow:hidden;-webkit-transition:max-height .25s;transition:max-height .25s}summary.accordion-header::-webkit-details-marker{display:none}.avatar{font-size:.8rem;height:1.6rem;width:1.6rem;background:#5755d9;border-radius:50%;color:hsla(0,0%,100%,.85);display:inline-block;font-weight:300;line-height:1.25;margin:0;position:relative;vertical-align:middle}.avatar.avatar-xs{font-size:.4rem;height:.8rem;width:.8rem}.avatar.avatar-sm{font-size:.6rem;height:1.2rem;width:1.2rem}.avatar.avatar-lg{font-size:1.2rem;height:2.4rem;width:2.4rem}.avatar.avatar-xl{font-size:1.6rem;height:3.2rem;width:3.2rem}.avatar img{border-radius:50%;height:100%;position:relative;width:100%;z-index:1}.avatar .avatar-icon,.avatar .avatar-presence{background:#fff;bottom:14.64%;height:50%;padding:.1rem;position:absolute;right:14.64%;-webkit-transform:translate(50%,50%);transform:translate(50%,50%);width:50%;z-index:2}.avatar .avatar-presence{background:#bcc3ce;-webkit-box-shadow:0 0 0 .1rem #fff;box-shadow:0 0 0 .1rem #fff;border-radius:50%;height:.5em;width:.5em}.avatar .avatar-presence.online{background:#32b643}.avatar .avatar-presence.busy{background:#e85600}.avatar .avatar-presence.away{background:#ffb700}.avatar[data-initial]:before{color:currentColor;content:attr(data-initial);left:50%;position:absolute;top:50%;-webkit-transform:translate(-50%,-50%);transform:translate(-50%,-50%);z-index:1}.badge{position:relative;white-space:nowrap}.badge:not([data-badge]):after,.badge[data-badge]:after{background:#5755d9;background-clip:padding-box;border-radius:.5rem;-webkit-box-shadow:0 0 0 .1rem #fff;box-shadow:0 0 0 .1rem #fff;color:#fff;content:attr(data-badge);display:inline-block;-webkit-transform:translate(-.05rem,-.5rem);transform:translate(-.05rem,-.5rem)}.badge[data-badge]:after{font-size:.7rem;height:.9rem;line-height:1;min-width:.9rem;padding:.1rem .2rem;text-align:center;white-space:nowrap}.badge:not([data-badge]):after,.badge[data-badge=\"\"]:after{height:6px;min-width:6px;padding:0;width:6px}.badge.btn:after{top:0;right:0}.badge.avatar:after,.badge.btn:after{position:absolute;-webkit-transform:translate(50%,-50%);transform:translate(50%,-50%)}.badge.avatar:after{top:14.64%;right:14.64%;z-index:100}.breadcrumb{list-style:none;margin:.2rem 0;padding:.2rem 0}.breadcrumb .breadcrumb-item{color:#66758c;display:inline-block;margin:0;padding:.2rem 0}.breadcrumb .breadcrumb-item:not(:last-child){margin-right:.2rem}.breadcrumb .breadcrumb-item:not(:last-child) a{color:#66758c}.breadcrumb .breadcrumb-item:not(:first-child):before{color:#66758c;content:\"/\";padding-right:.4rem}.bar{background:#eef0f3;border-radius:.1rem;display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-wrap:nowrap;flex-wrap:nowrap;height:.8rem;width:100%}.bar.bar-sm{height:.2rem}.bar .bar-item{background:#5755d9;color:#fff;display:block;font-size:.7rem;-ms-flex-negative:0;flex-shrink:0;line-height:.8rem;height:100%;position:relative;text-align:center;width:0}.bar .bar-item:first-child{border-bottom-left-radius:.1rem;border-top-left-radius:.1rem}.bar .bar-item:last-child{border-bottom-right-radius:.1rem;border-top-right-radius:.1rem;-ms-flex-negative:1;flex-shrink:1}.bar-slider{height:.1rem;margin:.4rem 0;position:relative}.bar-slider .bar-item{left:0;padding:0;position:absolute}.bar-slider .bar-item:not(:last-child):first-child{background:#eef0f3;z-index:1}.bar-slider .bar-slider-btn{background:#5755d9;border:0;border-radius:50%;height:.6rem;padding:0;position:absolute;right:0;top:50%;-webkit-transform:translate(50%,-50%);transform:translate(50%,-50%);width:.6rem}.bar-slider .bar-slider-btn:active{-webkit-box-shadow:0 0 0 .1rem #5755d9;box-shadow:0 0 0 .1rem #5755d9}.card{background:#fff;border:.05rem solid #dadee4;border-radius:.1rem;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column}.card .card-body,.card .card-footer,.card .card-header{padding:.8rem;padding-bottom:0}.card .card-body:last-child,.card .card-footer:last-child,.card .card-header:last-child{padding-bottom:.8rem}.card .card-body{-webkit-box-flex:1;-ms-flex:1 1 auto;flex:1 1 auto}.card .card-image{padding-top:.8rem}.card .card-image:first-child{padding-top:0}.card .card-image:first-child img{border-top-left-radius:.1rem;border-top-right-radius:.1rem}.card .card-image:last-child img{border-bottom-left-radius:.1rem;border-bottom-right-radius:.1rem}.chip{-webkit-box-align:center;-ms-flex-align:center;align-items:center;background:#eef0f3;border-radius:5rem;display:-webkit-inline-box;display:-ms-inline-flexbox;display:inline-flex;font-size:90%;height:1.2rem;line-height:.8rem;margin:.1rem;max-width:320px;overflow:hidden;padding:.2rem .4rem;text-decoration:none;text-overflow:ellipsis;vertical-align:middle;white-space:nowrap}.chip.active{background:#5755d9;color:#fff}.chip .avatar{margin-left:-.4rem;margin-right:.2rem}.chip .btn-clear{border-radius:50%;-webkit-transform:scale(.75);transform:scale(.75)}.dropdown{display:inline-block;position:relative}.dropdown .menu{-webkit-animation:slide-down .15s ease 1;animation:slide-down .15s ease 1;display:none;left:0;max-height:50vh;overflow-y:auto;position:absolute;top:100%}.dropdown.dropdown-right .menu{left:auto;right:0}.dropdown.active .menu,.dropdown .dropdown-toggle:focus+.menu,.dropdown .menu:hover{display:block}.dropdown .btn-group .dropdown-toggle:nth-last-child(2){border-bottom-right-radius:.1rem;border-top-right-radius:.1rem}.empty{background:#f7f8f9;border-radius:.1rem;color:#66758c;text-align:center;padding:3.2rem 1.6rem}.empty .empty-icon{margin-bottom:.8rem}.empty .empty-subtitle,.empty .empty-title{margin:.4rem auto}.empty .empty-action{margin-top:.8rem}.menu{-webkit-box-shadow:0 .05rem .2rem rgba(48,55,66,.3);box-shadow:0 .05rem .2rem rgba(48,55,66,.3);background:#fff;border-radius:.1rem;list-style:none;margin:0;min-width:180px;padding:.4rem;-webkit-transform:translateY(.2rem);transform:translateY(.2rem);z-index:300}.menu.menu-nav{background:transparent;-webkit-box-shadow:none;box-shadow:none}.menu .menu-item{margin-top:0;padding:0 .4rem;position:relative;text-decoration:none}.menu .menu-item>a{border-radius:.1rem;color:inherit;display:block;margin:0 -.4rem;padding:.2rem .4rem;text-decoration:none}.menu .menu-item>a.active,.menu .menu-item>a:active,.menu .menu-item>a:focus,.menu .menu-item>a:hover{background:#f1f1fc;color:#5755d9}.menu .menu-item .form-checkbox,.menu .menu-item .form-radio,.menu .menu-item .form-switch{margin:.1rem 0}.menu .menu-item+.menu-item{margin-top:.2rem}.menu .menu-badge{-webkit-box-align:center;-ms-flex-align:center;align-items:center;display:-webkit-box;display:-ms-flexbox;display:flex;height:100%;position:absolute;right:0;top:0}.menu .menu-badge .label{margin-right:.4rem}.modal{-webkit-box-align:center;-ms-flex-align:center;align-items:center;bottom:0;display:none;-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center;left:0;opacity:0;overflow:hidden;padding:.4rem;position:fixed;right:0;top:0}.modal.active,.modal:target{display:-webkit-box;display:-ms-flexbox;display:flex;opacity:1;z-index:400}.modal.active .modal-overlay,.modal:target .modal-overlay{background:rgba(247,248,249,.75);bottom:0;cursor:default;display:block;left:0;position:absolute;right:0;top:0}.modal.active .modal-container,.modal:target .modal-container{-webkit-animation:slide-down .2s ease 1;animation:slide-down .2s ease 1;z-index:1}.modal.modal-sm .modal-container{max-width:320px;padding:0 .4rem}.modal.modal-lg .modal-overlay{background:#fff}.modal.modal-lg .modal-container{-webkit-box-shadow:none;box-shadow:none;max-width:960px}.modal-container{-webkit-box-shadow:0 .2rem .5rem rgba(48,55,66,.3);box-shadow:0 .2rem .5rem rgba(48,55,66,.3);background:#fff;border-radius:.1rem;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column;max-height:75vh;max-width:640px;padding:0 .8rem;width:100%}.modal-container.modal-fullheight{max-height:100vh}.modal-container .modal-header{color:#303742;padding:.8rem}.modal-container .modal-body{overflow-y:auto;padding:.8rem;position:relative}.modal-container .modal-footer{padding:.8rem;text-align:right}.nav{display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column;list-style:none;margin:.2rem 0}.nav .nav-item a{color:#66758c;padding:.2rem .4rem;text-decoration:none}.nav .nav-item a:focus,.nav .nav-item a:hover{color:#5755d9}.nav .nav-item.active>a{color:#505c6e;font-weight:700}.nav .nav-item.active>a:focus,.nav .nav-item.active>a:hover{color:#5755d9}.nav .nav{margin-bottom:.4rem;margin-left:.8rem}.pagination{display:-webkit-box;display:-ms-flexbox;display:flex;list-style:none;margin:.2rem 0;padding:.2rem 0}.pagination .page-item{margin:.2rem .05rem}.pagination .page-item span{display:inline-block;padding:.2rem .2rem}.pagination .page-item a{border-radius:.1rem;display:inline-block;padding:.2rem .4rem;text-decoration:none}.pagination .page-item a:focus,.pagination .page-item a:hover{color:#5755d9}.pagination .page-item.disabled a{cursor:default;opacity:.5;pointer-events:none}.pagination .page-item.active a{background:#5755d9;color:#fff}.pagination .page-item.page-next,.pagination .page-item.page-prev{-webkit-box-flex:1;-ms-flex:1 0 50%;flex:1 0 50%}.pagination .page-item.page-next{text-align:right}.pagination .page-item .page-item-title{margin:0}.pagination .page-item .page-item-subtitle{margin:0;opacity:.5}.panel{border:.05rem solid #dadee4;border-radius:.1rem;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-orient:vertical;-webkit-box-direction:normal;-ms-flex-direction:column;flex-direction:column}.panel .panel-footer,.panel .panel-header{padding:.8rem}.panel .panel-footer,.panel .panel-header,.panel .panel-nav{-webkit-box-flex:0;-ms-flex:0 0 auto;flex:0 0 auto}.panel .panel-body{-webkit-box-flex:1;-ms-flex:1 1 auto;flex:1 1 auto;overflow-y:auto;padding:0 .8rem}.popover{display:inline-block;position:relative}.popover .popover-container{left:50%;opacity:0;padding:.4rem;position:absolute;top:0;-webkit-transform:translate(-50%,-50%) scale(0);transform:translate(-50%,-50%) scale(0);-webkit-transition:-webkit-transform .2s;transition:-webkit-transform .2s;transition:transform .2s;transition:transform .2s,-webkit-transform .2s;width:320px;z-index:300}.popover :focus+.popover-container,.popover:hover .popover-container{display:block;opacity:1;-webkit-transform:translate(-50%,-100%) scale(1);transform:translate(-50%,-100%) scale(1)}.popover.popover-right .popover-container{left:100%;top:50%}.popover.popover-right :focus+.popover-container,.popover.popover-right:hover .popover-container{-webkit-transform:translateY(-50%) scale(1);transform:translateY(-50%) scale(1)}.popover.popover-bottom .popover-container{left:50%;top:100%}.popover.popover-bottom :focus+.popover-container,.popover.popover-bottom:hover .popover-container{-webkit-transform:translate(-50%) scale(1);transform:translate(-50%) scale(1)}.popover.popover-left .popover-container{left:0;top:50%}.popover.popover-left :focus+.popover-container,.popover.popover-left:hover .popover-container{-webkit-transform:translate(-100%,-50%) scale(1);transform:translate(-100%,-50%) scale(1)}.popover .card{-webkit-box-shadow:0 .2rem .5rem rgba(48,55,66,.3);box-shadow:0 .2rem .5rem rgba(48,55,66,.3);border:0}.step{display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-wrap:nowrap;flex-wrap:nowrap;list-style:none;margin:.2rem 0;width:100%}.step .step-item{-webkit-box-flex:1;-ms-flex:1 1 0px;flex:1 1 0;margin-top:0;min-height:1rem;text-align:center;position:relative}.step .step-item:not(:first-child):before{background:#5755d9;content:\"\";height:2px;left:-50%;position:absolute;top:9px;width:100%}.step .step-item a{color:#5755d9;display:inline-block;padding:20px 10px 0;text-decoration:none}.step .step-item a:before{background:#5755d9;border:.1rem solid #fff;border-radius:50%;content:\"\";display:block;height:.6rem;left:50%;position:absolute;top:.2rem;-webkit-transform:translateX(-50%);transform:translateX(-50%);width:.6rem;z-index:1}.step .step-item.active a:before{background:#fff;border:.1rem solid #5755d9}.step .step-item.active~.step-item:before{background:#dadee4}.step .step-item.active~.step-item a{color:#bcc3ce}.step .step-item.active~.step-item a:before{background:#dadee4}.tab{-webkit-box-align:center;-ms-flex-align:center;align-items:center;border-bottom:.05rem solid #dadee4;display:-webkit-box;display:-ms-flexbox;display:flex;-ms-flex-wrap:wrap;flex-wrap:wrap;list-style:none;margin:.2rem 0 .15rem 0}.tab .tab-item{margin-top:0}.tab .tab-item a{border-bottom:.1rem solid transparent;color:inherit;display:block;margin:0 .4rem 0 0;padding:.4rem .2rem .3rem .2rem;text-decoration:none}.tab .tab-item a:focus,.tab .tab-item a:hover{color:#5755d9}.tab .tab-item.active a,.tab .tab-item a.active{border-bottom-color:#5755d9;color:#5755d9}.tab .tab-item.tab-action{-webkit-box-flex:1;-ms-flex:1 0 auto;flex:1 0 auto;text-align:right}.tab .tab-item .btn-clear{margin-top:-.2rem}.tab.tab-block .tab-item{-webkit-box-flex:1;-ms-flex:1 0 0px;flex:1 0 0;text-align:center}.tab.tab-block .tab-item a{margin:0}.tab.tab-block .tab-item .badge[data-badge]:after{position:absolute;right:.1rem;top:.1rem;-webkit-transform:translate(0);transform:translate(0)}.tab:not(.tab-block) .badge{padding-right:0}.tile{-ms-flex-line-pack:justify;align-content:space-between;-webkit-box-align:start;-ms-flex-align:start;align-items:flex-start;display:-webkit-box;display:-ms-flexbox;display:flex}.tile .tile-action,.tile .tile-icon{-webkit-box-flex:0;-ms-flex:0 0 auto;flex:0 0 auto}.tile .tile-content{-webkit-box-flex:1;-ms-flex:1 1 auto;flex:1 1 auto}.tile .tile-content:not(:first-child){padding-left:.4rem}.tile .tile-content:not(:last-child){padding-right:.4rem}.tile .tile-subtitle,.tile .tile-title{line-height:1.2rem}.tile.tile-centered{-webkit-box-align:center;-ms-flex-align:center;align-items:center}.tile.tile-centered .tile-content{overflow:hidden}.tile.tile-centered .tile-subtitle,.tile.tile-centered .tile-title{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:0}.toast{background:rgba(48,55,66,.95);border-color:#303742;border:.05rem solid #303742;border-radius:.1rem;color:#fff;display:block;padding:.4rem;width:100%}.toast.toast-primary{background:rgba(87,85,217,.95);border-color:#5755d9}.toast.toast-success{background:rgba(50,182,67,.95);border-color:#32b643}.toast.toast-warning{background:rgba(255,183,0,.95);border-color:#ffb700}.toast.toast-error{background:rgba(232,86,0,.95);border-color:#e85600}.toast a{color:#fff;text-decoration:underline}.toast a.active,.toast a:active,.toast a:focus,.toast a:hover{opacity:.75}.toast .btn-clear{margin:.1rem}.toast p:last-child{margin-bottom:0}.tooltip{position:relative}.tooltip:after{background:rgba(48,55,66,.95);border-radius:.1rem;bottom:100%;color:#fff;content:attr(data-tooltip);display:block;font-size:.7rem;left:50%;max-width:320px;opacity:0;overflow:hidden;padding:.2rem .4rem;pointer-events:none;position:absolute;text-overflow:ellipsis;-webkit-transform:translate(-50%,.4rem);transform:translate(-50%,.4rem);-webkit-transition:opacity .2s,-webkit-transform .2s;transition:opacity .2s,-webkit-transform .2s;transition:opacity .2s,transform .2s;transition:opacity .2s,transform .2s,-webkit-transform .2s;white-space:pre;z-index:300}.tooltip:focus:after,.tooltip:hover:after{opacity:1;-webkit-transform:translate(-50%,-.2rem);transform:translate(-50%,-.2rem)}.tooltip.disabled,.tooltip[disabled]{pointer-events:auto}.tooltip.tooltip-right:after{bottom:50%;left:100%;-webkit-transform:translate(-.2rem,50%);transform:translate(-.2rem,50%)}.tooltip.tooltip-right:focus:after,.tooltip.tooltip-right:hover:after{-webkit-transform:translate(.2rem,50%);transform:translate(.2rem,50%)}.tooltip.tooltip-bottom:after{bottom:auto;top:100%;-webkit-transform:translate(-50%,-.4rem);transform:translate(-50%,-.4rem)}.tooltip.tooltip-bottom:focus:after,.tooltip.tooltip-bottom:hover:after{-webkit-transform:translate(-50%,.2rem);transform:translate(-50%,.2rem)}.tooltip.tooltip-left:after{bottom:50%;left:auto;right:100%;-webkit-transform:translate(.4rem,50%);transform:translate(.4rem,50%)}.tooltip.tooltip-left:focus:after,.tooltip.tooltip-left:hover:after{-webkit-transform:translate(-.2rem,50%);transform:translate(-.2rem,50%)}@-webkit-keyframes loading{0%{-webkit-transform:rotate(0deg);transform:rotate(0deg)}to{-webkit-transform:rotate(1turn);transform:rotate(1turn)}}@keyframes loading{0%{-webkit-transform:rotate(0deg);transform:rotate(0deg)}to{-webkit-transform:rotate(1turn);transform:rotate(1turn)}}@-webkit-keyframes slide-down{0%{opacity:0;-webkit-transform:translateY(-1.6rem);transform:translateY(-1.6rem)}to{opacity:1;-webkit-transform:translateY(0);transform:translateY(0)}}@keyframes slide-down{0%{opacity:0;-webkit-transform:translateY(-1.6rem);transform:translateY(-1.6rem)}to{opacity:1;-webkit-transform:translateY(0);transform:translateY(0)}}.text-primary{color:#5755d9!important}a.text-primary:focus,a.text-primary:hover{color:#4240d4}a.text-primary:visited{color:#6c6ade}.text-secondary{color:#e5e5f9!important}a.text-secondary:focus,a.text-secondary:hover{color:#d1d0f4}a.text-secondary:visited{color:#fafafe}.text-gray{color:#bcc3ce!important}a.text-gray:focus,a.text-gray:hover{color:#adb6c4}a.text-gray:visited{color:#cbd0d9}.text-light{color:#fff!important}a.text-light:focus,a.text-light:hover{color:#f2f2f2}a.text-light:visited{color:#fff}.text-dark{color:#3b4351!important}a.text-dark:focus,a.text-dark:hover{color:#303742}a.text-dark:visited{color:#455060}.text-success{color:#32b643!important}a.text-success:focus,a.text-success:hover{color:#2da23c}a.text-success:visited{color:#39c94b}.text-warning{color:#ffb700!important}a.text-warning:focus,a.text-warning:hover{color:#e6a500}a.text-warning:visited{color:#ffbe1a}.text-error{color:#e85600!important}a.text-error:focus,a.text-error:hover{color:#cf4d00}a.text-error:visited{color:#ff6003}.bg-primary{background:#5755d9!important;color:#fff}.bg-secondary{background:#f1f1fc!important}.bg-dark{background:#303742!important;color:#fff}.bg-gray{background:#f7f8f9!important}.bg-success{background:#32b643!important;color:#fff}.bg-warning{background:#ffb700!important;color:#fff}.bg-error{background:#e85600!important;color:#fff}.c-hand{cursor:pointer}.c-move{cursor:move}.c-zoom-in{cursor:-webkit-zoom-in;cursor:zoom-in}.c-zoom-out{cursor:-webkit-zoom-out;cursor:zoom-out}.c-not-allowed{cursor:not-allowed}.c-auto{cursor:auto}.d-block{display:block}.d-inline{display:inline}.d-inline-block{display:inline-block}.d-flex{display:-webkit-box;display:-ms-flexbox;display:flex}.d-inline-flex{display:-webkit-inline-box;display:-ms-inline-flexbox;display:inline-flex}.d-hide,.d-none{display:none!important}.d-visible{visibility:visible}.d-invisible{visibility:hidden}.text-hide{background:transparent;border:0;color:transparent;font-size:0;line-height:0;text-shadow:none}.text-assistive{border:0;clip:rect(0,0,0,0);height:1px;margin:-1px;overflow:hidden;padding:0;position:absolute;width:1px}.divider,.divider-vert{display:block;position:relative}.divider-vert[data-content]:after,.divider[data-content]:after{background:#fff;color:#bcc3ce;content:attr(data-content);display:inline-block;font-size:.7rem;padding:0 .4rem;-webkit-transform:translateY(-.65rem);transform:translateY(-.65rem)}.divider{border-top:.05rem solid #f1f3f5;height:.05rem;margin:.4rem 0}.divider[data-content]{margin:.8rem 0}.divider-vert{display:block;padding:.8rem}.divider-vert:before{border-left:.05rem solid #dadee4;bottom:.4rem;content:\"\";display:block;left:50%;position:absolute;top:.4rem;-webkit-transform:translateX(-50%);transform:translateX(-50%)}.divider-vert[data-content]:after{left:50%;padding:.2rem 0;position:absolute;top:50%;-webkit-transform:translate(-50%,-50%);transform:translate(-50%,-50%)}.loading{color:transparent!important;min-height:.8rem;pointer-events:none;position:relative}.loading:after{-webkit-animation:loading .5s linear infinite;animation:loading .5s linear infinite;border:.1rem solid #5755d9;border-radius:50%;border-right-color:transparent;border-top-color:transparent;content:\"\";display:block;height:.8rem;left:50%;margin-left:-.4rem;margin-top:-.4rem;position:absolute;top:50%;width:.8rem;z-index:1}.loading.loading-lg{min-height:2rem}.loading.loading-lg:after{height:1.6rem;margin-left:-.8rem;margin-top:-.8rem;width:1.6rem}.clearfix:after{clear:both;content:\"\";display:table}.float-left{float:left!important}.float-right{float:right!important}.p-relative{position:relative!important}.p-absolute{position:absolute!important}.p-fixed{position:fixed!important}.p-sticky{position:-webkit-sticky!important;position:sticky!important}.p-centered{display:block;float:none;margin-left:auto;margin-right:auto}.flex-centered{-webkit-box-align:center;-ms-flex-align:center;align-items:center;display:-webkit-box;display:-ms-flexbox;display:flex;-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center}.m-0{margin:0!important}.mb-0{margin-bottom:0!important}.ml-0{margin-left:0!important}.mr-0{margin-right:0!important}.mt-0{margin-top:0!important}.mx-0{margin-left:0!important;margin-right:0!important}.my-0{margin-bottom:0!important;margin-top:0!important}.m-1{margin:.2rem!important}.mb-1{margin-bottom:.2rem!important}.ml-1{margin-left:.2rem!important}.mr-1{margin-right:.2rem!important}.mt-1{margin-top:.2rem!important}.mx-1{margin-left:.2rem!important;margin-right:.2rem!important}.my-1{margin-bottom:.2rem!important;margin-top:.2rem!important}.m-2{margin:.4rem!important}.mb-2{margin-bottom:.4rem!important}.ml-2{margin-left:.4rem!important}.mr-2{margin-right:.4rem!important}.mt-2{margin-top:.4rem!important}.mx-2{margin-left:.4rem!important;margin-right:.4rem!important}.my-2{margin-bottom:.4rem!important;margin-top:.4rem!important}.p-0{padding:0!important}.pb-0{padding-bottom:0!important}.pl-0{padding-left:0!important}.pr-0{padding-right:0!important}.pt-0{padding-top:0!important}.px-0{padding-left:0!important;padding-right:0!important}.py-0{padding-bottom:0!important;padding-top:0!important}.p-1{padding:.2rem!important}.pb-1{padding-bottom:.2rem!important}.pl-1{padding-left:.2rem!important}.pr-1{padding-right:.2rem!important}.pt-1{padding-top:.2rem!important}.px-1{padding-left:.2rem!important;padding-right:.2rem!important}.py-1{padding-bottom:.2rem!important;padding-top:.2rem!important}.p-2{padding:.4rem!important}.pb-2{padding-bottom:.4rem!important}.pl-2{padding-left:.4rem!important}.pr-2{padding-right:.4rem!important}.pt-2{padding-top:.4rem!important}.px-2{padding-left:.4rem!important;padding-right:.4rem!important}.py-2{padding-bottom:.4rem!important;padding-top:.4rem!important}.s-rounded{border-radius:.1rem}.s-circle{border-radius:50%}.text-left{text-align:left}.text-right{text-align:right}.text-center{text-align:center}.text-justify{text-align:justify}.text-lowercase{text-transform:lowercase}.text-uppercase{text-transform:uppercase}.text-capitalize{text-transform:capitalize}.text-normal{font-weight:400}.text-bold{font-weight:700}.text-italic{font-style:italic}.text-large{font-size:1.2em}.text-ellipsis{text-overflow:ellipsis}.text-clip,.text-ellipsis{overflow:hidden;white-space:nowrap}.text-clip{text-overflow:clip}.text-break{-webkit-hyphens:auto;-ms-hyphens:auto;hyphens:auto;word-break:break-word;word-wrap:break-word}\n\n/*! Spectre.css Icons v0.5.8 | MIT License | github.com/picturepan2/spectre */.icon{-webkit-box-sizing:border-box;box-sizing:border-box;display:inline-block;font-size:inherit;font-style:normal;height:1em;position:relative;text-indent:-9999px;vertical-align:middle;width:1em}.icon:after,.icon:before{content:\"\";display:block;left:50%;position:absolute;top:50%;-webkit-transform:translate(-50%,-50%);transform:translate(-50%,-50%)}.icon.icon-2x{font-size:1.6rem}.icon.icon-3x{font-size:2.4rem}.icon.icon-4x{font-size:3.2rem}.accordion .icon,.btn .icon,.menu .icon,.toast .icon{vertical-align:-10%}.btn-lg .icon{vertical-align:-15%}.icon-arrow-down:before,.icon-arrow-left:before,.icon-arrow-right:before,.icon-arrow-up:before,.icon-back:before,.icon-downward:before,.icon-forward:before,.icon-upward:before{border:.1rem solid currentColor;border-bottom:0;border-right:0;height:.65em;width:.65em}.icon-arrow-down:before{-webkit-transform:translate(-50%,-75%) rotate(225deg);transform:translate(-50%,-75%) rotate(225deg)}.icon-arrow-left:before{-webkit-transform:translate(-25%,-50%) rotate(-45deg);transform:translate(-25%,-50%) rotate(-45deg)}.icon-arrow-right:before{-webkit-transform:translate(-75%,-50%) rotate(135deg);transform:translate(-75%,-50%) rotate(135deg)}.icon-arrow-up:before{-webkit-transform:translate(-50%,-25%) rotate(45deg);transform:translate(-50%,-25%) rotate(45deg)}.icon-back:after,.icon-forward:after{background:currentColor;height:.1rem;width:.8em}.icon-downward:after,.icon-upward:after{background:currentColor;height:.8em;width:.1rem}.icon-back:after{left:55%}.icon-back:before{-webkit-transform:translate(-50%,-50%) rotate(-45deg);transform:translate(-50%,-50%) rotate(-45deg)}.icon-downward:after{top:45%}.icon-downward:before{-webkit-transform:translate(-50%,-50%) rotate(-135deg);transform:translate(-50%,-50%) rotate(-135deg)}.icon-forward:after{left:45%}.icon-forward:before{-webkit-transform:translate(-50%,-50%) rotate(135deg);transform:translate(-50%,-50%) rotate(135deg)}.icon-upward:after{top:55%}.icon-upward:before{-webkit-transform:translate(-50%,-50%) rotate(45deg);transform:translate(-50%,-50%) rotate(45deg)}.icon-caret:before{border-top:.3em solid currentColor;border-right:.3em solid transparent;border-left:.3em solid transparent;height:0;-webkit-transform:translate(-50%,-25%);transform:translate(-50%,-25%);width:0}.icon-menu:before{background:currentColor;-webkit-box-shadow:0 -.35em,0 .35em;box-shadow:0 -.35em,0 .35em;height:.1rem;width:100%}.icon-apps:before{background:currentColor;-webkit-box-shadow:-.35em -.35em,-.35em 0,-.35em .35em,0 -.35em,0 .35em,.35em -.35em,.35em 0,.35em .35em;box-shadow:-.35em -.35em,-.35em 0,-.35em .35em,0 -.35em,0 .35em,.35em -.35em,.35em 0,.35em .35em;height:3px;width:3px}.icon-resize-horiz:after,.icon-resize-horiz:before,.icon-resize-vert:after,.icon-resize-vert:before{border:.1rem solid currentColor;border-bottom:0;border-right:0;height:.45em;width:.45em}.icon-resize-horiz:before,.icon-resize-vert:before{-webkit-transform:translate(-50%,-90%) rotate(45deg);transform:translate(-50%,-90%) rotate(45deg)}.icon-resize-horiz:after,.icon-resize-vert:after{-webkit-transform:translate(-50%,-10%) rotate(225deg);transform:translate(-50%,-10%) rotate(225deg)}.icon-resize-horiz:before{-webkit-transform:translate(-90%,-50%) rotate(-45deg);transform:translate(-90%,-50%) rotate(-45deg)}.icon-resize-horiz:after{-webkit-transform:translate(-10%,-50%) rotate(135deg);transform:translate(-10%,-50%) rotate(135deg)}.icon-more-horiz:before,.icon-more-vert:before{background:currentColor;-webkit-box-shadow:-.4em 0,.4em 0;box-shadow:-.4em 0,.4em 0;border-radius:50%;height:3px;width:3px}.icon-more-vert:before{-webkit-box-shadow:0 -.4em,0 .4em;box-shadow:0 -.4em,0 .4em}.icon-cross:before,.icon-minus:before,.icon-plus:before{background:currentColor;height:.1rem;width:100%}.icon-cross:after,.icon-plus:after{background:currentColor;height:100%;width:.1rem}.icon-cross:before{width:100%}.icon-cross:after{height:100%}.icon-cross:after,.icon-cross:before{-webkit-transform:translate(-50%,-50%) rotate(45deg);transform:translate(-50%,-50%) rotate(45deg)}.icon-check:before{border:.1rem solid currentColor;border-right:0;border-top:0;height:.5em;width:.9em;-webkit-transform:translate(-50%,-75%) rotate(-45deg);transform:translate(-50%,-75%) rotate(-45deg)}.icon-stop{border:.1rem solid currentColor;border-radius:50%}.icon-stop:before{background:currentColor;height:.1rem;-webkit-transform:translate(-50%,-50%) rotate(45deg);transform:translate(-50%,-50%) rotate(45deg);width:1em}.icon-shutdown{border:.1rem solid currentColor;border-radius:50%;border-top-color:transparent}.icon-shutdown:before{background:currentColor;content:\"\";height:.5em;top:.1em;width:.1rem}.icon-refresh:before{border:.1rem solid currentColor;border-radius:50%;border-right-color:transparent;height:1em;width:1em}.icon-refresh:after{border:.2em solid currentColor;border-top-color:transparent;border-left-color:transparent;height:0;left:80%;top:20%;width:0}.icon-search:before{border:.1rem solid currentColor;border-radius:50%;height:.75em;left:5%;top:5%;-webkit-transform:translate(0) rotate(45deg);transform:translate(0) rotate(45deg);width:.75em}.icon-search:after{background:currentColor;height:.1rem;left:80%;top:80%;-webkit-transform:translate(-50%,-50%) rotate(45deg);transform:translate(-50%,-50%) rotate(45deg);width:.4em}.icon-edit:before{border:.1rem solid currentColor;height:.4em;-webkit-transform:translate(-40%,-60%) rotate(-45deg);transform:translate(-40%,-60%) rotate(-45deg);width:.85em}.icon-edit:after{border:.15em solid currentColor;border-top-color:transparent;border-right-color:transparent;height:0;left:5%;top:95%;-webkit-transform:translateY(-100%);transform:translateY(-100%);width:0}.icon-delete:before{border:.1rem solid currentColor;border-bottom-left-radius:.1rem;border-bottom-right-radius:.1rem;border-top:0;height:.75em;top:60%;width:.75em}.icon-delete:after{background:currentColor;-webkit-box-shadow:-.25em .2em,.25em .2em;box-shadow:-.25em .2em,.25em .2em;height:.1rem;top:.05rem;width:.5em}.icon-share{border:.1rem solid currentColor;border-radius:.1rem;border-right:0;border-top:0}.icon-share:before{border:.1rem solid currentColor;border-left:0;border-top:0;height:.4em;left:100%;top:.25em;-webkit-transform:translate(-125%,-50%) rotate(-45deg);transform:translate(-125%,-50%) rotate(-45deg);width:.4em}.icon-share:after{border:.1rem solid currentColor;border-bottom:0;border-right:0;border-radius:75% 0;height:.5em;width:.6em}.icon-flag:before{background:currentColor;height:1em;left:15%;width:.1rem}.icon-flag:after{border:.1rem solid currentColor;border-bottom-right-radius:.1rem;border-left:0;border-top-right-radius:.1rem;height:.65em;top:35%;left:60%;width:.8em}.icon-bookmark:before{border:.1rem solid currentColor;border-bottom:0;border-top-left-radius:.1rem;border-top-right-radius:.1rem;height:.9em;width:.8em}.icon-bookmark:after{border:.1rem solid currentColor;border-bottom:0;border-left:0;border-radius:.1rem;height:.5em;-webkit-transform:translate(-50%,35%) rotate(-45deg) skew(15deg,15deg);transform:translate(-50%,35%) rotate(-45deg) skew(15deg,15deg);width:.5em}.icon-download,.icon-upload{border-bottom:.1rem solid currentColor}.icon-download:before,.icon-upload:before{border:.1rem solid currentColor;border-bottom:0;border-right:0;height:.5em;width:.5em;-webkit-transform:translate(-50%,-60%) rotate(-135deg);transform:translate(-50%,-60%) rotate(-135deg)}.icon-download:after,.icon-upload:after{background:currentColor;height:.6em;top:40%;width:.1rem}.icon-upload:before{-webkit-transform:translate(-50%,-60%) rotate(45deg);transform:translate(-50%,-60%) rotate(45deg)}.icon-upload:after{top:50%}.icon-copy:before{border:.1rem solid currentColor;border-radius:.1rem;border-right:0;border-bottom:0;height:.8em;left:40%;top:35%;width:.8em}.icon-copy:after{border:.1rem solid currentColor;border-radius:.1rem;height:.8em;left:60%;top:60%;width:.8em}.icon-time{border:.1rem solid currentColor;border-radius:50%}.icon-time:before{height:.4em;-webkit-transform:translate(-50%,-75%);transform:translate(-50%,-75%)}.icon-time:after,.icon-time:before{background:currentColor;width:.1rem}.icon-time:after{height:.3em;-webkit-transform:translate(-50%,-75%) rotate(90deg);transform:translate(-50%,-75%) rotate(90deg);-webkit-transform-origin:50% 90%;transform-origin:50% 90%}.icon-mail:before{border:.1rem solid currentColor;border-radius:.1rem;height:.8em;width:1em}.icon-mail:after{border:.1rem solid currentColor;border-right:0;border-top:0;height:.5em;-webkit-transform:translate(-50%,-90%) rotate(-45deg) skew(10deg,10deg);transform:translate(-50%,-90%) rotate(-45deg) skew(10deg,10deg);width:.5em}.icon-people:before{border:.1rem solid currentColor;border-radius:50%;height:.45em;top:25%;width:.45em}.icon-people:after{border:.1rem solid currentColor;border-radius:50% 50% 0 0;height:.4em;top:75%;width:.9em}.icon-message{border:.1rem solid currentColor;border-bottom:0;border-radius:.1rem;border-right:0}.icon-message:before{border:.1rem solid currentColor;border-bottom-right-radius:.1rem;border-left:0;border-top:0;height:.8em;left:65%;top:40%;width:.7em}.icon-message:after{background:currentColor;border-radius:.1rem;height:.3em;left:10%;top:100%;-webkit-transform:translateY(-90%) rotate(45deg);transform:translateY(-90%) rotate(45deg);width:.1rem}.icon-photo{border:.1rem solid currentColor;border-radius:.1rem}.icon-photo:before{border:.1rem solid currentColor;border-radius:50%;height:.25em;left:35%;top:35%;width:.25em}.icon-photo:after{border:.1rem solid currentColor;border-bottom:0;border-left:0;height:.5em;left:60%;-webkit-transform:translate(-50%,25%) rotate(-45deg);transform:translate(-50%,25%) rotate(-45deg);width:.5em}.icon-link:after,.icon-link:before{border:.1rem solid currentColor;border-radius:5em 0 0 5em;border-right:0;height:.5em;width:.75em}.icon-link:before{-webkit-transform:translate(-70%,-45%) rotate(-45deg);transform:translate(-70%,-45%) rotate(-45deg)}.icon-link:after{-webkit-transform:translate(-30%,-55%) rotate(135deg);transform:translate(-30%,-55%) rotate(135deg)}.icon-location:before{border:.1rem solid currentColor;border-radius:50% 50% 50% 0;height:.8em;-webkit-transform:translate(-50%,-60%) rotate(-45deg);transform:translate(-50%,-60%) rotate(-45deg);width:.8em}.icon-location:after{height:.2em;-webkit-transform:translate(-50%,-80%);transform:translate(-50%,-80%);width:.2em}.icon-emoji,.icon-location:after{border:.1rem solid currentColor;border-radius:50%}.icon-emoji:before{border-radius:50%;-webkit-box-shadow:-.17em -.1em,.17em -.1em;box-shadow:-.17em -.1em,.17em -.1em;height:.15em;width:.15em}.icon-emoji:after{border:.1rem solid currentColor;border-bottom-color:transparent;border-radius:50%;border-right-color:transparent;height:.5em;-webkit-transform:translate(-50%,-40%) rotate(-135deg);transform:translate(-50%,-40%) rotate(-135deg);width:.5em}.form-input{border-radius:0;border:0}.panel .panel-body{padding:10px;-webkit-box-shadow:inset 1px 1px 10px #ccc;box-shadow:inset 1px 1px 10px #ccc;border-radius:.2rem}.toast{margin-bottom:.8rem}.page-item a{cursor:pointer}.divider[data-content]{margin:2rem 0}.panel{border:0}.tab{border-bottom:0}.tab .tab-item.active a,.tab .tab-item a.active{border-bottom-color:#1890ff;color:#1890ff}.tab .tab-item a:focus,.tab .tab-item a:hover{color:#1890ff}.panel .panel-nav{margin-bottom:.2rem}.comment-textarea{border-bottom:0;border:0;resize:none;margin:.1rem}.halo-Button{color:#fff;background-color:#1890ff;border-color:#1890ff;text-shadow:0 -1px 0 rgba(0,0,0,.12);-webkit-box-shadow:0 2px 0 rgba(0,0,0,.045);box-shadow:0 2px 0 rgba(0,0,0,.045);line-height:1.499;display:inline-block;font-weight:400;text-align:center;-ms-touch-action:manipulation;touch-action:manipulation;cursor:pointer;background-image:none;border:1px solid transparent;white-space:nowrap;padding:0 15px;font-size:14px;border-radius:4px;height:32px}.halo-input{border-radius:.2rem}.halo-input:hover{-webkit-transition:all .3s ease-in;transition:all .3s ease-in}.halo-input:focus,.halo-input:hover{-webkit-box-shadow:0 0 20px 0 #ccc;box-shadow:0 0 20px 0 #ccc;z-index:1000;transform:translateX(.5px);-ms-transform:translateX(.5px);-moz-transform:translateX(.5px);-webkit-transform:translateX(.5px);-o-transform:translateX(.5px)}.halo-input:focus{outline:none;border-right-width:1px!important}.avatar{background:none}.comment-wrapper{padding:1rem}.center{text-align:center}.input-wrapper{margin-left:0;margin-right:0;margin-bottom:5px}.comment-textarea-tip{padding:.5rem;padding-bottom:0;border-top:0;margin-bottom:0;margin-top:0}.tab-item a{cursor:pointer}.markdown-content{padding:.5rem}", ""]);

// exports


/***/ }),

/***/ "d28d":
/***/ (function(module, exports, __webpack_require__) {

var anObject = __webpack_require__("4d65");
var isObject = __webpack_require__("b429");
var newPromiseCapability = __webpack_require__("3efa");

module.exports = function (C, x) {
  anObject(C);
  if (isObject(x) && x.constructor === C) return x;
  var promiseCapability = newPromiseCapability.f(C);
  var resolve = promiseCapability.resolve;
  resolve(x);
  return promiseCapability.promise;
};


/***/ }),

/***/ "d475":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var Cancel = __webpack_require__("9f80");

/**
 * A `CancelToken` is an object that can be used to request cancellation of an operation.
 *
 * @class
 * @param {Function} executor The executor function.
 */
function CancelToken(executor) {
  if (typeof executor !== 'function') {
    throw new TypeError('executor must be a function.');
  }

  var resolvePromise;
  this.promise = new Promise(function promiseExecutor(resolve) {
    resolvePromise = resolve;
  });

  var token = this;
  executor(function cancel(message) {
    if (token.reason) {
      // Cancellation has already been requested
      return;
    }

    token.reason = new Cancel(message);
    resolvePromise(token.reason);
  });
}

/**
 * Throws a `Cancel` if cancellation has been requested.
 */
CancelToken.prototype.throwIfRequested = function throwIfRequested() {
  if (this.reason) {
    throw this.reason;
  }
};

/**
 * Returns an object that contains a new `CancelToken` and a function that, when called,
 * cancels the `CancelToken`.
 */
CancelToken.source = function source() {
  var cancel;
  var token = new CancelToken(function executor(c) {
    cancel = c;
  });
  return {
    token: token,
    cancel: cancel
  };
};

module.exports = CancelToken;


/***/ }),

/***/ "d4f6":
/***/ (function(module, exports, __webpack_require__) {

// 7.3.20 SpeciesConstructor(O, defaultConstructor)
var anObject = __webpack_require__("4d65");
var aFunction = __webpack_require__("9102");
var SPECIES = __webpack_require__("f3ae")('species');
module.exports = function (O, D) {
  var C = anObject(O).constructor;
  var S;
  return C === undefined || (S = anObject(C)[SPECIES]) == undefined ? D : aFunction(S);
};


/***/ }),

/***/ "d6e1":
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.9 / 15.2.3.2 Object.getPrototypeOf(O)
var has = __webpack_require__("3301");
var toObject = __webpack_require__("aa91");
var IE_PROTO = __webpack_require__("5ee9")('IE_PROTO');
var ObjectProto = Object.prototype;

module.exports = Object.getPrototypeOf || function (O) {
  O = toObject(O);
  if (has(O, IE_PROTO)) return O[IE_PROTO];
  if (typeof O.constructor == 'function' && O instanceof O.constructor) {
    return O.constructor.prototype;
  } return O instanceof Object ? ObjectProto : null;
};


/***/ }),

/***/ "d753":
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.14 / 15.2.3.14 Object.keys(O)
var $keys = __webpack_require__("7cbd");
var enumBugKeys = __webpack_require__("2ba0");

module.exports = Object.keys || function keys(O) {
  return $keys(O, enumBugKeys);
};


/***/ }),

/***/ "dad2":
/***/ (function(module, exports, __webpack_require__) {

var global = __webpack_require__("4839");
var navigator = global.navigator;

module.exports = navigator && navigator.userAgent || '';


/***/ }),

/***/ "dd61":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

module.exports = (
  utils.isStandardBrowserEnv() ?

  // Standard browser envs have full support of the APIs needed to test
  // whether the request URL is of the same origin as current location.
  (function standardBrowserEnv() {
    var msie = /(msie|trident)/i.test(navigator.userAgent);
    var urlParsingNode = document.createElement('a');
    var originURL;

    /**
    * Parse a URL to discover it's components
    *
    * @param {String} url The URL to be parsed
    * @returns {Object}
    */
    function resolveURL(url) {
      var href = url;

      if (msie) {
        // IE needs attribute set twice to normalize properties
        urlParsingNode.setAttribute('href', href);
        href = urlParsingNode.href;
      }

      urlParsingNode.setAttribute('href', href);

      // urlParsingNode provides the UrlUtils interface - http://url.spec.whatwg.org/#urlutils
      return {
        href: urlParsingNode.href,
        protocol: urlParsingNode.protocol ? urlParsingNode.protocol.replace(/:$/, '') : '',
        host: urlParsingNode.host,
        search: urlParsingNode.search ? urlParsingNode.search.replace(/^\?/, '') : '',
        hash: urlParsingNode.hash ? urlParsingNode.hash.replace(/^#/, '') : '',
        hostname: urlParsingNode.hostname,
        port: urlParsingNode.port,
        pathname: (urlParsingNode.pathname.charAt(0) === '/') ?
                  urlParsingNode.pathname :
                  '/' + urlParsingNode.pathname
      };
    }

    originURL = resolveURL(window.location.href);

    /**
    * Determine if a URL shares the same origin as the current location
    *
    * @param {String} requestURL The URL to test
    * @returns {boolean} True if URL shares the same origin, otherwise false
    */
    return function isURLSameOrigin(requestURL) {
      var parsed = (utils.isString(requestURL)) ? resolveURL(requestURL) : requestURL;
      return (parsed.protocol === originURL.protocol &&
            parsed.host === originURL.host);
    };
  })() :

  // Non standard browser envs (web workers, react-native) lack needed support.
  (function nonStandardBrowserEnv() {
    return function isURLSameOrigin() {
      return true;
    };
  })()
);


/***/ }),

/***/ "e552":
/***/ (function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("690e")(false);
// imports


// module
exports.push([module.i, ".pagination{-webkit-box-pack:center;-ms-flex-pack:center;justify-content:center}", ""]);

// exports


/***/ }),

/***/ "e555":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

// Headers whose duplicates are ignored by node
// c.f. https://nodejs.org/api/http.html#http_message_headers
var ignoreDuplicateOf = [
  'age', 'authorization', 'content-length', 'content-type', 'etag',
  'expires', 'from', 'host', 'if-modified-since', 'if-unmodified-since',
  'last-modified', 'location', 'max-forwards', 'proxy-authorization',
  'referer', 'retry-after', 'user-agent'
];

/**
 * Parse headers into an object
 *
 * ```
 * Date: Wed, 27 Aug 2014 08:58:49 GMT
 * Content-Type: application/json
 * Connection: keep-alive
 * Transfer-Encoding: chunked
 * ```
 *
 * @param {String} headers Headers needing to be parsed
 * @returns {Object} Headers parsed into an object
 */
module.exports = function parseHeaders(headers) {
  var parsed = {};
  var key;
  var val;
  var i;

  if (!headers) { return parsed; }

  utils.forEach(headers.split('\n'), function parser(line) {
    i = line.indexOf(':');
    key = utils.trim(line.substr(0, i)).toLowerCase();
    val = utils.trim(line.substr(i + 1));

    if (key) {
      if (parsed[key] && ignoreDuplicateOf.indexOf(key) >= 0) {
        return;
      }
      if (key === 'set-cookie') {
        parsed[key] = (parsed[key] ? parsed[key] : []).concat([val]);
      } else {
        parsed[key] = parsed[key] ? parsed[key] + ', ' + val : val;
      }
    }
  });

  return parsed;
};


/***/ }),

/***/ "e67d":
/***/ (function(module, exports) {

// document.currentScript polyfill by Adam Miller

// MIT license

(function(document){
  var currentScript = "currentScript",
      scripts = document.getElementsByTagName('script'); // Live NodeList collection

  // If browser needs currentScript polyfill, add get currentScript() to the document object
  if (!(currentScript in document)) {
    Object.defineProperty(document, currentScript, {
      get: function(){

        // IE 6-10 supports script readyState
        // IE 10+ support stack trace
        try { throw new Error(); }
        catch (err) {

          // Find the second match for the "at" string to get file src url from stack.
          // Specifically works with the format of stack traces in IE.
          var i, res = ((/.*at [^\(]*\((.*):.+:.+\)$/ig).exec(err.stack) || [false])[1];

          // For all scripts on the page, if src matches or if ready state is interactive, return the script tag
          for(i in scripts){
            if(scripts[i].src == res || scripts[i].readyState == "interactive"){
              return scripts[i];
            }
          }

          // If no match, return null
          return null;
        }
      }
    });
  }
})(document);


/***/ }),

/***/ "e6ca":
/***/ (function(module, exports, __webpack_require__) {

var global = __webpack_require__("4839");
var macrotask = __webpack_require__("0af5").set;
var Observer = global.MutationObserver || global.WebKitMutationObserver;
var process = global.process;
var Promise = global.Promise;
var isNode = __webpack_require__("9b6d")(process) == 'process';

module.exports = function () {
  var head, last, notify;

  var flush = function () {
    var parent, fn;
    if (isNode && (parent = process.domain)) parent.exit();
    while (head) {
      fn = head.fn;
      head = head.next;
      try {
        fn();
      } catch (e) {
        if (head) notify();
        else last = undefined;
        throw e;
      }
    } last = undefined;
    if (parent) parent.enter();
  };

  // Node.js
  if (isNode) {
    notify = function () {
      process.nextTick(flush);
    };
  // browsers with MutationObserver, except iOS Safari - https://github.com/zloirock/core-js/issues/339
  } else if (Observer && !(global.navigator && global.navigator.standalone)) {
    var toggle = true;
    var node = document.createTextNode('');
    new Observer(flush).observe(node, { characterData: true }); // eslint-disable-line no-new
    notify = function () {
      node.data = toggle = !toggle;
    };
  // environments with maybe non-completely correct, but existent Promise
  } else if (Promise && Promise.resolve) {
    // Promise.resolve without an argument throws an error in LG WebOS 2
    var promise = Promise.resolve(undefined);
    notify = function () {
      promise.then(flush);
    };
  // for other environments - macrotask based on:
  // - setImmediate
  // - MessageChannel
  // - window.postMessag
  // - onreadystatechange
  // - setTimeout
  } else {
    notify = function () {
      // strange IE + webpack dev server bug - use .call(global)
      macrotask.call(global, flush);
    };
  }

  return function (fn) {
    var task = { fn: fn, next: undefined };
    if (last) last.next = task;
    if (!head) {
      head = task;
      notify();
    } last = task;
  };
};


/***/ }),

/***/ "eb90":
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var utils = __webpack_require__("3332");

function InterceptorManager() {
  this.handlers = [];
}

/**
 * Add a new interceptor to the stack
 *
 * @param {Function} fulfilled The function to handle `then` for a `Promise`
 * @param {Function} rejected The function to handle `reject` for a `Promise`
 *
 * @return {Number} An ID used to remove interceptor later
 */
InterceptorManager.prototype.use = function use(fulfilled, rejected) {
  this.handlers.push({
    fulfilled: fulfilled,
    rejected: rejected
  });
  return this.handlers.length - 1;
};

/**
 * Remove an interceptor from the stack
 *
 * @param {Number} id The ID that was returned by `use`
 */
InterceptorManager.prototype.eject = function eject(id) {
  if (this.handlers[id]) {
    this.handlers[id] = null;
  }
};

/**
 * Iterate over all the registered interceptors
 *
 * This method is particularly useful for skipping over any
 * interceptors that may have become `null` calling `eject`.
 *
 * @param {Function} fn The function to call for each interceptor
 */
InterceptorManager.prototype.forEach = function forEach(fn) {
  utils.forEach(this.handlers, function forEachHandler(h) {
    if (h !== null) {
      fn(h);
    }
  });
};

module.exports = InterceptorManager;


/***/ }),

/***/ "f03e":
/***/ (function(module, exports) {

module.exports = {};


/***/ }),

/***/ "f043":
/***/ (function(module, exports, __webpack_require__) {

var redefine = __webpack_require__("7f00");
module.exports = function (target, src, safe) {
  for (var key in src) redefine(target, key, src[key], safe);
  return target;
};


/***/ }),

/***/ "f10a":
/***/ (function(module, exports, __webpack_require__) {

var ctx = __webpack_require__("0709");
var call = __webpack_require__("181f");
var isArrayIter = __webpack_require__("577d");
var anObject = __webpack_require__("4d65");
var toLength = __webpack_require__("33f2");
var getIterFn = __webpack_require__("a5de");
var BREAK = {};
var RETURN = {};
var exports = module.exports = function (iterable, entries, fn, that, ITERATOR) {
  var iterFn = ITERATOR ? function () { return iterable; } : getIterFn(iterable);
  var f = ctx(fn, that, entries ? 2 : 1);
  var index = 0;
  var length, step, iterator, result;
  if (typeof iterFn != 'function') throw TypeError(iterable + ' is not iterable!');
  // fast case for arrays with default iterator
  if (isArrayIter(iterFn)) for (length = toLength(iterable.length); length > index; index++) {
    result = entries ? f(anObject(step = iterable[index])[0], step[1]) : f(iterable[index]);
    if (result === BREAK || result === RETURN) return result;
  } else for (iterator = iterFn.call(iterable); !(step = iterator.next()).done;) {
    result = call(iterator, f, step.value, entries);
    if (result === BREAK || result === RETURN) return result;
  }
};
exports.BREAK = BREAK;
exports.RETURN = RETURN;


/***/ }),

/***/ "f1de":
/***/ (function(module, exports, __webpack_require__) {

// style-loader: Adds some css to the DOM by adding a <style> tag

// load the styles
var content = __webpack_require__("2bef");
if(typeof content === 'string') content = [[module.i, content, '']];
if(content.locals) module.exports = content.locals;
// add CSS to Shadow Root
var add = __webpack_require__("565c").default
module.exports.__inject__ = function (shadowRoot) {
  add("642f24a0", content, shadowRoot)
};

/***/ }),

/***/ "f3ae":
/***/ (function(module, exports, __webpack_require__) {

var store = __webpack_require__("13c4")('wks');
var uid = __webpack_require__("75ff");
var Symbol = __webpack_require__("4839").Symbol;
var USE_SYMBOL = typeof Symbol == 'function';

var $exports = module.exports = function (name) {
  return store[name] || (store[name] =
    USE_SYMBOL && Symbol[name] || (USE_SYMBOL ? Symbol : uid)('Symbol.' + name));
};

$exports.store = store;


/***/ }),

/***/ "fbd3":
/***/ (function(module, exports, __webpack_require__) {

var ITERATOR = __webpack_require__("f3ae")('iterator');
var SAFE_CLOSING = false;

try {
  var riter = [7][ITERATOR]();
  riter['return'] = function () { SAFE_CLOSING = true; };
  // eslint-disable-next-line no-throw-literal
  Array.from(riter, function () { throw 2; });
} catch (e) { /* empty */ }

module.exports = function (exec, skipClosing) {
  if (!skipClosing && !SAFE_CLOSING) return false;
  var safe = false;
  try {
    var arr = [7];
    var iter = arr[ITERATOR]();
    iter.next = function () { return { done: safe = true }; };
    arr[ITERATOR] = function () { return iter; };
    exec(arr);
  } catch (e) { /* empty */ }
  return safe;
};


/***/ })

/******/ });
//# sourceMappingURL=halo-comment.js.map
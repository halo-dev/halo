/*!
 * Bootstrap Colorpicker - Simple and customizable colorpicker component for Twitter Bootstrap.
 * @package bootstrap-colorpicker
 * @version v3.0.0-wip
 * @license MIT
 * @link https://farbelous.github.io/bootstrap-colorpicker/
 * @link https://github.com/farbelous/bootstrap-colorpicker.git
 */
(function webpackUniversalModuleDefinition(root, factory) {
	if(typeof exports === 'object' && typeof module === 'object')
		module.exports = factory(require("jQuery"));
	else if(typeof define === 'function' && define.amd)
		define("bootstrap-colorpicker", ["jQuery"], factory);
	else if(typeof exports === 'object')
		exports["bootstrap-colorpicker"] = factory(require("jQuery"));
	else
		root["bootstrap-colorpicker"] = factory(root["jQuery"]);
})(this, function(__WEBPACK_EXTERNAL_MODULE_0__) {
return /******/ (function(modules) { // webpackBootstrap
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
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
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
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 3);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports) {

module.exports = __WEBPACK_EXTERNAL_MODULE_0__;

/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _jquery = __webpack_require__(0);

var _jquery2 = _interopRequireDefault(_jquery);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/**
 * Colorpicker extension class.
 */
var Extension = function () {
  /**
   * @param {Colorpicker} colorpicker
   * @param {Object} options
   */
  function Extension(colorpicker) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    _classCallCheck(this, Extension);

    /**
     * @type {Colorpicker}
     */
    this.colorpicker = colorpicker;
    /**
     * @type {Object}
     */
    this.options = options;

    if (!(this.colorpicker.element && this.colorpicker.element.length)) {
      throw new Error('Extension: this.colorpicker.element is not valid');
    }

    this.colorpicker.element.on('colorpickerCreate.colorpicker-ext', _jquery2.default.proxy(this.onCreate, this));
    this.colorpicker.element.on('colorpickerDestroy.colorpicker-ext', _jquery2.default.proxy(this.onDestroy, this));
    this.colorpicker.element.on('colorpickerUpdate.colorpicker-ext', _jquery2.default.proxy(this.onUpdate, this));
    this.colorpicker.element.on('colorpickerChange.colorpicker-ext', _jquery2.default.proxy(this.onChange, this));
    this.colorpicker.element.on('colorpickerInvalid.colorpicker-ext', _jquery2.default.proxy(this.onInvalid, this));
    this.colorpicker.element.on('colorpickerShow.colorpicker-ext', _jquery2.default.proxy(this.onShow, this));
    this.colorpicker.element.on('colorpickerHide.colorpicker-ext', _jquery2.default.proxy(this.onHide, this));
    this.colorpicker.element.on('colorpickerEnable.colorpicker-ext', _jquery2.default.proxy(this.onEnable, this));
    this.colorpicker.element.on('colorpickerDisable.colorpicker-ext', _jquery2.default.proxy(this.onDisable, this));
  }

  /**
   * Function called every time a new color needs to be created.
   * Return false to skip this resolver and continue with other extensions' ones
   * or return anything else to consider the color resolved.
   *
   * @param {Color|String|*} color
   * @return {Color|String|*}
   */


  _createClass(Extension, [{
    key: 'resolveColor',
    value: function resolveColor(color) {
      return false;
    }

    /**
     * @listens colorpickerCreate
     * @param {Event} event
     */

  }, {
    key: 'onCreate',
    value: function onCreate(event) {}
    // to be extended


    /**
     * @listens colorpickerDestroy
     * @param {Event} event
     */

  }, {
    key: 'onDestroy',
    value: function onDestroy(event) {
      this.colorpicker.element.off('.colorpicker-ext');
    }

    /**
     * @listens colorpickerUpdate
     * @param {Event} event
     */

  }, {
    key: 'onUpdate',
    value: function onUpdate(event) {}
    // to be extended


    /**
     * @listens colorpickerChange
     * @param {Event} event
     */

  }, {
    key: 'onChange',
    value: function onChange(event) {}
    // to be extended


    /**
     * @listens colorpickerInvalid
     * @param {Event} event
     */

  }, {
    key: 'onInvalid',
    value: function onInvalid(event) {}
    // to be extended


    /**
     * @listens colorpickerHide
     * @param {Event} event
     */

  }, {
    key: 'onHide',
    value: function onHide(event) {}
    // to be extended


    /**
     * @listens colorpickerShow
     * @param {Event} event
     */

  }, {
    key: 'onShow',
    value: function onShow(event) {}
    // to be extended


    /**
     * @listens colorpickerDisable
     * @param {Event} event
     */

  }, {
    key: 'onDisable',
    value: function onDisable(event) {}
    // to be extended


    /**
     * @listens colorpickerEnable
     * @param {Event} event
     */

  }, {
    key: 'onEnable',
    value: function onEnable(event) {
      // to be extended
    }
  }]);

  return Extension;
}();

exports.default = Extension;

/***/ }),
/* 2 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _Extension2 = __webpack_require__(1);

var _Extension3 = _interopRequireDefault(_Extension2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var defaults = {
  /**
   * Key-value pairs defining a color alias and its CSS color representation.
   *
   * They can also be just an array of values. In that case, no special names are used, only the real colors.
   *
   * @type {Object|Array}
   * @default null
   * @example
   *  {
   *   'black': '#000000',
   *   'white': '#ffffff',
   *   'red': '#FF0000',
   *   'default': '#777777',
   *   'primary': '#337ab7',
   *   'success': '#5cb85c',
   *   'info': '#5bc0de',
   *   'warning': '#f0ad4e',
   *   'danger': '#d9534f'
   *  }
   *
   * @example ['#f0ad4e', '#337ab7', '#5cb85c']
   */
  colors: null,
  /**
   * If true, the when a color swatch is selected the name (alias) will be used as input value,
   * otherwise the swatch real color value will be used.
   *
   * @type {boolean}
   * @default true
   */
  namesAsValues: true
};

var Palette = function (_Extension) {
  _inherits(Palette, _Extension);

  _createClass(Palette, [{
    key: 'colors',


    /**
     * @returns {Object|Array}
     */
    get: function get() {
      return this.options.colors;
    }
  }]);

  function Palette(colorpicker) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    _classCallCheck(this, Palette);

    var _this = _possibleConstructorReturn(this, (Palette.__proto__ || Object.getPrototypeOf(Palette)).call(this, colorpicker, Object.assign({}, defaults, options)));

    if (!Array.isArray(_this.options.colors) && _typeof(_this.options.colors) !== 'object') {
      _this.options.colors = null;
    }
    return _this;
  }

  /**
   * @returns {int}
   */


  _createClass(Palette, [{
    key: 'getLength',
    value: function getLength() {
      if (!this.options.colors) {
        return 0;
      }

      if (Array.isArray(this.options.colors)) {
        return this.options.colors.length;
      }

      if (_typeof(this.options.colors) === 'object') {
        return Object.keys(this.options.colors).length;
      }

      return 0;
    }
  }, {
    key: 'resolveColor',
    value: function resolveColor(color) {
      if (this.getLength() <= 0) {
        return false;
      }

      if (Array.isArray(this.options.colors)) {
        if (this.options.colors.indexOf(color) >= 0) {
          return color;
        }
        if (this.options.colors.indexOf(color.toUpperCase()) >= 0) {
          return color.toUpperCase();
        }
        if (this.options.colors.indexOf(color.toLowerCase()) >= 0) {
          return color.toLowerCase();
        }
        return false;
      }

      if (_typeof(this.options.colors) !== 'object') {
        return false;
      }

      if (!this.options.namesAsValues) {
        return this.getValue(color, false);
      }
      return this.getName(color, this.getName('#' + color, this.getValue(color, false)));
    }

    /**
     * Given a color value, returns the corresponding color name or defaultValue.
     *
     * @param {String} value
     * @param {*} defaultValue
     * @returns {*}
     */

  }, {
    key: 'getName',
    value: function getName(value) {
      var defaultValue = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

      if (!(typeof value === 'string') || !this.options.colors) {
        return defaultValue;
      }
      for (var name in this.options.colors) {
        if (!this.options.colors.hasOwnProperty(name)) {
          continue;
        }
        if (this.options.colors[name].toLowerCase() === value.toLowerCase()) {
          return name;
        }
      }
      return defaultValue;
    }

    /**
     * Given a color name, returns the corresponding color value or defaultValue.
     *
     * @param {String} name
     * @param {*} defaultValue
     * @returns {*}
     */

  }, {
    key: 'getValue',
    value: function getValue(name) {
      var defaultValue = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

      if (!(typeof name === 'string') || !this.options.colors) {
        return defaultValue;
      }
      if (this.options.colors.hasOwnProperty(name)) {
        return this.options.colors[name];
      }
      return defaultValue;
    }
  }]);

  return Palette;
}(_Extension3.default);

exports.default = Palette;

/***/ }),
/* 3 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _Colorpicker = __webpack_require__(4);

var _Colorpicker2 = _interopRequireDefault(_Colorpicker);

var _jquery = __webpack_require__(0);

var _jquery2 = _interopRequireDefault(_jquery);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var plugin = 'colorpicker';

_jquery2.default[plugin] = _Colorpicker2.default;

_jquery2.default.fn[plugin] = function (option) {
  var apiArgs = Array.prototype.slice.call(arguments, 1),
      isSingleElement = this.length === 1,
      returnValue = null;

  var $jq = this.each(function () {
    var $this = (0, _jquery2.default)(this),
        inst = $this.data(plugin),
        options = (typeof option === 'undefined' ? 'undefined' : _typeof(option)) === 'object' ? option : {};

    if (!inst) {
      inst = new _Colorpicker2.default(this, options);
      $this.data(plugin, inst);
    }

    if (typeof option === 'string') {
      if (option === 'colorpicker') {
        returnValue = inst;
      } else if (_jquery2.default.isFunction(inst[option])) {
        returnValue = inst[option].apply(inst, apiArgs);
      } else {
        // its a property ?
        if (apiArgs.length) {
          // set property
          inst[option] = apiArgs[0];
        }
        returnValue = inst[option];
      }
    } else {
      returnValue = $this;
    }
  });

  return isSingleElement ? returnValue : $jq;
};

_jquery2.default.fn[plugin].constructor = _Colorpicker2.default;

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _Color = __webpack_require__(5);

var _Color2 = _interopRequireDefault(_Color);

var _Extension = __webpack_require__(1);

var _Extension2 = _interopRequireDefault(_Extension);

var _options = __webpack_require__(7);

var _options2 = _interopRequireDefault(_options);

var _extensions = __webpack_require__(8);

var _extensions2 = _interopRequireDefault(_extensions);

var _jquery = __webpack_require__(0);

var _jquery2 = _interopRequireDefault(_jquery);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var colorPickerIdCounter = 0;

/**
 * Colorpicker widget class
 */

var Colorpicker = function () {
  _createClass(Colorpicker, [{
    key: 'color',


    /**
     * color getter
     *
     * @type {Color|null}
     */
    get: function get() {
      return this.element.data('color');
    }

    /**
     * color setter
     *
     * @ignore
     * @param {Color|null} value
     */
    ,
    set: function set(value) {
      this.element.data('color', value);
    }

    /**
     * @fires colorpickerCreate
     * @param {Object|String} element
     * @param {Object} options
     * @constructor
     */

  }], [{
    key: 'Color',

    /**
     * Color class
     *
     * @static
     * @type {Color}
     */
    get: function get() {
      return _Color2.default;
    }

    /**
     * Extension class
     *
     * @static
     * @type {Extension}
     */

  }, {
    key: 'Extension',
    get: function get() {
      return _Extension2.default;
    }

    /**
     * Colorpicker bundled extension classes
     *
     * @static
     * @type {{Extension}}
     */

  }, {
    key: 'Extensions',
    get: function get() {
      return _extensions2.default;
    }
  }]);

  function Colorpicker(element, options) {
    var _this = this;

    _classCallCheck(this, Colorpicker);

    colorPickerIdCounter += 1;
    /**
     * The colorpicker instance number
     * @type {number}
     */
    this.id = colorPickerIdCounter;

    /**
     * @type {*|jQuery}
     */
    this.element = (0, _jquery2.default)(element).addClass('colorpicker-element');
    this.element.attr('data-colorpicker-id', this.id);

    /**
     * @type {defaults}
     */
    this.options = Object.assign({}, _options2.default, options, this.element.data());

    /**
     * @type {Extension[]}
     */
    this.extensions = [];

    if (!Array.isArray(this.options.extensions)) {
      this.options.extensions = [];
    }

    /**
     * @type {*|jQuery}
     */
    this.component = this.options.component;
    this.component = this.component !== false ? this.element.find(this.component) : false;
    if (this.component && this.component.length === 0) {
      this.component = false;
    }

    /**
     * @type {*|jQuery}
     */
    this.container = this.options.container === true ? this.element : this.options.container;
    this.container = this.container !== false ? (0, _jquery2.default)(this.container) : false;

    /**
     * @type {*|String}
     * @private
     */
    this.currentSlider = null;

    /**
     * @type {{left: number, top: number}}
     * @private
     */
    this.mousePointer = {
      left: 0,
      top: 0
    };

    /**
     * Latest external event
     *
     * @type {{name: String, e: *}}
     * @private
     */
    this.lastEvent = {
      name: null,
      e: null
    };

    // Is the element an input? Should we search inside for any input?
    /**
     * @type {*|jQuery}
     */
    this.input = this.element.is('input') ? this.element : this.options.input ? this.element.find(this.options.input) : false;

    if (this.input && this.input.length === 0) {
      this.input = false;
    }

    if (this.options.debug) {
      this.options.extensions.push({ name: 'Debugger' });
    }

    // Register extensions
    this.options.extensions.forEach(function (ext) {
      _this.addExtension(ext.name, _extensions2.default[ext.name.toLowerCase()], ext);
    });

    var colorValue = this.options.color !== false ? this.options.color : this.getValue();

    this.color = colorValue ? this.createColor(colorValue) : false;

    if (this.options.format === false) {
      // If format is false, use the first parsed one from now on
      this.options.format = this.color.format;
    }

    /**
     * @type {boolean}
     * @private
     */
    this.disabled = false;

    // Setup picker
    var $picker = this.picker = (0, _jquery2.default)(this.options.template);

    if (this.options.customClass) {
      $picker.addClass(this.options.customClass);
    }
    if (this.options.inline) {
      $picker.addClass('colorpicker-inline colorpicker-visible');
    } else {
      $picker.addClass('colorpicker-hidden');
    }
    if (this.options.horizontal) {
      $picker.addClass('colorpicker-horizontal');
    }

    if ((this.options.useAlpha || this.hasColor() && this.color.hasTransparency()) && this.options.useAlpha !== false) {
      this.options.useAlpha = true;
      $picker.addClass('colorpicker-with-alpha');
    }

    if (this.options.align === 'right') {
      $picker.addClass('colorpicker-right');
    }
    if (this.options.inline === true) {
      $picker.addClass('colorpicker-no-arrow');
    }

    // Prevent closing the colorpicker when clicking on itself
    $picker.on('mousedown.colorpicker touchstart.colorpicker', _jquery2.default.proxy(function (e) {
      if (e.target === e.currentTarget) {
        e.preventDefault();
      }
    }, this));

    // Bind click/tap events on the sliders
    $picker.find('.colorpicker-saturation, .colorpicker-hue, .colorpicker-alpha').on('mousedown.colorpicker touchstart.colorpicker', _jquery2.default.proxy(this._mousedown, this));

    $picker.appendTo(this.container ? this.container : (0, _jquery2.default)('body'));

    // Bind other events
    if (this.hasInput()) {
      this.input.on({
        'keyup.colorpicker': _jquery2.default.proxy(this._keyup, this)
      });
      this.input.on({
        'change.colorpicker': _jquery2.default.proxy(this._change, this)
      });
      if (this.component === false) {
        this.element.on({
          'focus.colorpicker': _jquery2.default.proxy(this.show, this)
        });
      }
      if (this.options.inline === false) {
        this.element.on({
          'focusout.colorpicker': _jquery2.default.proxy(this.hide, this)
        });
      }
    }

    if (this.component !== false) {
      this.component.on({
        'click.colorpicker': _jquery2.default.proxy(this.show, this)
      });
    }

    if (this.hasInput() === false && this.component === false && !this.element.has('.colorpicker')) {
      this.element.on({
        'click.colorpicker': _jquery2.default.proxy(this.show, this)
      });
    }

    // for HTML5 input[type='color']
    if (this.hasInput() && this.component !== false && this.input.attr('type') === 'color') {
      this.input.on({
        'click.colorpicker': _jquery2.default.proxy(this.show, this),
        'focus.colorpicker': _jquery2.default.proxy(this.show, this)
      });
    }

    // Update if there is a color option
    this.update(this.options.color !== false);

    (0, _jquery2.default)(_jquery2.default.proxy(function () {
      /**
       * (Colorpicker) When the Colorpicker instance has been created and the DOM is ready.
       *
       * @event colorpickerCreate
       */
      this.element.trigger({
        type: 'colorpickerCreate',
        colorpicker: this,
        color: this.color
      });
    }, this));
  }

  /**
   * Creates and registers the given extension
   *
   * @param {String|Extension} extensionName
   * @param {Extension} ExtensionClass
   * @param {Object} [config]
   * @returns {Extension}
   */


  _createClass(Colorpicker, [{
    key: 'addExtension',
    value: function addExtension(extensionName, ExtensionClass) {
      var config = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

      var ext = extensionName instanceof _Extension2.default ? extensionName : new ExtensionClass(this, config);

      this.extensions.push(ext);
      return ext;
    }

    /**
     * Destroys the current instance
     *
     * @fires colorpickerDestroy
     */

  }, {
    key: 'destroy',
    value: function destroy() {
      this.picker.remove();
      this.element.removeData('colorpicker', 'color').off('.colorpicker');
      if (this.hasInput()) {
        this.input.off('.colorpicker');
      }
      if (this.component !== false) {
        this.component.off('.colorpicker');
      }
      this.element.removeClass('colorpicker-element');

      /**
       * (Colorpicker) When the instance is destroyed with all events unbound.
       *
       * @event colorpickerDestroy
       */
      this.element.trigger({
        type: 'colorpickerDestroy',
        colorpicker: this,
        color: this.color
      });
    }

    /**
     * Returns true if the current color object is an instance of Color, false otherwise.
     * @returns {boolean}
     */

  }, {
    key: 'hasColor',
    value: function hasColor() {
      return this.color instanceof _Color2.default;
    }

    /**
     * @returns {*|String|Color}
     */

  }, {
    key: 'toInputColorString',


    /**
     * Formatted color string, with the formatting options applied
     * (e.g. useHashPrefix)
     * @returns {String}
     */
    value: function toInputColorString() {
      var str = this.toCssColorString();

      if (!str) {
        return str;
      }

      if (this.options.useHashPrefix === false) {
        str = str.replace(/^#/g, '');
      }

      return this._resolveColor(str);
    }

    /**
     * Formatted color string, suitable for CSS
     * @returns {String}
     */

  }, {
    key: 'toCssColorString',
    value: function toCssColorString() {
      if (!this.hasColor()) {
        return '';
      }
      return this.color.toString(this.format);
    }

    /**
     * If the widget is not inside a container or inline, rearranges its position relative to its element offset.
     *
     * @param {Event} [e]
     * @private
     * @returns {boolean} Returns false if the widget is inside a container or inline, true otherwise
     */

  }, {
    key: '_reposition',
    value: function _reposition(e) {
      this.lastEvent.name = 'reposition';
      this.lastEvent.e = e;

      if (this.options.inline !== false || this.options.container) {
        return false;
      }
      var type = this.container && this.container[0] !== window.document.body ? 'position' : 'offset';
      var element = this.component || this.element;
      var offset = element[type]();

      if (this.options.align === 'right') {
        offset.left -= this.picker.outerWidth() - element.outerWidth();
      }
      this.picker.css({
        top: offset.top + element.outerHeight(),
        left: offset.left
      });
      return true;
    }

    /**
     * Shows the colorpicker widget if hidden.
     * If the input is disabled this call will be ignored.
     *
     * @fires colorpickerShow
     * @param {Event} [e]
     * @returns {boolean} True if was hidden and afterwards visible, false if nothing happened.
     */

  }, {
    key: 'show',
    value: function show(e) {
      this.lastEvent.name = 'show';
      this.lastEvent.e = e;

      if (this.isVisible() || this.isDisabled()) {
        // Don't show the widget if it's already visible or it is disabled
        return false;
      }
      this.picker.addClass('colorpicker-visible').removeClass('colorpicker-hidden');

      this._reposition(e);
      (0, _jquery2.default)(window).on('resize.colorpicker', _jquery2.default.proxy(this._reposition, this));

      if (e && (!this.hasInput() || this.input.attr('type') === 'color')) {
        if (e.stopPropagation && e.preventDefault) {
          e.stopPropagation();
          e.preventDefault();
        }
      }
      if ((this.component || !this.input) && this.options.inline === false) {
        (0, _jquery2.default)(window.document).on({
          'mousedown.colorpicker': _jquery2.default.proxy(this.hide, this)
        });
      }

      /**
       * (Colorpicker) When show() is called and the widget can be shown.
       *
       * @event colorpickerShow
       */
      this.element.trigger({
        type: 'colorpickerShow',
        colorpicker: this,
        color: this.color
      });

      return true;
    }

    /**
     * Hides the colorpicker widget.
     * Hide is prevented when it is triggered by an event whose target element has been clicked/touched.
     *
     * @fires colorpickerHide
     * @param {Event} [e]
     * @returns {boolean} True if was visible and afterwards hidden, false if nothing happened.
     */

  }, {
    key: 'hide',
    value: function hide(e) {
      this.lastEvent.name = 'hide';
      this.lastEvent.e = e;

      if (this.isHidden()) {
        // Do not trigger if already hidden
        return false;
      }
      if (typeof e !== 'undefined' && e.target) {
        // Prevent hide if triggered by an event and an element inside the colorpicker has been clicked/touched
        if ((0, _jquery2.default)(e.currentTarget).parents('.colorpicker').length > 0 || (0, _jquery2.default)(e.target).parents('.colorpicker').length > 0) {
          return false;
        }
      }
      this.picker.addClass('colorpicker-hidden').removeClass('colorpicker-visible');
      (0, _jquery2.default)(window).off('resize.colorpicker', this._reposition);
      (0, _jquery2.default)(window.document).off({
        'mousedown.colorpicker': this.hide
      });

      /**
       * (Colorpicker) When hide() is called and the widget can be hidden.
       *
       * @event colorpickerHide
       */
      this.element.trigger({
        type: 'colorpickerHide',
        colorpicker: this,
        color: this.color
      });
      return true;
    }

    /**
     * Returns true if the colorpicker element has the colorpicker-visible class and not the colorpicker-hidden one.
     * False otherwise.
     *
     * @returns {boolean}
     */

  }, {
    key: 'isVisible',
    value: function isVisible() {
      return this.picker.hasClass('colorpicker-visible') && !this.picker.hasClass('colorpicker-hidden');
    }

    /**
     * Returns true if the colorpicker element has the colorpicker-hidden class and not the colorpicker-visible one.
     * False otherwise.
     *
     * @returns {boolean}
     */

  }, {
    key: 'isHidden',
    value: function isHidden() {
      return this.picker.hasClass('colorpicker-hidden') && !this.picker.hasClass('colorpicker-visible');
    }

    /**
     * If the input element is present, it updates the value with the current color object color string.
     * If value is set, this method fires a "change" event on the input element.
     *
     * @fires change
     * @private
     */

  }, {
    key: '_updateInput',
    value: function _updateInput() {
      if (this.hasInput()) {
        var val = this.toInputColorString();

        if (val === this.input.prop('value')) {
          // No need to set value or trigger any event if nothing changed
          return;
        }

        this.input.prop('value', val ? val : '');

        /**
         * (Input) Triggered on the input element when a new color is selected.
         *
         * @event change
         */
        this.input.trigger({
          type: 'change',
          colorpicker: this,
          color: this.color,
          value: val
        });
      }
    }

    /**
     * Changes the color adjustment bars using the current color object information.
     * @private
     */

  }, {
    key: '_updatePicker',
    value: function _updatePicker() {
      if (!this.hasColor()) {
        return;
      }

      var vertical = this.options.horizontal === false,
          sl = vertical ? this.options.sliders : this.options.slidersHorz;

      var saturationGuide = this.picker.find('.colorpicker-saturation .colorpicker-guide'),
          hueGuide = this.picker.find('.colorpicker-hue .colorpicker-guide'),
          alphaGuide = this.picker.find('.colorpicker-alpha .colorpicker-guide');

      var hsva = this.color.hsvaRatio;

      if (hueGuide.length) {
        hueGuide.css(vertical ? 'top' : 'left', (vertical ? sl.hue.maxTop : sl.hue.maxLeft) * (1 - hsva.h));
      }

      if (alphaGuide.length) {
        alphaGuide.css(vertical ? 'top' : 'left', (vertical ? sl.alpha.maxTop : sl.alpha.maxLeft) * (1 - hsva.a));
      }

      if (saturationGuide.length) {
        saturationGuide.css({
          'top': sl.saturation.maxTop - hsva.v * sl.saturation.maxTop,
          'left': hsva.s * sl.saturation.maxLeft
        });
      }

      this.picker.find('.colorpicker-saturation').css('backgroundColor', this.color.getHueOnlyCopy().toHexString()); // we only need hue

      this.picker.find('.colorpicker-alpha').css('backgroundColor', this.color.toString('hex6')); // we don't need alpha
    }

    /**
     * If the component element is present, its background color is updated
     * @private
     */

  }, {
    key: '_updateComponent',
    value: function _updateComponent() {
      if (!this.hasColor()) {
        return;
      }

      if (this.component !== false) {
        var icn = this.component.find('i').eq(0);

        if (icn.length > 0) {
          icn.css({
            'backgroundColor': this.toCssColorString()
          });
        } else {
          this.component.css({
            'backgroundColor': this.toCssColorString()
          });
        }
      }
    }

    /**
     * @private
     * @returns {boolean}
     */

  }, {
    key: '_shouldUpdate',
    value: function _shouldUpdate() {
      return this.hasColor() && this.getValue(false) !== false;
    }

    /**
     * Updated the component color, the input value and the widget if a color is present.
     *
     * If force is true, it is updated anyway.
     *
     * @fires colorpickerUpdate
     * @param {boolean} [force]
     */

  }, {
    key: 'update',
    value: function update() {
      var force = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;

      if (this._shouldUpdate() || force === true) {
        // Update only if the current value (from input or data) is not empty
        this._updateComponent();

        // Do not update input when autoInputFallback is disabled and last event is keyup.
        var preventInputUpdate = this.options.autoInputFallback !== true &&
        // this.isInvalidColor() ||  // prevent also on invalid color (on create, leaves invalid colors)
        this.lastEvent.name === 'keyup';

        if (!preventInputUpdate) {
          this._updateInput();
        }

        this._updatePicker();

        /**
         * (Colorpicker) Fired when the widget is updated.
         *
         * @event colorpickerUpdate
         */
        this.element.trigger({
          type: 'colorpickerUpdate',
          colorpicker: this,
          color: this.color
        });
      }
    }

    /**
     * Returns the color string from the input value or the 'data-color' attribute of the input or element.
     * If empty, it returns the defaultValue parameter.
     *
     * @param {String|*} [defaultValue]
     * @returns {String|*}
     */

  }, {
    key: 'getValue',
    value: function getValue() {
      var defaultValue = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;

      defaultValue = typeof defaultValue === 'undefined' ? this.fallbackColor : defaultValue;
      var candidates = [],
          val = false;

      if (this.hasInput()) {
        candidates.push(this.input.val());
        candidates.push(this.input.data('color'));
      }
      candidates.push(this.element.data('color'));

      candidates.map(function (item) {
        if (item && val === false) {
          val = item;
        }
      });

      val = val === false ? defaultValue : val;

      if (val instanceof _Color2.default) {
        return val.toString(this.format);
      }

      return val;
    }

    /**
     * Sets the color manually
     *
     * @fires colorpickerChange
     * @param {String|Color} val
     */

  }, {
    key: 'setValue',
    value: function setValue(val) {
      if (this.hasColor() && this.color.equals(val)) {
        // equal color object
        return;
      }

      var color = val ? this.createColor(val) : false;

      if (!this.hasColor() && !color) {
        // color was empty and the new one too
        return;
      }

      // force update if color is changed to empty
      var shouldForceUpdate = this.hasColor() && !color;

      this.color = color;

      /**
       * (Colorpicker) When the color is set programmatically with setValue().
       *
       * @event colorpickerChange
       */
      this.element.trigger({
        type: 'colorpickerChange',
        colorpicker: this,
        color: this.color,
        value: val
      });

      // force update if color has changed to empty
      this.update(shouldForceUpdate);
    }

    /**
     * Creates a new color using the widget instance options (fallbackColor, format).
     *
     * @fires colorpickerInvalid
     * @param {*} val
     * @param {boolean} useFallback
     * @returns {Color}
     */

  }, {
    key: 'createColor',
    value: function createColor(val) {
      var useFallback = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;

      var color = new _Color2.default(this._resolveColor(val), { format: this.format });

      if (!color.isValid()) {
        var invalidColor = color,
            fallback = void 0;

        if (useFallback) {
          fallback = this.fallbackColor instanceof _Color2.default && this.fallbackColor.isValid() ? this.fallbackColor : this._resolveColor(this.fallbackColor);

          color = new _Color2.default(fallback, { format: this.format });

          if (!color.isValid() && useFallback) {
            throw new Error('The fallback color is invalid.');
          }
        }

        color.previous = invalidColor;

        /**
         * (Colorpicker) Fired when the color is invalid and the fallback color is going to be used.
         *
         * @event colorpickerInvalid
         */
        this.element.trigger({
          type: 'colorpickerInvalid',
          colorpicker: this,
          color: color,
          value: val
        });
      }

      if (!this.isAlphaEnabled() && color.hasTransparency()) {
        // Alpha is disabled
        color.setAlpha(1);
      }

      if (!this.hasColor()) {
        // No previous color, so no need to compare
        return color;
      }

      var hsva = color.hsvaRatio;
      var prevHsva = this.color.hsvaRatio;

      if (hsva.s === 0 && hsva.h === 0 && prevHsva.h !== 0) {
        // Hue was set to 0 because saturation was 0, use previous hue, since it was not meant to change...
        color.setHueRatio(prevHsva.h);
      }

      if (!this.isAlphaEnabled() && color.hasTransparency()) {
        // Alpha is disabled
        color.setAlpha(1);
      }

      return color;
    }

    /**
     * Checks if there is a color object, that it is valid and it is not a fallback
     * @returns {boolean}
     */

  }, {
    key: 'isInvalidColor',
    value: function isInvalidColor() {
      return !this.hasColor() || !this.color.isValid() || !!this.color.previous;
    }

    /**
     * Returns true if the useAlpha option is exactly true, false otherwise
     * @returns {boolean}
     */

  }, {
    key: 'isAlphaEnabled',
    value: function isAlphaEnabled() {
      return this.options.useAlpha === true;
    }

    /**
     * Resolves a color, in case is not in a standard format (e.g. a custom color alias)
     *
     * @private
     * @param {String|*} color
     * @returns {String|*|null}
     */

  }, {
    key: '_resolveColor',
    value: function _resolveColor(color) {
      var extResolvedColor = false;

      _jquery2.default.each(this.extensions, function (name, ext) {
        if (extResolvedColor !== false) {
          // skip if resolved
          return;
        }
        extResolvedColor = ext.resolveColor(color);
      });

      if (extResolvedColor !== false) {
        color = extResolvedColor;
      }

      return color;
    }

    /**
     * Returns true if the widget has an associated input element, false otherwise
     * @returns {boolean}
     */

  }, {
    key: 'hasInput',
    value: function hasInput() {
      return this.input !== false;
    }

    /**
     * Returns true if this instance is disabled
     * @returns {boolean}
     */

  }, {
    key: 'isDisabled',
    value: function isDisabled() {
      return this.disabled === true;
    }

    /**
     * Disables the widget and the input if any
     *
     * @fires colorpickerDisable
     * @returns {boolean}
     */

  }, {
    key: 'disable',
    value: function disable() {
      if (this.hasInput()) {
        this.input.prop('disabled', true);
      }
      this.disabled = true;

      /**
       * (Colorpicker) When the widget has been disabled.
       *
       * @event colorpickerDisable
       */
      this.element.trigger({
        type: 'colorpickerDisable',
        colorpicker: this,
        color: this.color
      });
      return true;
    }

    /**
     * Enables the widget and the input if any
     *
     * @fires colorpickerEnable
     * @returns {boolean}
     */

  }, {
    key: 'enable',
    value: function enable() {
      if (this.hasInput()) {
        this.input.prop('disabled', false);
      }
      this.disabled = false;

      /**
       * (Colorpicker) When the widget has been enabled.
       *
       * @event colorpickerEnable
       */
      this.element.trigger({
        type: 'colorpickerEnable',
        colorpicker: this,
        color: this.color
      });
      return true;
    }

    /**
     * Function triggered when clicking in one of the color adjustment bars
     *
     * @private
     * @fires mousemove
     * @param {Event} e
     * @returns {boolean}
     */

  }, {
    key: '_mousedown',
    value: function _mousedown(e) {
      this.lastEvent.name = 'mousedown';
      this.lastEvent.e = e;

      if (!e.pageX && !e.pageY && e.originalEvent && e.originalEvent.touches) {
        e.pageX = e.originalEvent.touches[0].pageX;
        e.pageY = e.originalEvent.touches[0].pageY;
      }
      e.stopPropagation();
      e.preventDefault();

      var target = (0, _jquery2.default)(e.target);

      // detect the slider and set the limits and callbacks
      var zone = target.closest('div');
      var sl = this.options.horizontal ? this.options.slidersHorz : this.options.sliders;

      if (!zone.is('.colorpicker')) {
        if (zone.is('.colorpicker-saturation')) {
          this.currentSlider = _jquery2.default.extend({}, sl.saturation);
        } else if (zone.is('.colorpicker-hue')) {
          this.currentSlider = _jquery2.default.extend({}, sl.hue);
        } else if (zone.is('.colorpicker-alpha')) {
          this.currentSlider = _jquery2.default.extend({}, sl.alpha);
        } else {
          return false;
        }
        var offset = zone.offset();
        // reference to guide's style

        this.currentSlider.guide = zone.find('.colorpicker-guide')[0].style;
        this.currentSlider.left = e.pageX - offset.left;
        this.currentSlider.top = e.pageY - offset.top;
        this.mousePointer = {
          left: e.pageX,
          top: e.pageY
        };

        /**
         * (window.document) Triggered on mousedown for the document object,
         * so the color adjustment guide is moved to the clicked position.
         *
         * @event mousemove
         */
        (0, _jquery2.default)(window.document).on({
          'mousemove.colorpicker': _jquery2.default.proxy(this._mousemove, this),
          'touchmove.colorpicker': _jquery2.default.proxy(this._mousemove, this),
          'mouseup.colorpicker': _jquery2.default.proxy(this._mouseup, this),
          'touchend.colorpicker': _jquery2.default.proxy(this._mouseup, this)
        }).trigger('mousemove');
      }
      return false;
    }

    /**
     * Function triggered when dragging a guide inside one of the color adjustment bars.
     *
     * @private
     * @param {Event} e
     * @returns {boolean}
     */

  }, {
    key: '_mousemove',
    value: function _mousemove(e) {
      this.lastEvent.name = 'mousemove';
      this.lastEvent.e = e;

      var color = !this.hasColor() ? this.createColor(this.fallbackColor) : this.color.getCopy();

      if (!e.pageX && !e.pageY && e.originalEvent && e.originalEvent.touches) {
        e.pageX = e.originalEvent.touches[0].pageX;
        e.pageY = e.originalEvent.touches[0].pageY;
      }
      e.stopPropagation();
      e.preventDefault();
      var left = Math.max(0, Math.min(this.currentSlider.maxLeft, this.currentSlider.left + ((e.pageX || this.mousePointer.left) - this.mousePointer.left)));
      var top = Math.max(0, Math.min(this.currentSlider.maxTop, this.currentSlider.top + ((e.pageY || this.mousePointer.top) - this.mousePointer.top)));

      this.currentSlider.guide.left = left + 'px';
      this.currentSlider.guide.top = top + 'px';
      if (this.currentSlider.callLeft) {
        color[this.currentSlider.callLeft].call(color, left / this.currentSlider.maxLeft);
      }
      if (this.currentSlider.callTop) {
        color[this.currentSlider.callTop].call(color, top / this.currentSlider.maxTop);
      }

      this.setValue(color);
      return false;
    }

    /**
     * Function triggered when releasing the click in one of the color adjustment bars.
     *
     * @private
     * @param {Event} e
     * @returns {boolean}
     */

  }, {
    key: '_mouseup',
    value: function _mouseup(e) {
      this.lastEvent.name = 'mouseup';
      this.lastEvent.e = e;

      e.stopPropagation();
      e.preventDefault();
      (0, _jquery2.default)(window.document).off({
        'mousemove.colorpicker': this._mousemove,
        'touchmove.colorpicker': this._mousemove,
        'mouseup.colorpicker': this._mouseup,
        'touchend.colorpicker': this._mouseup
      });
      return false;
    }

    /**
     * Function triggered when the input has changed, so the colorpicker gets updated.
     *
     * @private
     * @param {Event} e
     * @returns {boolean}
     */

  }, {
    key: '_change',
    value: function _change(e) {
      this.lastEvent.name = 'change';
      this.lastEvent.e = e;

      var val = this.input.val();

      if (val !== this.toInputColorString()) {
        this.setValue(val);
      }
    }

    /**
     * Function triggered after a keyboard key has been released.
     *
     * @private
     * @param {Event} e
     * @returns {boolean}
     */

  }, {
    key: '_keyup',
    value: function _keyup(e) {
      this.lastEvent.name = 'keyup';
      this.lastEvent.e = e;

      var val = this.input.val();

      if (val !== this.toInputColorString()) {
        this.setValue(val);
      }
    }
  }, {
    key: 'fallbackColor',
    get: function get() {
      return this.options.fallbackColor ? this.options.fallbackColor : this.hasColor() ? this.color : '#000';
    }
  }, {
    key: 'format',
    get: function get() {
      if (this.options.format) {
        return this.options.format;
      }

      if (this.hasColor() && this.color.hasTransparency() && this.color.format.match(/^hex/)) {
        return this.options.enableHex8 ? 'hex8' : this.isAlphaEnabled() ? 'rgba' : 'hex';
      }

      if (this.hasColor()) {
        return this.color.format;
      }

      return null;
    }
  }]);

  return Colorpicker;
}();

exports.default = Colorpicker;

/***/ }),
/* 5 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _get = function get(object, property, receiver) { if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { return get(parent, property, receiver); } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } };

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _tinycolor2 = __webpack_require__(6);

var _tinycolor3 = _interopRequireDefault(_tinycolor2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

function unwrapColor(color) {
  if (color instanceof _tinycolor3.default) {
    return {
      r: color._r,
      g: color._g,
      b: color._b,
      a: color._a
    };
  }
  return color;
}

/**
 * Sanitizes a format string, so it is compatible with tinycolor,
 * or returns the same value if it is not a string.
 *
 * @param {String} format
 * @returns {String|*}
 * @private
 */
function getCompatibleFormat(format) {
  if (format instanceof String || typeof format === 'string') {
    return format.replace(/a$/gi, '');
  }

  return format;
}

/**
 * Color manipulation class that extends the tinycolor library class.
 */

var Color = function (_tinycolor) {
  _inherits(Color, _tinycolor);

  _createClass(Color, [{
    key: 'id',

    /**
     * Identifier of the color instance.
     *
     * @type {int}
     * @readonly
     */
    get: function get() {
      return this._tc_id;
    }

    /**
     * Format of the parsed color.
     *
     * @type {String}
     * @readonly
     */

  }, {
    key: 'format',
    get: function get() {
      return this._format;
    }

    /**
     * All options of the current instance.
     *
     * @type {{format: String, gradientType: String}}
     * @readonly
     */

  }, {
    key: 'options',
    get: function get() {
      return {
        format: this._format,
        gradientType: this._gradientType
      };
    }

    /**
     * @returns {{h, s, v, a}}
     */

  }, {
    key: 'hsva',
    get: function get() {
      return this.toHsv();
    }

    /**
     * @returns {{h, s, v, a}}
     */

  }, {
    key: 'hsvaRatio',
    get: function get() {
      var hsv = this.hsva;

      return {
        h: hsv.h / 360,
        s: hsv.s,
        v: hsv.v,
        a: hsv.a
      };
    }

    /**
     * foo bar
     * @param {Color|*} color
     * @param {{format}} [options]
     * @constructor
     */

  }]);

  function Color(color) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : { format: null };

    _classCallCheck(this, Color);

    if (options.format) {
      options.format = getCompatibleFormat(options.format);
    }

    /**
     * @type {Color|*}
     */
    var _this = _possibleConstructorReturn(this, (Color.__proto__ || Object.getPrototypeOf(Color)).call(this, unwrapColor(color), options));

    _this._originalInput = color; // keep real original color
    /**
     * Hue backup to not lose the information when saturation is 0.
     * @type {number}
     */
    _this._hbak = _this.hsva.h;
    /**
     * If set, it contains a reference to a previous color that caused the creation of this one.
     * @type {Color}
     */
    _this.previous = null;
    return _this;
  }

  /**
   * Compares a color object with this one and returns true if it is equal or false if not.
   *
   * @param {Color} color
   * @returns {boolean}
   */


  _createClass(Color, [{
    key: 'equals',
    value: function equals(color) {
      if (!(color instanceof _tinycolor3.default)) {
        return false;
      }
      return this._r === color._r && this._g === color._g && this._b === color._b && this._a === color._a && this._roundA === color._roundA && this._format === color._format && this._gradientType === color._gradientType && this._ok === color._ok;
    }

    /**
     * Imports all variables of the given color to this instance, excepting `_tc_id`.
     * @param {Color} color
     */

  }, {
    key: 'importColor',
    value: function importColor(color) {
      if (!(color instanceof _tinycolor3.default)) {
        throw new Error('Color.importColor: The color argument is not an instance of tinycolor.');
      }
      this._originalInput = color._originalInput;
      this._r = color._r;
      this._g = color._g;
      this._b = color._b;
      this._a = color._a;
      this._roundA = color._roundA;
      this._format = getCompatibleFormat(color._format);
      this._gradientType = color._gradientType;
      this._ok = color._ok;
      // omit .previous and ._tc_id import
    }

    /**
     * Imports the _r, _g, _b, _a, _hbak and _ok variables of the given color to this instance.
     * @param {Color} color
     */

  }, {
    key: 'importRgb',
    value: function importRgb(color) {
      if (!color instanceof Color) {
        throw new Error('Color.importColor: The color argument is not an instance of tinycolor.');
      }
      this._r = color._r;
      this._g = color._g;
      this._b = color._b;
      this._a = color._a;
      this._ok = color._ok;
      this._hbak = color._hbak;
    }

    /**
     * @param {{h,s,v,a}} hsv
     */

  }, {
    key: 'importHsv',
    value: function importHsv(hsv) {
      this._hbak = hsv.h;
      this.importRgb(new Color(hsv, this.options));
    }

    /**
     * @returns {Color}
     */

  }, {
    key: 'getCopy',
    value: function getCopy() {
      return new Color(this.hsva, this.options);
    }

    /**
     * @returns {Color}
     */

  }, {
    key: 'getHueOnlyCopy',
    value: function getHueOnlyCopy() {
      return new Color({ h: this._hbak ? this._hbak : this.hsva.h, s: 100, v: 100 }, this.options);
    }

    /**
     * @returns {Color}
     */

  }, {
    key: 'getOpaqueCopy',
    value: function getOpaqueCopy() {
      return new Color(Object.assign({}, this.hsva, { a: 1 }), this.options);
    }

    /**
     * @param {number} h Degrees from 0 to 360
     */

  }, {
    key: 'setHue',
    value: function setHue(h) {
      this.importHsv(Object.assign({}, this.hsva, { h: h }));
    }

    /**
     * @param {number} s Percent from 0 o 100
     */

  }, {
    key: 'setSaturation',
    value: function setSaturation(s) {
      this.importHsv(Object.assign({}, this.hsva, { s: s }));
    }

    /**
     * @param {number} v Percent from 0 o 100
     */

  }, {
    key: 'setBrightness',
    value: function setBrightness(v) {
      this.importHsv(Object.assign({}, this.hsva, { v: v }));
    }

    /**
     * @param {number} h Ratio from 0.0 to 1.0
     */

  }, {
    key: 'setHueRatio',
    value: function setHueRatio(h) {
      if (h === 0) {
        return;
      }
      this.setHue((1 - h) * 360);
    }

    /**
     * @param {number} s Ratio from 0.0 to 1.0
     */

  }, {
    key: 'setSaturationRatio',
    value: function setSaturationRatio(s) {
      this.setSaturation(s);
    }

    /**
     * @param {number} v Ratio from 0.0 to 1.0
     */

  }, {
    key: 'setBrightnessRatio',
    value: function setBrightnessRatio(v) {
      this.setBrightness(1 - v);
    }

    /**
     * @param {number} a Ratio from 0.0 to 1.0
     */

  }, {
    key: 'setAlphaRatio',
    value: function setAlphaRatio(a) {
      this.setAlpha(1 - a);
    }

    /**
     * @returns {boolean}
     */

  }, {
    key: 'isTransparent',
    value: function isTransparent() {
      return this._a === 0;
    }

    /**
     * @returns {boolean}
     */

  }, {
    key: 'hasTransparency',
    value: function hasTransparency() {
      return this._a !== 1;
    }

    /**
     * @param {string|null} [format] One of "rgb", "prgb", "hex"/"hex6", "hex3", "hex8", "hsl", "hsv"/"hsb", "name"
     * @returns {String}
     */

  }, {
    key: 'toString',
    value: function toString() {
      var format = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;

      format = format ? getCompatibleFormat(format) : this.format;

      var colorStr = _get(Color.prototype.__proto__ || Object.getPrototypeOf(Color.prototype), 'toString', this).call(this, format);

      if (colorStr && colorStr.match(/^#[0-9a-f]{3,8}$/i)) {
        // Support transparent for hex formats
        if (this.isTransparent() && this._r === 0 && this._g === 0 && this._b === 0) {
          return 'transparent';
        }
      }

      return colorStr;
    }
  }]);

  return Color;
}(_tinycolor3.default);

exports.default = Color;

/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_RESULT__;// TinyColor v1.4.1
// https://github.com/bgrins/TinyColor
// Brian Grinstead, MIT License

(function(Math) {

var trimLeft = /^\s+/,
    trimRight = /\s+$/,
    tinyCounter = 0,
    mathRound = Math.round,
    mathMin = Math.min,
    mathMax = Math.max,
    mathRandom = Math.random;

function tinycolor (color, opts) {

    color = (color) ? color : '';
    opts = opts || { };

    // If input is already a tinycolor, return itself
    if (color instanceof tinycolor) {
       return color;
    }
    // If we are called as a function, call using new instead
    if (!(this instanceof tinycolor)) {
        return new tinycolor(color, opts);
    }

    var rgb = inputToRGB(color);
    this._originalInput = color,
    this._r = rgb.r,
    this._g = rgb.g,
    this._b = rgb.b,
    this._a = rgb.a,
    this._roundA = mathRound(100*this._a) / 100,
    this._format = opts.format || rgb.format;
    this._gradientType = opts.gradientType;

    // Don't let the range of [0,255] come back in [0,1].
    // Potentially lose a little bit of precision here, but will fix issues where
    // .5 gets interpreted as half of the total, instead of half of 1
    // If it was supposed to be 128, this was already taken care of by `inputToRgb`
    if (this._r < 1) { this._r = mathRound(this._r); }
    if (this._g < 1) { this._g = mathRound(this._g); }
    if (this._b < 1) { this._b = mathRound(this._b); }

    this._ok = rgb.ok;
    this._tc_id = tinyCounter++;
}

tinycolor.prototype = {
    isDark: function() {
        return this.getBrightness() < 128;
    },
    isLight: function() {
        return !this.isDark();
    },
    isValid: function() {
        return this._ok;
    },
    getOriginalInput: function() {
      return this._originalInput;
    },
    getFormat: function() {
        return this._format;
    },
    getAlpha: function() {
        return this._a;
    },
    getBrightness: function() {
        //http://www.w3.org/TR/AERT#color-contrast
        var rgb = this.toRgb();
        return (rgb.r * 299 + rgb.g * 587 + rgb.b * 114) / 1000;
    },
    getLuminance: function() {
        //http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef
        var rgb = this.toRgb();
        var RsRGB, GsRGB, BsRGB, R, G, B;
        RsRGB = rgb.r/255;
        GsRGB = rgb.g/255;
        BsRGB = rgb.b/255;

        if (RsRGB <= 0.03928) {R = RsRGB / 12.92;} else {R = Math.pow(((RsRGB + 0.055) / 1.055), 2.4);}
        if (GsRGB <= 0.03928) {G = GsRGB / 12.92;} else {G = Math.pow(((GsRGB + 0.055) / 1.055), 2.4);}
        if (BsRGB <= 0.03928) {B = BsRGB / 12.92;} else {B = Math.pow(((BsRGB + 0.055) / 1.055), 2.4);}
        return (0.2126 * R) + (0.7152 * G) + (0.0722 * B);
    },
    setAlpha: function(value) {
        this._a = boundAlpha(value);
        this._roundA = mathRound(100*this._a) / 100;
        return this;
    },
    toHsv: function() {
        var hsv = rgbToHsv(this._r, this._g, this._b);
        return { h: hsv.h * 360, s: hsv.s, v: hsv.v, a: this._a };
    },
    toHsvString: function() {
        var hsv = rgbToHsv(this._r, this._g, this._b);
        var h = mathRound(hsv.h * 360), s = mathRound(hsv.s * 100), v = mathRound(hsv.v * 100);
        return (this._a == 1) ?
          "hsv("  + h + ", " + s + "%, " + v + "%)" :
          "hsva(" + h + ", " + s + "%, " + v + "%, "+ this._roundA + ")";
    },
    toHsl: function() {
        var hsl = rgbToHsl(this._r, this._g, this._b);
        return { h: hsl.h * 360, s: hsl.s, l: hsl.l, a: this._a };
    },
    toHslString: function() {
        var hsl = rgbToHsl(this._r, this._g, this._b);
        var h = mathRound(hsl.h * 360), s = mathRound(hsl.s * 100), l = mathRound(hsl.l * 100);
        return (this._a == 1) ?
          "hsl("  + h + ", " + s + "%, " + l + "%)" :
          "hsla(" + h + ", " + s + "%, " + l + "%, "+ this._roundA + ")";
    },
    toHex: function(allow3Char) {
        return rgbToHex(this._r, this._g, this._b, allow3Char);
    },
    toHexString: function(allow3Char) {
        return '#' + this.toHex(allow3Char);
    },
    toHex8: function(allow4Char) {
        return rgbaToHex(this._r, this._g, this._b, this._a, allow4Char);
    },
    toHex8String: function(allow4Char) {
        return '#' + this.toHex8(allow4Char);
    },
    toRgb: function() {
        return { r: mathRound(this._r), g: mathRound(this._g), b: mathRound(this._b), a: this._a };
    },
    toRgbString: function() {
        return (this._a == 1) ?
          "rgb("  + mathRound(this._r) + ", " + mathRound(this._g) + ", " + mathRound(this._b) + ")" :
          "rgba(" + mathRound(this._r) + ", " + mathRound(this._g) + ", " + mathRound(this._b) + ", " + this._roundA + ")";
    },
    toPercentageRgb: function() {
        return { r: mathRound(bound01(this._r, 255) * 100) + "%", g: mathRound(bound01(this._g, 255) * 100) + "%", b: mathRound(bound01(this._b, 255) * 100) + "%", a: this._a };
    },
    toPercentageRgbString: function() {
        return (this._a == 1) ?
          "rgb("  + mathRound(bound01(this._r, 255) * 100) + "%, " + mathRound(bound01(this._g, 255) * 100) + "%, " + mathRound(bound01(this._b, 255) * 100) + "%)" :
          "rgba(" + mathRound(bound01(this._r, 255) * 100) + "%, " + mathRound(bound01(this._g, 255) * 100) + "%, " + mathRound(bound01(this._b, 255) * 100) + "%, " + this._roundA + ")";
    },
    toName: function() {
        if (this._a === 0) {
            return "transparent";
        }

        if (this._a < 1) {
            return false;
        }

        return hexNames[rgbToHex(this._r, this._g, this._b, true)] || false;
    },
    toFilter: function(secondColor) {
        var hex8String = '#' + rgbaToArgbHex(this._r, this._g, this._b, this._a);
        var secondHex8String = hex8String;
        var gradientType = this._gradientType ? "GradientType = 1, " : "";

        if (secondColor) {
            var s = tinycolor(secondColor);
            secondHex8String = '#' + rgbaToArgbHex(s._r, s._g, s._b, s._a);
        }

        return "progid:DXImageTransform.Microsoft.gradient("+gradientType+"startColorstr="+hex8String+",endColorstr="+secondHex8String+")";
    },
    toString: function(format) {
        var formatSet = !!format;
        format = format || this._format;

        var formattedString = false;
        var hasAlpha = this._a < 1 && this._a >= 0;
        var needsAlphaFormat = !formatSet && hasAlpha && (format === "hex" || format === "hex6" || format === "hex3" || format === "hex4" || format === "hex8" || format === "name");

        if (needsAlphaFormat) {
            // Special case for "transparent", all other non-alpha formats
            // will return rgba when there is transparency.
            if (format === "name" && this._a === 0) {
                return this.toName();
            }
            return this.toRgbString();
        }
        if (format === "rgb") {
            formattedString = this.toRgbString();
        }
        if (format === "prgb") {
            formattedString = this.toPercentageRgbString();
        }
        if (format === "hex" || format === "hex6") {
            formattedString = this.toHexString();
        }
        if (format === "hex3") {
            formattedString = this.toHexString(true);
        }
        if (format === "hex4") {
            formattedString = this.toHex8String(true);
        }
        if (format === "hex8") {
            formattedString = this.toHex8String();
        }
        if (format === "name") {
            formattedString = this.toName();
        }
        if (format === "hsl") {
            formattedString = this.toHslString();
        }
        if (format === "hsv") {
            formattedString = this.toHsvString();
        }

        return formattedString || this.toHexString();
    },
    clone: function() {
        return tinycolor(this.toString());
    },

    _applyModification: function(fn, args) {
        var color = fn.apply(null, [this].concat([].slice.call(args)));
        this._r = color._r;
        this._g = color._g;
        this._b = color._b;
        this.setAlpha(color._a);
        return this;
    },
    lighten: function() {
        return this._applyModification(lighten, arguments);
    },
    brighten: function() {
        return this._applyModification(brighten, arguments);
    },
    darken: function() {
        return this._applyModification(darken, arguments);
    },
    desaturate: function() {
        return this._applyModification(desaturate, arguments);
    },
    saturate: function() {
        return this._applyModification(saturate, arguments);
    },
    greyscale: function() {
        return this._applyModification(greyscale, arguments);
    },
    spin: function() {
        return this._applyModification(spin, arguments);
    },

    _applyCombination: function(fn, args) {
        return fn.apply(null, [this].concat([].slice.call(args)));
    },
    analogous: function() {
        return this._applyCombination(analogous, arguments);
    },
    complement: function() {
        return this._applyCombination(complement, arguments);
    },
    monochromatic: function() {
        return this._applyCombination(monochromatic, arguments);
    },
    splitcomplement: function() {
        return this._applyCombination(splitcomplement, arguments);
    },
    triad: function() {
        return this._applyCombination(triad, arguments);
    },
    tetrad: function() {
        return this._applyCombination(tetrad, arguments);
    }
};

// If input is an object, force 1 into "1.0" to handle ratios properly
// String input requires "1.0" as input, so 1 will be treated as 1
tinycolor.fromRatio = function(color, opts) {
    if (typeof color == "object") {
        var newColor = {};
        for (var i in color) {
            if (color.hasOwnProperty(i)) {
                if (i === "a") {
                    newColor[i] = color[i];
                }
                else {
                    newColor[i] = convertToPercentage(color[i]);
                }
            }
        }
        color = newColor;
    }

    return tinycolor(color, opts);
};

// Given a string or object, convert that input to RGB
// Possible string inputs:
//
//     "red"
//     "#f00" or "f00"
//     "#ff0000" or "ff0000"
//     "#ff000000" or "ff000000"
//     "rgb 255 0 0" or "rgb (255, 0, 0)"
//     "rgb 1.0 0 0" or "rgb (1, 0, 0)"
//     "rgba (255, 0, 0, 1)" or "rgba 255, 0, 0, 1"
//     "rgba (1.0, 0, 0, 1)" or "rgba 1.0, 0, 0, 1"
//     "hsl(0, 100%, 50%)" or "hsl 0 100% 50%"
//     "hsla(0, 100%, 50%, 1)" or "hsla 0 100% 50%, 1"
//     "hsv(0, 100%, 100%)" or "hsv 0 100% 100%"
//
function inputToRGB(color) {

    var rgb = { r: 0, g: 0, b: 0 };
    var a = 1;
    var s = null;
    var v = null;
    var l = null;
    var ok = false;
    var format = false;

    if (typeof color == "string") {
        color = stringInputToObject(color);
    }

    if (typeof color == "object") {
        if (isValidCSSUnit(color.r) && isValidCSSUnit(color.g) && isValidCSSUnit(color.b)) {
            rgb = rgbToRgb(color.r, color.g, color.b);
            ok = true;
            format = String(color.r).substr(-1) === "%" ? "prgb" : "rgb";
        }
        else if (isValidCSSUnit(color.h) && isValidCSSUnit(color.s) && isValidCSSUnit(color.v)) {
            s = convertToPercentage(color.s);
            v = convertToPercentage(color.v);
            rgb = hsvToRgb(color.h, s, v);
            ok = true;
            format = "hsv";
        }
        else if (isValidCSSUnit(color.h) && isValidCSSUnit(color.s) && isValidCSSUnit(color.l)) {
            s = convertToPercentage(color.s);
            l = convertToPercentage(color.l);
            rgb = hslToRgb(color.h, s, l);
            ok = true;
            format = "hsl";
        }

        if (color.hasOwnProperty("a")) {
            a = color.a;
        }
    }

    a = boundAlpha(a);

    return {
        ok: ok,
        format: color.format || format,
        r: mathMin(255, mathMax(rgb.r, 0)),
        g: mathMin(255, mathMax(rgb.g, 0)),
        b: mathMin(255, mathMax(rgb.b, 0)),
        a: a
    };
}


// Conversion Functions
// --------------------

// `rgbToHsl`, `rgbToHsv`, `hslToRgb`, `hsvToRgb` modified from:
// <http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript>

// `rgbToRgb`
// Handle bounds / percentage checking to conform to CSS color spec
// <http://www.w3.org/TR/css3-color/>
// *Assumes:* r, g, b in [0, 255] or [0, 1]
// *Returns:* { r, g, b } in [0, 255]
function rgbToRgb(r, g, b){
    return {
        r: bound01(r, 255) * 255,
        g: bound01(g, 255) * 255,
        b: bound01(b, 255) * 255
    };
}

// `rgbToHsl`
// Converts an RGB color value to HSL.
// *Assumes:* r, g, and b are contained in [0, 255] or [0, 1]
// *Returns:* { h, s, l } in [0,1]
function rgbToHsl(r, g, b) {

    r = bound01(r, 255);
    g = bound01(g, 255);
    b = bound01(b, 255);

    var max = mathMax(r, g, b), min = mathMin(r, g, b);
    var h, s, l = (max + min) / 2;

    if(max == min) {
        h = s = 0; // achromatic
    }
    else {
        var d = max - min;
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch(max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }

        h /= 6;
    }

    return { h: h, s: s, l: l };
}

// `hslToRgb`
// Converts an HSL color value to RGB.
// *Assumes:* h is contained in [0, 1] or [0, 360] and s and l are contained [0, 1] or [0, 100]
// *Returns:* { r, g, b } in the set [0, 255]
function hslToRgb(h, s, l) {
    var r, g, b;

    h = bound01(h, 360);
    s = bound01(s, 100);
    l = bound01(l, 100);

    function hue2rgb(p, q, t) {
        if(t < 0) t += 1;
        if(t > 1) t -= 1;
        if(t < 1/6) return p + (q - p) * 6 * t;
        if(t < 1/2) return q;
        if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
        return p;
    }

    if(s === 0) {
        r = g = b = l; // achromatic
    }
    else {
        var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        var p = 2 * l - q;
        r = hue2rgb(p, q, h + 1/3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1/3);
    }

    return { r: r * 255, g: g * 255, b: b * 255 };
}

// `rgbToHsv`
// Converts an RGB color value to HSV
// *Assumes:* r, g, and b are contained in the set [0, 255] or [0, 1]
// *Returns:* { h, s, v } in [0,1]
function rgbToHsv(r, g, b) {

    r = bound01(r, 255);
    g = bound01(g, 255);
    b = bound01(b, 255);

    var max = mathMax(r, g, b), min = mathMin(r, g, b);
    var h, s, v = max;

    var d = max - min;
    s = max === 0 ? 0 : d / max;

    if(max == min) {
        h = 0; // achromatic
    }
    else {
        switch(max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h /= 6;
    }
    return { h: h, s: s, v: v };
}

// `hsvToRgb`
// Converts an HSV color value to RGB.
// *Assumes:* h is contained in [0, 1] or [0, 360] and s and v are contained in [0, 1] or [0, 100]
// *Returns:* { r, g, b } in the set [0, 255]
 function hsvToRgb(h, s, v) {

    h = bound01(h, 360) * 6;
    s = bound01(s, 100);
    v = bound01(v, 100);

    var i = Math.floor(h),
        f = h - i,
        p = v * (1 - s),
        q = v * (1 - f * s),
        t = v * (1 - (1 - f) * s),
        mod = i % 6,
        r = [v, q, p, p, t, v][mod],
        g = [t, v, v, q, p, p][mod],
        b = [p, p, t, v, v, q][mod];

    return { r: r * 255, g: g * 255, b: b * 255 };
}

// `rgbToHex`
// Converts an RGB color to hex
// Assumes r, g, and b are contained in the set [0, 255]
// Returns a 3 or 6 character hex
function rgbToHex(r, g, b, allow3Char) {

    var hex = [
        pad2(mathRound(r).toString(16)),
        pad2(mathRound(g).toString(16)),
        pad2(mathRound(b).toString(16))
    ];

    // Return a 3 character hex if possible
    if (allow3Char && hex[0].charAt(0) == hex[0].charAt(1) && hex[1].charAt(0) == hex[1].charAt(1) && hex[2].charAt(0) == hex[2].charAt(1)) {
        return hex[0].charAt(0) + hex[1].charAt(0) + hex[2].charAt(0);
    }

    return hex.join("");
}

// `rgbaToHex`
// Converts an RGBA color plus alpha transparency to hex
// Assumes r, g, b are contained in the set [0, 255] and
// a in [0, 1]. Returns a 4 or 8 character rgba hex
function rgbaToHex(r, g, b, a, allow4Char) {

    var hex = [
        pad2(mathRound(r).toString(16)),
        pad2(mathRound(g).toString(16)),
        pad2(mathRound(b).toString(16)),
        pad2(convertDecimalToHex(a))
    ];

    // Return a 4 character hex if possible
    if (allow4Char && hex[0].charAt(0) == hex[0].charAt(1) && hex[1].charAt(0) == hex[1].charAt(1) && hex[2].charAt(0) == hex[2].charAt(1) && hex[3].charAt(0) == hex[3].charAt(1)) {
        return hex[0].charAt(0) + hex[1].charAt(0) + hex[2].charAt(0) + hex[3].charAt(0);
    }

    return hex.join("");
}

// `rgbaToArgbHex`
// Converts an RGBA color to an ARGB Hex8 string
// Rarely used, but required for "toFilter()"
function rgbaToArgbHex(r, g, b, a) {

    var hex = [
        pad2(convertDecimalToHex(a)),
        pad2(mathRound(r).toString(16)),
        pad2(mathRound(g).toString(16)),
        pad2(mathRound(b).toString(16))
    ];

    return hex.join("");
}

// `equals`
// Can be called with any tinycolor input
tinycolor.equals = function (color1, color2) {
    if (!color1 || !color2) { return false; }
    return tinycolor(color1).toRgbString() == tinycolor(color2).toRgbString();
};

tinycolor.random = function() {
    return tinycolor.fromRatio({
        r: mathRandom(),
        g: mathRandom(),
        b: mathRandom()
    });
};


// Modification Functions
// ----------------------
// Thanks to less.js for some of the basics here
// <https://github.com/cloudhead/less.js/blob/master/lib/less/functions.js>

function desaturate(color, amount) {
    amount = (amount === 0) ? 0 : (amount || 10);
    var hsl = tinycolor(color).toHsl();
    hsl.s -= amount / 100;
    hsl.s = clamp01(hsl.s);
    return tinycolor(hsl);
}

function saturate(color, amount) {
    amount = (amount === 0) ? 0 : (amount || 10);
    var hsl = tinycolor(color).toHsl();
    hsl.s += amount / 100;
    hsl.s = clamp01(hsl.s);
    return tinycolor(hsl);
}

function greyscale(color) {
    return tinycolor(color).desaturate(100);
}

function lighten (color, amount) {
    amount = (amount === 0) ? 0 : (amount || 10);
    var hsl = tinycolor(color).toHsl();
    hsl.l += amount / 100;
    hsl.l = clamp01(hsl.l);
    return tinycolor(hsl);
}

function brighten(color, amount) {
    amount = (amount === 0) ? 0 : (amount || 10);
    var rgb = tinycolor(color).toRgb();
    rgb.r = mathMax(0, mathMin(255, rgb.r - mathRound(255 * - (amount / 100))));
    rgb.g = mathMax(0, mathMin(255, rgb.g - mathRound(255 * - (amount / 100))));
    rgb.b = mathMax(0, mathMin(255, rgb.b - mathRound(255 * - (amount / 100))));
    return tinycolor(rgb);
}

function darken (color, amount) {
    amount = (amount === 0) ? 0 : (amount || 10);
    var hsl = tinycolor(color).toHsl();
    hsl.l -= amount / 100;
    hsl.l = clamp01(hsl.l);
    return tinycolor(hsl);
}

// Spin takes a positive or negative amount within [-360, 360] indicating the change of hue.
// Values outside of this range will be wrapped into this range.
function spin(color, amount) {
    var hsl = tinycolor(color).toHsl();
    var hue = (hsl.h + amount) % 360;
    hsl.h = hue < 0 ? 360 + hue : hue;
    return tinycolor(hsl);
}

// Combination Functions
// ---------------------
// Thanks to jQuery xColor for some of the ideas behind these
// <https://github.com/infusion/jQuery-xcolor/blob/master/jquery.xcolor.js>

function complement(color) {
    var hsl = tinycolor(color).toHsl();
    hsl.h = (hsl.h + 180) % 360;
    return tinycolor(hsl);
}

function triad(color) {
    var hsl = tinycolor(color).toHsl();
    var h = hsl.h;
    return [
        tinycolor(color),
        tinycolor({ h: (h + 120) % 360, s: hsl.s, l: hsl.l }),
        tinycolor({ h: (h + 240) % 360, s: hsl.s, l: hsl.l })
    ];
}

function tetrad(color) {
    var hsl = tinycolor(color).toHsl();
    var h = hsl.h;
    return [
        tinycolor(color),
        tinycolor({ h: (h + 90) % 360, s: hsl.s, l: hsl.l }),
        tinycolor({ h: (h + 180) % 360, s: hsl.s, l: hsl.l }),
        tinycolor({ h: (h + 270) % 360, s: hsl.s, l: hsl.l })
    ];
}

function splitcomplement(color) {
    var hsl = tinycolor(color).toHsl();
    var h = hsl.h;
    return [
        tinycolor(color),
        tinycolor({ h: (h + 72) % 360, s: hsl.s, l: hsl.l}),
        tinycolor({ h: (h + 216) % 360, s: hsl.s, l: hsl.l})
    ];
}

function analogous(color, results, slices) {
    results = results || 6;
    slices = slices || 30;

    var hsl = tinycolor(color).toHsl();
    var part = 360 / slices;
    var ret = [tinycolor(color)];

    for (hsl.h = ((hsl.h - (part * results >> 1)) + 720) % 360; --results; ) {
        hsl.h = (hsl.h + part) % 360;
        ret.push(tinycolor(hsl));
    }
    return ret;
}

function monochromatic(color, results) {
    results = results || 6;
    var hsv = tinycolor(color).toHsv();
    var h = hsv.h, s = hsv.s, v = hsv.v;
    var ret = [];
    var modification = 1 / results;

    while (results--) {
        ret.push(tinycolor({ h: h, s: s, v: v}));
        v = (v + modification) % 1;
    }

    return ret;
}

// Utility Functions
// ---------------------

tinycolor.mix = function(color1, color2, amount) {
    amount = (amount === 0) ? 0 : (amount || 50);

    var rgb1 = tinycolor(color1).toRgb();
    var rgb2 = tinycolor(color2).toRgb();

    var p = amount / 100;

    var rgba = {
        r: ((rgb2.r - rgb1.r) * p) + rgb1.r,
        g: ((rgb2.g - rgb1.g) * p) + rgb1.g,
        b: ((rgb2.b - rgb1.b) * p) + rgb1.b,
        a: ((rgb2.a - rgb1.a) * p) + rgb1.a
    };

    return tinycolor(rgba);
};


// Readability Functions
// ---------------------
// <http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef (WCAG Version 2)

// `contrast`
// Analyze the 2 colors and returns the color contrast defined by (WCAG Version 2)
tinycolor.readability = function(color1, color2) {
    var c1 = tinycolor(color1);
    var c2 = tinycolor(color2);
    return (Math.max(c1.getLuminance(),c2.getLuminance())+0.05) / (Math.min(c1.getLuminance(),c2.getLuminance())+0.05);
};

// `isReadable`
// Ensure that foreground and background color combinations meet WCAG2 guidelines.
// The third argument is an optional Object.
//      the 'level' property states 'AA' or 'AAA' - if missing or invalid, it defaults to 'AA';
//      the 'size' property states 'large' or 'small' - if missing or invalid, it defaults to 'small'.
// If the entire object is absent, isReadable defaults to {level:"AA",size:"small"}.

// *Example*
//    tinycolor.isReadable("#000", "#111") => false
//    tinycolor.isReadable("#000", "#111",{level:"AA",size:"large"}) => false
tinycolor.isReadable = function(color1, color2, wcag2) {
    var readability = tinycolor.readability(color1, color2);
    var wcag2Parms, out;

    out = false;

    wcag2Parms = validateWCAG2Parms(wcag2);
    switch (wcag2Parms.level + wcag2Parms.size) {
        case "AAsmall":
        case "AAAlarge":
            out = readability >= 4.5;
            break;
        case "AAlarge":
            out = readability >= 3;
            break;
        case "AAAsmall":
            out = readability >= 7;
            break;
    }
    return out;

};

// `mostReadable`
// Given a base color and a list of possible foreground or background
// colors for that base, returns the most readable color.
// Optionally returns Black or White if the most readable color is unreadable.
// *Example*
//    tinycolor.mostReadable(tinycolor.mostReadable("#123", ["#124", "#125"],{includeFallbackColors:false}).toHexString(); // "#112255"
//    tinycolor.mostReadable(tinycolor.mostReadable("#123", ["#124", "#125"],{includeFallbackColors:true}).toHexString();  // "#ffffff"
//    tinycolor.mostReadable("#a8015a", ["#faf3f3"],{includeFallbackColors:true,level:"AAA",size:"large"}).toHexString(); // "#faf3f3"
//    tinycolor.mostReadable("#a8015a", ["#faf3f3"],{includeFallbackColors:true,level:"AAA",size:"small"}).toHexString(); // "#ffffff"
tinycolor.mostReadable = function(baseColor, colorList, args) {
    var bestColor = null;
    var bestScore = 0;
    var readability;
    var includeFallbackColors, level, size ;
    args = args || {};
    includeFallbackColors = args.includeFallbackColors ;
    level = args.level;
    size = args.size;

    for (var i= 0; i < colorList.length ; i++) {
        readability = tinycolor.readability(baseColor, colorList[i]);
        if (readability > bestScore) {
            bestScore = readability;
            bestColor = tinycolor(colorList[i]);
        }
    }

    if (tinycolor.isReadable(baseColor, bestColor, {"level":level,"size":size}) || !includeFallbackColors) {
        return bestColor;
    }
    else {
        args.includeFallbackColors=false;
        return tinycolor.mostReadable(baseColor,["#fff", "#000"],args);
    }
};


// Big List of Colors
// ------------------
// <http://www.w3.org/TR/css3-color/#svg-color>
var names = tinycolor.names = {
    aliceblue: "f0f8ff",
    antiquewhite: "faebd7",
    aqua: "0ff",
    aquamarine: "7fffd4",
    azure: "f0ffff",
    beige: "f5f5dc",
    bisque: "ffe4c4",
    black: "000",
    blanchedalmond: "ffebcd",
    blue: "00f",
    blueviolet: "8a2be2",
    brown: "a52a2a",
    burlywood: "deb887",
    burntsienna: "ea7e5d",
    cadetblue: "5f9ea0",
    chartreuse: "7fff00",
    chocolate: "d2691e",
    coral: "ff7f50",
    cornflowerblue: "6495ed",
    cornsilk: "fff8dc",
    crimson: "dc143c",
    cyan: "0ff",
    darkblue: "00008b",
    darkcyan: "008b8b",
    darkgoldenrod: "b8860b",
    darkgray: "a9a9a9",
    darkgreen: "006400",
    darkgrey: "a9a9a9",
    darkkhaki: "bdb76b",
    darkmagenta: "8b008b",
    darkolivegreen: "556b2f",
    darkorange: "ff8c00",
    darkorchid: "9932cc",
    darkred: "8b0000",
    darksalmon: "e9967a",
    darkseagreen: "8fbc8f",
    darkslateblue: "483d8b",
    darkslategray: "2f4f4f",
    darkslategrey: "2f4f4f",
    darkturquoise: "00ced1",
    darkviolet: "9400d3",
    deeppink: "ff1493",
    deepskyblue: "00bfff",
    dimgray: "696969",
    dimgrey: "696969",
    dodgerblue: "1e90ff",
    firebrick: "b22222",
    floralwhite: "fffaf0",
    forestgreen: "228b22",
    fuchsia: "f0f",
    gainsboro: "dcdcdc",
    ghostwhite: "f8f8ff",
    gold: "ffd700",
    goldenrod: "daa520",
    gray: "808080",
    green: "008000",
    greenyellow: "adff2f",
    grey: "808080",
    honeydew: "f0fff0",
    hotpink: "ff69b4",
    indianred: "cd5c5c",
    indigo: "4b0082",
    ivory: "fffff0",
    khaki: "f0e68c",
    lavender: "e6e6fa",
    lavenderblush: "fff0f5",
    lawngreen: "7cfc00",
    lemonchiffon: "fffacd",
    lightblue: "add8e6",
    lightcoral: "f08080",
    lightcyan: "e0ffff",
    lightgoldenrodyellow: "fafad2",
    lightgray: "d3d3d3",
    lightgreen: "90ee90",
    lightgrey: "d3d3d3",
    lightpink: "ffb6c1",
    lightsalmon: "ffa07a",
    lightseagreen: "20b2aa",
    lightskyblue: "87cefa",
    lightslategray: "789",
    lightslategrey: "789",
    lightsteelblue: "b0c4de",
    lightyellow: "ffffe0",
    lime: "0f0",
    limegreen: "32cd32",
    linen: "faf0e6",
    magenta: "f0f",
    maroon: "800000",
    mediumaquamarine: "66cdaa",
    mediumblue: "0000cd",
    mediumorchid: "ba55d3",
    mediumpurple: "9370db",
    mediumseagreen: "3cb371",
    mediumslateblue: "7b68ee",
    mediumspringgreen: "00fa9a",
    mediumturquoise: "48d1cc",
    mediumvioletred: "c71585",
    midnightblue: "191970",
    mintcream: "f5fffa",
    mistyrose: "ffe4e1",
    moccasin: "ffe4b5",
    navajowhite: "ffdead",
    navy: "000080",
    oldlace: "fdf5e6",
    olive: "808000",
    olivedrab: "6b8e23",
    orange: "ffa500",
    orangered: "ff4500",
    orchid: "da70d6",
    palegoldenrod: "eee8aa",
    palegreen: "98fb98",
    paleturquoise: "afeeee",
    palevioletred: "db7093",
    papayawhip: "ffefd5",
    peachpuff: "ffdab9",
    peru: "cd853f",
    pink: "ffc0cb",
    plum: "dda0dd",
    powderblue: "b0e0e6",
    purple: "800080",
    rebeccapurple: "663399",
    red: "f00",
    rosybrown: "bc8f8f",
    royalblue: "4169e1",
    saddlebrown: "8b4513",
    salmon: "fa8072",
    sandybrown: "f4a460",
    seagreen: "2e8b57",
    seashell: "fff5ee",
    sienna: "a0522d",
    silver: "c0c0c0",
    skyblue: "87ceeb",
    slateblue: "6a5acd",
    slategray: "708090",
    slategrey: "708090",
    snow: "fffafa",
    springgreen: "00ff7f",
    steelblue: "4682b4",
    tan: "d2b48c",
    teal: "008080",
    thistle: "d8bfd8",
    tomato: "ff6347",
    turquoise: "40e0d0",
    violet: "ee82ee",
    wheat: "f5deb3",
    white: "fff",
    whitesmoke: "f5f5f5",
    yellow: "ff0",
    yellowgreen: "9acd32"
};

// Make it easy to access colors via `hexNames[hex]`
var hexNames = tinycolor.hexNames = flip(names);


// Utilities
// ---------

// `{ 'name1': 'val1' }` becomes `{ 'val1': 'name1' }`
function flip(o) {
    var flipped = { };
    for (var i in o) {
        if (o.hasOwnProperty(i)) {
            flipped[o[i]] = i;
        }
    }
    return flipped;
}

// Return a valid alpha value [0,1] with all invalid values being set to 1
function boundAlpha(a) {
    a = parseFloat(a);

    if (isNaN(a) || a < 0 || a > 1) {
        a = 1;
    }

    return a;
}

// Take input from [0, n] and return it as [0, 1]
function bound01(n, max) {
    if (isOnePointZero(n)) { n = "100%"; }

    var processPercent = isPercentage(n);
    n = mathMin(max, mathMax(0, parseFloat(n)));

    // Automatically convert percentage into number
    if (processPercent) {
        n = parseInt(n * max, 10) / 100;
    }

    // Handle floating point rounding errors
    if ((Math.abs(n - max) < 0.000001)) {
        return 1;
    }

    // Convert into [0, 1] range if it isn't already
    return (n % max) / parseFloat(max);
}

// Force a number between 0 and 1
function clamp01(val) {
    return mathMin(1, mathMax(0, val));
}

// Parse a base-16 hex value into a base-10 integer
function parseIntFromHex(val) {
    return parseInt(val, 16);
}

// Need to handle 1.0 as 100%, since once it is a number, there is no difference between it and 1
// <http://stackoverflow.com/questions/7422072/javascript-how-to-detect-number-as-a-decimal-including-1-0>
function isOnePointZero(n) {
    return typeof n == "string" && n.indexOf('.') != -1 && parseFloat(n) === 1;
}

// Check to see if string passed in is a percentage
function isPercentage(n) {
    return typeof n === "string" && n.indexOf('%') != -1;
}

// Force a hex value to have 2 characters
function pad2(c) {
    return c.length == 1 ? '0' + c : '' + c;
}

// Replace a decimal with it's percentage value
function convertToPercentage(n) {
    if (n <= 1) {
        n = (n * 100) + "%";
    }

    return n;
}

// Converts a decimal to a hex value
function convertDecimalToHex(d) {
    return Math.round(parseFloat(d) * 255).toString(16);
}
// Converts a hex value to a decimal
function convertHexToDecimal(h) {
    return (parseIntFromHex(h) / 255);
}

var matchers = (function() {

    // <http://www.w3.org/TR/css3-values/#integers>
    var CSS_INTEGER = "[-\\+]?\\d+%?";

    // <http://www.w3.org/TR/css3-values/#number-value>
    var CSS_NUMBER = "[-\\+]?\\d*\\.\\d+%?";

    // Allow positive/negative integer/number.  Don't capture the either/or, just the entire outcome.
    var CSS_UNIT = "(?:" + CSS_NUMBER + ")|(?:" + CSS_INTEGER + ")";

    // Actual matching.
    // Parentheses and commas are optional, but not required.
    // Whitespace can take the place of commas or opening paren
    var PERMISSIVE_MATCH3 = "[\\s|\\(]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")\\s*\\)?";
    var PERMISSIVE_MATCH4 = "[\\s|\\(]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")[,|\\s]+(" + CSS_UNIT + ")\\s*\\)?";

    return {
        CSS_UNIT: new RegExp(CSS_UNIT),
        rgb: new RegExp("rgb" + PERMISSIVE_MATCH3),
        rgba: new RegExp("rgba" + PERMISSIVE_MATCH4),
        hsl: new RegExp("hsl" + PERMISSIVE_MATCH3),
        hsla: new RegExp("hsla" + PERMISSIVE_MATCH4),
        hsv: new RegExp("hsv" + PERMISSIVE_MATCH3),
        hsva: new RegExp("hsva" + PERMISSIVE_MATCH4),
        hex3: /^#?([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})$/,
        hex6: /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/,
        hex4: /^#?([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})([0-9a-fA-F]{1})$/,
        hex8: /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/
    };
})();

// `isValidCSSUnit`
// Take in a single string / number and check to see if it looks like a CSS unit
// (see `matchers` above for definition).
function isValidCSSUnit(color) {
    return !!matchers.CSS_UNIT.exec(color);
}

// `stringInputToObject`
// Permissive string parsing.  Take in a number of formats, and output an object
// based on detected format.  Returns `{ r, g, b }` or `{ h, s, l }` or `{ h, s, v}`
function stringInputToObject(color) {

    color = color.replace(trimLeft,'').replace(trimRight, '').toLowerCase();
    var named = false;
    if (names[color]) {
        color = names[color];
        named = true;
    }
    else if (color == 'transparent') {
        return { r: 0, g: 0, b: 0, a: 0, format: "name" };
    }

    // Try to match string input using regular expressions.
    // Keep most of the number bounding out of this function - don't worry about [0,1] or [0,100] or [0,360]
    // Just return an object and let the conversion functions handle that.
    // This way the result will be the same whether the tinycolor is initialized with string or object.
    var match;
    if ((match = matchers.rgb.exec(color))) {
        return { r: match[1], g: match[2], b: match[3] };
    }
    if ((match = matchers.rgba.exec(color))) {
        return { r: match[1], g: match[2], b: match[3], a: match[4] };
    }
    if ((match = matchers.hsl.exec(color))) {
        return { h: match[1], s: match[2], l: match[3] };
    }
    if ((match = matchers.hsla.exec(color))) {
        return { h: match[1], s: match[2], l: match[3], a: match[4] };
    }
    if ((match = matchers.hsv.exec(color))) {
        return { h: match[1], s: match[2], v: match[3] };
    }
    if ((match = matchers.hsva.exec(color))) {
        return { h: match[1], s: match[2], v: match[3], a: match[4] };
    }
    if ((match = matchers.hex8.exec(color))) {
        return {
            r: parseIntFromHex(match[1]),
            g: parseIntFromHex(match[2]),
            b: parseIntFromHex(match[3]),
            a: convertHexToDecimal(match[4]),
            format: named ? "name" : "hex8"
        };
    }
    if ((match = matchers.hex6.exec(color))) {
        return {
            r: parseIntFromHex(match[1]),
            g: parseIntFromHex(match[2]),
            b: parseIntFromHex(match[3]),
            format: named ? "name" : "hex"
        };
    }
    if ((match = matchers.hex4.exec(color))) {
        return {
            r: parseIntFromHex(match[1] + '' + match[1]),
            g: parseIntFromHex(match[2] + '' + match[2]),
            b: parseIntFromHex(match[3] + '' + match[3]),
            a: convertHexToDecimal(match[4] + '' + match[4]),
            format: named ? "name" : "hex8"
        };
    }
    if ((match = matchers.hex3.exec(color))) {
        return {
            r: parseIntFromHex(match[1] + '' + match[1]),
            g: parseIntFromHex(match[2] + '' + match[2]),
            b: parseIntFromHex(match[3] + '' + match[3]),
            format: named ? "name" : "hex"
        };
    }

    return false;
}

function validateWCAG2Parms(parms) {
    // return valid WCAG2 parms for isReadable.
    // If input parms are invalid, return {"level":"AA", "size":"small"}
    var level, size;
    parms = parms || {"level":"AA", "size":"small"};
    level = (parms.level || "AA").toUpperCase();
    size = (parms.size || "small").toLowerCase();
    if (level !== "AA" && level !== "AAA") {
        level = "AA";
    }
    if (size !== "small" && size !== "large") {
        size = "small";
    }
    return {"level":level, "size":size};
}

// Node: Export function
if (typeof module !== "undefined" && module.exports) {
    module.exports = tinycolor;
}
// AMD/requirejs: Define the module
else if (true) {
    !(__WEBPACK_AMD_DEFINE_RESULT__ = function () {return tinycolor;}.call(exports, __webpack_require__, exports, module),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
}
// Browser: Expose to window
else {
    window.tinycolor = tinycolor;
}

})(Math);


/***/ }),
/* 7 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

/**
 * @module
 */

/**
 * Colorpicker default options
 */

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = {
  /**
   * If true, loads the Debugger extension automatically into the current instance
   * @type {boolean}
   * @default false
   */
  debug: false,
  /**
   * Forces a color, ignoring the one from the elements value or data-color attribute.
   *
   * @type {(String|Color|boolean)}
   * @default false
   */
  color: false,
  /**
   * Forces an specific color format. If false, it will be automatically detected the first time,
   * but if null it will be always recalculated.
   *
   * Note that the ending 'a' of the format meaning "alpha" has currently no effect, meaning that rgb is the same as
   * rgba excepting if the alpha channel is disabled (see useAlpha).
   *
   * @type {('rgb'|'rgba'|'prgb'|'prgba'|'hex'|'hex3'|'hex6'|'hex8'|'hsl'|'hsla'|'hsv'|'hsva'|'name'|boolean)}
   * @default false
   */
  format: false,
  /**
   * Horizontal mode layout.
   *
   * If true, the hue and alpha channel bars will be rendered horizontally, above the saturation selector.
   *
   * @type {boolean}
   * @default false
   */
  horizontal: false,
  /**
   * Forces to show the colorpicker as an inline element
   *
   * @type {boolean}
   * @default false
   */
  inline: false,
  /**
   * Children input CSS selector
   *
   * @type {String}
   * @default 'input'
   */
  input: 'input',
  /**
   * Colorpicker container CSS selector. If given, the colorpicker will be placed inside this container.
   * If true, the colorpicker element itself will be used as the container.
   *
   * @type {String|boolean}
   * @default false
   */
  container: false, // container selector
  /**
   * Children color component CSS selector.
   * If it exists, the child <i> element background will be changed on color change.
   *
   * @type {String|boolean}
   * @default '.add-on, .input-group-addon'
   */
  component: '.add-on, .input-group-addon',
  /**
   * Fallback color to use when the given color is invalid.
   * If false, the latest valid color will be used as a fallback.
   *
   * @type {String|Color|boolean}
   * @default false
   */
  fallbackColor: false,
  /**
   * If enabled, the input content will be replaced always with a valid color,
   * if not enabled the invalid color will be left in the input, but valid in the internal color object.
   *
   * @type {boolean}
   * @default false
   */
  autoInputFallback: false,
  /**
   * If true a hash will be prepended to hexadecimal colors.
   * If false, the hash will be removed.
   * This only affects the input values.
   *
   * @type {boolean}
   * @default false
   */
  useHashPrefix: true,
  /**
   * If true or false the alpha adjustment bar will be displayed no matter what.
   * If false it will be always hidden and alpha channel won't be allowed programmatically for this instance,
   * so the selected or typed color will be always opaque.
   *
   * @type {boolean}
   * @default true
   */
  useAlpha: true,
  /**
   * This only applies when the color format is hexadecimal.
   * If true, the alpha channel will be allowed for hexadecimal formatted colors, making them having 4 or 8 chars
   * (RGBA or RRGGBBAA). This format is not yet supported in all modern browsers, so it is disabled by default.
   * If false, rgba will be used whenever there is an alpha change different than 1 and the color format is
   * automatic.
   *
   * @type {boolean}
   * @default true
   */
  enableHex8: false,
  /**
   * Vertical sliders configuration
   * @type {Object}
   */
  sliders: {
    saturation: {
      maxLeft: 100,
      maxTop: 100,
      callLeft: 'setSaturationRatio',
      callTop: 'setBrightnessRatio'
    },
    hue: {
      maxLeft: 0,
      maxTop: 100,
      callLeft: false,
      callTop: 'setHueRatio'
    },
    alpha: {
      maxLeft: 0,
      maxTop: 100,
      callLeft: false,
      callTop: 'setAlphaRatio'
    }
  },
  /**
   * Horizontal sliders configuration
   * @type {Object}
   */
  slidersHorz: {
    saturation: {
      maxLeft: 100,
      maxTop: 100,
      callLeft: 'setSaturationRatio',
      callTop: 'setBrightnessRatio'
    },
    hue: {
      maxLeft: 100,
      maxTop: 0,
      callLeft: 'setHueRatio',
      callTop: false
    },
    alpha: {
      maxLeft: 100,
      maxTop: 0,
      callLeft: 'setAlphaRatio',
      callTop: false
    }
  },
  /**
   * Colorpicker popup alignment.
   * For now only right is supported.
   *
   * @type {('right')}
   * @default 'right'
   */ // TODO: add 'left' and 'auto' support.
  align: 'right',
  /**
   * Custom class to be added to the colorpicker element
   *
   * @type {String}
   */
  customClass: null,
  /**
   * Colorpicker widget template
   * @type {String}
   * @example
   * <!-- This is the default template: -->
   * <div class="colorpicker">
   *   <div class="colorpicker-saturation"><i class="colorpicker-guide"><i></i></i></div>
   *   <div class="colorpicker-hue"><i class="colorpicker-guide"></i></div>
   *   <div class="colorpicker-alpha"><i class="colorpicker-guide"></i></div>
   *   <div class="colorpicker-color"><div /></div>
   * </div>
   */
  template: '<div class="colorpicker">\n    <div class="colorpicker-saturation"><i class="colorpicker-guide"><i /></div>\n    <div class="colorpicker-hue"><i class="colorpicker-guide"></i></div>\n    <div class="colorpicker-alpha"><i class="colorpicker-guide"></i></div></div>',
  /**
   *
   * Associative object with the extension class name and its config.
   * Colorpicker comes with many bundled extensions: debugger, palette, preview and swatches (a superset of Palette).
   *
   * @type {Object}
   * @example
   *   extensions: [
   *     {
   *       name: 'swatches'
   *       colors: {
   *         'primary': '#337ab7',
   *         'success': '#5cb85c',
   *         'info': '#5bc0de',
   *         'warning': '#f0ad4e',
   *         'danger': '#d9534f'
   *       },
   *       namesAsValues: true
   *     }
   *   ]
   */
  extensions: [{
    name: 'preview',
    showText: false
  }]
};

/***/ }),
/* 8 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Palette = exports.Swatches = exports.Preview = exports.Debugger = undefined;

var _Debugger = __webpack_require__(9);

var _Debugger2 = _interopRequireDefault(_Debugger);

var _Preview = __webpack_require__(10);

var _Preview2 = _interopRequireDefault(_Preview);

var _Swatches = __webpack_require__(11);

var _Swatches2 = _interopRequireDefault(_Swatches);

var _Palette = __webpack_require__(2);

var _Palette2 = _interopRequireDefault(_Palette);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.Debugger = _Debugger2.default;
exports.Preview = _Preview2.default;
exports.Swatches = _Swatches2.default;
exports.Palette = _Palette2.default;
exports.default = {
  'debugger': _Debugger2.default,
  'preview': _Preview2.default,
  'swatches': _Swatches2.default,
  'palette': _Palette2.default
};

/***/ }),
/* 9 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _get = function get(object, property, receiver) { if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { return get(parent, property, receiver); } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } };

var _Extension2 = __webpack_require__(1);

var _Extension3 = _interopRequireDefault(_Extension2);

var _jquery = __webpack_require__(0);

var _jquery2 = _interopRequireDefault(_jquery);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Debugger = function (_Extension) {
  _inherits(Debugger, _Extension);

  function Debugger(colorpicker) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    _classCallCheck(this, Debugger);

    /**
     * @type {number}
     */
    var _this = _possibleConstructorReturn(this, (Debugger.__proto__ || Object.getPrototypeOf(Debugger)).call(this, colorpicker, options));

    _this.eventCounter = 0;
    if (_this.colorpicker.hasInput()) {
      _this.colorpicker.input.on('change.colorpicker-ext', _jquery2.default.proxy(_this.onChangeInput, _this));
    }
    return _this;
  }

  /**
   * @fires colorpickerDebug
   * @param {string} eventName
   * @param {*} args
   */


  _createClass(Debugger, [{
    key: 'log',
    value: function log(eventName) {
      var _console;

      for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
        args[_key - 1] = arguments[_key];
      }

      this.eventCounter += 1;

      var logMessage = '#' + this.eventCounter + ': Colorpicker#' + this.colorpicker.id + ' [' + eventName + ']';

      (_console = console).debug.apply(_console, [logMessage].concat(args));

      /**
       * (Colorpicker) Fired by the ConsoleDebug extension whenever it logs something
       *
       * @event colorpickerDebug
       */
      this.colorpicker.element.trigger({
        type: 'colorpickerDebug',
        colorpicker: this.colorpicker,
        color: this.color,
        debug: {
          debugger: this,
          eventName: eventName,
          logArgs: args,
          logMessage: logMessage
        }
      });
    }
  }, {
    key: 'resolveColor',
    value: function resolveColor(color) {
      this.log('resolveColor()', color);
      return false;
    }
  }, {
    key: 'onCreate',
    value: function onCreate(event) {
      this.log('colorpickerCreate');
      return _get(Debugger.prototype.__proto__ || Object.getPrototypeOf(Debugger.prototype), 'onCreate', this).call(this, event);
    }
  }, {
    key: 'onDestroy',
    value: function onDestroy(event) {
      this.log('colorpickerDestroy');
      this.eventCounter = 0;

      if (this.colorpicker.hasInput()) {
        this.colorpicker.input.off('.colorpicker-ext');
      }

      return _get(Debugger.prototype.__proto__ || Object.getPrototypeOf(Debugger.prototype), 'onDestroy', this).call(this, event);
    }
  }, {
    key: 'onUpdate',
    value: function onUpdate(event) {
      this.log('colorpickerUpdate');
    }

    /**
     * @listens _change
     * @param {Event} event
     */

  }, {
    key: 'onChangeInput',
    value: function onChangeInput(event) {
      this.log('input:change.colorpicker', event.value, event.color);
    }
  }, {
    key: 'onChange',
    value: function onChange(event) {
      this.log('colorpickerChange', event.value, event.color);
    }
  }, {
    key: 'onInvalid',
    value: function onInvalid(event) {
      this.log('colorpickerInvalid', event.value, event.color);
    }
  }, {
    key: 'onHide',
    value: function onHide(event) {
      this.log('colorpickerHide');
      this.eventCounter = 0;
    }
  }, {
    key: 'onShow',
    value: function onShow(event) {
      this.log('colorpickerShow');
    }
  }, {
    key: 'onDisable',
    value: function onDisable(event) {
      this.log('colorpickerDisable');
    }
  }, {
    key: 'onEnable',
    value: function onEnable(event) {
      this.log('colorpickerEnable');
    }
  }]);

  return Debugger;
}(_Extension3.default);

exports.default = Debugger;

/***/ }),
/* 10 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _get = function get(object, property, receiver) { if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { return get(parent, property, receiver); } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } };

var _Extension2 = __webpack_require__(1);

var _Extension3 = _interopRequireDefault(_Extension2);

var _jquery = __webpack_require__(0);

var _jquery2 = _interopRequireDefault(_jquery);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Preview = function (_Extension) {
  _inherits(Preview, _Extension);

  function Preview(colorpicker) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    _classCallCheck(this, Preview);

    var _this = _possibleConstructorReturn(this, (Preview.__proto__ || Object.getPrototypeOf(Preview)).call(this, colorpicker, Object.assign({}, {
      template: '<div class="colorpicker-bar colorpicker-preview"><div /></div>',
      showText: true,
      format: colorpicker.format
    }, options)));

    _this.element = (0, _jquery2.default)(_this.options.template);
    _this.elementInner = _this.element.find('div');
    return _this;
  }

  _createClass(Preview, [{
    key: 'onCreate',
    value: function onCreate(event) {
      _get(Preview.prototype.__proto__ || Object.getPrototypeOf(Preview.prototype), 'onCreate', this).call(this, event);
      this.colorpicker.picker.append(this.element);
    }
  }, {
    key: 'onUpdate',
    value: function onUpdate(event) {
      _get(Preview.prototype.__proto__ || Object.getPrototypeOf(Preview.prototype), 'onUpdate', this).call(this, event);

      this.elementInner.css('backgroundColor', event.color.toRgbString());

      if (this.options.showText) {
        this.elementInner.html(event.color.toString(this.options.format || this.colorpicker.format));

        if (event.color.isDark()) {
          this.elementInner.css('color', 'white');
        } else {
          this.elementInner.css('color', 'black');
        }
      }
    }
  }]);

  return Preview;
}(_Extension3.default);

exports.default = Preview;

/***/ }),
/* 11 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _get = function get(object, property, receiver) { if (object === null) object = Function.prototype; var desc = Object.getOwnPropertyDescriptor(object, property); if (desc === undefined) { var parent = Object.getPrototypeOf(object); if (parent === null) { return undefined; } else { return get(parent, property, receiver); } } else if ("value" in desc) { return desc.value; } else { var getter = desc.get; if (getter === undefined) { return undefined; } return getter.call(receiver); } };

var _Palette2 = __webpack_require__(2);

var _Palette3 = _interopRequireDefault(_Palette2);

var _jquery = __webpack_require__(0);

var _jquery2 = _interopRequireDefault(_jquery);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var defaults = {
  barTemplate: '<div class="colorpicker-bar colorpicker-swatches"></div>',
  swatchTemplate: '<i class="colorpicker-swatch"></i>'
};

var Swatches = function (_Palette) {
  _inherits(Swatches, _Palette);

  function Swatches(colorpicker) {
    var options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    _classCallCheck(this, Swatches);

    return _possibleConstructorReturn(this, (Swatches.__proto__ || Object.getPrototypeOf(Swatches)).call(this, colorpicker, Object.assign({}, defaults, options)));
  }

  _createClass(Swatches, [{
    key: 'isEnabled',
    value: function isEnabled() {
      return this.getLength() > 0;
    }
  }, {
    key: 'onCreate',
    value: function onCreate(event) {
      var _this2 = this;

      _get(Swatches.prototype.__proto__ || Object.getPrototypeOf(Swatches.prototype), 'onCreate', this).call(this, event);

      if (!this.isEnabled()) {
        return;
      }

      var colorpicker = this.colorpicker,
          swatchContainer = (0, _jquery2.default)(this.options.barTemplate),
          isAliased = this.options.namesAsValues === true && !Array.isArray(this.colors);

      _jquery2.default.each(this.colors, function (name, value) {
        var $swatch = (0, _jquery2.default)(_this2.options.swatchTemplate).css('background-color', value).attr('data-name', name).attr('data-value', value).attr('title', name + ': ' + value);

        $swatch.on('mousedown.colorpicker touchstart.colorpicker', function (e) {
          e.preventDefault();
          colorpicker.setValue(isAliased ? (0, _jquery2.default)(this).data('name') : (0, _jquery2.default)(this).data('value'));
        });
        swatchContainer.append($swatch);
      });

      colorpicker.picker.append(swatchContainer);
    }
  }]);

  return Swatches;
}(_Palette3.default);

exports.default = Swatches;

/***/ })
/******/ ]);
});
//# sourceMappingURL=bootstrap-colorpicker.js.map
/*!
 * Link dialog plugin for Editor.md
 *
 * @file        link-dialog.js
 * @author      pandao
 * @version     1.2.0
 * @updateTime  2015-03-07
 * {@link       https://github.com/pandao/editor.md}
 * @license     MIT
 */

(function() {

    var factory = function (exports) {

		var $            = jQuery;           // if using module loader(Require.js/Sea.js).

		var langs = {
			"zh-cn" : {
				toolbar : {
					table : "表格"
				},
				dialog : {
					table : {
						title      : "添加表格",
						cellsLabel : "单元格数",
						alignLabel : "对齐方式",
						rows       : "行数",
						cols       : "列数",
						aligns     : ["默认", "左对齐", "居中对齐", "右对齐"]
					}
				}
			},
			"zh-tw" : {
				toolbar : {
					table : "添加表格"
				},
				dialog : {
					table : {
						title      : "添加表格",
						cellsLabel : "單元格數",
						alignLabel : "對齊方式",
						rows       : "行數",
						cols       : "列數",
						aligns     : ["默認", "左對齊", "居中對齊", "右對齊"]
					}
				}
			},
			"en" : {
				toolbar : {
					table : "Tables"
				},
				dialog : {
					table : {
						title      : "Tables",
						cellsLabel : "Cells",
						alignLabel : "Align",
						rows       : "Rows",
						cols       : "Cols",
						aligns     : ["Default", "Left align", "Center align", "Right align"]
					}
				}
			}
		};

		exports.fn.htmlEntities = function() {
			/*
			var _this       = this; // this == the current instance object of Editor.md
			var lang        = _this.lang;
			var settings    = _this.settings;
			var editor      = this.editor;
			var cursor      = cm.getCursor();
			var selection   = cm.getSelection();
			var classPrefix = this.classPrefix;

			$.extend(true, this.lang, langs[this.lang.name]); // l18n
			this.setToolbar();

			cm.focus();
			*/
			//....
		};

	};
    
	// CommonJS/Node.js
	if (typeof require === "function" && typeof exports === "object" && typeof module === "object")
    { 
        module.exports = factory;
    }
	else if (typeof define === "function")  // AMD/CMD/Sea.js
    {
		if (define.amd) { // for Require.js

			define(["editormd"], function(editormd) {
                factory(editormd);
            });

		} else { // for Sea.js
			define(function(require) {
                var editormd = require("./../../editormd");
                factory(editormd);
            });
		}
	} 
	else
	{
        factory(window.editormd);
	}

})();

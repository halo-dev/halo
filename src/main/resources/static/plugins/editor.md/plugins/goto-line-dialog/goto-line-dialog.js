/*!
 * Goto line dialog plugin for Editor.md
 *
 * @file        goto-line-dialog.js
 * @author      pandao
 * @version     1.2.1
 * @updateTime  2015-06-09
 * {@link       https://github.com/pandao/editor.md}
 * @license     MIT
 */

(function() {

	var factory = function (exports) {

		var $            = jQuery;
		var pluginName   = "goto-line-dialog";

		var langs = {
			"zh-cn" : {
				toolbar : {
					"goto-line" : "跳转到行"
				},
				dialog : {
					"goto-line" : {
						title  : "跳转到行",
						label  : "请输入行号",
						error  : "错误："
					}
				}
			},
			"zh-tw" : {
				toolbar : {
					"goto-line" : "跳轉到行"
				},
				dialog : {
					"goto-line" : {
						title  : "跳轉到行",
						label  : "請輸入行號",
						error  : "錯誤："
					}
				}
			},
			"en" : {
				toolbar : {
					"goto-line" : "Goto line"
				},
				dialog : {
					"goto-line" : {
						title  : "Goto line",
						label  : "Enter a line number, range ",
						error  : "Error: "
					}
				}
			}
		};

		exports.fn.gotoLineDialog = function() {
			var _this       = this;
			var cm          = this.cm;
			var editor      = this.editor;
			var settings    = this.settings;
			var path        = settings.pluginPath + pluginName +"/";
			var classPrefix = this.classPrefix;
			var dialogName  = classPrefix + pluginName, dialog;

			$.extend(true, this.lang, langs[this.lang.name]);
			this.setToolbar();

			var lang        = this.lang;
			var dialogLang  = lang.dialog["goto-line"];
			var lineCount   = cm.lineCount();

			dialogLang.error += dialogLang.label + " 1-" + lineCount;

			if (editor.find("." + dialogName).length < 1) 
			{			
				var dialogContent = [
					"<div class=\"editormd-form\" style=\"padding: 10px 0;\">",
					"<p style=\"margin: 0;\">" + dialogLang.label + " 1-" + lineCount +"&nbsp;&nbsp;&nbsp;<input type=\"number\" class=\"number-input\" style=\"width: 60px;\" value=\"1\" max=\"" + lineCount + "\" min=\"1\" data-line-number /></p>",
					"</div>"
				].join("\n");

				dialog = this.createDialog({
					name       : dialogName,
					title      : dialogLang.title,
					width      : 400,
					height     : 180,
					mask       : settings.dialogShowMask,
					drag       : settings.dialogDraggable,
					content    : dialogContent,
					lockScreen : settings.dialogLockScreen,
					maskStyle  : {
						opacity         : settings.dialogMaskOpacity,
						backgroundColor : settings.dialogMaskBgColor
					},
					buttons    : {
                        enter : [lang.buttons.enter, function() {
							var line   = parseInt(this.find("[data-line-number]").val());

							if (line < 1 || line > lineCount) {
								alert(dialogLang.error);

								return false;
							}

							_this.gotoLine(line);

                            this.hide().lockScreen(false).hideMask();

                            return false;
                        }],

                        cancel : [lang.buttons.cancel, function() {                                   
                            this.hide().lockScreen(false).hideMask();

                            return false;
                        }]
					}
				});
			}

			dialog = editor.find("." + dialogName);

			this.dialogShowMask(dialog);
			this.dialogLockScreen();
			dialog.show();
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

/*!
 * Table dialog plugin for Editor.md
 *
 * @file        table-dialog.js
 * @author      pandao
 * @version     1.2.1
 * @updateTime  2015-06-09
 * {@link       https://github.com/pandao/editor.md}
 * @license     MIT
 */

(function() {

	var factory = function (exports) {

		var $            = jQuery;
		var pluginName   = "table-dialog";

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

		exports.fn.tableDialog = function() {
			var _this       = this;
			var cm          = this.cm;
			var editor      = this.editor;
			var settings    = this.settings;
			var path        = settings.path + "../plugins/" + pluginName +"/";
			var classPrefix = this.classPrefix;
			var dialogName  = classPrefix + pluginName, dialog;

			$.extend(true, this.lang, langs[this.lang.name]);
			this.setToolbar();

			var lang        = this.lang;
			var dialogLang  = lang.dialog.table;
			
			var dialogContent = [
				"<div class=\"editormd-form\" style=\"padding: 13px 0;\">",
				"<label>" + dialogLang.cellsLabel + "</label>",
				dialogLang.rows + " <input type=\"number\" value=\"3\" class=\"number-input\" style=\"width:40px;\" max=\"100\" min=\"2\" data-rows />&nbsp;&nbsp;",
				dialogLang.cols + " <input type=\"number\" value=\"2\" class=\"number-input\" style=\"width:40px;\" max=\"100\" min=\"1\" data-cols /><br/>",
				"<label>" + dialogLang.alignLabel + "</label>",
				"<div class=\"fa-btns\"></div>",
				"</div>"
			].join("\n");

			if (editor.find("." + dialogName).length > 0) 
			{
                dialog = editor.find("." + dialogName);

				this.dialogShowMask(dialog);
				this.dialogLockScreen();
				dialog.show();
			} 
			else
			{
				dialog = this.createDialog({
					name       : dialogName,
					title      : dialogLang.title,
					width      : 360,
					height     : 226,
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
							var rows   = parseInt(this.find("[data-rows]").val());
							var cols   = parseInt(this.find("[data-cols]").val());
							var align  = this.find("[name=\"table-align\"]:checked").val();
							var table  = "";
							var hrLine = "------------";

							var alignSign = {
								_default : hrLine,
								left     : ":" + hrLine,
								center   : ":" + hrLine + ":",
								right    : hrLine + ":"
							};

							if ( rows > 1 && cols > 0) 
							{
								for (var r = 0, len = rows; r < len; r++) 
								{
									var row = [];
									var head = [];

									for (var c = 0, len2 = cols; c < len2; c++) 
									{
										if (r === 1) {
											head.push(alignSign[align]);
										}

										row.push(" ");
									}

									if (r === 1) {
										table += "| " + head.join(" | ") + " |" + "\n";
									}
									
									table += "| " + row.join( (cols === 1) ? "" : " | " ) + " |" + "\n";
								}
							}

							cm.replaceSelection(table);

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

			var faBtns = dialog.find(".fa-btns");

			if (faBtns.html() === "")
			{
				var icons  = ["align-justify", "align-left", "align-center", "align-right"];
				var _lang  = dialogLang.aligns;
				var values = ["_default", "left", "center", "right"];

				for (var i = 0, len = icons.length; i < len; i++) 
				{
					var checked = (i === 0) ? " checked=\"checked\"" : "";
					var btn = "<a href=\"javascript:;\"><label for=\"editormd-table-dialog-radio"+i+"\" title=\"" + _lang[i] + "\">";
					btn += "<input type=\"radio\" name=\"table-align\" id=\"editormd-table-dialog-radio"+i+"\" value=\"" + values[i] + "\"" +checked + " />&nbsp;";
					btn += "<i class=\"fa fa-" + icons[i] + "\"></i>";
					btn += "</label></a>";

					faBtns.append(btn);
				}
			}
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

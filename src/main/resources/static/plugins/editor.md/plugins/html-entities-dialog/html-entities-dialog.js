/*!
 * HTML entities dialog plugin for Editor.md
 *
 * @file        html-entities-dialog.js
 * @author      pandao
 * @version     1.2.0
 * @updateTime  2015-03-08
 * {@link       https://github.com/pandao/editor.md}
 * @license     MIT
 */

(function() {

	var factory = function (exports) {

		var $            = jQuery;
		var pluginName   = "html-entities-dialog";
		var selecteds    = [];
		var entitiesData = [];

		exports.fn.htmlEntitiesDialog = function() {
			var _this       = this;
			var cm          = this.cm;
			var lang        = _this.lang;
			var settings    = _this.settings;
			var path        = settings.pluginPath + pluginName + "/";
			var editor      = this.editor;
			var cursor      = cm.getCursor();
			var selection   = cm.getSelection();
			var classPrefix = _this.classPrefix;

			var dialogName  = classPrefix + "dialog-" + pluginName, dialog;
			var dialogLang  = lang.dialog.htmlEntities;

			var dialogContent = [
				'<div class="' + classPrefix + 'html-entities-box" style=\"width: 760px;height: 334px;margin-bottom: 8px;overflow: hidden;overflow-y: auto;\">',
				'<div class="' + classPrefix + 'grid-table">',
				'</div>',
				'</div>',
			].join("\r\n");

			cm.focus();

			if (editor.find("." + dialogName).length > 0) 
			{
                dialog = editor.find("." + dialogName);

				selecteds = [];
				dialog.find("a").removeClass("selected");

				this.dialogShowMask(dialog);
				this.dialogLockScreen();
				dialog.show();
			} 
			else
			{
				dialog = this.createDialog({
					name       : dialogName,
					title      : dialogLang.title,
					width      : 800,
					height     : 475,
					mask       : settings.dialogShowMask,
					drag       : settings.dialogDraggable,
					content    : dialogContent,
					lockScreen : settings.dialogLockScreen,
					maskStyle  : {
						opacity         : settings.dialogMaskOpacity,
						backgroundColor : settings.dialogMaskBgColor
					},
					buttons    : {
						enter  : [lang.buttons.enter, function() {							
							cm.replaceSelection(selecteds.join(" "));
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
				
			var table = dialog.find("." + classPrefix + "grid-table");

			var drawTable = function() {

				if (entitiesData.length < 1) return ;

				var rowNumber = 20;
				var pageTotal = Math.ceil(entitiesData.length / rowNumber);

				table.html("");
				
				for (var i = 0; i < pageTotal; i++)
				{
					var row = "<div class=\"" + classPrefix + "grid-table-row\">";
					
					for (var x = 0; x < rowNumber; x++)
					{
						var entity = entitiesData[(i * rowNumber) + x];
						
						if (typeof entity !== "undefined")
						{
							var name = entity.name.replace("&amp;", "&");

							row += "<a href=\"javascript:;\" value=\"" + entity.name + "\" title=\"" + name + "\" class=\"" + classPrefix + "html-entity-btn\">" + name + "</a>";
						}
					}
					
					row += "</div>";
					
					table.append(row);
				}

				dialog.find("." + classPrefix + "html-entity-btn").bind(exports.mouseOrTouch("click", "touchend"), function() {
					$(this).toggleClass("selected");

					if ($(this).hasClass("selected")) 
					{
						selecteds.push($(this).attr("value"));
					}
				});
			};
			
			if (entitiesData.length < 1) 
			{            
				if (typeof (dialog.loading) == "function") dialog.loading(true);

				$.getJSON(path + pluginName.replace("-dialog", "") + ".json", function(json) {

					if (typeof (dialog.loading) == "function") dialog.loading(false);

					entitiesData = json;
					drawTable();
				});
			}
			else
			{		
				drawTable();
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

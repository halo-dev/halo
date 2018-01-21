/*!
 * Reference link dialog plugin for Editor.md
 *
 * @file        reference-link-dialog.js
 * @author      pandao
 * @version     1.2.1
 * @updateTime  2015-06-09
 * {@link       https://github.com/pandao/editor.md}
 * @license     MIT
 */

(function() {

    var factory = function (exports) {

		var pluginName   = "reference-link-dialog";
		var ReLinkId     = 1;

		exports.fn.referenceLinkDialog = function() {

            var _this       = this;
            var cm          = this.cm;
            var lang        = this.lang;
			var editor      = this.editor;
            var settings    = this.settings;
            var cursor      = cm.getCursor();
            var selection   = cm.getSelection();
            var dialogLang  = lang.dialog.referenceLink;
            var classPrefix = this.classPrefix;
			var dialogName  = classPrefix + pluginName, dialog;

			cm.focus();

            if (editor.find("." + dialogName).length < 1)
            {      
                var dialogHTML = "<div class=\"" + classPrefix + "form\">" +
                                        "<label>" + dialogLang.name + "</label>" +
                                        "<input type=\"text\" value=\"[" + ReLinkId + "]\" data-name />" +  
                                        "<br/>" +
                                        "<label>" + dialogLang.urlId + "</label>" +
                                        "<input type=\"text\" data-url-id />" +
                                        "<br/>" +
                                        "<label>" + dialogLang.url + "</label>" +
                                        "<input type=\"text\" value=\"http://\" data-url />" + 
                                        "<br/>" +
                                        "<label>" + dialogLang.urlTitle + "</label>" +
                                        "<input type=\"text\" value=\"" + selection + "\" data-title />" +
                                        "<br/>" +
                                    "</div>";

                dialog = this.createDialog({   
                    name       : dialogName,
                    title      : dialogLang.title,
                    width      : 380,
                    height     : 296,
                    content    : dialogHTML,
                    mask       : settings.dialogShowMask,
                    drag       : settings.dialogDraggable,
                    lockScreen : settings.dialogLockScreen,
                    maskStyle  : {
                        opacity         : settings.dialogMaskOpacity,
                        backgroundColor : settings.dialogMaskBgColor
                    },
                    buttons : {
                        enter  : [lang.buttons.enter, function() {
                            var name  = this.find("[data-name]").val();
                            var url   = this.find("[data-url]").val();
                            var rid   = this.find("[data-url-id]").val();
                            var title = this.find("[data-title]").val();

                            if (name === "")
                            {
                                alert(dialogLang.nameEmpty);
                                return false;
                            }

                            if (rid === "")
                            {
                                alert(dialogLang.idEmpty);
                                return false;
                            }

                            if (url === "http://" || url === "")
                            {
                                alert(dialogLang.urlEmpty);
                                return false;
                            }

                            //cm.replaceSelection("[" + title + "][" + name + "]\n[" + name + "]: " + url + "");
                            cm.replaceSelection("[" + name + "][" + rid + "]");

                            if (selection === "") {
                                cm.setCursor(cursor.line, cursor.ch + 1);
                            }

							title = (title === "") ? "" : " \"" + title + "\"";

							cm.setValue(cm.getValue() + "\n[" + rid + "]: " + url + title + "");

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
			dialog.find("[data-name]").val("[" + ReLinkId + "]");
			dialog.find("[data-url-id]").val("");
			dialog.find("[data-url]").val("http://");
			dialog.find("[data-title]").val(selection);

			this.dialogShowMask(dialog);
			this.dialogLockScreen();
			dialog.show();

			ReLinkId++;
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

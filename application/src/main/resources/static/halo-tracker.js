(function() {
    'use strict';
    
    (function(window) {
        var screen = window.screen;
        var width = screen.width;
        var height = screen.height;
        var language = window.navigator.language;
        var location = window.location;
        var localStorage = window.localStorage;
        var document = window.document;
        var history = window.history;
        var hostname = location.hostname;
        var pathname = location.pathname;
        var search = location.search;
        var currentScript = document.currentScript;
        
        if (!currentScript) return;
        
        // 预览检测函数
        var isPreviewMode = function() {
            // 检测预览路径
            if (pathname.startsWith('/preview/')) {
                return true;
            }
            
            // 检测主题预览参数
            if (search.includes('preview-theme=')) {
                return true;
            }
            
            // 检测其他预览标识
            if (localStorage && localStorage.getItem('halo.preview.mode') === 'true') {
                return true;
            }
            
            return false;
        };
        
        // 如果是预览模式，直接返回不执行统计
        if (isPreviewMode()) {
            console.debug('Preview mode detected, skipping visit tracking');
            return;
        }
        
        var wrap = function(obj, method, handler) {
            var original = obj[method];
            return function() {
                var args = [];
                for (var i = arguments.length; i--;) args[i] = arguments[i];
                handler.apply(null, args);
                return original.apply(obj, args);
            };
        };
        
        var isDisabled = function() {
            return (localStorage && localStorage.getItem("haloTracker.disabled")) ||
                   (doNotTrack && (function() {
                       var dnt = window.doNotTrack;
                       var navigator = window.navigator;
                       var external = window.external;
                       var prop = "msTrackingProtectionEnabled";
                       var value = dnt || navigator.doNotTrack || navigator.msDoNotTrack || 
                                  (external && prop in external && external[prop]());
                       return value == "1" || value === "yes";
                   })()) ||
                   (domains && !allowedDomains.includes(hostname));
        };
        
        var prefix = "data-";
        var getAttribute = currentScript.getAttribute.bind(currentScript);
        var group = getAttribute(prefix + "group") || "";
        var plural = getAttribute(prefix + "plural");
        var name = getAttribute(prefix + "name");
        var hostUrl = getAttribute(prefix + "host-url");
        var autoTrack = getAttribute(prefix + "auto-track") !== "false";
        var doNotTrack = getAttribute(prefix + "do-not-track");
        var domains = getAttribute(prefix + "domains") || "";
        var allowedDomains = domains.split(",").map(function(domain) {
            return domain.trim();
        });
        
        var endpoint = (hostUrl ? hostUrl.replace(/\/$/, "") : currentScript.src.split("/").slice(0, -1).join("/")) + 
                       "/apis/api.halo.run/v1alpha1/trackers/counter";
        var screenSize = width + "x" + height;
        var url = pathname + search;
        var referrer = document.referrer;
        
        var trackView = function(customUrl, customReferrer) {
            if (customUrl === undefined) customUrl = url;
            if (customReferrer === undefined) customReferrer = referrer;
            
            return function(data) {
                if (!isDisabled()) {
                    return fetch(endpoint, {
                        method: "POST",
                        body: JSON.stringify(Object.assign({}, data)),
                        headers: {
                            "Content-Type": "application/json"
                        }
                    }).then(function(response) {
                        return response.text();
                    }).then(function(result) {
                        console.debug("Visit count:", result);
                    });
                }
            }({
                group: group,
                plural: plural,
                name: name,
                hostname: hostname,
                screen: screenSize,
                language: language,
                url: customUrl,
                referrer: customReferrer
            });
        };
        
        var handlePushState = function(originalMethod, state, title, url) {
            if (url) {
                referrer = location.pathname + location.search;
                var newUrl = url.toString();
                var newPath = newUrl.substring(0, 4) === "http" ? 
                             "/" + newUrl.split("/").splice(3).join("/") : newUrl;
                if (newPath !== referrer) {
                    trackView();
                }
            }
        };
        
        if (!window.haloTracker) {
            var trackEvent = function(event) {
                return trackEvent(event);
            };
            trackEvent.trackView = trackView;
            window.haloTracker = trackEvent;
        }
        
        if (autoTrack && !isDisabled()) {
            history.pushState = wrap(history, "pushState", handlePushState);
            history.replaceState = wrap(history, "replaceState", handlePushState);
            
            var track = function() {
                if (document.readyState === "complete") {
                    trackView();
                }
            };
            
            document.addEventListener("readystatechange", track, true);
            track();
        }
        
    })(window);
})();

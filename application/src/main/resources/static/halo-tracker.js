(function() {
    'use strict';
    
    (function(window) {
        const screen = window.screen;
        const width = screen.width;
        const height = screen.height;
        const language = window.navigator.language;
        const location = window.location;
        const localStorage = window.localStorage;
        const document = window.document;
        const history = window.history;
        const hostname = location.hostname;
        const pathname = location.pathname;
        const search = location.search;
        const currentScript = document.currentScript;
        
        if (!currentScript) return;
        
        // 预览检测函数
        const isPreviewMode = () => {
            // 检测预览路径
            if (pathname.startsWith('/preview/')) {
                return true;
            }
            
            // 检测主题预览参数
            if (search.includes('preview-theme=')) {
                return true;
            }
            
            // 检测其他预览标识
            return !!(localStorage && localStorage.getItem('halo.preview.mode') === 'true');
        };
        
        // 如果是预览模式，直接返回不执行统计
        if (isPreviewMode()) {
            console.debug('Preview mode detected, skipping visit tracking');
            return;
        }
        
        const wrap = (obj, method, handler) => {
            const original = obj[method];
            return (...args) => {
                handler(...args);
                return original.apply(obj, args);
            };
        };
        
        const isDisabled = () => {
            const checkDoNotTrack = () => {
                const dnt = window.doNotTrack;
                const navigator = window.navigator;
                const external = window.external;
                const prop = "msTrackingProtectionEnabled";
                const value = dnt || navigator.doNotTrack || navigator.msDoNotTrack || 
                           (external && prop in external && external[prop]());
                return value == "1" || value === "yes";
            };
            
            return !!(localStorage && localStorage.getItem("haloTracker.disabled")) ||
                   (doNotTrack && checkDoNotTrack()) ||
                   (domains && !allowedDomains.includes(hostname));
        };
        
        const prefix = "data-";
        const getAttribute = currentScript.getAttribute.bind(currentScript);
        const group = getAttribute(prefix + "group") || "";
        const plural = getAttribute(prefix + "plural");
        const name = getAttribute(prefix + "name");
        const hostUrl = getAttribute(prefix + "host-url");
        const autoTrack = getAttribute(prefix + "auto-track") !== "false";
        const doNotTrack = getAttribute(prefix + "do-not-track");
        const domains = getAttribute(prefix + "domains") || "";
        const allowedDomains = domains.split(",").map(domain => domain.trim());
        
        const endpoint = `${hostUrl ? hostUrl.replace(/\/$/, "") : currentScript.src.split("/").slice(0, -1).join("/")}` +
                       "/apis/api.halo.run/v1alpha1/trackers/counter";
        const screenSize = `${width}x${height}`;
        const url = pathname + search;
        let referrer = document.referrer;
        
        const trackView = (customUrl = url, customReferrer = referrer) => {
            const trackData = {
                group,
                plural,
                name,
                hostname,
                screen: screenSize,
                language,
                url: customUrl,
                referrer: customReferrer
            };
            
            if (!isDisabled()) {
                return fetch(endpoint, {
                    method: "POST",
                    body: JSON.stringify(trackData),
                    headers: {
                        "Content-Type": "application/json"
                    }
                })
                .then(response => response.text())
                .then(result => {
                    console.debug("Visit count:", result);
                });
            }
            return Promise.resolve();
        };
        
        const handlePushState = (state, title, newUrl) => {
            if (newUrl) {
                referrer = location.pathname + location.search;
                const urlString = newUrl.toString();
                const newPath = urlString.substring(0, 4) === "http" ? 
                             "/" + urlString.split("/").splice(3).join("/") : urlString;
                if (newPath !== referrer) {
                    trackView();
                }
            }
        };
        
        if (!window.haloTracker) {
            const trackEvent = event => trackEvent(event);
            trackEvent.trackView = trackView;
            window.haloTracker = trackEvent;
        }
        
        if (autoTrack && !isDisabled()) {
            history.pushState = wrap(history, "pushState", handlePushState);
            history.replaceState = wrap(history, "replaceState", handlePushState);
            
            const track = () => {
                if (document.readyState === "complete") {
                    trackView();
                }
            };
            
            document.addEventListener("readystatechange", track, true);
            track();
        }
        
    })(window);
})();

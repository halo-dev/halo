/**
 * Halo Visit Tracker - Enhanced with Preview Mode Detection
 * Tracks page visits and user interactions while preventing tracking in preview mode
 * @version 2.0.0
 */
(function() {
    'use strict';
    
    // Constants for better maintainability
    var CONSTANTS = {
        PREVIEW_PATH_PREFIX: '/preview/',
        PREVIEW_THEME_PARAM: 'preview-theme=',
        PREVIEW_MODE_KEY: 'halo.preview.mode',
        TRACKER_DISABLED_KEY: 'haloTracker.disabled',
        DATA_PREFIX: 'data-',
        API_ENDPOINT_PATH: '/apis/api.halo.run/v1alpha1/trackers/counter',
        HTTP_PREFIX: 'http',
        HTTP_PREFIX_LENGTH: 4,
        DNT_ENABLED_VALUES: ['1', 'yes'],
        MS_TRACKING_PROP: 'msTrackingProtectionEnabled',
        CONTENT_TYPE_JSON: 'application/json',
        HTTP_METHOD_POST: 'POST',
        READY_STATE_COMPLETE: 'complete',
        AUTO_TRACK_FALSE: 'false'
    };
    
    (function(window) {
        // Cache DOM and window references for better performance
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
        
        // Early return if script element is not available
        if (!currentScript) {
            return;
        }
        
        /**
         * Detects if the current page is in preview mode
         * Checks multiple conditions: URL path, query parameters, and localStorage
         * @returns {boolean} True if in preview mode, false otherwise
         */
        var isPreviewMode = function() {
            return isPreviewPath() || isThemePreview() || isLocalStoragePreview();
        };
        
        /**
         * Checks if current path indicates preview mode
         * @returns {boolean} True if path starts with preview prefix
         */
        var isPreviewPath = function() {
            return pathname.startsWith(CONSTANTS.PREVIEW_PATH_PREFIX);
        };
        
        /**
         * Checks if theme preview parameter is present in URL
         * @returns {boolean} True if preview-theme parameter exists
         */
        var isThemePreview = function() {
            return search.includes(CONSTANTS.PREVIEW_THEME_PARAM);
        };
        
        /**
         * Checks if preview mode is enabled via localStorage
         * @returns {boolean} True if localStorage preview flag is set
         */
        var isLocalStoragePreview = function() {
            return localStorage && 
                   localStorage.getItem(CONSTANTS.PREVIEW_MODE_KEY) === 'true';
        };
        
        // Early exit if preview mode is detected
        if (isPreviewMode()) {
            console.debug('Preview mode detected, skipping visit tracking');
            return;
        }
        
        /**
         * Wraps an object method with additional handler
         * @param {Object} obj - Target object
         * @param {string} method - Method name to wrap
         * @param {Function} handler - Handler function to execute
         * @returns {Function} Wrapped function
         */
        var wrapMethod = function(obj, method, handler) {
            var original = obj[method];
            return function() {
                var args = Array.prototype.slice.call(arguments);
                handler.apply(null, args);
                return original.apply(obj, args);
            };
        };
        
        /**
         * Checks if Do Not Track is enabled in browser
         * @returns {boolean} True if DNT is enabled
         */
        var isDoNotTrackEnabled = function() {
            var dnt = window.doNotTrack;
            var navigator = window.navigator;
            var external = window.external;
            
            var dntValue = dnt || 
                          navigator.doNotTrack || 
                          navigator.msDoNotTrack || 
                          (external && CONSTANTS.MS_TRACKING_PROP in external && 
                           external[CONSTANTS.MS_TRACKING_PROP]());
            
            return CONSTANTS.DNT_ENABLED_VALUES.includes(String(dntValue));
        };
        
        /**
         * Determines if tracking should be disabled
         * @returns {boolean} True if tracking is disabled
         */
        var isTrackingDisabled = function() {
            var isLocallyDisabled = localStorage && 
                                   localStorage.getItem(CONSTANTS.TRACKER_DISABLED_KEY);
            var isDntEnabled = doNotTrack && isDoNotTrackEnabled();
            var isDomainRestricted = domains && !allowedDomains.includes(hostname);
            
            return !!(isLocallyDisabled || isDntEnabled || isDomainRestricted);
        };
        
        // Initialize configuration from script attributes
        var config = initializeConfiguration();
        
        /**
         * Initializes tracker configuration from script data attributes
         * @returns {Object} Configuration object
         */
        function initializeConfiguration() {
            var getAttribute = currentScript.getAttribute.bind(currentScript);
            var doNotTrack = getAttribute(CONSTANTS.DATA_PREFIX + 'do-not-track');
            var domains = getAttribute(CONSTANTS.DATA_PREFIX + 'domains') || '';
            
            return {
                group: getAttribute(CONSTANTS.DATA_PREFIX + 'group') || '',
                plural: getAttribute(CONSTANTS.DATA_PREFIX + 'plural'),
                name: getAttribute(CONSTANTS.DATA_PREFIX + 'name'),
                hostUrl: getAttribute(CONSTANTS.DATA_PREFIX + 'host-url'),
                autoTrack: getAttribute(CONSTANTS.DATA_PREFIX + 'auto-track') !== CONSTANTS.AUTO_TRACK_FALSE,
                doNotTrack: doNotTrack,
                domains: domains,
                allowedDomains: parseAllowedDomains(domains)
            };
        }
        
        /**
         * Parses comma-separated domain list
         * @param {string} domainsString - Comma-separated domain string
         * @returns {Array<string>} Array of trimmed domain names
         */
        function parseAllowedDomains(domainsString) {
            return domainsString.split(',').map(function(domain) {
                return domain.trim();
            });
        }
        
        // Extract configuration values for backward compatibility
        var group = config.group;
        var plural = config.plural;
        var name = config.name;
        var hostUrl = config.hostUrl;
        var autoTrack = config.autoTrack;
        var doNotTrack = config.doNotTrack;
        var domains = config.domains;
        var allowedDomains = config.allowedDomains;
        
        /**
         * Builds the API endpoint URL
         * @returns {string} Complete API endpoint URL
         */
        var buildEndpoint = function() {
            var baseUrl = hostUrl ? 
                         hostUrl.replace(/\/$/, '') : 
                         currentScript.src.split('/').slice(0, -1).join('/');
            return baseUrl + CONSTANTS.API_ENDPOINT_PATH;
        };
        
        var endpoint = buildEndpoint();
        var screenSize = width + 'x' + height;
        var url = pathname + search;
        var referrer = document.referrer;
        
        /**
         * Creates a tracking function for page views
         * @param {string} customUrl - Custom URL to track (optional)
         * @param {string} customReferrer - Custom referrer (optional)
         * @returns {Promise|undefined} Fetch promise or undefined if disabled
         */
        var createTrackingFunction = function(customUrl, customReferrer) {
            var trackingUrl = customUrl !== undefined ? customUrl : url;
            var trackingReferrer = customReferrer !== undefined ? customReferrer : referrer;
            
            var trackingData = {
                group: group,
                plural: plural,
                name: name,
                hostname: hostname,
                screen: screenSize,
                language: language,
                url: trackingUrl,
                referrer: trackingReferrer
            };
            
            return sendTrackingRequest(trackingData);
        };
        
        /**
         * Sends tracking request to server
         * @param {Object} data - Tracking data payload
         * @returns {Promise|undefined} Fetch promise or undefined if disabled
         */
        function sendTrackingRequest(data) {
            if (isTrackingDisabled()) {
                return undefined;
            }
            
            return fetch(endpoint, {
                method: CONSTANTS.HTTP_METHOD_POST,
                body: JSON.stringify(Object.assign({}, data)),
                headers: {
                    'Content-Type': CONSTANTS.CONTENT_TYPE_JSON
                }
            })
            .then(function(response) {
                return response.text();
            })
            .then(function(result) {
                console.debug('Visit count:', result);
            })
            .catch(function(error) {
                console.error('Tracking request failed:', error);
            });
        }
        
        /**
         * Handles browser history state changes
         * @param {string} originalMethod - Original method name
         * @param {*} state - History state
         * @param {string} title - Page title
         * @param {string} url - New URL
         */
        var handleHistoryStateChange = function(originalMethod, state, title, url) {
            if (!url) {
                return;
            }
            
            referrer = location.pathname + location.search;
            var newUrl = url.toString();
            var newPath = extractPathFromUrl(newUrl);
            
            if (newPath !== referrer) {
                createTrackingFunction();
            }
        };
        
        /**
         * Extracts path from URL string
         * @param {string} urlString - Full URL string
         * @returns {string} Extracted path
         */
        function extractPathFromUrl(urlString) {
            var isFullUrl = urlString.substring(0, CONSTANTS.HTTP_PREFIX_LENGTH) === CONSTANTS.HTTP_PREFIX;
            return isFullUrl ? 
                   '/' + urlString.split('/').splice(3).join('/') : 
                   urlString;
        }
        
        // Initialize global tracker object
        if (!window.haloTracker) {
            var trackEvent = function(event) {
                return trackEvent(event);
            };
            trackEvent.trackView = createTrackingFunction;
            window.haloTracker = trackEvent;
        }
        
        // Set up automatic tracking if enabled
        if (autoTrack && !isTrackingDisabled()) {
            setupAutomaticTracking();
        }
        
        /**
         * Sets up automatic page tracking
         */
        function setupAutomaticTracking() {
            // Wrap history methods to track navigation
            history.pushState = wrapMethod(history, 'pushState', handleHistoryStateChange);
            history.replaceState = wrapMethod(history, 'replaceState', handleHistoryStateChange);
            
            // Track initial page load
            var trackInitialLoad = function() {
                if (document.readyState === CONSTANTS.READY_STATE_COMPLETE) {
                    createTrackingFunction();
                }
            };
            
            document.addEventListener('readystatechange', trackInitialLoad, true);
            trackInitialLoad();
        }
        
    })(window);
})();

/**
 * 全局函数
 */
$.extend({
    halo: function () {
    }
});

$.fn.extend({
    animateCss: function (animationName, callback) {
        var animationEnd = (function (el) {
            var animations = {
                animation: 'animationend',
                OAnimation: 'oAnimationEnd',
                MozAnimation: 'mozAnimationEnd',
                WebkitAnimation: 'webkitAnimationEnd'
            };

            for (var t in animations) {
                if (el.style[t] !== undefined) {
                    return animations[t];
                }
            }
        })(document.createElement('div'));

        this.addClass('animated ' + animationName).one(animationEnd, function () {
            $(this).removeClass('animated ' + animationName);

            if (typeof callback === 'function') callback();
        });

        return this;
    }
});

/**
 * 适配移动端并初始化菜单
 */
$(document).ready(function () {
    $("#animated-header,#animated-content").animateCss("fadeIn");
    if ($(window).width() < 1024) {
        if ($('body').hasClass('layout-boxed')) {
            $('body').removeClass('layout-boxed');
        }
        if ($('body').hasClass('sidebar-collapse')) {
            $('body').removeClass('sidebar-collapse');
        }
    }
    initMenu();
});

document.addEventListener('pjax:complete', function () {
    $("#animated-header,#animated-content").animateCss("fadeIn");
    initMenu();
});

/**
 * https://github.com/JpressProjects/jpress/blob/master/jpress-web/src/main/resources/static/admin/js/jpressadmin.js
 */
function initMenu() {
    var pathName = location.pathname;
    if (pathName === "/admin/posts/edit") {
        pathName = "/admin/posts/write";
    }
    if (pathName === "/admin/category/edit") {
        pathName = "/admin/category";
    }
    if (pathName === "/admin/tag/edit") {
        pathName = "/admin/tag";
    }
    if (pathName === "/admin/page/edit") {
        pathName = "/admin/page/new";
    }
    if (pathName === "/admin/page/links") {
        pathName = "/admin/page";
    }
    if (pathName === "/admin/page/galleries") {
        pathName = "/admin/page";
    }
    if (pathName === "/admin/menus/edit") {
        pathName = "/admin/menus";
    }
    $(".sidebar-menu").children().each(function () {
        var li = $(this);
        li.find('a').each(function () {
            var href = $(this).attr("href");
            if (pathName === href) {
                li.addClass("active");
                $(this).parent().addClass("active");
            } else {
                $(this).parent().removeClass("active");
            }
        });
    });
}

/**
 * 普通提示框
 *
 * @param text 提示文字
 * @param icon icon
 * @param hideAfter 隐藏时间
 */
$.halo.prototype.showMsg = function (text, icon, hideAfter) {
    if (heading == undefined) {
        var heading = "提示";
    }
    $.toast({
        text: text,
        heading: heading,
        icon: icon,
        showHideTransition: 'fade',
        allowToastClose: true,
        hideAfter: hideAfter,
        stack: 1,
        position: 'top-center',
        textAlign: 'left',
        loader: true,
        loaderBg: '#ffffff'
    });
};

/**
 * 提示之后刷新页面
 *
 * @param text 提示文字
 * @param icon icon
 * @param hideAfter 隐藏时间
 */
$.halo.prototype.showMsgAndReload = function (text, icon, hideAfter) {
    if (heading == undefined) {
        var heading = "提示";
    }
    $.toast({
        text: text,
        heading: heading,
        icon: icon,
        showHideTransition: 'fade',
        allowToastClose: true,
        hideAfter: hideAfter,
        stack: 1,
        position: 'top-center',
        textAlign: 'left',
        loader: true,
        loaderBg: '#ffffff',
        afterHidden: function () {
            window.location.reload();
        }
    });
};

/**
 * 提示之后重定向
 *
 * @param text 提示文字
 * @param icon icon
 * @param hideAfter 隐藏时间
 * @param url 重定向地址
 */
$.halo.prototype.showMsgAndRedirect = function (text, icon, hideAfter, url, isPjax) {
    if (heading == undefined) {
        var heading = "提示";
    }
    $.toast({
        text: text,
        heading: heading,
        icon: icon,
        showHideTransition: 'fade',
        allowToastClose: true,
        hideAfter: hideAfter,
        stack: 1,
        position: 'top-center',
        textAlign: 'left',
        loader: true,
        loaderBg: '#ffffff',
        afterHidden: function () {
            if (isPjax === 'true') {
                pjax.loadUrl(url);
            } else {
                window.location.href = url;
            }
        }
    });
};

/**
 * 提示之后 父页面重定向
 * @param text text
 * @param icon icon
 * @param hideAfter hideAfter
 * @param url url
 */
$.halo.prototype.showMsgAndParentRedirect = function (text, icon, hideAfter, url) {
    if (heading == undefined) {
        var heading = "提示";
    }
    $.toast({
        text: text,
        heading: heading,
        icon: icon,
        showHideTransition: 'fade',
        allowToastClose: true,
        hideAfter: hideAfter,
        stack: 1,
        position: 'top-center',
        textAlign: 'left',
        loader: true,
        loaderBg: '#ffffff',
        afterHidden: function () {
            parent.location.href = url;
        }
    });
};


/**
 * 格式化字符串
 *
 * @param a a
 * @returns {*} a
 */
$.halo.prototype.formatContent = function (a) {
    a = a.replace(/\r\n/g, '<br/>');
    a = a.replace(/\n/g, '<br/>');
    a = a.replace(/\s/g, ' ');
    return a;
};

/**
 * layer弹出框
 *
 * @param url 地址
 * @param title 标题
 */
$.halo.prototype.layerModal = function (url, title) {
    layer.open({
        type: 2,
        title: title,
        shadeClose: true,
        shade: 0.5,
        maxmin: true,
        area: ['90%', '90%'],
        content: url,
        scrollbar: false
    });
};

/**
 * 保存设置选项
 */
function saveOptions(option) {
    var param = $('#' + option).serialize();
    $.post('/admin/option/save', param, function (data) {
        if (data.code === 1) {
            halo.showMsg(data.msg, 'success', 1000);
        } else {
            halo.showMsg(data.msg, 'error', 2000);
        }
    }, 'JSON');
}

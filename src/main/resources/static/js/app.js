$(document).ready(function () {
    if($(window).width()<1024){
        if($('body').hasClass('layout-boxed')){
            $('body').removeClass('layout-boxed');
        }
        if($('body').hasClass('sidebar-collapse')){
            $('body').removeClass('sidebar-collapse');
        }
    }
    initMenu();
});

$(document).on('pjax:clicked', function() {
    $('.content-wrapper').html("");
});

$(document).on('pjax:complete',function () {
    initMenu();
});

/**
 * https://github.com/JpressProjects/jpress/blob/master/jpress-web/src/main/resources/static/admin/js/jpressadmin.js
 */
function initMenu() {
    var pathName = location.pathname;
    if(pathName=="/admin/posts/edit"){
        pathName="/admin/posts/new";
    }
    if(pathName=="/admin/category/edit"){
        pathName="/admin/category";
    }
    if(pathName=="/admin/tag/edit"){
        pathName="/admin/tag";
    }
    if(pathName=="/admin/page/edit"){
        pathName="/admin/page/new";
    }
    if(pathName=="/admin/page/links"){
        pathName="/admin/page";
    }
    if(pathName=="/admin/page/galleries"){
        pathName="/admin/page";
    }
    if(pathName=="/admin/menus/edit"){
        pathName="/admin/menus";
    }
    $(".sidebar-menu").children().each(function () {
        var li = $(this);
        li.find('a').each(function () {
            var href = $(this).attr("href");
            if (pathName == href) {
                li.addClass("active");
                $(this).parent().addClass("active");
            }else{
                //li.removeClass("active");
                $(this).parent().removeClass("active");
            }
        });
    });
}
/**
 * 提示框
 * @param text
 * @param icon
 * @param hideAfter
 */
function showMsg(text,icon,hideAfter) {
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
}

/**
 * 转义
 * @param str str
 * @returns {string}
 */
function stringEncode(str){
    var div=document.createElement('div');
    if(div.innerText){
        div.innerText=str;
    }else{
        div.textContent=str;
    }
    return div.innerHTML;
}

/**
 * 保存设置选项
 */
function saveOptions(option) {
    var param = $('#'+option).serialize();
    $.ajax({
        type: 'post',
        url: '/admin/option/save',
        data: param,
        success: function (data) {
            if(data.code==1){
                showMsg(data.msg,"success",1000);
            }else {
                showMsg(data.msg,"error",1000);
            }
        }
    });
}

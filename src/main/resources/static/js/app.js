/**
 * 提示框
 * @param text
 * @param icon
 * @param hideAfter
 */
function showMsg(text,icon,hideAfter) {
    $.toast({
        text: text,
        heading: '提示',
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
        success: function (result) {
            showMsg("保存成功！","success",1000);
        }
    });
}
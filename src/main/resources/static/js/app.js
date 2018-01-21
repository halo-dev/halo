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
        stack: 5,
        position: 'top-center',
        textAlign: 'left',
        loader: true,
        loaderBg: '#ffffff'
    });
}
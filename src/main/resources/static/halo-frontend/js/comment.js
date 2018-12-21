var s = new OwO({
    logo: 'OωO表情',
    container: document.getElementsByClassName('OwO')[0],
    target: document.getElementsByClassName('comment-input-content')[0],
    position: 'down',
    width: '100%',
    maxHeight: '210px',
    api: "/static/halo-common/OwO/OwO.min.json"
});
$(document).ready(function () {
    $(".comment-list-one-head").each(function (i) {
        var uaInfo = $(this).children(".comment-ua-info").html();
        $(this).append(show_ua(uaInfo));
    });
    $("#commentAuthor").val(localStorage.getItem("author"));
    $("#commentAuthorEmail").val(localStorage.getItem("email"));
    $("#commentAuthorUrl").val(localStorage.getItem("url"));
    loadAvatar();
});
$('#comment-submit').click(function () {
    var author = $("#commentAuthor");
    var content = $("#commentContent");
    var email = $("#commentAuthorEmail");
    var url = $("#commentAuthorUrl");
    var message = $(".comment-message");
    var submit = $("#comment-submit");
    if (author.val() === '' || content.val() === '') {
        message.html("<span style='color:red'>请输入必填项！</span>");
        message.fadeIn(1000);
        setTimeout(function () {
            message.fadeOut(1000);
        }, 1500);
        return;
    }
    $(this).attr("disabled", "disabled");
    $(this).html("提交中...");
    $.post('/newComment',{
        'postId': $('input[name=postId]').val(),
        'commentContent': formatContent(content.val()),
        'commentAuthor': author.val(),
        'commentAuthorEmail': email.val(),
        'commentAuthorUrl': url.val(),
        'commentAgent': navigator.userAgent,
        'commentParent': $('input[name=commentParent]').val()
    },function (data) {
        localStorage.setItem('author', author.val());
        localStorage.setItem('email', email.val());
        localStorage.setItem('url', url.val());
        if (data.code === 1) {
            $('.comment-input-content').val("");
            message.html("<span>" + data.msg + "</span>");
        } else {
            message.html("<span style='color:red'>" + data.msg + "</span>");
        }
        message.fadeIn(1000);
        setTimeout(function () {
            message.fadeOut(1000);
            submit.removeAttr("disabled");
            submit.html("提交");
            window.location.reload();
        }, 1500);
    },'JSON');
});
$('.comment-list-one-footer-reback').click(function () {
    var commentContent = $('#commentContent');
    var at = $(this).attr("at");
    var commentParentAuthor = $('#comment-id-' + at).find(".comment-list-one-head-name").html();
    $('#commentParent').val(at);
    commentContent.attr("placeholder", "@" + commentParentAuthor);
    $(".comment-cancel-reply").show();
    commentContent.focus();
});
$('.comment-cancel-reply').click(function () {
    $('#commentParent').val(0);
    $('#commentContent').attr("placeholder", "");
    $(".comment-cancel-reply").hide();
});

/**
 * 加载头像
 */
function loadAvatar() {
    var avatar = $(".comment-author-avatar");
    var email = $('input[name=commentAuthorEmail]');
    avatar.attr("src", "//gravatar.loli.net/avatar/" + md5(localStorage.getItem("email")) + "?s=256&d=" + avatarType);
    if (email.val() !== '' && email.val() !== null) {
        avatar.attr("src", "//gravatar.loli.net/avatar/" + md5(email.val()) + "?s=256&d=" + avatarType);
    }
}

/**
 * 格式化ua信息
 * @param string
 * @returns {string}
 */
var parser = new UAParser();

function show_ua(string) {
    parser.setUA(string);
    var uua = parser.getResult();
    if (uua.os.version === 'x86_64') {
        uua.os.version = 'x64';
    }
    var browser = uua.browser.name + ' ' + uua.browser.version;
    var os = uua.os.name + ' ' + uua.os.version;
    return '<span class="ua">' + browser + '</span><span class="ua">' + os + '</span>';
}

/**
 * 格式化字符串
 * @param a a
 * @returns {*}
 */
function formatContent(a) {
    a = a.replace(/\r\n/g, '<br/>');
    a = a.replace(/\n/g, '<br/>');
    a = a.replace(/\s/g, ' ');
    return a;
}

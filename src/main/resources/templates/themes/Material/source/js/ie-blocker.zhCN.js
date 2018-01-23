(function () {
    var ibContainer;
    var html;

    ibContainer = document.createElement('div');
    ibContainer.setAttribute('id', 'ib-container');

    var html="";
    html += "<div class=\"ib-modal\">";
    html += "    <div class=\"ib-header\">";
    html += "        <h1>您的浏览器需要更新<\/h1>";
    html += "        <p>为了保证页面的正常显示并保护您的个人信息，";
    html += "            <br><strong>请使用以下新版浏览器<\/strong>";
    html += "        <\/p>";
    html += "    <\/div>";
    html += "    <ul class=\"ib-browsers\">";
    html += "        <li>";
    html += "            <a href=\"http:\/\/www.google.cn\/chrome\/browser\/desktop\/index.html\">";
    html += "                <div class=\"ib-browser-name\">Chrome<\/div>";
    html += "                <div class=\"ib-browser-description\">快速，简单，安全 - 由谷歌开发<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "        <li>";
    html += "            <a href=\"http:\/\/www.firefox.com.cn\" target=\"_blank\">";
    html += "                <div class=\"ib-browser-name\">Firefox<\/div>";
    html += "                <div class=\"ib-browser-description\">快速，安全，免费，开源的浏览器<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "        <li>";
    html += "            <a href=\"https:\/\/www.apple.com\/cn\/safari\" target=\"_blank\">";
    html += "                <div class=\"ib-browser-name\">Safari<\/div>";
    html += "                <div class=\"ib-browser-description\">由苹果公司设计用于 Mac 的产品<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "        <li>";
    html += "            <a href=\"http:\/\/www.opera.com\/zh-cn\" target=\"_blank\">";
    html += "                <div class=\"ib-browser-name\">Opera<\/div>";
    html += "                <div class=\"ib-browser-description\">快速, 小巧的浏览器<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "    <\/ul>";
    html += "    <div class=\"ib-footer\">";
    html += "        <a class=\"ib-try\" href=\"http:\/\/www.google.cn\/chrome\/browser\/desktop\/index.html\">试试Chrome<\/a>";
    html += "    <\/div>";
    html += "<\/div>";
    html += "<div class=\"ib-mask\"><\/div>";
    html += "";

    ibContainer.innerHTML = html;

    window.onload = function () {

        document.body.appendChild(ibContainer);

        ibContainer.style.display = 'block';
    };
})();

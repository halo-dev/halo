(function () {
    var ibContainer;
    var html;

    ibContainer = document.createElement('div');
    ibContainer.setAttribute('id', 'ib-container');

    html = "";
    html += "<div class=\"ib-modal\">";
    html += "    <div class=\"ib-header\">";
    html += "        <h1>You are using an outdated browser.<\/h1>";
    html += "        <p>To display the website correctly and protect your information,";
    html += "            <br><strong>please use one of these up-to-date, free and excellent browsers.<\/strong>";
    html += "        <\/p>";
    html += "    <\/div>";
    html += "    <ul class=\"ib-browsers\">";
    html += "        <li>";
    html += "            <a href=\"http:\/\/www.google.com\/chrome\/browser\/desktop\/index.html\">";
    html += "                <div class=\"ib-browser-name\">Chrome<\/div>";
    html += "                <div class=\"ib-browser-description\">Speed, Simplicity, Security - Google.<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "        <li>";
    html += "            <a href=\"http:\/\/www.firefox.com\">";
    html += "                <div class=\"ib-browser-name\">Firefox<\/div>";
    html += "                <div class=\"ib-browser-description\">The Open Source browser.<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "        <li>";
    html += "            <a href=\"https:\/\/www.apple.com\/safari\">";
    html += "                <div class=\"ib-browser-name\">Safari<\/div>";
    html += "                <div class=\"ib-browser-description\">Browser designed by Apple Inc.<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "        <li>";
    html += "            <a href=\"http:\/\/www.opera.com\">";
    html += "                <div class=\"ib-browser-name\">Opera<\/div>";
    html += "                <div class=\"ib-browser-description\">Fastest and light browser.<\/div>";
    html += "            <\/a>";
    html += "        <\/li>";
    html += "    <\/ul>";
    html += "    <div class=\"ib-footer\">";
    html += "        <a class=\"ib-try\" href=\"http:\/\/www.google.com\/chrome\/browser\/desktop\/index.html\">Try Chrome now!<\/a>";
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

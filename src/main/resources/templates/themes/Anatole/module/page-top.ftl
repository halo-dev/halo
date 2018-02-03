<div class="page-top animated fadeInDown">
    <div class="nav">
        <li>
            <a href="/" <#if is_home?default(false)==true>class="current"</#if>>Home </a>
        </li>
        <li>
            <a href="/about">About</a>
        </li>
        <li>
            <a href="/archives" <#if is_archives?default(false)==true>class="current"</#if>>Archive</a>
        </li>
        <li>
            <a href="/links" <#if is_links?default(false)==true>class="current"</#if>>Links</a>
        </li>
    </div>
    <div class="information">
        <div class="back_btn">
            <li>
                <a onclick="window.history.go(-1)" style="display:none;" class="fa fa-chevron-left"></a>
            </li>
        </div>
        <div class="avatar">
            <img src="/anatole/source/images/favicon.png" />
        </div>
    </div>
</div>
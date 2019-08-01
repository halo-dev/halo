<ul class="social-links">

    <#if settings.rss!true>
    <li>
        <a target="_blank" href="${context!}/atom.xml">
            <i class="fa fa-rss"></i>
        </a>
    </li>
    </#if>

    <#if settings.twitter??>
    <li>
        <a target="_blank" href="https://twitter.com/${settings.twitter}">
            <i class="fa fa-twitter"></i>
        </a>
    </li>
    </#if>

    <#if settings.facebook??>
    <li>
        <a target="_blank" href="https://www.facebook.com/${settings.facebook}">
            <i class="fa fa-facebook"></i>
        </a>
    </li>
    </#if>

    <#if settings.instagram??>
    <li>
        <a target="_blank" href="https://www.instagram.com/${settings.instagram}">
            <i class="fa fa-instagram"></i>
        </a>
    </li>
    </#if>

    <#if settings.dribbble??>
    <li>
        <a target="_blank" href="https://dribbble.com/${settings.dribbble}">
            <i class="fa fa-dribbble"></i>
        </a>
    </li>
    </#if>

    <#if settings.weibo??>
    <li>
        <a target="_blank" href="https://weibo.com/${settings.weibo}">
            <i class="fa fa-weibo"></i>
        </a>
    </li>
    </#if>

    <#if settings.qq??>
        <li>
            <a target="_blank" href="tencent://message/?uin=${settings.qq}&Site=&Menu=yes">
                <i class="fa fa-qq"></i>
            </a>
        </li>
    </#if>

    <#if settings.telegram??>
        <li>
            <a target="_blank" href="https://t.me/${settings.telegram}">
                <i class="fa fa-telegram"></i>
            </a>
        </li>
    </#if>

    <#if settings.email??>
        <li>
            <a target="_blank" href="mailto:${settings.email}">
                <i class="fa fa-envelope"></i>
            </a>
        </li>
    </#if>

    <#if settings.github??>
        <li>
            <a target="_blank" href="https://github.com/${settings.github}">
                <i class="fa fa-github"></i>
            </a>
        </li>
    </#if>
</ul>

<ul class="social-links">

    <#if (options.theme_anatole_sns_rss!'true')=='true'>
    <li>
        <a target="_blank" href="${options.blog_url!}/atom.xml">
            <i class="fa fa-rss"></i>
        </a>
    </li>
    </#if>

    <#if options.theme_anatole_sns_twitter??>
    <li>
        <a target="_blank" href="https://twitter.com/${options.theme_anatole_sns_twitter}">
            <i class="fa fa-twitter"></i>
        </a>
    </li>
    </#if>

    <#if options.theme_anatole_sns_facebook??>
    <li>
        <a target="_blank" href="https://www.facebook.com/${options.theme_anatole_sns_facebook}">
            <i class="fa fa-facebook"></i>
        </a>
    </li>
    </#if>

    <#if options.theme_anatole_sns_instagram??>
    <li>
        <a target="_blank" href="https://www.instagram.com/${options.theme_anatole_sns_instagram}">
            <i class="fa fa-instagram"></i>
        </a>
    </li>
    </#if>

    <#if options.theme_anatole_sns_dribbble??>
    <li>
        <a target="_blank" href="https://dribbble.com/${options.theme_anatole_sns_dribbble}">
            <i class="fa fa-dribbble"></i>
        </a>
    </li>
    </#if>

    <#if options.theme_anatole_sns_weibo??>
    <li>
        <a target="_blank" href="https://weibo.com/${options.theme_anatole_sns_weibo}">
            <i class="fa fa-weibo"></i>
        </a>
    </li>
    </#if>

    <#if options.theme_anatole_sns_qq??>
        <li>
            <a target="_blank" href="tencent://message/?uin=${options.theme_anatole_sns_qq}&Site=&Menu=yes">
                <i class="fa fa-qq"></i>
            </a>
        </li>
    </#if>

    <#if options.theme_anatole_sns_telegram??>
        <li>
            <a target="_blank" href="https://t.me/${options.theme_anatole_sns_telegram}">
                <i class="fa fa-telegram"></i>
            </a>
        </li>
    </#if>

    <#if options.theme_anatole_sns_email??>
        <li>
            <a target="_blank" href="mailto:${options.theme_anatole_sns_email}">
                <i class="fa fa-envelope"></i>
            </a>
        </li>
    </#if>

    <#if options.theme_anatole_sns_github??>
        <li>
            <a target="_blank" href="https://github.com/${options.theme_anatole_sns_github}">
                <i class="fa fa-github"></i>
            </a>
        </li>
    </#if>
</ul>

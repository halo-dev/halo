<div class="sidebar animated fadeInDown">
    <div class="logo-title">
        <div class="title">
            <img src="${options.blog_logo!'${static!}/source/images/logo@2x.png'}" style="width:127px;<#if settings.avatar_circle!false>border-radius:50%</#if>" />
            <h3 title="">
                <a href="${context!}">${options.blog_title!'Anatole'}</a>
            </h3>
            <div class="description">
                <#if settings.hitokoto!false>
                    <p id="yiyan">获取中...</p>
                <#else >
                    <p>${user.description!'A other Halo theme'}</p>
                </#if>
            </div>
        </div>
    </div>
    <#include "social-list.ftl">
    <div class="footer">
        <a target="_blank" href="#">

        </a>
    </div>
</div>

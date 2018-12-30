<div class="sidebar animated fadeInDown">
    <div class="logo-title">
        <div class="title">
            <img src="${options.blog_logo!'/anatole/source/images/logo@2x.png'}" style="width:127px;<#if (options.anatole_style_avatar_circle!'false')=='true'>border-radius:50%</#if>" />
            <h3 title="">
                <a href="/">${options.blog_title!'ANATOLE'}</a>
            </h3>
            <div class="description">
                <#if (options.anatole_style_hitokoto!'false')=="true">
                    <p id="yiyan">获取中...</p>
                <#else >
                    <p>${user.userDesc!'A other Halo theme'}</p>
                </#if>
            </div>
        </div>
    </div>
    <#include "social-list.ftl">
    <div class="footer">
        <a target="_blank" href="#">
            <#-- 不允许修改该主题信息，也不能删除。 -->
            <span>Designed by </span>
            <a href="https://www.caicai.me">CaiCai</a>
            <#-- 虽然Halo使用了宽松的GPL协议，但开发不易，希望您可以保留一下版权声明。笔芯~ -->
            <div class="by_halo">
                <a href="https://github.com/ruibaby/halo" target="_blank">Proudly published with Halo&#65281;</a>
            </div>
            <div class="footer_text">
                <@footer_info></@footer_info>
            </div>
        </a>
    </div>
</div>

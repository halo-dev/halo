<#compress >
<#include "module/_macro.ftl">
<@head><@spring.message code="admin.halo.title" /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type='text/css'>
            blockquote{
                border-left: 4px solid #dddddd;
                padding: 0 15px;
                color: #777777;
                font-size: 16px;
            }
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.halo.title' /></h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li class="active"><@spring.message code='admin.halo.bread.active' /></li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div id='write' class='is-mac'>
                <blockquote style="font-size: 14px;">
                    <p><@spring.message code='admin.halo.content.p1' />😉</p>
                    <p><@spring.message code='admin.halo.content.p2' /></p>
                </blockquote>
                <p><@spring.message code='admin.halo.content.p3' /></p>
                <p>Github issues ：<a href='https://github.com/ruibaby/halo/issues' target="_blank">https://github.com/ruibaby/halo/issues</a></p>
                <p>Blog : <a href="https://ryanc.cc" target="_blank">https://ryanc.cc</a> </p>
                <p>Email : <a href='mailto:i@ryanc.cc'>i@ryanc.cc</a></p>
                <p>Telegram : <a href="https://t.me/ruibaby" target="_blank">ruibaby</a></p>
                <p>Telegram Channel: <a href="https://t.me/ryan0up" target="_blank">Ryan0up'S Channel</a></p>
                <p>QQ : 709831589</p>
            </div>
        </section>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>

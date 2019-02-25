<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.themes.title' /></@head>
<div class="content-wrapper">
    <link rel="stylesheet" href="/static/halo-backend/plugins/fileinput/fileinput.min.css">
    <style type="text/css" rel="stylesheet">
        .theme-thumbnail{
            width:100%;
            height:0;
            padding-bottom: 60%;
            overflow:hidden;
            border-top-left-radius: 3px;
            border-top-right-radius: 3px;
            border-bottom-right-radius: 0;
            border-bottom-left-radius: 0;
            background-position: center;
            -webkit-background-size:cover;
            -moz-background-size:cover;
            background-size:cover;
            cursor: pointer;
            transition: all .2s ease-in-out;
        }
        .box-footer{
            padding: 5px;
        }
        .theme-title{
            font-size: 18px;
            line-height: 30px;
        }
        .btn-delete:hover{
            color: red;
        }
    </style>
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.themes.title' /></h1>
        <a class="btn-header" id="showForm" href="javascript:void(0)" onclick="halo.layerModal('/admin/themes/install','<@spring.message code="admin.themes.js.install-theme" />')">
            <i class="fa fa-cloud-upload" aria-hidden="true"></i><@spring.message code='admin.themes.btn.install' />
        </a>
        <ol class="breadcrumb">
            <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a></li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.themes.bread.appearance' /></a></li>
            <li class="active"><@spring.message code='admin.themes.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <#if themes?? && (themes?size>0)>
                <#list themes as theme>
                    <div class="col-md-6 col-lg-3 col theme-body">
                        <div class="box box-solid">
                            <div class="box-body theme-thumbnail" style="background-image: url(/${theme.themeName!}/screenshot.png)">
                                <div class="pull-right btn-delete" style="display: none" onclick="modelShow('/admin/themes/remove?themeName=${theme.themeName}')"><i class="fa fa-times fa-lg" aria-hidden="true"></i></div>
                            </div>
                            <div class="box-footer">
                                <span class="theme-title">${theme.themeName!?cap_first}</span>
                                <#if theme.hasOptions>
                                    <button class="btn btn-primary btn-sm pull-right btn-theme-setting" onclick="halo.layerModal('/admin/themes/options?theme=${theme.themeName!}&hasUpdate=<#if theme.hasUpdate>true<#else>false</#if>','${theme.themeName!} <@spring.message code="admin.themes.js.theme-setting" />')" style="display: none"><@spring.message code='admin.themes.btn.setting' /></button>
                                </#if>
                                <#if activeTheme != "${theme.themeName}">
                                    <button class="btn btn-default btn-sm pull-right btn-theme-enable" onclick="setTheme('${theme.themeName!}')" style="display: none;margin-right: 3px"><@spring.message code='admin.themes.btn.enable' /></button>
                                    <#else>
                                    <button class="btn btn-default btn-sm pull-right btn-theme-enable" style="display: none;margin-right: 3px" disabled><@spring.message code='admin.themes.btn.activated' /></button>
                                </#if>
                            </div>
                        </div>
                    </div>
                </#list>
                <#else>
                <div class="col-md-12">
                    <h2><@spring.message code='common.text.no-data' /></h2>
                </div>
            </#if>
        </div>
    </section>
    <!-- 删除确认弹出层 -->
    <div class="modal fade" id="removeThemeModal">
        <div class="modal-dialog">
            <div class="modal-content message_align">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title"><@spring.message code='common.text.tips' /></h4>
                </div>
                <div class="modal-body">
                    <p id="message"><@spring.message code='common.text.define-delete' /></p>
                </div>
                <div class="modal-footer">
                    <input type="hidden" id="url"/>
                    <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message code='common.btn.cancel' /></button>
                    <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal"><@spring.message code='common.btn.define' /></a>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">

    /**
     * 设置主题
     * @param site_theme 主题名
     */
    function setTheme(site_theme) {
        $.get('/admin/themes/set',{'siteTheme': site_theme},function(data) {
            if(data.code === 1){
                halo.showMsgAndRedirect(data.msg,'success',1000,'/admin/themes',"${options.admin_pjax!'true'}");
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON');
    }

    var themeThumbnail = $('.theme-thumbnail');
    var themeBody = $('.theme-body');

    themeThumbnail.mouseover(function () {
        $(this).children('.btn-delete').show();
    });
    themeThumbnail.mouseleave(function () {
        $(this).children('.btn-delete').hide();
    });
    themeBody.mouseover(function () {
        $(this).find(".theme-thumbnail").css("opacity","0.8");
        $(this).find(".btn-theme-setting,.btn-theme-enable,.btn-theme-update").show();
    });
    themeBody.mouseleave(function () {
        $(this).find(".theme-thumbnail").css("opacity","1");
        $(this).find(".btn-theme-setting,.btn-theme-enable,.btn-theme-update").hide();
    });
    function modelShow(url) {
        $('#url').val(url);
        $('#removeThemeModal').modal();
    }
    function removeIt(){
        var url=$.trim($("#url").val());
        <#if (options.admin_pjax!'true') == 'true'>
            pjax.loadUrl(url);
        <#else>
            window.location.href = url;
        </#if>
    }
</script>
</@footer>
</#compress>

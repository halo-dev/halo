<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.themes.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
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
            #uploadForm{
                display: none;
            }
            #showForm{
                margin-left: 4px;
                padding: 3px 6px;
                position: relative;
                top: -4px;
                border: 1px solid #ccc;
                border-radius: 2px;
                background: #fff;
                text-shadow: none;
                font-weight: 600;
                font-size: 12px;
                line-height: normal;
                color: #3c8dbc;
                cursor: pointer;
                transition: all .2s ease-in-out;
            }
            #showForm:hover{
                background: #3c8dbc;
                color: #fff;
            }
            .btn-delete:hover{
                color: red;
            }
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;"><@spring.message code='admin.themes.title' /></h1>
            <a id="showForm" href="#" onclick="openThemeInstall()">
                <i class="fa fa-cloud-upload" aria-hidden="true"></i><@spring.message code='admin.themes.btn.install' />
            </a>
            <ol class="breadcrumb">
                <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a></li>
                <li><a data-pjax="true" href="#"><@spring.message code='admin.themes.bread.appearance' /></a></li>
                <li class="active"><@spring.message code='admin.themes.title' /></li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <#if themes?? && (themes?size>0)>
                    <#list themes as theme>
                        <div class="col-md-6 col-lg-3 col theme-body">
                            <div class="box box-solid">
                                <div class="box-body theme-thumbnail" style="background-image: url(/${theme.themeName?if_exists}/screenshot.png)">
                                    <div class="pull-right btn-delete" style="display: none" onclick="modelShow('/admin/themes/remove?themeName=${theme.themeName}')"><i class="fa fa-times fa-lg" aria-hidden="true"></i></div>
                                </div>
                                <div class="box-footer">
                                    <span class="theme-title">${theme.themeName?if_exists?upper_case}</span>
                                    <#if theme.hasOptions>
                                        <button class="btn btn-primary btn-sm pull-right btn-theme-setting" onclick="openSetting('${theme.themeName?if_exists}','<#if theme.hasUpdate>true<#else>false</#if>')" style="display: none"><@spring.message code='admin.themes.btn.setting' /></button>
                                    </#if>
                                    <#if activeTheme != "${theme.themeName}">
                                        <button class="btn btn-default btn-sm pull-right btn-theme-enable" onclick="setTheme('${theme.themeName?if_exists}')" style="display: none;margin-right: 3px"><@spring.message code='admin.themes.btn.enable' /></button>
                                        <#else>
                                        <button class="btn btn-default btn-sm pull-right btn-theme-enable" style="display: none;margin-right: 3px" disabled><@spring.message code='admin.themes.btn.activated' /></button>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </#list>
                    <#else>
                    <div class="col-md-12">
                        <h2>居然没有主题？</h2>
                        <h2>你仿佛在逗我？</h2>
                        <h2>赶紧去上传一个主题，不然前台会报错！</h2>
                        <h2>No themes?</h2>
                        <h2>You fang fu is douing me?</h2>
                        <h2>Please upload a theme,Otherwise the page will be incorrect.</h2>
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
                        <p id="message">你确定要删除该主题？</p>
                    </div>
                    <div class="modal-footer">
                        <input type="hidden" id="url"/>
                        <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message code='common.btn.cancel' /></button>
                        <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal"><@spring.message code='common.btn.define' /></a>
                    </div>
                </div>
            </div>
        </div>
        <script type="application/javascript">
            /**
             * 打开安装主题的窗口
             */
            function openThemeInstall() {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="admin.themes.js.install-theme" />',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/themes/install',
                    scrollbar: false
                });
            }

            /**
             * 设置主题
             * @param site_theme 主题名
             */
            function setTheme(site_theme) {
                $.ajax({
                    type: 'get',
                    url: '/admin/themes/set',
                    data: {
                        'siteTheme': site_theme
                    },
                    success: function (data) {
                        if(data.code==1){
                            $.toast({
                                text: data.msg,
                                heading: '<@spring.message code="common.text.tips" />',
                                icon: 'success',
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: 1000,
                                stack: 1,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff',
                                afterHidden: function () {
                                    window.location.reload();
                                }
                            });
                        }else{
                            $.toast({
                                text: data.msg,
                                heading: '<@spring.message code="common.text.tips" />',
                                icon: 'error',
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: 2000,
                                stack: 1,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff'
                            });
                        }
                    }
                });
            }

            /**
             * 打开主题设置
             *
             * @param theme 主题名
             */
            function openSetting(theme,hasUpdate) {
                layer.open({
                    type: 2,
                    title: theme+' <@spring.message code="admin.themes.js.theme-setting" />',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/themes/options?theme='+theme+'&hasUpdate='+hasUpdate,
                    scrollbar: false
                });
            }
            $('.theme-thumbnail').mouseover(function () {
                $(this).children('.btn-delete').show();
            });
            $('.theme-thumbnail').mouseleave(function () {
                $(this).children('.btn-delete').hide();
            });
            $('.theme-body').mouseover(function () {
                $(this).find(".theme-thumbnail").css("opacity","0.8");
                $(this).find(".btn-theme-setting,.btn-theme-enable,.btn-theme-update").show();
            });
            $('.theme-body').mouseleave(function () {
                $(this).find(".theme-thumbnail").css("opacity","1");
                $(this).find(".btn-theme-setting,.btn-theme-enable,.btn-theme-update").hide();
            });
            function modelShow(url) {
                $('#url').val(url);
                $('#removeThemeModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>

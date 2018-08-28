<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：主题"></@head>
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
            <h1 style="display: inline-block;">主题管理</h1>
            <a id="showForm" href="#" onclick="openThemeInstall()">
                <i class="fa fa-cloud-upload" aria-hidden="true"></i>安装主题
            </a>
            <ol class="breadcrumb">
                <li><a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a data-pjax="true" href="#">外观</a></li>
                <li class="active">主题管理</li>
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
                                        <button class="btn btn-primary btn-sm pull-right btn-theme-setting" onclick="openSetting('${theme.themeName?if_exists}')" style="display: none">设置</button>
                                    </#if>
                                    <#if theme.hasUpdate>
                                        <button class="btn btn-warning btn-sm pull-right btn-theme-update" data-loading-text="更新中..." onclick="updateTheme('${theme.themeName?if_exists}',this)" style="display: none;margin-right: 3px">更新</button>
                                    </#if>
                                    <#if activeTheme != "${theme.themeName}">
                                        <button class="btn btn-default btn-sm pull-right btn-theme-enable" onclick="setTheme('${theme.themeName?if_exists}')" style="display: none;margin-right: 3px">启用</button>
                                        <#else>
                                        <button class="btn btn-default btn-sm pull-right btn-theme-enable" style="display: none;margin-right: 3px" disabled>已启用</button>
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
                        <h4 class="modal-title">提示信息</h4>
                    </div>
                    <div class="modal-body">
                        <p id="message">你确定要删除该主题？</p>
                    </div>
                    <div class="modal-footer">
                        <input type="hidden" id="url"/>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal">确定</a>
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
                    title: '安装主题',
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
                                heading: '提示',
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
                                heading: '提示',
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
             * 更新主题
             */
            function updateTheme(theme,e) {
                $(e).button('loading');
                $.ajax({
                    type: 'get',
                    url: '/admin/themes/pull',
                    data: {
                        'themeName': theme
                    },
                    success: function (data) {
                        if(data.code==1){
                            $.toast({
                                text: data.msg,
                                heading: '提示',
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
                                    window.location.href="/admin/themes";
                                }
                            });
                        }else{
                            $.toast({
                                text: data.msg,
                                heading: '提示',
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
                            $(e).button('reset');
                        }
                    }
                });
            }

            /**
             * 打开主题设置
             *
             * @param theme 主题名
             */
            function openSetting(theme) {
                layer.open({
                    type: 2,
                    title: theme+'主题设置',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/themes/options?theme='+theme,
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

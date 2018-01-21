<#include "module/_macro.ftl">
<@head title="Halo后台管理-主题"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <link rel="stylesheet" href="/static/plugins/toast/css/jquery.toast.min.css">
        <link rel="stylesheet" href="/static/plugins/fileinput/fileinput.min.css">
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
            .theme-thumbnail:hover{
                opacity: .8;
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
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;">主题设置</h1>
            <a id="showForm" href="#">
                <i class="fa fa-cloud-upload" aria-hidden="true"></i>上传
            </a>
            <ol class="breadcrumb">
                <li><a href="/admin"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li class="active">主题设置</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row" id="uploadForm">
                <div class="col-md-12">
                    <div class="form-group">
                        <div class="file-loading">
                            <input id="uploadTheme" class="file-loading" type="file" name="file" multiple>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-3">
                    <div class="box box-solid">
                        <div class="box-body theme-thumbnail" style="background-image: url(/static/images/boxed-bg.jpg)"></div>
                        <div class="box-footer">
                            <span class="theme-title">Halo</span>
                            <button class="btn btn-primary btn-sm pull-right" onclick="openSetting('halo')">设置</button>
                            <#if theme == "halo">
                                <button class="btn btn-primary btn-sm pull-right" disabled>已启用</button>
                            <#else >
                                <button onclick="setTheme('halo')" class="btn btn-primary btn-sm pull-right">启用</button>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="box box-solid">
                        <div class="box-body theme-thumbnail" style="background-image: url(/static/images/material.png)"></div>
                        <div class="box-footer">
                            <span class="theme-title">Material</span>
                            <button class="btn btn-primary btn-sm pull-right" onclick="openSetting('material')">设置</button>
                            <#if theme == "material">
                                <button class="btn btn-primary btn-sm pull-right" disabled>已启用</button>
                            <#else >
                                <button onclick="setTheme('material')" class="btn btn-primary btn-sm pull-right">启用</button>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="box box-solid">
                        <div class="box-body theme-thumbnail" style="background-image: url(/static/images/material.png)"></div>
                        <div class="box-footer">
                            <span class="theme-title">Anatole</span>
                            <button class="btn btn-primary btn-sm pull-right" onclick="openSetting('Anatole')">设置</button>
                            <#if theme == "Anatole">
                                <button class="btn btn-primary btn-sm pull-right" disabled>已启用</button>
                            <#else >
                                <button onclick="setTheme('Anatole')" class="btn btn-primary btn-sm pull-right">启用</button>
                            </#if>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/toast/js/jquery.toast.min.js"></script>
        <script src="/static/plugins/layer/layer.js"></script>
        <script type="application/javascript">
            function loadFileInput() {
                $.getScript("/static/plugins/fileinput/fileinput.min.js",function () {
                    $.getScript("/static/plugins/fileinput/zh.min.js",function () {
                        $('#uploadTheme').fileinput({
                            language: 'zh',
                            uploadUrl: '/admin/themes/upload',
                            allowedFileExtensions: ['zip'],
                            maxFileCount: 1,
                            enctype: 'multipart/form-data',
                            dropZoneTitle: '拖拽文件到这里 &hellip;<br>此模式不支持多文件同时上传',
                            showClose: false
                        });
                    });
                });
            }
            $(document).ready(function () {
                loadFileInput();
            });
            <#if options.admin_pjax?default("true") == "true">
            $(document).on('pjax:complete',function () {
                loadFileInput();
            });
            </#if>
            $("#showForm").click(function(){
                $("#uploadForm").slideToggle(400);
            });
            function setTheme(site_theme) {
                $.ajax({
                    type: 'get',
                    url: '/admin/themes/set',
                    data: {
                        'siteTheme': site_theme
                    },
                    dataType: 'text',
                    success: function (result) {
                        if(result=="success"){
                            $.toast({
                                text: "设置中...",
                                heading: '提示',
                                icon: 'info',
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: 1000,
                                stack: 5,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff',
                                afterHidden: function () {
                                    window.location.reload();
                                }
                            });
                        }
                    }
                });
            }
            function openSetting(theme) {
                layer.open({
                    type: 2,
                    title: theme+'主题设置',
                    shadeClose: true,
                    shade: 0.5,
                    area: ['90%', '90%'],
                    content: '/admin/themes/options?theme='+theme,
                    scrollbar: false
                });
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
    <div class="control-sidebar-bg"></div>
</div>
<@footer></@footer>
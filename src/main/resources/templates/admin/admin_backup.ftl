<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：备份"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            .resourceType,.databaseType,.postType{list-style:none;float:left;margin:0;padding-bottom:10px}
        </style>
        <section class="content-header">
            <h1 style="display: inline-block;">备份管理</h1>
            <ol class="breadcrumb">
                <li>
                    <a href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a href="#">设置</a></li>
                <li class="active">备份管理</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-xs-12">
                    <ul style="list-style: none;padding-left: 0">
                        <li class="resourceType">
                            <a data-pjax="true" href="/admin/backup?type=resources" <#if type=='resources'>style="color: #000" </#if>>资源文件备份</a>&nbsp;|&nbsp;
                        </li>
                        <li class="databaseType">
                            <a data-pjax="true" href="/admin/backup?type=databases" <#if type=='databases'>style="color: #000" </#if>>数据库备份</a>&nbsp;|&nbsp;
                        </li>
                        <li class="postType">
                            <a data-pjax="true" href="/admin/backup?type=posts" <#if type=='posts'>style="color: #000" </#if>>文章备份</a>
                        </li>
                    </ul>
                </div>
                <div class="col-xs-12">
                    <div class="box box-primary">
                        <div class="box-body table-responsive">
                            <table class="table table-bordered table-hover">
                                <thead>
                                <tr>
                                    <th>文件名称</th>
                                    <th>备份时间</th>
                                    <th>文件大小</th>
                                    <th>文件类型</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if backups?size gt 0>
                                        <#list backups?sort_by("createAt")?reverse as backup>
                                            <tr>
                                                <td>${backup.fileName}</td>
                                                <td>${backup.createAt?string("yyyy-MM-dd HH:mm")}</td>
                                                <td>${backup.fileSize}</td>
                                                <td>${backup.fileType}</td>
                                                <td>
                                                    <a href="/backup/${type}/${backup.fileName}" class="btn btn-xs btn-primary" download="${backup.fileName}">下载</a>
                                                    <button type="button" class="btn btn-xs btn-info" onclick="sendToEmail('${backup.fileName}','${backup.backupType}')">发送到邮箱</button>
                                                    <button type="button" class="btn btn-xs btn-danger" onclick="delBackup('${backup.fileName}','${backup.backupType}')">删除</button>
                                                </td>
                                            </tr>
                                        </#list>
                                    <#else>
                                        <tr>
                                            <th colspan="5" style="text-align: center">暂无备份</th>
                                        </tr>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <div class="box-footer clearfix">
                            <button type="button" class="btn btn-primary btn-sm " onclick="btn_backup('${type}')">备份</button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script>

            /**
             * 备份
             */
            function btn_backup(type) {
                $.ajax({
                    type: 'GET',
                    url: '/admin/backup/doBackup',
                    async: false,
                    data: {
                        'type' : type
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
             * 发送备份到邮箱
             *
             * @param fileName
             * @param type
             */
            function sendToEmail(fileName,type) {
                $.ajax({
                    type: 'GET',
                    url: '/admin/backup/sendToEmail',
                    async: false,
                    data: {
                        'type' : type,
                        'fileName' : fileName
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
                                loaderBg: '#ffffff'
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
             * 删除备份
             */
            function delBackup(fileName,type) {
                $.ajax({
                    type: 'GET',
                    url: '/admin/backup/delBackup',
                    async: false,
                    data: {
                        'type' : type,
                        'fileName' : fileName
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
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>

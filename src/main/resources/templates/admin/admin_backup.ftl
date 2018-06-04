<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：备份"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <section class="content-header">
            <h1 style="display: inline-block;">博客备份</h1>
            <ol class="breadcrumb">
                <li>
                    <a href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a href="#">设置</a></li>
                <li class="active">博客备份</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-12">
                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs">
                            <li class="active">
                                <a href="#resources" data-toggle="tab">资源目录备份</a>
                            </li>
                            <li>
                                <a href="#database" data-toggle="tab">数据库备份</a>
                            </li>
                            <li>
                                <a href="#post" data-toggle="tab">文章备份</a>
                            </li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="resources">
                                <form method="post" class="form-horizontal" id="resourcesBackup">
                                    <div class="box-body table-responsive" style="padding: 10px 0;">
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
                                                <#if resourcesBackup?size gt 0>
                                                    <#list resourcesBackup as resource>
                                                        <tr>
                                                            <td>${resource.fileName}</td>
                                                            <td>${resource.createAt?string("yyyy-MM-dd HH:mm")}</td>
                                                            <td>${resource.fileSize}</td>
                                                            <td>${resource.fileType}</td>
                                                            <td>
                                                                <a href="/backup/resources/${resource.fileName}" class="btn btn-xs btn-primary" download="${resource.fileName}">下载</a>
                                                                <button type="button" class="btn btn-xs btn-info" onclick="sendToEmail('${resource.fileName}','${resource.backupType}')">发送到邮箱</button>
                                                                <button type="button" class="btn btn-xs btn-danger" onclick="delBackup('${resource.fileName}','${resource.backupType}')">删除</button>
                                                            </td>
                                                        </tr>
                                                    </#list>
                                                <#else>
                                                <tr>
                                                    <td colspan="5" style="text-align: center">暂无备份</td>
                                                </tr>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="btn_backup('resources')">备份</button>
                                    </div>
                                </form>
                            </div>
                            <div class="tab-pane" id="database">
                                <form method="post" class="form-horizontal" id="databaseBackup">
                                    <div class="box-body table-responsive" style="padding: 10px 0;">
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
                                                <#if databasesBackup?size gt 0>
                                                    <#list databasesBackup as database>
                                                        <tr>
                                                            <td>${database.fileName}</td>
                                                            <td>${database.createAt?string("yyyy-MM-dd HH:mm")}</td>
                                                            <td>${database.fileSize}</td>
                                                            <td>${database.fileType}</td>
                                                            <td>
                                                                <a href="/backup/databases/${database.fileName}" class="btn btn-xs btn-primary" download="${database.fileName}">下载</a>
                                                                <button type="button" class="btn btn-xs btn-info" onclick="sendToEmail('${database.fileName}','${database.backupType}')">发送到邮箱</button>
                                                                <button type="button" class="btn btn-xs btn-danger" onclick="delBackup('${database.fileName}','${database.backupType}')">删除</button>
                                                            </td>
                                                        </tr>
                                                    </#list>
                                                <#else>
                                                <tr>
                                                    <td colspan="5" style="text-align: center">暂无备份</td>
                                                </tr>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="btn_backup('db')">备份</button>
                                    </div>
                                </form>
                            </div>
                            <div class="tab-pane" id="post">
                                <form method="post" class="form-horizontal" id="postBackup">
                                    <div class="box-body table-responsive" style="padding: 10px 0;">
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
                                                <#if postsBackup?size gt 0>
                                                    <#list postsBackup as post>
                                                        <tr>
                                                            <td>${post.fileName}</td>
                                                            <td>${post.createAt?string("yyyy-MM-dd HH:mm")}</td>
                                                            <td>${post.fileSize}</td>
                                                            <td>${post.fileType}</td>
                                                            <td>
                                                                <a href="/backup/posts/${post.fileName}" class="btn btn-xs btn-primary" download="${post.fileName}">下载</a>
                                                                <button type="button" class="btn btn-xs btn-info" onclick="sendToEmail('${post.fileName}','${post.backupType}')">发送到邮箱</button>
                                                                <button type="button" class="btn btn-xs btn-danger" onclick="delBackup('${post.fileName}','${post.backupType}')">删除</button>
                                                            </td>
                                                        </tr>
                                                    </#list>
                                                <#else>
                                                <tr>
                                                    <td colspan="5" style="text-align: center">暂无备份</td>
                                                </tr>
                                                </#if>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="btn_backup('posts')">备份</button>
                                    </div>
                                </form>
                            </div>
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
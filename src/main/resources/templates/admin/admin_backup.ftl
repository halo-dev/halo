<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.backup.title' /></@head>
<div class="content-wrapper">
    <style type="text/css" rel="stylesheet">
        .resourceType,.databaseType,.postType{list-style:none;float:left;margin:0;padding-bottom:10px}
    </style>
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.backup.title' /></h1>
        <a class="btn-header" id="btnBackupOption" href="#">
            <@spring.message code='admin.backup.text.setting' />
        </a>
        <ol class="breadcrumb">
            <li>
                <a href="/admin"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a href="#"><@spring.message code='admin.backup.bread.setting' /></a></li>
            <li class="active"><@spring.message code='admin.backup.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-lg-12 col-xs-12" id="backupOptionsPanel" style="display: none">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.backup.text.setting' /></h3>
                    </div>
                    <form class="form-horizontal" id="backupOption" method="post" action="/admin/backup/backupOption">
                        <div class="box-body">
                            <div class="col-sm-6 col-xs-6">
                                <div class="form-group">
                                    <label for="autoBackup" class="col-sm-4 control-label"><@spring.message code='admin.backup.form.auto-backup' /></label>
                                    <div class="col-sm-8 control-radio">
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="auto_backup" id="autoBackup" value="true" ${((options.auto_backup!)=='true')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.enable' /></label>
                                            </div>
                                        </div>
                                        <div class="pretty p-default p-round">
                                            <input type="radio" name="auto_backup" id="autoBackup" value="false" ${((options.auto_backup!'false')=='false')?string('checked','')}>
                                            <div class="state p-primary">
                                                <label><@spring.message code='common.radio.disable' /></label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="box-footer">
                            <button type="submit" class="btn btn-primary pull-right" ><@spring.message code='common.btn.save' /></button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="col-xs-12">
                <ul style="list-style: none;padding-left: 0">
                    <li class="resourceType">
                        <a data-pjax="true" href="/admin/backup?type=resources" <#if type=='resources'>style="color: #000" </#if>><@spring.message code='admin.backup.item.resources' /></a>&nbsp;|&nbsp;
                    </li>
                    <li class="databaseType">
                        <a data-pjax="true" href="/admin/backup?type=databases" <#if type=='databases'>style="color: #000" </#if>><@spring.message code='admin.backup.item.database' /></a>&nbsp;|&nbsp;
                    </li>
                    <li class="postType">
                        <a data-pjax="true" href="/admin/backup?type=posts" <#if type=='posts'>style="color: #000" </#if>><@spring.message code='admin.backup.item.posts' /></a>
                    </li>
                </ul>
            </div>
            <div class="col-xs-12">
                <div class="box box-primary">
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.file-name' /></th>
                                    <th><@spring.message code='common.th.date' /></th>
                                    <th><@spring.message code='common.th.file-size' /></th>
                                    <th><@spring.message code='common.th.file-type' /></th>
                                    <th><@spring.message code='common.th.control' /></th>
                                </tr>
                                <#if backups?size gt 0>
                                    <#list backups?sort_by("createAt")?reverse as backup>
                                        <tr>
                                            <td>${backup.fileName}</td>
                                            <td>${backup.createAt?string("yyyy-MM-dd HH:mm")}</td>
                                            <td>${backup.fileSize}</td>
                                            <td>${backup.fileType}</td>
                                            <td>
                                                <a href="/backup/${type}/${backup.fileName}" class="btn btn-xs btn-primary" download="${backup.fileName}"><@spring.message code='admin.backup.btn.download' /></a>
                                                <button type="button" class="btn btn-xs btn-info" onclick="sendToEmail('${backup.fileName}','${backup.backupType}')"><@spring.message code='admin.backup.btn.send-to-email' /></button>
                                                <button type="button" class="btn btn-xs btn-danger" onclick="delBackup('${backup.fileName}','${backup.backupType}')"><@spring.message code='admin.backup.btn.delete' /></button>
                                            </td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <th colspan="5" style="text-align: center"><@spring.message code='common.text.no-data' /></th>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="box-footer clearfix">
                        <button type="button" class="btn btn-primary btn-sm " onclick="btn_backup('${type}')"><@spring.message code='admin.backup.btn.backup' /></button>
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
                        halo.showMsgAndReload(data.msg,'success',1000);
                    }else{
                        halo.showMsg(data.msg,'error',2000);
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
                        halo.showMsg(data.msg,'success',1000);
                    }else{
                        halo.showMsg(data.msg,'error',2000);
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
                        halo.showMsgAndReload(data.msg,'success',1000);
                    }else{
                        halo.showMsg(data.msg,'error',2000);
                    }
                }
            });
        }

        $('#btnBackupOption').click(function () {
            $('#backupOptionsPanel').slideToggle(400);
        });
    </script>
</div>
<@footer></@footer>
</#compress>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/halo-backend/plugins/bootstrap/css/bootstrap.min.css">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-12 attachDesc" style="padding-top: 15px;">
            <div class="box box-solid">
                <div class="box-body table-responsive no-padding">
                    <table class="table table-hover">
                        <tbody>
                            <tr>
                                <th><@spring.message code='admin.logs.th.log-id' /></th>
                                <th><@spring.message code='admin.logs.th.log-action' /></th>
                                <th><@spring.message code='admin.logs.th.log-result' /></th>
                                <th>IP</th>
                                <th><@spring.message code='admin.logs.th.log-datetime' /></th>
                            </tr>
                            <#list logs.content as log>
                                <tr>
                                    <td>${log.logId}</td>
                                    <td>${log.logTitle}</td>
                                    <td>${log.logContent}</td>
                                    <td>${log.logIp}</td>
                                    <td>${log.logCreated}</td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
                <div class="box-footer clearfix">
                    <div class="no-margin pull-left">
                        <@spring.message code='admin.pageinfo.text.no' />${logs.number+1}/${logs.totalPages}<@spring.message code='admin.pageinfo.text.page' />
                    </div>
                    <div class="btn-group pull-right btn-group-sm" role="group">
                        <a class="btn btn-default <#if !logs.hasPrevious()>disabled</#if>" href="/admin/logs">
                            <@spring.message code='admin.pageinfo.btn.first' />
                        </a>
                        <a class="btn btn-default <#if !logs.hasPrevious()>disabled</#if>" href="/admin/logs?page=${logs.number-1}">
                            <@spring.message code='admin.pageinfo.btn.pre' />
                        </a>
                        <a class="btn btn-default <#if !logs.hasNext()>disabled</#if>" href="/admin/logs?page=${logs.number+1}">
                            <@spring.message code='admin.pageinfo.btn.next' />
                        </a>
                        <a class="btn btn-default <#if !logs.hasNext()>disabled</#if>" href="/admin/logs?page=${logs.totalPages-1}">
                            <@spring.message code='admin.pageinfo.btn.last' />
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/static/halo-common/jquery/jquery.min.js"></script>
<script src="/static/halo-backend/plugins/bootstrap/js/bootstrap.min.js"></script>
</html>

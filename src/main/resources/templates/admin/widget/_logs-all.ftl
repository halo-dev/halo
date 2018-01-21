<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="/static/plugins/bootstrap/css/bootstrap.min.css">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-12 attachDesc" style="padding-top: 15px;">
            <div class="box box-solid">
                <div class="box-body table-responsive">
                    <table class="table table-bordered table-hover">
                        <thead>
                            <tr>
                                <th>日志编号</th>
                                <th>触发事件</th>
                                <th>产生结果</th>
                                <th>IP</th>
                                <th>产生时间</th>
                            </tr>
                        </thead>
                        <tbody>
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
                        第${logs.number+1}/${logs.totalPages}页
                    </div>
                    <div class="no-margin pull-right">
                        <ul class="pagination" style="margin: 0;">
                            <li><a class="btn btn-sm <#if !logs.hasPrevious()>disabled</#if>" href="/admin/logs">首页</a> </li>
                            <li><a class="btn btn-sm <#if !logs.hasPrevious()>disabled</#if>" href="/admin/logs?page=${logs.number-1}">上页</a></li>
                            <li><a class="btn btn-sm <#if !logs.hasNext()>disabled</#if>" href="/admin/logs?page=${logs.number+1}">下页</a></li>
                            <li><a class="btn btn-sm <#if !logs.hasNext()>disabled</#if>" href="/admin/logs?page=${logs.totalPages-1}">尾页</a> </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script src="/static/plugins/jquery/jquery.min.js"></script>
<script src="/static/plugins/bootstrap/js/bootstrap.min.js"></script>
<script>
</script>
</html>
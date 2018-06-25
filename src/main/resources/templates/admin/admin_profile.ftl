<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：个人资料"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style>
            .form-horizontal .control-label{
                text-align: left;
            }
        </style>
        <section class="content-header">
            <h1>
                个人资料
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin">
                        <i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a data-pjax="true" href="#">用户</a></li>
                <li class="active">个人资料</li>
            </ol>
        </section>
        <!-- tab选项卡 -->
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-12">
                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs">
                            <li class="active">
                                <a href="#general" data-toggle="tab">基本资料</a>
                            </li>
                            <li>
                                <a href="#pass" data-toggle="tab">密码修改</a>
                            </li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="general">
                                <form method="post" class="form-horizontal" id="profileForm">
                                    <input type="hidden" name="userId" value="${user.userId?if_exists}">
                                    <input type="hidden" id="userPass" name="userPass" value="${user.userPass?if_exists}">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="userName" class="col-sm-2 control-label">用户名：
                                                <span data-toggle="tooltip" data-placement="top" title="用于登录" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-sm-4">
                                                <input type="text" class="form-control" id="userName" name="userName" value="${user.userName?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userDisplayName" class="col-sm-2 control-label">显示名称：
                                                <span data-toggle="tooltip" data-placement="top" title="页面显示的名称" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-sm-4">
                                                <input type="text" class="form-control" id="userDisplayName" name="userDisplayName" value="${user.userDisplayName?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userEmail" class="col-sm-2 control-label">邮箱：</label>
                                            <div class="col-sm-4">
                                                <input type="email" class="form-control" id="userEmail" name="userEmail" value="${user.userEmail?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userAvatar" class="col-sm-2 control-label">头像：</label>
                                            <div class="col-sm-4">
                                                <div class="input-group">
                                                    <input type="text" class="form-control" id="userAvatar" name="userAvatar" value="${user.userAvatar?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " type="button" onclick="openAttach('userAvatar')">选择</button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userDesc" class="col-sm-2 control-label">个人说明：
                                                <span data-toggle="tooltip" data-placement="top" title="部分主题可在页面上显示这段话" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-sm-4">
                                                <textarea class="form-control" rows="3" id="userDesc" name="userDesc" style="resize: none">${user.userDesc?if_exists}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveUser('profileForm')">保存</button>
                                    </div>
                                </form>
                            </div>
                            <div class="tab-pane" id="pass">
                                <form method="post" class="form-horizontal" id="passForm">
                                    <input type="hidden" name="userId" value="${user.userId?c}">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="beforePass" class="col-sm-2 control-label">原密码：</label>
                                            <div class="col-sm-4">
                                                <input type="password" class="form-control" id="beforePass" name="beforePass">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="newPass" class="col-sm-2 control-label">新密码：</label>
                                            <div class="col-sm-4">
                                                <input type="password" class="form-control" id="newPass" name="newPass">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="reNewPass" class="col-sm-2 control-label">确认密码：</label>
                                            <div class="col-sm-4">
                                                <input type="password" class="form-control" id="reNewPass" name="reNewPass">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="changPass()">修改</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <script src="/static/plugins/layer/layer.js"></script>
        <@compress single_line=true>
        <script>
            $(function () {
                $('[data-toggle="tooltip"]').tooltip()
            });
            function openAttach(id) {
                layer.open({
                    type: 2,
                    title: '所有附件',
                    shadeClose: true,
                    shade: 0.5,
                    maxmin: true,
                    area: ['90%', '90%'],
                    content: '/admin/attachments/select?id='+id,
                    scrollbar: false
                });
            }
            function saveUser(option) {
                var param = $('#'+option).serialize();
                $.ajax({
                    type: 'post',
                    url: '/admin/profile/save',
                    data: param,
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
                            showMsg(data.msg,"error",2000);
                        }
                    }
                });
            }
            function changPass() {
                var beforePass = $('#beforePass').val();
                var newPass = $('#newPass').val();
                var reNewPass = $('#reNewPass').val();
                if(beforePass==""||newPass==""||reNewPass==""){
                    showMsg("请输入完整信息！","info",2000);
                    return;
                }
                if(newPass!=reNewPass){
                    showMsg("两次密码不一样！","error",2000);
                    return;
                }
                var param = $('#passForm').serialize();
                $.ajax({
                    type: 'post',
                    url: '/admin/profile/changePass',
                    data: param,
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
                            showMsg(data.msg,"error",2000);
                        }
                    }
                });
            }
        </script>
        </@compress>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>

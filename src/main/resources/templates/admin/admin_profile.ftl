<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.user.profile.title' /></@head>
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
                <@spring.message code='admin.user.profile.title' />
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin">
                        <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li><a data-pjax="true" href="#"><@spring.message code='admin.user.profile.bread.user' /></a></li>
                <li class="active"><@spring.message code='admin.user.profile.title' /></li>
            </ol>
        </section>
        <!-- tab选项卡 -->
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-12">
                    <div class="nav-tabs-custom">
                        <ul class="nav nav-tabs">
                            <li class="active">
                                <a href="#general" data-toggle="tab"><@spring.message code='admin.user.profile.tab.basic' /></a>
                            </li>
                            <li>
                                <a href="#pass" data-toggle="tab"><@spring.message code='admin.user.profile.tab.password-change' /></a>
                            </li>
                        </ul>
                        <div class="tab-content">
                            <div class="tab-pane active" id="general">
                                <form method="post" class="form-horizontal" id="profileForm">
                                    <input type="hidden" name="userId" value="${user.userId?if_exists}">
                                    <input type="hidden" id="userPass" name="userPass" value="${user.userPass?if_exists}">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="userName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.user.profile.form.username' />
                                                <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.user.profile.form.username.tips' />" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="userName" name="userName" value="${user.userName?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userDisplayName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.user.profile.form.display-name' />
                                                <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.user.profile.form.display-name.tips' />" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="userDisplayName" name="userDisplayName" value="${user.userDisplayName?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userEmail" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.user.profile.form.email' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="email" class="form-control" id="userEmail" name="userEmail" value="${user.userEmail?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userAvatar" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.user.profile.form.avatar' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="text" class="form-control" id="userAvatar" name="userAvatar" value="${user.userAvatar?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " type="button" onclick="openAttach('userAvatar')"><@spring.message code='common.btn.choose' /></button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="userDesc" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.user.profile.form.desc' />
                                                <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.user.profile.form.desc.tips' />" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <textarea class="form-control" rows="3" id="userDesc" name="userDesc" style="resize: none">${user.userDesc?if_exists}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="saveUser('profileForm')"><@spring.message code='common.btn.save' /></button>
                                    </div>
                                </form>
                            </div>
                            <div class="tab-pane" id="pass">
                                <form method="post" class="form-horizontal" id="passForm">
                                    <input type="hidden" name="userId" value="${user.userId?c}">
                                    <div class="box-body">
                                        <div class="form-group">
                                            <label for="beforePass" class="col-sm-2 control-label"><@spring.message code='admin.user.profile.form.old-password' /></label>
                                            <div class="col-sm-4">
                                                <input type="password" class="form-control" id="beforePass" name="beforePass">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="newPass" class="col-sm-2 control-label"><@spring.message code='admin.user.profile.form.new-password' /></label>
                                            <div class="col-sm-4">
                                                <input type="password" class="form-control" id="newPass" name="newPass">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="reNewPass" class="col-sm-2 control-label"><@spring.message code='admin.user.profile.form.confirm-password' /></label>
                                            <div class="col-sm-4">
                                                <input type="password" class="form-control" id="reNewPass" name="reNewPass">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="box-footer">
                                        <button type="button" class="btn btn-primary btn-sm " onclick="changPass()"><@spring.message code='common.btn.modify' /></button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <@compress single_line=true>
        <script>
            $(function () {
                $('[data-toggle="tooltip"]').tooltip()
            });
            function openAttach(id) {
                layer.open({
                    type: 2,
                    title: '<@spring.message code="common.js.all-attachment" />',
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
                    showMsg("<@spring.message code='common.js.info-no-complete' />","info",2000);
                    return;
                }
                if(newPass!=reNewPass){
                    showMsg("<@spring.message code='admin.user.profile.form.password.no-same' />","error",2000);
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

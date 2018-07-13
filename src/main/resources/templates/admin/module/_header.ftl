<header class="main-header">
    <a href="/admin" class="logo">
        <span class="logo-mini"><b>H</b>a</span>
        <span class="logo-lg"><b>Ha</b>lo</span>
    </a>
    <nav class="navbar navbar-static-top" role="navigation">
        <a href="#" class="sidebar-toggle" data-toggle="push-menu" role="button">
            <span class="sr-only">Toggle navigation</span>
        </a>
        <div class="navbar-custom-menu">
            <ul class="nav navbar-nav">
                <li><a href="/" title="跳转到前台" target="_blank"><i class="fa fa-location-arrow"></i></a></li>
                <@commonTag method="newComments">
                <li class="dropdown messages-menu">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="fa fa-envelope-o"></i>
                        <#if newComments?size gt 0>
                            <span class="label label-success">${newComments?size}</span>
                        </#if>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="header">你有${newComments?size}条新评论</li>
                        <li>
                            <ul class="menu">
                                <#if newComments?size gt 0>
                                <#assign x=0>
                                <#list newComments?sort_by("commentDate")?reverse as comment>
                                <#assign x = x+1>
                                    <li>
                                        <a href="/admin/comments?status=1">
                                            <div class="pull-left">
                                                <img src="//gravatar.loli.net/avatar/${comment.commentAuthorAvatarMd5?default("hash")}?s=256&d=${options.native_comment_avatar?default("mm")}" class="img-circle" alt="User Image">
                                            </div>
                                            <h4>
                                                ${comment.commentAuthor}
                                                <small> ${comment.commentDate?string("yyyy/MM/dd HH:mm")}</small>
                                            </h4>
                                            <object>${comment.commentContent}</object>
                                        </a>
                                    </li>
                                <#if x==10>
                                    <#break >
                                </#if>
                                </#list>
                                </#if>
                            </ul>
                        </li>
                        <li class="footer"><a href="/admin/comments?status=1">查看所有评论</a></li>
                    </ul>
                </li>
                </@commonTag>
                <li class="dropdown user user-menu">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <img src="<#if user_session.userAvatar?if_exists!="">${user_session.userAvatar}<#else >/static/images/default.png</#if>" class="user-image" alt="User Image">
                        <span class="hidden-xs">${user_session.userDisplayName?if_exists}</span>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="user-header">
                            <img src="<#if user_session.userAvatar?if_exists!="">${user_session.userAvatar}<#else >/static/images/default.png</#if>" class="img-circle" alt="User Image">
                            <p>${user_session.userDisplayName?if_exists}</p>
                        </li>
                        <li class="user-footer">
                            <div class="pull-left"><a data-pjax="true" href="/admin/profile" class="btn btn-default "><i class="fa fa-user"></i>个人资料</a></div>
                            <div class="pull-right"><a href="/admin/logOut" class="btn btn-default "><i class="fa fa-sign-out"></i>退出登录</a></div>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </nav>
</header>
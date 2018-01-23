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
                                <#list newComments! as comment>
                                    <li>
                                        <a href="/admin/comments?status=1">
                                            <div class="pull-left">
                                                <img src="/static/images/ryan0up.png" class="img-circle" alt="User Image">
                                            </div>
                                            <h4>${comment.commentAuthor}<small>${comment.commentDate}</small></h4>
                                            <p>${comment.commentContent}</p>
                                        </a>
                                    </li>
                                </#list>
                            </ul>
                        </li>
                        <li class="footer"><a href="/admin/comments">查看所有评论</a></li>
                    </ul>
                </li>
                <li class="dropdown user user-menu">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <img src="<#if user.userAvatar?if_exists!="">${user.userAvatar}<#else >/static/images/default.png</#if>" class="user-image" alt="User Image">
                        <span class="hidden-xs">${user.userDisplayName?if_exists}</span>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="user-header">
                            <img src="<#if user.userAvatar?if_exists!="">${user.userAvatar}<#else >/static/images/default.png</#if>" class="img-circle" alt="User Image">
                            <p>${user.userDisplayName?if_exists}</p>
                        </li>
                        <li class="user-footer">
                            <div class="pull-left"><a href="/admin/profile" class="btn btn-default btn-flat">个人资料</a></div>
                            <div class="pull-right"><a href="/admin/logOut" class="btn btn-default btn-flat">退出登录</a></div>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>
    </nav>
</header>
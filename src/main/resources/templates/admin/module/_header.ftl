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
                        <span class="label label-success">4</span>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="header">你有4条新评论</li>
                        <li>
                            <ul class="menu">
                                <#list postTopFive! as post>
                                    <li>
                                        <a href="#">
                                            <div class="pull-left">
                                                <img src="/static/images/ryan0up.png" class="img-circle" alt="User Image">
                                            </div>
                                            <h4>Support Team<small><i class="fa fa-clock-o"></i> 5 mins</small></h4>
                                            <p>${post.postTitle!}</p>
                                        </a>
                                    </li>
                                </#list>
                            </ul>
                        </li>
                        <li class="footer"><a href="#">查看所有评论</a></li>
                    </ul>
                </li>
                <li class="dropdown user user-menu">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <img src="<#if user.userAvatar!="">${user.userAvatar?if_exists}<#else >/static/images/default.png</#if>" class="user-image" alt="User Image">
                        <span class="hidden-xs">${user.userDisplayName?if_exists}</span>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="user-header">
                            <img src="<#if user.userAvatar!="">${user.userAvatar?if_exists}<#else >/static/images/default.png</#if>" class="img-circle" alt="User Image">
                            <p>${user.userDisplayName?if_exists}-${user.userDesc?if_exists}<small></small></p>
                        </li>
                        <li class="user-footer">
                            <div class="pull-left"><a href="/admin/profile" class="btn btn-default btn-flat">个人资料</a></div>
                            <div class="pull-right"><a href="/admin/logOut" class="btn btn-default btn-flat">退出登录</a></div>
                        </li>
                    </ul>
                </li>
                <!-- 工具栏 TODO: 暂时先不做 -->
                <!--
                <li><a href="#" data-toggle="control-sidebar"><i class="fa fa-gears"></i></a></li>
                -->
            </ul>
        </div>
    </nav>
</header>
<aside class="main-sidebar">
    <section class="sidebar">
        <div class="user-panel">
            <div class="pull-left image">
                <img src="<#if user_session.userAvatar?if_exists!="">${user_session.userAvatar?if_exists}<#else >/static/images/default.png</#if>" class="img-circle" alt="User Image">
            </div>
            <div class="pull-left info">
                <p>${user_session.userDisplayName?if_exists}</p><a href="/admin/profile"><i class="fa fa-circle text-success"></i>编辑</a>
            </div>
        </div>
        <form action="#" method="get" class="sidebar-form">
            <div class="input-group">
                <input type="text" name="q" class="form-control" placeholder="Search...">
                <span class="input-group-btn">
                  <button type="submit" name="search" id="search-btn" class="btn btn-flat">
                    <i class="fa fa-search"></i>
                  </button>
                </span>
            </div>
        </form>
        <ul class="sidebar-menu" data-widget="tree">
            <li class="header">HEADER</li>
            <li>
                <a data-pjax="true" href="/admin">
                    <i class="fa fa-dashboard"></i>
                    <span>仪表盘</span>
                </a>
            </li>
            <li class="treeview">
                <a data-pjax="true" href="#">
                    <i class="fa  fa-book"></i>
                    <span>文章</span>
                    <span class="pull-right-container"><i class="fa fa-angle-left pull-right"></i></span>
                </a>
                <ul class="treeview-menu" style="">
                    <li><a data-pjax="true" href="/admin/posts"><i class="fa fa-circle-o"></i>所有文章</a></li>
                    <li><a data-pjax="false" href="/admin/posts/new"><i class="fa fa-circle-o"></i>写文章</a></li>
                    <li><a data-pjax="true" href="/admin/category"><i class="fa fa-circle-o"></i>分类目录</a></li>
                    <li><a data-pjax="true" href="/admin/tag"><i class="fa fa-circle-o"></i>标签</a></li>
                </ul>
            </li>
            <li class="treeview">
                <a data-pjax="true" href="#">
                    <i class="fa fa-desktop"></i>
                    <span>页面</span>
                    <span class="pull-right-container">
                        <i class="fa fa-angle-left pull-right"></i>
                    </span>
                </a>
                <ul class="treeview-menu">
                    <li><a data-pjax="true" href="/admin/page"><i class="fa fa-circle-o"></i>所有页面</a></li>
                    <li><a data-pjax="false" href="/admin/page/new"><i class="fa fa-circle-o"></i>新建页面</a></li>
                </ul>
            </li>
            <li>
                <a data-pjax="true" href="/admin/attachments">
                    <i class="fa fa-camera"></i>
                    <span>附件</span>
                </a>
            </li>
            <li>
                <a data-pjax="true" href="/admin/comments">
                    <i class="fa fa-comment"></i>
                    <span>评论</span>
                    <span class="pull-right-container">
                        <#if newComments?size gt 0>
                            <span class="label label-primary pull-right">${newComments?size}</span>
                        </#if>
                    </span>
                </a>
            </li>
            <li class="treeview">
                <a data-pjax="true" href="#">
                    <i class="fa fa-paint-brush"></i>
                    <span>外观</span>
                    <span class="pull-right-container">
                        <i class="fa fa-angle-left pull-right"></i>
                    </span>
                </a>
                <ul class="treeview-menu">
                    <li><a data-pjax="true" href="/admin/themes"><i class="fa fa-circle-o"></i>主题</a></li>
                    <li><a data-pjax="true" href="/admin/menus"><i class="fa fa-circle-o"></i>菜单</a></li>
                    <li><a data-pjax="false" href="/admin/themes/editor"><i class="fa fa-circle-o"></i>主题编辑</a></li>
                </ul>
            </li>
            <li class="treeview">
                <a data-pjax="true" href="#">
                    <i class="fa fa-user-o"></i>
                    <span>用户</span>
                    <span class="pull-right-container"><i class="fa fa-angle-left pull-right"></i></span>
                </a>
                <ul class="treeview-menu">
                    <li><a data-pjax="true" href="/admin/profile"><i class="fa fa-circle-o"></i>个人资料</a></li>
                </ul>
            </li>
            <li class="treeview">
                <a data-pjax="true" href="#">
                    <i class="fa fa-cog"></i>
                    <span>设置</span>
                    <span class="pull-right-container"><i class="fa fa-angle-left pull-right"></i></span>
                </a>
                <ul class="treeview-menu">
                    <li><a data-pjax="true" href="/admin/option"><i class="fa fa-circle-o"></i>博客设置</a></li>
                    <li><a data-pjax="true" href="/admin/backup"><i class="fa fa-circle-o"></i>博客备份</a></li>
                </ul>
            </li>
        </ul>
    </section>
</aside>

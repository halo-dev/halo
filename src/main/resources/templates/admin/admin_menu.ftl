<#compress >
<#include "module/_macro.ftl">
<@head title="${options.blog_title} | 后台管理：菜单设置"></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <section class="content-header">
            <h1 style="display: inline-block;">菜单管理</h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin"><i class="fa fa-dashboard"></i> 首页</a>
                </li>
                <li><a data-pjax="true" href="#">外观</a></li>
                <li class="active">菜单管理</li>
            </ol>
        </section>
        <section class="content container-fluid">
            <div class="row">
                <div class="col-md-5">
                    <div class="box box-primary">
                        <#if updateMenu??>
                            <div class="box-header with-border">
                                <h3 class="box-title">修改菜单<#if updateMenu??>[${updateMenu.menuName}]</#if></h3>
                            </div>
                            <form action="/admin/menus/save" method="post" role="form" id="menuAddForm">
                                <input type="hidden" name="menuId" value="${updateMenu.menuId?c}">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="menuName">名称</label>
                                        <input type="text" class="form-control" id="menuName" name="menuName" value="${updateMenu.menuName}">
                                        <small>页面上所显示的名称</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="menuUrl">路径</label>
                                        <input type="text" class="form-control" id="menuUrl" name="menuUrl" value="${updateMenu.menuUrl}">
                                        <small>*菜单的路径</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="menuSort">排序编号</label>
                                        <input type="number" class="form-control" id="menuSort" name="menuSort" value="${updateMenu.menuSort}">
                                    </div>
                                    <div class="form-group">
                                        <label for="menuIcon">图标</label>
                                        <input type="text" class="form-control" id="menuIcon" name="menuIcon" value="${updateMenu.menuIcon}">
                                        <small>*请根据主题的支持选填</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="menuIcon">打开方式</label>
                                        <select class="form-control" id="menuTarget" name="menuTarget">
                                            <option value="_self" ${((updateMenu.menuTarget?default("_self")=="_self")?string('selected',''))}>当前窗口</option>
                                            <option value="_blank" ${((updateMenu.menuTarget?if_exists=="_blank")?string('selected',''))}>新窗口</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary btn-sm ">确定修改</button>
                                    <a data-pjax="true" href="/admin/menus" class="btn btn-info btn-sm ">返回添加</a>
                                </div>
                            </form>
                        <#else >
                            <div class="box-header with-border">
                                <h3 class="box-title">添加菜单</h3>
                            </div>
                            <form action="/admin/menus/save" method="post" role="form" id="menuAddForm">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="menuName">名称</label>
                                        <input type="text" class="form-control" id="menuName" name="menuName">
                                        <small>页面上所显示的名称</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="menuUrl">路径</label>
                                        <input type="text" class="form-control" id="menuUrl" name="menuUrl">
                                        <small>*菜单的路径</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="menuSort">排序编号</label>
                                        <input type="text" class="form-control" id="menuSort" name="menuSort">
                                    </div>
                                    <div class="form-group">
                                        <label for="menuIcon">图标</label>
                                        <input type="text" class="form-control" id="menuIcon" name="menuIcon">
                                        <small>*请根据主题的支持选填</small>
                                    </div>
                                    <div class="form-group">
                                        <label for="menuIcon">打开方式</label>
                                        <select class="form-control" id="menuTarget" name="menuTarget">
                                            <option value="_self">当前窗口</option>
                                            <option value="_blank">新窗口</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="submit" class="btn btn-primary btn-sm ">确定添加</button>
                                </div>
                            </form>
                        </#if>
                    </div>
                </div>
                <div class="col-md-7">
                    <div class="box box-primary">
                        <div class="box-header with-border">
                            <h3 class="box-title">所有菜单</h3>
                        </div>
                        <div class="box-body table-responsive">
                            <table class="table table-hover">
                                <thead>
                                <tr>
                                    <th>名称</th>
                                    <th>路径</th>
                                    <th>排序</th>
                                    <th>图标</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                <@commonTag method="menus">
                                    <#if menus?? && menus?size gt 0>
                                        <#list menus as menu>
                                            <tr>
                                                <td>${menu.menuName}</td>
                                                <td>${menu.menuUrl}</td>
                                                <td>${(menu.menuSort)!}</td>
                                                <td>${menu.menuIcon}</td>
                                                <td>
                                                    <#if updateMenu?? && menu.menuId?c==updateMenu.menuId?c>
                                                        <a href="#" class="btn btn-primary btn-xs " disabled="">正在修改</a>
                                                    <#else>
                                                        <a data-pjax="true" href="/admin/menus/edit?menuId=${menu.menuId?c}" class="btn btn-primary btn-xs ">修改</a>
                                                    </#if>
                                                    <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/menus/remove?menuId=${menu.menuId?c}')">删除</button>
                                                </td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </@commonTag>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        <!-- 删除确认弹出层 -->
        <div class="modal fade" id="removeMenuModal">
            <div class="modal-dialog">
                <div class="modal-content message_align">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                        <h4 class="modal-title">提示信息</h4>
                    </div>
                    <div class="modal-body"><p>您确认要删除吗？</p></div>
                    <div class="modal-footer">
                        <input type="hidden" id="url"/>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal">确定</a>
                    </div>
                </div>
            </div>
        </div>
        <script>
            function modelShow(url) {
                $('#url').val(url);
                $('#removeMenuModal').modal();
            }
            function removeIt(){
                var url=$.trim($("#url").val());
                window.location.href=url;
            }
        </script>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>
</#compress>

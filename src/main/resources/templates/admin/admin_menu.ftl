<#compress >
<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.menus.title' /></@head>
<div class="content-wrapper">
    <section class="content-header" id="animated-header">
        <h1 style="display: inline-block;"><@spring.message code='admin.menus.title' /></h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin/"><i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.themes.bread.appearance' /></a></li>
            <li class="active"><@spring.message code='admin.menus.title' /></li>
        </ol>
    </section>
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-md-5">
                <div class="box box-primary">
                    <#if updateMenu??>
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.menus.text.update-menu' /><#if updateMenu??>[${updateMenu.menuName}]</#if></h3>
                        </div>
                        <form role="form" id="menuSaveForm">
                            <input type="hidden" name="menuId" value="${updateMenu.menuId?c}">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="menuName"><@spring.message code='admin.menus.form.menu-name' /></label>
                                    <input type="text" class="form-control" id="menuName" name="menuName" value="${updateMenu.menuName!}">
                                    <small><@spring.message code='admin.menus.form.menu-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="menuUrl"><@spring.message code='admin.menus.form.menu-url' /></label>
                                    <input type="text" class="form-control" id="menuUrl" name="menuUrl" value="${updateMenu.menuUrl!}">
                                    <small><@spring.message code='admin.menus.form.menu-url-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="menuSort"><@spring.message code='admin.menus.form.menu-sort' /></label>
                                    <input type="number" class="form-control" id="menuSort" name="menuSort" value="${updateMenu.menuSort?c}">
                                </div>
                                <div class="form-group">
                                    <label for="menuIcon"><@spring.message code='admin.menus.form.menu-icon' /></label>
                                    <input type="text" class="form-control" id="menuIcon" name="menuIcon" value="${updateMenu.menuIcon!}">
                                    <small><@spring.message code='admin.menus.form.menu-icon-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="menuTarget"><@spring.message code='admin.menus.form.menu-target' /></label>
                                    <select class="form-control" id="menuTarget" name="menuTarget">
                                        <option value="_self" ${((updateMenu.menuTarget!'_self')=='_self')?string('selected','')}><@spring.message code='admin.menus.form.menu-target-self' /></option>
                                        <option value="_blank" ${((updateMenu.menuTarget!)=='_blank')?string('selected','')}><@spring.message code='admin.menus.form.menu-target-blank' /></option>
                                    </select>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm" onclick="save()"><@spring.message code='common.btn.define-edit' /></button>
                                <a data-pjax="true" href="/admin/menus" class="btn btn-info btn-sm "><@spring.message code='common.btn.back-to-add' /></a>
                            </div>
                        </form>
                    <#else >
                        <div class="box-header with-border">
                            <h3 class="box-title"><@spring.message code='admin.menus.text.add-menu' /></h3>
                        </div>
                        <form role="form" id="menuSaveForm">
                            <div class="box-body">
                                <div class="form-group">
                                    <label for="menuName"><@spring.message code='admin.menus.form.menu-name' /></label>
                                    <input type="text" class="form-control" id="menuName" name="menuName">
                                    <small><@spring.message code='admin.menus.form.menu-name-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="menuUrlType">路径类型：</label>
                                    <select class="form-control" id="menuUrlType" name="menuUrlType" onchange="urlTypeChoice()">
                                        <option value="0">自定义</option>
                                        <option value="1">预设</option>
                                    </select>
                                </div>
                                <div class="form-group" id="customUrlGroup">
                                    <label for="menuCustomUrl"><@spring.message code='admin.menus.form.menu-url' /></label>
                                    <input type="text" class="form-control" id="menuCustomUrl" name="menuUrl">
                                    <small><@spring.message code='admin.menus.form.menu-url-tips' /></small>
                                </div>
                                <div class="form-group" id="internalUrlGroup" style="display: none">
                                    <label for="menuInternalUrl"><@spring.message code='admin.menus.form.menu-url' /></label>
                                    <select class="form-control" id="menuInternalUrl" name="menuUrl" disabled="disabled">
                                        <optgroup label="内置页面">
                                            <option value="/">首页 ( / )</option>
                                            <option value="/archives">归档 ( /archives )</option>
                                            <option value="/links">友情链接 ( /links )</option>
                                            <option value="/gallery">图库 ( /gallery )</option>
                                            <option value="/categories">分类目录 ( /categories )</option>
                                            <option value="/tags">标签 ( /tags )</option>
                                        </optgroup>
                                        <#if posts?? && posts?size gt 0>
                                            <optgroup label="自定义页面">
                                                <#list posts as post>
                                                    <option value="/p/${post.postUrl!}">${post.postTitle!} ( /p/${post.postUrl!} )</option>
                                                </#list>
                                            </optgroup>
                                        </#if>
                                        <@commonTag method = "categories">
                                            <#if categories?? && categories?size gt 0>
                                                <optgroup label="分类目录">
                                                    <#list categories as cate>
                                                        <option value="/categories/${cate.cateUrl!}">${cate.cateName!} ( /categories/${cate.cateUrl!} )</option>
                                                    </#list>
                                                </optgroup>
                                            </#if>
                                        </@commonTag>
                                        <@commonTag method = "tags">
                                            <#if tags?? && tags?size gt 0>
                                                <optgroup label="标签">
                                                    <#list tags as tag>
                                                        <option value="/tags/${tag.tagName!}">${tag.tagName!} ( /tags/${tag.tagName!} )</option>
                                                    </#list>
                                                </optgroup>
                                            </#if>
                                        </@commonTag>
                                        <optgroup label="其他">
                                            <option value="/sitemap.xml">站点地图 ( /sitemap.xml )</option>
                                            <option value="/sitemap.html">站点地图 ( /sitemap.html )</option>
                                            <option value="/atom.xml">Atom 订阅 ( /atom.xml )</option>
                                            <option value="/rss.xml">Rss 订阅 ( /rss.xml )</option>
                                        </optgroup>
                                    </select>
                                    <small><@spring.message code='admin.menus.form.menu-url-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="menuSort"><@spring.message code='admin.menus.form.menu-sort' /></label>
                                    <input type="number" class="form-control" id="menuSort" name="menuSort">
                                </div>
                                <div class="form-group">
                                    <label for="menuIcon"><@spring.message code='admin.menus.form.menu-icon' /></label>
                                    <input type="text" class="form-control" id="menuIcon" name="menuIcon">
                                    <small><@spring.message code='admin.menus.form.menu-icon-tips' /></small>
                                </div>
                                <div class="form-group">
                                    <label for="menuTarget"><@spring.message code='admin.menus.form.menu-target' /></label>
                                    <select class="form-control" id="menuTarget" name="menuTarget">
                                        <option value="_self"><@spring.message code='admin.menus.form.menu-target-self' /></option>
                                        <option value="_blank"><@spring.message code='admin.menus.form.menu-target-blank' /></option>
                                    </select>
                                </div>
                            </div>
                            <div class="box-footer">
                                <button type="button" class="btn btn-primary btn-sm" onclick="save()"><@spring.message code='common.btn.define-add' /></button>
                            </div>
                        </form>
                    </#if>
                </div>
            </div>
            <div class="col-md-7">
                <div class="box box-primary">
                    <div class="box-header with-border">
                        <h3 class="box-title"><@spring.message code='admin.menus.text.all-menus' /></h3>
                    </div>
                    <div class="box-body table-responsive no-padding">
                        <table class="table table-hover">
                            <tbody>
                                <tr>
                                    <th><@spring.message code='common.th.name' /></th>
                                    <th><@spring.message code='common.th.url' /></th>
                                    <th><@spring.message code='common.th.sort' /></th>
                                    <th><@spring.message code='common.th.icon' /></th>
                                    <th><@spring.message code='common.th.name' /></th>
                                </tr>
                                <@commonTag method="menus">
                                    <#if menus?? && menus?size gt 0>
                                        <#list menus?sort_by('menuSort') as menu>
                                            <tr>
                                                <td>${menu.menuName!}</td>
                                                <td>${menu.menuUrl!}</td>
                                                <td>${menu.menuSort?c}</td>
                                                <td>${menu.menuIcon!}</td>
                                                <td>
                                                    <#if updateMenu?? && menu.menuId?c==updateMenu.menuId?c>
                                                        <a href="javascript:void(0)" class="btn btn-primary btn-xs " disabled=""><@spring.message code='common.btn.editing' /></a>
                                                    <#else>
                                                        <a data-pjax="true" href="/admin/menus/edit?menuId=${menu.menuId?c}" class="btn btn-primary btn-xs "><@spring.message code='common.btn.modify' /></a>
                                                    </#if>
                                                    <button class="btn btn-danger btn-xs " onclick="modelShow('/admin/menus/remove?menuId=${menu.menuId?c}')"><@spring.message code='common.btn.delete' /></button>
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
                    <h4 class="modal-title"><@spring.message code='common.text.tips' /></h4>
                </div>
                <div class="modal-body"><p><@spring.message code='common.text.define-delete' /></p></div>
                <div class="modal-footer">
                    <input type="hidden" id="url"/>
                    <button type="button" class="btn btn-default" data-dismiss="modal"><@spring.message code='common.btn.cancel' /></button>
                    <a onclick="removeIt()" class="btn btn-danger" data-dismiss="modal"><@spring.message code='common.btn.define' /></a>
                </div>
            </div>
        </div>
    </div>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    function modelShow(url) {
        $('#url').val(url);
        $('#removeMenuModal').modal();
    }
    function removeIt(){
        var url=$.trim($("#url").val());
        <#if (options.admin_pjax!'true') == 'true'>
            pjax.loadUrl(url);
        <#else>
            window.location.href = url;
        </#if>
    }

    function urlTypeChoice() {
        var customUrl = $("#menuCustomUrl");
        var internalUrl = $("#menuInternalUrl");
        var customUrlGroup = $("#customUrlGroup");
        var internalUrlGroup = $("#internalUrlGroup");
        if($("#menuUrlType").val() === "0"){
            customUrlGroup.show();
            customUrl.removeAttr("disabled");
            internalUrlGroup.hide();
            internalUrl.attr("disabled","disabled");
        }else{
            internalUrlGroup.show();
            internalUrl.removeAttr("disabled");
            customUrlGroup.hide();
            customUrl.attr("disabled","disabled");
        }
    }

    /**
     * 保存
     */
    function save() {
        var param = $('#menuSaveForm').serialize();
        $.post('/admin/menus/save', param, function (data) {
            if (data.code === 1) {
                halo.showMsgAndRedirect(data.msg, 'success', 1000,'/admin/menus',"${options.admin_pjax!'true'}");
            } else {
                halo.showMsg(data.msg, 'error', 2000);
            }
        }, 'JSON');
    }
</script>
</@footer>
</#compress>

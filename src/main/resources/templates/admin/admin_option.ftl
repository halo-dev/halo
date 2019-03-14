<#include "module/_macro.ftl">
<@head>${options.blog_title!} | <@spring.message code='admin.setting.title' /></@head>
<div class="content-wrapper">
    <style type="text/css" rel="stylesheet">
        .nav-tabs-custom > .nav-tabs > li.active {
            border-top-color: #d2d6de;
        }
    </style>
    <section class="content-header" id="animated-header">
        <h1>
            <@spring.message code='admin.setting.title' />
            <small></small>
        </h1>
        <ol class="breadcrumb">
            <li>
                <a data-pjax="true" href="/admin">
                    <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
            </li>
            <li><a data-pjax="true" href="javascript:void(0)"><@spring.message code='admin.setting.bread.setting' /></a></li>
            <li class="active"><@spring.message code='admin.setting.title' /></li>
        </ol>
    </section>
    <!-- tab选项卡 -->
    <section class="content container-fluid" id="animated-content">
        <div class="row">
            <div class="col-md-12">
                <div class="nav-tabs-custom">
                    <ul class="nav nav-tabs">
                        <li class="active">
                            <a href="#general" data-toggle="tab"><@spring.message code='admin.setting.tab.general' /></a>
                        </li>
                        <li>
                            <a href="#seo" data-toggle="tab"><@spring.message code='admin.setting.tab.seo' /></a>
                        </li>
                        <li>
                            <a href="#post" data-toggle="tab"><@spring.message code='admin.setting.tab.post' /></a>
                        </li>
                        <li>
                            <a href="#comment" data-toggle="tab"><@spring.message code='admin.setting.tab.comment' /></a>
                        </li>
                        <li>
                            <a href="#attach" data-toggle="tab"><@spring.message code='admin.setting.tab.attach' /></a>
                        </li>
                        <li>
                            <a href="#admin" data-toggle="tab"><@spring.message code='admin.setting.tab.admin' /></a>
                        </li>
                        <li>
                            <a href="#email" data-toggle="tab"><@spring.message code='admin.setting.tab.email' /></a>
                        </li>
                        <li>
                            <a href="#other" data-toggle="tab"><@spring.message code='admin.setting.tab.other' /></a>
                        </li>
                    </ul>
                    <!-- 基础设置 -->
                    <div class="tab-content">
                        <div class="tab-pane active" id="general">
                            <form method="post" class="form-horizontal" id="commonOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="blogLocale" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-locale' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <select class="form-control" id="blogLocale" name="blog_locale">
                                                <option value="zh_CN" ${((options.blog_locale!'zh_CN')=='zh_CN')?string('selected','')}>简体中文</option>
                                                <option value="en_US" ${((options.blog_locale!)=='en_US')?string('selected','')}>English</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogTitle" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-title' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="blogTitle" name="blog_title" value="${options.blog_title!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogUrl" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-url' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="url" class="form-control" id="blogUrl" name="blog_url" value="${options.blog_url!'http://localhost:8080'}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogLogo" class="col-lg-2 col-sm-4 control-label">LOGO：</label>
                                        <div class="col-lg-4 col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control selectData" id="blogLogo" name="blog_logo" value="${options.blog_logo!}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default btn-flat" type="button" onclick="halo.layerModal('/admin/attachments/select?id=blogLogo','<@spring.message code="common.js.all-attachment" />')"><@spring.message code='common.btn.choose' /></button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogFavicon" class="col-lg-2 col-sm-4 control-label">Favicon：</label>
                                        <div class="col-lg-4 col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control selectData" id="blogFavicon" name="blog_favicon" value="${options.blog_favicon!}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default btn-flat" type="button" onclick="halo.layerModal('/admin/attachments/select?id=blogFavicon','<@spring.message code="common.js.all-attachment" />')"><@spring.message code='common.btn.choose' /></button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogFooterInfo" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-footer-info' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.blog-footer-info-tips' />" style="cursor: pointer">
                                                <i class="fa fa-question-circle" aria-hidden="true"></i>
                                            </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <textarea class="form-control" rows="5" id="blogFooterInfo" name="blog_footer_info" style="resize: none">${options.blog_footer_info!}</textarea>
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('commonOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>
                        <!-- seo设置 -->
                        <div class="tab-pane" id="seo">
                            <form method="post" class="form-horizontal" id="seoOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="keywords" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.seo-keywords' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.seo-keywords-tip' />" style="cursor: pointer">
                                                <i class="fa fa-question-circle" aria-hidden="true"></i>
                                            </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="keywords" name="seo_keywords" value="${options.seo_keywords!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="desc" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.seo-desc' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="desc" name="seo_desc" value="${options.seo_desc!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="baiduToken" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.baidu-token' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.baidu-token-tips' />" style="cursor: pointer">
                                                <i class="fa fa-question-circle" aria-hidden="true"></i>
                                            </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control" id="baiduToken" name="seo_baidu_token" value="${options.seo_baidu_token!}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default btn-flat" id="btn_push_baidu" onclick="pushAllToBaidu()" type="button"><@spring.message code='admin.setting.form.baidu-token-btn-push' /></button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <#-- 站点验证代码 -->
                                    <div class="form-group">
                                        <label for="blogVerificationGoogle" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-google' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="blogVerificationGoogle" name="blog_verification_google" value="${options.blog_verification_google!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogVerificationBing" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-bing' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="blogVerificationBing" name="blog_verification_bing" value="${options.blog_verification_bing!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogVerificationBaidu" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-baidu' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="blogVerificationBaidu" name="blog_verification_baidu" value="${options.blog_verification_baidu!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="blogVerificationQihu" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-qihu' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="blogVerificationQihu" name="blog_verification_qihu" value="${options.blog_verification_qihu!}">
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('seoOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>
                        <!-- 文章设置 -->
                        <div class="tab-pane" id="post">
                            <form method="post" class="form-horizontal" id="postOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="indexPosts" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.index-posts' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.index-posts-tips' />" style="cursor: pointer">
                                                <i class="fa fa-question-circle" aria-hidden="true"></i>
                                            </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="number" class="form-control" id="indexPosts" name="index_posts" value="${options.index_posts!'10'}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="rssPosts" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.rss-posts' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="number" class="form-control" id="rssPosts" name="rss_posts" value="${options.rss_posts!}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="postSummary" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.post-summary' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <div class="input-group">
                                                <input type="number" class="form-control" id="postSummary" name="post_summary" value="${options.post_summary!'50'}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default btn-flat" id="btn_update_summary" onclick="updateAllSummary()" type="button"><@spring.message code='admin.setting.form.post-summary-btn-update' /></button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('postOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>

                        <!-- 评论设置 -->
                        <div class="tab-pane" id="comment">
                            <form method="post" class="form-horizontal" id="commentOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="nativeCommentAvatar" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.native-comment-avatar' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <select class="form-control" id="nativeCommentAvatar" name="native_comment_avatar">
                                                <option value="mm" ${((options.native_comment_avatar!'mm')=='mm')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-mm' /></option>
                                                <option value="identicon" ${((options.native_comment_avatar!)=='identicon')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-identicon' /></option>
                                                <option value="monsterid" ${((options.native_comment_avatar!)=='monsterid')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-monsterid' /></option>
                                                <option value="wavatar" ${((options.native_comment_avatar!)=='wavatar')?string('selected','')}>Wavatar</option>
                                                <option value="retro" ${((options.native_comment_avatar!)=='retro')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-retro' /></option>
                                                <option value="robohash" ${((options.native_comment_avatar!)=='robohash')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-robohash' /></option>
                                                <option value="blank" ${((options.native_comment_avatar!)=='blank')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-blank' /></option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-activate-power-mode' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_activate_power_mode" value="true" ${((options.comment_activate_power_mode!)=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_activate_power_mode" value="false" ${((options.comment_activate_power_mode!'false')=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.new-comment-need-check' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="new_comment_need_check" value="true" ${((options.new_comment_need_check!'true')=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="new_comment_need_check" value="false" ${((options.new_comment_need_check!)=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.new-comment-notice' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="new_comment_notice" value="true" ${((options.new_comment_notice!)=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="new_comment_notice" value="false" ${((options.new_comment_notice!)=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-pass-notice' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_pass_notice" value="true" ${((options.comment_pass_notice!)=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_pass_notice" value="false" ${((options.comment_pass_notice!)=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-reply-notice' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_reply_notice" value="true" ${((options.comment_reply_notice!)=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_reply_notice" value="false" ${((options.comment_reply_notice!)=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label">API 评论开关：</label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_api_switch" value="true" ${((options.comment_api_switch!)=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="comment_api_switch" value="false" ${((options.comment_api_switch!'false')=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="indexComments" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.index-comments' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.index-comments-tips' />" style="cursor: pointer">
                                                <i class="fa fa-question-circle" aria-hidden="true"></i>
                                            </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="number" class="form-control" id="indexComments" name="index_comments" value="${options.index_comments!'10'}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="nativeCommentPlaceholder" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.native-comment-placeholder' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="url" class="form-control" id="nativeCommentPlaceholder" name="native_comment_placeholder" value="${options.native_comment_placeholder!'赶快评论一个吧！'}">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="nativeCss" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-css' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-css-tips' />" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <textarea class="form-control" rows="5" id="nativeCss" name="native_css" style="resize: none">${options.native_css!}</textarea>
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('commentOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>
                        <!-- 附件设置 -->
                        <div class="tab-pane" id="attach">
                            <form method="post" class="form-horizontal" id="attachOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.attach-choose' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="attach_loc" value="server" ${((options.attach_loc!'server')=='server')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.attach-loc-server' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="attach_loc" value="upyun" ${((options.attach_loc!)=='upyun')?string('checked','')} >
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.attach-loc-upyun' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="attach_loc" value="qiniu" ${((options.attach_loc!)=='qiniu')?string('checked','')} >
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.attach-loc-qiniu' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 原生设置 -->
                                    <div class="server-options" style="display: none">

                                    </div>

                                    <!-- 又拍云选项 -->
                                    <div class="upyun-options" style="display: none">
                                        <div class="form-group">
                                            <label for="upyunOssDomain" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-domain' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="upyunOssDomain" name="upyun_oss_domain" value="${options.upyun_oss_domain!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="upyunOssBucket" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-bucket' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="upyunOssBucket" name="upyun_oss_bucket" value="${options.upyun_oss_bucket!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="upyunOssOperator" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-operator' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="upyunOssOperator" name="upyun_oss_operator" value="${options.upyun_oss_operator!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="upyunOssPwd" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-pwd' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="upyunOssPwd" name="upyun_oss_pwd" value="${options.upyun_oss_pwd!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="upyunOssSrc" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-src' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="upyunOssSrc" name="upyun_oss_src" value="${options.upyun_oss_src!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="upyunOssSmall" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-small' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="upyunOssSmall" name="upyun_oss_small" value="${options.upyun_oss_small!}">
                                            </div>
                                        </div>
                                    </div>

                                    <!-- 七牛云 -->
                                    <div class="qiniu-options" style="display: none">
                                        <div class="form-group">
                                            <label for="qiniuDomain" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.qiniu-domain' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="qiniuDomain" name="qiniu_domain" value="${options.qiniu_domain!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="qiniuAccessKey" class="col-lg-2 col-sm-4 control-label">Access Key：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="qiniuAccessKey" name="qiniu_access_key" value="${options.qiniu_access_key!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="qiniuSecretKey" class="col-lg-2 col-sm-4 control-label">Secret Key：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="qiniuSecretKey" name="qiniu_secret_key" value="${options.qiniu_secret_key!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="qiniuBucket" class="col-lg-2 col-sm-4 control-label">Bucket：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="qiniuBucket" name="qiniu_bucket" value="${options.qiniu_bucket!}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="qiniuSmallUrl" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.qiniu-small-url' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="qiniuSamllUrl" name="qiniu_small_url" value="${options.qiniu_small_url!}">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('attachOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>
                        <!-- 后台设置 -->
                        <div class="tab-pane" id="admin">
                            <form method="post" class="form-horizontal" id="adminOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.admin-pjax' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="admin_pjax" value="true" ${((options.admin_pjax!'true')=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="admin_pjax" value="false" ${((options.admin_pjax!)=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.admin-layout' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="admin_layout" value="" ${((options.admin_layout!'')=='')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.admin-layout-normal' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="admin_layout" value="layout-boxed" ${((options.admin_layout!)=='layout-boxed')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.admin-layout-box' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="adminLayoutBoxedBackground" class="col-lg-2 col-sm-4 control-label">盒子布局背景：</label>
                                        <div class="col-lg-4 col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control selectData" id="adminLayoutBoxedBackground" name="admin_layout_boxed_background" value="${options.admin_layout_boxed_background!}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default btn-flat" type="button" onclick="halo.layerModal('/admin/attachments/select?id=adminLayoutBoxedBackground','<@spring.message code="common.js.all-attachment" />')"><@spring.message code='common.btn.choose' /></button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="adminTheme" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.admin-theme' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <select class="form-control" id="adminTheme" name="admin_theme">
                                                <option value="skin-blue" ${((options.admin_theme!'skin-blue')=='skin-blue')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-blue' /></option>
                                                <option value="skin-blue-light" ${((options.admin_theme!)=='skin-blue-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-blue-light' /></option>
                                                <option value="skin-black" ${((options.admin_theme!)=='skin-black')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-black' /></option>
                                                <option value="skin-black-light" ${((options.admin_theme!)=='skin-black-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-black-light' /></option>
                                                <option value="skin-green" ${((options.admin_theme!)=='skin-green')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-green' /></option>
                                                <option value="skin-green-light" ${((options.admin_theme!)=='skin-green-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-green-light' /></option>
                                                <option value="skin-purple" ${((options.admin_theme!)=='skin-purple')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-purple' /></option>
                                                <option value="skin-purple-light" ${((options.admin_theme!)=='skin-purple-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-purple-light' /></option>
                                                <option value="skin-red" ${((options.admin_theme!)=='skin-red')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-red' /></option>
                                                <option value="skin-red-light" ${((options.admin_theme!)=='skin-red-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-red-light' /></option>
                                                <option value="skin-yellow" ${((options.admin_theme!)=='skin-yellow')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-yellow' /></option>
                                                <option value="skin-yellow-light" ${((options.admin_theme!)=='skin-yellow-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-yellow-light' /></option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.sidebar-style' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="sidebar_style" value="" ${((options.sidebar_style!'')=='')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.sidebar-style-none' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="sidebar_style" value="sidebar-collapse" ${((options.sidebar_style!)=='sidebar-collapse')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='admin.setting.form.sidebar-style-collapse' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('adminOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>

                        <!-- 邮箱设置 -->
                        <div class="tab-pane" id="email">
                            <form method="post" class="form-horizontal" id="emailOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.smtp-email-enable' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="smtp_email_enable" value="true" ${((options.smtp_email_enable!'false')=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="smtp_email_enable" value="false" ${((options.smtp_email_enable!)=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="emailSmtpHost" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-smtp-host' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="emailSmtpHost" name="mail_smtp_host" value="${options.mail_smtp_host!}" autocomplete='address-line1'>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="emailSmtpUserName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-smtp-user-name' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="email" class="form-control" id="emailSmtpUserName" name="mail_smtp_username" value="${options.mail_smtp_username!}" autocomplete="email">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="emailSmtpPassword" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-smtp-password' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="password" class="form-control" id="emailSmtpPassword" name="mail_smtp_password" value="${options.mail_smtp_password!}" autocomplete="current-password">
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="emailFromName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-from-name' /></label>
                                        <div class="col-lg-4 col-sm-8">
                                            <input type="text" class="form-control" id="emailFromName" name="mail_from_name" value="${options.mail_from_name!}" autocomplete="name">
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('emailOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>
                        <!-- 其他设置 -->
                        <div class="tab-pane" id="other">
                            <form method="post" class="form-horizontal" id="otherOptions">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.api-status' /></label>
                                        <div class="col-lg-4 col-sm-8 control-radio">
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="api_status" value="true" ${((options.api_status!)=='true')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.enable' /></label>
                                                </div>
                                            </div>
                                            <div class="pretty p-default p-round">
                                                <input type="radio" name="api_status" value="false" ${((options.api_status!'false')=='false')?string('checked','')}>
                                                <div class="state p-primary">
                                                    <label><@spring.message code='common.radio.disable' /></label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="apiToken" class="col-lg-2 col-sm-4 control-label">Api Token：</label>
                                        <div class="col-lg-4 col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control" id="apiToken" name="api_token" value="${options.api_token!}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-default btn-flat" id="btnUpdateToken" onclick="updateToken()" type="button"><@spring.message code='admin.setting.form.btn-update-token' /></button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <label for="statisticsCode" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.statistics-code' />
                                            <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.statistics-code-tips' />" style="cursor: pointer">
                                                <i class="fa fa-question-circle" aria-hidden="true"></i>
                                            </span>
                                        </label>
                                        <div class="col-lg-4 col-sm-8">
                                            <textarea class="form-control" rows="6" id="statisticsCode" name="statistics_code" style="resize: none">${options.statistics_code!}</textarea>
                                        </div>
                                    </div>
                                </div>
                                <div class="box-footer">
                                    <button type="button" class="btn btn-primary btn-sm " onclick="saveOptions('otherOptions')"><@spring.message code='common.btn.save' /></button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
</div>
<@footer>
<script type="application/javascript" id="footer_script">
    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
        checkAttachOption();
    });

    /**
     * 更新所有文章的摘要
     */
    function updateAllSummary() {
        $.get('/admin/posts/updateSummary',{'postSummary' : $('#postSummary').val()},function (data) {
            if(data.code === 1){
                halo.showMsg(data.msg,'success',1000);
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON');
    }

    /**
     * 主动提交文章到百度
     */
    function pushAllToBaidu() {
        $.get('/admin/posts/pushAllToBaidu',{'baiduToken' : $('#baiduToken').val()},function (data) {
            if(data.code === 1){
                halo.showMsg(data.msg,'success',1000);
            }else{
                halo.showMsg(data.msg,'error',2000);
            }
        },'JSON');
    }

    function updateToken() {
        $.get('/admin/getToken',function (data) {
            if(data.code === 1){
                $("#apiToken").val(data.result);
            }
        },'JSON');
    }

    /**
     * 附件选项切换
     */
    function checkAttachOption() {
        var server = $('input:radio[value=server]:checked').val();
        var upyun = $('input:radio[value=upyun]:checked').val();
        var qiniu = $('input:radio[value=qiniu]:checked').val();
        if(server!=null){
            $('.server-options').show();
        }else{
            $('.server-options').hide();
        }
        if(upyun!=null){
            $('.upyun-options').show();
        }else{
            $('.upyun-options').hide();
        }
        if(qiniu!=null){
            $('.qiniu-options').show();
        }else{
            $('.qiniu-options').hide();
        }
    }

    /**
     * 后台布局切换
     */
    function viewLayout() {
        var layout = $('input:radio[value=layout-boxed]:checked').val();
        if(layout!=null){
            $('body').addClass('layout-boxed');
        }else{
            $('body').removeClass('layout-boxed');
        }
    }

    /**
     * 预览侧边栏
     */
    function viewSideBar() {
        var layout = $('input:radio[value=sidebar-collapse]:checked').val();
        if(layout!=null){
            $('body').addClass('sidebar-collapse');
        }else{
            $('body').removeClass('sidebar-collapse');
        }
    }
    $('input[name=attach_loc]').click(function () {
        checkAttachOption();
    });
    $('input[name=admin_layout]').click(function () {
        viewLayout();
    });
    $('input[name=sidebar_style]').click(function () {
        viewSideBar();
    });

    /**
     * 预览后台样式切换
     */
    $(function () {
        var beforeTheme;
        $('#adminTheme').change(function () {
            if($('body').hasClass("${options.admin_theme!'skin-blue'}")){
                $('body').removeClass("${options.admin_theme!'skin-blue'}");
            }
            if(beforeTheme!=null){
                $('body').removeClass(beforeTheme);
            }
            $('body').addClass($(this).val());
            beforeTheme = $(this).val();
        })
    })
</script>
</@footer>

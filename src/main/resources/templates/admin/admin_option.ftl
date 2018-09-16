<#include "module/_macro.ftl">
<@head>${options.blog_title} | <@spring.message code='admin.setting.title' /></@head>
<div class="wrapper">
    <!-- 顶部栏模块 -->
    <#include "module/_header.ftl">
    <!-- 菜单栏模块 -->
    <#include "module/_sidebar.ftl">
    <div class="content-wrapper">
        <style type="text/css" rel="stylesheet">
            .form-horizontal .control-label{
                text-align: left;
            }
            .nav-tabs-custom > .nav-tabs > li.active {
                border-top-color: #d2d6de;
            }
        </style>
        <section class="content-header">
            <h1>
                <@spring.message code='admin.setting.title' />
                <small></small>
            </h1>
            <ol class="breadcrumb">
                <li>
                    <a data-pjax="true" href="/admin">
                        <i class="fa fa-dashboard"></i> <@spring.message code='admin.index.bread.index' /></a>
                </li>
                <li><a data-pjax="true" href="#"><@spring.message code='admin.setting.bread.setting' /></a></li>
                <li class="active"><@spring.message code='admin.setting.title' /></li>
            </ol>
        </section>
        <!-- tab选项卡 -->
        <section class="content container-fluid">
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
                                                    <option value="zh_CN" ${((options.blog_locale?default('zh_CN'))=='zh_CN')?string('selected','')}>简体中文</option>
                                                    <option value="en_US" ${((options.blog_locale?if_exists)=='en_US')?string('selected','')}>English</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogTitle" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-title' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="blogTitle" name="blog_title" value="${options.blog_title?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogUrl" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-url' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="url" class="form-control" id="blogUrl" name="blog_url" value="${options.blog_url?default('http://localhost:8080')}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogLogo" class="col-lg-2 col-sm-4 control-label">LOGO：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="text" class="form-control selectData" id="blogLogo" name="blog_logo" value="${options.blog_logo?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " type="button" onclick="openAttach('blogLogo')"><@spring.message code='common.btn.choose' /></button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogFavicon" class="col-lg-2 col-sm-4 control-label">Favicon：</label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="text" class="form-control selectData" id="blogFavicon" name="blog_favicon" value="${options.blog_favicon?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " type="button" onclick="openAttach('blogFavicon')"><@spring.message code='common.btn.choose' /></button>
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
                                                <textarea class="form-control" rows="5" id="blogFooterInfo" name="blog_footer_info" style="resize: none">${options.blog_footer_info?if_exists}</textarea>
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
                                                <input type="text" class="form-control" id="keywords" name="seo_keywords" value="${options.seo_keywords?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="desc" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.seo-desc' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="desc" name="seo_desc" value="${options.seo_desc?if_exists}">
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
                                                    <input type="text" class="form-control" id="baiduToken" name="seo_baidu_token" value="${options.seo_baidu_token?if_exists}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " id="btn_push_baidu" onclick="pushAllToBaidu()" type="button"><@spring.message code='admin.setting.form.baidu-token-btn-push' /></button>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <#-- 站点验证代码 -->
                                        <div class="form-group">
                                            <label for="blogVerificationGoogle" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-google' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="blogVerificationGoogle" name="blog_verification_google" value="${options.blog_verification_google?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogVerificationBing" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-bing' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="blogVerificationBing" name="blog_verification_bing" value="${options.blog_verification_bing?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogVerificationBaidu" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-baidu' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="blogVerificationBaidu" name="blog_verification_baidu" value="${options.blog_verification_baidu?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="blogVerificationQihu" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.blog-verification-qihu' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="blogVerificationQihu" name="blog_verification_qihu" value="${options.blog_verification_qihu?if_exists}">
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
                                                <input type="number" class="form-control" id="indexPosts" name="index_posts" value="${options.index_posts?default('10')}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="rssPosts" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.rss-posts' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="number" class="form-control" id="rssPosts" name="rss_posts" value="${options.rss_posts?if_exists}">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="postSummary" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.post-summary' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <div class="input-group">
                                                    <input type="number" class="form-control" id="postSummary" name="post_summary" value="${options.post_summary?default('50')}">
                                                    <span class="input-group-btn">
                                                        <button class="btn btn-default " id="btn_update_summary" onclick="updateAllSummary()" type="button"><@spring.message code='admin.setting.form.post-summary-btn-update' /></button>
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
                                            <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-system' />
                                                <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-system-tips' />" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="native" ${((options.comment_system?default('native'))=='native')?string('checked','')}> <@spring.message code='admin.setting.form.comment-system-native' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="valine" ${((options.comment_system?default('native'))=='valine')?string('checked','')}> Valine
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="disqus" ${((options.comment_system?default('native'))=='disqus')?string('checked','')}> Disqus
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="livere" ${((options.comment_system?default('native'))=='livere')?string('checked','')}> Livere
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="comment_system" value="changyan" ${((options.comment_system?default('native'))=='changyan')?string('checked','')}> <@spring.message code='admin.setting.form.comment-system-changyan' />
                                                </label>
                                            </div>
                                        </div>

                                        <!-- 原生设置 -->
                                        <div class="native-options" style="display: none">
                                            <div class="form-group">
                                                <label for="nativeCommentAvatar" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.native-comment-avatar' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.native-comment-avatar-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <select class="form-control" id="nativeCommentAvatar" name="native_comment_avatar">
                                                        <option value="mm" ${((options.native_comment_avatar?default('default'))=='mm')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-mm' /></option>
                                                        <option value="identicon" ${((options.native_comment_avatar?default('default'))=='identicon')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-identicon' /></option>
                                                        <option value="monsterid" ${((options.native_comment_avatar?default('default'))=='monsterid')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-monsterid' /></option>
                                                        <option value="wavatar" ${((options.native_comment_avatar?default('default'))=='wavatar')?string('selected','')}>Wavatar</option>
                                                        <option value="retro" ${((options.native_comment_avatar?default('default'))=='retro')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-retro' /></option>
                                                        <option value="robohash" ${((options.native_comment_avatar?default('default'))=='robohash')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-robohash' /></option>
                                                        <option value="blank" ${((options.native_comment_avatar?default('default'))=='blank')?string('selected','')}><@spring.message code='admin.setting.form.native-comment-avatar-blank' /></option>
                                                    </select>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.new-comment-need-check' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_need_check" value="true" ${((options.new_comment_need_check?default("true"))=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_need_check" value="false" ${((options.new_comment_need_check?if_exists)=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.new-comment-notice' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_notice" value="true" ${((options.new_comment_notice?if_exists)=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="new_comment_notice" value="false" ${((options.new_comment_notice?if_exists)=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-pass-notice' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_pass_notice" value="true" ${((options.comment_pass_notice?if_exists)=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_pass_notice" value="false" ${((options.comment_pass_notice?if_exists)=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-reply-notice' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_reply_notice" value="true" ${((options.comment_reply_notice?if_exists)=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                    </label>
                                                    <label class="radio-inline">
                                                        <input type="radio" name="comment_reply_notice" value="false" ${((options.comment_reply_notice?if_exists)=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                    </label>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="indexComments" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.index-comments' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.index-comments-tips' />" style="cursor: pointer">
                                                    <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="number" class="form-control" id="indexComments" name="index_comments" value="${options.index_comments?default('10')}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="nativeCommentPlaceholder" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.native-comment-placeholder' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="url" class="form-control" id="nativeCommentPlaceholder" name="native_comment_placeholder" value="${options.native_comment_placeholder?default('赶快评论一个吧！')}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="nativeCss" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-css' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-css-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="nativeCss" name="native_css" style="resize: none">${options.native_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- valine选项 -->
                                        <div class="valine-options" style="display: none">
                                            <div class="form-group">
                                                <label for="valineAppId" class="col-lg-2 col-sm-4 control-label">APP ID：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valineAppId" name="valine_appid" value="${options.valine_appid?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valineAppKey" class="col-lg-2 col-sm-4 control-label">APP KEY：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valineAppKey" name="valine_appkey" value="${options.valine_appkey?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valineAvatar" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.valine-avatar' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valineAvatar" name="valine_avatar" value="${options.valine_avatar?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valinePlaceholder" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.valine-placeholder' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="valinePlaceholder" name="valine_placeholder" value="${options.valine_placeholder?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="valineCss" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-css' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-css-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="valineCss" name="valine_css" style="resize: none">${options.valine_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- disqus选项 -->
                                        <div class="disqus-options" style="display: none">
                                            <div class="form-group">
                                                <label for="disqusShortname" class="col-lg-2 col-sm-4 control-label">Disqus ShortName：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="disqusShortname" name="disqus_shortname" value="${options.disqus_shortname?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="disqusCss" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-css' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-css-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="disqusCss" name="disqus_css" style="resize: none">${options.disqus_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- livere选项 -->
                                        <div class="livere-options" style="display: none">
                                            <div class="form-group">
                                                <label for="livereDataUid" class="col-lg-2 col-sm-4 control-label">livere data-uid：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="livereDataUid" name="livere_data_uid" value="${options.livere_data_uid?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="livereCss" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-css' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-css-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="livereCss" name="livere_css" style="resize: none">${options.livere_css?if_exists}</textarea>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 畅言选项 -->
                                        <div class="changyan-options" style="display: none">
                                            <div class="form-group">
                                                <label for="changyanAppId" class="col-lg-2 col-sm-4 control-label">APP ID：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="changyanAppId" name="changyan_appid" value="${options.changyan_appid?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="changyanConf" class="col-lg-2 col-sm-4 control-label">CONF：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="changyanConf" name="changyan_conf" value="${options.changyan_conf?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="changyanCss" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.comment-css' />
                                                    <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.comment-css-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                                </label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <textarea class="form-control" rows="5" id="changyanCss" name="changyan_css" style="resize: none">${options.changyan_css?if_exists}</textarea>
                                                </div>
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
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="attach_loc" value="server" ${((options.attach_loc?default('server'))=='server')?string('checked','')}> <@spring.message code='admin.setting.form.attach-loc-server' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="attach_loc" value="upyun" ${((options.attach_loc?if_exists)=='upyun')?string('checked','')} disabled="disabled"> <@spring.message code='admin.setting.form.attach-loc-upyun' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="attach_loc" value="qiniu" ${((options.attach_loc?if_exists)=='qiniu')?string('checked','')} disabled="disabled"> <@spring.message code='admin.setting.form.attach-loc-qiniu' />
                                                </label>
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
                                                    <input type="text" class="form-control" id="upyunOssDomain" name="upyun_oss_domain" value="${options.upyun_oss_domain?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssBucket" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-bucket' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssBucket" name="upyun_oss_bucket" value="${options.upyun_oss_bucket?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssOperator" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-operator' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssOperator" name="upyun_oss_operator" value="${options.upyun_oss_operator?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssPwd" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-pwd' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssPwd" name="upyun_oss_pwd" value="${options.upyun_oss_pwd?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="upyunOssSrc" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.upyun-oss-src' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="upyunOssSrc" name="upyun_oss_src" value="${options.upyun_oss_src?if_exists}">
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 七牛云 -->
                                        <div class="qiniu-options" style="display: none">
                                            <div class="form-group">
                                                <label for="qiniuDomain" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.qiniu-domain' /></label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuDomain" name="qiniu_domain" value="${options.qiniu_domain?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="qiniuAccessKey" class="col-lg-2 col-sm-4 control-label">Access Key：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuAccessKey" name="qiniu_access_key" value="${options.qiniu_access_key?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="qiniuSecretKey" class="col-lg-2 col-sm-4 control-label">Secret Key：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuSecretKey" name="qiniu_secret_key" value="${options.qiniu_secret_key?if_exists}">
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <label for="qiniuBucket" class="col-lg-2 col-sm-4 control-label">Bucket：</label>
                                                <div class="col-lg-4 col-sm-8">
                                                    <input type="text" class="form-control" id="qiniuBucket" name="qiniu_bucket" value="${options.qiniu_bucket?if_exists}">
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
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_pjax" value="true" ${((options.admin_pjax?default('true'))=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_pjax" value="false" ${((options.admin_pjax?if_exists)=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.admin-loading' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_loading" value="true" ${((options.admin_loading?if_exists)=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_loading" value="false" ${((options.admin_loading?default('false'))=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.admin-layout' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_layout" value="" ${((options.admin_layout?default(''))=='')?string('checked','')}> <@spring.message code='admin.setting.form.admin-layout-normal' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="admin_layout" value="layout-boxed" ${((options.admin_layout?default(''))=='layout-boxed')?string('checked','')}> <@spring.message code='admin.setting.form.admin-layout-box' />
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="adminTheme" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.admin-theme' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <select class="form-control" id="adminTheme" name="admin_theme">
                                                    <option value="skin-blue" ${((options.admin_theme?default('skin-blue'))=='skin-blue')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-blue' /></option>
                                                    <option value="skin-blue-light" ${((options.admin_theme?if_exists)=='skin-blue-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-blue-light' /></option>
                                                    <option value="skin-black" ${((options.admin_theme?if_exists)=='skin-black')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-black' /></option>
                                                    <option value="skin-black-light" ${((options.admin_theme?if_exists)=='skin-black-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-black-light' /></option>
                                                    <option value="skin-green" ${((options.admin_theme?if_exists)=='skin-green')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-green' /></option>
                                                    <option value="skin-green-light" ${((options.admin_theme?if_exists)=='skin-green-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-green-light' /></option>
                                                    <option value="skin-purple" ${((options.admin_theme?if_exists)=='skin-purple')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-purple' /></option>
                                                    <option value="skin-purple-light" ${((options.admin_theme?if_exists)=='skin-purple-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-purple-light' /></option>
                                                    <option value="skin-red" ${((options.admin_theme?if_exists)=='skin-red')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-red' /></option>
                                                    <option value="skin-red-light" ${((options.admin_theme?if_exists)=='skin-red-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-red-light' /></option>
                                                    <option value="skin-yellow" ${((options.admin_theme?if_exists)=='skin-yellow')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-yellow' /></option>
                                                    <option value="skin-yellow-light" ${((options.admin_theme?if_exists)=='skin-yellow-light')?string('selected','')}><@spring.message code='admin.setting.form.admin-theme-skin-yellow-light' /></option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.sidebar-style' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="sidebar_style" value="" ${((options.sidebar_style?default(''))=='')?string('checked','')}> <@spring.message code='admin.setting.form.sidebar-style-none' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="sidebar_style" value="sidebar-collapse" ${((options.sidebar_style?default(''))=='sidebar-collapse')?string('checked','')}> <@spring.message code='admin.setting.form.sidebar-style-collapse' />
                                                </label>
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
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="smtp_email_enable" value="true" ${((options.smtp_email_enable?default('false'))=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="smtp_email_enable" value="false" ${((options.smtp_email_enable?if_exists)=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailSmtpHost" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-smtp-host' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="emailSmtpHost" name="mail_smtp_host" value="${options.mail_smtp_host?if_exists}" autocomplete='address-line1'>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailSmtpUserName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-smtp-user-name' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="email" class="form-control" id="emailSmtpUserName" name="mail_smtp_username" value="${options.mail_smtp_username?if_exists}" autocomplete="email">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailSmtpPassword" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-smtp-password' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="password" class="form-control" id="emailSmtpPassword" name="mail_smtp_password" value="${options.mail_smtp_password?if_exists}" current-password>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="emailFromName" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.email-from-name' /></label>
                                            <div class="col-lg-4 col-sm-8">
                                                <input type="text" class="form-control" id="emailFromName" name="mail_from_name" value="${options.mail_from_name?if_exists}" autocomplete="name">
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
                                            <div class="col-lg-4 col-sm-8">
                                                <label class="radio-inline">
                                                    <input type="radio" name="api_status" value="true" ${((options.api_status?if_exists)=='true')?string('checked','')}> <@spring.message code='common.radio.enable' />
                                                </label>
                                                <label class="radio-inline">
                                                    <input type="radio" name="api_status" value="false" ${((options.api_status?default('false'))=='false')?string('checked','')}> <@spring.message code='common.radio.disable' />
                                                </label>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="statisticsCode" class="col-lg-2 col-sm-4 control-label"><@spring.message code='admin.setting.form.statistics-code' />
                                                <span data-toggle="tooltip" data-placement="top" title="<@spring.message code='admin.setting.form.statistics-code-tips' />" style="cursor: pointer">
                                                        <i class="fa fa-question-circle" aria-hidden="true"></i>
                                                    </span>
                                            </label>
                                            <div class="col-lg-4 col-sm-8">
                                                <textarea class="form-control" rows="6" id="statisticsCode" name="statistics_code" style="resize: none">${options.statistics_code?if_exists}</textarea>
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
        <@compress single_line=true>
        <script>
            $(function () {
                $('[data-toggle="tooltip"]').tooltip();
                checkCommentOption();
                checkAttachOption();
            });

            /**
             * 打开附件
             */
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

            /**
             * 更新所有文章的摘要
             */
            function updateAllSummary() {
                $.ajax({
                    type: 'GET',
                    url: '/admin/posts/updateSummary',
                    data: {
                        postSummary : $('#postSummary').val()
                    },
                    success: function (data) {
                        if(data.code==1){
                            showMsg(data.msg,"success",1000);
                        }else{
                            showMsg(data.msg,"success",2000);
                        }
                    }
                });
            }

            /**
             * 主动提交文章到百度
             */
            function pushAllToBaidu() {
                $.ajax({
                    type: 'GET',
                    url: '/admin/posts/pushAllToBaidu',
                    data: {
                        baiduToken : $('#baiduToken').val()
                    },
                    success: function (data) {
                        if(data.code==1){
                            showMsg(data.msg,"success",1000);
                        }else{
                            $.toast({
                                text: data.msg,
                                heading: '<@spring.message code="common.text.tips" />',
                                icon: icon,
                                showHideTransition: 'fade',
                                allowToastClose: true,
                                hideAfter: hideAfter,
                                stack: 1,
                                position: 'top-center',
                                textAlign: 'left',
                                loader: true,
                                loaderBg: '#ffffff'
                            });
                        }
                    }
                });
            }

            /**
             * 评论选项切换
             */
            function checkCommentOption() {
                var native = $('input:radio[value=native]:checked').val();
                var valine = $('input:radio[value=valine]:checked').val();
                var disqus = $('input:radio[value=disqus]:checked').val();
                var livere = $('input:radio[value=livere]:checked').val();
                var changyan = $('input:radio[value=changyan]:checked').val();
                if(native!=null){
                    $('.native-options').show();
                }else{
                    $('.native-options').hide();
                }
                if(valine!=null){
                    $('.valine-options').show();
                }else{
                    $('.valine-options').hide();
                }
                if(disqus!=null){
                    $('.disqus-options').show();
                }else{
                    $('.disqus-options').hide();
                }
                if(livere!=null){
                    $('.livere-options').show();
                }else{
                    $('.livere-options').hide();
                }
                if(changyan!=null){
                    $('.changyan-options').show();
                }else{
                    $('.changyan-options').hide();
                }
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
            $('input[name=comment_system]').click(function () {
                checkCommentOption();
            });
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
                    if($('body').hasClass("${options.admin_theme?default('skin-blue')}")){
                        $('body').removeClass("${options.admin_theme?default('skin-blue')}");
                    }
                    if(beforeTheme!=null){
                        $('body').removeClass(beforeTheme);
                    }
                    $('body').addClass($(this).val());
                    beforeTheme = $(this).val();
                })
            })
        </script>
        </@compress>
    </div>
    <#include "module/_footer.ftl">
</div>
<@footer></@footer>

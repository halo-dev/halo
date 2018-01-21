<#include "module/macro.ftl">
<@head title="${options.site_title?if_exists}" keywords="${options.seo_keywords?if_exists}" description="${options.seo_desc?if_exists}"></@head>
<div class="wrapper" style="height: auto; min-height: 100%;">
<#include "module/header.ftl">
<div class="main-content">
    <section class="container">
        <div class="row post-list">
            <#include "module/post_entry.ftl">
        </div>
        <div class="post-list-none" data-aos="fade-up">
            <span>没有更多文章</span>
        </div>
        <div class="btn-post-loader text-center" data-aos="fade-up">
            <div class="pagination">
                <div class="col-xs-12">
                    <button type="button" class="btn-next-page">
                        <i class="fa fa-angle-down fa-4x iconAction" aria-hidden="true"></i>
                        <i class="fa fa-spinner fa-pulse fa-3x fa-fw iconLoading" style="display: none"></i>
                    </button>
                </div>
            </div>
        </div>
    </section>
    <script>
        $('.btn-next-page').click(function () {
            nextPage();
        });
        var i = 2;
        function nextPage() {
            NProgress.start();
            $.ajax({
                type: 'GET',
                url: '/next',
                dataType: 'JSON',
                data: {
                    'page' : i
                },
                async: false,
                success: function (data) {
                    NProgress.done();
                    i=i+1;
                    if(data==null||data==""){
                        $('.post-list-none').css("display","block");
                        $('.btn-post-loader').css("display","none");
                        return;
                    }
                    $.each(data,function (n,value) {
                        $('.post-list').append('' +
                                '<div class="col-lg-4 col-md-6 col-sm-6 col-xs-12 post-list-item" data-aos="fade-up">\n' +
                                '<div class="post-item-main">\n' +
                                '<a href="/archives/'+value.postUrl+'">\n' +
                                '<div class="post-item-thumbnail lazy" style="background-image: url(/halo/source/img/pic12.jpg)"></div>\n' +
                                '</a>\n' +
                                '<div class="post-item-info">\n' +
                                '<div class="post-info-title">\n' +
                                '<a href="/archives/'+value.postUrl+'"><span>'+value.postTitle+'</span></a><br>\n' +
                                '</div>\n' +
                                '<div>\n' +
                                '<span class="post-info-desc">\n' +
                                ''+value.postSummary+'...' +
                                '</span>\n' +
                                '</div>\n' +
                                '<div class="post-info-other" style="text-align: right">\n' +
                                '<a href="#">MORE></a>\n' +
                                '</div>\n' +
                                '</div>\n' +
                                '</div>\n' +
                                '</div>'
                        );
                    });
                }
            })
        }
    </script>
</div>
<@footer></@footer>
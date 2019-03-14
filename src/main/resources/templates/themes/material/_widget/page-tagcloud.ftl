<style>
    #bottom{
        position: relative;
    }

    @media screen and (max-width: 480px) {
        .material-tagscloud{
            margin: 6em 2em 0;
        }
    }

    .material-tagscloud a{
        text-decoration:none;
        padding: 1px 9px;
        margin: 9px 1px;
        line-height: 40px;
        white-space: nowrap;
        transition: .6s;
        opacity: .85;
    }

    .material-tagscloud a:hover{
        transition: .6s;
        opacity: 1;
        background: #FAFAFA;
        box-shadow: 0 2px 2px 0 rgba(0,0,0,.14), 0 3px 1px -2px rgba(0,0,0,.2), 0 1px 5px 0 rgba(0,0,0,.12);
    }
</style>

<div class=" material-tagscloud">
    <div class="material-post mdl-grid">
        <@commonTag method="tags">
            <#if tags?? && tags?size gt 0>
                <#list tags as tag>
                    <a href="${options.blog_url!}/tags/${tag.tagUrl}/" style="font-size: 22.33px; color: #757575">${tag.tagName}</a>
                </#list>
            </#if>
        </@commonTag>
    </div>
</div>


<script type="text/ls-javascript" id="page-tagcloud-script">
        var adjustFooter = function() {
        if( ($('#bottom').offset().top + $('#bottom').outerHeight() ) < $(window).height() ) {
            var footerBottom = $(window).height() - $('#bottom').outerHeight() - $('#bottom').offset().top;
            $('#bottom').css('bottom', '-' + footerBottom + 'px');
        }
    };

    $(document).ready(function() {
        adjustFooter();
    });
</script>

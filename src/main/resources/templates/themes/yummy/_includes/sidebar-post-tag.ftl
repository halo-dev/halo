<h3 class="sidebar-title">Blog Categories</h3>
<div id="blog-categories">
    <div class="list-group">
        <a href="javascript:" class="list-group-item" data-category="" data-posts="">
            All
            <span class="badge"><@articleTag method="postsCount">${postsCount}</@articleTag></span>
        </a>
        <@commonTag method="categories">
            <#list categories as category>
                <li class="list-group-item">
                    ${category.cateName}
                    <span class="badge">${category.posts?size}</span>
                </li>
            </#list>
        </@commonTag>
    </div>
</div>
<script type="text/javascript">

    $(document).ready(function () {

        filterPosts();

        $("#blog-categories li").css('text-transform', 'capitalize');

        $(".list-group-item").click(function () {
            window.location.hash = $(this).attr('data-category') ? $(this).attr('data-category') : "";
        });

        window.addEventListener('hashchange', function () {
            filterPosts();
        });
    });

    function filterPosts() {
        if (location.hash) {
            var hash = decodeURIComponent(location.hash.slice(1));

            $("#blog-categories .list-group-item").each(function (i, item) {
                if ($(item).attr("data-category") == hash) {
                    $(item).addClass("active");
                    var posts = $(item).attr("data-posts").split("||");
                    $("#posts-list .posts-list-item").each(function (i, item) {
                        $.inArray($(item).attr("data-title"), posts) != -1 ? $(item).show() : $(item).hide();
                    });
                } else {
                    $(item).removeClass("active");
                }
            });
        } else {
            $("#posts-list .posts-list-item").each(function (i, item) {
                $(item).show();
            });
            $("#blog-categories .list-group-item").each(function (i, item) {
                $(item).attr("data-category") == "" ? $(item).addClass("active") : $(item).removeClass("active");
            });
        }
    }

</script>

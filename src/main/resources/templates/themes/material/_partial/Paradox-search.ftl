<div class="mdl-textfield mdl-js-textfield mdl-textfield--expandable" method="post" action="">
    <label id="search-label" class="mdl-button mdl-js-ripple-effect mdl-js-button mdl-button--fab mdl-color--accent mdl-shadow--4dp" for="search">
        <i class="material-icons mdl-color-text--white" role="presentation">search</i>
    </label>

    <form autocomplete="off" id="search-form" method="get" action="${options.blog_url!}/search" accept-charset="UTF-8" class="mdl-textfield__expandable-holder">
        <input class="mdl-textfield__input search-input" type="search" name="keyword" id="search" placeholder="">
        <label id="search-form-label" class="mdl-textfield__label" for="search"></label>
        <input type="hidden" name="sitesearch" value="">
    </form>
</div>


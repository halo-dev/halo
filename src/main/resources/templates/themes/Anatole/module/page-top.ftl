<div class="page-top animated fadeInDown">
    <div class="nav">
        <#list menus?sort_by('menuSort') as menu>
            <li>
                <a href="${menu.menuUrl}">${menu.menuName} </a>
            </li>
        </#list>
    </div>
    <div class="information">
        <div class="back_btn">
            <li>
                <a onclick="window.history.go(-1)" style="display:none;" class="fa fa-chevron-left"></a>
            </li>
        </div>
        <div class="avatar">
            <img src="/anatole/source/images/favicon.png" />
        </div>
    </div>
</div>
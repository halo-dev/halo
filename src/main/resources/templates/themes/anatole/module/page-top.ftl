<div class="page-top animated fadeInDown">
    <div class="nav">
        <@commonTag method="menus">
            <#list menus?sort_by('menuSort') as menu>
                <li>
                    <a href="${menu.menuUrl}" target="${menu.menuTarget!}">${menu.menuName} </a>
                </li>
            </#list>
        </@commonTag>
    </div>
    <div class="information">
        <div class="back_btn">
            <li>
                <a onclick="window.history.go(-1)" style="display: none" class="fa fa-chevron-left"></a>
            </li>
        </div>
        <div class="avatar">
            <img src="${options.anatole_style_right_icon!'/anatole/source/images/logo.png'}" />
        </div>
    </div>
</div>

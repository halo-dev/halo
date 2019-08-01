<div class="page-top animated fadeInDown">
    <div class="nav">
        <@menuTag method="list">
            <#list menus?sort_by('priority') as menu>
                <li>
                    <a href="${menu.url}" target="${menu.target!}">${menu.name} </a>
                </li>
            </#list>
        </@menuTag>
    </div>
    <div class="information">
        <div class="back_btn">
            <li>
                <a onclick="window.history.go(-1)" style="display: none" class="fa fa-chevron-left"></a>
            </li>
        </div>
        <div class="avatar">
            <img src="${settings.icon!'${static!}/source/images/logo.png'}" />
        </div>
    </div>
</div>

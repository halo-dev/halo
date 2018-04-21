<aside id="menu" <% if((is_post() || page.layout === 'page') && theme.hideMenu){ %>class="hide"<% } %> >
  <div class="inner flex-row-vertical">
      <a href="javascript:;" class="header-icon waves-effect waves-circle waves-light" id="menu-off">
          <i class="icon icon-lg icon-close"></i>
      </a>
      <div class="brand-wrap"
           style="background-image:url(${options.indigo_general_brand?default("/indigo/source/img/brand.jpg")})">
          <div class="brand">
              <a href="${user.userAvatar?default("/indigo/source/img/avatar.jpg")}"
                 class="avatar waves-effect waves-circle waves-light">
                  <img src="${user.userAvatar?default("/indigo/source/img/avatar.jpg")}">
              </a>
              <hgroup class="introduce">
                  <h5 class="nickname">${user.userDisplayName?default("indigo")}</h5>
                  <a href="mailto:i@ryanc.cc" title="${user.userEmail}" class="mail">${user.userEmail}</a>
              </hgroup>
          </div>
      </div>
      <div class="scroll-wrap flex-col">
          <ul class="nav">
          <#list menus as menu>
              <li class="waves-block waves-effect active">
                  <a href="${menu.menuUrl}">
                      <i class="icon icon-lg ${menu.menuIcon?default("")}"></i>
                      ${menu.menuName}
                  </a>
              </li>
          </#list>
          </ul>
      </div>
  </div>
</aside>

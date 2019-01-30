<!-- Daily pic -->
<div class="mdl-card mdl-shadow--${options.theme_material_uiux_card_elevation!2}dp daily-pic mdl-cell mdl-cell--8-col index-top-block">
    <!-- Pic & Slogan -->
    <div class="mdl-card__media mdl-color-text--grey-50" style="background-image:url(${options.theme_material_daily_pic!'/material/source/img/daily_pic.png'})">
        <p class="index-top-block-slogan"><a href="#">
            ${options.theme_material_uiux_slogan!'Hi,nice to meet you'}
        </a></p>
    </div>

    <!-- Avatar & Name -->
    <div class="mdl-card__supporting-text meta mdl-color-text--grey-600">
        <!-- Author Avatar -->
        <div id="author-avatar">
            <img src="${user.userAvatar!'/material/source/img/avatar.png'}" width="32px" height="32px" alt="avatar">
        </div>
        <div>
            <strong>${user.userDisplayName!'Material'}</strong>
        </div>
    </div>
</div>

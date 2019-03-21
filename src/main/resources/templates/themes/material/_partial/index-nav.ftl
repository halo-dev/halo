<#macro nav url="">
    <#if posts.pages gt 1 >
        <nav class="material-nav mdl-cell mdl-cell--12-col">
            <#if posts.hasPrevious()>
                <#if posts.page == 1>
                    <a class="extend prev" rel="prev" href="${url}">
                        <button class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
                            <i class="material-icons" role="presentation">arrow_back</i>
                        </button>
                    </a>
                <#else >
                    <a class="extend prev" rel="prev" href="${url}page/${posts.page}">
                        <button class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
                            <i class="material-icons" role="presentation">arrow_back</i>
                        </button>
                    </a>
                </#if>
            </#if>
            <span class="page-number current">${posts.page+1}</span>
            <#if posts.hasNext()>
                <a class="extend next" rel="next" href="${url}page/${posts.page+2}">
                    <button class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
                        <i class="material-icons" role="presentation">arrow_forward</i>
                    </button>
                </a>
            </#if>
        </nav>
    </#if>
</#macro>
<#macro nav url="">
    <#if posts.totalPages gt 1 >
        <nav class="material-nav mdl-cell mdl-cell--12-col">
            <#if posts.hasPrevious()>
                <#if posts.number == 1>
                    <a class="extend prev" rel="prev" href="${url}">
                        <button class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
                            <i class="material-icons" role="presentation">arrow_back</i>
                        </button>
                    </a>
                <#else >
                    <a class="extend prev" rel="prev" href="${url}page/${posts.number}">
                        <button class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
                            <i class="material-icons" role="presentation">arrow_back</i>
                        </button>
                    </a>
                </#if>
            </#if>
            <span class="page-number current">${posts.number+1}</span>
            <#if posts.hasNext()>
                <a class="extend next" rel="next" href="${url}page/${posts.number+2}">
                    <button class="mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon">
                        <i class="material-icons" role="presentation">arrow_forward</i>
                    </button>
                </a>
            </#if>
        </nav>
    </#if>
</#macro>
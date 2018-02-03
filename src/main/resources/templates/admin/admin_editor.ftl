<#if (options.post_editor?default('editor.md'))=='editor.md'>
    <#include "admin_md-editor.ftl">
<#else >
    <#include "admin_rt-editor.ftl">
</#if>
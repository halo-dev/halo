<#if (options.post_editor?default('editor.md'))=='editor.md'>
    <#include "_md-editor.ftl">
<#else >
    <#include "_rt-editor.ftl">
</#if>
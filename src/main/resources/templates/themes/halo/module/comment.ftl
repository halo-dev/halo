<#if (options.comment_system!)=='valine'>
    <#include "../../../common/comment/_valine_comment.ftl">
<#elseif (options.comment_system!)=='disqus'>
    <#include "../../../common/comment/_disqus_comment.ftl">
<#elseif (options.comment_system!)=='livere'>
    <#include "../../../common/comment/_livere_comment.ftl">
<#elseif (options.comment_system!)=='changyan'>
    <#include "../../../common/comment/_changyan_comment.ftl">
</#if>
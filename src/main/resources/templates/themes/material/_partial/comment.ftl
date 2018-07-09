<#if post.allowComment?default(1)==1>
    <#switch options.comment_system>
        <#case "native">
            <#include "../../../common/comment/_native_comment.ftl">
            <#break >
        <#case "valine">
            <#include "../../../common/comment/_valine_comment.ftl">
            <#break >
        <#case "disqus">
            <#include "../../../common/comment/_disqus_comment.ftl">
            <#break >
        <#case "livere">
            <#include "../../../common/comment/_livere_comment.ftl">
            <#break >
        <#case "changyan">
            <#include "../../../common/comment/_changyan_comment.ftl">
            <#break >
    </#switch>
</#if>

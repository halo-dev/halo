# AI 摘要接口初版框架（周四任务）

## 一、接口功能概述
为 Halo 博客的文章详情页，新增 AI 摘要接口，调用 DeepSeek API 自动生成文章内容摘要，供前端页面展示。

## 二、接口设计
- 接口路径：`/api/v1/posts/{postId}/ai-summary`
- 请求方式：`GET`
- 输入参数：文章 ID（`Long postId`）
- 返回结果：文章摘要文本（`String summary`）

## 三、核心流程框架（伪代码）
```java
// 1. 接收前端请求，获取文章ID
public ResponseEntity<String> getPostAiSummary(Long postId) {
    // 2. 从数据库获取文章内容
    Post post = postRepository.findById(postId);
    String articleContent = post.getContent();

    // 3. 调用 DeepSeek API 生成摘要
    String prompt = "请为以下博客文章生成一段100字以内的摘要：" + articleContent;
    String summary = deepSeekApiClient.chat(prompt);

    // 4. 返回摘要给前端
    return ResponseEntity.ok(summary);
}
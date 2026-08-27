# AI 助手接口文档

本文档对应 GooShare 当前的 Spring AI Alibaba ReactAgent 主链路，以及前端 `AiAssistant.vue` 的实际调用方式。

## 接口概览

| 方法 | 路径 | 是否登录 | 用途 |
| --- | --- | --- | --- |
| `POST` | `/ai/chat-v2` | 是 | Agent 多轮问答、商品查询和平台知识问答 |
| `GET` | `/ai/chat` | 按当前拦截器配置需要登录 | 早期意图识别实验接口，不建议前端继续使用 |

当前后端没有提供以下独立接口：

- `/ai/history`
- `/ai/recommend`
- `/ai/context/item/{id}`
- `DELETE /ai/history`

会话恢复由 ReactAgent 与 RedisSaver 完成；前端“清空聊天”通过生成新的 `conversationId` 开启新会话。

## 基础配置

后端默认地址：

```text
http://localhost:8084
```

前端通过以下变量覆盖默认地址：

```dotenv
VUE_APP_API_BASE_URL=http://localhost:8084
```

前端环境变量示例位于：

```text
frontend/.env.example
```

## 鉴权

`POST /ai/chat-v2` 会经过登录拦截器。请求前需要先调用密码登录或验证码登录接口，取得 GooShare Token。

Token 直接放入 `Authorization` 请求头：

```http
Authorization: your-login-token
```

当前实现没有使用 `Bearer` 前缀。

未登录时，接口返回业务错误：

```json
{
  "code": 0,
  "msg": "请先登录",
  "data": null
}
```

## Agent 对话接口

### 请求

```http
POST /ai/chat-v2
Content-Type: application/json
Authorization: your-login-token
```

请求体：

```json
{
  "conversationId": "demo-conversation-001",
  "message": "帮我找 2000 元以下的手机"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `message` | `string` | 是 | 用户问题，去除首尾空格后不能为空 |
| `conversationId` | `string` | 否 | 会话标识；为空时后端自动生成 UUID |

### 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "conversationId": "demo-conversation-001",
    "answer": "根据商品工具返回的数据生成的回答"
  }
}
```

第一次请求没有提供 `conversationId` 时，应保存响应中的 ID，并在后续请求中复用。

### 常见业务错误

消息为空：

```json
{
  "code": 0,
  "msg": "消息内容不能为空",
  "data": null
}
```

Agent 执行异常：

```json
{
  "code": 0,
  "msg": "AI 助手执行失败，请稍后重试",
  "data": null
}
```

当前控制器通过统一 `Result` 返回业务状态。调用方除了检查 HTTP 状态，还必须检查响应体中的 `code`。

## PowerShell 调用示例

```powershell
$headers = @{
    Authorization = "your-login-token"
    "Content-Type" = "application/json"
}

$body = @{
    conversationId = "demo-conversation-001"
    message = "平台是否支持物流发货？"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8084/ai/chat-v2" `
    -Headers $headers `
    -Body $body
```

## 会话与记忆隔离

后端不会直接使用客户端传来的 `conversationId` 作为 Redis 检查点键，而是绑定当前登录用户：

```text
user:{userId}:conversationId:{conversationId}
```

这保证：

- 相同用户、相同会话可以恢复上下文。
- 相同用户的不同会话互不影响。
- 不同用户使用相同 `conversationId` 时不会共享记忆。

ReactAgent 的检查点通过 RedisSaver 保存，因此应用重启后仍可恢复已有会话，但 Redis 数据被清理后记忆也会丢失。

## Agent 工具调用

ReactAgent 当前注册两个工具组件。

### 商品工具

`AiShoppingTools` 提供：

- 按关键词、品牌、分类、价格和排序方式检索商品。
- 查询平台商品分类。
- 按商品 ID 查询商品详情。

商品价格、库存、卖家和 ID 必须来自工具查询结果，Agent 不应自行编造。

### 平台知识工具

`AiKnowledgeTools` 根据问题类型检索 Milvus：

- `PLATFORM_RULES`：账号、发布、购物车和订单规则。
- `CAMPUS_TRADE`：校园面交、交易安全和防骗。
- `SHIPPING`：发货、物流、收货和售后。
- `ALL`：跨类型问题或无法确定类型时使用。

非 `ALL` 查询会使用 `knowledgeType` 元数据表达式过滤向量检索范围。

## RAG 知识来源

当前知识文件：

```text
backend/src/main/resources/knowledge/platform-rules.md
backend/src/main/resources/knowledge/campus-trade.md
backend/src/main/resources/knowledge/shipping.md
```

回答平台规则时，Agent 应在结尾标注：

```text
依据：source 文件名
```

如果召回片段没有明确回答问题，Agent 应说明知识库未收录，而不是根据常识推断平台规则。

## 前端接入方式

当前入口：

```text
frontend/src/components/AiAssistant.vue
```

前端行为：

1. 首次打开时在 `localStorage.aiConversationId` 生成会话 ID。
2. 从 `localStorage.token` 获取登录 Token。
3. 调用 `POST /ai/chat-v2`，在请求体中发送 `conversationId` 和 `message`。
4. 如果用户在商品详情页询问“这个商品”，前端会把当前商品 ID 拼入问题，Agent 再调用商品详情工具。
5. 点击清空聊天时，前端生成新的 `conversationId` 并清空本地消息列表。

清空聊天不会删除 Redis 中的旧检查点，只是不再使用旧的 `conversationId`。

## 模型与密钥

当前 ReactAgent 使用 DashScope 模型，后端从环境变量读取：

```text
AI_DASHSCOPE_API_KEY
DASHSCOPE_MODEL
DASHSCOPE_EMBEDDING_MODEL
```

前端不需要也不允许保存模型 API Key。任何以 `VUE_APP_` 开头的变量都会进入浏览器构建产物，只能用于公开配置，例如后端 API 地址。

## 相关代码

```text
backend/src/main/java/com/gooshare/controller/AgentController.java
backend/src/main/java/com/gooshare/config/GooShareAgentConfig.java
backend/src/main/java/com/gooshare/config/AgentMemoryConfig.java
backend/src/main/java/com/gooshare/agent/tool/AiShoppingTools.java
backend/src/main/java/com/gooshare/agent/tool/AiKnowledgeTools.java
backend/src/main/java/com/gooshare/agent/rag/KnowledgeIngestionRunner.java
frontend/src/components/AiAssistant.vue
```


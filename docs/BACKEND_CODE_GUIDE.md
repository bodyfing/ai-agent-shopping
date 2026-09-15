# GooShare 后端代码导读

本文面向第一次阅读 GooShare 后端代码的开发者，说明 `com.gooshare` 包下各个子包和类的职责。这里的介绍以“这个类解决什么问题”为主，不展开每个方法的实现细节。

## 1. 先理解一次请求如何流转

普通业务请求大致经过下面几层：

```text
浏览器 / 前端
    -> controller       接收 HTTP 请求，参数校验，返回结果
    -> service          编排业务流程
    -> mapper            访问 MySQL
    -> entity / DTO      在不同层之间传递数据
    -> VO                整理成返回给前端的数据
```

AI 导购请求的主要路径是：

```text
前端
  -> AgentController
  -> AI Agent / AiShoppingTools
  -> ItemService 或 ItemMapper
  -> MySQL
  -> AgentChatResponse
```

登录校验由拦截器完成，登录用户信息通常暂存在 `UserHolder`；Redis 用于登录令牌、验证码、购物车、秒杀等场景；Qdrant 用于知识库向量检索。

## 2. 根包和启动类

### `com.gooshare.GooShareApplication`

Spring Boot 程序入口。运行它会创建 Spring 容器、加载 `application.yml` 和相关 Profile，并扫描 `com.gooshare` 下的组件。

## 3. `agent`：AI Agent 和知识库

这是项目中面向 AI 的代码，不是传统的商品 CRUD 层。

### `agent.dto`

- `AgentChatRequest`：前端发送 AI 对话时的请求对象，包含用户问题、会话等信息。
- `AgentChatResponse`：AI 对话接口返回对象，包含回答及会话相关信息。
- `ItemSearchToolRequest`：AI 调用商品搜索工具时的结构化参数，如关键词、分类、品牌、价格和排序方式。
- `KnowledgeSearchToolRequest`：AI 调用知识库搜索工具时的结构化参数。

### `agent.tool`

- `AiShoppingTools`：提供商品搜索、商品筛选等工具给 AI。它把模型提取出的购买条件转换成数据库查询，并负责分类别名等兼容处理。
- `AiKnowledgeTools`：提供平台规则、校园交易、物流等知识库检索工具，底层使用向量检索。

### `agent.rag`

- `KnowledgeIngestionRunner`：应用启动时或按配置把 Markdown 等知识文件切分、向量化并写入 Qdrant。它属于知识库导入流程，不是普通用户请求流程。

## 4. `common`：全局常量和通用返回对象

- `IntentType`：AI 意图类型枚举，例如商品搜索、知识问答等。
- `ItemConstants`：商品相关常量，如状态、库存或缓存前缀。
- `ItemSortType`：商品排序方式，如最新、价格等。
- `KnowledgeType`：知识库文档类型枚举。
- `RedisConstants`：Redis key 前缀、过期时间等集中定义。
- `Result`：接口统一返回包装，通常包含成功状态、消息和数据。

## 5. `config`：Spring 和第三方组件配置

- `AgentMemoryConfig`：配置 AI Agent 的会话记忆，使不同用户/会话的上下文能够隔离。
- `AIConfig`：配置 AI 相关 Bean 或模型参数。
- `CorsConfig`：配置跨域规则，让前端开发服务器可以访问后端。
- `GooShareAgentConfig`：GooShare Agent 总体配置和组装入口。
- `LangChain4jConfig`：配置 LangChain4j 相关模型、工具或 Agent 组件。
- `MvcConfig`：配置 Spring MVC，例如拦截器、静态资源或参数处理。
- `OpenApiConfig`：配置 Swagger / OpenAPI 文档信息。

### `config.properties`

- `DifyProperties`：读取 Dify 相关配置项。即使当前主要使用其他 AI 链路，该类仍是配置绑定类，具体是否生效取决于配置和 Bean 使用情况。

## 6. `controller`：HTTP 接口入口

Controller 只应该负责接收请求、调用 Service、处理登录用户和返回结果；核心业务通常在 Service 中。

- `AgentController`：AI Agent 对话接口，负责会话路由、用户身份和 AI 请求响应。
- `AIController`：传统 AI 相关接口，例如意图识别或独立 AI 能力入口。
- `CartController`：购物车增删改查、数量变更和结算前查询。
- `CommentController`：商品评论的查询、发布等接口。
- `FollowController`：关注/取消关注及关注状态接口。
- `InboxController`：站内信、消息列表和消息读取接口。
- `ItemController`：商品列表、详情、搜索、点赞和排行榜等接口。
- `OrderController`：订单创建、查询、支付状态或订单操作接口。
- `PublishController`：用户发布商品、修改商品和上传发布信息的接口。
- `SeckillController`：秒杀商品查询和秒杀下单接口。
- `UserController`：注册、密码登录、验证码登录、退出登录和用户信息接口。

## 7. `converter`：数据转换

- `ItemSearchConverter`：把普通商品搜索请求、AI 商品搜索参数等转换成统一的 `ItemSearchRequest` 或内部查询对象，避免不同入口重复拼装参数。

## 8. `dto`：请求和内部传输对象

DTO 是“传输数据的对象”，一般不直接对应数据库表。

- `CategoryDTO`：分类创建或修改时的请求数据。
- `IntentResult`：AI 意图识别结果，通常包含识别出的意图和结构化参数。
- `ItemSearchRequest`：商品搜索条件，例如关键词、分类、品牌、价格区间和排序方式。
- `SearchDTO`：通用搜索请求或搜索条件的中间对象。
- `UserDTO`：用户注册、更新或内部传输时使用的用户数据。
- `UserLoginDTO`：用户登录请求数据，如手机号/用户名和密码或验证码。

## 9. `entity`：数据库实体

Entity 主要表示数据库中的业务数据，通常由 MyBatis Mapper 读写。

- `User`：用户账号、手机号、密码、头像等用户数据。
- `Seller`：卖家/商家信息，与发布的商品关联。
- `ItemInfo`：商品信息，包括标题、价格、库存、分类、品牌、卖家和发布时间等。
- `OrderInfo`：订单及订单状态、买家、商品、金额等信息。
- `Coupon`：优惠券及其使用条件、有效期和状态。

## 10. `mapper`：MyBatis 数据库访问层

Mapper 通常是接口，具体 SQL 位于 `backend/src/main/resources/com/gooshare/mapper/*.xml`。它们负责把 Java 方法映射成 MySQL 查询，不负责完整的业务编排。

- `UserMapper`：用户查询、注册信息、手机号和密码登录相关 SQL。
- `SellerMapper`：卖家信息查询和保存。
- `BrandMapper`：品牌查询。
- `CategoryMapper`：一级分类查询、分类新增或维护。
- `CategoryItemMapper`：分类与子分类/商品分类项的关联查询。
- `ItemMapper`：商品列表、详情、筛选、库存、点赞相关的核心 SQL。
- `CommentMapper`：商品评论及评论统计查询。
- `InboxMapper`：站内信或消息数据查询和更新。
- `OrderMapper`：订单创建、订单查询、状态更新。
- `PublishMapper`：发布商品相关的数据写入和查询。
- `SeckillMapper`：秒杀商品、库存和秒杀订单相关 SQL。

## 11. `service`：业务接口

Service 接口定义业务能力，便于 Controller 调用，也便于后续替换实现或测试。

- `UserService`：用户注册、登录、验证码、令牌和用户信息业务。
- `ItemService`：商品查询、详情、点赞、收藏/相关商品业务。
- `CategoryService`：商品分类和分类项业务。
- `CartService`：购物车添加、删除、数量更新和查询。
- `CommentService`：评论发布、查询和相关业务。
- `FollowService`：关注关系处理。
- `InboxService`：站内信和消息业务。
- `OrderService`：下单、订单查询、订单状态和订单相关业务。
- `PublishService`：发布、编辑和管理商品业务。
- `SeckillService`：秒杀资格、库存扣减和秒杀下单业务。
- `IntentRecognitionService`：识别用户输入的 AI 意图。
- `KnowledgeSearchService`：从知识库进行相似内容检索。

## 12. `service.impl`：业务接口实现

这些类是上一节接口的默认实现，通常通过注入 Mapper、Redis 和其他 Service 完成具体逻辑。

- `UserServiceImpl`：实现用户注册、登录、验证码和用户信息处理。
- `ItemServiceImpl`：实现商品搜索、详情、点赞和商品相关业务。
- `CategoryServiceImpl`：实现分类查询和分类维护。
- `CartServiceImpl`：实现 Redis/数据库购物车操作。
- `CommentServiceImpl`：实现评论发布和查询。
- `FollowServiceImpl`：实现关注关系处理。
- `InboxServiceImpl`：实现站内信查询和状态变更。
- `OrderServiceImpl`：实现订单创建、查询和状态流转。
- `PublishServiceImpl`：实现发布商品和编辑商品。
- `SeckillServiceImpl`：实现秒杀流程，包括库存和订单处理。
- `IntentRecognitionServiceImpl`：调用 AI 模型并把自然语言转换为 `IntentResult`。
- `KnowledgeSearchServiceImpl`：调用 Qdrant 向量库对应的 VectorStore，返回知识检索结果。

## 13. `vo`：返回给前端的视图对象

VO 是面向页面展示的对象。它可以组合多个表的数据，也可以隐藏数据库内部字段。

- `UserLoginVO`：登录成功后返回给前端的用户和令牌信息。
- `ItemVO`：商品列表或详情展示对象，包含前端需要的商品、卖家、分类等信息。
- `CategoryVO`：一级分类展示对象。
- `CategoryItemVO`：分类下的子项或商品分类展示对象。
- `CartVO`：购物车条目展示对象。
- `CommentVO`：评论内容及评论者信息展示对象。
- `LikedVO`：点赞状态或点赞统计展示对象。
- `RankingVO`：商品排行榜展示对象。
- `ScrollResult`：滚动分页结果，通常包含数据列表和下一页游标/分页信息。

## 14. `interceptor`：请求拦截和登录校验

- `LoginInterceptor`：检查需要登录的请求是否携带有效登录状态；没有登录时阻止继续访问。
- `RefreshTokenInterceptor`：读取请求中的 Token，刷新登录有效期，并把当前用户信息放入 `UserHolder`。

两个拦截器通常配合 `MvcConfig` 注册。登录相关请求可能会被排除，不经过强制登录校验。

## 15. `consumer`：消息队列消费者

- `SeckillOrderConsumer`：消费秒杀下单消息，异步创建或处理秒杀订单，避免请求线程直接执行全部高并发数据库操作。

## 16. `utils`：通用工具

- `RedisIdWorker`：使用 Redis 生成分布式递增 ID，避免多实例部署时 ID 冲突。
- `UploadFile`：处理文件上传及文件地址/存储相关逻辑。
- `UserHolder`：保存当前请求对应的用户信息。通常由登录拦截器写入，Controller 或 Service 读取，请求结束后应清理上下文。

## 17. 看到配置和 SQL 时应该去哪里找

阅读 Java 类时，可以同时查看下面几个目录：

```text
backend/src/main/resources/application.yml
    -> Spring Boot、MySQL、Redis、DashScope、Qdrant 等配置

backend/src/main/resources/application-local.yml
    -> 本机开发配置（如果启用了 local profile）

backend/src/main/resources/com/gooshare/mapper/*.xml
    -> Mapper 对应的真实 SQL

backend/src/main/resources/knowledge/*.md
    -> 导入 Qdrant 的知识库原文

backend/src/main/resources/scripts/*.lua
    -> Redis Lua 脚本，例如秒杀原子扣库存
```

## 18. 建议的阅读顺序

如果想快速理解整个项目，建议按以下顺序阅读：

1. `GooShareApplication`，了解启动方式。
2. `application.yml`，了解数据库、Redis、模型和 Qdrant 的连接配置。
3. `UserController` + `UserServiceImpl`，理解登录和拦截器。
4. `ItemController` + `ItemServiceImpl` + `ItemMapper.xml`，理解商品查询主流程。
5. `AgentController` + `AiShoppingTools`，理解 AI 如何调用商品搜索。
6. `KnowledgeIngestionRunner` + `KnowledgeSearchServiceImpl`，理解知识库如何写入和检索。
7. `OrderServiceImpl`、`SeckillServiceImpl` 和 `SeckillOrderConsumer`，理解下单和高并发流程。

最重要的分层原则是：Controller 处理 HTTP，Service 处理业务，Mapper 处理 SQL，Entity 表示数据库数据，DTO 表示输入，VO 表示输出；AI Agent 通过 Tool 调用已有的业务能力，而不是直接把数据库操作全部写在 Controller 中。

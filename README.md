# 仿牛客网（community）
## 开发社区首页
- 社区首页主要是提供帖子查看、分页等功能
- 我们需要将没有被拉黑的帖子呈现给用户
- 没有登录的情况下也可以查看
- 重构登录模块：
  - 使用Redis存储验证码
    - 验证码需要频繁的访问与刷新，对性能要求较高
    - 验证码不需永久保存，通常在很短的时间后就会失效
    - 分布式部署时，存在Session共享的问题
  - 使用Redis存储登录凭证
    - 处理每次请求时，都要查询用户的登录凭证，访问的频率非常高
    - 每次请求都从redis里查询登录凭证，不再从session里边查，解决了分布式session问题
  - 使用Redis缓存用户信息
    - 处理每次请求时，都要根据凭证查询用户信息，访问的效率非常高。
    - 查询用户信息之前，先在redis里面查找，如果没有则再访问mysql
    - 如果修改了用户信息，则将redis缓存的数据删除
  - 使用 LocalTime 序列化时踩坑，在文档记录一下方便查阅。解决方法
    - 添加依赖包`jackson-datatype-jsr310`
    - 实体字段上添加：`@JsonDeserialize(using = LocalDateTimeDeserializer.class)` `@JsonSerialize(using = LocalDateTimeSerializer.class)`

## 开发社区登录模块
- 开启新浪邮箱的 POP3/SMTP 服务，通过 JavaMailSender 可以实现邮件的发送功能。
- 发送邮件功能，导入`spring-boot-starter-mail`依赖，在 application 中配置邮件发送信息。
- 使用 thymeleaf 提供的 context 封装HTML文件，用来发送HTML类型的邮件，TemplateEngine 指定要发送的模板，再调用 JavaMailSender 的 send 方法
- 注册功能，注册完成后需要发送邮件由用户激活该账户，激活成功后跳转到登录页面
- 使用 Hutool 工具生成登录验证码功能，再将生成的图片流写到 response.getOutputStream() 中
- 登录过程中先检查验证码是否正确，如果不正确返回提示信息，验证成功之后生成登录凭证保存到数据库中，并把登录凭证存到客户端 cookie 中，设置携带 cookie 路径和有效时间
- 退出登录功能，将用户登录凭证更新为不可用
- 登录成功后显示用户信息，拦截器的应用：
  - 在请求开始时从 cookie 中获取凭证，并查询登录用户
  - 在本次请求中持有用户数据
  - 在模板视图上显示用户数据
  - 在请求结束时清理用户数据
  - 由于tomcat是并发执行的，所以将并发访问的用户数据存入一个 ThreadLocal 中
- 修改用户信息，包括头像、密码等
  - 上传头像，将文件写入服务器存放路径，指定访问路径格式
  - 编写访问头像方法
  - 编写修改密码逻辑，更换 加密盐
- 拦截未登录资源
  - 自定义注解 LoginRequired，用来做标识必须登录才能访问的资源
  - 在拦截器中判断所请求资源是否有 LoginRequired 标识，如果有再判断是否登录，如果没有登录则重定向到登录界面

## 开发社区核心功能，发帖和评论等
- 过滤敏感词
  - 为了保障社区的友好和谐环境，不允许出现辱骂非法的词汇
  - 构建一个 txt 文件作为敏感词库，通过这个词库构建一颗前缀树
  - 每次发帖或评论都需要通过前缀树的过滤，发现敏感词则使用 *** 替换
- 发布帖子功能
- 帖子详情功能
- 事务管理
  - 什么是事务？
    - 事务是由N步数据库操作序列组成的逻辑执行单元，这系列操作要么全执行，要么全放弃执行
  - 事务的特性（ACID）
    - 原子性（Atomicity）：事务是应用中不可再分的最小执行体。
    - 一致性（Consistency）：事务执行的结果，须使数据从一个一致性状态，变为另一个一致性状态。
    - 隔离性（Isolation）：各个事务的执行互不干扰，任何事务的内部操作对其他事务都是隔离的。
    - 持久性（Durability）：事务一旦提交，对数据所做的任何改变都要记录到永久存储器中。
  - 事务的隔离性
    - 多线程并发访问同一个功能，可能会处理同一条数据，如果不做隔离性处理，将会产生问题
  - 常见的并发异常：
    - 第一类丢失更新：某一个事务的回滚，导致另外一个事务已更新的数据丢失了
    - 第二类丢失更新：某一个事务的提交，导致另外一个事务已更新的数据丢失了
    - 脏读：某一个事务，读取了另一个事务未提交的数据
    - 不可重复读：某一个事务，对同一个数据前后读取的结果不一致
    - 幻读：某一个事务，对同一个表前后查询到的行数不一致
  - 常见的隔离级别：
    - Read Uncommitted：读取未提交的数据，所有并发异常都没处理，效率高，安全性低
    - Read Committed：读取已提交的数据，处理了第一类丢失更新和脏读
    - Repeatable Read：可重复读，处理了第一类丢失更新、脏读、第二类丢失更新、不可重复读
    - Serializable：串行化，处理全部并发异常，安全性高，效率低
  - 事务管理实现机制：
    - 悲观锁（数据库）：
      - 共享锁（s锁），事务A对某数据加了共享锁后，其他事务只能对该数据加共享锁，但不能加排他锁。
      - 排他锁（X锁），事务A对某数据加了排他锁后，其他事务对该数据既不能加共享锁，也不能加排他锁。
    - 乐观锁（自定义）：
      - 版本号、时间戳等，在更新数据前，检查版本号是否发送变化，若变化则取消本次更新，否则就更新数据（版本号+1）
  - 事务的传播机制：
    - 业务方法A调用业务方法B，该以谁的事务为准
    - Spring为我们提供了七种传播机制的处理办法，并定义成枚举类，常用的有：
      - REQUIRED：支持当前事务（外部事务），如果外部事物不存在则创建新事务
      - REQUIRES_NEW：创建一个新事务，并且暂停当前事务（外部事务）
      - NESTED：如果当前存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则就会和REQUIRED一样
- 显示评论功能
  - 考虑到很多地方都会有评论这个功能，所以要设计一套通用的回复方案
  - 例如：
    - 帖子：天气真好！
    - 评论：（名字）是啊是啊（1楼）
    - 回复该评论：111
    - 评论：（名字）万里无云（2楼）
    - 回复该评论：222
  - 首先在评论表中添加一个字段用来标识评论的类型，比如：1 - 对帖子的评论，2 - 对帖子评论的评论（对帖子评论的回复也算一种类型）
  - 再使用一个 type_id 表示在谁下面的评论，target_id 表示回复谁
- 添加评论功能
  - 在本项目中一共有两种类型的评论，添加评论也是添加两种类型
  - 提交的时候区分，后端统一添加并对回帖内容进行敏感词过滤，并更新回帖数量即可
- 私信列表和私信详情功能
  - 根据当前登录的信息，获取到当前登录对象作为发送人或接收人的所有私信，再通过发送人和接收人组成的id进行分组，获取最新的那一条就是一个私信列表
  - 根据 conversationId 查询所有会话，分页展示到详情页面，再查询发送方的用户信息
- 发送私信和标识已读消息功能
  - 前端发送 ajax 请求，传入目标人的姓名和发送内容，根据目标人姓名查询目标用户信息，填充目标人id和发送人id，前缀树过滤发送内容，提交到数据库
  - 进入详情页面后，查询当前用户与私信列表的接收方是否一致，一致则修改消息状态标识为已读
- 统一处理异常
  - 在 templates 目录下 新建 error 文件夹，里边存放发生错误页面，发生错误SpringBoot能自动识别，并自动跳转到相应的页面
  - ControllerAdvice 注解，能够处理所有 Controller 下的异常
- 统一记录日志
  - AOP：面向切面编程
  - 利用AOP针对业务组件进行日志记录

## Redis，一站式高性能存储方案
- 点赞功能
  - 利用Redis的集合存储点赞数据
  - 设计Redis的key："like:key:" + "点赞的类型（是帖子还是评论）:" + "点赞的目标id（所要点赞类型的具体目标）" 
  - 设计Redis的value：把点赞人的id作为value
  - 前端需要的数据：点赞状态（当前用户是否点赞），点赞数量（相同key集合的数量）
  - ajax 异步请求实现 点赞状态的修改 点赞数量的增加和减少
  - 修改一些页面的显示
  - 点赞的总数，设计点赞总数key，记录该用户的所有帖子、评论收到的总赞数
  - 编写代码保证 Redis 事务
- 个人信息页面，关注和取消关注功能
  - 展示个人信息内容，总点赞数，被多少人关注，关注了多少人等
  - 设计某个用户关注的实体的key：followee:userId:entityType -> zset(entityId,now)
  - 设计某个实体拥有的粉丝的key：follower:entityType:entityId -> zset(userId,now)
  - 关注的时候的逻辑：我 关注了 他，则 我 需要看到我关注了多少个 类型的实体，我 被别人关注了 则需要看到 关注人的信息
  - 基于上面的逻辑，所以添加到redis时需要做事务处理，处理 '我' 和 '帖子'
  - 取消关注则与关注逻辑大同小异
  - 页面需要的信息包括：某人关注的实体的数量、该实体被关注的数量、某人是否关注该实体
- 关注列表和粉丝列表
  - 根据key分页查询 redis 中 zset 的数据，显示到页面
  - 查询某人的关注状态

## Kafka，构建TB级异步消息系统
- kafka启动方式：
  - bin\windows\zookeeper-server-start.bat config\zookeeper.properties
  - bin\windows\kafka-server-start.bat config\server.properties
  - 创建主题：
    - kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
  - 查看主题：
    - bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
  - 生产数据：
    - kafka-console-producer.bat --broker-list localhost:9092 --topic test
  - 消费数据：
    - kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning
- 阻塞队列
  - BlockingQueue
    - 解决线程通信的问题
    - 阻塞方法：put、take
  - 生产者消费者模式
    - 生产者：产生数据的线程
    - 消费者：使用数据的线程
  - 实现类：
    - ArrayBlockingQueue
    - LinkedBlockingQueue
    - PriorityBlockingQueue、SynchronousQueue、DelayQueue等
- 发送系统通知功能
  - 系统通知类型可能有多种，需要定义一个通用的实体(Event)来封装不同的事件
  - 构造事件生产者，事件消费者
  - 触发消息的事件：
    - 评论后，生成事件，发布通知
    - 点赞后，生成事件，发布通知
    - 关注后，生成事件，发布通知
- 显示系统通知功能
  - 通知列表
    - 显示评论、点赞、关注三种类型的通知
  - 通知详情
    - 分页显示某一类主题所包含的通知
  - 未读消息
    - 在页面头部显示所有的未读消息数量

## Elasticsearch,分布式搜索引擎
- Elasticsearch简介
  - 一个分布式的、Restful风格的搜索引擎
  - 支持对各种类型的数据的检索
  - 搜索速度快，可以提供实时的搜索服务
  - 便于水平扩展，每秒可以处理PB级海量数据
- 社区搜索功能
  - 搜索服务
    - 将帖子保存到 Elasticsearch 服务器
    - 从 Elasticsearch 服务器删除帖子
    - 从 Elasticsearch 服务器搜索帖子
  - 发布事件
    - 发布帖子时，将帖子异步的提交到 Elasticsearch 服务器
    - 增加评论时，将帖子异步的提交到 Elasticsearch 服务器
    - 在消费组件中增加一个方法，消费帖子发布事件
  - 显示结果
    - 在控制器中处理搜索请求，在 HTML 上显示搜索结果
## 项目进阶，构建安全高效的企业服务
- Spring Security
  - 简介
    - Spring Security是一个专注于为Java应用程序提供身份认证和授权的框架，它的强大之处在于它可以轻松扩展以满足自定义的需求
  - 特征
    - 对身份的认证和授权提供全面的、可扩展的支持
    - 防止各种攻击，如会话固定攻击、点击劫持、csrf攻击等。
    - 支持与 Servlet API、Spring MVC等web技术集成
- 权限控制
  - 登录检查
    - 之前采用拦截器实现了登录检查，这是简单的权限管理方案，现在将其废弃
  - 授权配置
    - 对当前系统内包含的所有请求，分配访问权限（普通用户、版主、管理员）
  - 认证方案
    - 绕过Security认证流程，采用系统原来的认证方案
  - CSRF配置
    - 防止 CSRF 攻击的基本原理，以及表单、AJAX相关的配置
- 置顶、加精、删除功能
  - 功能实现
    - 点击 置顶，修改帖子类型
    - 点击 加精、删除，修改帖子的状态
  - 权限管理
    - 版主可以执行“置顶”、“加精”操作
    - 管理员可以执行“删除”操作
  - 按钮显示
    - 版主可以看到“置顶”、“加精”按钮
    - 管理员可以看到删除按钮
- Redis高级数据类型
  - HyperLogLog
    - 采用一种基数算法，用于完成独立总数的统计
    - 占据空间小，无论统计多少个数据，只占12k的内存空间
    - 不精确的统计算法，标准误差为 0.81%
  - Bitmap
    - 不是一种独立的数据结构，实际上就是字符串
    - 支持按位存取数据，可以将其看成是 byte 数组
    - 适合存储索大量的连续的数据的布尔值
- Redis高级数据类型应用：网站数据统计功能
  - UV(Unique Visitor)
    - 独立访客，需通过用户IP排重统计数据
    - 每次访问都要进行统计
    - HyperLogLog，性能好，且存储空间小
  - DAU(Daily Active User)
    - 日活跃用户，需通过用户ID排重统计数据。
    - 访问过一次，则认为其活跃
    - Bitmap，性能好、且可以统计精确的结果
- 任务执行和调度
  - JDK线程池
    - ExecutorService
    - ScheduledExecutorService
  - Spring线程池
    - ThreadPoolTaskExecutor
    - ThreadPollTaskScheduler
  - 分布式定时任务
    - Spring Quartz
- 热帖排行功能
  - 分值计算公式：log(精华分+评论数*10+点赞数*2) + (发布时间-牛客纪元)
  - 使用定时任务去计算分值
  - 将一定时间内存在变化的帖子存到缓存中，每次只计算变化的帖子，避免重复计算无变化的帖子
- 将图片文件等上传到七牛云服务器
  - from 表单直接提交
  - 应用服务器上传到七牛云服务器
- 优化网站的性能
  - 本地缓存
    - 将数据缓存在应用服务器上，性能最好
    - 常用缓存工具：Ehcache、Guava、Caffeine等
  - 分布式缓存
    - 将数据缓存在NoSQL数据库上，跨服务器
    - 常用缓存工具：MemCache、Redis等
  - 多级缓存
    - 一级缓存（本地缓存）> 二级缓存（分布式缓存）> DB
    - 避免缓存雪崩（缓存失败，大量请求直达DB），提高系统的可用性
- 优化热帖排行
  - 增加本地缓存caffeine缓存热帖数据
  - 设置缓存定期清理
  - 增加50万帖子数据，降低数据库访问速度
  - 使用压测工具，分别测试加本地缓存前后
  - ![img.png](img.png)
- 完善我的帖子、我的回复等功能
## 项目发布与总结
- 调整项目，准备部署

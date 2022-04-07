# 仿牛客网（community）
## 开发社区首页
- 社区首页主要是提供帖子查看、分页等功能
- 我们需要将没有被拉黑的帖子呈现给用户
- 没有登录的情况下也可以查看
- 留个伏笔：redis优化，后面补充

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
- 显示评论功能



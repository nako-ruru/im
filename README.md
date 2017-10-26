# im
### 客户端在房间文本聊天
用户在进入某一房间后，客户端须按照如下流程初始化会话
> 1. 客户端应当向47.92.98.23:6000发起连接，
> 1. 注册推送消息回调
> 1. 向服务器登记用户id
> 1. 向服务器告知进入房间id
> 1. 接着就是随意聊天。

java/android代码可参考[ClientTest.java](https://github.com/nako-ruru/im/blob/master/connector/src/test/java/ClientTest.java "ClientTest.java")

同时提取了比较方便的[MessageUtils.java](https://github.com/nako-ruru/im/blob/master/performance_test/src/main/java/com/mycompany/im/util/MessageUtils.java "MessageUtils.java")供java/android开发者使用
其中
- 注册推送消息回调
```java
public static void pushCallback(DataInputStream in, Consumer<Msg> consumer, Consumer<Exception> eConsumer)
```
- 向服务器登记用户id
```java
public static void register(DataOutput out, String userId, String token, String clientVersion) throws IOException
```
- 刷新token
```java
public static void refreshToken(DataOutput out, String token) throws IOException
```
- 进入房间
```java
public static void enter(DataOutput out, String roomId) throws IOException
```
- 普通聊天
```java
public static void chat(DataOutput out, @Deprecated String roomId, String content, String nickname, int level) throws IOException
```
> 注意：chat方法中的roomId已经标记为过时

### 客户端在房间接收消息(文本聊天或业务消息)
接收消息分为两种。
> 1. 一种是文本聊天或者实时性要求不高的业务消息，这种消息通过http polling方式来获取；
> 1. 另一种则是通过接收服务器主动推送业务消息

> 注意：这两种方式应该在客户端同时存在，而不是选择其中一种

下面我们来详细说明两种方式的获取方式。
#### http polling
假设用户进入房间的id是24
- 第一次进入房间时以**GET**访问*http://47.92.98.23:8080/message/a?roomId=24&from=0*
返回结果如下
```json
[
    {
        "toRoomId": "24",
        "fromUserId": "",
        "fromLevel": 0,
        "type": 10001,
        "params": {},
        "time": 1504601256272,
        "roomId": "24",
        "userId": "",
        "level": 0
    }
]
```
> 应当注意的是roomId, userId, level均是已废弃的属性，鉴于目前暂时兼容旧的数据格式，我们临时保留一段时间
- 接着定时轮询，from的值为上一次结果最后一条消息的**time+1**(最大的time + 1)
```json
[
    {
        "toRoomId": "24",
        "fromUserId": "3",
        "fromNickname": "刘德华",
        "fromLevel": 0,
        "type": 1,
        "params": {
            "content": "这位主播不错"
        },
        "time": 1504601669582,
        "roomId": "24",
        "userId": "3",
        "nickname": "",
        "level": 4
    },
    {
        "toRoomId": "24",
        "fromUserId": "4",
        "fromNickname": "Enya",
        "fromLevel": 0,
        "type": 1,
        "params": {
            "content": "主播居然唱我的成名曲<Only Time>...，缴纳版权费了么？"
        },
        "time": 1504601671381,
        "roomId": "24",
        "userId": "4",
        "nickname": "",
        "level": 7
    },
    {
        "toRoomId": "24",
        "fromUserId": "13",
        "fromNickname": "酱油粉",
        "fromLevel": 1,
        "type": 1,
        "params": {
            "content": "好饿啊，我还没吃晚饭，我先闪了，88"
        },
        "time": 1504601675281,
        "roomId": "24",
        "userId": "13",
        "nickname": "",
        "level": 1
    }
]
```
> 和第一种情况一样，roomId, userId, level均是已废弃的属性

##### 推送
在发送消息一栏已经提到过注册推送消息回调，此后就能正常接收来自服务器通过推送管道下发的消息，其格式如下：
```json
{
    "userId": "",
    "roomId": "7c52118f-1b8e-46aa-9413-8931a67fe88e",
    "content": "%7B%22type%22%3A80%2C%22data%22%3A%7B%22actorId%22%3A%2227%22%7D%7D",
    "toUserId": "",
    "toRoomId": "7c52118f-1b8e-46aa-9413-8931a67fe88e",
    "params": {
        "content": "%7B%22type%22%3A80%2C%22data%22%3A%7B%22actorId%22%3A%2227%22%7D%7D"
    }
}
```
> 注意：userId, roomId, content均已废弃，目前短暂保留

> content是业务方发送的消息，采用BASE64方式编码。事实上content格式不受任何限制，只要和客户端沟通好具体编码解码方式即可。

### 业务方发送业务消息
业务方发送业务消息目前是通过restful api来完成的，稍后我们会提供mq方式。

客户端接收消息有两种途径，同样业务消息的下发也有两种途径。
> 然而由哪种途径下发并非由业务方决定，业务方仅仅能决定这条消息的重要性(importance)；系统根据当前负载和这条消息的重要性来决定这条消息由哪个途径给客户端。

> 重要性值越小则重要程度越大。重要性最高程度的值为0，但没有严格要求最低程度的值。

下面是业务方发送业务消息的相关接口
- 发送消息给指定房间，则以**POST**访问*http://47.92.98.23:8080/router/send?roomId=3&impotance=8&content=xxxxx*
- 发送消息给世界，则以**POST**访问*http://47.92.98.23:8080/router/sendall?impotance=8&content=xxxxx*
- 发送消息给指定用户，则以**POST**访问*http://47.92.98.23:8080/router/sendtouser?userId=8&content=xxxxx*

>注意：content应当用UTF8 URLEncoding编码

### 设定业务方消息路由到推送途径的消息级别阈值
由于业务方发送消息会指定重要性，那么系统如何根据重要性来确定由哪个途径下发到客户端呢？具体规则如下：
> 当业务方发送的消息的重要性大于系统设定的阈值时，消息会由推送管道下发到客户端；反之则由http poll管道下发到客户端

如何设定系统阈值？目前我们通过人工的方式设定消息阈值。具体方式是通过restful api来设定

假设我们要设定通过推送管道下发消息的重要性的阈值为4，则以**POST**访问
*http://47.92.98.23:8080/router/importance?threshold=4*


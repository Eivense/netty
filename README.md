# 用于Netty 学习

## Netty中的ChannelHandler

- SslHandler  负责对请求进行加密和解密，是放在ChannelPipeline中的第一个ChannelHandler

- HttpClientCodec和HttpServerCodec
  - HttpServerCodec中整合了HttpResponseEncoder和HttpRequestDecoder
  - HttpClientCodec中整合了HttpRequestEncoder和HttpResponseDecoder
  
- HttpObjectAggregator  负责将http聚合成完整的消息，而不是原始的多个部分

- HttpContentCompressor和HttpContentDecompressor
  - HttpContentCompressor  用于服务器压缩数据
  - HttpContentDecompressor  用于客户端解压数据

- IdleStateHandler  连接空闲时间过长，触发IdleStateEvent事件

- ReadTimeoutHandler  指定时间内没有收到任何的入站数据，抛出ReadTimeoutException异常,并关闭channel

- WriteTimeoutHandler  指定时间内没有任何出站数据写入，抛出WriteTimeoutException异常，并关闭channel

- DelimiterBasedFrameDecoder  使用任何用户提供的分隔符来提取帧的通用解码器

- FixedLengthFrameDecoder  提取在调用构造函数时的定长帧

- ChunkedWriteHandler  将大型文件从文件系统复制到内存

- HttpServerExpectContinueHandler 用于处理100-continue协议
  - 100-continue协议 用于征询服务器是否处理POST类型数据，常用于POST大数据时

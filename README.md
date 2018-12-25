# 用于Netty 学习

## Netty中的ChannelHandler

- HttpClientCodec和HttpServerCodec
  - HttpServerCodec中整合了HttpResponseEncoder和HttpRequestDecoder
  - HttpClientCodec中整合了HttpRequestEncoder和HttpResponseDecoder
  
- HttpObjectAggregator: 负责将http聚合成完整的消息，而不是原始的多个部分

- HttpContentCompressor和HttpContentDecompressor:HttpContentCompressor用于服务器压缩数据，HttpContentDecompressor用于客户端解压数据

- IdleStateHandler:连接空闲时间过长，触发IdleStateEvent事件

- ReadTimeoutHandler:指定时间内没有收到任何的入站数据，抛出ReadTimeoutException异常,并关闭channel

- WriteTimeoutHandler:指定时间内没有任何出站数据写入，抛出WriteTimeoutException异常，并关闭channel

- DelimiterBasedFrameDecoder:使用任何用户提供的分隔符来提取帧的通用解码器

- FixedLengthFrameDecoder:提取在调用构造函数时的定长帧

- ChunkedWriteHandler：将大型文件从文件系统复制到内存【DefaultFileRegion进行大型文件传输】

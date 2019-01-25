package initializer;

import handler.HttpProxyClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;


/**
 *  解析http 之后再进行转发
 */
public class HttpProxyClientInitializer extends ChannelInitializer<SocketChannel> {

  private Channel clientChannel;
  private String host;
  private int port;

  public HttpProxyClientInitializer(Channel clientChannel,
      String host,int port) {
    this.clientChannel = clientChannel;
    this.host=host;
    this.port=port;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline=ch.pipeline();

    SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();

    pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc(), host, port));

    pipeline.addLast("clientCodec",new HttpClientCodec());

    //把多个消息转换成一个FullHttpRequest或者是FullHttpResponse
    pipeline.addLast("aggregator",new HttpObjectAggregator(1024*1024));

    pipeline.addLast(new HttpProxyClientHandler(clientChannel));
  }
}

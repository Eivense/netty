package initializer;

import handler.HttpProxyClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;

public class HttpProxyClientInitializer extends ChannelInitializer<SocketChannel> {

  private Channel clientChannel;
  private ProxyHandler proxyHandler;

  public HttpProxyClientInitializer(Channel clientChannel,
      ProxyHandler proxyHandler) {
    this.clientChannel = clientChannel;
    this.proxyHandler = proxyHandler;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline=ch.pipeline();


    pipeline.addLast(new HttpProxyClientHandler(clientChannel));
  }
}

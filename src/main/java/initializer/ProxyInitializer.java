package initializer;

import handler.HttpProxyClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.proxy.ProxyHandler;


/**
 * 直接转发 不解析http
 */
public class ProxyInitializer extends ChannelInitializer<SocketChannel> {

  private Channel clientChannel;
  private ProxyHandler proxyHandler;

  public ProxyInitializer(Channel clientChannel,
      ProxyHandler proxyHandler) {
    this.clientChannel = clientChannel;
    this.proxyHandler = proxyHandler;
  }


  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline=ch.pipeline();

    if(proxyHandler!=null){
      pipeline.addLast("proxy",proxyHandler);
    }
    ch.pipeline().addLast(new HttpProxyClientHandler(clientChannel));

  }
}

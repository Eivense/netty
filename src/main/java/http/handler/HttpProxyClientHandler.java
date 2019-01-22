package http.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {

  private Channel clientChannel;

  public HttpProxyClientHandler(Channel clientChannel) {
    this.clientChannel = clientChannel;
  }


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    FullHttpResponse response= (FullHttpResponse) msg;
    response.headers().add("test","from proxy");
    clientChannel.writeAndFlush(msg);
  }


}

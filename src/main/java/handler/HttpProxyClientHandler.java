package handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * 用于数据的转发
 */
@Sharable
public class HttpProxyClientHandler extends ChannelInboundHandlerAdapter {

  private Channel clientChannel;

  public HttpProxyClientHandler(Channel clientChannel) {
    this.clientChannel = clientChannel;
  }


  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    clientChannel.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    clientChannel.write(msg);
  }
}

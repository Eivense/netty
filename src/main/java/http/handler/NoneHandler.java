package http.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NoneHandler extends ChannelInboundHandlerAdapter {

  private Channel outChannel;

  public NoneHandler(Channel outChannel) {
    this.outChannel = outChannel;
  }


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    outChannel.write(msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    outChannel.flush();
  }

}

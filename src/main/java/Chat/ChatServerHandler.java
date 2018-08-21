package Chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    public static ChannelGroup channels=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming=ctx.channel();
        channels.writeAndFlush("[Server]-"+incoming.remoteAddress()+"join\n");
        channels.add(ctx.channel());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming=ctx.channel();
        channels.writeAndFlush("[Server]-"+incoming.remoteAddress()+"leave\n");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel incoming=ctx.channel();
        for(Channel channel:channels){
            if(channel!=incoming){
                channel.writeAndFlush("["+incoming.remoteAddress()+"]"+msg+"\n");
            }else {
                channel.writeAndFlush("[you]"+msg+"\n");
            }
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("ChatClient:"+incoming.remoteAddress()+"在线");
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        ctx.writeAndFlush(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
                        ctx.writeAndFlush(
                                "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite.\n");

                        channels.add(ctx.channel());
                    }
                });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("ChatClient:"+incoming.remoteAddress()+"掉线");
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        System.out.println("ChatClient:"+incoming.remoteAddress()+"异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}

package handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import server.HttpProxyServer;

public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture channelFuture;
    private String host;
    private int port;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            HttpRequest request=(HttpRequest)msg;
            String host=request.headers().get("host");
            String[] temp=host.split(":");
            int port=80;
            if(temp.length>1){
                port=Integer.parseInt(temp[1]);
            }else{
                if(request.uri().indexOf("https")==0){
                    port=443;
                }
            }
            this.host=temp[0];
            this.port=port;

            if("CONNECT".equalsIgnoreCase(request.method().name())){//HTTPS建立代理握手
                HttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpProxyServer.SUCCESS);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("httpObject");
                return;
            }

            //连接至目标服务器
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())//注册线程池
                    .channel(ctx.channel().getClass())
                    .handler(new HttpProxyInitializer(ctx.channel()));

            channelFuture=bootstrap.connect(this.host,this.port);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        future.channel().writeAndFlush(msg);
                    }else{
                        ctx.channel().close();
                    }
                }
            });
        }else{
            if(channelFuture==null){
                Bootstrap bootstrap=new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .handler(new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ctx.channel().writeAndFlush(msg);
                                    }
                                });
                            }
                        });
                channelFuture=bootstrap.connect(this.host,this.port);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            future.channel().writeAndFlush(msg);
                        }else{
                            ctx.channel().close();
                        }
                    }
                });
            }
        }
    }
}

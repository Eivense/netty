package handler;

import initializer.HttpProxyClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {


    private HttpRequest httpRequest;

    private String host;
    //默认端口为80
    private int port=80;


    public static final HttpResponseStatus SUCCESS=new HttpResponseStatus(200,"Connection established");

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){

            httpRequest= (HttpRequest) msg;

            log.info(httpRequest.uri());

            //获取请求头中的Host字段
            String headerHost = httpRequest.headers().get("Host");

            //可能有请求是 host:port的情况，
            String[] split = headerHost.split(":");
            host = split[0];
            if (split.length > 1) {
                port = Integer.valueOf(split[1]);
            }

            Promise<Channel> promise = createPromise(ctx, host, port);

            //HTTPS建立代理握手
            if (httpRequest.method().equals(HttpMethod.CONNECT)) {

                promise.addListener((FutureListener<Channel>) future -> {
                    //首先向浏览器发送一个200的响应
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,SUCCESS);
                    //向浏览器发送同意连接的响应，并在发送完成后移除handler

                    ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                        ChannelPipeline p = ctx.pipeline();
                        p.remove("serverCodec");
                        p.remove("aggregator");
                        p.remove("httpProxyServerHandler");
                    });
                    ChannelPipeline p = ctx.pipeline();

                    p.addLast(new HttpProxyClientHandler(future.getNow()));
                });
            }else{
                //TODO
            }
        }
    }



    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(ChannelHandlerContext ctx,String host,int port) {
        Promise<Channel> promise=ctx.executor().newPromise();
        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
            .channel(NioSocketChannel.class)
            .remoteAddress(host, port)
            .handler(new HttpProxyClientInitializer(ctx.channel(),host,port))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        ChannelFuture channelFuture=bootstrap.connect();

        channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    promise.setSuccess(future.channel());
                } else {
                    future.channel().close();
                }
            });
        return promise;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

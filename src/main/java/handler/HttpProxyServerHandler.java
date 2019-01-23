package handler;

import initializer.HttpProxyClientInitializer;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import proxy.ProxyHandlerFactory;
import proxy.ProxyType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.ProxyHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HttpProxyServerHandler extends SimpleChannelInboundHandler<HttpObject> {


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
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){

            httpRequest= (HttpRequest) msg;

            if(HttpUtil.is100ContinueExpected(httpRequest)){
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                ctx.write(response);
            }

            //获取请求头中的Host字段
            String headerHost = httpRequest.headers().get("Host");

            //可能有请求是 host:port的情况，
            String[] split = headerHost.split(":");
            host = split[0];
            if (split.length > 1) {
                port = Integer.valueOf(split[1]);
            }

            log.info(httpRequest.uri());

//            Promise<Channel> promise = createPromise(ctx,host, port);

            //HTTPS建立代理握手
            if (httpRequest.method().equals(HttpMethod.CONNECT)) {

                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,SUCCESS);
                ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture1 -> {
                        ChannelPipeline p = ctx.pipeline();
                        p.remove("serverCodec");
                        p.remove("httpProxyServerHandler");
                    });
//                promise.addListener((FutureListener<Channel>) channelFuture -> {
//                    //首先向浏览器发送一个200的响应，证明已经连接成功了，可以发送数据了
//                    FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,SUCCESS);
//                    //向浏览器发送同意连接的响应，并在发送完成后移除httpcode和httpservice两个handler
//
//                    ctx.writeAndFlush(resp).addListener((ChannelFutureListener) channelFuture1 -> {
//                        ChannelPipeline p = ctx.pipeline();
//                        p.remove("serverCodec");
//                        p.remove("httpProxyServerHandler");
//                    });
//                    ChannelPipeline p = ctx.pipeline();
//                    p.addLast(new HttpProxyClientHandler(channelFuture.getNow()));
//                });
            }
        }else if(msg instanceof HttpContent){

        }
    }


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(ChannelHandlerContext ctx,String host,int port) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        ProxyHandler proxyHandler= ProxyHandlerFactory.getHandler(ProxyType.HTTP);
        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())//注册线程池
            .channel(NioSocketChannel.class)
            .remoteAddress(host, port)
            .handler(new HttpProxyClientInitializer(ctx.channel(),proxyHandler))
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

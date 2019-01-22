package http.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    //保留全局ctx
    private ChannelHandlerContext ctx;

    private HttpHeaders headers;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;



    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){

            httpRequest= (HttpRequest) msg;

            //获取请求头中的Host字段
            String headerHost = httpRequest.headers().get("Host");

            //端口默认80
            int port = 80;

            //可能有请求是 host:port的情况，
            String[] split = headerHost.split(":");
            String host = split[0];
            if (split.length > 1) {
                port = Integer.valueOf(split[1]);
            }

            log.info(httpRequest.uri());

            //根据请求中的host和port建立连接
            Promise<Channel> promise = createPromise(host, port);

            //HTTPS建立代理握手
            if (httpRequest.method().equals(HttpMethod.CONNECT)) {
                promise.addListener((FutureListener<Channel>) future -> {
                    //首先向浏览器发送一个200的响应，证明已经连接成功了，可以发送数据了
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200, "OK"));
                    //向浏览器发送同意连接的响应，并在发送完成后移除httpcode和httpservice两个handler
                    ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                        ChannelPipeline p = ctx.pipeline();
//                            p.remove("aggregator");
//                            p.remove("100-continue");
//                            p.remove("compressor");
//                            p.remove("handler");
                    });
                    ChannelPipeline p = ctx.pipeline();
                    //将客户端channel添加到转换数据的channel，（这个NoneHandler是自己写的）
                    p.addLast(new HttpProxyClientHandler(future.getNow()));
                });
            }else{
                EmbeddedChannel em = new EmbeddedChannel(new HttpRequestEncoder());
                em.writeOutbound(httpRequest);
                final Object o = em.readOutbound();
                em.close();
                promise.addListener((FutureListener<Channel>) channelFuture -> {
                    //移除	httpcode	httpservice 并添加	HttpProxyClientHandler，并向服务器发送请求的byte数据
                    ChannelPipeline p = ctx.pipeline();
                    p.remove("httpcode");
                    p.remove("httpservice");
                    //添加handler
                    p.addLast(new HttpProxyClientHandler(channelFuture.getNow()));
                    channelFuture.get().writeAndFlush(o);
                });
            }
        }
    }


    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        final Promise<Channel> promise = ctx.executor().newPromise();

        Bootstrap bootstrap=new Bootstrap();

        bootstrap.group(ctx.channel().eventLoop())//注册线程池
            .channel(NioSocketChannel.class)
            .remoteAddress(host, port)
            .handler(new HttpProxyClientHandler(ctx.channel()))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        ChannelFuture channelFuture=bootstrap.connect();

        channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    promise.setSuccess(future.channel());
                } else {
                    ctx.close();
                    future.cancel(true);
                }
            });
        return promise;
    }

}

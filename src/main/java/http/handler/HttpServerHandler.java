package http.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    //保留全局ctx
    private ChannelHandlerContext ctx;
    //创建一会用于连接web服务器的	Bootstrap
    private Bootstrap b = new Bootstrap();


    private HttpHeaders headers;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;


    private static final String FAVICON_ICO = "/favicon.ico";

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");



    //channelActive方法中将ctx保留为全局变量
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){

            httpRequest= (HttpRequest) msg;

            //获取请求头中的Host字段
            String headerHost = httpRequest.headers().get("Host");
            String host = "";

            //端口默认80
            int port = 80;

            //可能有请求是 host:port的情况，
            String[] split = headerHost.split(":");
            host = split[0];
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
                    ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            ChannelPipeline p = ctx.pipeline();
//                            p.remove("aggregator");
//                            p.remove("100-continue");
//                            p.remove("compressor");
//                            p.remove("handler");
                        }
                    });
                    ChannelPipeline p = ctx.pipeline();
                    //将客户端channel添加到转换数据的channel，（这个NoneHandler是自己写的）
                    p.addLast(new NoneHandler(future.getNow()));
                });
            }else{
                EmbeddedChannel em = new EmbeddedChannel(new HttpRequestEncoder());
                em.writeOutbound(httpRequest);
                final Object o = em.readOutbound();
                em.close();
                promise.addListener(new FutureListener<Channel>() {
                    @Override
                    public void operationComplete(Future<Channel> channelFuture) throws Exception {
                        //移除	httpcode	httpservice 并添加	NoneHandler，并向服务器发送请求的byte数据
                        ChannelPipeline p = ctx.pipeline();
                        p.remove("httpcode");
                        p.remove("httpservice");
                        //添加handler
                        p.addLast(new NoneHandler(channelFuture.getNow()));
                        channelFuture.get().writeAndFlush(o);
                    }
                });
            }
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }



    //根据host和端口，创建一个连接web的连接
    private Promise<Channel> createPromise(String host, int port) {
        final Promise<Channel> promise = ctx.executor().newPromise();

        b.group(ctx.channel().eventLoop())
            .channel(NioSocketChannel.class)
            .remoteAddress(host, port)
            .handler(new NoneHandler(ctx.channel()))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .connect()
            .addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    promise.setSuccess(channelFuture.channel());
                } else {
                    ctx.close();
                    channelFuture.cancel(true);
                }
            });
        return promise;
    }

}

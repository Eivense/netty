package http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private HttpHeaders headers;
    private HttpRequest request;
    private FullHttpRequest fullHttpRequest;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

    }
}

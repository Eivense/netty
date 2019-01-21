package http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;
import java.util.Map.Entry;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    private HttpHeaders headers;
    private HttpRequest httpRequest;

    private static final String FAVICON_ICO = "/favicon.ico";

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){
            httpRequest= (HttpRequest) msg;
            headers=httpRequest.headers();
            String uri=httpRequest.uri();
            log.info("http uri:"+uri);

            if(uri.equals(FAVICON_ICO)){
                return;
            }

            for(Entry<String,String> headerEntry:headers.entries()){
                log.info(headerEntry.getKey()+" : "+headerEntry.getValue());
            }


        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}

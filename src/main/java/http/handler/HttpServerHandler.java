package http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;


@Log4j2
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    private HttpHeaders headers;
    private HttpRequest request;
    private FullHttpRequest fullHttpRequest;

    private static final String FAVICON_ICO = "/favicon.ico";


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){
            request= (HttpRequest) msg;
            headers=request.headers();
            String uri=request.uri();
            log.info("http uri:"+uri);

            if(uri.equals(FAVICON_ICO)){
                return;
            }
            HttpMethod method=request.method();
            if(method.equals(HttpMethod.GET)){
                QueryStringDecoder queryDecoder=new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
            }
        }
    }
}

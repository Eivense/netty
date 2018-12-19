package http.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;

import java.util.List;
import java.util.Map;


@Log4j2
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    private HttpHeaders headers;
    private HttpRequest request;
    private FullHttpRequest fullHttpRequest;

    private static final String FAVICON_ICO = "/favicon.ico";

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");


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
                Map<String, List<String>> uriAttributes=queryDecoder.parameters();

                for(Map.Entry<String,List<String>> attr:uriAttributes.entrySet()){
                    for(String attrValue:attr.getValue()){
                        log.info(attr.getKey()+"="+attrValue);
                    }
                }
            }else if(method.equals(HttpMethod.POST)){
                //POST请求,由于你需要从消息体中获取数据,因此有必要把msg转换成FullHttpRequest
                fullHttpRequest= (FullHttpRequest) msg;

            }

            byte[] content="aaaa".getBytes();

            FullHttpResponse response=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.wrappedBuffer(content));
            response.headers().set(CONTENT_TYPE,"text/plain");
            response.headers().setInt(CONTENT_LENGTH,response.content().readableBytes());

            boolean keepAlive=HttpUtil.isKeepAlive(request);
            if(!keepAlive){
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }else{
                response.headers().set(CONNECTION,KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}

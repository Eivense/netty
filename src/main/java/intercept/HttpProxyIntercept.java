package intercept;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

import java.nio.channels.Channel;

public class HttpProxyIntercept {

    /**
     * 拦截代理服务器到目标服务器的请求头
     */
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        pipeline.beforeRequest(clientChannel, httpRequest);
    }

    /**
     * 拦截代理服务器到目标服务器的请求体
     */
    public void beforeRequest(Channel clientChannel, HttpContent httpContent,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        pipeline.beforeRequest(clientChannel, httpContent);
    }

    /**
     * 拦截代理服务器到客户端的响应头
     */
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse,
                              HttpProxyInterceptPipeline pipeline) throws Exception {
        pipeline.afterResponse(clientChannel, proxyChannel, httpResponse);
    }


    /**
     * 拦截代理服务器到客户端的响应体
     */
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent,
                              HttpProxyInterceptPipeline pipeline)
            throws Exception {
        pip    }
eline.afterResponse(clientChannel, proxyChannel, httpContent);
}

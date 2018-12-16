package intercept;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Iterator;
import java.util.List;

public class HttpProxyInterceptPipeline implements Iterable<HttpProxyIntercept> {

    private List<HttpProxyIntercept> intercepts;
    private HttpProxyIntercept defaultIntercept;

    private int posBeforeHead = 0;
    private int posBeforeContent = 0;
    private int posAfterHead = 0;
    private int posAfterContent = 0;

    private RequestProto requestProto;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;

    

    @Override
    public Iterator<HttpProxyIntercept> iterator() {
        return null;
    }
}

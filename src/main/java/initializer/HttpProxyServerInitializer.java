package initializer;

import handler.HttpProxyServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

public class HttpProxyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch){
        ChannelPipeline pipeline=ch.pipeline();

        /*
         *或者使用HttpRequestDecoder & HttpResponseEncoder
         * 对request解码 HttpRequestDecoder
         * 对response编码 HttpResponseEncoder
         */
        pipeline.addLast("serverCodec",new HttpServerCodec());

        //把多个消息转换成一个FullHttpRequest或者是FullHttpResponse
        pipeline.addLast("aggregator",new HttpObjectAggregator(1024*1024));
////
////
////        //用于处理http 100-continue
////        pipeline.addLast("100-continue",new HttpServerExpectContinueHandler());
////
////        /*
////         * 压缩
////         *
////         * 根据HttpRequest中的Accept-Encoding
////         * 对HttpMessage和HttpContent进行压缩
////         * 支持gzip和deflate
////         */
////        pipeline.addLast("compressor", new HttpContentCompressor());



        pipeline.addLast("httpProxyServerHandler",new HttpProxyServerHandler());
    }
}

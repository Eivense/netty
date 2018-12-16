package server;

import handler.HttpProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpProxyServer {

    private static int PORT=8080;

    public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
            "Connection established");

    public static void main(String[] args) {
        EventLoopGroup bossGroup=new NioEventLoopGroup();//用于接收TCP请求
        EventLoopGroup workerGroup=new NioEventLoopGroup(2);//用于处理获取到的连接

        try{
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,100)//tcp中的listen函数的backlog参数 指已连接但是未进行accept处理的socket队列大小
                    .option(ChannelOption.TCP_NODELAY,true)//对应nagle算法
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast("httpCodec",new HttpServerCodec());
                            ch.pipeline().addLast("httpObject",new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("serverHandle",new HttpProxyServerHandler());
                        }
                    });

            ChannelFuture future=serverBootstrap.bind(PORT).sync();
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

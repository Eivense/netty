package server;

import initializer.HttpProxyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServer {

    private int port;


    public HttpServer(int port) {
        this.port=port;
    }

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpProxyServerInitializer());


            Channel ch=b.bind(port).sync().channel();
            log.info("Netty http server listening on port "+ port);
            ch.closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        HttpServer httpServer=new HttpServer(8000);
        httpServer.start();
    }



}

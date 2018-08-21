package Chat;

import Util.Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;


public class ChatClient {
    static final String host=System.getProperty("host","127.0.0.1");
    static final int port=Integer.parseInt(System.getProperty("port","8080"));



    public void run(){
        EventLoopGroup group=new NioEventLoopGroup();
        try{
            final SslContext sslContext= SslContextBuilder.forClient().trustManager(Util.getCertficate()).build();
            Bootstrap bootstrap=new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChatClientInitializer(sslContext));
            Channel channel=bootstrap.connect(host,port).sync().channel();
            BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
            while(true){
                channel.writeAndFlush(in.readLine()+"\r\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatClient().run();
    }
}

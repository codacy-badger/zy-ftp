package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.server.codec.FtpServerCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * The ftp server
 *
 * @author yangziwen
 */
@Slf4j
public class FtpServer {

	private final FtpServerContext serverContext;

	private final ServerBootstrap serverBootstrap;

	private final EventLoopGroup bossEventLoopGroup;

	private final EventLoopGroup workerEventLoopGroup;

	public FtpServer(FtpServerContext serverContext) {
		this.serverContext = serverContext;
		this.serverBootstrap = new ServerBootstrap();
		this.bossEventLoopGroup = new NioEventLoopGroup(1);
		this.workerEventLoopGroup = new NioEventLoopGroup(16);
		serverContext.setServer(this);
	}

	/**
	 * Start the ftp server
	 * @throws Exception
	 */
	public void start() throws Exception {
		this.serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.option(ChannelOption.SO_REUSEADDR, true)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			.localAddress(this.serverContext.getServerConfig().getLocalAddress())
			.childHandler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					channel.pipeline()
						.addLast(new FtpServerCodec())
						.addLast(new IdleStateHandler(0, 0, serverContext.getServerConfig().getMaxIdleSeconds()))
						.addLast(new FtpServerHandler(serverContext));
				}
			});
		this.serverBootstrap.bind().sync().addListener(f -> {
			log.info("ftp server is started and listening {}", this.serverContext.getServerConfig().getLocalAddress());
		});
	}

	/**
	 * Stop the ftp server
	 */
	public void stop() {
		try {
			this.workerEventLoopGroup.shutdownGracefully().sync();
		} catch (Exception e) {
			log.error("failed to shutdown the worker event loop group", e);
		}
		try {
			this.bossEventLoopGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			log.error("failed to shutdown the boss event loop group", e);
		}
		try {
			this.serverContext.getPassivePorts().destroy().sync();
		} catch (InterruptedException e) {
			log.error("failed to destroy the PassivePorts instance");
		}
		log.info("ftp server is stopped");
	}

}

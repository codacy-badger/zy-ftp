package io.github.yangziwen.zyftp.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import lombok.Data;

@Data
public class FtpServerConfig {

	public static final long DEFAULT_UPLOAD_BYTES_PER_SECOND = 200 * 1024L;

	public static final long DEFAULT_DOWNLOAD_BYTES_PER_SECOND = 200 * 1024L;

	// TODO 参数需要可配置

	private SocketAddress localAddress;

	private int maxIdleSeconds = 120;

	private int dataConnectionMaxIdleSeconds = 30;

	private String passiveAddress = "127.0.0.1";

	private String passivePortsString = "40000-50000";

	private ConnectionConfig connectionConfig;

	public FtpServerConfig() {
		this.localAddress = new InetSocketAddress("0.0.0.0", 8121);
		this.connectionConfig = new ConnectionConfig();
	}

	@Data
	public static class ConnectionConfig {

		private boolean isAnonymousEnabled = true;

		private int maxAnonymousLogins = 20;

		private int maxLogins = 50;

	}

}

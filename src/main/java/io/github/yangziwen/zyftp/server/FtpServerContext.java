package io.github.yangziwen.zyftp.server;

import io.github.yangziwen.zyftp.command.CommandFactory;
import io.github.yangziwen.zyftp.config.FtpServerConfig;
import io.github.yangziwen.zyftp.filesystem.FileSystemManager;
import io.github.yangziwen.zyftp.message.MessageResource;
import io.github.yangziwen.zyftp.user.UserManager;
import lombok.Getter;

@Getter
public class FtpServerContext {

	private FtpServerConfig serverConfig;
	
	private UserManager userManager;
	
	private FileSystemManager fileSystemManager;
	
	private CommandFactory commandFactory;
	
	private MessageResource messageResource;
	
	public FtpServerContext() {
		this.serverConfig = new FtpServerConfig();
		this.messageResource = new MessageResource();
	}
	
}

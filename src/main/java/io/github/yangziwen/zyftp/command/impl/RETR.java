package io.github.yangziwen.zyftp.command.impl;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.filesystem.FileView;
import io.github.yangziwen.zyftp.server.FtpDataConnection;
import io.github.yangziwen.zyftp.server.FtpDataWriter;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpServerHandler;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.concurrent.Promise;

public class RETR implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		if (!request.hasArgument()) {
			return Command.createResponse(FtpReply.REPLY_501, "RETR", session);
		}
		FileView file = session.getFileSystemView().getFile(request.getArgument());
		if (file == null || !file.doesExist()) {
			return Command.createResponse(FtpReply.REPLY_550, "RETR.missing", request, session, request.getArgument());
		}
		if (!file.isFile()) {
			return Command.createResponse(FtpReply.REPLY_550, "RETR.invalid", request, session, file.getVirtualPath());
		}
		if (!file.isReadable()) {
			return Command.createResponse(FtpReply.REPLY_550, "RETR.permission", request, session, file.getVirtualPath());
		}
		if (!session.isLatestDataConnectionReady()) {
			return Command.createResponse(FtpReply.REPLY_425, "RETR", request, session, file.getVirtualPath());
		}
		long offset = parseOffset(session.getCommandState().getRequest("REST"));
		FtpResponse response = Command.createResponse(FtpReply.REPLY_150, "RETR", session);
		response.setFlushedPromise(session.newChannelPromise().addListener(f -> {
			doSendFileContent(session, request, file, offset);
		}));
		return response;
	}

	private long parseOffset(FtpRequest request) {
		if (request == null || !request.hasArgument()) {
			return 0L;
		}
		try {
			return Long.parseLong(request.getArgument());
		} catch (Exception e) {
			return 0L;
		}
	}

	private void doSendFileContent(FtpSession session, FtpRequest request, FileView file, long offset) {
		FileRegion region = new DefaultFileRegion(file.getRealFile(), offset, file.getSize());
		Promise<FtpDataConnection> promise = session.writeAndFlushData(new FtpDataWriter() {
			@Override
			public ChannelFuture writeAndFlushData(Channel ctx) {
				return ctx.writeAndFlush(region);
			}
		});
		promise.addListener(f -> {
			FtpServerHandler.sendResponse(Command.createResponse(FtpReply.REPLY_226, "RETR", session), session.getContext())
				.addListener(f2 -> {
					promise.get().stop();
				});
		});
	}

}

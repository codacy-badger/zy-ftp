package io.github.yangziwen.zyftp.command.impl;

import java.net.InetSocketAddress;

import io.github.yangziwen.zyftp.command.Command;
import io.github.yangziwen.zyftp.common.DataConnectionType;
import io.github.yangziwen.zyftp.common.FtpReply;
import io.github.yangziwen.zyftp.server.FtpPassiveDataServer;
import io.github.yangziwen.zyftp.server.FtpRequest;
import io.github.yangziwen.zyftp.server.FtpResponse;
import io.github.yangziwen.zyftp.server.FtpSession;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PASV implements Command {

	@Override
	public FtpResponse execute(FtpSession session, FtpRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Promise<FtpResponse> executeAsync(FtpSession session, FtpRequest request) {
		log.info("session[{}] receive request [{}]", session, request);
		Promise<Integer> portPromise = session.getServerContext().getPassivePorts().borrowPort();
		Promise<FtpResponse> promise = session.newPromise();
		portPromise.addListener(f1 -> {
			int port = portPromise.get();
			FtpPassiveDataServer passiveDataServer = new FtpPassiveDataServer(session);
			session.addPassiveDataServer(passiveDataServer);
			passiveDataServer.start(port).addListener(f2 -> {
				if (!f2.isSuccess()) {
					FtpResponse response = createResponse(FtpReply.REPLY_425, request);
					promise.setSuccess(response);
					log.error("failed to open passive connection", f2.cause());
				} else {
					InetSocketAddress address = (InetSocketAddress) passiveDataServer.getServerChannel().localAddress();
					request.attr("address", encode(session, address));
					FtpResponse response = createResponse(FtpReply.REPLY_227, request);
					session.setDataConnectionType(DataConnectionType.PASV);
					promise.setSuccess(response);
					log.info("passive data connection[{}] is opened for session[{}]", address, session);
				}
			});
		});
		return promise;
	}

    public static String encode(FtpSession session, InetSocketAddress address) {
        int servPort = address.getPort();
        return session.getServerConfig().getPassiveAddress().replaceAll("\\.", ",") + ","
        		+ (servPort >> 8) + "," + (servPort & 0xFF);
    }

}

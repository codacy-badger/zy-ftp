package io.github.yangziwen.zyftp.command;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.github.yangziwen.zyftp.command.impl.CWD;
import io.github.yangziwen.zyftp.command.impl.FEAT;
import io.github.yangziwen.zyftp.command.impl.HELP;
import io.github.yangziwen.zyftp.command.impl.NOOP;
import io.github.yangziwen.zyftp.command.impl.OPTS;
import io.github.yangziwen.zyftp.command.impl.PASS;
import io.github.yangziwen.zyftp.command.impl.PASV;
import io.github.yangziwen.zyftp.command.impl.PWD;
import io.github.yangziwen.zyftp.command.impl.QUIT;
import io.github.yangziwen.zyftp.command.impl.SYST;
import io.github.yangziwen.zyftp.command.impl.TYPE;
import io.github.yangziwen.zyftp.command.impl.USER;

public interface CommandFactory {

	static Map<String, Command> COMMANDS = ImmutableMap.<String, Command>builder()
			.put("HELP", new HELP())
			.put("QUIT", new QUIT())
			.put("NOOP", new NOOP())
			.put("USER", new USER())
			.put("PASS", new PASS())
			.put("SYST", new SYST())
			.put("FEAT", new FEAT())
			.put("OPTS", new OPTS())
			.put("TYPE", new TYPE())
			.put("PASV", new PASV())
			.put("PWD", new PWD())
			.put("CWD", new CWD())
			.build();

	static Command getCommand(String name) {
		return COMMANDS.get(name);
	}

}

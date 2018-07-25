package io.github.yangziwen.zyftp.user;

import lombok.Data;

@Data
public class User {

	private String username;

	private String password;

	private int maxIdleTime;

	private String homeDirectory;

	public User(String username) {
		this.username = username;
	}

}

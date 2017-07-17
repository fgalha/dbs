package br.com.b3.digsta.dbs.server.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {

	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new IllegalStateException("Erro ao encontrar hostname", e);
		}
	}
}

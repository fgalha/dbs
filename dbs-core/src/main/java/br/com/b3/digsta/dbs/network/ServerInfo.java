package br.com.b3.digsta.dbs.network;

import java.io.Serializable;

public class ServerInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String hostname;
	private long creationTimestamp;
	private long lastInformAliveTimestamp;
	
	public ServerInfo(String id, String hostname) {
		super();
		this.id = id;
		this.hostname = hostname;
		this.creationTimestamp = System.currentTimeMillis();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public long getLastInformAliveTimestamp() {
		return lastInformAliveTimestamp;
	}

	public void setLastInformAliveTimestamp(long lastInformAliveTimestamp) {
		this.lastInformAliveTimestamp = lastInformAliveTimestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerInfo other = (ServerInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", hostname=" + hostname + "]";
	}

}

package qetaa.service.customer.model;

import java.io.Serializable;

public class ActiveSessions implements Serializable{

	private static final long serialVersionUID = 1L;
	private int windows;
	private int ios;
	private int android;
	private int unix;
	private int unknown;
	private int mac;
	
	public int getWindows() {
		return windows;
	}
	public void setWindows(int windows) {
		this.windows = windows;
	}
	public int getIos() {
		return ios;
	}
	public void setIos(int ios) {
		this.ios = ios;
	}
	public int getAndroid() {
		return android;
	}
	public void setAndroid(int android) {
		this.android = android;
	}
	public int getUnix() {
		return unix;
	}
	public void setUnix(int unix) {
		this.unix = unix;
	}
	public int getUnknown() {
		return unknown;
	}
	public void setUnknown(int unknown) {
		this.unknown = unknown;
	}
	public int getMac() {
		return mac;
	}
	public void setMac(int mac) {
		this.mac = mac;
	}

	@Override
	public String toString() {
		return "ActiveSessions [windows=" + windows + ", ios=" + ios + ", android=" + android + ", unix=" + unix
				+ ", unknown=" + unknown + ", mac=" + mac + "]";
	}
	
	
	
	
}

package com.ratepay.prapi.environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalHostService {
	
	public static String getLocalHostName() 
	{
		try {
			InetAddress localHost = InetAddress.getLocalHost();		
			return localHost.getCanonicalHostName();
		}catch (UnknownHostException e) {
			return "";
		}
	}
}

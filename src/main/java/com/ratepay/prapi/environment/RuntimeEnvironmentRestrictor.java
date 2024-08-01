package com.ratepay.prapi.environment;

import java.util.regex.Pattern;

public class RuntimeEnvironmentRestrictor {
	
	public static boolean verifyEnvironment()
	{	
		 Pattern allowedEnvironment = Pattern.compile("^docker-(ds-(?!d))?[ds]\\d{2}$" ,Pattern.CASE_INSENSITIVE);
	     return allowedEnvironment.matcher(LocalHostService.getLocalHostName()).matches();
	}
}

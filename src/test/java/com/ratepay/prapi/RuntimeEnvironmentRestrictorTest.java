package com.ratepay.prapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ratepay.prapi.environment.LocalHostService;
import com.ratepay.prapi.environment.RuntimeEnvironmentRestrictor;

public class RuntimeEnvironmentRestrictorTest {
	
	@Test
	public void whenLocalHostnameNotRegExStyle_thenEnvRestrictorReturnFalse() 
	{	
		try (MockedStatic<LocalHostService> mockLocalhostService = Mockito.mockStatic(LocalHostService.class)) {
			mockLocalhostService.when(LocalHostService::getLocalHostName).thenReturn("thisShouldReturnFalse");
			assertThat(LocalHostService.getLocalHostName()).isEqualTo("thisShouldReturnFalse");
			
			assertThat(RuntimeEnvironmentRestrictor.verifyEnvironment()).isEqualTo(false);
		}
	}
}

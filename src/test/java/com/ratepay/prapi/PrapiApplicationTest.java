package com.ratepay.prapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ratepay.prapi.environment.RuntimeEnvironmentRestrictor;

public class PrapiApplicationTest {
	
	@Test
	public void whenEnvironmentNotAllowed_thenDontRunApp() 
	{
		try (MockedStatic<RuntimeEnvironmentRestrictor> mockRER = Mockito.mockStatic(RuntimeEnvironmentRestrictor.class)) {
	        mockRER.when(RuntimeEnvironmentRestrictor::verifyEnvironment).thenReturn(false);
	        assertThat(RuntimeEnvironmentRestrictor.verifyEnvironment()).isEqualTo(false);
	        
	        assertThatThrownBy(() -> { PrapiApplication.main(new String[] {}); }).isInstanceOf(IllegalStateException.class);
	    }	
	}

}

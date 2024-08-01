package com.ratepay.prapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ratepay.prapi.processing.BashScriptExecutor;
import com.ratepay.prapi.processing.ProcessFactory;
import com.ratepay.prapi.processing.bo.ScriptExecution;
import com.ratepay.prapi.processing.bo.ScriptExecution.State;
import com.ratepay.prapi.processing.bo.ScriptParameterDTO;

@ExtendWith(MockitoExtension.class)                         
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PrapiControllerTest {
	
	@Mock
	private ScriptExecution scriptExecution;
	@MockBean
	private ProcessFactory processFactory;
	@Autowired
	private BashScriptExecutor bashScriptExecutor;
	@Autowired
	private RestTemplate restTemplate;
	@LocalServerPort
	private int webServerPort;
	private ScriptParameterDTO scriptParameterDTO = new ScriptParameterDTO(LocalDate.parse("2021-01-01"));
	
	@Test
	public void whenScriptNotExecuted_then404Response()
	{	
		assertThatThrownBy(() -> { invokeGetEndpointGetById(); }).isInstanceOf(HttpClientErrorException.class).hasMessageStartingWith("404");;
	}
	
	@Test
	public void whenScriptSuccessful_thenStateSuccess() throws IOException, InterruptedException
	{	
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);		
		when(process.waitFor()).thenReturn(0);
	    bashScriptExecutor.executeScript(scriptParameterDTO);
	    Thread.sleep(1000);
	    
		ResponseEntity<ScriptExecution> result = invokeGetEndpointGetById();
		
		assertThat(State.EXECUTION_SUCCESS).isEqualTo(result.getBody().getStatus());
		assertThat(result.getBody().getEndDateTime()).isNotNull();	
		assertThat(scriptParameterDTO.getStartDate()).isEqualTo(result.getBody().getScriptStartDateParameter());
		assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();
	}
	
	@Test
	public void whenScriptError_thenStateError() throws IOException, InterruptedException
	{
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);
		when(process.waitFor()).thenReturn(1);
		bashScriptExecutor.executeScript(scriptParameterDTO);
		Thread.sleep(1000);
		
		ResponseEntity<ScriptExecution> result = invokeGetEndpointGetById();
		
		assertThat(State.EXECUTION_ERROR).isEqualTo(result.getBody().getStatus());
		assertThat(scriptParameterDTO.getStartDate()).isEqualTo(result.getBody().getScriptStartDateParameter());
		assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();
	    assertThat(result.getBody().getEndDateTime()).isNotNull();
	}
	
	@Test
	public void whenScriptInvokedButNotStarted_thenStatusFailed() throws IOException 
	{
		doThrow(new IOException()).when(processFactory).createProcess(scriptParameterDTO);
		bashScriptExecutor.executeScript(scriptParameterDTO);
		
		ResponseEntity<ScriptExecution> result = invokeGetEndpointGetById();
		
		assertThat(State.INITIALIZING_FAILED).isEqualTo(result.getBody().getStatus());
		assertThat(scriptParameterDTO.getStartDate()).isEqualTo(result.getBody().getScriptStartDateParameter());
		assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();
	    assertThat( result.getBody().getEndDateTime() ).isNotNull();
	}
	
	@Test
	public void whenProcessStillRunning_thenEndTimeNull() throws InterruptedException, IOException
	{
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);
		when(process.waitFor()).thenAnswer(new Answer<Integer>() {
		public Integer answer(InvocationOnMock invocation) throws Throwable {Thread.sleep(3000);return null;}});
		bashScriptExecutor.executeScript(scriptParameterDTO);
		
		ResponseEntity<ScriptExecution> result = invokeGetEndpointGetById();
	
	    assertThat(result.getBody().getEndDateTime()).isNull();;
		assertThat(State.PROCESS_RUNNING).isEqualTo(result.getBody().getStatus());
		assertThat(scriptParameterDTO.getStartDate()).isEqualTo(result.getBody().getScriptStartDateParameter());
		assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();
	}
	
	@Test
	public void whenStartDateNull_then201Response() throws IOException, InterruptedException 
	{	
		ScriptParameterDTO s = new ScriptParameterDTO(null);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(Mockito.mock(Process.class));
	    
		ResponseEntity<ScriptExecution> result = restTemplate.postForEntity(getBaseUrl(), s, ScriptExecution.class);
		
		assertThat(201).isEqualTo(result.getStatusCodeValue());		
		assertThat(LocalDate.now().minusDays(1)).isEqualTo(result.getBody().getScriptStartDateParameter());
		assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();	
	    assertThat(result.getBody().getStatus()).isEqualTo(State.PROCESS_RUNNING);
	}
	@Test
	public void whenPostEndpointInvoked_then201Response() throws InterruptedException, IOException 
	{	
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);
	    when(process.waitFor()).thenReturn(0);
	    
		ResponseEntity<ScriptExecution> result = invokePostEndpoint();   
	    
	    assertThat(201).isEqualTo(result.getStatusCodeValue());
	    assertThat(scriptParameterDTO.getStartDate()).isEqualTo(result.getBody().getScriptStartDateParameter());
	    assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();
	}
	
	@Test
	public void whenEmptyRequestBody_then201ResponseWithDefaultDate() throws IOException 
	{	
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(Mockito.mock(Process.class));
		
	    ResponseEntity<ScriptExecution> result = restTemplate.postForEntity(getBaseUrl(), createCustomHttpRequest(null) , ScriptExecution.class);
	   
	    assertThat(201).isEqualTo(result.getStatusCodeValue());
	    assertThat(result.getBody().getScriptStartDateParameter()).isEqualTo(LocalDate.now().minusDays(1));
	    assertThat(result.getBody().getId()).isNotNull();
	    assertThat(result.getBody().getStartDateTime()).isNotNull();
	    assertThat(result.getBody().getStatus()).isEqualTo(State.PROCESS_RUNNING);
	}		   
	@Test
	public void whenGetById_thenReturnWithWantedId() throws IOException, InterruptedException 
	{
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);
	    when(process.waitFor()).thenReturn(0);
	    bashScriptExecutor.executeScript(scriptParameterDTO);
		bashScriptExecutor.executeScript(scriptParameterDTO);
		
		ResponseEntity<ScriptExecution> result = invokeGetEndpointGetById();
		
		assertThat(result.getBody().getId()).isEqualTo(1);
		assertThat(result.getBody().getStartDateTime()).isNotNull();
		assertThat(result.getBody().getScriptStartDateParameter()).isEqualTo(LocalDate.parse("2021-01-01"));
		assertThat(200).isEqualTo(result.getStatusCodeValue());
	}
	@Test
	public void whenGetAllEndpoint_thenReturnListWithMax10Elems() throws IOException, InterruptedException 
	{
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);
	    when(process.waitFor()).thenReturn(0);
		for(int i=0;i<11;i++) {
			bashScriptExecutor.executeScript(scriptParameterDTO);
		}
		
		@SuppressWarnings("rawtypes")
		ResponseEntity<List> result = invokeGetEndpointGetAll();
		
		assertThat(result.getBody().size()).isEqualTo(10);
	}
	@Test
	public void whenParameterNotADate_then400Response() 
	{
		assertThatThrownBy(() -> { restTemplate.postForEntity(getBaseUrl(), createCustomHttpRequest("This should cause 400") , ScriptExecution.class);; })
		.isInstanceOf(HttpClientErrorException.class).hasMessageStartingWith("400");
	}
	
	@Test
	public void whenMultithreading_thenAllowOnlyOneScriptExecutionThread() throws InterruptedException, IOException
	{	
		Process process = Mockito.mock(Process.class);
		when(processFactory.createProcess(Mockito.any(ScriptParameterDTO.class))).thenReturn(process);
		when(process.isAlive()).thenReturn(true);
		when(process.waitFor()).thenReturn(0);
		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		Callable<ResponseEntity<ScriptExecution>> task = () -> invokePostEndpoint();
		
	    List<Future<ResponseEntity<ScriptExecution>>> taskResults = threadPool.invokeAll(Arrays.asList(task,task));
		
	    Mockito.verify(processFactory).createProcess(Mockito.any(ScriptParameterDTO.class));
	    assertThat(taskResults).anySatisfy(taskResult->assertThat(getFutureResult(taskResult).getStatusCodeValue()).isEqualTo(201));
		assertThat(taskResults).anySatisfy(taskResult->assertThat(getFutureResult(taskResult).getStatusCodeValue()).isEqualTo(429));
		
		threadPool.shutdown();  	
	}
	
	private ResponseEntity<ScriptExecution> getFutureResult(Future<ResponseEntity<ScriptExecution>> taskResult) 
	{
		try {
			return taskResult.get();
		} catch (InterruptedException | ExecutionException e) {
		
			if(e.getMessage().contains("org.springframework.web.client.HttpClientErrorException$TooManyRequests: 429")){
				return new ResponseEntity<ScriptExecution>(HttpStatus.TOO_MANY_REQUESTS);
			}else {
				throw new RuntimeException(e);
			}
		}
	}
	
	private HttpEntity<String> createCustomHttpRequest(String jsonInput) 
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(jsonInput, headers);
		return entity;
	}
	
	private ResponseEntity<ScriptExecution> invokeGetEndpointGetById() 
	{	
	    return restTemplate.getForEntity(getBaseUrl()+"/1", ScriptExecution.class);
	}
	@SuppressWarnings("rawtypes")
	private ResponseEntity<List> invokeGetEndpointGetAll() 
	{	
	    return restTemplate.getForEntity(getBaseUrl(), List.class);
	}
	
	private ResponseEntity<ScriptExecution> invokePostEndpoint() 
	{	
	    return restTemplate.postForEntity(getBaseUrl(), scriptParameterDTO, ScriptExecution.class);
	}
	
	private URI getBaseUrl() 
	{	
		String versionUrl = "/api/v1";
		try {
			return new URI("http://localhost:" + webServerPort + versionUrl+ "/prapi");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}	
}

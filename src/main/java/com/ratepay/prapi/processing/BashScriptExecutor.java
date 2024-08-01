package com.ratepay.prapi.processing;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ratepay.prapi.processing.bo.ScriptExecution;
import com.ratepay.prapi.processing.bo.ScriptExecution.State;
import com.ratepay.prapi.processing.bo.ScriptParameterDTO;
@Component
public class BashScriptExecutor {
	
	@Autowired
	private ProcessFactory processFactory;
	private Thread thread;
	private ScriptExecution scriptExecution; 
	private long idCounter; 
	private Process process;
	private List<ScriptExecution> scriptExecutionHistory = new ArrayList<>();
	
	public ScriptExecution executeScript(ScriptParameterDTO scriptParameterDTO) 
	{		
		thread = new Thread(()->monitorExecution());
		thread.setDaemon(true);
		
		scriptExecution = new ScriptExecution();
		scriptExecution.setScriptStartDateParameter(scriptParameterDTO.getStartDate());
		scriptExecution.setId(++idCounter);
		scriptExecution.setStartDateTime(OffsetDateTime.now());
		
		try{    
		    process = processFactory.createProcess(scriptParameterDTO);
		    scriptExecution.setStatus(State.PROCESS_RUNNING);
		    thread.start(); 
		}catch(Exception e) {
			scriptExecution.setEndDateTime(OffsetDateTime.now());
			scriptExecution.setStatus(State.INITIALIZING_FAILED);
		}	
		historize();
		return scriptExecution;
	} 
	
	private void monitorExecution() 
	{	
		try {
			int processExitValue = process.waitFor();
			scriptExecution.setEndDateTime(OffsetDateTime.now());
			if(processExitValue==0) {
				scriptExecution.setStatus(State.EXECUTION_SUCCESS); 
			}else {
				scriptExecution.setStatus(State.EXECUTION_ERROR);
			}	
		} catch (InterruptedException e) {
			scriptExecution.setEndDateTime(OffsetDateTime.now());
			scriptExecution.setStatus(State.EXECUTION_ERROR);
		}
	}
	
	public List<ScriptExecution> getScriptExecutionHistory() 
	{	
		return scriptExecutionHistory;
	}
	public ScriptExecution getByScriptExecutionId(long id) 
	{	
		return scriptExecutionHistory.stream().filter(scriptExecution -> scriptExecution.getId()==id).findFirst().orElse(null);
	}
	public boolean isProcessAlive() 
	{	
		return process != null ? process.isAlive() : false;
	}
	private void historize() 
	{
		if(scriptExecutionHistory.size()>9) {
			scriptExecutionHistory.remove(0);
		}
		scriptExecutionHistory.add(scriptExecution);	
	}
}

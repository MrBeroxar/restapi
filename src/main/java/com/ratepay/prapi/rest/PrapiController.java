package com.ratepay.prapi.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ratepay.prapi.processing.BashScriptExecutor;
import com.ratepay.prapi.processing.bo.ScriptExecution;
import com.ratepay.prapi.processing.bo.ScriptParameterDTO;


@RestController
@RequestMapping(path="/api/v1")
public class PrapiController {
	
	@Autowired
	private BashScriptExecutor bashScriptExecuter;
	
	@GetMapping("/prapi")
	public List<ScriptExecution> getScriptExecutionHistory() 
	{		
		if(bashScriptExecuter.getScriptExecutionHistory().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);	
		}else {
			return bashScriptExecuter.getScriptExecutionHistory();	
		}
	}
	
	@GetMapping("/prapi/{id}")
	public ScriptExecution getScriptExecutionById(@PathVariable long id)
	{
		if(bashScriptExecuter.getByScriptExecutionId(id)==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}else{
			return bashScriptExecuter.getByScriptExecutionId(id);
		}
	}

	@ResponseStatus(code = HttpStatus.CREATED)
	@PostMapping(value ="/prapi",consumes="application/json",produces="application/json")
	public ScriptExecution executeScript(@RequestBody @Nullable ScriptParameterDTO scriptParameterDTO)  
	{	
		synchronized(this) {
			if(bashScriptExecuter.isProcessAlive()) {
				throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
			}else {
				try {
					return bashScriptExecuter.executeScript(scriptParameterDTO);
				}catch(Exception e){
					return bashScriptExecuter.executeScript(new ScriptParameterDTO());
				}
			}		
		}		
	}
}
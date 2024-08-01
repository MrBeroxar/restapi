package com.ratepay.prapi.processing;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.ratepay.prapi.processing.bo.ScriptParameterDTO;

@Component
public class ProcessFactory {
	
	public Process createProcess(ScriptParameterDTO scriptParameterDTO) throws IOException 
	{		
		String startDate = scriptParameterDTO.getStartDate().toString();
		ProcessBuilder builder = new ProcessBuilder("sh","-c","echo \"Starting prior with startdate="+startDate+"\" | tee -a /data/DOCKER/prior/prior.log && docker exec prior bash -c \"cd /prior/ && python3 -m prior.app  --config_path=/data/DOCKER/prior/config.json --start_date="+startDate+"\" 2>&1 | tee -a /data/DOCKER/prior/prior.log"); 
		return builder.inheritIO().start();
	}
}

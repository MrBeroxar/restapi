package com.ratepay.prapi.processing.bo;

import java.time.LocalDate;

public class ScriptParameterDTO {
	
	private LocalDate startDate;
	
	public ScriptParameterDTO() {
	}
	public ScriptParameterDTO(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getStartDate() {
		if(startDate == null) {
			startDate = LocalDate.now().minusDays(1);
		}
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
}

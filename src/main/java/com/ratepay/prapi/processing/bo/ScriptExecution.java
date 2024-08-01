package com.ratepay.prapi.processing.bo;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class ScriptExecution {
	
	private long id;
	private OffsetDateTime startDateTime;
	private OffsetDateTime endDateTime;
	private LocalDate scriptStartDateParameter;
	private State status;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public OffsetDateTime getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(OffsetDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}
	public OffsetDateTime getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(OffsetDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}
	public State getStatus() {
		return status;
	}
	public void setStatus(State running) {
		this.status = running;
	}
	public LocalDate getScriptStartDateParameter() {
		return scriptStartDateParameter;
	}
	public void setScriptStartDateParameter(LocalDate scriptStartDateParameter) {
		this.scriptStartDateParameter = scriptStartDateParameter;
	}
	public enum State {
		  PROCESS_RUNNING,
		  EXECUTION_SUCCESS,
		  EXECUTION_ERROR,
		  INITIALIZING_FAILED,
	}
}

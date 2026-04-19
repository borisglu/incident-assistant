package com.boris.ai.incident.assistant.exception;

public class IncidentAssistantException extends RuntimeException {

	public IncidentAssistantException(String message) {
		super(message);
	}

	public IncidentAssistantException(String message, Throwable cause) {
		super(message, cause);
	}
}

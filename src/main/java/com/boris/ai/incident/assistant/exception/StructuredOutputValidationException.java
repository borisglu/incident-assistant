package com.boris.ai.incident.assistant.exception;

public class StructuredOutputValidationException extends RuntimeException {

	public StructuredOutputValidationException(String message) {
		super(message);
	}

	public StructuredOutputValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}

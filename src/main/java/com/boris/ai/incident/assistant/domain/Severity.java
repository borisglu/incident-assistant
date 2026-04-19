package com.boris.ai.incident.assistant.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Severity {
	LOW("low"),
	MEDIUM("medium"),
	HIGH("high");

	private final String wireValue;

	Severity(String wireValue) {
		this.wireValue = wireValue;
	}

	@JsonValue
	public String wireValue() {
		return wireValue;
	}

	@JsonCreator
	public static Severity fromWire(String value) {
		if (value == null) {
			throw new IllegalArgumentException("severity is required");
		}
		return switch (value.trim().toLowerCase()) {
			case "low" -> LOW;
			case "medium" -> MEDIUM;
			case "high" -> HIGH;
			default -> throw new IllegalArgumentException("Unsupported severity: " + value);
		};
	}
}

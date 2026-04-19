package com.boris.ai.incident.assistant.agent.model;

import java.util.List;

import lombok.Value;

@Value
public class RecoveryHint {

	List<String> hints;

	public static RecoveryHint empty() {
		return new RecoveryHint(List.of());
	}

	public static RecoveryHint of(String... hints) {
		return new RecoveryHint(List.of(hints));
	}
}

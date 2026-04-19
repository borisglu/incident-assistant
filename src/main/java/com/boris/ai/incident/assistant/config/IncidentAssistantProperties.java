package com.boris.ai.incident.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "incident.assistant")
public class IncidentAssistantProperties {

	@Min(256)
	@Max(64_000)
	private int maxInputLength;

	@Valid
	@NotNull
	private Llm llm = new Llm();

	@Valid
	@NotNull
	private Prompt prompt = new Prompt();

	@Data
	public static class Llm {

		@Min(1)
		@Max(5)
		private int maxAttempts;
	}

	@Data
	public static class Prompt {

		/**
		 * System message for the triage LLM. Prefer a YAML literal block ({@code |}). If blank, a built-in default is used.
		 */
		private String system;
	}
}

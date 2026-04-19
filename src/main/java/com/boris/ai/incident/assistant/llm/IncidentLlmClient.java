package com.boris.ai.incident.assistant.llm;

/**
 * Abstraction over the concrete chat model provider (Anthropic via Spring AI in production).
 */
public interface IncidentLlmClient {

	String complete(String systemPrompt, String userPrompt);
}

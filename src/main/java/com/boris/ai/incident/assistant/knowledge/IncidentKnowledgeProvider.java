package com.boris.ai.incident.assistant.knowledge;

/**
 * Supplies static operational context (system topology, historical incidents) used by the triage agent.
 */
public interface IncidentKnowledgeProvider {

	String knowledgeText();
}

package com.boris.ai.incident.assistant.service;

import org.springframework.stereotype.Service;

import com.boris.ai.incident.assistant.agent.IncidentTriageAgent;
import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentAnalysisApplicationService {

	private final IncidentTriageAgent triageAgent;

	public IncidentAnalysisResult analyze(String rawIncidentDescription) {
		IncidentAnalysisResult analysis = triageAgent.analyze(rawIncidentDescription);
		log.info("Incident triage completed (severity={})", analysis.getSeverity());
		return analysis;
	}
}

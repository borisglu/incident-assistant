package com.boris.ai.incident.assistant.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentAnalysisResult {

	private String category;
	private String summary;
	private Severity severity;
	private List<Hypothesis> hypotheses;
}

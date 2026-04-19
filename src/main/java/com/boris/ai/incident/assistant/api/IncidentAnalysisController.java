package com.boris.ai.incident.assistant.api;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boris.ai.incident.assistant.api.dto.AnalyzeIncidentRequest;
import com.boris.ai.incident.assistant.domain.IncidentAnalysisResult;
import com.boris.ai.incident.assistant.service.IncidentAnalysisApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class IncidentAnalysisController {

	private final IncidentAnalysisApplicationService incidentAnalysisApplicationService;

	@PostMapping(path = "/v1/incidents/analyze", consumes = MediaType.APPLICATION_JSON_VALUE)
	public IncidentAnalysisResult analyze(@Valid @RequestBody AnalyzeIncidentRequest request) {
		return incidentAnalysisApplicationService.analyze(request.getDescription());
	}
}

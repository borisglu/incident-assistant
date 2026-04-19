package com.boris.ai.incident.assistant.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hypothesis {

	private String title;
	private String reasoning;
	@JsonProperty("next_steps")
	private List<String> nextSteps;
}

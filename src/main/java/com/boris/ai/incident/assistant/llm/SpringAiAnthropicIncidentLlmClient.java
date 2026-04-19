package com.boris.ai.incident.assistant.llm;

import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiAnthropicIncidentLlmClient implements IncidentLlmClient {

	private final ChatModel chatModel;

	@Override
	public String complete(String systemPrompt, String userPrompt) {
		Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));

		log.debug(
				"Calling chat model (systemPromptChars={}, userPromptChars={})",
				systemPrompt.length(),
				userPrompt.length());

		ChatResponse response = chatModel.call(prompt);

		List<Generation> generations = response.getResults();

		if (generations == null|| generations.isEmpty()) {
			throw new IllegalStateException("Model returned no generations");
		}
		String text = generations.get(0).getOutput().getText();
		if (text == null || text.isBlank()) {
			throw new IllegalStateException("Model returned an empty response");
		}
		String stripped = text.strip();
		log.debug("Received model response (chars={})", stripped.length());

		return stripped;
	}
}

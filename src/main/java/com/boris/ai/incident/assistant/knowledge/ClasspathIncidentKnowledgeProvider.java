package com.boris.ai.incident.assistant.knowledge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class ClasspathIncidentKnowledgeProvider implements IncidentKnowledgeProvider, InitializingBean {

	private static final String RESOURCE = "agent/incident-knowledge.md";

	private volatile String cached;

	@Override
	public String knowledgeText() {
		return cached;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		ClassPathResource resource = new ClassPathResource(RESOURCE);
		if (!resource.exists()) {
			throw new IllegalStateException("Missing classpath resource: " + RESOURCE);
		}
		this.cached = resource.getContentAsString(StandardCharsets.UTF_8);
		log.info("Loaded incident knowledge from classpath:{} (chars={})", RESOURCE, cached.length());
	}
}

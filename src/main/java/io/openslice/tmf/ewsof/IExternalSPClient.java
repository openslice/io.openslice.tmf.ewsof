package io.openslice.tmf.ewsof;

import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;

import io.openslice.tmf.scm633.model.ServiceSpecification;

public interface IExternalSPClient {

	WebClient setWebClient();
	WebClient getWebClient();
	
	List<ServiceSpecification> getSpecs();
	
	
	
}

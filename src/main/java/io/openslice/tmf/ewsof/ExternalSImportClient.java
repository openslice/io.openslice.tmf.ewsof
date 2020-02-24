package io.openslice.tmf.ewsof;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import io.openslice.tmf.scm633.model.ServiceCatalog;
import io.openslice.tmf.scm633.model.ServiceSpecification;

@RestController
public class ExternalSImportClient {

	public void getAll() {
		WebClient.RequestBodySpec request1 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceCatalogManagement/v4/serviceCatalog");
		WebClient.RequestBodySpec request2 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceCatalogManagement/v4/serviceSpecification");
		
		
		List<ServiceCatalog> response2 = request1.exchange()
				  .block()
				  .bodyToMono( new ParameterizedTypeReference<List<ServiceCatalog>>() {})
				  .block();
		for (ServiceCatalog serviceCatalog : response2) {
			System.out.println("serviceCatalog: " + serviceCatalog.getName());
			
		}
		
		List<ServiceSpecification> responseSpecs = request2.exchange()
				  .block()
				  .bodyToMono( new ParameterizedTypeReference<List<ServiceSpecification>>() {})
				  .block();
		for (ServiceSpecification spec : responseSpecs) {
			System.out.println("spec: " + spec.getName());
			
		}
	}
	


    private WebClient createWebClientWithServerURLAndDefaultValues() {
        return WebClient.builder()
            .baseUrl("http://portal.openslice.io:80")
            .defaultCookie("cookieKey", "cookieValue")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultUriVariables(Collections.singletonMap("url", "http://portal.openslice.io:80"))
            .build();
    }
}

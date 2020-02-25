package io.openslice.tmf.ewsof;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import io.openslice.tmf.scm633.model.ServiceCatalog;
import io.openslice.tmf.scm633.model.ServiceSpecification;
import io.openslice.tmf.so641.model.ServiceOrder;

@Service
public class ExternalSImportClient {

	
	private final WebClient webClient;

	public ExternalSImportClient(WebClient.Builder webClientBuilder) {
		this.webClient = createWebClientWithServerURLAndDefaultValues( webClientBuilder );
	}
	
	 private WebClient createWebClientWithServerURLAndDefaultValues(Builder webClientBuilder) {
		 return  webClientBuilder
		            .baseUrl("http://portal.openslice.io:80")
		            .defaultCookie("cookieKey", "cookieValue")
		            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .defaultUriVariables(Collections.singletonMap("url", "http://portal.openslice.io:80"))
		            .build();

	}

	private WebClient createWebClientWithServerURLAndDefaultValues() {
	        return WebClient.builder()
	            .baseUrl("http://portal.openslice.io:80")
	            .defaultCookie("cookieKey", "cookieValue")
	            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .defaultUriVariables(Collections.singletonMap("url", "http://portal.openslice.io:80"))
	            .build();
	    }
	
	
	public void getAll() {
		WebClient.RequestBodySpec request1 = (RequestBodySpec) this.webClient.get().uri("/tmf-api/serviceCatalogManagement/v4/serviceCatalog");
		WebClient.RequestBodySpec request2 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceCatalogManagement/v4/serviceSpecification");
		WebClient.RequestBodySpec request3 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceOrdering/v4/serviceOrder");
		
		
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
		
		
		
		/*
		 * this needs authentication
		 */
		
		List<ServiceOrder> responseOrders = request3.retrieve()
				  .bodyToMono( new ParameterizedTypeReference<List<ServiceOrder>>() {})
				  .block();
		if ( responseOrders!=null ) {
			for (ServiceOrder o : responseOrders) {
				System.out.println("order: " + o.getDescription() );
				
			}			
		}

		
	}
	


   
}

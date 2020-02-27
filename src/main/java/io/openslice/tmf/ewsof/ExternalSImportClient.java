package io.openslice.tmf.ewsof;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.fasterxml.jackson.databind.JsonNode;

import io.openslice.tmf.scm633.model.ServiceCatalog;
import io.openslice.tmf.scm633.model.ServiceSpecification;
import io.openslice.tmf.so641.model.ServiceOrder;
import reactor.core.publisher.Mono;

@Service
public class ExternalSImportClient {


	private static final transient Log log = LogFactory.getLog( ExternalSImportClient.class.getName());
	
	@Autowired
	WebClient webClient;

	private Object authorizedClient;

//	public ExternalSImportClient(WebClient.Builder webClientBuilder) {
//		this.webClient = createWebClientWithServerURLAndDefaultValues( webClientBuilder );
//	}
//	
//	 private WebClient createWebClientWithServerURLAndDefaultValues(Builder webClientBuilder) {
//
//			ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
//	                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); //spring.codec.max-in-memory-size=-1 ?? if use autoconfiguration
//			
//		 return  webClientBuilder
//	        	 .exchangeStrategies(exchangeStrategies)
//		            .baseUrl("http://portal.openslice.io:80")
//		            .defaultCookie("cookieKey", "cookieValue")
//		            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//		            .defaultUriVariables(Collections.singletonMap("url", "http://portal.openslice.io:80"))
//		            .build();
//
//	}

	private WebClient createWebClientWithServerURLAndDefaultValues() {
		
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); //spring.codec.max-in-memory-size=-1 ?? if use autoconfiguration

	        return WebClient.builder()
	        	 .exchangeStrategies(exchangeStrategies)
	            .baseUrl("http://portal.openslice.io:80")
	            .defaultCookie("cookieKey", "cookieValue")
	            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .defaultUriVariables(Collections.singletonMap("url", "http://portal.openslice.io:80"))
	            .build();
	    }
	
	
	public void getAll() {
		WebClient.RequestBodySpec request1 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceCatalogManagement/v4/serviceCatalog");
		WebClient.RequestBodySpec request2 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceCatalogManagement/v4/serviceSpecification");
		//WebClient.RequestBodySpec request3 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceOrdering/v4/serviceOrder");
		
		
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
		
//		 String encodedClientData = 
//			      Base64Utils.encodeToString("osapiWebClientId:secret".getBytes());
//		 
//		MultiValueMap<String, String> fd = new LinkedMultiValueMap<>();
//
//		fd.add("grant_type", "password");
//		fd.add("username", "admin");
//		fd.add("password", "openslice");
//		
//		
//		Mono<List<ServiceOrder>> resource = this.webClient.post()
//	      .uri("http://portal.openslice.io/osapi-oauth-server/oauth/token")
//	      .header("Authorization", "Basic " + encodedClientData)
//	      .body(BodyInserters.fromFormData( fd ))
//	      .retrieve()
//	      .bodyToMono(JsonNode.class)
//	      .flatMap(tokenResponse -> {
//	          String accessTokenValue = tokenResponse.get("access_token").textValue();
//	          return this.webClient.get()
//	            .uri("http://portal.openslice.io/tmf-api/serviceOrdering/v4/serviceOrder")
//	            .headers(h -> h.setBearerAuth(accessTokenValue))
//	            .retrieve()
//	            .bodyToMono(  new ParameterizedTypeReference<List<ServiceOrder>>() {} );
//	        });
//		List<ServiceOrder> responseOrders = resource.block();
		
		
//		webClient.get().uri("/tmf-api/serviceOrdering/v4/serviceOrder")
//				.exchange()			
//                .subscribe(  it -> {log.debug("Success with HTTP Status "+ it.statusCode()); }  );
		//System.out.println("ss : " + ss.bodyToMono(String.class).block().toString()  );
		List<ServiceOrder> responseOrders = webClient.get().uri("/tmf-api/serviceOrdering/v4/serviceOrder")
					.attributes( ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("myregoauth"))
					.retrieve()
				  .bodyToMono( new ParameterizedTypeReference<List<ServiceOrder>>() {})
				  .block();
		if ( responseOrders!=null ) {
			for (ServiceOrder o : responseOrders) {
				System.out.println("order date: " + o.getOrderDate()  );
				
			}			
		}

		
	}

   
}

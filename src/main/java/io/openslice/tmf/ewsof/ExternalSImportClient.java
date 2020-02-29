package io.openslice.tmf.ewsof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.openslice.tmf.scm633.model.ServiceCatalog;
import io.openslice.tmf.scm633.model.ServiceSpecification;
import io.openslice.tmf.so641.model.ServiceOrder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Service
public class ExternalSImportClient {


	private static final transient Logger log = LoggerFactory.getLogger( ExternalSImportClient.class.getName());

	private static WebClient webClient;
	private static WebClient webClient2;

	private Object authorizedClient;


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
	
	
	public void getAll() throws SSLException {
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
		
		if ( webClient == null) {
			GenericClient oac = new GenericClient("admin", "openslice", "authOpensliceProvider", "http://portal.openslice.io" );
			webClient = oac.getWebClient(); 
		}
		
		List<ServiceOrder> responseOrders = webClient.get()
				.uri("/tmf-api/serviceOrdering/v4/serviceOrder")
					.attributes( ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("authOpensliceProvider"))
					.retrieve()
				  .bodyToMono( new ParameterizedTypeReference<List<ServiceOrder>>() {})
				  .block();
		if ( responseOrders!=null ) {
			for (ServiceOrder o : responseOrders) {
				System.out.println("order date: " + o.getOrderDate()  );
				
			}			
		}
		
		
		if ( webClient2 == null) {
			GenericClient oac = new GenericClient("admin", "openslice", "authOpensliceProvider", "http://portal.openslice.io");
			webClient2 = oac.getWebClient(); 
		}
		
		responseOrders = webClient2.get()
				.uri("/tmf-api/serviceOrdering/v4/serviceOrder")
					.attributes( ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("authOpensliceProvider"))
					.retrieve()
				  .bodyToMono( new ParameterizedTypeReference<List<ServiceOrder>>() {})
				  .block();
		if ( responseOrders!=null ) {
			for (ServiceOrder o : responseOrders) {
				System.out.println("order id: " + o.getId() );
				
			}			
		}

		
		
		
		
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)).build(); //spring.codec.max-in-memory-size=-1 ?? if use autoconfiguration

		var tcpClient = TcpClient.create()
			      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
			      .doOnConnected(connection ->
			        connection.addHandlerLast(new ReadTimeoutHandler(2))
			          .addHandlerLast(new WriteTimeoutHandler(2)));
		SslContext sslContext = SslContextBuilder
				.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();
		WebClient webClient3 =    WebClient.builder()
	        	 .exchangeStrategies(exchangeStrategies)
	        	 .clientConnector(new ReactorClientHttpConnector(
	        			 HttpClient.from(tcpClient)
	        			 .secure( sslContextSpec -> sslContextSpec.sslContext(sslContext) ))
	        			 )
	            .baseUrl("https://patras5g.eu")
	            .defaultCookie("cookieKey", "cookieValue")
	            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .filter(ExchangeFilterFunctions.basicAuthentication("username", "pass"))
	            .defaultUriVariables(Collections.singletonMap("url", "https://patras5g.eu"))
	            .build();
		
		String resp= webClient3.get()
				.uri("/apiportal/services/api/repo/vxfs")
					.attributes( ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("authOpensliceProvider"))
					.retrieve()
					.onStatus(HttpStatus::is4xxClientError, response -> {
				        System.out.println("4xx eror");
				        return Mono.error(new RuntimeException("4xx"));
				      })
				      .onStatus(HttpStatus::is5xxServerError, response -> {
				        System.out.println("5xx eror");
				        return Mono.error(new RuntimeException("5xx"));
				      })
				  .bodyToMono( String.class)
				  .block();
				

				System.out.println("resp: " + resp );
	}

	private ExchangeFilterFunction logRequest() {
	    return (clientRequest, next) -> {
	      log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
	      log.info("--- Http Headers: ---");
	      clientRequest.headers().forEach(this::logHeader);
	      log.info("--- Http Cookies: ---");
	      clientRequest.cookies().forEach(this::logHeader);
	      return next.exchange(clientRequest);
	    };
	  }
	 
	  private ExchangeFilterFunction logResponse() {
	    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
	      log.info("Response: {}", clientResponse.statusCode());
	      clientResponse.headers().asHttpHeaders()
	        .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
	      return Mono.just(clientResponse);
	    });
	  }
	  
	  
	  private void logHeader(String name, List<String> values) {
		    values.forEach(value -> log.info("{}={}", name, value));
	 }
	  
	  
	 public List<ServiceSpecification> getExternalPartners() {
		 log.info("getExternalPartners");
//		 return new ArrayList<ServiceSpecification>();
		 
			WebClient.RequestBodySpec request2 = (RequestBodySpec) createWebClientWithServerURLAndDefaultValues().get().uri("/tmf-api/serviceCatalogManagement/v4/serviceSpecification");
			List<ServiceSpecification> responseSpecs = request2.exchange()
					  .block()
					  .bodyToMono( new ParameterizedTypeReference<List<ServiceSpecification>>() {})
					  .block();

			 log.info("responseSpecs = " + responseSpecs.size());
			return responseSpecs;
			
	 }
	 
	 public void printAllSpecs( List<ServiceSpecification> responseSpecs) {
			for (ServiceSpecification spec : responseSpecs) {
				System.out.println("spec retreived: " + spec.getName());
				
			}
	 }
}

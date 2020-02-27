package io.openslice.tmf.ewsof;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class OAuthConfiguration implements WebMvcConfigurer {


	private static final transient Log log = LogFactory.getLog( OAuthConfiguration.class.getName());
	
//    @Bean("authOpensliceProvider") //bean qualifier
//    WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {
//        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
//                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
//                        clientRegistrations,
//                        new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
//        oauth.setDefaultClientRegistrationId("authOpensliceProvider");
//        return WebClient.builder()
//                .filter(oauth)
//                .build();
//    }
	
	@Bean
	public WebClient messageWebClient(
			ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction,
	        ClientHttpConnector clientHttpConnector
	) {



        log.info("WebClientConfiguration.messageWebClient()");
//	    ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
//	            new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);

//	    oauth.setDefaultClientRegistrationId("message");

	    return WebClient.builder()
	            .baseUrl("http://portal.openslice.io")
	            .clientConnector(clientHttpConnector)
	            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
	            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
	            .apply(servletOAuth2AuthorizedClientExchangeFilterFunction.oauth2Configuration())
	            .filter(logRequest())
	            .build();
	}
    
     private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info( "Request: " + clientRequest.method() +", "+ clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{"+name+"}={"+value+"}")));
            return next.exchange(clientRequest);
        };
    }

	@Bean
    public ClientRegistrationRepository  clientRegistrations() {
		

        log.info("WebClientConfiguration.clientRegistrations()");
        
        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId("myregoauth")
                .clientId("osapiWebClientId")
                .clientSecret("secret")
                .scope("admin")
                .authorizationGrantType(AuthorizationGrantType.PASSWORD )
                .tokenUri("http://portal.openslice.io/osapi-oauth-server/oauth/token")
                .build();

        return new InMemoryClientRegistrationRepository(clientRegistration);
    }
	
	  @Bean
	    public ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction(
	            ClientRegistrationRepository clientRegistrations,
	            OAuth2AuthorizedClientRepository authorizedClients,
	            OAuth2AuthorizedClientManager authorizedClientManager
	    ) {

	        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
	                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

	        //oauth.setDefaultOAuth2AuthorizedClient(true);
	        oauth.setDefaultClientRegistrationId("myregoauth");
	        //oauth.setAccessTokenExpiresSkew(Duration.ofSeconds(30));

	        return oauth;
	    }

	    @Bean
	    public ClientHttpConnector clientHttpConnector() {

	        log.info("WebClientConfiguration.clientHttpConnector()");

	        TcpClient tcpClient = TcpClient.create()
	                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
	                .doOnConnected(connection ->
	                        connection.addHandlerLast(new ReadTimeoutHandler(1))
	                                .addHandlerLast(new WriteTimeoutHandler(1))
	                );

	        return new ReactorClientHttpConnector(HttpClient.from(tcpClient));
	    }
	    
	    @Bean
	    public OAuth2AuthorizedClientManager authorizedClientManager(
	            ClientRegistrationRepository clientRegistrationRepository,
	            OAuth2AuthorizedClientService clientService)
	    {

	        OAuth2AuthorizedClientProvider authorizedClientProvider = 
	            OAuth2AuthorizedClientProviderBuilder.builder()
	                .clientCredentials()
	                .build();

	        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = 
	            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
	                clientRegistrationRepository, clientService);
	        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
	        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper());
	        return authorizedClientManager;
	    }

		private Function<OAuth2AuthorizeRequest, Map<String, Object>> contextAttributesMapper() {
			return authorizeRequest -> {
				Map<String, Object> contextAttributes = Collections.emptyMap();
				HttpServletRequest servletRequest = authorizeRequest.getAttribute(HttpServletRequest.class.getName());
				String username = "admin";// servletRequest.getParameter(OAuth2ParameterNames.USERNAME);
				String password = "openslice";//servletRequest.getParameter(OAuth2ParameterNames.PASSWORD);
				if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
					contextAttributes = new HashMap<>();

					// `PasswordOAuth2AuthorizedClientProvider` requires both attributes
					contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);
					contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);
				}
				return contextAttributes;
			};
		}

    
}
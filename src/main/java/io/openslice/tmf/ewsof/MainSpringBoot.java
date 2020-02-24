/*-
 * ========================LICENSE_START=================================
 * io.openslice.osom
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.openslice.tmf.ewsof;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;

import io.openslice.tmf.scm633.model.ServiceCatalog;
import io.openslice.tmf.scm633.model.ServiceSpecification;

/**
 * @author ctranoris
 *
 */

//@EnableDiscoveryClient
//@RefreshScope
//@EnableConfigurationProperties
//@EnableAutoConfiguration
@SpringBootApplication
//@ComponentScan(basePackages = { 
//		"io.openslice.osom"
//})

public class MainSpringBoot implements CommandLineRunner {

	private static ApplicationContext applicationContext;

	

	@Override
	public void run(String... arg0) throws Exception {
		if (arg0.length > 0 && arg0[0].equals("exitcode")) {
			throw new ExitException();
		}

	}

	public static void main(String[] args) throws Exception {

		applicationContext = new SpringApplication(MainSpringBoot.class).run(args);

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

	class ExitException extends RuntimeException implements ExitCodeGenerator {
		private static final long serialVersionUID = 1L;

		@Override
		public int getExitCode() {
			return 10;
		}

	}
	

    private static WebClient createWebClientWithServerURLAndDefaultValues() {
        return WebClient.builder()
            .baseUrl("http://portal.openslice.io:80")
            .defaultCookie("cookieKey", "cookieValue")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultUriVariables(Collections.singletonMap("url", "http://portal.openslice.io:80"))
            .build();
    }

}

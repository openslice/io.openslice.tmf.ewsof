package io.openslice.services.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.tmf.ewsof.ExternalSPController;
import io.openslice.tmf.ewsof.MainSpringBoot;
import io.openslice.tmf.pm632.model.Organization;
@RunWith(SpringRunner.class)
@Transactional
@AutoConfigureMockMvc 
@ActiveProfiles("testing")
@SpringBootTest(
		classes = MainSpringBoot.class,
		properties = { "CATALOG_GET_EXTERNAL_SERVICE_PARTNERS = direct:get_mocked_partners" })
public class PartnerIntegrationTest {


	private static final transient Log logger = LogFactory.getLog( PartnerIntegrationTest.class.getName());
	


	@Autowired
	private CamelContext camelContext;


    @Autowired
    ExternalSPController externalSPController;
    
	SPMocked scmocked = new SPMocked();
    
	@Test
	public void getPartners() throws Exception {

		camelContext.removeRoute("timerFetchSPs");
		
		RoutesBuilder builder = new RouteBuilder() {
			@Override
			public void configure() {
				from("direct:get_mocked_partners").bean(scmocked, "getPartners");

			};
		};
		
		camelContext.addRoutes(builder);
		
		List<Organization> sps = externalSPController.fetchSPs();

		assertThat( sps  ).isInstanceOf( List.class); //Organization
		assertThat( sps).hasSize(1);
		assertThat( sps.get(0) ).isInstanceOf( Organization.class);
		
		logger.info("waiting 1secs");
		Thread.sleep(1000); // wait
		
		
		
	}
	
	 static byte[] toJson(Object object) throws IOException {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        return mapper.writeValueAsBytes(object);
	    }
	 
	static <T> T toJsonObj(String content, Class<T> valueType)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue( content, valueType);
    }
}

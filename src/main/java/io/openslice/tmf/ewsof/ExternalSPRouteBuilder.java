package io.openslice.tmf.ewsof;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ctranoris
 *
 */
@Component
public class ExternalSPRouteBuilder extends RouteBuilder {

	private static final transient Log logger = LogFactory.getLog( ExternalSPRouteBuilder.class.getName());
	
	@Autowired
	ExternalSImportClient externalSImportClient;
	

	

    @Autowired
    private ProducerTemplate template;
    
    @Autowired
    ExternalSPController externalSPController;
	
	@Override
	public void configure() throws Exception {
	
		
//		from("timer://getSpecsTimer?delay=2000&period=20000&daemon=true").
//		log(LoggingLevel.INFO, log, "Check External SP Sppecs")
//		.bean( externalSImportClient, "getAll")
//		.convertBodyTo(String.class);
		
		
		from("timer://getSpecsTimer?delay=2000&period=20000&daemon=true")
		.id("timerFetchSPs")
		.to("seda:startProcess");
		
		from("seda:startProcess")
		.log(LoggingLevel.INFO, log, "1-Check External SP Specs")	
		.bean( externalSPController, "fetchSPs")	
		.process(exchange -> log.info("2-The response code is: {}", exchange.getMessage() .getBody() ));
		
		
//		.toD(  CATALOG_GET_EXTERNAL_SERVICE_PARTNERS )
//		.process(exchange -> log.info("3-The response code is: {}", exchange.getMessage() .getBody() ))
//		.convertBodyTo( String.class)
//		.log(LoggingLevel.INFO, log, "4-"+CATALOG_GET_EXTERNAL_SERVICE_PARTNERS + " RESPONSE message received!")
////		.unmarshal()
////		.json(JsonLibrary.Jackson, List.class, true)
////		.bean( externalSImportClient, "printAllSpecs")
//		.convertBodyTo(String.class);
//		
//		/**
//		 * just for test now
//		 */
//		from( CATALOG_GET_EXTERNAL_SERVICE_PARTNERS )
//		.log(LoggingLevel.INFO, log, "2-"+CATALOG_GET_EXTERNAL_SERVICE_PARTNERS + " message received!")
//		.to("log:DEBUG?showBody=true&showHeaders=true")
//		.bean( externalSImportClient, "getExternalPartners")
//		.marshal().json()
//		.convertBodyTo( List.class )
//		.to("log:DEBUG?showBody=true&showHeaders=true");
		
		
	}
	
}

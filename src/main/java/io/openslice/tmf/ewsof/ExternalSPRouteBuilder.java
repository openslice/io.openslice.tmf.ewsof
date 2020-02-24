package io.openslice.tmf.ewsof;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Override
	public void configure() throws Exception {
		
		from("timer://getSpecsTimer?delay=2000&period=20000&daemon=true").
		log(LoggingLevel.INFO, log, "Check External SP Sppecs")
		.bean( externalSImportClient, "getAll")
		.convertBodyTo(String.class);
	}
	
}

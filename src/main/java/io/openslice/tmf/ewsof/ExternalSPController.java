package io.openslice.tmf.ewsof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.openslice.tmf.pm632.model.Organization;

/**
 * @author ctranoris
 *
 */

@Service
public class ExternalSPController {
	

	private static final transient Log logger = LogFactory.getLog( ExternalSPController.class.getName());
	
    @Autowired
    private ProducerTemplate template;

	@Value("${CATALOG_GET_EXTERNAL_SERVICE_PARTNERS}")
	private String CATALOG_GET_EXTERNAL_SERVICE_PARTNERS = "";
	
	
	private List<Organization> externalProviders = new ArrayList<>();
	
	public List<Organization> fetchSPs() {
		
		logger.info("will retrieve Service Providers  from catalog "   );
		try {
			Map<String, Object> map = new HashMap<>();
			Object response = template.
					requestBodyAndHeaders( CATALOG_GET_EXTERNAL_SERVICE_PARTNERS, "", map );

			if ( !(response instanceof String)) {
				logger.error("List  object is wrong.");
				return null;
			}

			Class<List<Organization>> clazz = (Class) List.class;
			List<Organization> organizations = mapJsonToObjectList( new Organization(), (String)response, Organization.class  ); 
			logger.debug("retrieveSPs response is: " + response);
			externalProviders = organizations;
			return organizations;
			
		}catch (Exception e) {
			logger.error("Cannot retrieve Listof Service Providers from catalog. " + e.toString());
		}
		return null;
		
	}
	
	

	static <T> T toJsonObj(String content, Class<T> valueType)  throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue( content, valueType);
    }
	
	 protected static <T> List<T> mapJsonToObjectList(T typeDef,String json,Class clazz) throws Exception
	   {
	      List<T> list;
	      ObjectMapper mapper = new ObjectMapper();
	      System.out.println(json);
	      TypeFactory t = TypeFactory.defaultInstance();
	      list = mapper.readValue(json, t.constructCollectionType(ArrayList.class,clazz));

//	      System.out.println(list);
//	      System.out.println(list.get(0).getClass());
	      return list;
	   }



	public List<Organization> getExternalProviders() {
		return externalProviders;
	}



	public void setExternalProviders(List<Organization> externalProviders) {
		this.externalProviders = externalProviders;
	}
}

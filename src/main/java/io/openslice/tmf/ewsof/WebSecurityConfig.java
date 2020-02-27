package io.openslice.tmf.ewsof;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//@EnableWebFluxSecurity
/**
 * @author ctranoris
 *
 *https://stackoverflow.com/questions/55894460/is-spring-boot-webclient-oauth2-client-credentials-supported
 *https://github.com/fdlessard/SpringBootOauth2WebClient
 *https://github.com/spring-projects/spring-security/blob/master/docs/manual/src/docs/asciidoc/_includes/servlet/oauth2/oauth2-client.adoc#oauth2Client-client-creds-grant
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	

	private static final transient Log log = LogFactory.getLog( WebSecurityConfig.class.getName());
	
//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        http.authorizeExchange()
//            .anyExchange()
//            .authenticated()
//            .and()
//            .oauth2Login();
//        return http.build();
//    }
	
	 @Override
	    protected void configure(HttpSecurity http) throws Exception {

	        log.info("SecurityConfig.configure()");

	        http.authorizeRequests()
	                //   .antMatchers("/actuator/health").permitAll()
	                .antMatchers("/WebClientMessage").authenticated()
	                .and()
	                .httpBasic()
	                //.disable()
	        ;
	        http.csrf().disable();
	    }

	    @Override
	    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

	        log.info("SecurityConfig.configure(AuthenticationManagerBuilder)" + auth.toString());

	        auth.inMemoryAuthentication()
	                .withUser("admin")
	                .password("openslice")
	                .roles("USER");
	    }
	
}
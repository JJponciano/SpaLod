package info.ponciano.lab.spalodwfs.controller.security;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.core.util.Json;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.application.VITE_APP_FRONT_BASE_URL}")
    private String VITE_APP_FRONT_BASE_URL;
	@Autowired
    private UserService userDetailsService;

	@Autowired
    private PasswordEncoder passwordEncoder;

	@Autowired
    private EntryPoint authenticationEntryPoint;

	@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}
	

	@Override 
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/admin").hasRole("ADMIN")
			.antMatchers("/user").hasRole("USER")
			.antMatchers("/register").permitAll()
			.anyRequest().authenticated()
			.and()
			.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
			.and()
			.csrf().disable()
			.cors()
			.and()
			.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.successHandler((request, response, authentication) -> {
					
					String message = "Successfully authenticated as " + authentication.getName() + ".";
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("text/plain");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write(message);
				})
				.failureHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"message\": \"Authentication failed.\"}");
                })
				.permitAll()
			.and()
			.oauth2Login()
				.defaultSuccessUrl(VITE_APP_FRONT_BASE_URL,true);
			//.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
	}



	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}
}

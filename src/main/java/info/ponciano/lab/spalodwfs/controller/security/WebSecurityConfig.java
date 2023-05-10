package info.ponciano.lab.spalodwfs.controller.security;

import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


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
	
	/* How to login as an admin : curl -X POST -i http://localhost:8081/login -d "username=admin&password=admin123" -v
		The -i and -v are used to find the JSESSIONID which is how the we know which sessions is used, it will be used to authenticate the session later.
		Now that we have the JSESSIONID value we can put it in the following command :
		curl -b "JSESSIONID=value" http://localhost:8081/home
		to access to the home page.
		*/


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
			.and()
			.csrf().disable()
			.cors();

			//.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
	}



	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}
}
// OLD DOCUMENT
///*
// * Copyright (C)  2021 Dr Claire Prudhomme <claire@prudhomme.info).
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
// * MA 02110-1301  USA
// */
//package info.ponciano.lab.Spalodwfs.geotime.controllers.security;
//
///**
// *
// * @author Dr Jean-Jacques Ponciano (Contact: jean-jacques@ponciano.info)
// */
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//
///**
// *
// * @author Dr Jean-Jacques Ponciano (Contact: jean-jacques@ponciano.info)
// */
//@Configuration
//@EnableWebSecurity
//public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    /*
//
//
//    	// @formatter:off
//        http
//            .authorizeRequests(a -> a
//                .antMatchers("/", "/error", "/webjars/**").permitAll()
//                .anyRequest().authenticated()
//            )
//            .exceptionHandling(e -> e
//                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
//            )
//            .oauth2Login();
//        // @formatter:on
//    }
//     */
// /*The WebSecurityConfig class is annotated with @EnableWebSecurity to enable
//    Spring Security’s web security support and provide the Spring MVC integration.
//    It also extends WebSecurityConfigurerAdapter and overrides a couple
//    of its methods to set some specifics of the web security configuration.*/
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        oauthLogin(http);
//    }
//
//    protected void oauthLogin(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests(a -> a
//                .antMatchers("/", "/openapi", "/importer", "/home", "/auth", "/user", "/configuration",
//                        "/queryinterface", "/semanticwfs/geotreeview", "/css/main.css", "/css/navbar.css", "/css/form.css",
//                        "/pictures/github.png",
//                        "/semanticwfs/css/style.css", "/semanticwfs/css/leaflet_legend.css",
//                        "/semanticwfs/css/yasqe.css", "/error", "/metadata/update", "/webjars/**")
//                .permitAll()
//               // .anyRequest().authenticated()
//                )
//                .exceptionHandling(e -> {
//            e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/auth"))
////                    e .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
//;
//                }
//
//                ).logout(l -> l
//                .logoutSuccessUrl("/").permitAll()
//                ).csrf(c -> c
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                )
//                .oauth2Login();
//    }
//
//    protected void localLogin(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .antMatchers("/", "/openapi", "/importer", "/home", "/configuration", "/queryinterface", "/semanticwfs/geotreeview", "/css/main.css","/css/form.css", "/semanticwfs/css/style.css", "/semanticwfs/css/leaflet_legend.css", "/semanticwfs/css/yasqe.css").permitAll()// Specifically, the / and /home paths are configured to not require any authentication.
//                //  uncomment this line for enable the security
//                //.anyRequest().authenticated()//All other paths must be authenticated.
//                .and()
//                .formLogin()
//                .loginPage("/login")//There is a custom /login page (which is specified by loginPage())
//                .permitAll()//, and everyone is allowed to view it.
//                .and()
//                .logout()
//                .permitAll();//When a user successfully logs in, they are redirected to the previously requested page that required authentication.
//    }
//
//    /**
//     * The userDetailsService() method sets up an in-memory user store with a
//     * single user. That user is given a user name of user, a password of
//     * password, and a role of USER.
//     *
//     * @return
//     */
//    @Bean
//    @Override
//    public UserDetailsService userDetailsService() {
//        @SuppressWarnings("deprecation")
//		UserDetails user
//                = User.withDefaultPasswordEncoder()
//                        .username("JJ")
//                        .password("qwertz")
//                        .roles("USER")
//                        .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }
//}

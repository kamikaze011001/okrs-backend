package org.ptit.okrs.core_authentication.configuration;

import lombok.RequiredArgsConstructor;
import org.ptit.okrs.core_authentication.error_handle.AuthenticationErrorHandle;
import org.ptit.okrs.core_authentication.filter.TokenAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@ComponentScan(basePackages = {
    "org.ptit.okrs.core_authentication.filter",
    "org.ptit.okrs.core_authentication.error_handle"
})
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(jsr250Enabled = true, securedEnabled = true, prePostEnabled = true)
public class OkrsWebSecurityConfiguration {

  private final TokenAuthenticationFilter tokenAuthenticationFilter;
  private final AuthenticationErrorHandle authenticationErrorHandle;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/api/v1/user/login").permitAll()
        .antMatchers(
            "/swagger-ui**",
            "/v2/api-docs**",
            "/webjars/**", "/error",
            "/swagger-resources",
            "/swagger-resources/**",
            "/api/v1/auth/users/**"
        ).permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling().authenticationEntryPoint(authenticationErrorHandle)
        .and().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().build();
  }
}

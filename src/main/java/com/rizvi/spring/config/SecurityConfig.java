package com.rizvi.spring.config;

import com.rizvi.spring.filter.JwtRequestFilter;
import com.rizvi.spring.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

      private final AppUserDetailsService appUserDetailsService;

      private final JwtRequestFilter jwtRequestFilter;

      private final CustomAuthenticationEntryPoint authenticationEntryPoint;

       @Bean
       public SecurityFilterChain securityFilterChain(
               HttpSecurity http) throws Exception {

         http
                 .cors(Customizer.withDefaults())
                 .csrf(AbstractHttpConfigurer::disable)
                 .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                         .requestMatchers("/login", "/register","/send-reset-otp","/reset-password", "/logout")
                         .permitAll().anyRequest().authenticated())
                 .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 .logout(AbstractHttpConfigurer::disable)
                 .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                 .exceptionHandling(ex ->ex.authenticationEntryPoint(authenticationEntryPoint));
                 return http.build();
      }

      @Bean
      public PasswordEncoder passwordEncoder() {
           return new BCryptPasswordEncoder();
      }

      @Bean
      public CorsFilter corsFilter() {
           return new CorsFilter(corsConfigurationSource());
      }

    private CorsConfigurationSource corsConfigurationSource() {
           CorsConfiguration config = new CorsConfiguration();
           config.setAllowedOrigins(List.of("http://localhost:5173"));
           config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
           config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
           config.setAllowCredentials(true);

           UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
           source.registerCorsConfiguration("/**", config);
           return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        // ✅ Use the constructor that takes the UserDetailsService
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(appUserDetailsService);
        // ✅ Set the password encoder for BCrypt validation
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}

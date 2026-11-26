package pe.edu.idat.essaludmiconsulta.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en los controllers/services
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // En Spring Security 7 (Spring Boot 4) el UserDetailsService ya no
        // se inyecta con setUserDetailsService(); ahora es obligatorio
        // pasarlo por el constructor.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // no aplica: la API es stateless, sin cookies de sesión
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Públicos: login y registro de paciente
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Catálogo de especialidades, médicos, búsqueda de horarios e
                        // información general: visible sin login (el paciente necesita ver
                        // la oferta antes de registrarse)
                        .requestMatchers(HttpMethod.GET, "/api/v1/especialidades/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/horarios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/informacion/**").permitAll()
                        // Solo ADMIN gestiona médicos, especialidades y horarios
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // El resto de endpoints de negocio requieren estar autenticado
                        // (el detalle de "solo puede ver SUS citas" se valida a nivel
                        // de servicio comparando el DNI del token con el del recurso)
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Orígenes de los frontends React + Vite en desarrollo: el de
        // pacientes (Mi Consulta, puerto 5173) y el administrativo/EFI
        // (puerto 5174, fijado en frontend-efi/vite.config.js).
        // Al desplegar a producción, agregar aquí los dominios reales.
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

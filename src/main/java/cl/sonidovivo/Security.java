package cl.sonidovivo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.*; import org.springframework.security.authentication.*; import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.web.*; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; import org.springframework.stereotype.*; import org.springframework.web.cors.*; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.nio.charset.StandardCharsets; import java.security.Key; import java.util.*;

@Service class JwtService {
  private final Key key; private final long expiration;
  JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiration){ if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT secret debe contener al menos 32 bytes"); this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiration=expiration; }
  String generar(Usuario usuario){ return Jwts.builder().subject(usuario.email).claim("rol",usuario.rol.name()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration)).signWith(key).compact(); }
  String email(String token){ return Jwts.parser().verifyWith((javax.crypto.SecretKey)key).build().parseSignedClaims(token).getPayload().getSubject(); }
}
@Component class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final UsuarioRepository usuarios;
  JwtAuthenticationFilter(JwtService jwt,UsuarioRepository usuarios){this.jwt=jwt;this.usuarios=usuarios;}
  @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
    String header=req.getHeader(HttpHeaders.AUTHORIZATION);
    if(header!=null&&header.startsWith("Bearer ")) try { Usuario u=usuarios.findByEmailIgnoreCase(jwt.email(header.substring(7))).filter(x->x.activo).orElse(null); if(u!=null){ var auth=new UsernamePasswordAuthenticationToken(u.email,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.rol.name()))); SecurityContextHolder.getContext().setAuthentication(auth); } } catch(JwtException|IllegalArgumentException ignored){}
    chain.doFilter(req,res);
  }
}
@Configuration @EnableWebSecurity @EnableMethodSecurity class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
  @Bean FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter){FilterRegistrationBean<JwtAuthenticationFilter> registration=new FilterRegistrationBean<>(filter);registration.setEnabled(false);return registration;}
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwt)throws Exception{return http.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->error(r,401,"No autenticado")).accessDeniedHandler((q,r,x)->error(r,403,"No autorizado"))).authorizeHttpRequests(a->a.requestMatchers("/auth/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/api/productos/**","/api/contacto","/api/reparaciones","/api/pedidos/seguimiento").permitAll().anyRequest().authenticated()).addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();}
  @Bean CorsConfigurationSource corsConfigurationSource(){ CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:5173"));c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));c.setExposedHeaders(List.of("Authorization")); UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s; }
  private void error(HttpServletResponse r,int status,String message)throws IOException{r.setStatus(status);r.setContentType(MediaType.APPLICATION_JSON_VALUE);new ObjectMapper().writeValue(r.getOutputStream(),Map.of("status",status,"message",message));}
}
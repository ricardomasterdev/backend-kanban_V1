package com.seuprojeto.kanban.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.io.Decoders; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import java.security.Key; import java.util.Date;
@Component public class JwtService {
  private final Key key; private final long expiration;
  public JwtService(@Value("${security.jwt.secret}") String base64Secret, @Value("${security.jwt.expiration}") long expirationSeconds){
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret)); this.expiration = expirationSeconds*1000;
  }
  public String generate(String subject){ Date now=new Date(); Date exp=new Date(now.getTime()+expiration); return Jwts.builder().setSubject(subject).setIssuedAt(now).setExpiration(exp).signWith(key, SignatureAlgorithm.HS256).compact(); }
  public String validateAndGetSubject(String token){ return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject(); }
}

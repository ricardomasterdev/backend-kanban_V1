package com.seuprojeto.kanban.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI apiInfo(){
    return new OpenAPI()
      .components(new Components().addSecuritySchemes("bearerAuth",
          new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
      .info(new Info()
        .title("API Kanban Projetos")
        .version("1.1.0")
        .description("API documentada com exemplos de payload/respostas e erros padronizados (400/401/404/422).\n"
                   + "1) Faça login em /api/v1/auth/login para obter o token.\n"
                   + "2) Clique em Authorize e informe 'Bearer <token>' para testar as rotas protegidas."));
  }
}

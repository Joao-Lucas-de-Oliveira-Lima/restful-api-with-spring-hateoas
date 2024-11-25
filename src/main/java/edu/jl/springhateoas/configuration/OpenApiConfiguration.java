package edu.jl.springhateoas.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI configureOpenApi() {
        return new OpenAPI().info(new Info()
                .description("")
                .version("v1.0.0")
                .title("Hateoas Spring")
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/license/mit")));
    }
}

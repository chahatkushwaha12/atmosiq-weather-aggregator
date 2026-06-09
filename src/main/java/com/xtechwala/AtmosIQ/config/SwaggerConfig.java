package com.xtechwala.AtmosIQ.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*@Configuration:- Spring application start hote hi is class ko scan karega
        aur iske andar jo beans hain unhe register karega.*/
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI atmosIQOpenAPI(){
        return new OpenAPI()
                .info(new Info() // OpenAPI ke andar API ki information set kar rahe hain
                        .title("AtmosisIQ Weather API")
                        .description("Real-time weather intelligence with multi-provider fallback, DB caching, and auto-refresh.")
                        .version("v1.0.0")
                        .contact(new Contact() // Developer ki details add kar rahe ho.
                                .name("xtechwala")
                                .url("https://github.com/chahatkushwaha12/atmosiq-weather-aggregator"))
                        .license(new License() // Adds project license information to Swagger/OpenAPI documentation.
                                .name("MIT License")
                                .url("https://opensource.org/license/MIT")));
    }

}

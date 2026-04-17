package com.example.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  @SpringBootApplication                                       ║
 * ║                                                               ║
 * ║  This is a META-ANNOTATION — a shortcut that combines:        ║
 * ║                                                               ║
 * ║  1. @Configuration                                            ║
 * ║     Marks this class as a source of Spring Bean definitions. ║
 * ║     Spring will look here for @Bean methods.                  ║
 * ║                                                               ║
 * ║  2. @EnableAutoConfiguration                                  ║
 * ║     Tells Spring Boot to auto-configure beans based on the    ║
 * ║     JARs on the classpath. Sees H2? Configures a datasource.  ║
 * ║     Sees Spring MVC? Configures a DispatcherServlet.          ║
 * ║                                                               ║
 * ║  3. @ComponentScan                                            ║
 * ║     Scans the current package (and sub-packages) for classes  ║
 * ║     annotated with @Component, @Service, @Repository,         ║
 * ║     @Controller, @RestController, etc., and registers them    ║
 * ║     as Spring Beans in the ApplicationContext.                ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
@SpringBootApplication
public class CrudDemoApplication {

    public static void main(String[] args) {
        /**
         * SpringApplication.run():
         * 1. Creates an ApplicationContext (the Spring IoC container)
         * 2. Registers all beans found by @ComponentScan
         * 3. Applies auto-configuration
         * 4. Starts the embedded Tomcat server on port 8080
         */
        SpringApplication.run(CrudDemoApplication.class, args);

        System.out.println("""
                ╔══════════════════════════════════════════════════╗
                ║   Spring Boot CRUD Demo is running!              ║
                ║                                                  ║
                ║   API Base URL : http://localhost:8080/api       ║
                ║   H2 Console   : http://localhost:8080/h2-console║
                ║   (JDBC URL: jdbc:h2:mem:cruddb)                 ║
                ╚══════════════════════════════════════════════════╝
                """);
    }
}

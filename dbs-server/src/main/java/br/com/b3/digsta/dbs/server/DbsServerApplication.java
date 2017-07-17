package br.com.b3.digsta.dbs.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@ComponentScan(basePackages = {"br.com.b3.digsta.dbs"})
@SpringBootApplication(scanBasePackages={"br.com.b3.digsta.dbs"})
@PropertySources({
	@PropertySource("file:///${project.properties}"),
})
public class DbsServerApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DbsServerApplication.class, args);
		ApplicationBoot applicationBoot = context.getBean(ApplicationBoot.class);
		applicationBoot.start();
	}
}

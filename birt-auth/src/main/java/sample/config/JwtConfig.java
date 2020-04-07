package sample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@ConfigurationProperties(prefix = "birt.runner.security")
public class JwtConfig {
	private String Uri = "/auth/**";
	String header = "Authorization";
	String prefix = "Bearer ";
	int expiration = 24 * 60 * 60;
	String secret = "JwtSecretKey";
}

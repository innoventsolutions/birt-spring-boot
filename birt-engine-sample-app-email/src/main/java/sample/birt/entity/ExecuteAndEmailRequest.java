package sample.birt.entity;

import com.innoventsolutions.birt.entity.ExecuteRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecuteAndEmailRequest {
	private ExecuteRequest executeRequest;
	private EmailRequest emailRequest;
}

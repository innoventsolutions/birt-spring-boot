package sample.birt.entity;

import com.innoventsolutions.birt.entity.ExecuteRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecuteAndEmailRequest extends ExecuteRequest {
	private EmailRequest email;
}

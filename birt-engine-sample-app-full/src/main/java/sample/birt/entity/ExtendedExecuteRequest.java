package sample.birt.entity;

import com.innoventsolutions.birt.entity.ExecuteRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtendedExecuteRequest extends ExecuteRequest {
	private ScheduleRequest schedule;
	private EmailRequest email;
}

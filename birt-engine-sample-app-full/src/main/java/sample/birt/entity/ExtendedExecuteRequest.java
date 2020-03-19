package sample.birt.entity;

import com.innoventsolutions.birt.entity.ExecuteRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExtendedExecuteRequest extends ExecuteRequest {
	private ScheduleRequest schedule;
	private EmailRequest email;
}

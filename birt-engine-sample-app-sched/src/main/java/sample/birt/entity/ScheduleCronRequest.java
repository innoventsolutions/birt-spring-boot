package sample.birt.entity;

import java.util.Date;

import com.innoventsolutions.birt.entity.ExecuteRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleCronRequest extends ExecuteRequest {
	private String cronString;
	private String misfireInstruction;
	private String group;
	private String name;
	private Date startDate;
}

package sample.birt.entity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleRequest {
	private String cronString;
	private String misfireInstruction;
	private String group;
	private String name;
	private Date startDate;
}

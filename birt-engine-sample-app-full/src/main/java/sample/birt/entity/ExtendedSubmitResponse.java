package sample.birt.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.JobKey;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtendedSubmitResponse extends SubmitResponse {
	private Date emailBegin;
	private Date emailFinish;
	private JobKey jobKey; // set if job was scheduled

	public ExtendedSubmitResponse(final ExecuteRequest request) {
		super(request);
	}

	public String replace(String string) {
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		string = string.replace("${designFileName}", request.getDesignFile());
		string = string.replace("${startTime}", df.format(runBegin));
		final Date finishTime = this.renderFinish;
		string = string.replace("${finishTime}", finishTime != null ? df.format(finishTime) : "");
		return string;
	}
}

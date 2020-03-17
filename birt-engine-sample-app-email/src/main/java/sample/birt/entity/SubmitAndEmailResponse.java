package sample.birt.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.innoventsolutions.birt.entity.ExecuteRequest;
import com.innoventsolutions.birt.entity.SubmitResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class SubmitAndEmailResponse extends SubmitResponse {
	private Date emailBegin;
	private Date emailFinish;

	public SubmitAndEmailResponse(final ExecuteRequest request) {
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

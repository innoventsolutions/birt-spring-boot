package sample.birt.entity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.innoventsolutions.birt.entity.SubmitResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetJobResponse {
	// List<Trigger> triggers;
	List<CompletableFuture<SubmitResponse>> runs;
}
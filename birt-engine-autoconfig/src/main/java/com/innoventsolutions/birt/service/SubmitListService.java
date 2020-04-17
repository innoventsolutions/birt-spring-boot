package com.innoventsolutions.birt.service;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.entity.SubmitResponse;

@Service
public class SubmitListService extends HashMap<String, CompletableFuture<SubmitResponse>> {
	private static final long serialVersionUID = 1L;
}

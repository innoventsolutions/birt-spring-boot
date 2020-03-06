package com.innoventsolutions.birt.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TestCF {

	public TestCF() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		
		System.out.println("Current Thread " + Thread.currentThread());
		CompletableFuture.runAsync(() -> System.out.println("Run async in completable future " + Thread.currentThread()));

		CompletableFuture<String> welcomeText = CompletableFuture.supplyAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
			return "Rajeev";
		}).thenApply(name -> {
			return "Hello " + name;
		}).thenApply(greeting -> {
			return greeting + ", Welcome to the CalliCoder Blog";
		});

		System.out.println(welcomeText.get());


		CompletableFuture<Integer> welcomeNext = CompletableFuture.supplyAsync(() -> slowGet())
				.thenApplyAsync(b -> {System.out.println("s2: " + Thread.currentThread()); return b - 2;})
				.thenApplyAsync(k -> {System.out.println("s3: " + Thread.currentThread()); return k * 3;})
				.thenApply(l -> {System.out.println("The result is " + l); return l*l; });

		System.out.println(welcomeNext.get());

	}

	private static Integer slowGet() {
		System.out.println("Enter slow get: " + Thread.currentThread());
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			System.out.println("timer exception");
		}
		System.out.println("finish slow get");
		return 5;
	}

}

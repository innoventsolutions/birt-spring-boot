package com.innoventsolutions.birt;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innoventsolutions.birt.config.BirtProperties;

@ConfigurationProperties(prefix = "birt.runner")
public class BirtConfig extends BirtProperties implements Serializable {

	private static final long serialVersionUID = -6821187546631689762L;

}

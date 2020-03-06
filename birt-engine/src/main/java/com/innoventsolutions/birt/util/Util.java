package com.innoventsolutions.birt.util;

import org.springframework.http.MediaType;

public class Util {

	public Util() {
		// TODO Auto-generated constructor stub
	}

	public static MediaType getMediaType(final String format) {
		if ("pdf".equalsIgnoreCase(format)) {
			return MediaType.APPLICATION_PDF;
		}
		if ("html".equalsIgnoreCase(format)) {
			return MediaType.TEXT_HTML;
		}
		if ("xls".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-excel");
		}
		if ("xlsx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
		if ("doc".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/ms-word");
		}
		if ("docx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}
		if ("ppt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-powerpoint");
		}
		if ("pptx".equalsIgnoreCase(format)) {
			return MediaType
					.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
		}
		if (".odp".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.presentation");
		}
		if (".ods".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.spreadsheet");
		}
		if (".odt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.text");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}



}

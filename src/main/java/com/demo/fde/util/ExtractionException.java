package com.demo.fde.util;

public class ExtractionException extends Exception {
	
	private static final long serialVersionUID = -5108931481040742838L;

	ExtractionException(String msg) {
		super(msg);
	}

	public ExtractionException(String msg, Exception e) {
		super(msg, e);
	}
}

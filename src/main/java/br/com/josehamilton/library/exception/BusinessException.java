package br.com.josehamilton.library.exception;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = -8307504616248622740L;

	public BusinessException(String s) {
		super(s);
	}
}

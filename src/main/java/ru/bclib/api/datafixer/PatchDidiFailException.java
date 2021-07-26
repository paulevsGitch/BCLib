package ru.bclib.api.datafixer;

public class PatchDidiFailException extends Exception {
	public PatchDidiFailException(){
		super();
	}
	public PatchDidiFailException(Exception e){
		super(e);
	}
}

package org.probe.rls.data;

public enum FileType {
	CSV(","),
	TSV("\t");
	
	FileType(String str){
		this.str = str;
	}
	
	public String getSeparator(){
		return str;
	}
	
	private final String str;
}

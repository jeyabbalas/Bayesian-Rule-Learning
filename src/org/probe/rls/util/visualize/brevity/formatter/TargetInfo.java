package org.probe.rls.util.visualize.brevity.formatter;

public class TargetInfo {
	
	public TargetInfo() {
		this.targetName = null;
		this.targetValue = null;
		this.targetColor = null;
	}

	public TargetInfo(String targetName, String targetValue) {
		this.targetName = targetName;
		this.targetValue = targetValue;
		
		if (targetValue.equalsIgnoreCase("Yes")) {
			this.targetColor = "#8B008B";
		} else if (targetValue.equalsIgnoreCase("No")) {
			this.targetColor = "#DDA0DD";
		}
	}
	
	public String getTargetName() {
		return targetName;
	}
	
	public String getTargetValue() {
		return targetValue;
	}
	
	public String getTargetColor() {
		return targetColor;
	}
	
	
	private String targetName;
	private String targetValue;
	private String targetColor;
}

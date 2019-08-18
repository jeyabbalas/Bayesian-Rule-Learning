package org.probe.rls.models.rulemodel;

public enum LiteralRelation {
	GREATER_THAN(">"), LESSER_THAN("<"), EQUAL("="), GREATER_THAN_OR_EQUAL_TO(">="), LESSER_THAN_OR_EQUAL_TO("<="),
	NOT_EQUAL_TO("!="), UNKNOWN("UNKNOWN");

	LiteralRelation(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}

	private String str;
}

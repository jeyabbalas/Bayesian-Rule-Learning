package org.probe.rls.models.rulemodel.general;

import java.util.List;
import java.util.StringTokenizer;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.LiteralRelation;
import org.probe.rls.models.rulemodel.RuleLiteral;
import org.probe.rls.models.rulemodel.UnexpectedLiteralRelationException;
import org.probe.rls.util.RuleFormatter;

public class Literal implements RuleLiteral {

	public Literal(String field, LiteralRelation relation, String value) {
		this.setIdentifier();
		this.field = field;
		this.relation = relation;
		this.value = value;
	}
	
	private void setIdentifier() {
		this.identifer = "Conjunct:"+(Literal.callCounter++);
	}
	
	public void setIdentifier(String identifierStr) {
		this.identifer = identifierStr;
	}
	
	public String getIdentifier() {
		return this.identifer;
	}

	public static Literal parseString(String literalStr) {
		String parsedStr = literalStr;
		while(RuleFormatter.isEnclosedInParenthesis(parsedStr.trim())) {
			parsedStr = RuleFormatter.removeParentheses(parsedStr.trim());
		}
		return parseStrForLiteral(parsedStr);
	}

	private static Literal parseStrForLiteral(String literalStr) {
		LiteralRelation thisRelation = LiteralRelation.UNKNOWN;
		if (isEqualRelation(literalStr)) {
			thisRelation = LiteralRelation.EQUAL;
		} 

		return getItemsFromStr(literalStr, thisRelation);
	}

	private static boolean isEqualRelation(String literalStr) {
		return literalStr.contains(LiteralRelation.EQUAL.toString());
	}

	private static Literal getItemsFromStr(String literalStr,
			LiteralRelation thisRelation) {
		String relationStr = thisRelation.toString();

		StringTokenizer st = new StringTokenizer(literalStr, relationStr);
		String field = st.nextToken().trim();
		String value = st.nextToken().trim();

		return new Literal(field, thisRelation, value);
	}

	public boolean matchesInstance(DataModel dataModel, int rowIndex) {
		DataAttribute conjunctAttribute = getConjunctDataAttribute(dataModel);
		List<String> attributeValues = dataModel.getColumnItemsByIndex(conjunctAttribute.getAttributeIndex());
		String dataValue = attributeValues.get(rowIndex);
		
		return compareWithNominalValue(dataValue);
	}

	private DataAttribute getConjunctDataAttribute(DataModel dataModel) {
		List<DataAttribute> attributes = dataModel.getAttributes();
		for(DataAttribute attribute : attributes) {
			if (attribute.getAttributeName().equalsIgnoreCase(field)) {
				return attribute;
			}
		}

		return null;
	}

	private boolean compareWithNominalValue(String dataValue) {
		try{
			if(relation.equals(LiteralRelation.EQUAL)){
				if(equalsAnyLiteralValue(dataValue)) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new UnexpectedLiteralRelationException("For "
						+ "nominal attributes only EQUAL_TO: \"=\" "
						+ "relations is supported.");
			}
		} catch (UnexpectedLiteralRelationException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private boolean equalsAnyLiteralValue(String dataValue) {
		if(value.contains(",")) {
			// split commas except when within square brackets (imposed during discretization).
			String[] values = value.split(",(?![^\\[]*\\])");
			boolean equals = false;
			for(String literalValue: values) {
				if(dataValue.equalsIgnoreCase(literalValue)) {
					return true;
				}
			}
			return equals;
		} else {
			return dataValue.equalsIgnoreCase(value);
		}
	}

	public static boolean isLiteral(String str) {
		return (str.split("=").length == 2);
	}

	public String getField() {
		return field;
	}

	public LiteralRelation getRelation() {
		return relation;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(field).append(relation.toString()).append(value);
		sb.append(")");
		return sb.toString();
	}
	
	
	private String identifer;
	private static int callCounter = 1;

	private final String field;
	private final LiteralRelation relation;
	private final String value;
}

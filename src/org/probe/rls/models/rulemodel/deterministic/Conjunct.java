package org.probe.rls.models.rulemodel.deterministic;

import java.util.List;
import java.util.StringTokenizer;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.LiteralRelation;
import org.probe.rls.models.rulemodel.RuleLiteral;
import org.probe.rls.models.rulemodel.UnexpectedLiteralRelationException;
import org.probe.rls.util.RuleFormatter;

public class Conjunct implements RuleLiteral {

	public Conjunct(String field, LiteralRelation relation, String value) {
		this.setIdentifier();
		this.field = field;
		this.relation = relation;
		this.value = value;
	}
	
	private void setIdentifier() {
		this.identifer = "Conjunct:"+(Conjunct.callCounter++);
	}
	
	public void setIdentifier(String identifierStr) {
		this.identifer = identifierStr;
	}
	
	public String getIdentifier() {
		return this.identifer;
	}

	public static Conjunct parseString(String conjunctStr) {
		String parsedStr = RuleFormatter.formatRuleString(conjunctStr);
		return parseStrForConjunct(parsedStr);
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

	public boolean containsField(String field) {
		return this.field.equals(field);
	}
	
	public boolean matchesInstance(DataModel dataModel, int rowIndex) {
		DataAttribute conjunctAttribute = getConjunctDataAttribute(dataModel);
		List<String> attributeValues = dataModel.getColumnItemsByIndex(conjunctAttribute.getAttributeIndex());
		
		String dataValue = attributeValues.get(rowIndex);
		
		if (conjunctAttribute.isNominal()) {
			return compareWithNominalValue(dataValue);
		} else {
			return compareWithDoubleValue(dataValue);
		}
	}
	
	private boolean compareWithNominalValue(String dataValue) {
		
		try{
			if(relation.equals(LiteralRelation.EQUAL)){
				if(dataValue.equalsIgnoreCase(value)) {
					return true;
				} else {
					return false;
				}
			} else if(relation.equals(LiteralRelation.NOT_EQUAL_TO)) {
				if(!dataValue.equalsIgnoreCase(value)) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new UnexpectedLiteralRelationException("For "
						+ "nominal attributes only EQUAL_TO: \"=\" "
						+ "and NOT_EQUAL_TO: \"!=\" relations are "
						+ "supported.");
			}
		} catch (UnexpectedLiteralRelationException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean compareWithDoubleValue(String dataValue) {
		Double doubleDataValue = Double.valueOf(dataValue);
		Double doubleConjunctValue = Double.valueOf(value);
		
		try {
			if (relation.equals(LiteralRelation.GREATER_THAN)) {
				if (doubleDataValue > doubleConjunctValue) {
					return true;
				} else {
					return false;
				}
			} else if (relation.equals(LiteralRelation.LESSER_THAN)) {
				if (doubleDataValue < doubleConjunctValue) {
					return true;
				} else {
					return false;
				}
				
			} else if (relation.equals(LiteralRelation.GREATER_THAN_OR_EQUAL_TO)) {
				if (doubleDataValue >= doubleConjunctValue) {
					return true;
				} else {
					return false;
				}
				
			} else if (relation.equals(LiteralRelation.LESSER_THAN_OR_EQUAL_TO)) {
				if (doubleDataValue <= doubleConjunctValue) {
					return true;
				} else {
					return false;
				}
				
			} else if (relation.equals(LiteralRelation.EQUAL)) {
				if (doubleDataValue.equals(doubleConjunctValue)) {
					return true;
				} else {
					return false;
				}
			} else if (relation.equals(LiteralRelation.NOT_EQUAL_TO)) {
				if (!doubleDataValue.equals(doubleConjunctValue)) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new UnexpectedLiteralRelationException("Unsupported "
						+ "Conjunct relation found: "+relation);
			}
		} catch (UnexpectedLiteralRelationException e) {
			e.printStackTrace();
		}
		
		
		return false;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(field).append(" ").append(relation.toString()).append(" ")
				.append(value);
		sb.append(")");
		return sb.toString();
	}

	private static Conjunct parseStrForConjunct(String conjunctStr) {
		LiteralRelation thisRelation = LiteralRelation.UNKNOWN;
		if (isGreaterThanRelation(conjunctStr)) {
			thisRelation = LiteralRelation.GREATER_THAN;
		} else if (isEqualRelation(conjunctStr)) {
			thisRelation = LiteralRelation.EQUAL;
		} else if (isLessThanRelation(conjunctStr)) {
			thisRelation = LiteralRelation.LESSER_THAN;
		} else if (isGreaterThanOrEqualToRelation(conjunctStr)) {
			thisRelation = LiteralRelation.GREATER_THAN_OR_EQUAL_TO;
		} else if (isLessThanOrEqualToRelation(conjunctStr)) {
			thisRelation = LiteralRelation.LESSER_THAN_OR_EQUAL_TO;
		} else if (isNotEqualToRelation(conjunctStr)) {
			thisRelation = LiteralRelation.NOT_EQUAL_TO;
		}

		return getItemsFromStr(conjunctStr, thisRelation);
	}

	private static boolean isLessThanRelation(String conjunctStr) {
		return conjunctStr.contains(LiteralRelation.LESSER_THAN.toString());
	}

	private static boolean isEqualRelation(String conjunctStr) {
		return conjunctStr.contains(LiteralRelation.EQUAL.toString());
	}

	private static boolean isGreaterThanRelation(String conjunctStr) {
		return conjunctStr.contains(LiteralRelation.GREATER_THAN.toString());
	}
	
	private static boolean isGreaterThanOrEqualToRelation(String conjunctStr) {
		return conjunctStr.contains(LiteralRelation.GREATER_THAN_OR_EQUAL_TO.toString());
	}
	
	private static boolean isLessThanOrEqualToRelation(String conjunctStr) {
		return conjunctStr.contains(LiteralRelation.LESSER_THAN_OR_EQUAL_TO.toString());
	}
	
	private static boolean isNotEqualToRelation(String conjunctStr) {
		return conjunctStr.contains(LiteralRelation.NOT_EQUAL_TO.toString());
	}

	private static Conjunct getItemsFromStr(String conjunctStr,
			LiteralRelation thisRelation) {
		String relationStr = thisRelation.toString();

		StringTokenizer st = new StringTokenizer(conjunctStr, relationStr);
		String field = st.nextToken().trim();
		String value = st.nextToken().trim();

		return new Conjunct(field, thisRelation, value);
	}
	
	private String identifer;
	private static int callCounter = 1;

	private final String field;
	private final LiteralRelation relation;
	private final String value;
}

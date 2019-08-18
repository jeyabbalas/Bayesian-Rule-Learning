package org.probe.rls.models.rulemodel.general;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.probe.rls.data.DataModel;
import org.probe.rls.models.rulemodel.RuleLHS;
import org.probe.rls.models.rulemodel.RuleLiteral;
import org.probe.rls.util.RuleFormatter;

/**
 * Builds a boolean algebra expression tree for the LHS of a rule.
 * 
 * @author jeya
 *
 */
public class Expression implements RuleLHS  {

	public Expression(ExpressionNode root) {
		this.setIdentifier();
		this.root = root;
		this.accountFieldsUsed();
	}

	private void setIdentifier() {
		this.identifer = "LHS:"+(Expression.callCounter++);
	}

	public String getIdentifier() {
		return this.identifer;
	}

	public static Expression parseString(String ruleLHSStr) {
		Expression ruleLHS = new Expression(constructExpressionTree(ruleLHSStr.trim()));
		return ruleLHS;
	}

	private void accountFieldsUsed() {
		this.fieldsUsed = new TreeSet<String>(this.root.getFieldsUsed());
	}


	static class ExpressionNode {
		public ExpressionNode(boolean isDisjunctive, List<ExpressionNode> children) {
			this.isDisjunctive = isDisjunctive;
			this.children = children;
			this.isLeaf = false;
		}
		public boolean isDisjunctive() {//else conjunctive
			return this.isDisjunctive;
		}
		public void addChild(ExpressionNode child) {
			this.children.add(child);
		}
		public boolean matchesInstance(DataModel dataModel, int rowIndex) {
			if(isDisjunctive) {
				return logicalOrOnChildren(dataModel, rowIndex);
			} else {
				return logicalAndOnChildren(dataModel, rowIndex);
			}
		}
		private boolean logicalOrOnChildren(DataModel dataModel, int rowIndex) {
			boolean matches = false;
			for(ExpressionNode child: children) {
				matches = matches || child.matchesInstance(dataModel, rowIndex);
			}
			return matches;
		}
		private boolean logicalAndOnChildren(DataModel dataModel, int rowIndex) {
			boolean matches = true;
			for(ExpressionNode child: children) {
				matches = matches && child.matchesInstance(dataModel, rowIndex);
			}
			return matches;
		}
		public Set<String> getFieldsUsed() {
			Set<String> nodeFieldsUsed = new TreeSet<String>();			
			for(int i=0; i<children.size(); i++) {
				nodeFieldsUsed.addAll(children.get(i).getFieldsUsed());
			}
			return nodeFieldsUsed;
		}
		public Set<RuleLiteral> getLiteralsUsed() {
			Set<RuleLiteral> nodeLiteralsUsed = new TreeSet<RuleLiteral>();			
			for(int i=0; i<children.size(); i++) {
				nodeLiteralsUsed.addAll(children.get(i).getLiteralsUsed());
			}
			return nodeLiteralsUsed;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("(");
			for(int i=0; i<children.size(); i++) {
				if(isDisjunctive && i>0) {
					sb.append(",");
				}
				sb.append(children.get(i).toString());
			}
			sb.append(")");

			return sb.toString();
		}

		boolean isDisjunctive;
		List<ExpressionNode> children;
		boolean isLeaf;
	}


	static class ExpressionLeaf extends ExpressionNode {
		public ExpressionLeaf(boolean isDisjunctive, Literal literal) {
			super(isDisjunctive, new LinkedList<ExpressionNode>());
			this.literal = literal;
			this.isLeaf = true;
		}
		@Override
		public boolean matchesInstance(DataModel dataModel, int rowIndex) {
			return literal.matchesInstance(dataModel, rowIndex);
		}
		@Override
		public Set<String> getFieldsUsed() {
			Set<String> nodeFieldsUsed = new TreeSet<String>();			
			nodeFieldsUsed.add(literal.getField());
			return nodeFieldsUsed;
		}
		@Override
		public Set<RuleLiteral> getLiteralsUsed() {
			Set<RuleLiteral> nodeLiteralsUsed = new TreeSet<RuleLiteral>();			
			nodeLiteralsUsed.add(literal);
			return nodeLiteralsUsed;
		}
		@Override
		public String toString() {
			return literal.toString();
		}

		Literal literal;
	}


	private static ExpressionNode constructExpressionTree(String infix) {
		checkBadArguments(infix);

		ExpressionNode subTreeRoot = null;
		ArrayList<String> ruleLiterals = splitIntoLiteralsAndRelationship(infix);

		String relationship = ruleLiterals.get(ruleLiterals.size()-1);
		ruleLiterals.remove(relationship);
		boolean isDisjunctive = false;
		if(relationship.equalsIgnoreCase("||")) {
			isDisjunctive = true;
		}

		if(ruleLiterals.size() == 1) {
			if(!Literal.isLiteral(ruleLiterals.get(0))) {
				throw new IllegalArgumentException("Incorrect syntax for input rule string.");
			}
			Literal onlyLiteral = Literal.parseString(ruleLiterals.get(0));
			subTreeRoot = new ExpressionLeaf(isDisjunctive, onlyLiteral);
			return subTreeRoot;
		}

		subTreeRoot = constructSubTreeFromLiterals(ruleLiterals, isDisjunctive);

		return subTreeRoot;
	}

	private static ExpressionNode constructSubTreeFromLiterals(ArrayList<String> ruleLiterals, boolean isDisjunctive) {
		ExpressionNode parent = new ExpressionNode(isDisjunctive, new LinkedList<ExpressionNode>());

		for(int i=0; i<ruleLiterals.size(); i++) {
			ExpressionNode child = parseComplexLiteral(ruleLiterals.get(i), isDisjunctive);
			parent.addChild(child);
		}

		return parent;
	}

	private static ExpressionNode parseComplexLiteral(String complexLiteral, boolean isDisjunctive) {
		ExpressionNode literalNode = null;

		if(Literal.isLiteral(complexLiteral)) { //simple literal
			Literal simpleLiteral = Literal.parseString(complexLiteral);
			literalNode = new ExpressionLeaf(isDisjunctive, simpleLiteral);
		} else {
			literalNode = constructExpressionTree(complexLiteral);
		}

		return literalNode;
	}

	private static void checkBadArguments(String infix) {
		if((infix == null ) || (infix.length() == 0)) {
			throw new IllegalArgumentException("The rule input expression should not be empty.");
		}
	}

	private static ArrayList<String> splitIntoLiteralsAndRelationship(String infix) {
		ArrayList<String> literals = new ArrayList<String>();

		infix = RuleFormatter.removeParentheses(infix);

		StringBuilder sb = new StringBuilder();
		int openBrackets = 0;
		boolean disjunctive = false;


		for(int i=0; i<infix.length(); i++) {
			char ch = infix.charAt(i);
			if(ch == '(') {
				openBrackets++;
			} else if(ch == ')') {
				openBrackets--;
			}

			if(openBrackets != 0) {
				sb.append(ch);
			} else {
				if(ch == ',') {
					disjunctive = true;
					continue;
				} else {
					sb.append(ch);
				}
				literals.add(sb.toString());
				sb = new StringBuilder();
			}
		}

		if(disjunctive) {//relationship (and, or)
			literals.add("||"); //or
		} else {
			literals.add("&&"); //and
		}

		return literals;
	}

	public List<String> getFieldsUsed() {
		return new LinkedList<String>(fieldsUsed);
	}

	public List<RuleLiteral> getLiteralsUsed() {
		return new LinkedList<RuleLiteral>(root.getLiteralsUsed());
	}

	public boolean matchesInstance(DataModel dataModel, int rowIndex) {
		return root.matchesInstance(dataModel, rowIndex);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if(root.isLeaf) {
			sb.append("(").append(root.toString()).append(")");
		} else {
			sb.append(root.toString());
		}

		return sb.toString();
	}


	private String identifer;
	private static int callCounter = 1;

	private ExpressionNode root;

	private Set<String> fieldsUsed = new TreeSet<String>();
}

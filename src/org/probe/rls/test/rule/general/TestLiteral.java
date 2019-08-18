package org.probe.rls.test.rule.general;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.probe.rls.models.rulemodel.general.Literal;
import org.probe.rls.util.RuleFormatter;

public class TestLiteral {

	@Test
	public void testParse(){
		String testRHS = "(Inflammation=no)"; 
		
		Literal ruleRHS = Literal.parseString(testRHS);
		assertTrue(testRHS.equalsIgnoreCase(ruleRHS.toString()));
		//System.out.println(ruleLHS);
	}
	
	@Test
	public void testParse2(){
		String testRHS = " (Inflammation=no) "; 
		
		Literal ruleRHS = Literal.parseString(testRHS);
		assertTrue(testRHS.trim().equalsIgnoreCase(ruleRHS.toString()));
		//System.out.println(ruleLHS);
	}
	
	@Test
	public void test3() {
		String label = "(A=0)";
		String variableInLiteral = RuleFormatter.removeParentheses(label).split("=")[0];
		
		System.out.println(label);
		System.out.println(variableInLiteral);
	}
}

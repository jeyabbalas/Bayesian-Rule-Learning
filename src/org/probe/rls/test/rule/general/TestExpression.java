package org.probe.rls.test.rule.general;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;
import org.probe.rls.data.FileType;
import org.probe.rls.models.rulemodel.general.Expression;


public class TestExpression {

	@Test
	public void testParse(){
		String testLHS = "((((Temp=High)(Nausea=yes)),((Temp=Low)(Nausea=no)),((Temp=Mid)(Nausea=no)))(Lumbar Pain=no)(Urine Pushing=yes))";
		
		Expression ruleLHS = Expression.parseString(testLHS);
		assertTrue(testLHS.equalsIgnoreCase(ruleLHS.toString()));
		//System.out.println(ruleLHS);
		
	}
	
	@Test
	public void testParse2(){
		String testLHS = "((Lumbar Pain=no))"; 
		
		Expression ruleLHS = Expression.parseString(testLHS);
		assertTrue(testLHS.equalsIgnoreCase(ruleLHS.toString()));
		//System.out.println(ruleLHS);
	}
	
	@Test
	public void testParse3(){
		String testLHS = "((Temp=Low,High))"; 
		
		Expression ruleLHS = Expression.parseString(testLHS);
		assertTrue(testLHS.equalsIgnoreCase(ruleLHS.toString()));
		//System.out.println(ruleLHS);
		
	}
	
	@Test
	public void testExpressionMatch(){
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW2, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);
		
		String testLHS = "((A1=Up))"; 
		Expression ruleLHS = Expression.parseString(testLHS);
		
		assertTrue(ruleLHS.matchesInstance(dataModel, 0));
		assertFalse(ruleLHS.matchesInstance(dataModel, 1));
		assertTrue(ruleLHS.matchesInstance(dataModel, 2));
		//System.out.println(ruleLHS);
	}
	
	@Test
	public void testExpressionMatch2(){
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW2, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);
		
		String testLHS = "((A1=Up)(A2=Down))"; 
		Expression ruleLHS = Expression.parseString(testLHS);
		
		assertTrue(ruleLHS.matchesInstance(dataModel, 0));
		assertFalse(ruleLHS.matchesInstance(dataModel, 1));
		assertFalse(ruleLHS.matchesInstance(dataModel, 2));
		//System.out.println(ruleLHS);
	}
	
	@Test
	public void testExpressionMatch3(){
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW2, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);
		
		String testLHS = "((A1=Up),(A2=Down))"; 
		Expression ruleLHS = Expression.parseString(testLHS);
		
		assertTrue(ruleLHS.matchesInstance(dataModel, 0));
		assertTrue(ruleLHS.matchesInstance(dataModel, 1));
		assertTrue(ruleLHS.matchesInstance(dataModel, 2));
		//System.out.println(ruleLHS);
	}
	
	
	private static String SEPARATOR = FileType.CSV.getSeparator();

	private static String HEADER = "#Index, @Class, A1, A2, A3";
	private static String ROW1 = "1,Pos,Up,Down,Up";
	private static String ROW2 = "2,Neg,Down,Down,Down";
	private static String ROW3 = "3,Pos,Up,Up,Up";
}

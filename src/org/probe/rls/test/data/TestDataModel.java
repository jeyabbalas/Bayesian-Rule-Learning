package org.probe.rls.test.data;

import java.util.List;

import org.junit.Test;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;
import org.probe.rls.data.FileType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDataModel {

	@Test
	public void testHeaderLoad() {
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		String actualInstanceColumnName = dataModel.getInstanceAttribute().getAttributeName();
		String actualLabelColumnName = dataModel.getClassAttribute().getAttributeName();

		assertEquals("Index", actualInstanceColumnName);
		assertEquals("Class", actualLabelColumnName);
	}

	@Test
	public void testRowLoad() {
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW2, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);

		assertEquals("Down", dataModel.getItemAt(0, 3));
		assertEquals("Up", dataModel.getItemAt(2, 2));
		assertEquals("Pos", dataModel.getClassLabelForRow(0));

		List<String> itemsInColumn = dataModel.getColumnItemsByIndex(2);
		assertEquals("Up", itemsInColumn.get(0));
		assertEquals("Down", itemsInColumn.get(1));
		assertEquals("Up", itemsInColumn.get(2));
	}

	@Test
	public void testContainsTrue() {
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW2, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);

		DataModel dataModel2 = new DefaultDataModel();
		dataModel2.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel2.parseAndAddRow(ROW1, SEPARATOR);
		dataModel2.parseAndAddRow(ROW2, SEPARATOR);
		dataModel2.parseAndAddRow(ROW3, SEPARATOR);
		
		assertTrue(!dataModel.doesNotContain(dataModel2));
	}
	
	@Test
	public void testContainsTrue2() {
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW2, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);

		DataModel dataModel2 = new DefaultDataModel();
		dataModel2.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel2.parseAndAddRow(ROW1, SEPARATOR);
		dataModel2.parseAndAddRow(ROW3, SEPARATOR);
		
		assertTrue(!dataModel.doesNotContain(dataModel2));
	}
	
	@Test
	public void testContainsFalse() {
		DataModel dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel.parseAndAddRow(ROW1, SEPARATOR);
		dataModel.parseAndAddRow(ROW3, SEPARATOR);

		DataModel dataModel2 = new DefaultDataModel();
		dataModel2.parseAndAddHeader(HEADER,SEPARATOR);

		dataModel2.parseAndAddRow(ROW2, SEPARATOR);
		
		assertTrue(dataModel.doesNotContain(dataModel2));
	}
	
	private static String SEPARATOR = FileType.CSV.getSeparator();

	private static String HEADER = "#Index, @Class, A1, A2, A3";
	private static String ROW1 = "1,Pos,Up,Down,Up";
	private static String ROW2 = "2,Neg,Down,Down,Down";
	private static String ROW3 = "3,Pos,Up,Up,Up";
}

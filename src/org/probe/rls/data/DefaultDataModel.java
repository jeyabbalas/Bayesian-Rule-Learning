package org.probe.rls.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DefaultDataModel implements DataModel {
	
	
	public DefaultDataModel(){
		
	}
	
	public DefaultDataModel(DataModel currentDataModel){
		super();
		this.instanceColumnIndex = currentDataModel.getInstanceAttribute().
				getAttributeIndex();
		this.classColumnIndex = currentDataModel.getClassAttribute().
				getAttributeIndex();
		
		this.attributes = new ArrayList<DataAttribute>();
		for(DataAttribute attribute: currentDataModel.getAttributes()) {
			this.attributes.add(new DataAttribute(attribute));
		}
		
		this.isTrainData = currentDataModel.isTrainData();
		
	}
	
	/**
	 * Load a CSV row in String format into the Data model
	 * 
	 * @param rowStr
	 */
	public void parseAndAddRow(String rowStr, String separator) {
		List<String> rowItems = new ArrayList<String>();

		String[] row = rowStr.split(separator);
		for (int i = 0; i < row.length; i++) {
			row[i] = cleanCell(row[i]);
			rowItems.add(row[i]);
			addItemToColumnList(i, row[i]);
			attributes.get(i).addAttributeValue(row[i]);
		}
		rows.add(rowItems);
	}
	
	/**
	 * Remove parenthesis which are boundary indicators for BRL rule parsing.
	 * Also removing "\'" introduced by Weka discretizers for more readable rules.
	 * 
	 * @return
	 */
	private String cleanCell(String cell) {
		return cell.replaceAll("[\\[\\]()\\\\']", "");
	}

	/**
	 * Load a CSV header in String format into the Data Model
	 * 
	 * @param headerStr
	 */
	public void parseAndAddHeader(String header, String separator) {
		clear();

		String[] columns = header.split(separator);
		for (int i = 0; i < columns.length; i++) {
			String columnName = columns[i].trim();
			if (checkPrefixAtIndex(columnName, i)) {
				columnName = columnName.substring(1, columnName.length());
			}

			DataAttribute attribute = new DataAttribute(i,columnName);
			if(isClassColumn(i))
				attribute.setIsClass(true);
			else if(isInstanceColumn(i))
				attribute.setIsInstance(true);
			
			attributes.add(attribute);
		}
	}

	/**
	 * Clear data model of all values
	 */
	public void clear() {
		attributes.clear();
		rows.clear();
		columns.clear();
	}

	/**
	 * Get the column header that contains the Instance's index number. This is
	 * the header that begins with the prefix '#'
	 * 
	 * @return
	 */
	public DataAttribute getInstanceAttribute() {
		return attributes.get(instanceColumnIndex);
	}

	/**
	 * Get the column header that contains the Label/Class for the instance.
	 * This is the header that begins with the prefix '@'
	 * 
	 * @return
	 */
	public DataAttribute getClassAttribute() {
		return attributes.get(classColumnIndex);
	}

	/**
	 * Return the Label/Class for the row
	 * 
	 * @param i
	 */
	public String getClassLabelForRow(int row) {
		return rows.get(row).get(classColumnIndex);
	}

	/**
	 * Returns item at [row][column]
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public String getItemAt(int row, int column) {
		return rows.get(row).get(column);
	}

	/**
	 * 
	 */
	public List<String> getColumnItemsByAttribute(DataAttribute attribute) {
		return columns.get(attribute.getAttributeIndex());
	}
	
	/**
	 * 
	 */
	public List<String> getColumnItemsByIndex(int index) {
		return columns.get(index);
	}

	/**
	 * 
	 */
	@Override
	public List<String> getClassColumn() {
		return columns.get(classColumnIndex);
	}

	/**
	 * 
	 */
	@Override
	public List<String> getClassLabels() {
		if (classLabelsSet.isEmpty()) {
			loadClassLabels();
		}
		return uniqueClassLabels;
	}
	
	@Override
	public List<String> getUniqueAttributeLabels(DataAttribute attribute) {
		List<String> uniqueAttributeLabelsAsList = new LinkedList<String>();
		HashSet<String> uniqueAttributeLabels = new HashSet<String>();
		
		List<String> attributeLabels = getColumnItemsByAttribute(attribute);
		for(String attributeLabel: attributeLabels) {
			uniqueAttributeLabels.add(attributeLabel);
		}
		
		uniqueAttributeLabelsAsList.addAll(uniqueAttributeLabels);
		
		return uniqueAttributeLabelsAsList;
	}
	
	@Override
	public Map<String, List<Integer>> getAttributeLabelIndices(DataAttribute attribute) {
		Map<String, List<Integer>> attributeLabelIndices = new TreeMap<String, List<Integer>>();
		
		List<String> attributeLabels = getColumnItemsByAttribute(attribute);
		for(int i = 0; i < attributeLabels.size(); i++) {
			String attributeLabel = attributeLabels.get(i);
			
			if(!attributeLabelIndices.containsKey(attributeLabel)) {
				List<Integer> indices = new LinkedList<Integer>();
				indices.add(i);
				attributeLabelIndices.put(attributeLabel, indices);
			} else {
				attributeLabelIndices.get(attributeLabel).add(i);
			}
		}
		
		return attributeLabelIndices;
	}

	/**
	 * 
	 */
	@Override
	public List<DataAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * 
	 */
	private void loadClassLabels() {
		List<String> classColumn = getClassColumn();
		for (String classLabel : classColumn) {
			classLabelsSet.add(classLabel);
		}

		uniqueClassLabels.clear();
		for (String classLabel : classLabelsSet) {
			uniqueClassLabels.add(classLabel);
		}
	}

	/**
	 * check if the column name begins with either a "#' or a "@. If so, set the
	 * appropriate index values to point to these.
	 * 
	 * @param columnName
	 * @param index
	 */
	private boolean checkPrefixAtIndex(String columnName, int index) {
		if (columnName.startsWith(INSTANCE_PREFIX)) {
			instanceColumnIndex = index;
			return true;
		} else if (columnName.startsWith(CLASS_PREFIX)) {
			classColumnIndex = index;
			return true;
		}
		return false;
	}
	
	private boolean isClassColumn(int index){
		return classColumnIndex == index;
	}
	
	private boolean isInstanceColumn(int index){
		return instanceColumnIndex == index;
	}

	/**
	 * add the row item to the appropriate column. This allows the user to
	 * access an items by their columns
	 * 
	 * @param rowIndex
	 * @param item
	 */
	private void addItemToColumnList(int rowIndex, String item) {
		if (rowIndex >= columns.size()) {
			columns.add(rowIndex, new LinkedList<String>());
		}

		columns.get(rowIndex).add(item);
	}

	@Override
	public void addItemsAsRow(List<String> items) {
		rows.add(items);
		
		for (int i = 0; i < items.size(); i++) {
			addItemToColumnList(i, items.get(i));
		}
	}

	@Override
	public void addItemsAsRow(String[] items) {
		List<String> itemsAsList = Arrays.asList(items);
		addItemsAsRow(itemsAsList);
	}

	@Override
	public List<String> getRow(int row) {
		return rows.get(row);
	}

	@Override
	public int size() {
		return rows.size();
	}

	@Override
	public boolean isTrainData() {
		return isTrainData == true;
	}

	@Override
	public void setDataSetAsTraining() {
		isTrainData = true;
	}

	@Override
	public boolean isValidationData() {
		return isTrainData == false;
	}

	@Override
	public void setDataSetAsValidation() {
		isTrainData = false;
	}

	@Override
	public boolean doesNotContain(DataModel otherDataModel) {
		// **** NOTE ***
		// This is only to be used for unit testing
		// It is a SLOW algorithm given the data structure
		// used for the container

		for (int x = 0; x < rows.size(); x++) {
			List<String> items = rows.get(x);

			for (int y = 0; y < otherDataModel.size(); y++) {
				List<String> otherItems = otherDataModel.getRow(y);

				if (areItemsEquivalent(items, otherItems))
					return false;
			}
		}

		return true;
	}

	@Override
	public int getNumRows() {
		return rows.size();
	}

	@Override
	public int getNumCols() {
		return attributes.size();
	}

	@Override
	public List<String> getHeader() {
		List<String> header = new LinkedList<String>();
		
		for(DataAttribute attribute : attributes){
			header.add(attribute.getAttributeName());
		}
		
		return header;
	}
	
	private String writeHeader() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		
		for(DataAttribute attribute : attributes) {
			if(!first) {
				sb.append("\t");
			}
			if(attribute.isInstance()) {
				sb.append(INSTANCE_PREFIX);
			} else if(attribute.isClass()) {
				sb.append(CLASS_PREFIX);
			}
			sb.append(attribute.getAttributeName());
			first = false;
		}
		
		return sb.toString();
	}

	@Override
	public void setAttributes(List<DataAttribute> header) {
		attributes = header;
		
		for(int index = 0; index < header.size(); index++){
			if(attributes.get(index).isClass()){
				classColumnIndex = index;
			}else if(attributes.get(index).isInstance()){
				instanceColumnIndex = index;
			}
		}
	}
	
	@Override
	public void setAllAttributesAsNominal() {
		for(DataAttribute attribute: this.getAttributes()) {
			attribute.setNominal(true);
		}
	}

	@Override
	public void print() {
		for (List<String> row : rows) {
			for (String column : row) {
				System.out.print(column + ",");
			}
			System.out.println();
		}
	}
	
	@Override
	public boolean hasRowIndex(int index) {
		if(index > 0 && index < rows.size())
			return true;
		else return false;
	}
	
	@Override
	public List<Integer> getAllValidIndexes() {
		if(validIndexes == null){
			initValidIndexes();
		}
		
		return validIndexes;
	}

	private List<Integer> initValidIndexes() {
		validIndexes = new LinkedList<Integer>();
		for(int index = 0; index < rows.size(); index++){
			validIndexes.add(index);
		}
		
		return validIndexes;
	}

	@Override
	public List<Double> getInstanceWeights() {
		return instanceWeights;
	}

	@Override
	public void setInstanceWeights(List<Double> instanceWeights) {
		this.instanceWeights = instanceWeights;
	}
	
	@Override
	public double getInstanceWeight(int rowIndex) {
		return this.instanceWeights.get(rowIndex);
	}

	@Override
	public double getSumOfInstanceWeights(List<Integer> rowIndices) {
		double sum = 0.0;
		
		for(int rowIndex: rowIndices) {
			sum += getInstanceWeight(rowIndex);
		}
		
		return sum;
	}

	private boolean areItemsEquivalent(List<String> items,
			List<String> otherItems) {
		for (int x = 0; x < items.size(); x++) {
			if (!items.get(x).equals(otherItems.get(x)))
				return false;
		}

		return true;
	}
	
	@Override
	public void saveDataModelToTSVFile(String fileName) throws IOException {
		FileWriter dataFw = new FileWriter(fileName, true);
		BufferedWriter dataBw = new BufferedWriter(dataFw);
		PrintWriter dataOut = new PrintWriter(dataBw);
		
		dataOut.append(writeHeader()).append("\n");
		for(int i=0; i<getNumRows(); i++) {
			boolean first = true;
			for(int j=0; j<getNumCols(); j++) {
				if(!first) {
					dataOut.append("\t");
				}
				dataOut.append(getItemAt(i,j));
				first = false;
			}
			dataOut.append("\n");
		}
		
		dataOut.close();
	}
	
	@Override
	public void saveDataModelToCSVFile(String fileName) throws IOException {
		FileWriter dataFw = new FileWriter(fileName, true);
		BufferedWriter dataBw = new BufferedWriter(dataFw);
		PrintWriter dataOut = new PrintWriter(dataBw);
		
		dataOut.append(writeHeader()).append("\n");
		for(int i=0; i<getNumRows(); i++) {
			boolean first = true;
			for(int j=0; j<getNumCols(); j++) {
				if(!first) {
					dataOut.append(",");
				}
				dataOut.append(getItemAt(i,j));
				first = false;
			}
			dataOut.append("\n");
		}
		
		dataOut.close();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(writeHeader());
		sb.append("\n");
		for(int i=0; i<getNumRows(); i++) {
			boolean first = true;
			for(int j=0; j<getNumCols(); j++) {
				if(!first) {
					sb.append("\t");
				}
				sb.append(getItemAt(i,j));
				first = false;
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	

	public static final String INSTANCE_PREFIX = "#";
	public static final String CLASS_PREFIX = "@";

	private int instanceColumnIndex = -1;
	private int classColumnIndex = -1;

	private List<DataAttribute> attributes = new ArrayList<DataAttribute>();
	private List<List<String>> rows = new LinkedList<List<String>>();
	private List<List<String>> columns = new LinkedList<List<String>>();

	private HashSet<String> classLabelsSet = new HashSet<String>();
	private List<String> uniqueClassLabels = new LinkedList<String>();

	private boolean isTrainData = true;
	
	private List<Integer> validIndexes = null;
	
	private List<Double> instanceWeights = new ArrayList<Double>();

}

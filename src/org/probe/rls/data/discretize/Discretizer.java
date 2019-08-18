package org.probe.rls.data.discretize;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.probe.rls.data.DataAttribute;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.DefaultDataModel;

public class Discretizer {

	public Discretizer(BinGenerator computer) {
		this.binGenerator = computer;
		this.discretizationScheme = new HashMap<DataAttribute, 
				List<String>>();
	}

	public DataModel discretize(DataModel rawModel) throws Exception {
		List<DataAttribute> attributes = rawModel.getAttributes();
		int numRows = rawModel.getNumRows();
		int numCols = rawModel.getNumCols();

		String[][] newDataMatrix = new String[numRows][numCols];

		for (DataAttribute attribute : attributes) {
			List<String> columnValues = rawModel.getColumnItemsByAttribute(attribute);

			if (attribute.isClass() || attribute.isInstance()) {
				discretizationScheme.put(attribute, null);
				map(newDataMatrix, attribute, columnValues);
				continue;
			}

			List<String> binsForColumn = binGenerator.generate(rawModel,
					attribute);
			discretizationScheme.put(attribute, binsForColumn);
			map(newDataMatrix, attribute, columnValues, binsForColumn);
			attribute.setNominal(true);
		}

		return generateDataModelFromDataMatrix(rawModel, newDataMatrix, numRows, numCols);
	}
	
	public DataModel discretize(DataModel rawModel, List<DataAttribute> attributesToDiscretize) throws Exception {
		List<DataAttribute> attributes = rawModel.getAttributes();
		int numRows = rawModel.getNumRows();
		int numCols = rawModel.getNumCols();

		String[][] newDataMatrix = new String[numRows][numCols];

		for (DataAttribute attribute : attributes){
			List<String> columnValues = rawModel.getColumnItemsByAttribute(attribute);

			if (attribute.isClass() || attribute.isInstance()){
				discretizationScheme.put(attribute, null);
				map(newDataMatrix, attribute, columnValues);
				continue;
			}
			
			if(!attributesToDiscretize.contains(attribute)){
				discretizationScheme.put(attribute, null);
				map(newDataMatrix, attribute, columnValues);
				continue;
			}

			List<String> binsForColumn = binGenerator.generate(rawModel,
					attribute);
			discretizationScheme.put(attribute, binsForColumn);
			map(newDataMatrix, attribute, columnValues, binsForColumn);
			attribute.setNominal(true);
		}

		return generateDataModelFromDataMatrix(rawModel, newDataMatrix, numRows, numCols);
	}
	
	public DataModel applyDiscretizationScheme(DataModel validationData){
		List<DataAttribute> attributes = validationData.getAttributes();
		int numRows = validationData.getNumRows();
		int numCols = validationData.getNumCols();
		
		String[][] newDataMatrix = new String[numRows][numCols];
		
		for(DataAttribute attribute : attributes){
			List<String> columnValues = validationData.getColumnItemsByAttribute(attribute);
			
			if (attribute.isClass() || attribute.isInstance()){
				map(newDataMatrix, attribute, columnValues);
				continue;
			}
			
			List<String> scheme = discretizationScheme.get(attribute);
			if(scheme == null){
				map(newDataMatrix, attribute, columnValues);
				continue;
			}
			else{
				map(newDataMatrix, attribute, columnValues, scheme);
			}
		}
		
		return generateDataModelFromDataMatrix(validationData, newDataMatrix, numRows, numCols);
	}

	private void map(String[][] newDataMatrix, DataAttribute attribute,
			List<String> columnValues) {
		int rowIndex = 0;
		for (String columnValue : columnValues) {
			newDataMatrix[rowIndex++][attribute.getAttributeIndex()] = columnValue;
		}
	}

	private void map(String[][] newDataMatrix, DataAttribute attribute,
			List<String> columnValues, List<String> binsForColumn) {
		int rowIndex = 0;
		List<Double> binsAsList = getListFromBins(binsForColumn);
		for (String columnValue : columnValues) {
			String columnValueAsBin = getBinForColumnValue(columnValue,
					binsForColumn, binsAsList);
			newDataMatrix[rowIndex++][attribute.getAttributeIndex()] = columnValueAsBin;
		}
		
		attribute.clearAttributeValues();
		for(String binValue: binsForColumn) {
			attribute.addAttributeValue(binValue);
		}
	}

	private String getBinForColumnValue(String columnValue,
			List<String> binsForColumn, List<Double> binsAsList) {
		int index = getIndexOfColumnValue(binsAsList, columnValue);

		assert (index >= 0 && index < binsForColumn.size());
		return binsForColumn.get(index);
	}

	private List<Double> getListFromBins(List<String> binsForColumn) {
		List<Double> binsAsList = new LinkedList<Double>();
		for (String bin : binsForColumn) {
			String range = bin.substring(1, bin.length() - 1);
			//String lowerBound = range.substring(0, range.indexOf(',')).trim();
			String upperBound = range.substring(range.indexOf(',') + 1,
					range.length()).trim();
			double value;
			if (isInf(upperBound)) {
				value = Double.MAX_VALUE;
			} else
				value = Double.parseDouble(upperBound);
			binsAsList.add(value);
		}
		return binsAsList;
	}

	private boolean isInf(String lowerBound) {
		return lowerBound.contains("Inf");
	}

	private int getIndexOfColumnValue(List<Double> binsAsList,
			String columnValue) {
		double columnAsDouble = Double.parseDouble(columnValue);
		int index = 0;
		for (Double binLowerBound : binsAsList) {
			if (binLowerBound > columnAsDouble)
				return index;
			index++;
		}
		return -1;
	}

	private DataModel generateDataModelFromDataMatrix(DataModel rawModel, String[][] newDataMatrix,
			int numRows, int numCols) {
		DataModel newDataModel = new DefaultDataModel();
		newDataModel.setAttributes(rawModel.getAttributes());
		
		for (int row = 0; row < numRows; row++) {
			newDataModel.addItemsAsRow(newDataMatrix[row]);
		}
		return newDataModel;
	}

	private final BinGenerator binGenerator;
	private Map<DataAttribute, List<String>> discretizationScheme;
}

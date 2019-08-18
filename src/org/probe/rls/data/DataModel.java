package org.probe.rls.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataModel {

	void parseAndAddHeader(String header, String separator);
	void parseAndAddRow(String rowStr, String separator);
	void addItemsAsRow(List<String> items);
	void addItemsAsRow(String[] items);
	
	void setAttributes(List<DataAttribute> header);
	
	void setAllAttributesAsNominal();

	DataAttribute getInstanceAttribute();
	DataAttribute getClassAttribute();
	String getClassLabelForRow(int row);
	List<String> getHeader();
	
	List<String> getRow(int row);
	List<Integer> getAllValidIndexes();
	boolean hasRowIndex(int index);
	
	List<String> getColumnItemsByAttribute(DataAttribute attribute);
	List<String> getColumnItemsByIndex(int columnIndex);
	String getItemAt(int row, int column);
	List<String> getClassColumn();
	List<String> getClassLabels();
	List<String> getUniqueAttributeLabels(DataAttribute attribute);
	Map<String, List<Integer>> getAttributeLabelIndices(DataAttribute attribute);
	
	List<DataAttribute> getAttributes();
	
	List<Double> getInstanceWeights();
	void setInstanceWeights(List<Double> instanceWeights);
	double getInstanceWeight(int rowIndex);
	double getSumOfInstanceWeights(List<Integer> rowIndices);
	
	int size();
	void clear();
	void print();
	
	boolean isTrainData();
	void setDataSetAsTraining(); 
	boolean isValidationData();
	void setDataSetAsValidation();
	
	boolean doesNotContain(DataModel otherDataModel);
	int getNumRows();
	int getNumCols();
	
	void saveDataModelToTSVFile(String fileName) throws IOException;
	void saveDataModelToCSVFile(String fileName) throws IOException;
	
}

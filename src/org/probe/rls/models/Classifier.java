package org.probe.rls.models;

import java.util.Map;

import org.probe.rls.data.DataModel;

public interface Classifier {

	public String classifyInstance(DataModel dataModel, int rowIndex);
	public Map<String, Double> getProbabilities(DataModel dataModel, int rowIndex);
	public Prediction predictInstance(DataModel dataModel, int rowIndex);
	public String getDescription();
	public double getModelScore();
	public int getNumRules();
	public int getNumVariables();
}

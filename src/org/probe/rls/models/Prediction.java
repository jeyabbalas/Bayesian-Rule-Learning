package org.probe.rls.models;

import java.util.Map;

public class Prediction {

	public Prediction(String predictedClass, Map<String, Double> predictedProbabilities) {
		this.setPredictedClass(predictedClass);
		this.setPredictedProbabilities(predictedProbabilities);
	}
	
	public String getPredictedClass() {
		return predictedClass;
	}
	public void setPredictedClass(String predictedClass) {
		this.predictedClass = predictedClass;
	}

	public Map<String, Double> getPredictedProbabilities() {
		return predictedProbabilities;
	}

	public void setPredictedProbabilities(Map<String, Double> predictedProbabilities) {
		this.predictedProbabilities = predictedProbabilities;
	}


	private String predictedClass;
	private Map<String, Double> predictedProbabilities;
}

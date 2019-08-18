package org.probe.rls.report;

import java.io.File;

import org.probe.rls.algo.evaluator.Evaluator;
import org.probe.rls.data.DataModel;
import org.probe.rls.models.Classifier;

public interface ReportManager {
	void saveTrainData(DataModel trainDataModel, String fileName);
	void saveTestData(DataModel testDataModel, String fileName);
	
	void writeClassifierToFile(Classifier classifier, String fileName);
	
	void writeEvaluationPerformanceToFile(Evaluator evaluator, String fileName);
}

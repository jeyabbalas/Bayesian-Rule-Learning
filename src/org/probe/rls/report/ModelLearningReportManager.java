package org.probe.rls.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.logging.Level;

import org.probe.rls.algo.evaluator.Evaluator;
import org.probe.rls.data.DataModel;
import org.probe.rls.data.log.RLFileLogger;
import org.probe.rls.data.log.RLLogger;
import org.probe.rls.models.Classifier;

public class ModelLearningReportManager implements ReportManager {

	public ModelLearningReportManager(String dirLocation) {
		this.dirLocation = dirLocation;
		
		File file = new File(dirLocation);
		file.mkdir();
	}
	
	public void saveTrainData(DataModel trainDataModel, String fileName) {
		String message = trainDataModel.toString();
		createNewFileInDirectory(fileName, message);
	}

	@Override
	public void saveTestData(DataModel testDataModel, String fileName) {
		String message = testDataModel.toString();
		createNewFileInDirectory(fileName, message);
	}

	@Override
	public void writeClassifierToFile(Classifier classifier, String fileName) {
		String message = classifier.toString();
		createNewFileInDirectory(fileName, message);
	}
	
	@Override
	public void writeEvaluationPerformanceToFile(Evaluator evaluator, String fileName) {
		String message = evaluator.toString();
		createNewFileInDirectory(fileName, message);
	}

	private void createNewFileInDirectory(String name, String message) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(dirLocation).append("//").append(name);
			File file = new File(sb.toString());

			PrintWriter pw = new PrintWriter(file);

			pw.write(message + "\n");
			pw.close();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
	}

	private void saveToDirectory(File file) {
		String fileName = file.getName();
		StringBuilder sb = new StringBuilder();
		sb.append(dirLocation).append("//").append(fileName);

		File newFile = new File(sb.toString());

		copyFile(file, newFile);
	}

	private void copyFile(File from, File to) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(from));
			PrintWriter pr = new PrintWriter(to);

			String line = "";
			while ((line = br.readLine()) != null) {
				pr.write(line);
				pr.write("\n");
			}

			pr.close();
			br.close();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
	}

	
	private final String dirLocation;
	private final RLLogger LOGGER = RLFileLogger.getLogger();
}

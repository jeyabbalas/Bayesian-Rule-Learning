package org.probe.rls.test.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.probe.rls.report.ModelLearningReportManager;
import org.probe.rls.report.ReportManager;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestReportManager {
	
	@Before
	public void setup() {
		
	}

	@Test
	public void testSaveFile() {
		// TODO
	}
	
	private String readTxtFromFile(File file){
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line = "";
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null){
				sb.append(line).append("\n");
			}
			br.close();
			
			return sb.toString().trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void writeTxtToFile(File file, String txt){
		try {
			PrintWriter pw = new PrintWriter(file);
			pw.write(txt);
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private final String DIR = "Test//testoutput";
	private ReportManager reportManager = new ModelLearningReportManager(DIR);
}

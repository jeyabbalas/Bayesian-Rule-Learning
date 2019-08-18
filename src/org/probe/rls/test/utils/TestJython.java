package org.probe.rls.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class TestJython {

	public static void main(String[] args) throws IOException {

		// set up the command and parameter
		// python ./python_scripts/prg.py 1,1,1,0,1,1,1,1,1,1,0,1,1,1,0,1,0,0,1,0,0,0,1,0,1 25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1
		String pythonScriptPath = "./python_scripts/prg.py";
		String[] cmd = new String[4];
		cmd[0] = "python";
		cmd[1] = pythonScriptPath;
		cmd[2] = "1,1,1,0,1,1,1,1,1,1,0,1,1,1,0,1,0,0,1,0,0,0,1,0,1"; 
		cmd[3] = "25,24,23,10,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1";

		// create runtime to execute external command
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd);

		// retrieve output from python script
		BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		Double auprg = 0.0;
		while((line = br.readLine()) != null) {
			 auprg = Double.parseDouble(line);
		}
		br.close();
		System.out.println(auprg);
	}
}

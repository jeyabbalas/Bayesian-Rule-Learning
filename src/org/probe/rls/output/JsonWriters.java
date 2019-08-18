package org.probe.rls.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class JsonWriters {

	
	public static void writeJsonFile(Object rlsObject, String jsonFilename) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();;
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFilename))) {
			bw.write(gson.toJson(rlsObject));
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

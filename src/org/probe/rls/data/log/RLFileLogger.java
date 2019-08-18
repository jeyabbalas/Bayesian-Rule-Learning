package org.probe.rls.data.log;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RLFileLogger implements RLLogger {
	
	private RLFileLogger(){
		LOGGER = Logger.getLogger(RLFileLogger.class.getName());
		
		initLogger();
	}
	
	public static RLFileLogger getLogger(){
		if(_instance == null){
			_instance = new RLFileLogger();
		}
		
		return _instance;
	}
	
	
	public void log(Level level,String message){
		LOGGER.log(level, message);
	}
	
	private void initLogger(){
		try {
			FileHandler fileHandler = new FileHandler("log.txt");
			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
			
			LOGGER.addHandler(fileHandler);	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static RLFileLogger _instance = null;
	private final Logger LOGGER;
}

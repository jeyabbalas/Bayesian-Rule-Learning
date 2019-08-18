package org.probe.rls.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.probe.rls.data.fold.FoldGenerator;

public class FileDataManager implements DataManager {

	@Override
	public DataModel getDataModel(){
		return dataModel;
	}

	@Override
	public List<List<DataModel>> createFolds(FoldGenerator foldGenerator) throws Exception {
		if(dataModel == null)
			return null;
		
		foldGenerator.generateFolds(dataModel);
		List<List<DataModel>> foldDataModels = new LinkedList<List<DataModel>>();
		for(int foldID=0; foldID<foldGenerator.numFolds(); foldID++) {
			foldDataModels.add(foldGenerator.getFold(foldID));
		}
		
		return foldDataModels;
	}

	public void loadFromFile(String fileName, String itemSeparator) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		dataModel = new DefaultDataModel();
		dataModel.parseAndAddHeader(br.readLine(), itemSeparator);

		String line = "";
		while ((line = br.readLine()) != null) {
			dataModel.parseAndAddRow(line, itemSeparator);
		}

		br.close();
	}
	
	private DataModel dataModel = null;
}

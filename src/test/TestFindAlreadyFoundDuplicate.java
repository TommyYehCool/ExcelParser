package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;

import com.dreamer.common.constant.ConfigSetting;

public class TestFindAlreadyFoundDuplicate {
    private ArrayList<String> writeToNewFile = new ArrayList<String>();
    
    private void start() {
	File fileExcludePattern = new File(ConfigSetting.ONLINE_ALREADY_FOUND_LIST_FILE_PATH);
	if (!fileExcludePattern.isFile()) {
	    fileExcludePattern = new File(ConfigSetting.TEST_ALREADY_FOUND_LIST_FILE_PATH);
	    if (!fileExcludePattern.isFile()) {
		return;
	    }
	}
	
	HashSet<String> allCompanyName = new HashSet<String>();
	
	ArrayList<Integer> duplicateIndex = new ArrayList<Integer>();

	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileExcludePattern), ConfigSetting.ALREADY_FOUND_LIST_ENCODING));

	    String line = null;
	    int lineIndex = 1;
	    
	    int duplicateFoundCounts = 0;
	    
	    while ((line = reader.readLine()) != null) {
		String companyNameNo = line.trim();
		if ("".equals(companyNameNo)) {
		    continue;
		}

		String[] splitByComma = companyNameNo.split(", ");
		String splitCompanyName = splitByComma[0];

		String companyName = splitCompanyName.substring(splitCompanyName.indexOf(":") + 1).trim();
		
		if (!allCompanyName.contains(companyName)) {
		    allCompanyName.add(companyName);
		    
		    writeToNewFile.add(line);
		}
		else {
		    duplicateIndex.add(lineIndex);

		    duplicateFoundCounts++;
		}
		lineIndex++;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    private void createNewFile() {
	RandomAccessFile newFileWriter = null;
	try {
	    newFileWriter = new RandomAccessFile(new File("C:/AlreadyFoundList.txt"), "rw");
	    for (String xxx : writeToNewFile) {
		newFileWriter.write(xxx.getBytes());
		newFileWriter.write(0x0D);
		newFileWriter.write(0x0A);
	    }
	} 
	catch (IOException e) {
	    e.printStackTrace();
	} 
	finally {
	    if (newFileWriter != null) {
		try {
		    newFileWriter.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    public static void main(String[] args) {
	TestFindAlreadyFoundDuplicate instance = new TestFindAlreadyFoundDuplicate();
	instance.start();
	instance.createNewFile();
    }
}

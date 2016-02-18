package com.dreamer.excelparser.db.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.util.AccessDbUtil;
import com.dreamer.excelparser.db.queue.PersonalProcessStatusQ;
import com.dreamer.excelparser.db.queue.PersonalToDbQ;
import com.dreamer.excelparser.vo.Personal;

public class PersonalToDbHandler extends Thread {
    // ------------- Logger -------------
    private Logger mLogger = Logger.getLogger(Log4jSetting.COMPANY_PERSONAL_PARSER);

    private PersonalToDbQ queue;

    private List<Personal> batchBuffer;

    private int batchProcessSize;

    private long totalPersonals;

    private long personalsProcessed;

    public PersonalToDbHandler() {
	setName(this.getClass().getSimpleName());
	queue = PersonalToDbQ.getInstance();
	batchBuffer = new ArrayList<Personal>();
	batchProcessSize = AccessDbUtil.getInstance().getBatchProcessSize();
    }

    public void setTotalPersonals(long totalPersonals) {
	this.totalPersonals = totalPersonals;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		List<Personal> personals = queue.getPersonals();

		int takenFromQSize = personals.size();

		if (takenFromQSize != 0) {
		    batchBuffer.addAll(personals);

		    if (batchBuffer.size() >= batchProcessSize) {
			Personal[] totalPersonalsToDb = batchBuffer.toArray(new Personal[0]);
			int totalSize = totalPersonalsToDb.length;

			if (totalSize == batchProcessSize) {
			    AccessDbUtil.getInstance().insertPersonals(totalPersonalsToDb);
			}
			else {
			    int arraySize = totalSize / batchProcessSize;
			    int remain = totalSize % batchProcessSize;
			    boolean addOne = (remain != 0);
			    if (addOne) {
				arraySize++;
			    }
			    Personal[][] personalsToDb = new Personal[arraySize][];

			    int from = 0;
			    int to = batchProcessSize;
			    for (int i = 0; i < arraySize; i++) {
				if (addOne && i == arraySize - 1) {
				    personalsToDb[i] = Arrays.copyOfRange(totalPersonalsToDb, from, totalPersonalsToDb.length);
				}
				else {
				    personalsToDb[i] = Arrays.copyOfRange(totalPersonalsToDb, from, to);
				    from += batchProcessSize;
				    to += batchProcessSize;
				}
				AccessDbUtil.getInstance().insertPersonals(personalsToDb[i]);
			    }
			}
			personalsProcessed += totalSize;
			batchBuffer.clear();
		    }
		}
		else {
		    if (batchBuffer.size() != 0) {
			Personal[] remainPersonalsToDb = batchBuffer.toArray(new Personal[0]);
			AccessDbUtil.getInstance().insertPersonals(remainPersonalsToDb);

			personalsProcessed += batchBuffer.size();
			batchBuffer.clear();
		    }
		}

		if (totalPersonals != 0 && personalsProcessed == totalPersonals) {
		    PersonalProcessStatusQ.getInstance().offer("個人資料處理成功，共 " + totalPersonals + " 筆");
		    break;
		}
	    }
	    catch (InterruptedException e) {
		mLogger.error("InterruptedException while taking Personals data from queue", e);
	    }
	}
    }
}
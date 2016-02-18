package com.dreamer.excelparser.db.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.util.AccessDbUtil;
import com.dreamer.excelparser.db.queue.LegalPersonProcessStatusQ;
import com.dreamer.excelparser.db.queue.LegalPersonToDbQ;
import com.dreamer.excelparser.vo.LegalPerson;

public class LegalPersonToDbHandler extends Thread {
    // ------------- Logger -------------
    private Logger mLogger = Logger.getLogger(Log4jSetting.COMPANY_PERSONAL_PARSER);

    private LegalPersonToDbQ queue;

    private List<LegalPerson> batchBuffer;

    private int batchProcessSize;

    private long totalLegalPersons;

    private long legalPersonsProcessed;

    public LegalPersonToDbHandler() {
	setName(this.getClass().getSimpleName());
	queue = LegalPersonToDbQ.getInstance();
	batchBuffer = new ArrayList<LegalPerson>();
	batchProcessSize = AccessDbUtil.getInstance().getBatchProcessSize();
    }

    public void setTotalLegalPersons(long totalLegalPersons) {
	this.totalLegalPersons = totalLegalPersons;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		List<LegalPerson> legalPersons = queue.getLegalPersons();

		int takenFromQSize = legalPersons.size();

		if (takenFromQSize != 0) {
		    batchBuffer.addAll(legalPersons);

		    if (batchBuffer.size() >= batchProcessSize) {
			LegalPerson[] totalLegalPersonsToDb = batchBuffer.toArray(new LegalPerson[0]);
			int totalSize = totalLegalPersonsToDb.length;

			if (totalSize == batchProcessSize) {
			    AccessDbUtil.getInstance().insertLegalPersons(totalLegalPersonsToDb);
			}
			else {
			    int arraySize = totalSize / batchProcessSize;
			    int remain = totalSize % batchProcessSize;
			    boolean addOne = (remain != 0);
			    if (addOne) {
				arraySize++;
			    }
			    LegalPerson[][] legalPersonsToDb = new LegalPerson[arraySize][];

			    int from = 0;
			    int to = batchProcessSize;
			    for (int i = 0; i < arraySize; i++) {
				if (addOne && i == arraySize - 1) {
				    legalPersonsToDb[i] = Arrays.copyOfRange(totalLegalPersonsToDb, from, totalLegalPersonsToDb.length);
				}
				else {
				    legalPersonsToDb[i] = Arrays.copyOfRange(totalLegalPersonsToDb, from, to);
				    from += batchProcessSize;
				    to += batchProcessSize;
				}
				AccessDbUtil.getInstance().insertLegalPersons(legalPersonsToDb[i]);
			    }
			}
			legalPersonsProcessed += totalSize;
			batchBuffer.clear();
		    }
		}
		else {
		    if (batchBuffer.size() > 0) {
			LegalPerson[] remainLegalPersonsToDb = batchBuffer.toArray(new LegalPerson[0]);
			AccessDbUtil.getInstance().insertLegalPersons(remainLegalPersonsToDb);

			legalPersonsProcessed += batchBuffer.size();
			batchBuffer.clear();
		    }
		}

		if (totalLegalPersons != 0 && legalPersonsProcessed == totalLegalPersons) {
		    LegalPersonProcessStatusQ.getInstance().offer("法人資料處理成功，共 " + totalLegalPersons + " 筆");
		    break;
		}
	    }
	    catch (InterruptedException e) {
		mLogger.error("InterruptedException while taking LegalPersons data from queue", e);
	    }
	}
    }
}

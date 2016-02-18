package com.dreamer.excelparser.db.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.util.AccessDbUtil;
import com.dreamer.excelparser.db.queue.OthersProcessStatusQ;
import com.dreamer.excelparser.db.queue.OthersToDbQ;
import com.dreamer.excelparser.vo.Others;

public class OthersToDbHandler extends Thread {
    // ------------- Logger -------------
    private Logger mLogger = Logger.getLogger(Log4jSetting.COMPANY_PERSONAL_PARSER);

    private OthersToDbQ queue;

    private List<Others> batchBuffer;

    private int batchProcessSize;

    private long totalOthers;

    private long othersProcessed;

    public OthersToDbHandler() {
	setName(this.getClass().getSimpleName());
	queue = OthersToDbQ.getInstance();
	batchBuffer = new ArrayList<Others>();
	batchProcessSize = AccessDbUtil.getInstance().getBatchProcessSize();
    }

    public void setTotalOthers(long totalOthers) {
	this.totalOthers = totalOthers;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		List<Others> others = queue.getOthers();

		int takenFromQSize = others.size();

		if (takenFromQSize != 0) {
		    batchBuffer.addAll(others);

		    if (batchBuffer.size() >= batchProcessSize) {
			Others[] totalOthersToDb = batchBuffer.toArray(new Others[0]);
			int totalSize = totalOthersToDb.length;

			if (totalSize == batchProcessSize) {
			    AccessDbUtil.getInstance().insertOthers(totalOthersToDb);
			}
			else {
			    int arraySize = totalSize / batchProcessSize;
			    int remain = totalSize % batchProcessSize;
			    boolean addOne = (remain != 0);
			    if (addOne) {
				arraySize++;
			    }
			    Others[][] othersToDb = new Others[arraySize][];

			    int from = 0;
			    int to = batchProcessSize;
			    for (int i = 0; i < arraySize; i++) {
				if (addOne && i == arraySize - 1) {
				    othersToDb[i] = Arrays.copyOfRange(totalOthersToDb, from, totalOthersToDb.length);
				}
				else {
				    othersToDb[i] = Arrays.copyOfRange(totalOthersToDb, from, to);
				    from += batchProcessSize;
				    to += batchProcessSize;
				}
				AccessDbUtil.getInstance().insertOthers(othersToDb[i]);
			    }
			}
			othersProcessed += totalSize;
			batchBuffer.clear();
		    }
		}
		else {
		    if (batchBuffer.size() != 0) {
			Others[] remainOthersToDb = batchBuffer.toArray(new Others[0]);
			AccessDbUtil.getInstance().insertOthers(remainOthersToDb);

			othersProcessed += batchBuffer.size();
			batchBuffer.clear();
		    }
		}

		if (totalOthers != 0 && othersProcessed == totalOthers) {
		    OthersProcessStatusQ.getInstance().offer("其他資料處理成功，共 " + totalOthers + " 筆");
		    break;
		}
	    }
	    catch (InterruptedException e) {
		mLogger.error("InterruptedException while taking Others data from queue", e);
	    }
	}
    }
}

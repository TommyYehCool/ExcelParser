package com.dreamer.excelparser.db.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.util.AccessDbUtil;
import com.dreamer.excelparser.db.queue.CompanyProcessStatusQ;
import com.dreamer.excelparser.db.queue.CompanyToDbQ;
import com.dreamer.excelparser.vo.Company;

public class CompanyToDbHandler extends Thread {
    // ------------- Logger -------------
    private Logger mLogger = Logger.getLogger(Log4jSetting.COMPANY_PERSONAL_PARSER);

    private CompanyToDbQ queue;

    private List<Company> batchBuffer;

    private int batchProcessSize;

    private long totalCompanies;

    private long companiesProcessed;

    public CompanyToDbHandler() {
	setName(this.getClass().getSimpleName());
	queue = CompanyToDbQ.getInstance();
	batchBuffer = new ArrayList<Company>();
	batchProcessSize = AccessDbUtil.getInstance().getBatchProcessSize();
    }

    public void setTotalCompanies(long totalCompanies) {
	this.totalCompanies = totalCompanies;
    }

    @Override
    public void run() {
	while (true) {
	    try {
		List<Company> companies = queue.getCompanys();

		int takenFromQSize = companies.size();

		if (takenFromQSize != 0) {
		    batchBuffer.addAll(companies);

		    if (batchBuffer.size() >= batchProcessSize) {
			Company[] totalCompaniesToDb = batchBuffer.toArray(new Company[0]);
			int totalSize = totalCompaniesToDb.length;

			if (totalSize == batchProcessSize) {
			    AccessDbUtil.getInstance().insertCompanies(totalCompaniesToDb);
			}
			else {
			    int arraySize = totalSize / batchProcessSize;
			    int remain = totalSize % batchProcessSize;
			    boolean addOne = (remain != 0);
			    if (addOne) {
				arraySize++;
			    }
			    Company[][] companiesToDb = new Company[arraySize][];

			    int from = 0;
			    int to = batchProcessSize;
			    for (int i = 0; i < arraySize; i++) {
				if (addOne && i == arraySize - 1) {
				    companiesToDb[i] = Arrays.copyOfRange(totalCompaniesToDb, from, totalCompaniesToDb.length);
				}
				else {
				    companiesToDb[i] = Arrays.copyOfRange(totalCompaniesToDb, from, to);
				    from += batchProcessSize;
				    to += batchProcessSize;
				}
				AccessDbUtil.getInstance().insertCompanies(companiesToDb[i]);
			    }
			}
			companiesProcessed += totalSize;
			batchBuffer.clear();
		    }
		}
		else {
		    if (batchBuffer.size() != 0) {
			Company[] remainCompaniesToDb = batchBuffer.toArray(new Company[0]);
			AccessDbUtil.getInstance().insertCompanies(remainCompaniesToDb);

			companiesProcessed += batchBuffer.size();
			batchBuffer.clear();
		    }
		}

		if (totalCompanies != 0 && companiesProcessed == totalCompanies) {
		    CompanyProcessStatusQ.getInstance().offer("公司資料處理成功，共 " + totalCompanies + " 筆");
		    break;
		}
	    }
	    catch (InterruptedException e) {
		mLogger.error("InterruptedException while taking Companys data from queue", e);
	    }
	}
    }
}

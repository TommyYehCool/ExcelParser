package com.dreamer.excelparser.db.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dreamer.excelparser.vo.Company;

public class CompanyToDbQ {
    private static CompanyToDbQ instance = new CompanyToDbQ();

    private BlockingQueue<Company> queue;

    private CompanyToDbQ() {
	queue = new LinkedBlockingQueue<Company>();
    }

    public static CompanyToDbQ getInstance() {
	return instance;
    }

    public void offer(Company company) {
	queue.offer(company);
    }

    public List<Company> getCompanys() throws InterruptedException {
	List<Company> results = new ArrayList<Company>();

	Company company = queue.poll(10, TimeUnit.SECONDS);
	if (company != null) {
	    results.add(company);

	    Company remainData = null;
	    while ((remainData = queue.poll()) != null) {
		results.add(remainData);
	    }
	}
	return results;
    }
}

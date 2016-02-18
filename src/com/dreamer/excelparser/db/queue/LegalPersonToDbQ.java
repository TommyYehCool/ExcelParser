package com.dreamer.excelparser.db.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dreamer.excelparser.vo.LegalPerson;

public class LegalPersonToDbQ {
    private static LegalPersonToDbQ instance = new LegalPersonToDbQ();

    private BlockingQueue<LegalPerson> queue;

    private LegalPersonToDbQ() {
	queue = new LinkedBlockingQueue<LegalPerson>();
    }

    public static LegalPersonToDbQ getInstance() {
	return instance;
    }

    public void offer(LegalPerson legalPerson) {
	queue.offer(legalPerson);
    }

    public List<LegalPerson> getLegalPersons() throws InterruptedException {
	List<LegalPerson> results = new ArrayList<LegalPerson>();

	LegalPerson legalPerson = queue.poll(10, TimeUnit.SECONDS);
	if (legalPerson != null) {
	    results.add(legalPerson);

	    LegalPerson remainData = null;
	    while ((remainData = queue.poll()) != null) {
		results.add(remainData);
	    }
	}
	return results;
    }
}

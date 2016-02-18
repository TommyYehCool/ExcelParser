package com.dreamer.excelparser.db.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dreamer.excelparser.vo.Personal;

public class PersonalToDbQ {
    private static PersonalToDbQ instance = new PersonalToDbQ();

    private BlockingQueue<Personal> queue;

    private PersonalToDbQ() {
	queue = new LinkedBlockingQueue<Personal>();
    }

    public static PersonalToDbQ getInstance() {
	return instance;
    }

    public void offer(Personal personal) {
	queue.offer(personal);
    }

    public List<Personal> getPersonals() throws InterruptedException {
	List<Personal> results = new ArrayList<Personal>();

	Personal personal = queue.poll(10, TimeUnit.SECONDS);
	if (personal != null) {
	    results.add(personal);

	    Personal remainData = null;
	    while ((remainData = queue.poll()) != null) {
		results.add(remainData);
	    }
	}
	return results;
    }
}

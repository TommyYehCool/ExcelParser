package com.dreamer.excelparser.db.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dreamer.excelparser.vo.Others;

public class OthersToDbQ {
    private static OthersToDbQ instance = new OthersToDbQ();

    private BlockingQueue<Others> queue;

    private OthersToDbQ() {
	queue = new LinkedBlockingQueue<Others>();
    }

    public static OthersToDbQ getInstance() {
	return instance;
    }

    public void offer(Others others) {
	queue.offer(others);
    }

    public List<Others> getOthers() throws InterruptedException {
	List<Others> results = new ArrayList<Others>();

	Others others = queue.poll(10, TimeUnit.SECONDS);
	if (others != null) {
	    results.add(others);

	    Others remainData = null;
	    while ((remainData = queue.poll()) != null) {
		results.add(remainData);
	    }
	}
	return results;
    }
}

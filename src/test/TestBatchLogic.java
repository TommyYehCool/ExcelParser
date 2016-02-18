package test;

public class TestBatchLogic {

    public static void main(String[] args) {
	int totalDatas = 14165;
	int expectExecuteBatchCnts = totalDatas % 1000 == 0 ? totalDatas / 1000 : (totalDatas / 1000) + 1;

	int executeBatchCnts = 0;

	int handledDataCnts = 0;
	for (int i = 0; i < 14165; i++) {
	    handledDataCnts++;

	    if (handledDataCnts % 1000 == 0) {
		executeBatchCnts++;
		System.out.println("處理了第 " + executeBatchCnts + " 次");
	    }
	    else if (executeBatchCnts == expectExecuteBatchCnts - 1) {
		executeBatchCnts++;
		System.out.println("處理了第 " + executeBatchCnts + " 次");
	    }
	}
    }
}

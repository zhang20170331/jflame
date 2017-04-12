package org.jflame.toolkit.keygenerate.serialnum;

/**
 * 限定时间内自增数.即指定时间内自增,超过时间后重新计数.
 * <p>
 * 同一实例线程安全 ，不同实例线程不安全
 * 
 * @author yucan.zhang
 */
public class PeriodIncreaseNum extends BaseIncreaseNum {

    private long period;

    /**
     * 构造函数.
     * 
     * @param periodMillis 限定时间.毫秒
     */
    public PeriodIncreaseNum(long periodMillis) {
        super();
        this.period = periodMillis;
    }

    /**
     * 构造函数
     * 
     * @param periodMillis 限定时间.毫秒
     * @param initNum 初始值
     */
    public PeriodIncreaseNum(int periodMillis, int initNum) {
        super(initNum);
        this.period = periodMillis;
    }

    /**
     * 下一个递增值
     * 
     * @return
     */
    @Override
    public long nextNum() {
        long curTime = System.currentTimeMillis();
        long next=sequence.getAndIncrement();
        if (curTime - lastTimestamp.get() >= period) {
            sequence.set(initSeq);
            lastTimestamp.set(curTime);
            // System.out.println("reset...");
        }
        return next;
    }

    // public static void main(String[] args) {
    // PeriodIncreaseNum increaseNum = new PeriodIncreaseNum(10000);
    // for (int i = 0; i < 100; i++) {
    // System.out.println(increaseNum.nextNum());
    // try {
    // Thread.sleep(50);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // }

}

package dev.andreasgeorgatos.nbc.data.prior;

public class PriorData {
    private Object classValue;
    private long count;

    public PriorData(Object className) {
        this.classValue = className;
        count = 1;
    }

    public void increaseCount() {
        this.count++;
    }

    public Object getClassValue() {
        return classValue;
    }

    public long getCount() {
        return count;
    }
}

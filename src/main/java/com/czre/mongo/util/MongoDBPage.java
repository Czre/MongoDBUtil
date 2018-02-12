package com.czre.mongo.util;

import java.util.ArrayList;

/**
 * Created by czre on 2018/2/7
 */
public class MongoDBPage {
    private int size;
    private int number;
    private int total;
    private int count;
    private ArrayList<Object> result;

    public MongoDBPage(int size, int number) {
        this.size = size;
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCount() {
        if (total % size == 0) {
            count = total / size;
        } else {
            count = total / size + 1;
        }
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Object> getResult() {
        return result;
    }

    public void setResult(ArrayList<Object> result) {
        this.result = result;
    }
}

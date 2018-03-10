package org.hugoandrade.euro2016.predictor.admin.processing;


import java.util.ArrayList;
import java.util.List;

public class ListContainer<T> {

    private List<T> mList;

    ListContainer() {
        mList = new ArrayList<>();
    }

    void add(T obj) {
        if (obj != null)
            mList.add(obj);
    }

    public List<T> getList() {
        return mList;
    }
}

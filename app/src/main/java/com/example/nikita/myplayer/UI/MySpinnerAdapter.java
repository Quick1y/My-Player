package com.example.nikita.myplayer.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nikita.myplayer.R;

import java.util.ArrayList;

/**
 * Created by nikita on 24.07.17.
 */

public class MySpinnerAdapter extends BaseAdapter {
    private static final String TAG = "MySpinnerAdapter";

    private String[] mItemList;
    private LayoutInflater mInflater;


    //принемает только листы объектов, реализующих интерфейс ISpinnerItem
    public MySpinnerAdapter(String[] items, LayoutInflater inflater){

        if(items == null){
            items = new String[0];
        }

        mItemList = items;
        mInflater = inflater;
    }

    @Override
    public int getCount() {
        /*
        * Длина - 1 используется для того, чтобы выводить на один элемент меньше. Это нужно, чтобы
        * после клика пользователя по элементу установить выбранным снова последний (невидимый).
        * Если этого не сделать, то повторный клик на выбранный элемент игнорируется.
        * */
        if (mItemList != null){
            return mItemList.length - 1;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        return mItemList[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = mInflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        ((TextView) view).setText(mItemList[i]);
        return view;
    }
}

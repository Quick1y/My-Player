package com.example.nikita.myplayer.Utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nikita.myplayer.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nikita on 11.08.17.
 */

public class FileManagerAdapter extends BaseAdapter {
    private static final String TAG = "FileManagerAdapter";

    public static final int SHOW_BACK = 1;
    public static final int SHOW_NOTHING = 2;

    private int mMaxNameLength;
    private int mShowFirst;

    private ArrayList<File> mItemList;
    private LayoutInflater mInflater;


    public FileManagerAdapter(ArrayList<File> list, LayoutInflater inflater, int showFirst, int maxNameLength){
        if(list == null){
            list = new ArrayList<>();
        }

        mMaxNameLength = maxNameLength;
        mItemList = list;
        mInflater = inflater;
        mShowFirst = showFirst;

        //добавляем пустой элемент для кнопки назад или для выбора другого ФМ
        if(mShowFirst == SHOW_BACK){
            mItemList.add(0, null);
        }
    }

    @Override
    public int getCount() {
        if (mItemList != null){
            return mItemList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //тут нужно делать View Holder
        view = mInflater.inflate(R.layout.file_manager_list_view, viewGroup, false);

        TextView name = (TextView) view.findViewById(R.id.fm_listview_track_name);
        ImageView image = (ImageView) view.findViewById(R.id.fm_listview_image);

        //устанавливаем кнопку назад или другой ФМ в первый элемент
        if(mItemList.get(i) == null){
            switch (mShowFirst) {
                case SHOW_BACK:
                    name.setText(R.string.fm_button_back);
                    image.setImageResource(R.drawable.ic_back);
                    break;

                default: Log.d(TAG, "Что-то пошло не так в getView. Вызван default");
            }

        } else {
            //устанавливаем имя
            String fileName = mItemList.get(i).getName();

            //Если больше определенной длины, то обрезаем имя
            if(fileName.length() > mMaxNameLength){
                fileName = fileName.substring(0, mMaxNameLength) + "...";
                Log.d(TAG, "name is shorted: " + fileName + "; max length" + mMaxNameLength);
            }
            name.setText(fileName);


            //Устанавливаем картинку
            if(mItemList.get(i).isDirectory()){
                image.setImageResource(R.drawable.ic_folder);
            } else {
                if(FileQualifier.isTrack(mItemList.get(i))){
                    image.setImageResource(R.drawable.ic_track);
                } else {
                    image.setImageResource(R.drawable.ic_file);
                }
            }
        }

        return view;
    }


}

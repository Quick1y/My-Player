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
    private static int mMaxNameLength;

    private ArrayList<File> mItemList;
    private LayoutInflater mInflater;


    public FileManagerAdapter(ArrayList<File> list, LayoutInflater inflater, boolean showBack, int maxNameLength){
        if(list == null){
            list = new ArrayList<>();
        }

        mMaxNameLength = maxNameLength;
        mItemList = list;
        mInflater = inflater;

        //если это корневая директория, то добавляем пустой элемент для кнопки назад
        if(showBack){
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

        //устанавливаем кнопку назад в первый элемент
        if(mItemList.get(i) == null){
            name.setText(R.string.fm_button_back);
            image.setImageResource(R.drawable.ic_file_back);
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

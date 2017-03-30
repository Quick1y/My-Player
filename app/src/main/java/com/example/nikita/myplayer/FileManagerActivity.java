package com.example.nikita.myplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class FileManagerActivity extends AppCompatActivity {
    private static final String TAG = "FileManagerActivity";
    private static final String SD_PATH = "/storage/7F2A-1905/Music";

    private ListView fileListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        //Установка темы
        setTheme(R.style.CommonTheme_orange);


        fileListView = (ListView) findViewById(R.id.file_manager_list_view);

        //устанавливает в listView все файлы в каталоге SD_PATH
        ArrayList<File> fileArray = listFilesWithSubFolders(new File(SD_PATH));
        ArrayAdapter<File> adapter =
                new ArrayAdapter<File>(this, android.R.layout.simple_expandable_list_item_1, fileArray);
        fileListView.setAdapter(adapter);

    }


    public ArrayList<File> listFilesWithSubFolders(File dir) {
        ArrayList<File> files = new ArrayList<File>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                files.addAll(listFilesWithSubFolders(file));
            else
                files.add(file);
        }
        return files;
    }

}

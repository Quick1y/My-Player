package com.example.nikita.myplayer.UI;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.R;
import com.example.nikita.myplayer.Utils.FileManagerAdapter;
import com.example.nikita.myplayer.Utils.FileQualifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileManagerActivity extends AppCompatActivity {
    private static final String TAG = "FileManagerActivity";
    private static final String STORAGE_PATH = "/storage";
    private static final String DEFAULT_PATH = "default";
    private static final String KEY_CURR_PATH = "FileManagerActivity.KEY_CURR_PATH";
    private static final String KEY_CHOSEN_DEVICE = "FileManagerActivity.KEY_CHOSEN_DEVICE";

    public static final int GET_PATH = 144;
    public static final int GET_PATH_OK = 180;
    public static final String KEY_STRING = "FileManagerActivity.KEY_STRING";
    public static final String KEY_WITH_INNER = "FileManagerActivity.KEY_WITH_INNER";

    private File mChosenDevice;
    private File mParentDir;
    private FloatingActionButton mFab;


    private ListView mFileListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        setTheme(R.style.CommonTheme_orange);


        mFileListView = (ListView) findViewById(R.id.file_manager_list_view);
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                File currentFile = (File) adapterView.getItemAtPosition(i);

                if(adapterView.getItemAtPosition(i) == null){ // Если нажат первый элемент и он пуст (Назад), то назад
                    if(mParentDir.equals(mChosenDevice)){ //если дальше отступать некуда
                        showFileList(DEFAULT_PATH);
                        hideFab();
                    } else {
                        showFileList(mParentDir.getParent());
                    }
                    return;
                }

                if (currentFile.isDirectory()) {// Если директория, то переходим в нее
                    showFileList(currentFile.getPath());
                } else if (currentFile.isFile()) {
                    if (FileQualifier.isTrack(currentFile)) { //Если это трек, то запускаем плеер (урезанный)
                        Intent intent = PlayerActivity.newIntent(getBaseContext(), currentFile.getPath());
                        startActivity(intent);
                    } else {
                        //файл не поддерживается
                        Toast.makeText(getBaseContext(), R.string.fm_toast_file_not_support, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fm_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportAlertDialog();
            }
        });

        if (savedInstanceState != null){
            String path = savedInstanceState.getString(KEY_CURR_PATH);
            String chosenDev = savedInstanceState.getString(KEY_CHOSEN_DEVICE);
            if(chosenDev != null){
                mChosenDevice = new File(chosenDev);
                showFab();
            }
            showFileList(path);
        } else {
            showFileList(DEFAULT_PATH); // предлагаем выбор устройств
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.filemanager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.filemanager_menu_ok :
                finish();
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        //если мы в корне, то выходим из ФМ; если нет, то в родительскую папку.
        if (mChosenDevice == null){
            super.onBackPressed();
        } else {
            if(mParentDir.equals(mChosenDevice)){
                showFileList(DEFAULT_PATH);
                hideFab();
            } else {
                showFileList(mParentDir.getParent());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        if(mChosenDevice == null){
            savedInstanceState.putString(KEY_CURR_PATH, DEFAULT_PATH);
        } else {
            savedInstanceState.putString(KEY_CURR_PATH, mParentDir.getPath());
            savedInstanceState.putString(KEY_CHOSEN_DEVICE, mChosenDevice.getPath());
        }
    }


    private void showFileList(String path){
        ArrayList<File> fileList;
        ActionBar abar = getSupportActionBar();
        FileManagerAdapter adapter;

        if(path.equals(DEFAULT_PATH)){
            mChosenDevice = null;
            if(abar != null){
                String st = getString(R.string.fm_subtitle_choose_device);
                abar.setSubtitle(st);
            }

            String def_path = Environment.getExternalStorageDirectory().getPath();
            fileList = new ArrayList<>();

            fileList.add(new File(def_path)); // добавляем внутренние хранилище
            fileList.addAll(getListFiles(new File(STORAGE_PATH), false)); // и не пустые из /storage

            int maxLength = getResources().getInteger(R.integer.fm_max_name_length);
            adapter = new FileManagerAdapter(fileList, getLayoutInflater(), false, maxLength);

        } else {
            Log.d(TAG, "Moving to " + path);
            mParentDir = new File(path);

            if(mChosenDevice == null){ // когда выбрали какое-то усройство
                mChosenDevice = mParentDir;
                showFab();
            }

            //устанавливаем путь до текущей дирректории в субтайтл
            if(abar != null) abar.setSubtitle(path);

            fileList = getListFiles(mParentDir, true); //получаем список файлов

            fileList = sortFileList(fileList); // сортируем: сначала папки, потом файлы
            int maxLength = getResources().getInteger(R.integer.fm_max_name_length);
            adapter = new FileManagerAdapter(fileList, getLayoutInflater(), true, maxLength);
        }

        mFileListView.setAdapter(adapter);
    }

    //Сортирует список файлов по алфавиту, устанавливая сначала папки, потом файлы
    private ArrayList<File> sortFileList(ArrayList<File> fileList){
        int lastDirIndex = 0;

        if(fileList == null){
            return null;
        }

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareToIgnoreCase(t1.getName());
            }
        });

        if(fileList == null){
            return null;
        }

        for(int i = 0; i < fileList.size(); i++){
            if(fileList.get(i).isDirectory()){
                fileList.add(lastDirIndex++, fileList.get(i));
                fileList.remove(i + 1);
            }
        }
        return fileList;
    }


    private ArrayList<File> getListFiles(File dir, boolean withEmpty) {
        if(dir == null || dir.listFiles() == null){
            Log.d(TAG, "Empty directory");
            return null;
        }

        ArrayList<File> files = new ArrayList<File>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                if(withEmpty){
                    files.add(file);
                } else {
                    if(file.listFiles() != null){
                        files.add(file);
                    }
                }
            }
            else
                files.add(file);
        }
        return files;
    }

    private void showImportAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final LinearLayout view = new LinearLayout(this);
        getLayoutInflater().inflate(R.layout.fm_alert_dialog, view);

        //Выводит сообщение в алерт
        String mess = getString(R.string.fm_alert_description);
        ((TextView) view.findViewById(R.id.fm_dialog_desc))
                .setText(String.format(mess, mParentDir.getName()));

        builder.setTitle(R.string.fm_alert_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean withInner = ((CheckBox)view.findViewById(R.id.fm_dialog_cb)).isChecked();

                        Intent intent = new Intent();
                        intent.putExtra(KEY_STRING, mParentDir.getPath());
                        intent.putExtra(KEY_WITH_INNER, withInner);

                        setResult(GET_PATH_OK, intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    private void showFab(){
        mFab.setScaleX(0);
        mFab.setScaleY(0);
        mFab.setVisibility(View.VISIBLE);
        mFab.animate().scaleX(1).scaleY(1).setDuration(100).start();
    }

    private void hideFab(){
        mFab.animate().scaleX(0).scaleY(0).setDuration(100).start();
        mFab.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {
                mFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

}

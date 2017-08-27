package com.example.nikita.myplayer.UI;

import android.animation.Animator;
import android.content.Context;
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
import com.example.nikita.myplayer.Utils.StorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class FileManagerActivity extends AppCompatActivity {
    private static final String TAG = "FileManagerActivity";
    private static final String STORAGE_PATH = "/storage";
    private static final String DEFAULT_PATH = "default";
    private static final String KEY_CURR_PATH = "FileManagerActivity.KEY_CURR_PATH";
    private static final String KEY_CHOSEN_DEVICE = "FileManagerActivity.KEY_CHOSEN_DEVICE";

    public static final int GET_PATH = 144;
    public static final int GET_PATH_DIR = 180;
    public static final int GET_PATH_FILE = 181;
    public static final String KEY_STRING = "FileManagerActivity.KEY_STRING";
    public static final String KEY_WITH_INNER = "FileManagerActivity.KEY_WITH_INNER";

    private File mChosenDevice;
    private File mParentDir;
    private FloatingActionButton mFab;


    private ListView mFileListView;

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, FileManagerActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        mFileListView = (ListView) findViewById(R.id.file_manager_list_view);
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                File currentFile = (File) adapterView.getItemAtPosition(i);

                if (adapterView.getItemAtPosition(i) == null) { // Если нажат первый элемент и он пуст

                    if (mParentDir.equals(mChosenDevice)) { //если дальше отступать некуда
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
                        showImportFileAlertDialog(currentFile);
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
                showImportDirAlertDialog();
            }
        });

        if (savedInstanceState != null) {
            String path = savedInstanceState.getString(KEY_CURR_PATH);
            String chosenDev = savedInstanceState.getString(KEY_CHOSEN_DEVICE);
            if (chosenDev != null) {
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
        switch (item.getItemId()) {
            case R.id.filemanager_menu_close:
                finish();
                return true;

            case R.id.filemanager_menu_another_fm:
                Toast.makeText(this, "Запускаем другой файловый менеджер", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //если мы в корне, то выходим из ФМ; если нет, то в родительскую папку.
        if (mChosenDevice == null) {
            super.onBackPressed();
        } else {
            if (mParentDir.equals(mChosenDevice)) {
                showFileList(DEFAULT_PATH);
                hideFab();
            } else {
                showFileList(mParentDir.getParent());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (mChosenDevice == null) {
            savedInstanceState.putString(KEY_CURR_PATH, DEFAULT_PATH);
        } else {
            savedInstanceState.putString(KEY_CURR_PATH, mParentDir.getPath());
            savedInstanceState.putString(KEY_CHOSEN_DEVICE, mChosenDevice.getPath());
        }
    }


    private void showFileList(String path) {
        ArrayList<File> fileList = new ArrayList<>();
        ActionBar abar = getSupportActionBar();
        FileManagerAdapter adapter;

        if (path.equals(DEFAULT_PATH)) {
            mChosenDevice = null;
            if (abar != null) {
                String st = getString(R.string.fm_subtitle_choose_device);
                abar.setSubtitle(st);
            }

            String def_path = Environment.getExternalStorageDirectory().getPath();

            fileList.add(new File(def_path)); // добавляем внутренние хранилище

            Set<File> files = StorageHelper.getSetFiles(new File(STORAGE_PATH), false); // и не пустые из /storage
            if (files != null) fileList.addAll(files);

            int maxLength = getResources().getInteger(R.integer.fm_max_name_length);
            adapter = new FileManagerAdapter(fileList, getLayoutInflater(),
                    FileManagerAdapter.SHOW_NOTHING, maxLength);

        } else {
            Log.d(TAG, "Moving to " + path);
            mParentDir = new File(path);

            if (mChosenDevice == null) { // когда выбрали какое-то усройство
                mChosenDevice = mParentDir;
                showFab();
            }

            //устанавливаем путь до текущей дирректории в субтайтл
            if (abar != null) {
                int lengthSubtitle = getResources().getInteger(R.integer.fm_max_length_subtitle);
                String subtitle = path;
                if (path.length() > lengthSubtitle){
                    subtitle = subtitle.substring(subtitle.length() - lengthSubtitle);
                    subtitle = "..." + subtitle;
                }
                abar.setSubtitle(subtitle);
                Log.d(TAG, "path: " + path.length() + "; subt: " + subtitle.length() + "; ls: " + lengthSubtitle);
            }

            Set<File> files = StorageHelper.getSetFiles(mParentDir, true); //получаем список файлов
            if (files != null) fileList.addAll(files);


            fileList = StorageHelper.sortFileList(fileList); // сортируем: сначала папки, потом файлы
            int maxLength = getResources().getInteger(R.integer.fm_max_name_length);
            adapter = new FileManagerAdapter(fileList, getLayoutInflater(),
                    FileManagerAdapter.SHOW_BACK, maxLength);
        }

        mFileListView.setAdapter(adapter);
    }


    private void showImportDirAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final LinearLayout view = new LinearLayout(this);
        getLayoutInflater().inflate(R.layout.fm_alert_dialog_dir, view);

        //Выводит сообщение в алерт
        String mess = getString(R.string.fm_alert_dir_description);
        ((TextView) view.findViewById(R.id.fm_dialog_desc))
                .setText(String.format(mess, mParentDir.getName()));

        builder.setTitle(R.string.fm_alert_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean withInner = ((CheckBox) view.findViewById(R.id.fm_dialog_cb)).isChecked();

                        Intent intent = new Intent();
                        intent.putExtra(KEY_STRING, mParentDir.getPath());
                        intent.putExtra(KEY_WITH_INNER, withInner);

                        setResult(GET_PATH_DIR, intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    private void showImportFileAlertDialog(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final LinearLayout view = new LinearLayout(this);
        getLayoutInflater().inflate(R.layout.fm_alert_dialog_file, view);

        //Выводит сообщение в алерт
        String mess = getString(R.string.fm_alert_file_description);
        ((TextView) view.findViewById(R.id.fm_dialog_desc))
                .setText(String.format(mess, file.getName()));

        builder.setTitle(R.string.fm_alert_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.putExtra(KEY_STRING, file.getPath());

                        setResult(GET_PATH_FILE, intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }



    private void showFab() {
        mFab.setScaleX(0);
        mFab.setScaleY(0);
        mFab.setVisibility(View.VISIBLE);
        mFab.animate().scaleX(1).scaleY(1).setDuration(100).start();
    }

    private void hideFab() {
        mFab.animate().scaleX(0).scaleY(0).setDuration(100).start();
        mFab.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

}

package com.example.nikita.myplayer.UI.Settings;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikita.myplayer.R;
import com.example.nikita.myplayer.UI.FileManagerActivity;
import com.example.nikita.myplayer.Utils.FileQualifier;
import com.example.nikita.myplayer.Utils.Importer;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by nikita on 27.08.17.
 */

public class SettingsImportFragment extends Fragment {
    private static final String TAG = "SettingsImportFragment";

    private RecyclerView mSourceRecycler;
    private TextView mListEmptyText;
    private FloatingActionButton mAddFAB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_import, container, false);

        mAddFAB = (FloatingActionButton) view.findViewById(R.id.fsi_fab_add);
        mAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = FileManagerActivity.getIntent(getActivity());
                startActivityForResult(intent, FileManagerActivity.GET_PATH);
            }
        });

        mSourceRecycler = (RecyclerView) view.findViewById(R.id.fsi_sources_list);
        mSourceRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        mListEmptyText = (TextView) view.findViewById(R.id.fsi_list_is_empty_text);

        updateList();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult, resultCode: " + requestCode);

        switch (requestCode) {
            case FileManagerActivity.GET_PATH: {
                if (resultCode == FileManagerActivity.GET_PATH_DIR) { //импорт директории
                    String path = data.getStringExtra(FileManagerActivity.KEY_STRING);
                    boolean withInner = data.getBooleanExtra(FileManagerActivity.KEY_WITH_INNER, false);

                    Importer.doImport(path, withInner, getActivity());

                    Toast.makeText(getActivity(), "Path = " + path + "; with inner = " + withInner, Toast.LENGTH_LONG).show();
                    updateList();
                }

                if (resultCode == FileManagerActivity.GET_PATH_FILE) {//импорт конкретного трека
                    String path = data.getStringExtra(FileManagerActivity.KEY_STRING);

                    Importer.doImport(path, false, getActivity());

                    Toast.makeText(getActivity(), "Single file. Path = " + path, Toast.LENGTH_LONG).show();
                    updateList();

                }
                break;
            }

            default:
                break;
        }
    }

    //updating sources list
    private void updateList() {
        SharedPreferences sp = getActivity().getSharedPreferences(
                getString(R.string.preferences),
                Context.MODE_PRIVATE);

        ArrayList<String> sources = new ArrayList<>();

        sources.add("/example1/example1/example1");
        sources.add("/example2/example2/example2/example2/example2/example2/ololololololololololololololololo");
        sources.add("/example3/example3/example3.mp3");

        if (sources.isEmpty()) {
            mListEmptyText.setVisibility(View.VISIBLE);
        } else {
            mListEmptyText.setVisibility(View.INVISIBLE);
            SourceListAdapter adapter = new SourceListAdapter(sources);
            mSourceRecycler.setAdapter(adapter);
            mSourceRecycler.refreshDrawableState();
        }
    }


    //Adapter & Holder для RecyclerView с источниками треков
    private class SourceViewHolder extends RecyclerView.ViewHolder {
        private String mSource;
        private TextView mSourceTextView;
        private ImageButton mDeleteButton;
        private ImageView mIconImageView;

        SourceViewHolder(View itemView) {
            super(itemView);
            mIconImageView = (ImageView) itemView.findViewById(R.id.sli_icon) ;
            mSourceTextView = (TextView) itemView.findViewById(R.id.sli_source);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.sli_delete);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Удаляем " + mSource, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void bind(String source) {
            mSource = source;
            mSourceTextView.setText(source);

            if(FileQualifier.isTrack(new File(mSource))){
                mIconImageView.setImageResource(R.drawable.ic_track);
            } else {
                mIconImageView.setImageResource(R.drawable.ic_folder);
            }

        }
    }

    private class SourceListAdapter extends RecyclerView.Adapter<SourceViewHolder> {
        private ArrayList<String> mSources;

        public SourceListAdapter(ArrayList<String> sources) {
            mSources = sources;
        }

        @Override
        public SourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.sources_list_item, parent, false);

            return new SourceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SourceViewHolder holder, int position) {
            holder.bind(mSources.get(position));
        }

        @Override
        public int getItemCount() {
            return mSources == null ? 0 : mSources.size();
        }
    }

}

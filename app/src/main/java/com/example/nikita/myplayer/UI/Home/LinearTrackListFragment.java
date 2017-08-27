package com.example.nikita.myplayer.UI.Home;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.nikita.myplayer.Database.TrackDataBase;
import com.example.nikita.myplayer.Model.Track;
import com.example.nikita.myplayer.R;
import com.example.nikita.myplayer.UI.Settings.SettingsActivity;
import com.example.nikita.myplayer.Utils.MySpinnerAdapter;

import java.util.Locale;


public class LinearTrackListFragment extends Fragment {
    private String TAG = "LinearTrackListFragment";

    private RecyclerView mTrackRecyclerView;
    private TrackListAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_linear_track_list, container, false);

        ((FrameLayout) view.findViewById(R.id.fltl_current_track_fl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity) getActivity()).showPlayer(null);
            }
        });

        mTrackRecyclerView = (RecyclerView) view.findViewById(R.id.fltl_recycler_view);
        mTrackRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //ActionBar menu
        final String[] data = getResources().getStringArray(R.array.ha_menu_item);

        MySpinnerAdapter adapter = new MySpinnerAdapter(data, getActivity().getLayoutInflater());
        final Spinner spinner = (Spinner) view.findViewById(R.id.ab_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstStart = true;
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!firstStart){ // ignore first click (from system)

                    switch (i) {
                        case 0:
                            Intent intent = SettingsActivity.getIntent(getActivity());
                            startActivity(intent);
                    }

                } else {
                    firstStart = false;
                }
                ((TextView) view).setText("");
                spinner.setSelection(data.length-1); // это костыль, чтобы спиннер работал как меню.
                                        // Подробнее в getCount() MySpinnerAdapter
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // /ActionBar menu



        updateUI();

        return view;
    }


    private void updateUI(){
        Track[] tracks = TrackDataBase.getAll(getActivity());
        mAdapter = new TrackListAdapter(tracks);
        mTrackRecyclerView.setAdapter(mAdapter);
    }



    private class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTrackName;
        private TextView mArtist;
        private TextView mDuration;
        private ImageView mAlbumImg;

        private Track mTrack;


        public TrackViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTrackName = (TextView) itemView.findViewById(R.id.fltl_list_trackname);
            mArtist = (TextView) itemView.findViewById(R.id.fltl_list_artist);
            mDuration = (TextView) itemView.findViewById(R.id.fltl_list_duration);
            mAlbumImg = (ImageView) itemView.findViewById(R.id.fltl_list_album_image);
        }

        public void bind(Track track){
            mTrack = track;
            mTrackName.setText(track.getName());
            mArtist.setText(track.getArtist());
            mDuration.setText(millisecToHour(track.getDuration()));

        }

        //Перевод миллисекунд в формат hh:mm:ss
        private String millisecToHour(int millisec) {
            return String.format(Locale.US,
                    "%02d:%02d:%02d",
                    millisec / (3600 * 1000),
                    (millisec / (60 * 1000)) % 60,
                    (millisec / (1000) % 60));
        }

        @Override
        public void onClick(View view) {
            ((HomeActivity) getActivity()).showPlayer(mTrack);
        }
    }


    private class TrackListAdapter extends RecyclerView.Adapter<TrackViewHolder>{
        private Track[] mTracks;

        public TrackListAdapter(Track[] tracks){
            mTracks = tracks;
        }

        @Override
        public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.fltl_list_view, parent, false);

            return new TrackViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TrackViewHolder holder, int position) {
            Track track = mTracks[position];
            holder.bind(track);
        }

        @Override
        public int getItemCount() {
            return mTracks == null? 0 : mTracks.length ;
        }
    }


}

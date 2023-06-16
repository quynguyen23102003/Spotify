package com.workshops.onlinemusicplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Logger;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.workshops.onlinemusicplayer.R;
import com.workshops.onlinemusicplayer.adapter.MusicAdapter;
import com.workshops.onlinemusicplayer.adapter.NewsAdapter;
import com.workshops.onlinemusicplayer.adapter.PhotoAdapter;
import com.workshops.onlinemusicplayer.adapter.SongsAdapter;
import com.workshops.onlinemusicplayer.adapter.TrendAdapter;
import com.workshops.onlinemusicplayer.helper.ListHelper;
import com.workshops.onlinemusicplayer.helper.MusicLibraryHelper;
import com.workshops.onlinemusicplayer.listener.CallBackDatabase;
import com.workshops.onlinemusicplayer.listener.MusicSelectListener;
import com.workshops.onlinemusicplayer.listener.PlayListListener;
import com.workshops.onlinemusicplayer.model.Music;
import com.workshops.onlinemusicplayer.model.Photo;
import com.workshops.onlinemusicplayer.model.Singer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener, PlayListListener {

    public static MusicSelectListener listener;
    private final List<Music> musicList = new ArrayList<>();
    private final List<Music> musicSearchList = new ArrayList<>();
    private SongsAdapter songAdapter;
    private final List<Music> unChangedList = new ArrayList<>();

    private MaterialToolbar toolbar;
    private SearchView searchView;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private PhotoAdapter photoAdapter;
    private List<Photo> mListPhoto;
    private Timer mTimer;
    private RecyclerView recyclerViewSearch;
    private RecyclerView recyclerViewSong;

    public static ArrayList<Singer> singers = new ArrayList<>();

    public HomeFragment() {
    }

    public static HomeFragment newInstance(MusicSelectListener selectListener) {
        HomeFragment.listener = selectListener;
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchView = view.findViewById(R.id.search_view);
        searchView.clearFocus();
        setUpSearchView();

        recyclerViewSearch = view.findViewById(R.id.recycler_view_search);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerViewSong = view.findViewById(R.id.songs_layout);
        recyclerViewSong.setLayoutManager(new LinearLayoutManager(getActivity()));
        songAdapter = new SongsAdapter(listener, this, musicList);

        //image slider
        mListPhoto = getListPhoto();
        viewPager = view.findViewById(R.id.viewPager);
        circleIndicator = view.findViewById(R.id.circleIndicator);

        autoImageSlider();

        photoAdapter = new PhotoAdapter(getActivity(),mListPhoto);

        viewPager.setAdapter(photoAdapter);

        circleIndicator.setViewPager(viewPager);
        photoAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());
        // end image slider

        MusicLibraryHelper.fetchMusicLibrary(view.getContext(), new CallBackDatabase() {
            @Override
            public List<Music> onCallback(List<Music> result) {
                unChangedList.addAll(result);
                musicList.clear();
                musicList.addAll(unChangedList);
                recyclerViewSong.setAdapter(songAdapter);
                return result;
            }
        });

        TabLayout tabLayout = view.findViewById(R.id.tab_layout_fragment);
        ViewPager2 viewPager2 = view.findViewById(R.id.view_pager);
        viewPager2.setSaveEnabled(false);
        TrendAdapter demoAdapter = new TrendAdapter(getChildFragmentManager(), getLifecycle());
        viewPager2.setAdapter(demoAdapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("News");
                    break;
                case 1:
                    tab.setText("Video");
                    break;
                case 2:
                    tab.setText("Artists");
                    break;
                case 3:
                    tab.setText("Podcast");
                    break;
            }
        }).attach();

        return view;
    }

    private void autoImageSlider() {
        if(mListPhoto == null || mListPhoto.isEmpty() || viewPager == null){
            return;
        }
        if(mTimer == null){
            mTimer = new Timer();
        }

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int currentItem = viewPager.getCurrentItem();
                        int totalItem = mListPhoto.size() - 1;
                        if(currentItem < totalItem){
                            currentItem ++;
                            viewPager.setCurrentItem(currentItem);
                        }else{
                            viewPager.setCurrentItem(0);
                        }

                    }
                });

            }
        },1000, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private List<Photo> getListPhoto() {
        List<Photo> list = new ArrayList<>();
        list.add(new Photo(R.drawable.anh_1));
        list.add(new Photo(R.drawable.anh_2));
        list.add(new Photo(R.drawable.anh_3));
        list.add(new Photo(R.drawable.anh_4));
        list.add(new Photo(R.drawable.anh_5));
        list.add(new Photo(R.drawable.anh_6));

        return list;
    }

    private void setUpSearchView() {
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        updateAdapter(ListHelper.searchMusicByName(unChangedList, query.toLowerCase()));
        recyclerViewSearch.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        updateAdapter(ListHelper.searchMusicByName(unChangedList, newText.toLowerCase()));
        if (newText.isEmpty()) recyclerViewSearch.setVisibility(View.GONE);
        else recyclerViewSearch.setVisibility(View.VISIBLE);
        return true;
    }

    private void updateAdapter(List<Music> list) {
        musicSearchList.clear();
        musicSearchList.addAll(list);
        songAdapter = new SongsAdapter(listener, this, musicSearchList);
        recyclerViewSearch.setAdapter(songAdapter);
    }

    @Override
    public void option(Context context, Music music) {
    }

}




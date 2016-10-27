package com.test.schedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabFragment extends Fragment {
    private static final String TAG = "schedule:log";

    public TabFragment() {
    }

    public static TabFragment newInstance(SchoolDay data) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putParcelable("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SchoolDay schoolDay = getArguments().getParcelable("data");

        if (schoolDay == null) {
            Log.d(TAG, "onCreateView: schoolday == null");
            return inflater.inflate(R.layout.fragment_empty, container, false);
        } else if (schoolDay.isEmpty()) {
            Log.d(TAG, schoolDay.getDate().toString() + "is empty");
            return inflater.inflate(R.layout.fragment_empty, container, false);
        } else {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(layoutManager);
            Log.d(TAG, "onCreateView: Creating LessonAdapter for tab " + schoolDay.getDate().toString());
            RecyclerView.Adapter adapter = new LessonAdapter(schoolDay);
            recyclerView.setAdapter(adapter);
            return rootView;
        }
    }
}
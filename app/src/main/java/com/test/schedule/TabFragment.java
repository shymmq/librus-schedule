package com.test.schedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TabFragment extends Fragment {
    public TabFragment() {
    }

    private static final String TAG = "schedule:log";

    public static TabFragment newInstance(SchoolDay data) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putParcelable("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LessonAdapter adapter;
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        SchoolDay schoolDay = getArguments().getParcelable("data");

        ListView list = (ListView) rootView.findViewById(R.id.listView);
        adapter = new LessonAdapter(schoolDay, getActivity());
        list.setAdapter(adapter);
        return rootView;
    }
}
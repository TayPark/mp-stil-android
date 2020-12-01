package com.example.mp_final_stil;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TabMy extends ListFragment {
    private ArrayList<String> _items = new ArrayList<>();
    CheckBoxAdapter adapter;

    public TabMy() {
        // Required empty public constructor
    }

    public TabMy(JSONArray items) {
        try {
            for (int i = 0; i < items.length(); i++) {
                this._items.add(items.get(i).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static TabMy newInstance(JSONArray items) {
        TabMy fragment = new TabMy(items);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new CheckBoxAdapter();
        setListAdapter(adapter);

        for (int i = 0; i < _items.size(); i++) {
            adapter.addItem(_items.get(i));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
package com.example.mp_final_stil;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TabShare extends ListFragment {
    private ArrayList<JSONObject> items = new ArrayList<>();
    ListViewAdapter adapter = new ListViewAdapter();
    TextView titleTextView, summaryTextView, contentTextView, emailHolder, idHolder;

    Button bookmarkBtn;
    Button closeBtn;
    Button deleteBtn;

    public TabShare() {
        // Required empty public constructor
    }

    public void updateItem(JSONArray data) {
        this.items.clear();
        JSONObject temp;
        for (int i = 0; i < data.length(); i++) {
            try {
                if (data.getJSONObject(i) != null) {
                    temp = data.getJSONObject(i);
                    this.items.add(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * This method called when Android render viewpager.
     * Also it calls ListViewAdapter.getView() method.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter = new ListViewAdapter();

        for (JSONObject data : items) {
            try {
                adapter.addItem(data.getString("title"),
                        data.getString("summary"),
                        data.getString("content"),
                        data.getString("author"),
                        data.getString("_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setListAdapter(this.adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /* Content opener */
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        /* Close all opened content */
        for (int i = 0; i <l.getChildCount(); i++) {
            View each = l.getChildAt(i);
            each.findViewById(R.id.closeBtn).performClick();
        }

        /* View/Button getters */
        titleTextView = v.findViewById(R.id.titleTextView);
        summaryTextView = v.findViewById(R.id.summaryTextView);
        contentTextView = v.findViewById(R.id.contentTextView);
        emailHolder = v.findViewById(R.id.itemAuthor);
        idHolder = v.findViewById(R.id.itemId);

        bookmarkBtn = v.findViewById(R.id.bookmarkBtn);
        closeBtn = v.findViewById(R.id.closeBtn);
        deleteBtn = v.findViewById(R.id.deleteBtn);

        /* Open content */
        summaryTextView.setVisibility(View.GONE);
        contentTextView.setVisibility(View.VISIBLE);

        SharedPreferences userAccount = getContext().getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
        if (userAccount.getString("email", null).equals(emailHolder.getText())) {
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setBackgroundColor(Color.parseColor("#e65a1e"));
        }

        bookmarkBtn.setVisibility(View.VISIBLE);
        closeBtn.setVisibility(View.VISIBLE);

        bookmarkBtn.setBackgroundColor(Color.parseColor("#ffb347"));
        closeBtn.setBackgroundColor(Color.parseColor("#21b2de"));

        /* Button click listeners */
        closeBtn.setOnClickListener(contentCloser());
        bookmarkBtn.setOnClickListener(addBookmark());
        deleteBtn.setOnClickListener(deleteContent());

        adapter.notifyDataSetChanged();
    }

    /**
     * Close opened content.
     *
     * @return OnClickListener to close content
     */
    private View.OnClickListener contentCloser() {
        return v -> {
            titleTextView.setVisibility(View.VISIBLE);
            summaryTextView.setVisibility(View.VISIBLE);

            contentTextView.setVisibility(View.GONE);
            bookmarkBtn.setVisibility(View.GONE);
            closeBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        };
    }

    /**
     * Bookmark this STIL.
     *
     * @return OnClickListener to add bookmark
     */
    private View.OnClickListener addBookmark() {
        return v -> {
            Context context = getContext();
            ViewGroup parent = (ViewGroup) v.getParent();
            TextView idHolder = null;
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChildAt(i).getId() == R.id.itemId) {
                    idHolder = (TextView) parent.getChildAt(i);
                    break;
                }
            }
            String itemId = idHolder.getText().toString();
            SharedPreferences userAccount = getActivity().getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("email", userAccount.getString("email", null));
                requestBody.put("stilId", itemId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "http://15.164.96.105:8080/stil/bookmark";
            queue.add(new JsonObjectRequest(Request.Method.POST, url, requestBody, response -> {
                try {
                    if (response.getString("ok").equals("1")) {
                        Toast.makeText(context, "Bookmarked successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> {
                Toast.makeText(context, "Already bookmarked", Toast.LENGTH_SHORT).show();
            }));
        };
    }

    /**
     * Delete content if it is user's
     *
     * @return OnClickListener to delete bookmark
     */
    private View.OnClickListener deleteContent() {
        return v -> {
            Context context = getContext();
            ViewGroup parent = (ViewGroup) v.getParent();

            TextView idHolder = null;
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChildAt(i).getId() == R.id.itemId) {
                    idHolder = (TextView) parent.getChildAt(i);
                    break;
                }
            }
            JSONObject requestBody = new JSONObject();
            String itemId = idHolder.getText().toString();
            try {
                requestBody.put("stilId", itemId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue queue = Volley.newRequestQueue(context);

            String url = "http://15.164.96.105:8080/stil/delete";
            queue.add(new JsonObjectRequest(Request.Method.POST, url, requestBody, response -> {
                try {
                    if (response.getString("ok").equals("1")) {
                        Log.d("Tab-share-delete", response.toString(2));
                        JSONArray dataFromServer = response.getJSONArray("data");
                        ((TabShare) Main.viewpagerAdapter.getItem(1)).updateItem(dataFromServer);
                        Main.viewpagerAdapter.notifyDataSetChanged();
                        Main.setIcons();
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                Toast.makeText(context, "Delete failed(Already processed)", Toast.LENGTH_SHORT).show();
            }));

            queue.start();
        };
    }
}
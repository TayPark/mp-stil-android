package com.example.mp_final_stil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CheckBoxAdapter extends BaseAdapter {
    private ArrayList<CheckBoxItem> checkBoxItems = new ArrayList<>();

    public CheckBoxAdapter() {
    }

    @Override
    public int getCount() {
        return checkBoxItems.size();
    }

    @Override
    public Object getItem(int position) {
        return checkBoxItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /* 뷰가 렌더링 될 때 호출되는 코드 */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        /* 뷰를 inflate 하기 위해 서비스와 인플레이터를 호출 함 */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.checkbox_item, parent, false);
        }

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        CheckBoxItem checkBoxItem = checkBoxItems.get(position);
        checkBox.setText(checkBoxItem.getContent());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checkBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    checkBox.setBackgroundColor(Color.parseColor("#c4c4c4"));
                } else {
                    checkBox.setPaintFlags(0);
                    checkBox.setBackgroundColor(Color.WHITE);
                }
            }
        });

        checkBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Select action");

                /* 삭제 이벤트리스너 */
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder editDialog = new AlertDialog.Builder(context);
                        editDialog.setTitle("Delete TIL");

                        /* 체크박스 뷰어 홀더 getter */
                        ViewGroup viewGroup = (ViewGroup) checkBox.getParent().getParent();

                        editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                JSONObject requestBody = new JSONObject();
                                SharedPreferences userAccount = context.getSharedPreferences("autoLogin", Context.MODE_PRIVATE);

                                try {
                                    requestBody.put("author", userAccount.getString("email", null));
                                    JSONArray contentArray = new JSONArray();
                                    ViewGroup tempViewGroup;
                                    CheckBox eachItem;

                                    /* 삭제할 체크박스 외에 데이터를 request body에 주입 */
                                    for (int idx = 0; idx < viewGroup.getChildCount(); idx++) {
                                        tempViewGroup = (ViewGroup) viewGroup.getChildAt(idx);
                                        eachItem = (CheckBox) tempViewGroup.getChildAt(0);

                                        if (!eachItem.getText().toString().equals(checkBox.getText().toString())) {
                                            contentArray.put(eachItem.getText().toString());
                                        }
                                    }
                                    requestBody.put("content", contentArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                /* 데이터 수정 요청 */
                                RequestQueue queue = Volley.newRequestQueue(context);
                                String url = "http://15.164.96.105:8080/stil/all";
                                JsonObjectRequest deployRequest = new JsonObjectRequest(Request.Method.PATCH, url, requestBody, response -> {
                                    try {
                                        if (response.getString("ok").equals("1")) {
                                            Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("DEBUG/Main-deploy", response.toString());
                                }, error -> {
                                    Toast.makeText(context, "Unexpected error: " + error, Toast.LENGTH_SHORT).show();
                                    Log.e("DEBUG/Main-deploy", error.toString());
                                });
                                queue.add(deployRequest);
                            }
                        });
                        editDialog.setNegativeButton("Cancel", null);
                        editDialog.show();
                    }
                    /* On Edit action */
                }).setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder editDialog = new AlertDialog.Builder(context);
                        editDialog.setTitle("Edit TIL");
                        EditText editText = new EditText(context);
                        editText.setText(checkBox.getText());
                        editDialog.setView(editText);

                        /* 체크박스 뷰어 홀더 getter */
                        ViewGroup viewGroup = (ViewGroup) checkBox.getParent().getParent();

                        editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkBox.setText(editText.getText().toString());

                                JSONObject requestBody = new JSONObject();
                                SharedPreferences userAccount = context.getSharedPreferences("autoLogin", Context.MODE_PRIVATE);

                                try {
                                    requestBody.put("author", userAccount.getString("email", null));
                                    JSONArray contentArray = new JSONArray();
                                    /* 변경값을 포함한 체크박스 전체를 가져와서 업데이트 */
                                    for (int idx = 0; idx < viewGroup.getChildCount(); idx++) {
                                        ViewGroup temp = (ViewGroup) viewGroup.getChildAt(idx);
                                        CheckBox item = (CheckBox) temp.getChildAt(0);
                                        contentArray.put(item.getText().toString());
                                    }
                                    requestBody.put("content", contentArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                /* 수정 요청 */
                                RequestQueue queue = Volley.newRequestQueue(context);
                                String url = "http://15.164.96.105:8080/stil/all";
                                JsonObjectRequest deployRequest = new JsonObjectRequest(Request.Method.PATCH, url, requestBody, response -> {
                                    try {
                                        if (response.getString("ok").equals("1")) {
                                            Toast.makeText(context, "Edited.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.d("DEBUG/Main-deploy", response.toString());
                                }, error -> {
                                    Toast.makeText(context, "Unexpected error: " + error, Toast.LENGTH_SHORT).show();
                                    Log.e("DEBUG/Main-deploy", error.toString());
                                });
                                queue.add(deployRequest);
                            }
                        });
                        editDialog.setNegativeButton("Cancel", null);
                        editDialog.show();
                    }
                });
                dialog.show();
                return true;
            }
        });

        return convertView;
    }

    public void addItem(String content) {
        CheckBoxItem item = new CheckBoxItem(content);

        checkBoxItems.add(item);
    }

}

package com.dfsc.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dfsc.R;

public class CheckFreeService extends AppCompatActivity {

    Toolbar toolbar;
    TextView filterError, toolbar_text;
    Spinner sa_mobile;
    private ProgressDialog dialog;
    protected AppPreferences appPrefs;
    String filter_sel, cust_id, menu_name, typeee, veh_reg_no;
    Button submit;
    TextInputLayout custId_input_layout, kms_input_layout, type_input_layout, veh_reg_no_input_layout;
    EditText et_custId, et_kms, et_type, et_veh_reg_no;
    ImageView flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_free_service);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        filter_sel = "";
        cust_id = "";
        menu_name = "";
        typeee = "";
        veh_reg_no = "";

        if (getIntent().getStringExtra("menu_name") != null) {
            menu_name = getIntent().getStringExtra("menu_name");
        } else {
            menu_name = "";
        }

        et_custId = (EditText) findViewById(R.id.et_custId);
        et_veh_reg_no = (EditText) findViewById(R.id.et_veh_reg_no);

        if (getIntent().getStringExtra("cust_id") != null) {
            cust_id = getIntent().getStringExtra("cust_id");
            et_custId.setEnabled(false);
        } else {
            cust_id = "";
            et_custId.setEnabled(true);
        }
        et_custId.setText(cust_id);

        if (getIntent().getStringExtra("veh_reg_no") != null) {
            veh_reg_no = getIntent().getStringExtra("veh_reg_no");
            et_veh_reg_no.setEnabled(false);
        } else {
            veh_reg_no = "";
            et_veh_reg_no.setEnabled(true);
        }
        et_veh_reg_no.setText(veh_reg_no);

        dialog = new ProgressDialog(CheckFreeService.this);
        appPrefs = new AppPreferences(CheckFreeService.this);

        flag = (ImageView) findViewById(R.id.flag);
        Picasso.with(CheckFreeService.this).load(appPrefs.getFlag()).into(flag);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.check_free_page_head));

        toolbar_text = (TextView) findViewById(R.id.toolbar_text);
        toolbar_text.setText(getResources().getString(R.string.check_free_page_head));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        filterError = (TextView) findViewById(R.id.filterError);
        filterError.setText(getResources().getString(R.string.validate_mobile_no));

        custId_input_layout = (TextInputLayout) findViewById(R.id.custId_input_layout);
        kms_input_layout = (TextInputLayout) findViewById(R.id.kms_input_layout);
        type_input_layout = (TextInputLayout) findViewById(R.id.type_input_layout);
        veh_reg_no_input_layout = (TextInputLayout) findViewById(R.id.veh_reg_no_input_layout);

        et_kms = (EditText) findViewById(R.id.et_kms);
        et_type = (EditText) findViewById(R.id.et_type);

        kms_input_layout.setHint(getResources().getString(R.string.kms));
        type_input_layout.setHint(getResources().getString(R.string.service_type));
        custId_input_layout.setHint(getResources().getString(R.string.cust_idd));
        veh_reg_no_input_layout.setHint(getResources().getString(R.string.veh_no));

        submit = (Button) findViewById(R.id.submit);
        submit.setText(getResources().getString(R.string.submit));

        GradientDrawable bgShape = (GradientDrawable) submit.getBackground();
        bgShape.setColor(Color.parseColor("#003763"));

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utilities.hideSoftKeyboard(CheckFreeService.this);

                if (appPrefs.getCountry().matches("india")) {
                    if (!validateCustomerId()) {
                        return;
                    }

                    if (!validateType()) {
                        return;
                    }

                } else {
                    if (!validateVehRegNo()) {
                        return;
                    }
                }

                if (!selectedMobile()) {
                    return;
                }

                if (!validateKms()) {
                    return;
                }


                if (Utilities.checkNetworkConnection(getApplicationContext())) {
                    dialog.setMessage(getResources().getString(R.string.please_wait));
                    dialog.setCancelable(false);
                    dialog.show();
                    check();
                    //startActivity(new Intent(Login.this, Drawer.class));
                    //finish();

                } else {
                    Toast.makeText(CheckFreeService.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        sa_mobile = (Spinner) findViewById(R.id.sa_mobile);
        sa_mobile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Mobile selected = (Mobile) arg0.getAdapter().getItem(arg2);
                filter_sel = selected.mobile_no;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (appPrefs.getCountry().matches("india")) {
            custId_input_layout.setVisibility(View.VISIBLE);
            type_input_layout.setVisibility(View.VISIBLE);
            veh_reg_no_input_layout.setVisibility(View.GONE);
            veh_reg_no = "";
        } else {
            custId_input_layout.setVisibility(View.GONE);
            type_input_layout.setVisibility(View.GONE);
            typeee = "";
            cust_id = "";
            veh_reg_no_input_layout.setVisibility(View.VISIBLE);

        }

        dialog.setMessage(getResources().getString(R.string.please_wait));
        dialog.setCancelable(false);
        dialog.show();
        getSAMobile();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean selectedMobile() {
        if (filter_sel.matches("")) {
            filterError.setVisibility(View.VISIBLE);
            return false;
        } else {
            filterError.setVisibility(View.GONE);
        }
        return true;
    }

    private boolean validateCustomerId() {
        if (et_custId.getText().toString().trim().isEmpty()) {
            custId_input_layout.setError(getResources().getString(R.string.validate_cust_id));
            return false;
        } else {
            custId_input_layout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateKms() {
        if (et_kms.getText().toString().trim().isEmpty()) {
            kms_input_layout.setError(getResources().getString(R.string.validate_km));
            return false;
        } else {
            kms_input_layout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateType() {
        if (et_type.getText().toString().trim().isEmpty()) {
            type_input_layout.setError(getResources().getString(R.string.validate_service_type));
            return false;
        } else {
            type_input_layout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateVehRegNo() {
        if (et_veh_reg_no.getText().toString().trim().isEmpty()) {
            veh_reg_no_input_layout.setError(getResources().getString(R.string.validate_vechicle_no));
            return false;
        } else {
            veh_reg_no_input_layout.setErrorEnabled(false);
        }

        return true;
    }


    private void getSAMobile() {
        String JSON_URL = appPrefs.getURL() + "User/service_man_list";
        RequestQueue queue = Volley.newRequestQueue(CheckFreeService.this);

        StringRequest req = new StringRequest(Request.Method.POST, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray arr = obj.getJSONArray("employee_dtl");

                            final ArrayList<Mobile> list = new ArrayList<Mobile>();

                            if (arr.length() == 1) {
                                list.add(new Mobile(arr.getJSONObject(0).getString("firstname") + " " + arr.getJSONObject(0).getString("lastname")
                                        , arr.getJSONObject(0).getString("mobile_no"),
                                        arr.getJSONObject(0).getString("user_code")  + " (" + arr.getJSONObject(0).getString("mobile_no") + ")"));
                                //sa_mobile.setSelection();
                                sa_mobile.setEnabled(false);
                            } else {
                                list.add(new Mobile("Select mobile number", "", getResources().getString(R.string.select_mobile_number)));
                                for (int i = 0; i < arr.length(); i++) {
                                    list.add(new Mobile(arr.getJSONObject(i).getString("firstname") + " " + arr.getJSONObject(i).getString("lastname")
                                            , arr.getJSONObject(i).getString("mobile_no"),
                                            arr.getJSONObject(i).getString("user_code") + " (" + arr.getJSONObject(i).getString("mobile_no") + ")"));
                                }
                            }


                            ArrayAdapter<Mobile> adapter = new ArrayAdapter<Mobile>(CheckFreeService.this,
                                    R.layout.list_item, R.id.name, list);
                            sa_mobile.setAdapter(adapter);
                            dialog.dismiss();

                            //Toast.makeText(CheckFreeService.this, obj.getString("message"), Toast.LENGTH_SHORT).show();


                        } catch (Throwable t) {
                            dialog.dismiss();
                            Log.e("REsponse", "Could not parse malformed JSON: \"" + response + "\"");
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(Login.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Toast.makeText(CheckFreeService.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public byte[] getBody() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("country", appPrefs.getCountry());
                params.put("group", appPrefs.getGroup());
                params.put("mobile_no", appPrefs.getMobile());
                params.put("user_id", appPrefs.getUserId());
                params.put("menu_name", menu_name);
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "bajaj:indian@1361";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(req);

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void check() {
        typeee = et_type.getText().toString().trim();

        String JSON_URL = appPrefs.getURL() + "Transaction/checkCoupon";
        RequestQueue queue = Volley.newRequestQueue(CheckFreeService.this);

        StringRequest req = new StringRequest(Request.Method.POST, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").matches("true")) {
                                //Toast.makeText(CheckFreeService.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(CheckFreeService.this, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(Html.fromHtml(getResources().getString(R.string.free_service)));
                                builder.setMessage(obj.getString("message"));
                                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        Intent i = new Intent(CheckFreeService.this, Drawer.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                    }
                                });
                                //builder.setNegativeButton("Cancel", null);
                                builder.show();
                            } else {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(CheckFreeService.this, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(Html.fromHtml(getResources().getString(R.string.free_service)));
                                builder.setMessage(obj.getString("message"));
                                builder.setPositiveButton(getResources().getString(R.string.ok), null);
                                //builder.setNegativeButton("Cancel", null);
                                builder.show();
                                //Toast.makeText(CheckFreeService.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }

                            //Toast.makeText(CheckFreeService.this, obj.getString("message"), Toast.LENGTH_SHORT).show();


                        } catch (Throwable t) {
                            Log.e("REsponse", "Could not parse malformed JSON: \"" + response + "\"");
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(Login.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Toast.makeText(CheckFreeService.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public byte[] getBody() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("country", appPrefs.getCountry());
                params.put("customer_id", et_custId.getText().toString().trim());
                params.put("km", et_kms.getText().toString().trim());
                params.put("mobile_no", filter_sel);
                params.put("service_type", typeee);

                if (!(appPrefs.getCountry().matches("india"))) {
                    params.put("veh_reg_no", et_veh_reg_no.getText().toString().trim());
                }

                Log.e("params", new JSONObject(params).toString());
                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "bajaj:indian@1361";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(req);

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class Mobile {
        public String name;
        public String mobile_no;
        public String comb;

        public Mobile(String name, String mobile_no, String comb) {
            this.name = name;
            this.mobile_no = mobile_no;
            this.comb = comb;
        }

        @Override
        public String toString() {
            return comb;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

}

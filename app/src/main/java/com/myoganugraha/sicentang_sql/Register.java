package com.myoganugraha.sicentang_sql;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.myoganugraha.sicentang_sql.R;
import com.myoganugraha.sicentang_sql.app.AppConfig;
import com.myoganugraha.sicentang_sql.app.AppController;
import com.myoganugraha.sicentang_sql.helper.SessionManager;
import com.myoganugraha.sicentang_sql.helper.SQLiteHandler;



public class Register extends AppCompatActivity {

    private LinearLayout linearLayout;
    private AnimationDrawable animationDrawable;
    private EditText inputFullName, inputEmail, inputPassword, inputPassword1;
    private Button btnRegister, btnLinkToLogin, image ;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private static final String TAG = Register.class.getSimpleName();
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutReg);
        animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(4000);
        animationDrawable.setExitFadeDuration(2000);

        inputFullName = (EditText) findViewById(R.id.namaReg);
        inputEmail = (EditText) findViewById(R.id.emailReg);
        inputPassword = (EditText) findViewById(R.id.passwordReg);
        inputPassword1 = (EditText) findViewById(R.id.passwordReg1);
        btnRegister = (Button) findViewById(R.id.btnReg);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLogin);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(Register.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String password1 = inputPassword1.getText().toString().trim();

                if (name.isEmpty() && email.isEmpty() && password.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(linearLayout,"Isi Kolom Yang Tersedia!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else if (!password.equals(password1)){
                    Toast.makeText(getApplicationContext(),
                            "Password Tidak Sama!", Toast.LENGTH_LONG)
                            .show();
                }
                else if(password.length() < 8){
                    Toast.makeText(getApplicationContext(),
                            "Minimal Password 8 Karakter!", Toast.LENGTH_LONG)
                            .show();
                }
                else if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !password1.isEmpty()){
                    registerUser(name, email, password);

                }

                try  {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {

                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        Login.class);
                startActivity(i);
                finish();
            }
        });
    }
    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, uid, created_at);

                        Toast.makeText(getApplicationContext(), "Register Sukses, Silahkan Login", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                Register.this,
                                Login.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    protected void onResume() {
        super.onResume();

        if (animationDrawable != null && !animationDrawable.isRunning()) {
            // start the animation
            animationDrawable.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning()) {
            // stop the animation
            animationDrawable.stop();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan 2 Kali Untuk Keluar", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}

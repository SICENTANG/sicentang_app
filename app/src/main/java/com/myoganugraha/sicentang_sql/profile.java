package com.myoganugraha.sicentang_sql;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.myoganugraha.sicentang_sql.helper.SQLiteHandler;
import com.myoganugraha.sicentang_sql.helper.SessionManager;

import java.util.HashMap;


public class profile extends Fragment implements View.OnClickListener {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtJoin;
    private Button btnLogout;

    private SQLiteHandler db;
    private SessionManager session;

    public static profile newInstance() {
       profile fragment = new profile();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());

        // session manager
        session = new SessionManager(getActivity().getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }











    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(getActivity(), Login.class);
        startActivity(intent);
        getActivity().finish();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_profile, container, false);

        txtName = (TextView) rootview.findViewById(R.id.name);
        txtEmail = (TextView) rootview.findViewById(R.id.email);
        txtJoin = (TextView) rootview.findViewById(R.id.joinDate);
        btnLogout = (Button) rootview.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");
        String created_at = user.get("created_at");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);
        txtJoin.setText("member since : " + created_at);

        return rootview;
    }

    public void onClick(View v){
            logoutUser();

    }


}
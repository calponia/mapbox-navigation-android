package com.bosch.calponia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bosch.calponia.REST;
import com.bosch.calponia.Request;
import com.mapbox.services.android.navigation.testapp.R;

import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOGTAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    protected EditText txtUsername;
    protected EditText txtPassword;
    protected Button btnLogin;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        txtUsername = findViewById(R.id.txt_username);
        txtPassword = findViewById(R.id.txt_password);
        btnLogin = findViewById(R.id.btn_login);
        
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick (View v) {
        Log.d(LOGTAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        btnLogin.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        JSONObject data = new JSONObject();
        try {
            data.put("email", txtUsername.getText().toString());
            data.put("password", txtPassword.getText().toString());
        } catch (Exception e) {}

        // Login to calponia
        REST.Call(new Request.Params(REST.LOGIN, "POST", data) {
            @Override
            void onPostExecute (JSONObject data) {
                try {
                    if (REST.IsError(data)) {
                        onLoginFailed();
                        return;
                    }
                    REST.setAccessToken(data.getJSONObject("response"));
                    onLoginSuccess();
                } catch (Exception e) {
                    Log.i(LOGTAG, e.getMessage());
                } finally {
                    progressDialog.dismiss();
                }
            }
        });
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_SIGNUP) {
//            if (resultCode == RESULT_OK) {
//                // GoTo scanner
//                startActivity(new Intent(this, ScannerActivity.class));
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        btnLogin.setEnabled(true);

        // GoTo scanner
        startActivity(new Intent(this, ScannerActivity.class));
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtUsername.setError("enter a valid email address");
            valid = false;
        } else {
            txtUsername.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            txtPassword.setError("needs at least 4 characters");
            valid = false;
        } else {
            txtPassword.setError(null);
        }

        return valid;
    }
}

package com.example.tpb_projekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.User;
import com.example.tpb_projekt.services.AuthenticatedUser;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LogIn extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }
    public void onLogInClick(View v){
        TextView username = findViewById(R.id.loginUsername);
        TextView lozinka = findViewById(R.id.loginPassword);
        String korime = username.getText().toString();
        String loz = lozinka.getText().toString();
        Intent i = new Intent(this, Main.class);
        OkHttpClient okhttpclient = new OkHttpClient();
        Request request = new Request.Builder().url("http://192.168.178.62:5000/getUser/"+korime).build();
        okhttpclient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
            @Override
            public void onResponse(
                    @NotNull Call call,
                    @NotNull Response response)
                    throws IOException {
                String jsonData = response.body().string();
                String data = parser.SingleStringClear(jsonData);
                User user = objectMapper.readValue(data, User.class);
                if(user != null) {
                    Boolean pr = Objects.equals(user.lozinka, loz);
                    if (pr) {
                        AuthenticatedUser.loggedIn = user;
                        startActivity(i);
                        LogIn.this.finish();
                    } else {
                        LogIn.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LogIn.this, "Lozinka nije ispravna.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                else{
                    LogIn.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LogIn.this, "Korisničko ime ne postoji.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
    public void goToRegistration(View v){
        Intent i = new Intent(this, Registration.class);
        startActivity(i);
        this.finish();
    }

}
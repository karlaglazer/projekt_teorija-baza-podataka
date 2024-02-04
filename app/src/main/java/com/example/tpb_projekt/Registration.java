package com.example.tpb_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.User;
import com.example.tpb_projekt.models.Person;
import com.example.tpb_projekt.services.AuthenticatedUser;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Registration extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }
    public void goToLogIn(View v){
        Intent i = new Intent(this, LogIn.class);
        startActivity(i);
    }
    public void onRegistrationClick(View v){
        Intent i = new Intent(this, Main.class);
        TextView nameT = findViewById(R.id.inputName);
        TextView lastNameT = findViewById(R.id.inputLastName);
        TextView emailT = findViewById(R.id.inputEmail);
        TextView usernameT = findViewById(R.id.inputUsername);
        TextView passwordT = findViewById(R.id.inputPassword);
        TextView passwordConfT = findViewById(R.id.inputConfirmPassword);
        String name = nameT.getText().toString();
        String lastName = lastNameT.getText().toString();
        String email = emailT.getText().toString();
        String username = usernameT.getText().toString();
        String password = passwordT.getText().toString();
        String passConf = passwordConfT.getText().toString();
        if(name.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || passConf.isEmpty()){
            Toast.makeText(Registration.this, "Svi podaci moraju biti popunjeni.", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(passConf)){
            Toast.makeText(Registration.this, "Lozinke nisu iste.", Toast.LENGTH_SHORT).show();
        }
        else{
            OkHttpClient okhttpclient = new OkHttpClient();
            Request request = new Request.Builder().url("http://192.168.178.62:5000/getAllUsers").build();
            okhttpclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Registration.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String jsonData = response.body().string();
                    String data ="["+ parser.SingleStringClear(jsonData)+"]";
                    final Boolean[] checkUsername = {false};
                    final Boolean[] checkEmail = {false};
                    List<User> users = objectMapper.readValue(data, new TypeReference<List<User>>(){});
                    users.forEach(igrac -> {
                        if(igrac.korisnicko_ime.equals(username)){
                            checkUsername[0] = true;
                        }
                        else if(igrac.osoba.email.equals(email)){
                            checkEmail[0] = true;
                        }
                    });
                    if(checkEmail[0]){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Registration.this, "Email već postoji.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if(checkUsername[0]){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Registration.this, "Korisničko ime već postoji.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        User newUser = new User();
                        Person newPerson = new Person();
                        newPerson.ime = name;
                        newPerson.prezime = lastName;
                        newPerson.email = email;
                        newUser.osoba = newPerson;
                        newUser.korisnicko_ime = username;
                        newUser.lozinka = password;
                        String jsonToSend = objectMapper.writeValueAsString(newUser);
                        RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), jsonToSend);
                        Request registerUser = new Request.Builder().url("http://192.168.178.62:5000/setUser").post(formBody).build();
                        Call callInsertUser = okhttpclient.newCall(registerUser);
                        Response responseInsertUser = callInsertUser.execute();
                        String responseInsertUserString = responseInsertUser.body().string();
                        if(responseInsertUserString.equals("ok")){
                            Request getUser = new Request.Builder().url("http://192.168.178.62:5000/getUser/"+newUser.korisnicko_ime).build();
                            okhttpclient.newCall(getUser).enqueue(new Callback() {
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
                                        AuthenticatedUser.loggedIn = user;
                                        startActivity(i);
                                    }
                                }
                            });
                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Registration.this, "Korisnik nije registriran. Pokušajte ponovno.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
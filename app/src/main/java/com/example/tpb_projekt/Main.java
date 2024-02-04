package com.example.tpb_projekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.tpb_projekt.models.Game;
import com.example.tpb_projekt.models.User;
import com.example.tpb_projekt.services.AuthenticatedUser;
import com.example.tpb_projekt.services.CurrentGame;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView username =(TextView) findViewById(R.id.idTxtUsername);
        username.setText(AuthenticatedUser.loggedIn.korisnicko_ime);
    }
    public void CreateNewGame(View v){
        Intent i = new Intent(this, NewGame.class);
        Request createGame = new Request.Builder().url("http://192.168.178.62:5000/createGame/"+AuthenticatedUser.loggedIn.id_igrac).build();
        okhttpclient.newCall(createGame).enqueue(new Callback() {
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
                Log.e("data", data);
                Game newGame = objectMapper.readValue(data, Game.class);
                Log.e("game", newGame.id_igra.toString()
                );
                CurrentGame.currentGame = newGame;
                startActivity(i);
                Main.this.finish();
            }
        });
    }
    public void logOut(View v){
        AuthenticatedUser.loggedIn = null;
        CurrentGame.currentGame = null;
        Intent i = new Intent(this, LogIn.class);
        startActivity(i);
        this.finish();
    }
    public void openMyGames(View v){
        Intent i = new Intent(this, MyGames.class);
        startActivity(i);
        this.finish();
    }
    public void onStatisticClick(View v){
        Intent i = new Intent(this, Statistic.class);
        startActivity(i);
        this.finish();
    }
}
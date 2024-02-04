package com.example.tpb_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.User;
import com.example.tpb_projekt.services.CurrentGame;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Playing extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();
    final List<User>[] listOfPlayers = new List[1];
    LinearLayout usersInPlay;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        usersInPlay = findViewById(R.id.usersInPlayLayout);
        try {
            getUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUsers() throws IOException{
        Request getAddedUsers = new Request.Builder().url("http://192.168.178.62:5000/getUsersInGame/"+ CurrentGame.currentGame.id_igra).build();
        okhttpclient.newCall(getAddedUsers).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Playing.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                String data ="["+ parser.SingleStringClear(jsonData)+"]";
                Log.e("data za spinner", data);
                List<User> users = objectMapper.readValue(data, new TypeReference<List<User>>(){});
                listOfPlayers[0] = users;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listOfPlayers[0].forEach(player->{
                            TextView newView = new TextView(Playing.this);
                            newView.setText(player.korisnicko_ime);
                            newView.setTextSize(18);
                            newView.setTextColor(Color.parseColor("#710C04"));
                            if(newView.getParent() != null){
                                ((ViewGroup)newView.getParent()).removeView(newView);
                            }
                            usersInPlay.addView(newView);
                        });
                    }
                });
            }
        });
    }
    public void onDrawMissionClick(View v){
        Intent i = new Intent(this, DrawMission.class);
        startActivity(i);
        this.finish();
    }
    public void onBuildTrainClick(View v){
        Intent i = new Intent(this, BuildTrain.class);
        startActivity(i);
        this.finish();
    }
    public void onBuidStationClick(View v){
        Intent i = new Intent(this, Statistic.class);
        startActivity(i);
        this.finish();
    }
    public void onReturnClick(View v){
        Intent i = new Intent(this, Main.class);
        startActivity(i);
        this.finish();
    }
    public void onFinishClick(View v){
        Request finishGame =new Request.Builder().url("http://192.168.178.62:5000/finishGame/"+ CurrentGame.currentGame.id_igra).build();
        okhttpclient.newCall(finishGame).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Playing.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String res = response.body().string();
                if (res.equals("ok")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(Playing.this, Statistic.class);
                            startActivity(i);
                            Playing.this.finish();
                        }
                    });
                }
            }
        });
    }


}
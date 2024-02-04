package com.example.tpb_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.Game;
import com.example.tpb_projekt.services.AuthenticatedUser;
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

public class MyGames extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();
    final List<Game>[] listOfAllGames = new List[1];
    LinearLayout gamesLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_games);
        gamesLayout = findViewById(R.id.usersInPlayLayout);
        try {
            runGetGames();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void runGetGames() throws IOException{
        Request getAllGames = new Request.Builder().url("http://192.168.178.62:5000/getAllGames/"+ AuthenticatedUser.loggedIn.id_igrac).build();
        okhttpclient.newCall(getAllGames).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(MyGames.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                String data ="["+ parser.SingleStringClearWithArray(jsonData)+"]";
                Log.e("data", data);
                List<Game> games = objectMapper.readValue(data, new TypeReference<List<Game>>(){});
                listOfAllGames[0] = games;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listOfAllGames[0].forEach(game->{
                            TextView newView = new TextView(MyGames.this);
                            newView.setTextSize(24);
                            newView.setTextColor(Color.parseColor("#710C04"));
                            if(game.zavrseno!=null){
                                newView.setText("Završeno: "+game.zavrseno);
                                newView.setClickable(true);
                                newView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CurrentGame.currentGame = game;
                                        Intent in = new Intent(MyGames.this, Statistic.class);
                                        startActivity(in);
                                        MyGames.this.finish();
                                    }
                                });
                            }
                            else{
                                newView.setText("Započeto: "+game.zapoceto);
                                newView.setClickable(true);
                                if(game.zapoceto != null){
                                    newView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CurrentGame.currentGame = game;
                                            Intent in = new Intent(MyGames.this, Playing.class);
                                            startActivity(in);
                                            MyGames.this.finish();
                                        }
                                    });
                                }
                                else{
                                    newView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CurrentGame.currentGame = game;
                                            Intent in = new Intent(MyGames.this, NewGame.class);
                                            startActivity(in);
                                            MyGames.this.finish();
                                        }
                                    });
                                }
                            }
                            if(newView.getParent() != null){
                                ((ViewGroup)newView.getParent()).removeView(newView);
                            }
                            gamesLayout.addView(newView);

                        });
                    }
                });
            }
        });
    }
    public void OnBtnReturnClick(View v){
        Intent i = new Intent(this, Main.class);
        startActivity(i);
        this.finish();
    }
}
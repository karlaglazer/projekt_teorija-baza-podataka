package com.example.tpb_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.PlayerGame;
import com.example.tpb_projekt.models.PlayerTrain;
import com.example.tpb_projekt.models.Trains;
import com.example.tpb_projekt.models.User;
import com.example.tpb_projekt.services.CurrentGame;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Statistic extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();
    final List<PlayerGame>[] listOfPlayers = new List[1];
    LinearLayout playersLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        playersLayout = findViewById(R.id.statisticLayout);
        try {
            getPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getPlayers() throws IOException {
        Request getAllPlayers = new Request.Builder().url("http://192.168.178.62:5000/getUsersGamesInGame/"+CurrentGame.currentGame.id_igra).build();
        okhttpclient.newCall(getAllPlayers).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonDataMy = response.body().string();
                String dataMy = "[" + parser.SingleStringClear(jsonDataMy) + "]";
                if (dataMy.equals("[null]")) {
                    listOfPlayers[0] = Collections.emptyList();
                } else {
                    List<PlayerGame> myTrains = objectMapper.readValue(dataMy, new TypeReference<List<PlayerGame>>() {
                    });
                    listOfPlayers[0] = myTrains;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listOfPlayers[0].forEach(player->{
                            TextView text = new TextView(Statistic.this);
                            text.setTextColor(Color.parseColor("#710C04"));
                            text.setTextSize(20);
                            Request getUser = new Request.Builder().url("http://192.168.178.62:5000/getUserWithID/"+player.id_igrac).build();
                            okhttpclient.newCall(getUser).enqueue(new Callback() {
                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    String jsonData = response.body().string();
                                    String data =parser.SingleStringClear(jsonData);
                                    User user = objectMapper.readValue(data, User.class);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            text.setText(user.korisnicko_ime+"\n");
                                            text.append("preostali vlakovi -> "+ player.vlakovi+"\n");
                                            text.append("bodovi -> "+player.bodovi+"\n");
                                            if (text.getParent() != null) {
                                                ((ViewGroup) text.getParent()).removeView(text);
                                            }
                                            playersLayout.addView(text);
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(Statistic.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                        });

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Statistic.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void OnReturnFromStat(View v){
        CurrentGame.currentGame = null;
        Intent i = new Intent(Statistic.this, MyGames.class);
        startActivity(i);
        Statistic.this.finish();
    }
}
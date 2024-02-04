package com.example.tpb_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.Mission;
import com.example.tpb_projekt.models.PlayerGame;
import com.example.tpb_projekt.models.PlayerMission;
import com.example.tpb_projekt.services.AuthenticatedUser;
import com.example.tpb_projekt.services.CurrentGame;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DrawMission extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();
    final List<Mission>[] listOfMissions = new List[1];
    final List<PlayerMission>[] listOfMyMissions = new List[1];
    LinearLayout myMissionsLayout;
    Spinner missionSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_mission);
        myMissionsLayout = findViewById(R.id.myMissionLayout);
        missionSpinner = findViewById(R.id.missionSpinner);
        try {
            getMission();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getMission() throws IOException {

        Request getAllMission = new Request.Builder().url("http://192.168.178.62:5000/getAllMissions").build();
        okhttpclient.newCall(getAllMission).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                String data = "[" + parser.SingleStringClear(jsonData) + "]";
                List<Mission> missions = objectMapper.readValue(data, new TypeReference<List<Mission>>() {
                });
                listOfMissions[0] = missions;
                Request getMyMissions = new Request.Builder().url("http://192.168.178.62:5000/getMyMissions?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
                okhttpclient.newCall(getMyMissions).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String jsonDataMy = response.body().string();
                        String dataMy = "[" + parser.SingleStringClear(jsonDataMy) + "]";
                        if (dataMy.equals("[null]")) {
                            listOfMyMissions[0] = Collections.emptyList();
                        } else {
                            List<PlayerMission> myMissions = objectMapper.readValue(dataMy, new TypeReference<List<PlayerMission>>() {
                            });
                            listOfMyMissions[0] = myMissions;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<String> missionArrayList = new ArrayList<>();
                                listOfMissions[0].forEach(mission -> {
                                    final boolean[] exist = {false};
                                    if (!listOfMyMissions[0].isEmpty()) {
                                        listOfMyMissions[0].forEach(id -> {
                                            if (id.misija == mission.id_misija) {
                                                exist[0] = true;
                                            }
                                        });
                                    }
                                    if (exist[0] == true) {
                                        CheckBox check = new CheckBox(DrawMission.this);
                                        check.setText(mission.grad1.naziv + "->" + mission.grad2.naziv + ":" + mission.bodovi);
                                        check.setTextColor(Color.parseColor("#710C04"));
                                        PlayerMission checkBoolean = listOfMyMissions[0].stream()
                                                .filter(playerMission -> mission.id_misija == playerMission.misija)
                                                .findAny()
                                                .orElse(null);
                                        if (checkBoolean.zavrsena == true) {
                                            check.setChecked(true);
                                        }
                                        check.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (check.isChecked()) {
                                                    Request getId = new Request.Builder().url("http://192.168.178.62:5000/getUserGameId?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
                                                    okhttpclient.newCall(getId).enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                            String data = response.body().string();
                                                            Integer id = Integer.valueOf(parser.SingleStringClear(data));
                                                            Request finishMission = new Request.Builder().url("http://192.168.178.62:5000/finishMission?id_igrac_igra=" + id + "&id_misija=" + mission.id_misija).build();
                                                            okhttpclient.newCall(finishMission).enqueue(new Callback() {
                                                                @Override
                                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                    String res = response.body().string();
                                                                    if (res.equals("ok")) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Request getPlayerGame = new Request.Builder().url("http://192.168.178.62:5000/getUserGame?id_igrac_igra=" + id).build();
                                                                                okhttpclient.newCall(getPlayerGame).enqueue(new Callback() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }

                                                                                    @Override
                                                                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                        String res = response.body().string();
                                                                                        String data = parser.SingleStringClear(res);
                                                                                        PlayerGame playerGame = objectMapper.readValue(data, PlayerGame.class);
                                                                                        Integer points = playerGame.bodovi + mission.bodovi;
                                                                                        Request updatePoints = new Request.Builder().url("http://192.168.178.62:5000/updatePoints?id_igrac_igra=" + id + "&bodovi=" + points).build();
                                                                                        okhttpclient.newCall(updatePoints).enqueue(new Callback() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                                runOnUiThread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                            }

                                                                                            @Override
                                                                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                                String res = response.body().string();
                                                                                                if (res.equals("ok")) {
                                                                                                    runOnUiThread(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            Toast.makeText(DrawMission.this, "Bodovi su dodani.", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    Request getId = new Request.Builder().url("http://192.168.178.62:5000/getUserGameId?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
                                                    okhttpclient.newCall(getId).enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                            String data = response.body().string();
                                                            Integer id = Integer.valueOf(parser.SingleStringClear(data));
                                                            Request finishMission = new Request.Builder().url("http://192.168.178.62:5000/finishMission?id_igrac_igra=" + id + "&id_misija=" + mission.id_misija).build();
                                                            okhttpclient.newCall(finishMission).enqueue(new Callback() {
                                                                @Override
                                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                    String res = response.body().string();
                                                                    if (res.equals("ok")) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Request getPlayerGame = new Request.Builder().url("http://192.168.178.62:5000/getUserGame?id_igrac_igra=" + id).build();
                                                                                okhttpclient.newCall(getPlayerGame).enqueue(new Callback() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                        runOnUiThread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                                    }

                                                                                    @Override
                                                                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                        String res = response.body().string();
                                                                                        String data = parser.SingleStringClear(res);
                                                                                        PlayerGame playerGame = objectMapper.readValue(data, PlayerGame.class);
                                                                                        Integer points = playerGame.bodovi - mission.bodovi;
                                                                                        Request updatePoints = new Request.Builder().url("http://192.168.178.62:5000/updatePoints?id_igrac_igra=" + id + "&bodovi=" + points).build();
                                                                                        okhttpclient.newCall(updatePoints).enqueue(new Callback() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                                runOnUiThread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                            }

                                                                                            @Override
                                                                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                                String res = response.body().string();
                                                                                                if (res.equals("ok")) {
                                                                                                    runOnUiThread(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            Toast.makeText(DrawMission.this, "Bodovi su oduzeti.", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        if (check.getParent() != null) {
                                            ((ViewGroup) check.getParent()).removeView(check);
                                        }
                                        myMissionsLayout.addView(check);
                                    } else {
                                        missionArrayList.add(mission.id_misija + ":" + mission.grad1.naziv + "->" + mission.grad2.naziv);
                                    }
                                });
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(DrawMission.this, android.R.layout.simple_spinner_item, missionArrayList) {
                                    @Override
                                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        ((TextView) view).setTextColor(Color.parseColor("#710C04"));
                                        return view;
                                    }
                                };
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                missionSpinner.setAdapter(arrayAdapter);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
    }

    public void onAddClick(View v) {
        String[] mission = missionSpinner.getSelectedItem().toString().split(":");
        Integer id_mission = Integer.valueOf(mission[0]);
        Request getId = new Request.Builder().url("http://192.168.178.62:5000/getUserGameId?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
        okhttpclient.newCall(getId).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                Integer id = Integer.valueOf(parser.SingleStringClear(data));
                Request setMission = new Request.Builder().url("http://192.168.178.62:5000/setMission?id_igrac_igra=" + id + "&id_misija=" + id_mission).build();
                okhttpclient.newCall(setMission).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Toast.makeText(DrawMission.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String data = response.body().toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (data.contains("ok")) {
                                    Toast.makeText(DrawMission.this, "Misija je uspješno dodana.", Toast.LENGTH_SHORT).show();
                                    recreate();
                                } else {
                                    Toast.makeText(DrawMission.this, "Pokušajte ponovno.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        });
    }
    public void onReturnClickMission(View v){
        Intent i = new Intent(this, Playing.class);
        startActivity(i);
        this.finish();
    }
}
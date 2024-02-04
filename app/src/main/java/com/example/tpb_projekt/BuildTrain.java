package com.example.tpb_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tpb_projekt.models.Mission;
import com.example.tpb_projekt.models.PlayerGame;
import com.example.tpb_projekt.models.PlayerMission;
import com.example.tpb_projekt.models.PlayerTrain;
import com.example.tpb_projekt.models.Trains;
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

public class BuildTrain extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();
    final List<Trains>[] listOfTrains = new List[1];
    final List<PlayerTrain>[] listOfMyTrains = new List[1];
    final List<Trains>[] listOfPlayedTrains = new List[1];
    LinearLayout myTrainLayout;
    Spinner trainSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_train);
        myTrainLayout = findViewById(R.id.myTrainLayout);
        trainSpinner = findViewById(R.id.spinnerTrains);
        try {
            getTrains();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTrains() throws IOException {
        Request getAllTrains = new Request.Builder().url("http://192.168.178.62:5000/getAllTrains").build();
        okhttpclient.newCall(getAllTrains).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonData = response.body().string();
                String data = "[" + parser.SingleStringClear(jsonData) + "]";
                List<Trains> trains = objectMapper.readValue(data, new TypeReference<List<Trains>>() {
                });
                listOfTrains[0] = trains;
                Request getMyTrains = new Request.Builder().url("http://192.168.178.62:5000/getMyTrains?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
                okhttpclient.newCall(getMyTrains).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String jsonDataMy = response.body().string();
                        String dataMy = "[" + parser.SingleStringClear(jsonDataMy) + "]";
                        if (dataMy.equals("[null]")) {
                            listOfMyTrains[0] = Collections.emptyList();
                        } else {
                            List<PlayerTrain> myTrains = objectMapper.readValue(dataMy, new TypeReference<List<PlayerTrain>>() {
                            });
                            listOfMyTrains[0] = myTrains;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<String> trainArrayList = new ArrayList<>();
                                Request getAllPlayedTrains = new Request.Builder().url("http://192.168.178.62:5000/getPlayedTrains?id_igra=" + CurrentGame.currentGame.id_igra).build();
                                okhttpclient.newCall(getAllPlayedTrains).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        String jsonData = response.body().string();
                                        String data = "[" + parser.SingleStringClear(jsonData) + "]";
                                        if (dataMy.equals("[null]")) {
                                            listOfPlayedTrains[0] = Collections.emptyList();
                                        } else {
                                            List<Trains> trains = objectMapper.readValue(data, new TypeReference<List<Trains>>() {
                                            });
                                            listOfPlayedTrains[0] = trains;
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                listOfTrains[0].forEach(train -> {
                                                    final boolean[] exist = {false};
                                                    if (!listOfMyTrains[0].isEmpty()) {
                                                        listOfMyTrains[0].forEach(id -> {
                                                            if (id.pruga == train.id_pruga) {
                                                                exist[0] = true;
                                                            }
                                                        });
                                                    }
                                                    if (exist[0] == true) {
                                                        TextView text = new TextView(BuildTrain.this);
                                                        text.setText(train.grad1.naziv + "->" + train.grad2.naziv);
                                                        text.setTextColor(Color.parseColor("#710C04"));
                                                        text.setTextSize(18);
                                                        text.setClickable(true);
                                                        text.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Request getId = new Request.Builder().url("http://192.168.178.62:5000/getUserGameId?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
                                                                okhttpclient.newCall(getId).enqueue(new Callback() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                        String data = response.body().string();
                                                                        Integer id = Integer.valueOf(parser.SingleStringClear(data));
                                                                        Request deleteTrain = new Request.Builder().url("http://192.168.178.62:5000/deleteTrain?id_igrac_igra=" + id+"&id_pruga="+train.id_pruga).build();
                                                                        okhttpclient.newCall(deleteTrain).enqueue(new Callback() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                            }

                                                                            @Override
                                                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                String res = response.body().string();
                                                                                if (res.equals("ok")) {
                                                                                    Request getTrain = new Request.Builder().url("http://192.168.178.62:5000/getTrain?id_pruga=" + train.id_pruga).build();
                                                                                    okhttpclient.newCall(getTrain).enqueue(new Callback() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                            runOnUiThread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            });
                                                                                        }

                                                                                        @Override
                                                                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                            String res = response.body().string();
                                                                                            String data = parser.SingleStringClear(res);
                                                                                            Trains removedTrain = objectMapper.readValue(data, Trains.class);
                                                                                            Request getPlayerGame = new Request.Builder().url("http://192.168.178.62:5000/getUserGame?id_igrac_igra=" + id).build();
                                                                                            okhttpclient.newCall(getPlayerGame).enqueue(new Callback() {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                                    runOnUiThread(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    });
                                                                                                }

                                                                                                @Override
                                                                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                                                                    String res = response.body().string();
                                                                                                    String data = parser.SingleStringClear(res);
                                                                                                    PlayerGame playerGame = objectMapper.readValue(data, PlayerGame.class);
                                                                                                    Integer trainPoints = 0;
                                                                                                    switch (removedTrain.put.duzina) {
                                                                                                        case 1:
                                                                                                            trainPoints = 1;
                                                                                                            break;
                                                                                                        case 2:
                                                                                                            trainPoints = 2;
                                                                                                            break;
                                                                                                        case 3:
                                                                                                            trainPoints = 4;
                                                                                                            break;
                                                                                                        case 4:
                                                                                                            trainPoints = 7;
                                                                                                            break;
                                                                                                        case 6:
                                                                                                            trainPoints = 15;
                                                                                                            break;
                                                                                                        case 8:
                                                                                                            trainPoints = 21;
                                                                                                            break;
                                                                                                        default:
                                                                                                            break;
                                                                                                    }
                                                                                                    Integer points = playerGame.bodovi - trainPoints;
                                                                                                    Integer remainingTrains = playerGame.vlakovi + removedTrain.put.duzina;
                                                                                                    Request updatePoints = new Request.Builder().url("http://192.168.178.62:5000/updateTrainPoints?id_igrac_igra=" + id + "&bodovi=" + points+"&vlakovi="+remainingTrains).build();
                                                                                                    okhttpclient.newCall(updatePoints).enqueue(new Callback() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                                                                            runOnUiThread(new Runnable() {
                                                                                                                @Override
                                                                                                                public void run() {
                                                                                                                    Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
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
                                                                                                                        Toast.makeText(BuildTrain.this, "Bodovi su oduzeti.", Toast.LENGTH_SHORT).show();
                                                                                                                        recreate();
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
                                                        });
                                                        if (text.getParent() != null) {
                                                            ((ViewGroup) text.getParent()).removeView(text);
                                                        }
                                                        myTrainLayout.addView(text);
                                                    } else {
                                                        final boolean[] check = {false};
                                                        if (!listOfPlayedTrains[0].isEmpty()) {
                                                            listOfPlayedTrains[0].forEach(id -> {
                                                                if (id.id_pruga == train.id_pruga) {
                                                                    check[0] = true;
                                                                }
                                                            });
                                                        }
                                                        if (check[0] == false) {
                                                            trainArrayList.add(train.id_pruga + ":" + train.grad1.naziv + "->" + train.grad2.naziv);
                                                        }
                                                    }
                                                });
                                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BuildTrain.this, android.R.layout.simple_spinner_item, trainArrayList) {
                                                    @Override
                                                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                                                        View view = super.getView(position, convertView, parent);
                                                        ((TextView) view).setTextColor(Color.parseColor("#710C04"));
                                                        return view;
                                                    }
                                                };
                                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                trainSpinner.setAdapter(arrayAdapter);
                                                arrayAdapter.notifyDataSetChanged();
                                            }
                                        });

                                    }
                                });


                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void onAddTrainClick(View v) {
        String[] train = trainSpinner.getSelectedItem().toString().split(":");
        Integer id_train = Integer.valueOf(train[0]);
        Request getId = new Request.Builder().url("http://192.168.178.62:5000/getUserGameId?id_igra=" + CurrentGame.currentGame.id_igra + "&id_igrac=" + AuthenticatedUser.loggedIn.id_igrac).build();
        okhttpclient.newCall(getId).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String data = response.body().string();
                Integer id = Integer.valueOf(parser.SingleStringClear(data));
                Request setTrain = new Request.Builder().url("http://192.168.178.62:5000/setTrain?id_igrac_igra=" + id + "&id_pruga=" + id_train).build();
                okhttpclient.newCall(setTrain).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String data = response.body().toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (data.contains("ok")) {
                                    Request getTrain = new Request.Builder().url("http://192.168.178.62:5000/getTrain?id_pruga=" + id_train).build();
                                    okhttpclient.newCall(getTrain).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            String res = response.body().string();
                                            String data = parser.SingleStringClear(res);
                                            Trains addedTrain = objectMapper.readValue(data, Trains.class);
                                            Request getPlayerGame = new Request.Builder().url("http://192.168.178.62:5000/getUserGame?id_igrac_igra=" + id).build();
                                            okhttpclient.newCall(getPlayerGame).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                    String res = response.body().string();
                                                    String data = parser.SingleStringClear(res);
                                                    PlayerGame playerGame = objectMapper.readValue(data, PlayerGame.class);
                                                    Integer trainPoints = 0;
                                                    switch (addedTrain.put.duzina) {
                                                        case 1:
                                                            trainPoints = 1;
                                                            break;
                                                        case 2:
                                                            trainPoints = 2;
                                                            break;
                                                        case 3:
                                                            trainPoints = 4;
                                                            break;
                                                        case 4:
                                                            trainPoints = 7;
                                                            break;
                                                        case 6:
                                                            trainPoints = 15;
                                                            break;
                                                        case 8:
                                                            trainPoints = 21;
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    Integer points = playerGame.bodovi + trainPoints;
                                                    Integer remainingTrains = playerGame.vlakovi - addedTrain.put.duzina;
                                                    Request updatePoints = new Request.Builder().url("http://192.168.178.62:5000/updateTrainPoints?id_igrac_igra=" + id + "&bodovi=" + points+"&vlakovi="+remainingTrains).build();
                                                    okhttpclient.newCall(updatePoints).enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(BuildTrain.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
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
                                                                        Toast.makeText(BuildTrain.this, "Bodovi su dodani.", Toast.LENGTH_SHORT).show();
                                                                        recreate();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });

                                } else {
                                    Toast.makeText(BuildTrain.this, "Pokušajte ponovno.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        });
    }
    public void onReturnBTNClick(View v){
        Intent i = new Intent(this, Playing.class);
        startActivity(i);
        this.finish();
    }
}
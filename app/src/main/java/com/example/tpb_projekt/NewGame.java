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
import com.example.tpb_projekt.models.User;
import com.example.tpb_projekt.services.AuthenticatedUser;
import com.example.tpb_projekt.services.CurrentGame;
import com.example.tpb_projekt.services.ParsingClass;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewGame extends AppCompatActivity {
    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static ParsingClass parser = new ParsingClass();
    private static OkHttpClient okhttpclient = new OkHttpClient();
    final List<User>[] listOfAll = new List[1];
    final List<User>[] listOfAddedId = new List[1];
    LinearLayout addedUsers;
    LinearLayout otherUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        addedUsers = findViewById(R.id.addedPlayersLayout);
        otherUsers = findViewById(R.id.otherPlayersLayout);
        try {
            runNewGame();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void runNewGame() throws IOException {

        Intent i = new Intent(this, NewGame.class);
        Request getAllUsers = new Request.Builder().url("http://192.168.178.62:5000/getAllUsers/"+ AuthenticatedUser.loggedIn.id_igrac).build();
        okhttpclient.newCall(getAllUsers).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewGame.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(
                    @NotNull Call call,
                    @NotNull Response response)
                    throws IOException {
                String jsonData = response.body().string();
                String data ="["+ parser.SingleStringClear(jsonData)+"]";
                Log.e("data", data);
                List<User> users = objectMapper.readValue(data, new TypeReference<List<User>>(){});
                listOfAll[0] = users;
                Request getAddedUsers = new Request.Builder().url("http://192.168.178.62:5000/getUsersInGame/"+ CurrentGame.currentGame.id_igra).build();
                okhttpclient.newCall(getAddedUsers).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(NewGame.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(
                            @NotNull Call call,
                            @NotNull Response response)
                            throws IOException {
                        String jsonData = response.body().string();
                        String data ="["+ parser.SingleStringClear(jsonData)+"]";
                        List<User> ids = objectMapper.readValue(data, new TypeReference<List<User>>(){});
                        listOfAddedId[0] = ids;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listOfAll[0].forEach(user->{
                                    TextView newView = new TextView(NewGame.this);
                                    newView.setText(user.korisnicko_ime);
                                    newView.setTextSize(20);
                                    newView.setClickable(true);
                                    newView.setTextColor(Color.parseColor("#710C04"));
                                    final boolean[] added = {false};
                                    listOfAddedId[0].forEach(id->{
                                        if(id.id_igrac == user.id_igrac){
                                            added[0] = true;
                                        }
                                    });
                                    if(added[0]==true){
                                        newView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Request removeUser = new Request.Builder().url("http://192.168.178.62:5000/removeUserFromGame?id_igra="+ CurrentGame.currentGame.id_igra+"&id_igrac="+user.id_igrac).build();
                                                okhttpclient.newCall(removeUser).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(NewGame.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                        String res = response.body().string();
                                                        if(res.equals("ok")){
                                                                /*Intent refresh = new Intent(NewGame.this, NewGame.class);
                                                                startActivity(refresh);*/
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    recreate();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        if(newView.getParent() != null){
                                            ((ViewGroup)newView.getParent()).removeView(newView);
                                        }
                                        addedUsers.addView(newView);
                                    }
                                    else{
                                        newView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Request addUser = new Request.Builder().url("http://192.168.178.62:5000/addUserToGame?id_igra="+ CurrentGame.currentGame.id_igra+"&id_igrac="+user.id_igrac).build();
                                                okhttpclient.newCall(addUser).enqueue(new Callback() {
                                                    @Override
                                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(NewGame.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                        String res = response.body().string();
                                                        if(res.equals("ok")){
                                                                /*Intent refresh = new Intent(NewGame.this, NewGame.class);
                                                                startActivity(refresh);*/
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    recreate();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        if(newView.getParent() != null){
                                            ((ViewGroup)newView.getParent()).removeView(newView);
                                        }
                                        otherUsers.addView(newView);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }
    public void onStartClick(View v){
        try {
            newOnClick();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newOnClick() throws IOException {
        if(listOfAddedId[0].size()>=2){
            Integer[] array = new Integer[listOfAddedId[0].size()];
            for (int i=0; i<listOfAddedId[0].size(); i++){
                array[i] = listOfAddedId[0].get(i).id_igrac;
            }
            Game updateGame = CurrentGame.currentGame;
            updateGame.redoslijed = array;
            String jsonToSend = objectMapper.writeValueAsString(updateGame);
            Log.e("igra", jsonToSend.toString());
            RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), jsonToSend);
            Request updateTableGame = new Request.Builder().url("http://192.168.178.62:5000/startGame").post(formBody).build();
            okhttpclient.newCall(updateTableGame).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Toast.makeText(NewGame.this, "Server ne radi.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String jsonData = response.body().string();
                    String data = parser.SingleStringClearWithArray(jsonData);
                    Log.e("data", data);
                    Game newGame = objectMapper.readValue(data, Game.class);
                    Log.e("game", newGame.id_igra.toString()
                    );
                    CurrentGame.currentGame = newGame;
                    Intent in = new Intent(NewGame.this, Playing.class);
                    startActivity(in);
                    NewGame.this.finish();
                }
            });
        }
        else{
            Toast.makeText(NewGame.this, "Morate odabrati barem jednog igrača.", Toast.LENGTH_SHORT).show();
        }
    }

}
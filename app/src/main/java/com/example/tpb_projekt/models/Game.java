package com.example.tpb_projekt.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class Game {
    public Integer id_igra;
    public Date zapoceto;
    public Date zavrseno;
    public Integer kreator;
    public Integer[] redoslijed;
}

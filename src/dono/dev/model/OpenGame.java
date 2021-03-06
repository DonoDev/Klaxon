package dono.dev.model;

import android.util.Log;

public class OpenGame {

    private static final String TAG = "OpenGame";

    private String name;
    private String number;
    private String players;
    private String version;

    private FullReport fullreport;

    public OpenGame(String name, String number, String players, String version) {
        super();
        this.name =    name;
        this.number =  number;
        this.players = players;
        this.version = version;
        Log.d(TAG, "OpenGame created: " + this.toString());
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getPlayers() {
        return players;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Game[ Name: ")
        .append(name)
        .append(" Number: ")
        .append(number)
        .append(" Players: ")
        .append(players)
        .append(" Version: ")
        .append(version)
        .append(" ]");
        return builder.toString();
    }

    public void createFullReport(String JsonString) {
        
    }
}

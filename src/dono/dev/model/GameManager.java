package dono.dev.model;

import java.util.ArrayList;

public class GameManager {

    private static GameManager instance = null;

    private GameManager(){/*defeat instantiation*/}

    ArrayList<OpenGame> openGames;

    public GameManager getInstance() {
        if(instance == null) {
            instance = new GameManager();
            openGames = new ArrayList<OpenGame>();
        }
        return instance;
    }

    public void addGame(OpenGame game){
        openGames.add(game);
    }

    public void addFullReportToGame(String jsonstring){
        
    }
}

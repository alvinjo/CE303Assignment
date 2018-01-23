package Assignment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.glassfish.jersey.client.ClientConfig;
import webserver.GsonMessageBodyHandler;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alvin on 15/11/2017.
 */
public class BotClient implements Runnable{
    public static final String HOST = "http://localhost:8080/bot";
    public static final String BOT_URL = HOST+"/order66";
    public final WebTarget target;
    static Player player = new Player("hector");
    int playerListIndexPos;

    public BotClient(){
//        this.playerListIndexPos = playerListIndexPos;
        ClientConfig config = new ClientConfig(GsonMessageBodyHandler.class);
        target = ClientBuilder.newClient(config).target(BOT_URL);
    }

    @Override
    public void run(){
        BotClient bc = new BotClient();
        int numOfBots = 4 - bc.getPlayersConn();

        for (int i = 0; i < numOfBots; i++) {
            bc.addBotSimple();
        }
        execute();
//        System.out.println(bc.getPlayer());
//        System.out.println(bc.getPlayerByInd(0));
    }

    public static void main(String[] args) {
//        BotClient bc = new BotClient();
//        bc.addBotSimple();
//        System.out.println(bc.getPlayer());

    }

    public ArrayList<Player> getPlayerList(){
        ArrayList arrayJson = target.path("getPlayerList").request().get(ArrayList.class);
        Type t = new TypeToken<ArrayList<Player>>() {}.getType();
        return new Gson().fromJson(String.valueOf(arrayJson), t);
    }


    public void addBot(String name){
        target.path("addBot").path(name).request().post(Entity.json(name), String.class);
    }

    public void addBotByObject(Player player){
        target.path("addBotByObject").path(""+player).request().post(Entity.json(player), Player.class);
    }

    public int addBotSimple(){
        return target.path("addBotSimple").request().get(Integer.class);
    }


    public String getPlayer(){
        return target.path("getPlayer").request().get(String.class);
    }

    public boolean execute(){
        return target.path("execute").request().get(boolean.class);
    }


    public void botLogic(){
        trade();
        vote();
        await();
    }


    public void trade(){
        Stock highestValueStock = getHighestSharePriceStock();
        if(target.path("isCardPositive").path(""+highestValueStock).request().get(boolean.class)){
            int numShares = player.getFunds()/(highestValueStock.getSharePrice()+3);
            player.getShares().set(getIndex(highestValueStock), player.getShares().get(getIndex(highestValueStock)) + numShares);
            player.setFunds(-numShares*(highestValueStock.getSharePrice()+3));
        }
    }

    public void vote(){}

    public void await(){}

    public int getIndex(Stock stock){return target.path("getIndex").path(""+stock).request().get(int.class);}

    public Stock getHighestSharePriceStock(){return target.path("getHighestSharePriceStock").request().get(Stock.class);}

    public Integer getPlayersConn() {
        return target.path("getPlayersConn").request().get(Integer.class);
    }

    public String getPlayerByInd(int index){
        return target.path("getPlayerByInd").path(""+index).request().get(String.class);
    }

}
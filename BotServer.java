package Assignment;

/**
 * Created by Alvin on 15/11/2017.
 */

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/order66")
@Produces(MediaType.APPLICATION_JSON)
public class BotServer {
    public static Game g = GameServer.game;

    @GET
    @Path("/getPlayersConn")
    public int getPlayersConn(){
        return g.getPlayersConnected();
    }


    @GET
    @Path("/addBotSimple")
    public int addBotSimple(){
        Player player = new Player("(BOT)Hector");
        g.addPlayerObject(player);
        g.incrementPlayersConnected();
        return g.getPlayersConnected();
    }

    @GET
    @Path("/getPlayer")
    public String getPlayer(){
        int index = g.getPlayersConnected()-1;
        System.out.println(index);
        return g.getPlayerList().get(index).toString();
    }



    @GET
    @Path("/execute")
    public boolean execute(){
        for (int i = 0; i < g.getPlayerList().size(); i++) {
            if (g.getPlayerList().get(i).getName() == "(BOT)Hector"){
                buy(i);
            }
        }
        return true;
    }


    @GET
    @Path("/getPlayerByInd/{index}")
    public Player getPlayerByInd(@PathParam("index") int index){
        ArrayList<Player> alp = g.getPlayerList();
        return alp.get(index);
    }

/*    @GET
    @Path("/vote")
    public boolean vote(int yesno){

        return true;
    }*/

    @GET
    @Path("/buy/{index}")
    public boolean buy(@PathParam("index") int index){
        Stock highestValueStock = getHighestSharePriceStock();
        Player player = g.getPlayerList().get(index);


        if(isCardPositive(highestValueStock)){
            int numShares = player.getFunds()/(highestValueStock.getSharePrice()+3);
            player.getShares().set(getIndex(highestValueStock), player.getShares().get(getIndex(highestValueStock)) + numShares);
            player.setFunds(-numShares*(highestValueStock.getSharePrice()+3));
        }
        return true;
    }


    @GET
    @Path("/getIndex/{stock}")
    public int getIndex(@PathParam("stock") Stock stock){
        return g.getIndex(stock);
    }

    @GET
    @Path("/isCardPositive/{stock}")
    public boolean isCardPositive(@PathParam("stock") Stock stock){
        return g.isCardPositive(stock);
    }


    @GET
    @Path("/getHighestSharePriceStock")
    public Stock getHighestSharePriceStock(){
        return g.getHighestSharePriceStock();
    }

}
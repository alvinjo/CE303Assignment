package Assignment;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Alvin on 26/10/2017.
 */
public class Game {

    Stock apple, bp, cisco, dell, ericsson;
    private static CyclicBarrier barrier;
    private int playersConnected; //rem, check for usages
    private ArrayList<Player> playerList;
//    static BotClient bot = new BotClient();



    /**
     * The Game constructor instantiates all the stock attributes.
     */
    public Game(){
        apple = new Stock("Apple");
        bp = new Stock("BP");
        cisco = new Stock("Cisco");
        dell = new Stock("Dell");
        ericsson = new Stock("Ericsson");

        playersConnected = 0;
        barrier = new CyclicBarrier(2);
        playerList = new ArrayList<>();
    }


    /**
     * Getters and setters for Game attributes playersConnected and barrier.
     */
    public int getPlayersConnected(){return playersConnected;}

    public void incrementPlayersConnected(){playersConnected++;}

    public CyclicBarrier getBarrier(){return barrier;}

    public void setBarrierNumOfParties(int x){barrier = new CyclicBarrier(x);}

    public ArrayList<Player> getPlayerList(){return playerList;}

    /**
     * Creates a player and adds the player to the playerList.
     *
     * @param name The name of the player.
     * @return the Player object.
     */
    public Player addPlayer(String name){
        Player player = new Player(name);
        playerList.add(player);
        return player;
    }

    /**
     * Adds a Player object to the playerList.
     *
     * @param player Player object.
     */
    public void addPlayerObject(Player player){
        playerList.add(player);
    }



    /**
     * This method applies the influence cards to the stock. It applies the cards based on the value of voteCount.
     * It then resets the voteCount variables for each stock, ready for the next round.
     */
    public void applyCards(){
        if(apple.getVoteCount()>0){apple.setSharePrice(apple.top());apple.removeTop();}else if (apple.getVoteCount()<0){apple.removeTop();}
        if(bp.getVoteCount()>0){bp.setSharePrice(bp.top());bp.removeTop();}else if (bp.getVoteCount()<0){bp.removeTop();}
        if(cisco.getVoteCount()>0){cisco.setSharePrice(cisco.top());cisco.removeTop();}else if (cisco.getVoteCount()<0){cisco.removeTop();}
        if(dell.getVoteCount()>0){dell.setSharePrice(dell.top());dell.removeTop();}else if (dell.getVoteCount()<0){dell.removeTop();}
        if(ericsson.getVoteCount()>0){ericsson.setSharePrice(ericsson.top());ericsson.removeTop();}else if (ericsson.getVoteCount()<0){ericsson.removeTop();}

        apple.resetVoteCount();
        bp.resetVoteCount();
        cisco.resetVoteCount();
        dell.resetVoteCount();
        ericsson.resetVoteCount();
    }



    /**
     * Checks if a stocks influence card is positive.
     *
     * @param stock whose influence card is to be checked.
     * @return true if the card is positive and false if negative.
     */
    public boolean isCardPositive(Stock stock){return stock.top() > 0;}


    /**
     * Gets the stock with the highest share price.
     *
     * @return the stock with the highest share price.
     */
    public Stock getHighestSharePriceStock(){
        int sharePrice = 0;
        int index = 0;
        for (int i = 0; i < 4; i++) {
            if (sharePrice < getStockByIndex(i).getSharePrice()){
                sharePrice = getStockByIndex(i).getSharePrice();
                index = i;
            }
        }
        return getStockByIndex(index);
    }


    /**
     * This method is used to find the stock associated with the index position in a players shares Array.
     *
     * @param index this is the index position in a players shares Array.
     * @return the stock associated with the index position. E.g. index = 0 returns Apple.
     */
    public Stock getStockByIndex(int index){
        switch(index){
            case 0: return apple;
            case 1: return bp;
            case 2: return cisco;
            case 3: return dell;
            case 4: return ericsson;
            default: return null;
        }
    }

    /**
     * This method is used to find the index position of a stock in the shares Array.
     *
     * @param stock the stock who's index value is being returned.
     * @return the index position of the stock.
     */
    public int getIndex(Stock stock){
        switch (stock.getName()){
            case "Apple": return 0;
            case "BP": return 1;
            case "Cisco": return 2;
            case "Dell": return 3;
            case "Ericsson": return 4;
            default: return -1;
        }
    }


}
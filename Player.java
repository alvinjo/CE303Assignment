package Assignment;

import java.util.ArrayList;

/**
 * Created by Alvin on 26/10/2017.
 */
public class Player implements Comparable<Player> {

    private String name;
    private int funds;
    private ArrayList<Integer> shares;
    private StringBuilder votedStocks;
    private int votesLeft;
    private int tradesLeft;
    private int playerWorth;
    private boolean botMode;


    /**
     * The Player constructor instantiates the necessary attributes for a Player. It takes the users input as a parameter
     * and instantiates the variable name with this input.
     *
     * @param name the users desired player name.
     */
    Player(String name){
        this.name = name;
        funds = 500;
        shares = new ArrayList<>(5);
        votedStocks = new StringBuilder();
        votesLeft = 2;
        tradesLeft = 2;
        playerWorth = 0;
        botMode = false;
        addShares();
    }


    /**
     * This method randomly apply values to the shares ArrayList. The values sharesAmount and sharesLeft are used to keep track.
     * of remaining shares to be given out.
     */
    private void addShares(){
        int sharesLeft = 10;
        for (int i = 0; i < 5; i++) {
            int shareAmount = (int)Math.round(Math.random()*sharesLeft);
            sharesLeft -= shareAmount;
            shares.add(i,shareAmount);
            if(i==4 && sharesLeft>0){
                int index = (int)Math.round(Math.random()*4);
                shares.set(index, shares.get(index)+sharesLeft);
            }
        }
    }

    public void resetActions(){
        votesLeft = tradesLeft = 2;
        votedStocks.delete(0,2);
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public int getFunds(){return this.funds;}
    public void setFunds(int x){funds += x;}

    public ArrayList<Integer> getShares(){return this.shares;}

    public StringBuilder getVotedStocks() {return votedStocks;}

    public int getVotesLeft() {return votesLeft;}
    public void deductVotesLeft(){votesLeft--;}

    public int getTradesLeft(){return tradesLeft;}
    public void deductTradesLeft(){tradesLeft--;}

    public int getPlayerWorth(){return playerWorth;}
    public void setPlayerWorth(int x){playerWorth = x;}

    /**
     * Displays the players name, funds and shares.
     *
     * @return a String containing the above stated attributes.
     */
    @Override
    public String toString() {
        return name + " Funds: £" + funds +
                ", Shares: A[" + shares.get(0) + "], B[" + shares.get(1) + "], C[" + shares.get(2) +
                "], D[" + shares.get(3) + "], E[" + shares.get(4) + "]";
    }


    /**
     * This compareTo method sorts players in terms of playerWorth. The player with the highest playerWorth will be at
     * the top of the list.
     *
     * @param p the Player to compare with.
     * @return the player with the higher playerWorth.
     */
    @Override
    public int compareTo(Player p) {
        if (p.playerWorth>playerWorth)return 1;
        else if (p.playerWorth<playerWorth) return -1;
        return 0;
    }


}

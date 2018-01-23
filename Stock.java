package Assignment;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Alvin on 26/10/2017.
 */
public class Stock {

    private String name;
    private int sharePrice;
    private ArrayList<Integer> deck;
    private int voteCount;


    /**
     * The Stock constructor instantiates the necessary attributes for the stock class. It also calls the createDeck method
     * to fill the deck.
     *
     * @param name the name of the stock. E.g. Apple.
     */
    public Stock(String name){
        sharePrice = 100;
        deck = new ArrayList<>(6);
        voteCount = 0;
        this.name = name;
        createDeck();
    }

    public Stock(){}

    /**
     * Getters and setters for Stock attributes name, sharePrice and voteCount.
     */
    public String getName(){return name;}

    public int getSharePrice(){return sharePrice;}
    public void setSharePrice(int x){sharePrice += x;}

    public ArrayList<Integer> getDeck(){return deck;}

    public int getVoteCount(){return voteCount;}
    public void resetVoteCount(){voteCount=0;}


    /**
     * This method creates the deck for the stock and then shuffles them.
     */
    public void createDeck(){
        deck.add(-20); deck.add(-10); deck.add(-5); deck.add(5); deck.add(10); deck.add(20);
        Collections.shuffle(deck);
    }

    /**
     * This method returns the card at the top of the deck.
     *
     * @return the value of the first item in the stocks deck.
     */
    public int top(){
        return deck.get(0);
    }


    /**
     * This method removes the card at the top of the deck and trims the size of the ArrayList.
     */
    public void removeTop(){
        deck.remove(0);
        deck.trimToSize();
    }


    /**
     * Prints the share price and each card in the deck.
     *
     * @return a string containing the above mentioned attributes.
     */
    @Override
    public String toString(){
        StringBuffer print = new StringBuffer();
        print.append("[");
        for (int i = 0; i < deck.size()-1; i++) {
            print.append(deck.get(i) + ", ");
        }
        print.append(deck.get(deck.size()-1) + "]");

        return print.toString();
    }


    /**
     * This method is used for voting on the influence cards. The voteCount variable is updated according to the vote
     * parameter.
     * If voteCount is positive, the card is executed and removed. If voteCount is negative, the card is removed.
     * If voteCount is 0 then the card is not executed and not removed.
     *
     * @param vote acts as a yes or no argument. 0 = NO, 1 = YES.
     */
    public synchronized void  vote(int vote){
        if (vote == 0){
            voteCount -= 1;
        }else{
            voteCount += 1;
        }
    }
}

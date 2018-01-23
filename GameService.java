package Assignment;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Alvin on 27/10/2017.
 */
public class GameService implements Runnable{

    private Scanner in;
    private PrintWriter out;
    private Player player;
    private boolean login;
    public static ArrayList<Player> playerList;
    private Game game;
    private int round;
    private static final Object lock = new Object();
    private boolean botMode;
    BotClient bot;
    boolean botSignal = false;



    /**
     * The constructor for GameService instantiates all the necessary attributes of the class.
     * The cyclic barrier is instantiated with 4 parties. This means anytime the await() method is called for the barrier,
     * it will be waiting for 4 threads.
     *
     * @param game this is the Game object contains the stock information. This object is shared between all threads.
     * @param socket this is the port which all threads communicate through.
     */
    GameService(Game game, Socket socket, boolean botMode){
        this.game = game;
        player = null;
        login = false;
        playerList = game.getPlayerList();
        round = 0;
        this.botMode = botMode;

        System.out.println("thread created");

        try{
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * This is the method in which the game is played. Calls to other methods are made within this method to provide the
     * gameplay.
     * If a player logs out before the last round, the second while loop is initiated and the botLogic method called,
     * allowing the players actions to be automated.
     */
    @Override
    public void run() {
        if(!botMode && game.getPlayersConnected() < 4){
            login();
            out.println("waiting for players to connect...\r\n");
            awaitT();
        }

        while(login && !botMode){
            try {
                synchronized (lock){nextRound();}
                if(round==6){
                    exitGame();
                }else{
                    display();
//                    synchronized (lock){executeBot();} //let the bot thread perf

                    out.println("\r\nList of commands: \r\n" +
                            "vote (vote for the execution of an influence card)\r\n" +
                            "trade (buy or sell shares)\r\n" +
                            "next (proceed to next round)\r\n" +
                            "logout (exit game)\r\n"
                    );
                    command();
                }

            } catch (NoSuchElementException e) {
                login = false;
            }
        }

        //close session
        in.close();
        out.close();

        while(botMode && round !=6){   //move above close session
            botLogic();
        }

    }


    /**
     * The player enters their desired display name and a Player object is created with the desired name.
     * The player name is passed into the constructor for the Player class.
     * The variable playersConnected is updated to reflect the current number of players connected.
     * The variable playerList is updated to hold the new player object.
     */
    private void login(){
        out.println("Enter player name: ");
        player = new Player(in.nextLine());
        game.addPlayerObject(player);
        out.println("Player name set to: " + player.getName() + "\r\n");

        game.incrementPlayersConnected();

        login = true;
    }


    /**
     * This method changes the login variable to false. The first while loop in the run method is exited, allowing the
     * rest of the code to be executed and the thread to finish.
     * This method also activates botMode which will allow the automation of player actions.
     */
    private void logout(){
        player.setName("(bot) " + player.getName());
        botMode = true;
        System.out.println("bot made");
        login = false;
    }


    /**
     * Starts the wait process for the running threads.
     */
    private void await() {
        try {
            game.getBarrier().await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the wait process for the threads attempting to login.
     * If after 5 seconds, 4 players are not connected, the cyclic barrier is trimmed according to the number of
     * players connected. A bot thread is created which will create bots until there are 4 players in the game.
     */
    private void awaitT(){
        try {
            game.getBarrier().await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            trimBarrier();
            synchronized (lock){
                if(!botSignal){
                    bot = new BotClient();
                    new Thread(bot).start();
                    botSignal = true;
                }

            }
        }
    }

    /**
     * Calls the setBarrierNumOfParties(int x) method in Game. This will trim the cyclic barrier.
     */
    public void trimBarrier(){
        game.setBarrierNumOfParties(game.getPlayersConnected());
    }


    /**
     * This method executes the bot threads execute() method.
     */
    public void executeBot(){
        if(botSignal){
            if(bot != null){
                bot.execute();
            }
        }
        botSignal=false;
    }



    /**
     * This method is used as a waiting point for all threads after the command "next" is entered in the command() method.
     * @return A string explaining the waiting status.
     */
    private String waitPrint(){return "waiting for other players...";}


    /**
     * This method increments the round number, prints the round number, resets every players votes and trade actions and
     * makes a call to the applyCards() method in the Game class.
     * Each player can once again perform two voting and/or trading actions.
     * On the sixth round, the endGame() method is called and the end game process begins.
     */
    private void nextRound(){
        round++;
        if(round == 6){
            endGame();
        }else{
            out.println("###ROUND " + round + "###");
//            ArrayList<Player> playerList = game.getPlayerList();
            for (Player e : playerList) {
                e.resetActions();
            }
            game.applyCards();
        }
    }


    /**
     * This method acts as the main menu for the player.
     * It allows the player to choose from three commands: vote, trade and next (as well as an additional logout command).
     */
    private void command(){
        out.println("Enter command: ");
        String comInput = in.nextLine();

        if (comInput.toLowerCase().equals("vote")) {
            voteCommand();
        }else if (comInput.toLowerCase().equals("trade")){
            tradeCommand();
        }else if (comInput.toLowerCase().equals("next")){
            out.println(waitPrint());
            await();
//            nextRound();
        }else if(comInput.toLowerCase().equals("logout")){
//            login = false;
            logout();
        }
    }


    /**
     * This method acts as the final input before the players vote is cast. The input consists of a letter and a number.
     * The letter indicates the stock to vote on, and the number represents yes or no (1=yes, 0=no).
     */
    private void voteCommand(){
        out.println("\r\nEnter stock and vote (e.g. A1 for YES, B0 for NO). Enter 'X' to exit.");
        String voteInput = in.nextLine();

        switch(voteInput.toUpperCase()){
            case "A1":castVote(game.apple, 1);
                break;
            case "A0":castVote(game.apple, 0);
                break;
            case "B1":castVote(game.bp, 1);
                break;
            case "B0":castVote(game.bp, 0);
                break;
            case "C1":castVote(game.cisco, 1);
                break;
            case "C0":castVote(game.cisco, 0);
                break;
            case "D1":castVote(game.dell, 1);
                break;
            case "D0":castVote(game.dell, 0);
                break;
            case "E1":castVote(game.ericsson, 1);
                break;
            case "E0":castVote(game.ericsson, 0);
                break;
            case "X":command();
                break;
            default:
                out.println("Invalid input: Please enter a stock (e.g. A) and a vote (0 or 1)" + "\r\n");
                voteCommand();
                break;
        }

    }

    /**
     * This method makes a call to the vote method in the Stock class which updates the stocks voteCount value.
     * The method checks if the player still has vote actions left and then checks if the player has already voted
     * for a stock.
     * It then calls the stocks vote method with the appropriate yesno value (1=YES, 0=NO).
     * E.g. stock=Apple, yesno=1, this would mean a yes vote for the apple influence card.
     *
     * @param stock the desired stock to vote for.
     * @param yesno indicates whether the vote is a yes or a no. 1=YES, 0=NO.
     */
    private void castVote(Stock stock, int yesno){
        if (player.getVotesLeft() != 0){
            //if the stock names first letter doesn't exist in voteStocks
            if (!(player.getVotedStocks().lastIndexOf(stock.getName().charAt(0) + "") >= 0)){
                stock.vote(yesno);
                if(yesno == 1){out.println("Voted YES for " + stock.getName() + " influence card");}
                else{out.println("Voted NO for " + stock.getName() + " influence card");}
                player.getVotedStocks().append(stock.getName().charAt(0));
                player.deductVotesLeft();
                voteCommand();
            }else{out.println("Vote already cast for " + stock.getName()); voteCommand();}
        }else{out.println("No more votes left\r\n");command();}
    }


    /**
     * This method acts as the trade menu. It provides the player with options to buy or sell shares.
     * It also displays the players shares as well as the total value of all their shares.
     */
    private void tradeCommand(){
        out.println("\r\nShares you own:" +
                "\r\nApple: " + player.getShares().get(0) +
                "\r\nBP: " + player.getShares().get(1) +
                "\r\nCisco: " + player.getShares().get(2) +
                "\r\nDell: " + player.getShares().get(3) +
                "\r\nEricsson: " + player.getShares().get(4) +
                "\r\n\r\nShare prices: Apple-" + game.apple.getSharePrice() +
                ", BP-" + game.bp.getSharePrice() +
                ", Cisco-" + game.cisco.getSharePrice() +
                ", Dell-" + game.dell.getSharePrice() +
                ", Ericsson-" + game.ericsson.getSharePrice() +
                "\r\n\r\nFunds: £" + player.getFunds() + ", Current value of shares: £" + getTotalShareValue(player) + "\r\n"
        );

        out.println("Would you like to buy(b) or sell(s) shares? Enter 'X' to exit.");
        String bsInput = in.nextLine().trim();
        if (bsInput.toUpperCase().equals("B")){
            getTradeParam(1); //buy
        }else if (bsInput.toUpperCase().equals("S")){
            getTradeParam(0); //sell
        }else if (bsInput.toUpperCase().equals("X")){
            command();
        }else{
            out.println("Invalid input");
            tradeCommand();
        }
    }


    /**
     * This method takes an integer argument that selects the type of trade that the method will perform. E.g. select=1
     * is buy, select=0 is sell.
     * The player is prompted for an input. The input is split into the first character which indicates the stock to
     * trade, and the number of shares to buy/sell.
     * After the input, the commitBuy or commitSell methods are called depending on the select value.
     *
     * @param select is an integer value (either 1 or 0) that represents the buy and sell options. 1=BUY, 0=SELL.
     */
    private void getTradeParam(int select){
        String buysell;if (select==1){buysell = "buy";}else{buysell = "sell";}
        out.println("\r\nEnter stock and number of shares you wish to " + buysell + " (e.g. E5). Enter 'X' to exit." +
                "\r\n£3 transaction charge for purchases");
        String tradeInput = in.nextLine();
        if(player.getTradesLeft() == 0){out.println("No more trade actions left\r\n");command();}
        if(tradeInput.toUpperCase().equals("X")){tradeCommand();}
        int numShares = Integer.parseInt(tradeInput.trim().substring(1));
        switch(tradeInput.toUpperCase().charAt(0)){
            case 'A': if(select==1){commitBuy(game.apple, numShares);}
            else{commitSell(game.apple, numShares);}
                break;
            case 'B': if(select==1){commitBuy(game.bp, numShares);}
            else{commitSell(game.bp, numShares);}
                break;
            case 'C': if(select==1){commitBuy(game.cisco, numShares);}
            else{commitSell(game.cisco, numShares);}
                break;
            case 'D': if(select==1){commitBuy(game.dell, numShares);}
            else{commitSell(game.dell, numShares);}
                break;
            case 'E': if(select==1){commitBuy(game.ericsson, numShares);}
            else{commitSell(game.ericsson, numShares);}
                break;
            default:
                out.println("Invalid input");
                command();
                break;
        }
    }


    /**
     * This method lets the player purchase stock. It first calculates whether or not the player can afford the transaction
     * and then asks for confirmation before the purchase. After this, the players funds are deducted accordingly (with the
     * £3 transaction charge applied for each share bought) and the player is brought back to the main command menu.
     * If a player does not have enough funds for a purchase, they will be brought back to the trade menu.
     *
     * @param stock represents the stock object the player wishes to purchase (e.g. Apple).
     * @param numShares the number of shares the player wishes to purchase.
     */
    private void commitBuy(Stock stock, int numShares){
        out.println("\r\nBuying " + numShares + " " + stock.getName() + " shares for £" + stock.getSharePrice() + " each." );
        if(player.getFunds()-numShares*(stock.getSharePrice()+3) >= 0){
            out.println("\r\nFunds after transaction: £" + (player.getFunds()-numShares*(stock.getSharePrice()+3)));
            out.println("\r\nConfirm purchase? (Y/N)");
            String bConfirmInput = in.nextLine();
            if (bConfirmInput.toUpperCase().equals("Y")){
                player.getShares().set(game.getIndex(stock), player.getShares().get(game.getIndex(stock)) + numShares);
                player.setFunds(-numShares*(stock.getSharePrice()+3));
                player.deductTradesLeft();
                out.println("Purchase complete.\r\nCurrent stats: " + player.toString() + "\r\n");
                display();
                command();
            }else if(bConfirmInput.toUpperCase().equals("N")){
                out.println("Purchase cancelled.");
                getTradeParam(1);
            }
        }else{
            out.println("\r\nInsufficient funds. Purchase cancelled.");
            getTradeParam(1);
        }
    }


    /**
     * This method lets the player sell stock. It will display the players potential funds after the transaction and ask
     * for confirmation. After this, this players funds are updated accordingly and the player is brought back to the
     * main command menu.
     * A player cannot attempt to sell more shares then they own.
     *
     * @param stock represents the stock object the player wishes to sell.
     * @param numShares the number of shares the player wishes to sell.
     */
    private void commitSell(Stock stock, int numShares){
        if(numShares <= player.getShares().get(game.getIndex(stock))){
            out.println("Selling " + numShares + " " + stock.getName() + " shares for £" + stock.getSharePrice() + " each.");
            out.println("Funds after transaction: £" + (player.getFunds()+numShares*stock.getSharePrice()));
            out.println("\r\nConfirm sale? (Y/N)");
            String sConfirmInput = in.nextLine();
            if(sConfirmInput.toUpperCase().equals("Y")){
                player.getShares().set(game.getIndex(stock), player.getShares().get(game.getIndex(stock)) - numShares);
                player.setFunds(numShares*stock.getSharePrice());
                player.deductTradesLeft();
                out.println("Sale complete.");
                display();
                command();
            }else if (sConfirmInput.toUpperCase().equals("N")){
                out.println("Sale cancelled.");
                getTradeParam(0);
            }
        }else{
            out.println("You do not own that many shares in this stock");
            getTradeParam(0);
        }
    }



    /**
     * This method displays the winner(s) by sorting the playerList by highest player worth and listing the top player(s).
     * A call to the updatePlayersWorth method is made to keep the scores accurate.
     */
    private void endGame(){
        synchronized (lock){
            out.println("\r\n###GAME OVER###\r\n");
            out.println("The winner(s) are:");
            updatePlayersWorth();
            Collections.sort(playerList);
            out.println(playerList.get(0).getName() + ": £" + playerList.get(0).getPlayerWorth());
            for (int i = 1; i < playerList.size(); i++) {
                if(playerList.get(0).getPlayerWorth() == playerList.get(i).getPlayerWorth()){
                    out.println(playerList.get(i).getName() + ": £" + playerList.get(i).getPlayerWorth());
                }
            }
        }
    }


    /**
     * Updates the playerWorth variable for every player by calling the getTotalShareValue method and adding the players
     * funds to the variable.
     */
    private void updatePlayersWorth(){
        for (Player e : playerList) {
            e.setPlayerWorth(getTotalShareValue(e) + e.getFunds());
        }
    }


    /**
     * This method calculates the value of a players shares.
     *
     * @param player the player who's shares are being calculated.
     * @return an integer value representing the total value of a players shares.
     */
    private int getTotalShareValue(Player player){
        return player.getShares().get(0) * game.apple.getSharePrice() + player.getShares().get(1) * game.bp.getSharePrice() +
                player.getShares().get(2) * game.cisco.getSharePrice() + player.getShares().get(3) * game.dell.getSharePrice() +
                player.getShares().get(4) * game.ericsson.getSharePrice();
    }

    /**
     * The final method that is called before the game finishes.
     * The login variable becomes false and the run loop exits.
     */
    private void exitGame(){
        out.println("\r\nPress ENTER to finish");
        in.nextLine();
//        game.playersConnected--;
        login = false;
    }


    /**
     * Displays all the stocks with their share prices and current influence card.
     * Also shows all the players stats.
     */
    private void display(){
        out.println("\n-STOCK EXCHANGE-");
        out.println("Apple    - Share Price: " + game.apple.getSharePrice() + " [" + game.apple.top() + "]");
        out.println("BP       - Share Price: " + game.bp.getSharePrice() + " [" + game.bp.top() + "]");
        out.println("Cisco    - Share Price: " + game.cisco.getSharePrice() + " [" + game.cisco.top() + "]");
        out.println("Dell     - Share Price: " + game.dell.getSharePrice() + " [" + game.dell.top() + "]");
        out.println("Ericsson - Share Price: " + game.ericsson.getSharePrice() + " [" + game.ericsson.top() + "]");

        out.println("\n-PLAYERS-");
//        System.out.println(playerList.toString());
        for (Player e : playerList) {
            if (e.equals(player)) {
                out.println(e.toString() + " (you)");
            } else {
                out.println(e.toString());
            }
        }
    }


    //######################## BOT LOGIC ########################

    /**
     * This method first sorts the playerList so that it is easier to find the top player.
     * It then calls the botTrading and botVoting methods and waits for the other players to finish their turns.
     */
    private void botLogic(){
        synchronized (lock){Collections.sort(playerList);}
        botTrading();
        botVoting();
        waitPrint();
        await();
    }



    /**
     * This method looks for the most valuable stock and then checks if its influence card is positive. If it is positive
     * the bot purchases the max number of shares it can.
     */
    private void botTrading(){
        if(game.isCardPositive(game.getHighestSharePriceStock())){
            int numShares = player.getFunds()/(game.getHighestSharePriceStock().getSharePrice()+3);
            player.getShares().set(game.getIndex(game.getHighestSharePriceStock()), player.getShares().get(game.getIndex(game.getHighestSharePriceStock())) + numShares);
            player.setFunds(-numShares*(game.getHighestSharePriceStock().getSharePrice()+3));
        }
    }


    /**
     * Allows the bot to purchase stocks. The bots funds are deducted accordingly.
     *
     * @param stock the desired stock to purchase.
     * @param numShares the number of shares to purchase.
     */
    private void botBuy(Stock stock, int numShares){
        player.getShares().set(game.getIndex(stock), player.getShares().get(game.getIndex(stock)) + numShares);
        player.setFunds(-numShares*(stock.getSharePrice()+3));
        System.out.println("purchase made"); //rem
    }


    /**
     * Gets the stocks of the top players most owned shares as well as the bots most owned shares and casts two votes.
     * The first vote aims to lessen the value of the enemy players shares. This is done by judging whether the influence
     * card for the players stock is positive or negative and then casting a vote to disrupt the player.
     * The second vote aims to increase the value of the bots own shares.
     * If both the bot and the top players most owned shares come from the same stock, the bot does not vote.
     */
    private void botVoting(){
        int stockPos;
        int myStockPos = getMostShares(playerList.lastIndexOf(player));
        Collections.sort(playerList);
        if(playerList.get(0) == player){ //if i am the leader
            stockPos = getMostShares(1); //get the second players info
        }else{
            stockPos = getMostShares(0); //else get the first players info
        }

        //if my most shares are the same as other players most shares, don't vote.
        if(game.getStockByIndex(stockPos) != game.getStockByIndex(myStockPos)){
            //offensive play against leader
            if(game.isCardPositive(game.getStockByIndex(stockPos))){
                game.getStockByIndex(stockPos).vote(0);
                player.getVotedStocks().append(game.getStockByIndex(stockPos).getName().charAt(0));
                player.deductVotesLeft();
                System.out.println("bot voted NO for " + game.getStockByIndex(stockPos).getName() );
            }else{
                game.getStockByIndex(stockPos).vote(1);
                player.getVotedStocks().append(game.getStockByIndex(stockPos).getName().charAt(0));
                player.deductVotesLeft();
                System.out.println("bot voted YES for " + game.getStockByIndex(stockPos).getName());
            }
            //defensive play, votes that will benefit me
            if(game.isCardPositive(game.getStockByIndex(myStockPos))){
                game.getStockByIndex(myStockPos).vote(1);
                player.getVotedStocks().append(game.getStockByIndex(myStockPos).getName().charAt(0));
                player.deductVotesLeft();
                System.out.println("bot voted YES for " + game.getStockByIndex(myStockPos).getName());
            }else{
                game.getStockByIndex(myStockPos).vote(0);
                player.getVotedStocks().append(game.getStockByIndex(myStockPos).getName().charAt(0));
                player.deductVotesLeft();
                System.out.println("bot voted NO for " + game.getStockByIndex(myStockPos).getName());
            }
        }
    }


    /**
     * This method gets the index position of the players most owned shares.
     *
     * @param start the player leader board position. E.g. 0 returns the.
     * @return the index position of a players stock with the most shares.
     */
    private int getMostShares(int start){
        int mostShares = 0;
        int index = 0;
        for (int i = 0; i < player.getShares().size(); i++) {
            if(mostShares < playerList.get(start).getShares().get(i)){
                mostShares = playerList.get(start).getShares().get(i);
                index = i;
            }
        }
        return index;
    }



}
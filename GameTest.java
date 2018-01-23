package Assignment;

import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;

import static org.junit.Assert.*;

/**
 * Created by Alvin on 29/11/2017.
 */
public class GameTest {
    Game game;

    @Before
    public void setUp() throws Exception {
        game = new Game();
    }

    @Test
    public void incrementPlayersConnected() throws Exception {
        game.incrementPlayersConnected();
        assertTrue(game.getPlayersConnected()==1);
    }

    @Test
    public void setBarrierNumOfParties() throws Exception {
        game.setBarrierNumOfParties(5);
        assertTrue(game.getBarrier().getParties()==5);
    }

    @Test
    public void addPlayer() throws Exception {
        game.addPlayer("Test");
        assertTrue(game.getPlayerList().get(0) != null);
        assertTrue(game.getPlayerList().get(0).getName() == "Test");
    }

    @Test
    public void addPlayerObject() throws Exception {
        Player test = new Player("Test");
        game.addPlayerObject(test);
        assertTrue(game.getPlayerList().get(0) != null);
        assertTrue(game.getPlayerList().get(0).getName() == "Test");
    }

    @Test
    public void applyCards() throws Exception {
        game.apple.vote(1);
        game.bp.vote(1);
        game.cisco.vote(0);
        game.dell.vote(0);
        game.ericsson.vote(1);
        game.ericsson.vote(0);

        game.applyCards();
        assertFalse(game.apple.getSharePrice() == 100);
        assertFalse(game.bp.getSharePrice() == 100);
        assertTrue(game.cisco.getSharePrice() == 100);
        assertTrue(game.dell.getSharePrice() == 100);
        assertTrue(game.ericsson.getSharePrice() == 100);


    }

    @Test
    public void isCardPositive() throws Exception {
        if(game.apple.top()>0){
            assertTrue(game.isCardPositive(game.apple));
        }else{
            assertFalse(game.isCardPositive(game.apple));
        }
    }

    @Test
    public void getHighestSharePriceStock() throws Exception {
        game.apple.setSharePrice(900);
        assertTrue(game.getHighestSharePriceStock() == game.apple);
    }

    @Test
    public void getStockByIndex() throws Exception {
        assertTrue(game.getStockByIndex(0) == game.apple);
    }

    @Test
    public void getIndex() throws Exception {
       assertTrue(game.getIndex(game.apple) == 0);
    }

}
package Assignment;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Alvin on 29/11/2017.
 */
public class PlayerTest {
    Player player;

    @Before
    public void setUp() throws Exception {
        player = new Player("Test");
    }

    @Test
    public void resetActions() throws Exception {
        player.deductVotesLeft();
        player.deductTradesLeft();
        player.resetActions();
        assertTrue(player.getTradesLeft() == 2);
        assertTrue(player.getVotesLeft() == 2);
    }

    @Test
    public void deductVotesLeft() throws Exception {
        player.deductVotesLeft();
        assertTrue(player.getVotesLeft() == 1);
    }

    @Test
    public void deductTradesLeft() throws Exception {
        player.deductTradesLeft();
        assertTrue(player.getTradesLeft() == 1);
    }

}
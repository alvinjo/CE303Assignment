package Assignment;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Alvin on 29/11/2017.
 */
public class StockTest {
    Stock stock;

    @Before
    public void setUp() throws Exception {
        stock = new Stock();
    }

    @Test
    public void resetVoteCount() throws Exception {
        stock.vote(1);
        stock.resetVoteCount();
        assertTrue(stock.getVoteCount()==0);
    }

    @Test
    public void vote() throws Exception {
        stock.vote(1);
        assertTrue(stock.getVoteCount()==1);
    }

}
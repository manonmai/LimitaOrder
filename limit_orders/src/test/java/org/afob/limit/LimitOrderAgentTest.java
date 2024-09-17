package org.afob.limit;

import org.junit.Assert;
import org.junit.Test;
import org.afob.execution.ExecutionClient;
import org.junit.Before;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;

public class LimitOrderAgentTest {
	private ExecutionClient executionClient;
    private LimitOrderAgent limitOrderAgent;
    
    @Before
    public void initialSetUp() {
        executionClient = Mockito.mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(executionClient);
    }
    @Test
    public void removeExecutedOrdersTest() throws ExecutionClient.ExecutionException {
        // Add an order and execute it
        limitOrderAgent.addOrder(true, "IBM", 300, new BigDecimal("300"));

        // Simulate a price tick that meets the limit
        limitOrderAgent.priceTick("IBM", new BigDecimal("300"));

        // Verify that the buy method is called
        Mockito.verify(executionClient).buy("IBM", 300);

        // Simulate another price tick for the same product
        limitOrderAgent.priceTick("IBM", new BigDecimal("300"));

        // Verify that the buy method is not called again (order should be removed)
        Mockito.verify(executionClient, times(1)).buy("IBM", 300);
        Assert.assertEquals(limitOrderAgent.orders.size(), 1);
        Assert.assertNotEquals("IBM", limitOrderAgent.orders.get(0).getProductId()); // only has default "IMB" productId
    }
    
    @Test
    public void defaultOrderTest() throws ExecutionClient.ExecutionException {
        // Simulate a price tick below $100
        limitOrderAgent.priceTick("IBM", new BigDecimal("99.99"));
        // Verify that buy method is called with correct parameters
        Mockito.verify(executionClient).buy("IBM", 1000);
    }
    
    @Test
    public void NoExecutyionOfBuyOrderTest() throws ExecutionClient.ExecutionException {
        // Add an order with a limit price
        limitOrderAgent.addOrder(true, "GOOGLE", 200, new BigDecimal("2500"));
        // Simulate a price tick that meets the limit
        limitOrderAgent.priceTick("GOOGLE", new BigDecimal("2600"));
        // Verify that the buy method is not called
        Mockito.verify(executionClient, times(0)).buy("GOOGLE", 2600);
    }

    @Test
    public void executeBuyOrderTest() throws ExecutionClient.ExecutionException {
        // Add a custom order
        limitOrderAgent.addOrder(true, "MAC", 500, new BigDecimal("150"));
        // Simulate a price tick that should trigger the order
        limitOrderAgent.priceTick("MAC", new BigDecimal("149.99"));
        // Verify that buy method is called for AAPL
        Mockito.verify(executionClient).buy("MAC", 500);
    }
}

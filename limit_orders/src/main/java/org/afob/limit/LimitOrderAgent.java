package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {
	
	 private final ExecutionClient executionClient;
	 List<Orders> orders = new ArrayList<>();
	 
	 /**
	     * Method to add a new limit order.
	     *
	     * @param isBuy      true for buy orders, false for sell orders
	     * @param productId  product id
	     * @param amount     amount to buy or sell
	     * @param limitPrice price at which order is executed
	     */
	 public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
		 Orders order = new Orders(isBuy, productId, amount, limitPrice);
		 orders.add(order);
		 }
	 
	 
	 public LimitOrderAgent(final ExecutionClient ec) {
		 this.executionClient = ec;
        // Automatically add a buy order for 1000 shares of IBM when price drops below $100
        addOrder(true, "IBM", 1000, new BigDecimal("100"));
    }
	 
    @Override
    public void priceTick(String productId, BigDecimal price) {
    	
    	 Iterator<Orders> iterator = orders.iterator();
         // Iterate through all added orders and execute if conditions are met
         while (iterator.hasNext()) {
             Orders order = iterator.next();
             if (order.getProductId().equals(productId)) {
                 boolean shouldExecute = (order.isBuy() && price.compareTo(order.getLimitPrice()) <= 0) ||
                         (!order.isBuy() && price.compareTo(order.getLimitPrice()) >= 0);
                 if (shouldExecute) {
                     try {
                         if (order.isBuy()) {
                             executionClient.buy(order.getProductId(), order.getAmount());
                         } else {
                             executionClient.sell(order.getProductId(), order.getAmount());
                         }
                         System.out.println("Executed order: " + order);
                         // Remove executed orders from the original list
                         iterator.remove();
                     } catch (ExecutionClient.ExecutionException e) {
                         System.err.println("Failed to execute order " + order + ": " + e.getMessage());
                     }
                 }
             }
         }
    }

}

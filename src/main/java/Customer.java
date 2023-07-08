import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Customer {

    private ElectronicStore electronicStore;

    private static int newCustomerId = 0;
    private int customerId;
    private Map<Integer, Integer> basket; // key = productId, value = amount

    public Customer(ElectronicStore electronicStore){
        this.electronicStore = electronicStore;
        this.customerId = newCustomerId++;
        this.basket = new ConcurrentHashMap<>();
        this.electronicStore.addCustomer(this);
    }

    public int addProductToBasket(String productName, int amount){
        if (null == getProductIdMap(productName)){
            return -1;
        }
        Product product = getProductByIDInStore(getProductIdMap(productName)).getKey();
        if (null == product) {
            return -1;
        }
        return addProductToBasket(getStoreInventory().get(getProductIdMap(productName)).getKey(), amount);
    }


    public synchronized int addProductToBasket(Product product, int amount) {
        Pair<Product, Integer> productAmountPair = getStoreInventory().get(product.getId());
        if (null == productAmountPair){
            return -1;
        }
        int remainingStoreInventory = productAmountPair.getValue();
        if (remainingStoreInventory < amount){
            System.out.println("Store does not have enough inventory for product " + product.getName() + ". Store only has " + remainingStoreInventory + " amount, while you wanted " + amount + ".");
            return -1;
        }

        getStoreInventory().put(product.getId(), new Pair(product, remainingStoreInventory - amount));  // also remove from store inventory
        basket.put(product.getId(), amount + basket.computeIfAbsent(product.getId(), k -> 0));
        return basket.get(product.getId());
    }

    public int removeAllUnitsOfSingleProduct(String productName) {
        if (null == getProductIdMap(productName)){
            return -1;
        }
        int productId = getProductIdMap(productName);
        if (basket.containsKey(productId))
            return basket.remove(getProductIdMap(productName));
        else
            return 0;
    }

    public int removeProductFromBasket(String productName, int amount) {
        if (null == getProductIdMap(productName)){
            return -1;
        }
        Product product = getProductByIDInStore(getProductIdMap(productName)).getKey();
//        Product product = getStoreInventory().get(getProductIdMap(productName)).getKey();
        if (null == product) {
            return -1;
        }
        return removeProductFromBasket(getStoreInventory().get(getProductIdMap(productName)).getKey(), amount);
    }


    public synchronized int removeProductFromBasket(Product product, int amount) {
        if (null == basket.get(product.getId())) {
            return -1;
        }

        if (amount > basket.get(product.getId())){
            return -1;
        }

        int existingInventoryInStore = getProductByIDInStore(product.getId()).getValue();
        getStoreInventory().put(product.getId(), new Pair(product, amount + existingInventoryInStore));  // also add back to store inventory
        basket.put(product.getId(), basket.get(product.getId()) - amount);
        return basket.get(product.getId());
    }

    public synchronized BigDecimal calculateBasketTotalPrice(){
        System.out.println("Starting to calculate receipt of item for customer " + customerId);
        this.basket.entrySet().forEach(entry -> {

            if (!getStoreInventory().containsKey(entry.getKey())){
                return;
            }
            Product product = getStoreInventory().get(entry.getKey()).getKey();
            double itemPrice = product.getPrice().doubleValue();
            String productName = product.getName();

            System.out.println(productName + ": " + entry.getValue() + " units. Price: " + itemPrice);
        });
        BigDecimal totalPrice = BigDecimal.valueOf(this.basket.entrySet().stream().mapToDouble( entry -> calculatePrice(entry.getKey(), entry.getValue()).doubleValue()).sum()); // TODO: refactor this
        System.out.println("total price for this purchase is " + totalPrice);
        return totalPrice;
    }

    private synchronized BigDecimal calculatePrice(int productId, int amount) {

        if (electronicStore.getDiscounts().containsKey(productId)){
            return electronicStore.getDiscounts().get(productId).apply(new Pair(productId, amount));
        }

        BigDecimal productPrice = electronicStore.getInventory().get(productId).getKey().getPrice();
        return productPrice.multiply(BigDecimal.valueOf(amount));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return (customerId);
    }

    public Map<Integer, Pair<Product, Integer>> getStoreInventory() {
        return electronicStore.getInventory();
    }

    public Pair<Product, Integer> getProductByIDInStore(int productId) {
        return electronicStore.getInventory().get(productId);
    }

    public Integer getProductIdMap(String productName) {
        return electronicStore.getProductIdMap().get(productName);
    }

    public Map<Integer, Function<Pair<Integer, Integer>, BigDecimal>> getDiscounts() {
        return electronicStore.getDiscounts();
    }

    public void resetBasket(){
        this.basket.clear();
    }
}

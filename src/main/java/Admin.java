import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class Admin {

    private ElectronicStore electronicStore;

    public Admin(ElectronicStore electronicStore){
        this.electronicStore = electronicStore;
    }


    public synchronized int createNewProduct(String productName, int amount, BigDecimal price) {
        if (null != getProductId(productName)){
            System.out.println("product '" + productName + "' already exist in productIdMap, please retry with different name");
            return -1;
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0){
            System.out.println("You specified price as 0 or less than 0, please try again");
            return -1;
        }
        if (amount < 0){
            System.out.println("You specified amount as 0 or less than 0, please try again");
            return -1;
        }
        Product newProduct = new Product(productName, price);
        int productId = getProductIdMap().computeIfAbsent(productName, k -> newProduct.getId());
        getInventory().putIfAbsent(productId, new Pair(newProduct, amount));
        return getInventory().get(productId).getValue();
    }

    public synchronized int addProductQuantity(String productName, int amount) {
        if (null == getProductId(productName)){
            System.out.println("product '" + productName + "' does not exist yet in productIdMap, please create new product first");
            return -1;
        }
        if (amount <= 0){
            System.out.println("You specified amount as 0 or less than 0, please try again");
            return -1;
        }
        int productId = getProductId(productName);

        int existingAmountInInventory = getInventoryByProductId(productId).getValue();
        getInventory().put(productId, new Pair(productId, amount + existingAmountInInventory));
        return getInventory().get(productId).getValue();
    }

    public synchronized int removeProduct(String name) {

        if (null == getProductIdMap().get(name)){
            return -1;
        }
        int productId = getProductIdMap().get(name);
        getCustomers().forEach(customer -> customer.removeAllUnitsOfSingleProduct(name));
        getInventory().remove(productId);
        getProductIdMap().remove(name);
        System.out.println("Successfully removed " + name + " from store inventory and from customers' baskets");
        return 0;
    }

    public synchronized void addDiscountDeal(String productName, Function discount){
        getDiscounts().put(getProductId(productName), discount);
    }

    public synchronized void addDiscountDeal(int productId, Function discount){
        getDiscounts().put(productId, discount);
    }

    public Map<Integer, Pair<Product, Integer>> getInventory() {
        return electronicStore.getInventory();
    }

    public Pair<Product, Integer> getInventoryByProductId(int productId) {
        return electronicStore.getInventory().get(productId);
    }

    public Map<String, Integer> getProductIdMap() {
        return electronicStore.getProductIdMap();
    }

    public Integer getProductId(String name) {
        return electronicStore.getProductIdMap().get(name);
    }

    public Map<Integer, Function<Pair<Integer, Integer>, BigDecimal>> getDiscounts() {
        return electronicStore.getDiscounts();
    }

    public ArrayList<Customer> getCustomers(){
        return electronicStore.getCustomers();
    }
}

import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ElectronicStore {

    private Map<String, Integer> productIdMap;
    private Map<Integer, Pair<Product, Integer>> inventory; // Map<ProductID, Pair<Product, Amount>>
    private Map<Integer, Function<Pair<Integer, Integer>, BigDecimal>> discounts;

    private ArrayList<Customer> customers;

    public ElectronicStore() {
        inventory = new HashMap<Integer, Pair<Product, Integer>>();
        discounts = new HashMap<>();
        productIdMap = new HashMap<>();
        Collections.synchronizedMap(inventory);
        Collections.synchronizedMap(discounts);
        Collections.synchronizedMap(productIdMap);
        customers = new ArrayList<>();
    }

    public Map<String, Integer> getProductIdMap() {
        return productIdMap;
    }

    public Map<Integer, Pair<Product, Integer>> getInventory() {
        return inventory;
    }

    public Map<Integer, Function<Pair<Integer, Integer>, BigDecimal>> getDiscounts() {
        return discounts;
    }

    public void reset(){
        this.inventory.clear();
        this.productIdMap.clear();
        this.discounts.clear();
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }

    public ArrayList<Customer> getCustomers(){
        return this.customers;
    }
}

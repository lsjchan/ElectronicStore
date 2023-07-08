import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.function.Function;

public class RunApplication {

    // Run simple case
    public static void main(String[] args) {
        ElectronicStore electronicStore = new ElectronicStore();
        Admin adminUser = new Admin(electronicStore);
        Customer customer1 = new Customer(electronicStore);

        // discount 1 applied
        Function<Pair<Integer, Integer>, BigDecimal> discount1 = productIdAmountPair -> {

            int productId = productIdAmountPair.getKey();
            int amount = productIdAmountPair.getValue();
            System.out.println("Applying discount #1: Buy 1 get 50% off the second to product '" + electronicStore.getInventory().get(productId).getKey().getName() + "'");
            BigDecimal productPrice = electronicStore.getInventory().get(productId).getKey().getPrice();
            int halfAmount = amount / 2;
            BigDecimal normalTotalPrice = productPrice.multiply(new BigDecimal(halfAmount));
            BigDecimal discountedTotalPrice = productPrice.divide(new BigDecimal(2)).multiply(new BigDecimal(halfAmount));
            if (amount % 2 == 0)
                return normalTotalPrice.add(discountedTotalPrice);
            else if (amount % 1 == 0)
                return normalTotalPrice.add(discountedTotalPrice).add(productPrice);
            else
                return null;
        };


        String BANANA_STR = "banana";
        int productQty = adminUser.createNewProduct(BANANA_STR, 90, new BigDecimal(15));
        System.out.println("admin has created " + productQty + " bananas in the store");

        int customer1RemainingBananas = customer1.addProductToBasket(BANANA_STR, 50);
        System.out.println("there are " + customer1RemainingBananas + " bananas remaining for customer 1");

        int storeBananaInventory = adminUser.getInventory().get(adminUser.getProductId(BANANA_STR)).getValue();
        System.out.println("there are " + storeBananaInventory + " bananas remaining for customer 1");

        int failedResult1 = customer1.removeProductFromBasket("strawberry", 90);
        if (failedResult1 == -1){
            System.out.println("customer 1 has failed to remove strawberry that does not exist");
        }

        int failedResult2 = customer1.removeProductFromBasket(BANANA_STR, 90);
        if (failedResult2 == -1){
            System.out.println("customer 1 has failed to remove 90 bananas");
        }

        int bananasRemaining2 = customer1.removeProductFromBasket(BANANA_STR, 40);
        System.out.println("customer 1 has got " + bananasRemaining2 + " bananas after removing 40 of them");

        int storeBananaInventory2 = adminUser.getInventory().get(adminUser.getProductId(BANANA_STR)).getValue();
        System.out.println("store has now got " + storeBananaInventory2 + " bananas");

        customer1.calculateBasketTotalPrice();

        adminUser.addDiscountDeal(BANANA_STR, discount1);

        customer1.calculateBasketTotalPrice();

    }
}

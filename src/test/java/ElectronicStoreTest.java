import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.Function;

public class ElectronicStoreTest {

    private final static String BANANA_STR = "banana";
    
    private ElectronicStore electronicStore;
    private Admin adminUser;
    private Customer customer1;
    private Customer customer2;

    private Function<Pair<Integer, Integer>, BigDecimal> discount1; // Function<Pair<productId,amount>, discountedPrice>

    @Before
    public void setUp(){
        this.electronicStore = new ElectronicStore();
        this.adminUser = new Admin(electronicStore);
        this.customer1 = new Customer(electronicStore);
        this.customer2 = new Customer(electronicStore);

        // Add discount deal for banana (Buy 1 get 50% off the second) e.g. buy 3 bananas at $15 each, total=15+7.5+15=$37.5
        // Function<Pair<productId,amount>, discountedPrice>
        discount1 = productIdAmountPair -> {

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
    }

    @Test
    public void testAdminCreateProduct_ThenCustomerPutProductToBasket(){
        adminUser.createNewProduct(BANANA_STR, 8, new BigDecimal(15));
//        adminUser.createNewProduct(BANANA, 8, new BigDecimal(15));

        int bananaProductId = electronicStore.getProductIdMap().get(BANANA_STR);
        int amountOfBananasInStore = electronicStore.getInventory().get(bananaProductId).getValue();
        Assert.assertEquals(8, amountOfBananasInStore);

        int failedResult = customer1.addProductToBasket(BANANA_STR, 11);
        Assert.assertEquals(-1, failedResult);

        int amountOfBananasByCustomer1 = customer1.addProductToBasket(BANANA_STR, 3);
        Assert.assertEquals(3, amountOfBananasByCustomer1);

        BigDecimal bananasTotalPrice = customer1.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(45.0), bananasTotalPrice);
    }

    @Test
    public void testAdminCreateProductAndDiscount_ThenCustomerPutProductToBasket(){
        adminUser.createNewProduct(BANANA_STR, 8, new BigDecimal(15));
        adminUser.addDiscountDeal(BANANA_STR, discount1);

        customer1.addProductToBasket(BANANA_STR, 3);

        BigDecimal bananasTotalPrice = customer1.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(37.5), bananasTotalPrice);
    }

    @Test
    public void testAdminCreateSameProductTwiceButFailed(){
        int productQty = adminUser.createNewProduct(BANANA_STR, 8, new BigDecimal(15));
        Assert.assertEquals(8, productQty);

        int failedResult = adminUser.createNewProduct(BANANA_STR, 80, new BigDecimal(20));
        Assert.assertEquals(-1, failedResult);
    }


    @Test
    public void testAdminCreateProductAndThenAddQuantity(){
        int productQty = adminUser.createNewProduct(BANANA_STR, 0, new BigDecimal(15));
        Assert.assertEquals(0, productQty);

        int failedResult = adminUser.addProductQuantity(BANANA_STR, 0);
        Assert.assertEquals(-1, failedResult);

        int productQty1stAdd = adminUser.addProductQuantity(BANANA_STR, 40);
        Assert.assertEquals(40, productQty1stAdd);

        int productQty2stAdd = adminUser.addProductQuantity(BANANA_STR, 75);
        Assert.assertEquals(115, productQty2stAdd);

    }


    @Test
    public void testAdminCreateProductAndThenRemoveProduct(){
        int productQty = adminUser.createNewProduct(BANANA_STR, 90, new BigDecimal(15));
        Assert.assertEquals(90, productQty);

        int failedResult = adminUser.removeProduct("strawberry");
        Assert.assertEquals(-1, failedResult);

        int successResult = adminUser.removeProduct(BANANA_STR);
        Assert.assertEquals(0, successResult);

    }

    @Test
    public void testAdminCreateProductThenRemoveProduct_ThenUserTriesToAddToBasket(){
        int productQty = adminUser.createNewProduct(BANANA_STR, 90, new BigDecimal(15));
        Assert.assertEquals(90, productQty);

        int successResult = adminUser.removeProduct(BANANA_STR);
        Assert.assertEquals(0, successResult);

        int failedResult = customer1.addProductToBasket(BANANA_STR, 1);
        Assert.assertEquals(-1, failedResult);

    }

    @Test
    public void testAdminCreateProduct_ThenUserAddAndRemoveFromBasket_ThenAdminAppliesDiscount(){
        int productQty = adminUser.createNewProduct(BANANA_STR, 90, new BigDecimal(15));
        Assert.assertEquals(90, productQty);
        
        int customer1RemainingBananas = customer1.addProductToBasket(BANANA_STR, 50);
        Assert.assertEquals(50, customer1RemainingBananas);

        int storeBananaInventory = adminUser.getInventory().get(adminUser.getProductId(BANANA_STR)).getValue();
        Assert.assertEquals(40, storeBananaInventory);
        
        int failedResult1 = customer1.removeProductFromBasket("strawberry", 90);
        Assert.assertEquals(-1, failedResult1);

        int failedResult2 = customer1.removeProductFromBasket(BANANA_STR, 90);
        Assert.assertEquals(-1, failedResult2);

        int bananasRemaining2 = customer1.removeProductFromBasket(BANANA_STR, 40);
        Assert.assertEquals(10, bananasRemaining2);

        int storeBananaInventory2 = adminUser.getInventory().get(adminUser.getProductId(BANANA_STR)).getValue();
        Assert.assertEquals(80, storeBananaInventory2);

        BigDecimal cust1BasketTotalPrice1 = customer1.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(150.0), cust1BasketTotalPrice1);

        adminUser.addDiscountDeal(BANANA_STR, discount1);

        BigDecimal cust1BasketTotalPrice2 = customer1.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(112.5), cust1BasketTotalPrice2);
    }


    @Test
    public void testAdminCreateProduct_ThenTwoCustomersAddAndRemoveFromBasket(){
        int productQty = adminUser.createNewProduct(BANANA_STR, 90, new BigDecimal(15));
        Assert.assertEquals(90, productQty);

        int customer1RemainingBananas = customer1.addProductToBasket(BANANA_STR, 50);
        Assert.assertEquals(50, customer1RemainingBananas);

        int failedResult1 = customer2.addProductToBasket(BANANA_STR, 50);
        Assert.assertEquals(-1, failedResult1);

        int customer1RemainingBananas2 = customer1.removeProductFromBasket(BANANA_STR, 30);
        Assert.assertEquals(20, customer1RemainingBananas2);

        int customer2RemainingBananas2 = customer2.addProductToBasket(BANANA_STR, 50);
        Assert.assertEquals(50, customer2RemainingBananas2);

        BigDecimal customer1BasketTotalPrice = customer1.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(300.0), customer1BasketTotalPrice);

        BigDecimal customer2BasketTotalPrice = customer2.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(750.0), customer2BasketTotalPrice);

        adminUser.removeProduct(BANANA_STR);
        int failedResult2 = customer1.addProductToBasket(BANANA_STR, 20);
        Assert.assertEquals(-1, failedResult2);

        BigDecimal customer1BasketTotalPrice2 = customer1.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(0.0), customer1BasketTotalPrice2);

        BigDecimal customer2BasketTotalPrice2 = customer2.calculateBasketTotalPrice();
        Assert.assertEquals(BigDecimal.valueOf(0.0), customer2BasketTotalPrice2);
    }

}

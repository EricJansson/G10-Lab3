import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class AccountTest {

    @Test
    void testGetMaxOverdrawn() {
        Account myTestAccount = new Account(BigDecimal.ZERO, "SEK", BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, myTestAccount.getMaxOverdrawn());

        Account myTestAccount2 = new Account(BigDecimal.ZERO, "SEK", new BigDecimal(-1));
        assertEquals(BigDecimal.ZERO, myTestAccount2.getMaxOverdrawn()); //max_overdrawn must be non-negative

        Account myTestAccount3 = new Account(BigDecimal.ZERO, "SEK", new BigDecimal(1000));
        assertEquals(new BigDecimal(1000), myTestAccount3.getMaxOverdrawn());
    }

    @Test
    void testSetMaxOverdrawn() {
        Account myTestAccount = new Account(BigDecimal.ZERO, "SEK", BigDecimal.ZERO);

        myTestAccount.setMaxOverdrawn(new BigDecimal(-1));
        assertEquals(BigDecimal.ZERO, myTestAccount.getMaxOverdrawn()); //max_overdrawn must be non-negative

        myTestAccount.setMaxOverdrawn(new BigDecimal(100));
        assertEquals(new BigDecimal(100), myTestAccount.getMaxOverdrawn()); //max_overdrawn must be non-negative
    }

    @Test
    void testGetCurrency() {
        Account myTestAccount = new Account(BigDecimal.ZERO,  "SEK", BigDecimal.ZERO);
        assertEquals("SEK", myTestAccount.getCurrency());

        myTestAccount = new Account(BigDecimal.ZERO,  "EUR", BigDecimal.ZERO);
        assertEquals("EUR", myTestAccount.getCurrency());

        myTestAccount = new Account(BigDecimal.ZERO,  "USD", BigDecimal.ZERO);
        assertEquals("USD", myTestAccount.getCurrency());
    }

    @Test
    void testSetCurrency() {
        Account myTestAccount = new Account(BigDecimal.ZERO, "SEK", BigDecimal.ZERO);
        myTestAccount.setCurrency("EUR");
        assertEquals("EUR", myTestAccount.getCurrency());

        myTestAccount.setCurrency("SEK");
        assertEquals("SEK", myTestAccount.getCurrency());
    }

    @Test
    void testGetBalance() {
        Account myTestAccount = new Account(BigDecimal.ZERO, "SEK", BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, myTestAccount.getBalance());

        myTestAccount = new Account(new BigDecimal(100), "SEK", BigDecimal.ZERO);
        assertEquals(new BigDecimal(100), myTestAccount.getBalance());
    }

    @Test
    void testSetBalance() {
        Account myTestAccount = new Account(BigDecimal.ZERO, "SEK", BigDecimal.ONE);

        //should not be allowed to set balance to lower that -1 * maxOverdrawn
        myTestAccount.setBalance(new BigDecimal(-2));
        assertEquals(BigDecimal.ZERO, myTestAccount.getBalance());

        myTestAccount.setBalance(new BigDecimal(42));
        assertEquals(new BigDecimal(42), myTestAccount.getBalance());
    }

    @Test
    void testWithdraw() {
        Account myTestAccount = new Account(new BigDecimal(100), "SEK", BigDecimal.ZERO);
        // Withdrawing 100 SEK with a balance of 100 SEK should result in 0 SEK left.
        assertEquals(BigDecimal.ZERO, myTestAccount.withdraw(new BigDecimal(100)));

        Account myTestAccount2 = new Account(new BigDecimal(10), "SEK", new BigDecimal(10));
        // It should not be possible to withdraw a negative amount. The balance should keep its original amount
        assertEquals(new BigDecimal(10), myTestAccount2.withdraw(new BigDecimal(-20)));

        Account myTestAccount3 = new Account(new BigDecimal(100), "SEK", new BigDecimal(-100));
        // Withdrawing 0 shouldn't change the balance
        assertEquals(new BigDecimal(100), myTestAccount3.withdraw(BigDecimal.ZERO));

        Account myTestAccount4 = new Account(new BigDecimal(1000), "SEK", new BigDecimal(200));
        // Withdrawing more than the balance (but within overdrawn range) should end up with negative value
        assertEquals(new BigDecimal(-100), myTestAccount4.withdraw(new BigDecimal(1100)));

        Account myTestAccount5 = new Account(new BigDecimal(1000), "USD", new BigDecimal(200));
        // Withdrawing more than the balance+maxOverdrawn shouldn't be possible and result in the original balance
        assertEquals(new BigDecimal(1000), myTestAccount5.withdraw(new BigDecimal(1201)));
    }

    @Test
    void testDeposit() {
        Account myTestAccount = new Account(new BigDecimal(100), "SEK", BigDecimal.ZERO);
        // A deposit of 50 SEK with a balance of 100 SEK should result in 200 SEK.
        assertEquals(new BigDecimal(150), myTestAccount.deposit(new BigDecimal(50)));

        Account myTestAccount2 = new Account(new BigDecimal(-100), "SEK", new BigDecimal(150));
        // A low deposit with a negative balance should increase but remain negative.
        assertEquals(new BigDecimal(-80), myTestAccount2.deposit(new BigDecimal(20)));

        Account myTestAccount3 = new Account(new BigDecimal(2000), "SEK", BigDecimal.ZERO);
        // A negative deposit shouldn't be possible and keep the original balance
        assertEquals(new BigDecimal(2000), myTestAccount3.deposit(new BigDecimal(-500)));
    }

    @Test
    void testConvertToCurrency() {
        Account myTestAccount = new Account(new BigDecimal(2000.0), "USD", new BigDecimal(100));
        // A normal conversion from USD to SEK (with decimal) should result in a tenfold increase in the balance and the overdrawn amount. From 2000 USD to 20000 SEK
        assertTrue(myTestAccount.convertToCurrency("SEK", 10.0));
        assertEquals(new BigDecimal(20000), myTestAccount.getBalance());
        assertEquals(new BigDecimal(1000), myTestAccount.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount.getCurrency());

        Account myTestAccount2 = new Account(new BigDecimal(-100), "EUR", new BigDecimal(200));
        // Negative initial balance should still result in a negative balance (max_overdrawn should be updated at the same rate to the new currency)
        myTestAccount2.convertToCurrency("SEK", 10.0);
        assertEquals(new BigDecimal(-1000), myTestAccount2.getBalance());
        assertEquals(new BigDecimal(2000), myTestAccount2.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount2.getCurrency());

        Account myTestAccount3 = new Account(new BigDecimal(50), "SEK", new BigDecimal(200));
        // Using a rate that is between 0-1 should decrease the balance and the overdrawn amount.
        myTestAccount3.convertToCurrency("USD", 0.1);
        assertEquals(new BigDecimal(5), myTestAccount3.getBalance());
        assertEquals(new BigDecimal(20), myTestAccount3.getMaxOverdrawn());
        assertEquals("USD", myTestAccount3.getCurrency());

        Account myTestAccount4 = new Account(new BigDecimal(10), "SEK", new BigDecimal(20));
        // Using a negative rate should not make the conversion possible and result in the original values.
        myTestAccount4.convertToCurrency("USD", -10.0);
        assertEquals(new BigDecimal(10), myTestAccount4.getBalance());
        assertEquals(new BigDecimal(20), myTestAccount4.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount4.getCurrency());
    }

    @Test
    void testTransferToAccount() {
        Account myTestAccountA = new Account(new BigDecimal(-100), "SEK", new BigDecimal(100));
        Account myTestAccountB = new Account(new BigDecimal(200), "SEK", new BigDecimal(10));
        // A transfer with negative funds should not be possible and result in the original values
        myTestAccountA.TransferToAccount(myTestAccountB);
        assertEquals(new BigDecimal(-100), myTestAccountA.getBalance());
        assertEquals(new BigDecimal(200), myTestAccountB.getBalance());

        Account myTestAccountA2 = new Account(new BigDecimal(10), "SEK", new BigDecimal(0));
        Account myTestAccountB2 = new Account(new BigDecimal(-200), "SEK", new BigDecimal(1000));
        // Transfering a low amount to a negative account should empty one account and increase the other, but stay negative.
        myTestAccountA2.TransferToAccount(myTestAccountB2);
        assertEquals(new BigDecimal(0), myTestAccountA2.getBalance());
        assertEquals(new BigDecimal(-190), myTestAccountB2.getBalance()); // Added 10 SEK

        Account myTestAccountA3 = new Account(new BigDecimal(100), "SEK", new BigDecimal(0));
        Account myTestAccountB3 = new Account(new BigDecimal(200), "SEK", new BigDecimal(10));
        Account myTestAccountC3 = new Account(new BigDecimal(1000), "SEK", new BigDecimal(10));
        // Multiple transfers from one account shouldn't be possible since it will empty the account during the first transfer.
        myTestAccountA3.TransferToAccount(myTestAccountB3); // Account B should now have 300 SEK
        assertEquals(new BigDecimal(0), myTestAccountA3.getBalance());
        myTestAccountA3.TransferToAccount(myTestAccountC3); // Account A should now be empty. A transfer of 0 funds shouldn't make any changes to the balances of the sender AND receiver
        assertEquals(new BigDecimal(0), myTestAccountA3.getBalance());
        assertEquals(new BigDecimal(300), myTestAccountB3.getBalance()); // Added 100
        assertEquals(new BigDecimal(1000), myTestAccountC3.getBalance()); // Unchanged

        Account myTestAccountA4 = new Account(new BigDecimal(10), "USD", new BigDecimal(100));
        Account myTestAccountB4 = new Account(new BigDecimal(20), "SEK", new BigDecimal(1000));
        // A transfer from to an account with a different currency shouldn't be possible. Should result in the original balance.
        myTestAccountA4.TransferToAccount(myTestAccountB4);
        assertEquals(new BigDecimal(10), myTestAccountA4.getBalance());
        assertEquals(new BigDecimal(20), myTestAccountB4.getBalance());
    }

    @Test
    void testWithdrawAll() {
        Account myTestAccount = new Account(new BigDecimal(-200), "SEK", new BigDecimal(1000));
        // Can't withdraw with negative balance
        assertEquals(new BigDecimal(-200), myTestAccount.withdrawAll());

        Account myTestAccount2 = new Account(new BigDecimal(200), "SEK", new BigDecimal(1000));
        // Withdraws full balance
        assertEquals(new BigDecimal(0), myTestAccount2.withdrawAll());

        Account myTestAccount3 = new Account(new BigDecimal(-0), "SEK", new BigDecimal(1000));
        // Withdrawing from an empty account makes no changes (-0 should also be == 0)
        assertEquals(new BigDecimal(0), myTestAccount3.withdrawAll());
    }
}

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


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
        // It should not be possible to withdraw a negative amount
        assertEquals(new BigDecimal(10), myTestAccount2.withdraw(new BigDecimal(-20)));

        Account myTestAccount3 = new Account(new BigDecimal(100), "SEK", new BigDecimal(-100));
        // Withdrawing 0 shouldn't change the balance
        assertEquals(new BigDecimal(100), myTestAccount3.withdraw(BigDecimal.ZERO));

        Account myTestAccount4 = new Account(new BigDecimal(1000), "SEK", new BigDecimal(200));
        // Withdrawing more than the balance (but within overdrawn range) should end up with negative value
        assertEquals(new BigDecimal(-100), myTestAccount4.withdraw(new BigDecimal(1100)));
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
        // A negative deposit should keep its balance
        assertEquals(new BigDecimal(2000), myTestAccount3.deposit(new BigDecimal(-500)));
    }

    @Test
    void testConvertToCurrency() {
        Account myTestAccount = new Account(new BigDecimal(2000.0), "USD", new BigDecimal(100));
        // Normal convertion to SEK (with decimal)
        myTestAccount.convertToCurrency("SEK", 10.0);
        assertEquals(new BigDecimal(20000), myTestAccount.getBalance());
        assertEquals(new BigDecimal(1000), myTestAccount.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount.getCurrency());

        Account myTestAccount2 = new Account(new BigDecimal(-100), "EUR", new BigDecimal(200));
        // Negative initial balance should still result in a negative balance (max_overdrawn should be updated to the new currency)
        myTestAccount2.convertToCurrency("SEK", 10.0);
        assertEquals(new BigDecimal(-1000), myTestAccount2.getBalance());
        assertEquals(new BigDecimal(2000), myTestAccount2.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount2.getCurrency());

        Account myTestAccount3 = new Account(new BigDecimal(50), "SEK", new BigDecimal(200));
        // Using a rate that is < 1 and > 0.
        myTestAccount3.convertToCurrency("USD", 0.1);
        // First param results in crazy decimal
        assertEquals(new BigDecimal(5), myTestAccount3.getBalance());
        assertEquals(new BigDecimal(20), myTestAccount3.getMaxOverdrawn());
        assertEquals("USD", myTestAccount3.getCurrency());
    }

    @Test
    void testTransferToAccount() {
        Account myTestAccountA = new Account(new BigDecimal(-100), "SEK", new BigDecimal(100));
        Account myTestAccountB = new Account(new BigDecimal(200), "SEK", new BigDecimal(10));
        // Can't transfer negative funds
        myTestAccountA.TransferToAccount(myTestAccountB);
        assertEquals(new BigDecimal(-100), myTestAccountA.getBalance());
        assertEquals(new BigDecimal(200), myTestAccountB.getBalance());

        Account myTestAccountA2 = new Account(new BigDecimal(100), "SEK", new BigDecimal(0));
        Account myTestAccountB2 = new Account(new BigDecimal(-200), "SEK", new BigDecimal(1000));
        // Transfering a low amount to a negative account should stay negative.
        myTestAccountA2.TransferToAccount(myTestAccountB2);
        assertEquals(new BigDecimal(0), myTestAccountA2.getBalance());
        assertEquals(new BigDecimal(-100), myTestAccountB2.getBalance());

        Account myTestAccountA3 = new Account(new BigDecimal(100), "SEK", new BigDecimal(0));
        Account myTestAccountB3 = new Account(new BigDecimal(200), "SEK", new BigDecimal(10));
        Account myTestAccountC3 = new Account(new BigDecimal(1000), "SEK", new BigDecimal(10));
        // Transfering to one account should empty the account correctly.
        myTestAccountA3.TransferToAccount(myTestAccountB3);
        assertEquals(new BigDecimal(0), myTestAccountA3.getBalance());
        // Transfering 0 funds makes no changes
        myTestAccountA3.TransferToAccount(myTestAccountC3);
        assertEquals(new BigDecimal(0), myTestAccountA3.getBalance());
        assertEquals(new BigDecimal(300), myTestAccountB3.getBalance()); // Added 100
        assertEquals(new BigDecimal(1000), myTestAccountC3.getBalance()); // Unchanged
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
        // Withdrawing from an empty account makes no changes
        assertEquals(new BigDecimal(0), myTestAccount3.withdrawAll());
    }
}

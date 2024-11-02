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
        // Test Case 1: Exact balance withdrawal
        Account myTestAccount = new Account(new BigDecimal(100), "SEK", BigDecimal.ZERO);
        // Expected behavior: Withdrawing 100 SEK from a balance of 100 SEK should leave 0 SEK in the account.
        // Skeleton code behavior: Worked as expected.
        // Input reasoning: Tests basic withdrawal functionality with an amount equal to the account balance.
        assertEquals(BigDecimal.ZERO, myTestAccount.withdraw(new BigDecimal(100)));

        // Test Case 2: Attempting a negative withdrawal
        Account myTestAccount2 = new Account(new BigDecimal(10), "SEK", new BigDecimal(10));
        // Expected behavior: A negative withdrawal should be ignored, keeping the balance unchanged at 10 SEK.
        // Skeleton code behavior: Skeleton code tried to process the negative withdrawal, which was fixed.
        // Input reasoning: Tests for correct handling of invalid input values (negative amounts).
        // Fix: Add validation to ensure withdrawal amounts are positive.
        assertEquals(new BigDecimal(10), myTestAccount2.withdraw(new BigDecimal(-20)));

        // Test Case 3: Zero amount withdrawal
        Account myTestAccount3 = new Account(new BigDecimal(100), "SEK", new BigDecimal(-100));
        // Expected behavior: Withdrawing 0 SEK should leave the balance unchanged at 100 SEK.
        // Skeleton code behavior: Worked correctly since subtracting 0 doesn't change anything.
        // Input reasoning: Tests withdrawal with a zero amount to confirm it doesn’t impact the account balance.
        assertEquals(new BigDecimal(100), myTestAccount3.withdraw(BigDecimal.ZERO));

        // Test Case 4: Overdrawing within the allowable limit
        Account myTestAccount4 = new Account(new BigDecimal(1000), "SEK", new BigDecimal(200));
        // Expected behavior: Withdrawing 1100 SEK should reduce the balance to -100 SEK, within the allowable overdraft range.
        // Skeleton code behavior: It worked as expected.
        // Input reasoning: Tests withdrawing more than the balance but within the overdraft limit.
        assertEquals(new BigDecimal(-100), myTestAccount4.withdraw(new BigDecimal(1100)));

       // Test Case 5: Exceeding balance and overdraft limit
        Account myTestAccount5 = new Account(new BigDecimal(1000), "USD", new BigDecimal(200));
        // Expected behavior: Attempting to withdraw 1201 USD should fail, leaving the balance at the original 1000 USD.
        // Skeleton code behavior: Overdraft limits were not implemented yet so it returned -201.
        // Input reasoning: Tests how the account handles withdrawal requests exceeding both balance and overdraft limit.
        // Fix: Add logic to ensure withdrawals beyond the allowable overdraft limit are rejected.
        assertEquals(new BigDecimal(1000), myTestAccount5.withdraw(new BigDecimal(1201)));
    }

    @Test
    void testDeposit() {
        // Test Case 1: Standard deposit with positive balance
        Account myTestAccount = new Account(new BigDecimal(100), "SEK", BigDecimal.ZERO);
        // Expected behavior: A deposit of 50 SEK with an initial balance of 100 SEK should update the balance to 150 SEK.
        // Skeleton code behavior: Works as expected
        // Input reasoning: This straightforward deposit tests basic balance addition functionality.
        assertEquals(new BigDecimal(150), myTestAccount.deposit(new BigDecimal(50)));

         // Test Case 2: Deposit with negative balance
        Account myTestAccount2 = new Account(new BigDecimal(-100), "SEK", new BigDecimal(150));
        // Expected behavior: A deposit of 20 SEK should increase the balance to -80 SEK, even though it’s still negative.
        // Skeleton code behavior: Works as expected.
        // Input reasoning: This case tests how deposits work with accounts in overdraft (negative balance).
        assertEquals(new BigDecimal(-80), myTestAccount2.deposit(new BigDecimal(20)));

        // Test Case 3: Negative deposit
        Account myTestAccount3 = new Account(new BigDecimal(2000), "SEK", BigDecimal.ZERO);
        // Expected behavior: A negative deposit should be ignored, keeping the balance at 2000 SEK.
        // Skeleton code behavior: It allowed a negative deposit, which was fixed.
        // Input reasoning: Testing with a negative deposit to ensure such values are rejected.
        // Fix: Add a check to ensure deposit amount is non-negative.
        assertEquals(new BigDecimal(2000), myTestAccount3.deposit(new BigDecimal(-500)));
    }

    @Test
    void testConvertToCurrency() {
        // Test Case 1: Positive rate conversion from USD to SEK
        Account myTestAccount = new Account(new BigDecimal(2000.0), "USD", new BigDecimal(100));
        // Expected behavior: Conversion from 2000 USD to SEK at a rate of 10.0 should result in a balance of 20000 SEK
        // and max overdrawn limit of 1000 SEK. Currency should change to SEK.
        // Skeleton code behavior: Worked as expected, except for the maxoverdrawn that was left unchanged.
        // Input reasoning: Tests currency conversion functionality with a positive rate greater than 1, increasing balance.
        // Fix: Ensure the maxoverdrawn gets updated aswell.
        assertTrue(myTestAccount.convertToCurrency("SEK", 10.0));
        assertEquals(new BigDecimal(20000), myTestAccount.getBalance());
        assertEquals(new BigDecimal(1000), myTestAccount.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount.getCurrency());

        // Test Case 2: Conversion with negative initial balance
        Account myTestAccount2 = new Account(new BigDecimal(-100), "EUR", new BigDecimal(200));
        // Expected behavior: Conversion from -100 EUR to SEK at a rate of 10.0 should result in -1000 SEK balance,
        // max overdrawn should update to 2000 SEK, and currency should change to SEK.
        // Skeleton code behavior: Worked as expected, except for the maxoverdrawn that was left unchanged.
        // Input reasoning: Tests conversion handling of accounts with negative balance and standard conversion rate.
        // Fix: Ensure the maxoverdrawn gets updated aswell.
        myTestAccount2.convertToCurrency("SEK", 10.0);
        assertEquals(new BigDecimal(-1000), myTestAccount2.getBalance());
        assertEquals(new BigDecimal(2000), myTestAccount2.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount2.getCurrency());

        // Test Case 3: Conversion with a fractional rate
        Account myTestAccount3 = new Account(new BigDecimal(50), "SEK", new BigDecimal(200));
        // Expected behavior: Conversion from 50 SEK to USD at a rate of 0.1 should reduce balance to 5 USD
        // and max overdrawn to 20 USD. Currency should change to USD.
        // Skeleton code behavior: Worked as expected, except for the maxoverdrawn that was left unchanged.
        // Input reasoning: Tests conversion with rates between 0 and 1 to confirm expected balance decrease.
        // Fix: Ensure the maxoverdrawn gets updated aswell.
        myTestAccount3.convertToCurrency("USD", 0.1);
        assertEquals(new BigDecimal(5), myTestAccount3.getBalance());
        assertEquals(new BigDecimal(20), myTestAccount3.getMaxOverdrawn());
        assertEquals("USD", myTestAccount3.getCurrency());

        // Test Case 4: Attempting conversion with a negative rate
        Account myTestAccount4 = new Account(new BigDecimal(10), "SEK", new BigDecimal(20));
        // Expected behavior: A negative conversion rate should prevent the conversion, leaving balance, max overdrawn,
        // and currency unchanged.
        // Skeleton code behavior: Lack of rate validation could incorrectly allow negative rates and skew balance values.
        // Input reasoning: Tests handling of invalid input by using a negative rate, which should be rejected.
        // Fix: Add validation to disallow negative rates and return original values without conversion.
        myTestAccount4.convertToCurrency("USD", -10.0);
        assertEquals(new BigDecimal(10), myTestAccount4.getBalance());
        assertEquals(new BigDecimal(20), myTestAccount4.getMaxOverdrawn());
        assertEquals("SEK", myTestAccount4.getCurrency());
    }

    @Test
    void testTransferToAccount() {
        // Test Case 1: Transfer with a negative balance
        Account myTestAccountA = new Account(new BigDecimal(-100), "SEK", new BigDecimal(100));
        Account myTestAccountB = new Account(new BigDecimal(200), "SEK", new BigDecimal(10));
        // Expected behavior: Transferring funds with a negative balance should not be possible, and both accounts should keep their original balances.
        // Skeleton code behavior: Without a check, a negative balance transferred incorrectly.
        // Input reasoning: Tests prevention of transfers initiated with negative balance.
        // Fix: Ensure the transfer method validates positive balance before transferring.
        myTestAccountA.TransferToAccount(myTestAccountB);
        assertEquals(new BigDecimal(-100), myTestAccountA.getBalance());
        assertEquals(new BigDecimal(200), myTestAccountB.getBalance());

        // Test Case 2: Transfer a small positive balance to an account with a negative balance
        Account myTestAccountA2 = new Account(new BigDecimal(10), "SEK", new BigDecimal(0));
        Account myTestAccountB2 = new Account(new BigDecimal(-200), "SEK", new BigDecimal(1000));
        // Expected behavior: Transferring 10 SEK from Account A to Account B should result in Account A being emptied (balance = 0) 
        // and Account B increasing its balance by 10 SEK but remaining negative (balance = -190).
        // Skeleton code behavior: Worked as expected for account B, account A still had 10 though which was resolved to now setting current balance to 0 after successful transfer.
        // Input reasoning: This case checks if a positive transfer from an account with low funds correctly updates the balance of a receiving account with a negative balance, 
        // and it ensures that the transfer fully depletes the sending account while only partially offsetting the negative balance in the receiving account.
        // Fix: Ensure that the transfer method correctly adjusts both account balances.
        myTestAccountA2.TransferToAccount(myTestAccountB2);
        assertEquals(new BigDecimal(0), myTestAccountA2.getBalance()); // Account A should be emptied after the transfer.
        assertEquals(new BigDecimal(-190), myTestAccountB2.getBalance()); // Account B should increase by 10 SEK, resulting in -190 SEK.


        // Test Case 3: Multiple transfers from one account
        Account myTestAccountA3 = new Account(new BigDecimal(100), "SEK", new BigDecimal(0));
        Account myTestAccountB3 = new Account(new BigDecimal(200), "SEK", new BigDecimal(10));
        Account myTestAccountC3 = new Account(new BigDecimal(1000), "SEK", new BigDecimal(10));
        // Expected behavior: First transfer should deplete Account A, adding funds to Account B.
        // Second transfer from an empty Account A should have no effect on any balances.
        // Skeleton code behavior: Without any balance check NOR any emptying of the account, both transfers worked which was fixed.
        // Input reasoning: Tests for blocking transfers when source account is empty after an initial transfer.
        // Fix: Ensure zero-balance transfers do not proceed and do not alter receiver's balance aswell as (same as test case 2), set balance to 0 after transfer.
        myTestAccountA3.TransferToAccount(myTestAccountB3);
        assertEquals(new BigDecimal(0), myTestAccountA3.getBalance());
        assertEquals(new BigDecimal(300), myTestAccountB3.getBalance()); // Added 100
        myTestAccountA3.TransferToAccount(myTestAccountC3);
        assertEquals(new BigDecimal(0), myTestAccountA3.getBalance());
        assertEquals(new BigDecimal(1000), myTestAccountC3.getBalance()); // Unchanged

        // Test Case 4: Transfer between accounts with different currencies
        Account myTestAccountA4 = new Account(new BigDecimal(10), "USD", new BigDecimal(100));
        Account myTestAccountB4 = new Account(new BigDecimal(20), "SEK", new BigDecimal(1000));
        // Expected behavior: Transfer between accounts with different currencies should not proceed, leaving both balances unchanged.
        // Skeleton code behavior: Lack of currency validation allowed unintended transfers with mismatched currencies.
        // Input reasoning: Verifies that currency matching is enforced for transfers.
        // Fix: Ensure transfer method validates that both accounts use the same currency before proceeding.
        myTestAccountA4.TransferToAccount(myTestAccountB4);
        assertEquals(new BigDecimal(10), myTestAccountA4.getBalance());
        assertEquals(new BigDecimal(20), myTestAccountB4.getBalance());
    }

    @Test
    void testWithdrawAll() {
       Account myTestAccount = new Account(new BigDecimal(-200), "SEK", new BigDecimal(1000));
        // Expected behavior: Cannot withdraw when the balance is negative. The balance should remain unchanged.
        // Skeleton code behavior: It tried withdrawing a negative number, which should not be allowed.
        // Input reasoning: This case verifies that the withdrawAll method prevents further withdrawals when the balance is negative.
        // Fix: Ensure the method checks for a positive balance before performing the withdrawal.
        assertEquals(new BigDecimal(-200), myTestAccount.withdrawAll());

        Account myTestAccount2 = new Account(new BigDecimal(200), "SEK", new BigDecimal(1000));
        // Expected behavior: Withdraws the full balance, resulting in a zero balance.
        // Skeleton code behavior: Worked as expected.
        // Input reasoning: This case checks that the full balance can be withdrawn successfully.
        assertEquals(new BigDecimal(0), myTestAccount2.withdrawAll());

        Account myTestAccount3 = new Account(new BigDecimal(0), "SEK", new BigDecimal(1000));
        // Expected behavior: Withdrawing from an account with a zero balance should make no changes.
        // Skeleton code behavior: Worked as expected.
        // Input reasoning: This case ensures no modifications are made when there is no balance to withdraw.
        assertEquals(new BigDecimal(0), myTestAccount3.withdrawAll());
    }
}

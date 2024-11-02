import java.math.BigDecimal;
import java.util.Objects;

public class Account implements IAccount {

    /**
     * Current balance this account holds
     */
    private BigDecimal balance;
    /**
     * Currency used in this account, can be "SEK", "EUR", or "USD"
     */
    private String currency;
    /**
     * max_overdrawn is a non-negative number indicating how much the account can be "in the red"
     * The minimum balance of the account is -1 * max_overdrawn
     */
    private BigDecimal max_overdrawn;

    public BigDecimal getMaxOverdrawn() {
        return this.max_overdrawn;
    }

    public void setMaxOverdrawn(BigDecimal max_overdrawn) {
        if(max_overdrawn.compareTo(BigDecimal.ZERO) < 0) {
            this.max_overdrawn = BigDecimal.ZERO;
        } else {
            this.max_overdrawn = max_overdrawn;
        }
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setBalance(BigDecimal balance) {
        // Small change to if statement. (from "<=" to "<") No longer changes value if it equals to 0. No real change in functionality. 
        if(!(balance.compareTo(this.max_overdrawn.multiply(new BigDecimal(-1))) < 0)) {
            this.balance = balance;
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Account() {
        this.balance = BigDecimal.ZERO;
        this.currency = "SEK";
        this.max_overdrawn = BigDecimal.ZERO;
    }

    public Account(BigDecimal starting_balance, String currency, BigDecimal max_overdrawn) {
        this.balance = starting_balance;
        this.currency = currency;
        if(max_overdrawn.compareTo(BigDecimal.ZERO) <= 0) {
            this.max_overdrawn = BigDecimal.ZERO;
        } else {
            this.max_overdrawn = max_overdrawn;
        }
    }

    @Override
    // Problem: Returned the requested amount no matter the balance. Also didn't update the balance.
    // Fix: 
    // * Now checks if a withdraw is allowed in regards to the max_overdrawn amount.
    // * If a withdraw is allowed, the balance will be updated and return the new balance.
    // Otherwise it will return the old balance.
    public BigDecimal withdraw(BigDecimal requestedAmount) {
        BigDecimal withdrawMax = balance.add(max_overdrawn);
        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) { // If requestedAmount is negative
            return this.balance;
        } else if (withdrawMax.subtract(requestedAmount).compareTo(BigDecimal.ZERO) >= 0) { // if requestedAmount is <= withdrawMax
            this.setBalance(balance.subtract(requestedAmount));
        }
        return this.balance;
    }

    @Override
    // Problem: Only returns the balance after the deposit, doesn't actually change the balance.
    // Fix: 
    // * Now returns the correct balance. 
    // * Will return original balance if a negative amount is attempted to be deposited.
    public BigDecimal deposit(BigDecimal amount_to_deposit) {
        if (amount_to_deposit.compareTo(BigDecimal.ZERO) <= 0) { // If requestedAmount is negative
            return this.balance;
        }
        this.setBalance(this.balance.add(amount_to_deposit));
        return this.balance;
    }

    @Override
    // Problem: Doesn't return the correct type and makes the conversion even with a negative rate. Also, never changed the balance
    // Fix: 
    // * Now doesn't change anything with a negative "rate" as input
    // * Adjusts max_overdrawn to the new currency
    // * Changes the balance correctly
    // * Makes some conversion to avoid trailing zeroes during calculation. (Makes the tests show the correct results)
    public boolean convertToCurrency(String currencyCode, double rate) {
        if (rate <= 0) { // If rate is negative
            return false;
        }
        String S_rate = rate + "";
        this.currency = currencyCode;
        // stripTrailingZero conversion sets the correct format in the new balance/maxOverdrawn
        this.setMaxOverdrawn(new BigDecimal(max_overdrawn.multiply(new BigDecimal(S_rate)).stripTrailingZeros().toPlainString()));
        this.setBalance(new BigDecimal(balance.multiply(new BigDecimal(S_rate)).stripTrailingZeros().toPlainString()));
        return true;
    }

    @Override
    // Problem: Transfered money from an account no matter the currency and balance.
    // Fix: 
    // * Can now only transfer positive funds.
    // * Can only transfer to an account with matching "currency"
    // * Updates the balance correctly after transfering the funds.
    public void TransferToAccount(IAccount to_account) {
        if (this.balance.compareTo(BigDecimal.ZERO) <= 0) {
            return; // Can't transfer negative funds
        } else if (!Objects.equals(this.getCurrency(), to_account.getCurrency())) {
            return; // Can't transfer to an account with different currency
        }
        to_account.deposit(this.balance);
        this.setBalance(BigDecimal.ZERO);
    }

    @Override
    // Problem: The if statement didn't check if the balance was negative and the return amount was off.
    // Fix: 
    // * Now withdraws the correct amount
    // * Withdraws when there are enough funds (positive amount).
    public BigDecimal withdrawAll() {
        if (this.balance.compareTo(BigDecimal.ZERO) <= 0) {
            return this.balance;
        }
        return withdraw(balance);
    }
}

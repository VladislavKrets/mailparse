package online.omnia.mailparser.zoho.zohoentities;

/**
 * Created by lollipop on 05.08.2017.
 */
public class ZohoAccount {
    private String country;
    private String accountName;
    private String displayName;
    private String primaryEmailAddress;
    private String accountId;

    public ZohoAccount(String country, String accountName, String displayName,
                       String primaryEmailAddress, String accountId) {
        this.country = country;
        this.accountName = accountName;
        this.displayName = displayName;
        this.primaryEmailAddress = primaryEmailAddress;
        this.accountId = accountId;
    }

    public String getCountry() {
        return country;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrimaryEmailAddress() {
        return primaryEmailAddress;
    }

    public String getAccountId() {
        return accountId;
    }
}

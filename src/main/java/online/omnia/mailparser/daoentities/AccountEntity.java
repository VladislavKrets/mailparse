package online.omnia.mailparser.daoentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by lollipop on 17.08.2017.
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    @Column(name = "account_id")
    private int accountId;
    @Column(name = "buyer_id")
    private int buyerId;
    @Column(name = "token_table_name")
    private String tokenTableName;
    @Column(name = "client_id")
    private String clientId;
    @Column(name = "client_secret")
    private String clientSecret;
    @Column(name = "api_key")
    private String apiKey;
    @Column(name = "username")
    private String username;
    @Column(name = "type")
    private String type;
    @Column(name = "password")
    private String password;
    @Column(name = "api_URL")
    private String apiURL;
    @Column(name = "owner")
    private String owner;
    @Column(name = "statistics_type")
    private String statisticsType;
    @Column(name = "cabinet_type")
    private String cabinetType;
    @Column(name = "description")
    private String description;
    @Column(name = "actual")
    private int actual;

    public AccountEntity() {
    }

    public int getAccountId() {
        return accountId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public String getTokenTableName() {
        return tokenTableName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getPassword() {
        return password;
    }

    public String getApiURL() {
        return apiURL;
    }

    public String getOwner() {
        return owner;
    }

    public String getStatisticsType() {
        return statisticsType;
    }

    public String getCabinetType() {
        return cabinetType;
    }

    public String getDescription() {
        return description;
    }

    public int getActual() {
        return actual;
    }

    @Override
    public String toString() {
        return "AccountEntity{" +
                "accountId=" + accountId +
                ", buyerId=" + buyerId +
                ", tokenTableName='" + tokenTableName + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", password='" + password + '\'' +
                ", apiURL='" + apiURL + '\'' +
                ", owner='" + owner + '\'' +
                ", statisticsType='" + statisticsType + '\'' +
                ", cabinetType='" + cabinetType + '\'' +
                ", description='" + description + '\'' +
                ", actual=" + actual +
                '}';
    }
}

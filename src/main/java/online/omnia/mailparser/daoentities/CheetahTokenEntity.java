package online.omnia.mailparser.daoentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lollipop on 17.08.2017.
 */
@Entity
@Table(name = "cheetah_token")
public class CheetahTokenEntity {
    @Id
    @Column(name = "account_id")
    private int accountId;
    @Column(name = "token_type")
    private String tokenType;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "expires_time")
    private Date expiresTime;

    public int getAccountId() {
        return accountId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getExpiresTime() {
        return expiresTime;
    }
}

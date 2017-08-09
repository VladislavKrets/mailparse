package online.omnia.mailparser.zoho.daoentities;

import javax.persistence.*;

/**
 * Created by lollipop on 09.08.2017.
 */
@Entity
@Table(name = "email_access")
public class EmailAccessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "account_id")
    private int accountId;
    @Column(name = "server_name")
    private String serverName;
    @Column(name = "server_port")
    private String serverPort;
    @Column(name = "server_protocol")
    private String serverProtocol;
    @Column(name = "SSL_on")
    private int sslOn;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "email_address")
    private String emailAddress;
    @Column(name = "check_cheetah")
    private int checkCheetah;

    public EmailAccessEntity() {
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getServerProtocol() {
        return serverProtocol;
    }

    public int getSslOn() {
        return sslOn;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public int getCheckCheetah() {
        return checkCheetah;
    }
}

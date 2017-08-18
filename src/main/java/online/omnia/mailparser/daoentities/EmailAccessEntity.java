package online.omnia.mailparser.daoentities;

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

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerProtocol(String serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    public void setSslOn(int sslOn) {
        this.sslOn = sslOn;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setCheckCheetah(int checkCheetah) {
        this.checkCheetah = checkCheetah;
    }

}

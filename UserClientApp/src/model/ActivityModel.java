package model;

import javafx.beans.property.SimpleStringProperty;

public class ActivityModel {

    private SimpleStringProperty token;
    private SimpleStringProperty loginTime;
    private SimpleStringProperty logoutTime;
    private SimpleStringProperty loginSpentTime;

    public ActivityModel(String token, String loginTime, String logoutTime, String loginSpentTime) {
        this.token = new SimpleStringProperty(token);
        this.loginTime = new SimpleStringProperty(loginTime);
        this.logoutTime = new SimpleStringProperty(logoutTime);
        this.loginSpentTime = new SimpleStringProperty(loginSpentTime);
    }

    public ActivityModel() {
    }

    public String getToken() {
        return token.get();
    }

    public SimpleStringProperty tokenProperty() {
        return token;
    }

    public void setToken(String token) {
        this.token.set(token);
    }

    public String getLoginTime() {
        return loginTime.get();
    }

    public SimpleStringProperty loginTimeProperty() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime.set(loginTime);
    }

    public String getLogoutTime() {
        return logoutTime.get();
    }

    public SimpleStringProperty logoutTimeProperty() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime.set(logoutTime);
    }

    public String getLoginSpentTime() {
        return loginSpentTime.get();
    }

    public SimpleStringProperty loginSpentTimeProperty() {
        return loginSpentTime;
    }

    public void setLoginSpentTime(String loginSpentTime) {
        this.loginSpentTime.set(loginSpentTime);
    }
}

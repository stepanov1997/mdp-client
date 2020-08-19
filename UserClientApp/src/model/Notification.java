package model;

import java.util.Objects;

public class Notification {
    private String token;
    private String potential_contact_from;
    private String potential_contact_to;
    private int interval;
    private int lat;
    private int aLong;
    private int distance;
    private String infection;
    private String typeOfNotification;

    public Notification(String token, String infection, String typeOfNotification) {
        this.token = token;
        this.infection = infection;
        this.typeOfNotification = typeOfNotification;
    }

    public Notification(String token, String potential_contact_from, String potential_contact_to, int interval, int lat, int aLong, int distance, String infection, String typeOfNotification) {
        this.token = token;
        this.potential_contact_from = potential_contact_from;
        this.potential_contact_to = potential_contact_to;
        this.interval = interval;
        this.lat = lat;
        this.aLong = aLong;
        this.distance = distance;
        this.infection = infection;
        this.typeOfNotification = typeOfNotification;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPotential_contact_from() {
        return potential_contact_from;
    }

    public void setPotential_contact_from(String potential_contact_from) {
        this.potential_contact_from = potential_contact_from;
    }

    public String getPotential_contact_to() {
        return potential_contact_to;
    }

    public void setPotential_contact_to(String potential_contact_to) {
        this.potential_contact_to = potential_contact_to;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getaLong() {
        return aLong;
    }

    public void setaLong(int aLong) {
        this.aLong = aLong;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getInfection() {
        return infection;
    }

    public void setInfection(String infection) {
        this.infection = infection;
    }

    public String getTypeOfNotification() {
        return typeOfNotification;
    }

    public void setTypeOfNotification(String typeOfNotification) {
        this.typeOfNotification = typeOfNotification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return getInterval() == that.getInterval() &&
                getLat() == that.getLat() &&
                getaLong() == that.getaLong() &&
                getDistance() == that.getDistance() &&
                Objects.equals(getToken(), that.getToken()) &&
                Objects.equals(getPotential_contact_from(), that.getPotential_contact_from()) &&
                Objects.equals(getPotential_contact_to(), that.getPotential_contact_to()) &&
                Objects.equals(getInfection(), that.getInfection()) &&
                Objects.equals(getTypeOfNotification(), that.getTypeOfNotification());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken(), getPotential_contact_from(), getPotential_contact_to(), getInterval(), getLat(), getaLong(), getDistance(), getInfection(), getTypeOfNotification());
    }
}

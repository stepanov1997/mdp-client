package util;

import java.io.*;
import java.util.Properties;

public class CurrentUser {

    private static final String CONFIG_PATH = "src/util/config.properties";

    public static String getToken() {
        String token = null;
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties prop = new Properties();
            prop.load(input);
            token = prop.getProperty("token");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return token;
    }

    public static void setToken(String token) {
        Properties properties = null;
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            properties.setProperty("token", token);
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static String getPasswordHash() {
        String passwordHash = null;
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties prop = new Properties();
            prop.load(input);
            passwordHash = prop.getProperty("passwordHash");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return passwordHash;
    }

    public static void setPassword(String password) {
        Properties properties = null;
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            properties.setProperty("passwordHash", SHA1.encryptPassword(password));
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}

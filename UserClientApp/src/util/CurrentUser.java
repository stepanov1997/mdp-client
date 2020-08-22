package util;

import controller.MainMenuController;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CurrentUser {
    private static final Logger LOGGER = Logger.getLogger(CurrentUser.class.getName());

    private static String CONFIG_PATH;

    static {
        try {
            CONFIG_PATH = ConfigUtil.getPropertiesPath();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Config file - cannot read properties file", e);
            CONFIG_PATH = "src/util/config.properties";
        }
    }

    public static String getToken() {
        String token = null;
        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            Properties prop = new Properties();
            prop.load(input);
            token = prop.getProperty("token");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Getting token");
        }
        return token;
    }

    public static void setToken(String token) {
        Properties properties = null;
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Config file - set token", e);
            e.printStackTrace();
        }
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            properties.setProperty("token", token);
            properties.store(output, null);
        } catch (IOException io) {
            LOGGER.log(Level.WARNING, "Config file - cannot set token");
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
            LOGGER.log(Level.WARNING, "Config file - cannot get password",ex);
        }
        return passwordHash;
    }

    public static void setPassword(String password) {
        Properties properties = null;
        try (FileInputStream in = new FileInputStream(CONFIG_PATH)) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Config file - cannot set password.");
        }
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            properties.setProperty("passwordHash", SHA1.encryptPassword(password));
            properties.store(output, null);
        } catch (IOException io) {
            LOGGER.log(Level.WARNING, "Config file - cannot set password file", io);
        }
    }
}

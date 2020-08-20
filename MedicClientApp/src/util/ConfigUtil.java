package util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtil {
    public static String getServerHostname() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("server").getAsString();
    }

    public static int getChatServerPort() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("chatServerPort").getAsInt();
    }

    public static int getTokenServerPort() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("tokenServerPort").getAsInt();
    }

    public static int getCentralRegisterPort() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("centralRegisterPort").getAsInt();
    }

    public static int getMulticastChatServerPort() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("multicastChatServerPort").getAsInt();
    }

    public static String getMulticastChatServerAddress() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("multiCastAddress").getAsString();
    }

    public static String getKeystorePath() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("keystorePath").getAsString();
    }

    public static String getTruststorePath() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("truststorePath").getAsString();
    }

    public static String getKeystorePassword() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("keystorePassword").getAsString();
    }

    public static String getTruststorePassword() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("truststorePassword").getAsString();
    }

    public static int getFileServerPort() throws IOException {
        String config = Files.readString(Path.of("src/config.json"));
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(config);
        return jsonObject.get("fileServerPort").getAsInt();
    }
}

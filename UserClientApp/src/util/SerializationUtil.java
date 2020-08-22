package util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import controller.MainMenuController;
import model.Notification;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerializationUtil {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    private static int i = -1;

    private static final Consumer<Notification>[] functions = new Consumer[]{
            SerializationUtil::serializeAsXML,
            SerializationUtil::serializeAsJson,
            SerializationUtil::javaSerialization,
            SerializationUtil::kryoSerialization
    };

    public static void serializeNotification(Notification notification) {
        functions[i = (i + 1) % 4].accept(notification);
    }

    private static void serializeAsXML(Object object) {
        File file = new File("notifications" + File.separator + CurrentUser.getToken() + File.separator + System.currentTimeMillis() + ".xml");
        file.getParentFile().mkdirs();

        Notification notification = (Notification) object;
        try (FileWriter writer = new FileWriter(file)) {
            JAXBContext context = JAXBContext.newInstance(Notification.class);
            Marshaller m = context.createMarshaller();
            m.marshal(new JAXBElement<>(new QName(Notification.class.getSimpleName()), Notification.class, notification), writer);
        } catch (JAXBException | IOException e) {
            LOGGER.log(Level.WARNING, "Unsuccessfully xml serialization..", e);
        }
    }

    private static void serializeAsJson(Object object) {
        File file = new File("notifications" + File.separator + CurrentUser.getToken() + File.separator + System.currentTimeMillis() + ".json");
        file.getParentFile().mkdirs();

        Gson gson = new Gson();
        String json = gson.toJson(object);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unsuccessfully json serialization..", e);
        }
    }

    private static void javaSerialization(Object object) {
        File file = new File("notifications" + File.separator + CurrentUser.getToken() + File.separator + System.currentTimeMillis() + ".ser");
        file.getParentFile().mkdirs();

        Notification notification = (Notification) object;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos)) {
                objectOutputStream.writeObject(notification);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unsuccessfully java serialization..", e);
        }
    }

    private static void kryoSerialization(Object object) {
        File file = new File("notifications" + File.separator + CurrentUser.getToken() + File.separator + System.currentTimeMillis() + ".kryo");
        file.getParentFile().mkdirs();

        Notification notification = (Notification) object;

        Kryo kryo = new Kryo();
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, notification);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Unsuccessfully kryo serialization..", e);
        }
    }
}

package util;

import controller.MainMenuController;
import server.IFileServer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RmiClient {
    private static final Logger LOGGER = Logger.getLogger(RmiClient.class.getName());

    public static void sendFileToServer(File file) {
        try {
            String token = CurrentUser.getToken();
            String nm = "FileServer";
            String hostname = "";
            int port = 0;
            try {
                hostname = ConfigUtil.getServerHostname();
                port = ConfigUtil.getFileServerPort();
            } catch (IOException ioException) {
                hostname = "127.0.0.1";
                port = 1099;
            }
            IFileServer srv = (IFileServer) Naming.lookup("rmi://" + hostname + ":" + port + "/" + nm);
            long fileSize = file.length();
            long sended = 0;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                while (sended < fileSize) {
                    int toSend = 10_000_000;
                    if (fileSize - sended < toSend)
                        toSend = (int) (fileSize - sended);
                    byte[] buffer = new byte[toSend];
                    randomAccessFile.seek(sended);
                    randomAccessFile.read(buffer, 0, toSend);
                    boolean response = srv.uploadFileOnServer(token, file.getName(), buffer, sended);
                    if (!response) {
                        throw new RemoteException();
                    }
                    sended += toSend;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't send file on server.", e);
        }
    }
}

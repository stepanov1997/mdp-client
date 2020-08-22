package util;

import controller.DocumentationController;
import server.IFileServer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RmiClient {
    private static final Logger LOGGER = Logger.getLogger(RmiClient.class.getName());


    public static void downloadFilesFromServer(String token, String fileName, long fileSize) {
        try {
            String nm = "FileServer";
            String hostname = "";
            int port = 0;
            try {
                hostname = ConfigUtil.getServerHostname();
                port = ConfigUtil.getFileServerPort();
            } catch (IOException ioException) {
                LOGGER.log(Level.WARNING, "Cannot open file.", ioException);
                hostname = "127.0.0.1";
                port = 1099;
            }
            IFileServer srv = (IFileServer) Naming.lookup("rmi://" + hostname + ":" + port + "/" + nm);
            File file = new File("files" + File.separator + token + File.separator + fileName);
            if(!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            int toSend = 10_000_000;
            long sended = 0;
            try(RandomAccessFile raf = new RandomAccessFile(file, "rw"))
            {
                while (sended < fileSize) {
                    if (fileSize - sended < toSend)
                        toSend = (int) (fileSize - sended);
                    byte[] bytes = srv.downloadFileFromServer(token, fileName, sended, toSend);
                    raf.seek(sended);
                    raf.write(bytes, 0, bytes.length);
                    if(bytes==null){
                        throw new RemoteException();
                    }
                    sended += toSend;
                }
            }catch (IOException ex){
                LOGGER.log(Level.WARNING, "Cannot open file.", ex);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cannot open file.", e);
        }
    }
}

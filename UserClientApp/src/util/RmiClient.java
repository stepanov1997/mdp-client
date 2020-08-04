package util;

import server.IFileServer;

import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class RmiClient {

    public static void sendFileToServer(File file) {
        try {
            String token = CurrentUser.getToken();
            String nm = "FileServer";
            IFileServer srv = (IFileServer) Naming.lookup("rmi://pisio.etfbl.net:1099/" + nm);
            int fileSize = (int) file.length();
            int sended = 0;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                while (sended < fileSize) {
                    int toSend = 10_000_000;
                    if (fileSize - sended < toSend)
                        toSend = (int) (fileSize - sended);
                    byte[] buffer = new byte[toSend];
                    randomAccessFile.seek(sended);
                    randomAccessFile.read(buffer, 0, toSend);
                    String pathOnServer = srv.uploadFileOnServer(token, file.getName(), buffer, sended);
                    if(pathOnServer==null){
                        throw new RemoteException();
                    }
                    sended += toSend;
                }
            }
        } catch (Exception e) {
            System.err.println("FileServer exception:");
            e.printStackTrace();
        }
    }
}

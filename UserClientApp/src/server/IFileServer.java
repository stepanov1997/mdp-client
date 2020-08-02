package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileServer extends Remote {
	String uploadFileOnServer(String token, String filename, byte[] data, int offset) throws RemoteException;
	byte[] downloadFileFromServer(String token, String filename, int offset, int count) throws RemoteException;
}

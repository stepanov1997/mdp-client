package server;

import com.google.gson.JsonArray;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

public interface IFileServer extends Remote {
	HashMap<String, Long> getFilePathsForToken(String token) throws RemoteException;
	String uploadFileOnServer(String token, String filename, byte[] data, long offset) throws RemoteException;
	byte[] downloadFileFromServer(String token, String filename, long offset, int count) throws RemoteException;
}

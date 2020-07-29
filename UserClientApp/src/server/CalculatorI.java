package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculatorI extends Remote { 					// 1
	int add(int x, int y) throws RemoteException; 		// 2
	int sub(int x, int y) throws RemoteException; 		// 3
}


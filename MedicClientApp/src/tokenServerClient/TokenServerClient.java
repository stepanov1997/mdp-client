package tokenServerClient;

import soap.Application_PortType;
import soap.Application_ServiceLocator;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

public class TokenServerClient implements Application_PortType {

    private Application_ServiceLocator asl;
    private Application_PortType apt;

    public TokenServerClient() throws ServiceException {
        asl = new Application_ServiceLocator();
        apt = asl.getApplication();
    }

    @Override
    public Boolean checkToken(String token) throws RemoteException {
        return apt.checkToken(token);
    }

    @Override
    public Boolean deactivateToken(String token) throws RemoteException {
        return apt.checkToken(token);
    }

    @Override
    public String[] getActiveTokens() throws RemoteException {
        return apt.getActiveTokens();
    }

    @Override
    public String signIn(String name, String surname, String jmbg) throws RemoteException {
        return apt.signIn(name, surname, jmbg);
    }
}

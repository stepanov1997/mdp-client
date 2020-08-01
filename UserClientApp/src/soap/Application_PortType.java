/**
 * Application_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package soap;

public interface Application_PortType extends java.rmi.Remote {
    public java.lang.String checkToken(java.lang.String token) throws java.rmi.RemoteException;
    public java.lang.String get_time() throws java.rmi.RemoteException;
    public java.lang.String signIn(java.lang.String name, java.lang.String surname, java.lang.String jmbg) throws java.rmi.RemoteException;
}

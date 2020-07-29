package main;

import server.CalculatorI;

import java.rmi.Naming;
import java.util.Scanner;

public class RMIMain {
    public static void main(String[] args) {
        System.setProperty("java.security.policy", "client.policy");
        try {
            String nm = "Calculator";
            CalculatorI srv = (CalculatorI) Naming.lookup("rmi://pisio.etfbl.net:1099/" + nm);
            Scanner in = new Scanner(System.in);
            System.out.print("First number: ");
            int n1 = in.nextInt();
            System.out.print("Second number: ");
            int n2 = in.nextInt();
            System.out.println("Add: " + srv.add(n1, n2)); 								// 5
            System.out.println("Sub: " + srv.sub(n1, n2)); 								// 6
        } catch (Exception e) {
            System.err.println("Calculator exception:");
            e.printStackTrace();
        }
    }
}


package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    // podesavamo port na kom cemo osluskivati
    public static final int TCP_PORT = 8084;
    public static final String IP_ADDRESS = "pisio.etfbl.net";

    public static void main(String[] args) {
        try {
            InetAddress addr = InetAddress.getByName(IP_ADDRESS);
            Socket sock = new Socket(addr, TCP_PORT);

            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);


            Scanner scan = new Scanner(System.in);
            String msg = "";
            var ref = new Object() {
                boolean flag = true;
            };

            // prikaz poruka
            Thread thread = new Thread(() -> {
                while (ref.flag) {
                    char[] response = new char[1024];
                    try {
                        in.read(response);
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                    System.out.print(String.valueOf(response));
                }
            });
            thread.start();

            // slanje poruka
            String username = scan.nextLine();
            out.println(username);

            // slanje poruka
            String usernameSagovornika = scan.nextLine();
            out.println(usernameSagovornika);

            // slanje poruka
            while (!"KRAJ".equals(msg)) {
                System.out.println();
                System.out.print("["+username+"]: ");
                msg = scan.nextLine();
                out.println(msg);
            }

            scan.close();
            ref.flag = false;
            // zatvori konekciju
            in.close();
            out.close();
            sock.close();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}

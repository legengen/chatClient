import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        Server server=new Server(8189);
        server.start();

    }
}
class Server{
    static Set<PrintWriter> clientWriters =new HashSet<>();
    int port;
    Scanner share;
    public Server(int port){
        this.port=port;
    }
    public void start() throws IOException {
        try(ServerSocket serverSocket=new ServerSocket(port)) {
            while(true){
                Socket incoming=serverSocket.accept();
                Runnable r=new ThreadedEchoHandler(incoming);
                Thread t=new Thread(r);
                t.start();
            }
        }
    }

    class ThreadedEchoHandler implements Runnable{
        Socket incoming;
        PrintWriter out;
        Scanner share;
        String message;
        ThreadedEchoHandler(Socket incomingSocket){
            incoming=incomingSocket;
        }

        @Override
        public void run() {
            try {
                Scanner in=new Scanner(incoming.getInputStream());
                PrintWriter out=new PrintWriter(incoming.getOutputStream(),true);

                synchronized (clientWriters){
                    clientWriters.add(out);
                }

                String message;
                while(in.hasNextLine()){
                    message=in.nextLine();
                    System.out.println("Received: " + message);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                try {
                    incoming.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}

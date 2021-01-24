package be.afhistos.socketclient;

import be.afhistos.socketserver.ServerPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

public class ServerClient {
    private String ip;
    private int port;
    private Socket s;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread inThread, outThread;
    private Deque<ServerPacket> packetDeque;
    private List<ClientListener> listeners;
    private boolean running;

    public ServerPacket logoutPacket = new ServerPacket("LOGOUT", null);

    public ServerClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        packetDeque = new LinkedList<>();
        listeners = new ArrayList<>();
    }
    public ServerClient(Properties props){
        this(props.getProperty("ip", "localhost"), Integer.parseInt(props.getProperty("port", "44444")));
    }

    public ServerClient(String ip) {
        this(ip, 44444);
    }

    public ServerClient(int port) {
        this("localhost", port);
    }

    public ServerClient() {
        this("localhost", 44444);
    }

    public void start(){
        try{
            s = new Socket(ip, port);
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
            running =true;
            inThread = getInThread();
            outThread = getOutThread();
            inThread.start();
            outThread.start();
            listeners.forEach(sl->{sl.onConnection(this);});
        } catch (IOException e) {
            listeners.forEach(sl->{sl.onError(this, e);});
        }
    }

    public void logout(){
        listeners.forEach(sl->{sl.onLogout(this);});
        listeners.add(new ClientListener() {
            @Override
            public void onPacketReceived(ServerClient client, ServerPacket packet) {
                if(packet == logoutPacket){
                    running = false;
                    inThread.interrupt();
                    outThread.interrupt();
                    try{
                        in.close();
                        out.close();
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    packetDeque.clear();
                }
            }
        });
        packetDeque.offerFirst(logoutPacket);
    }

    public void sendPacket(ServerPacket packet){
        packetDeque.offer(packet);
    }

    private Thread getInThread(){
        if(in ==  null){return null;}
        return new Thread(()->{
            while(running){
                listeners.forEach(sl-> {
                    try {
                        sl.onPacketReceived(this,(ServerPacket) in.readObject());
                    } catch (IOException | ClassNotFoundException e) {
                        sl.onError(this, e);
                    }
                });
            }
        });
    }

    private Thread getOutThread() {
        if(out == null){return null;}
        return new Thread(()->{
            while(running){
                packetDeque.forEach(serverPacket -> {
                    try {
                        out.writeObject(serverPacket);
                        listeners.forEach(sl->{sl.onPacketSent(this,serverPacket);});
                    } catch (IOException e) {
                        listeners.forEach(sl->{sl.onError(this, e);});
                    }
                });
            }
        });
    }

    public void setLogoutPacket(ServerPacket logoutPacket) {
        this.logoutPacket = logoutPacket;
    }
}

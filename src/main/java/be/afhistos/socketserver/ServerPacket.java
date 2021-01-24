package be.afhistos.socketserver;

import java.io.Serializable;

public class ServerPacket implements Serializable {
    public static final long serialVersionUID = 23L;
    private String action;
    private Object value;

    public ServerPacket(String action, Object value) {
        this.action = action;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public Object getValue() {
        return value;
    }
}

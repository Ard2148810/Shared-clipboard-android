package pl.adrian.sharedclipboard;

import java.util.HashSet;
import java.util.Set;

public class ConnectionStatus {
    private static ConnectionStatusState state = ConnectionStatusState.DISCONNECTED;
    private static final Set<ConnectionStatusListener> listeners = new HashSet<>();


    public static void setConnectionStatus(ConnectionStatusState state) {
        ConnectionStatus.state = state;
        for(ConnectionStatusListener listener: listeners) {
           listener.statusChanged(ConnectionStatus.state);
        }
    }

    public static ConnectionStatusState getConnectionStatus() {
        return ConnectionStatus.state;
    }

    public static void addListener(ConnectionStatusListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(ConnectionStatusListener listener) {
        listeners.remove(listener);
    }


    public enum ConnectionStatusState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        PAUSED
    }

    public interface ConnectionStatusListener {
        void statusChanged(ConnectionStatusState state);
    }

}

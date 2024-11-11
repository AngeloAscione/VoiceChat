import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VoiceChatServer {
    private static final int PORT = 50005;
    private static final int BUFFER_SIZE = 4096;

    // Set per memorizzare i client connessi
    private static Set<ClientInfo> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try {
            // Crea il socket per ricevere pacchetti UDP
            DatagramSocket socket = new DatagramSocket(PORT);

            System.out.println("Voice Chat Server is running...");

            // Loop principale per ricevere e inoltrare pacchetti
            while (true) {
                // Crea un nuovo buffer e un DatagramPacket per ogni ricezione
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

                // Ricevi il pacchetto
                socket.receive(receivedPacket);

                // Ottiene le informazioni del client mittente
                ClientInfo sender = new ClientInfo(receivedPacket.getAddress(), receivedPacket.getPort());

                // Aggiunge il client all'elenco se non è già presente
                clients.add(sender);

                // Inoltra il pacchetto a tutti gli altri client
                forwardPacketToClients(socket, receivedPacket, sender);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo per inoltrare il pacchetto a tutti i client tranne il mittente
    private static void forwardPacketToClients(DatagramSocket socket, DatagramPacket packet, ClientInfo sender) {
        byte[] data = packet.getData();
        for (ClientInfo client : clients) {
            // Non invia il pacchetto al client mittente
            if (!client.equals(sender)) {
                try {
                    // Crea un nuovo buffer e un nuovo DatagramPacket per ogni client
                    byte[] clientBuffer = new byte[data.length];
                    System.arraycopy(data, 0, clientBuffer, 0, data.length);
                    DatagramPacket forwardPacket = new DatagramPacket(clientBuffer, clientBuffer.length, client.address, client.port);
                    socket.send(forwardPacket);
                } catch (Exception e) {
                    System.out.println("Errore nell'invio a " + client);
                }
            }
        }
    }

    // Classe interna per memorizzare le informazioni del client
    private static class ClientInfo {
        InetAddress address;
        int port;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ClientInfo that = (ClientInfo) obj;
            return port == that.port && address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return address.hashCode() * 31 + port;
        }

        @Override
        public String toString() {
            return "ClientInfo{" +
                    "address=" + address +
                    ", port=" + port +
                    '}';
        }
    }
}

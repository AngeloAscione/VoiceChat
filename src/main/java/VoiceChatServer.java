import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VoiceChatServer {
    private static final int PORT = 50005;
    private static final int BUFFER_SIZE = 4096;
    private static Set<ClientInfo> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        try {
            // Imposta il formato audio per la riproduzione
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();

            // Crea il socket per ricevere pacchetti UDP
            DatagramSocket socket = new DatagramSocket(PORT);
            byte[] buffer = new byte[BUFFER_SIZE];

            System.out.println("Voice Chat Server is running...");

            // Loop per gestire i pacchetti audio in arrivo
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Aggiunge il nuovo client all'elenco
                ClientInfo client = new ClientInfo(packet.getAddress(), packet.getPort());
                clients.add(client);

                // Inoltra il pacchetto a tutti gli altri client
                forwardPacketToClients(socket, packet, client);

                // Riproduci l'audio in locale
                //speaker.write(packet.getData(), 0, packet.getLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo per inoltrare il pacchetto a tutti i client tranne quello che l'ha inviato
    private static void forwardPacketToClients(DatagramSocket socket, DatagramPacket packet, ClientInfo sender) {
        byte[] data = packet.getData();
        for (ClientInfo client : clients) {
            // Non invia il pacchetto al client mittente
            if (!client.equals(sender)) {
                try {
                    DatagramPacket forwardPacket = new DatagramPacket(data, data.length, client.address, client.port);
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
    }
}

import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VoiceChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 50005;
    private static final int BUFFER_SIZE = 2;

    public static void main(String[] args) {
        try {
            // Set up audio format for capturing
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            // Set up client socket
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            byte[] buffer = new byte[BUFFER_SIZE];

            System.out.println("Voice Chat Client is sending audio...");

            // Capture and send audio data
            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverAddress, SERVER_PORT);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class VoiceChatServer {
    private static final int PORT = 50005;
    private static final int BUFFER_SIZE = 2;

    public static void main(String[] args) {
        try {
            // Set up audio format for playback
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();

            // Set up server socket
            DatagramSocket socket = new DatagramSocket(PORT);
            byte[] buffer = new byte[BUFFER_SIZE];

            System.out.println("Voice Chat Server is running...");

            // Receive and play audio data
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                speaker.write(packet.getData(), 0, packet.getLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

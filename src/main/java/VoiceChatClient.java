import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

public class VoiceChatClient extends JFrame implements KeyListener {
    private static final String SERVER_ADDRESS = "localhost";  // Cambia con l'IP del server
    private static final int SERVER_PORT = 50005;
    private static final int BUFFER_SIZE = 4096;

    private boolean isTalking = false;      // Stato push-to-talk
    private boolean isReceiving = false;    // Stato per bloccare l'invio durante la ricezione

    public VoiceChatClient() {
        // Configura la finestra per ascoltare gli eventi del tasto
        setTitle("Voice Chat Client");
        setSize(200, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addKeyListener(this);
        setVisible(true);
    }

    public static void main(String[] args) {
        new VoiceChatClient().startVoiceChat();
    }

    public void startVoiceChat() {
        try {
            // Configura il formato audio
            AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, true);

            // Configura il microfono per la cattura audio
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(format);
            microphone.start();

            // Configura lo speaker per la riproduzione audio
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();

            // Crea il socket per inviare e ricevere pacchetti
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            byte[] buffer = new byte[BUFFER_SIZE];

            System.out.println("Voice Chat Client is running...");

            // Thread per inviare l'audio al server (solo quando si tiene premuto il tasto Y e non in ricezione)
            new Thread(() -> {
                try {
                    while (true) {
                        if (isTalking && !isReceiving) {  // Invia solo se è abilitato il push-to-talk e non si sta ricevendo
                            int bytesRead = microphone.read(buffer, 0, buffer.length);
                            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverAddress, SERVER_PORT);
                            socket.send(packet);
                        }
                        // Evita l'uso eccessivo della CPU
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Thread per ricevere l'audio dal server e riprodurlo
            new Thread(() -> {
                try {
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        // Attiva la modalità "ricezione" e disabilita l'invio
                        isReceiving = true;
                        System.out.println("Ricezione audio in corso. Invio disabilitato.");

                        // Riproduce l'audio ricevuto tramite lo speaker
                        speaker.write(packet.getData(), 0, packet.getLength());

                        // Terminata la ricezione, disabilita la modalità "ricezione"
                        isReceiving = false;
                        System.out.println("Fine ricezione audio. Invio abilitato.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodi per rilevare la pressione e il rilascio del tasto Y
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_Y) {
            if (!isReceiving) {  // Permetti di parlare solo se non si sta ricevendo
                isTalking = true;
                System.out.println("Push-to-talk attivato");
            } else {
                System.out.println("Impossibile parlare, si sta ricevendo audio.");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_Y) {
            isTalking = false;
            System.out.println("Push-to-talk disattivato");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilizzato, ma richiesto dall'interfaccia KeyListener
    }
}

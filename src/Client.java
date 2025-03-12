import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

class Client implements Runnable {
    Socket socket;

    Scanner in;
    PrintStream out;

    ChatServer server;

    String userName;

    //�����������
    public Client(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        // ��������� �����
        new Thread(this).start();
    }

    void receive(String message) {
        out.println(message);
    }

    public String getUserName() {
        return userName;
    }

    public void sendMessageToUser(String recipient, String message) {
        for (Client client : server.getClients()) {
            if (client.getUserName().equals(recipient)) {
                client.receive(userName + " -> " + recipient + ": " + message);
                break;
            }
        }
    }

    public void run() {
        try {
            // �������� ������ ����� � ������
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // ������� ������� �������� ����� � ������
            in = new Scanner(is);
            out = new PrintStream(os);

            // ������ �� ���� � ����� � ����
            out.println("����� ���������� � ���!");
            out.println("��� ������ �� ���� ������� - exit");
            out.println("������� ���:");

            //���� �� ������ ��� �� ������������ � ����
            //���� �� ����, �� ��� ���� ��������
            //� �� ��� �� ����, � ��� �����

            // �������� ���� ����
            userName = in.nextLine();
            out.println("����� ���������� � ���, " + userName + "!");
            String input = in.nextLine();
            while (!input.equals("exit")) {
                if (input.startsWith("@")) {
                    int spaceIndex = input.indexOf(' ');
                    if (spaceIndex != -1) {
                        String recipient = input.substring(1, spaceIndex);
                        String message = input.substring(spaceIndex + 1);
                        sendMessageToUser(recipient, message);
                    } else {
                        out.println("�������� ������. ����������� '@���� �����'.");
                    }
                } else {
                    server.sendAll(userName + ": " + input);
                }
                input = in.nextLine();
            }
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
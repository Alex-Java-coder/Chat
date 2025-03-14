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

    //конструктор
    public Client(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        // запускаем поток
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
            // получаем потоки ввода и вывода
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // создаем удобные средства ввода и вывода
            in = new Scanner(is);
            out = new PrintStream(os);

            // читаем из сети и пишем в сеть
            out.println("Добро пожаловать в Супер чат!");
            out.println("Для выхода из чата введите - пока");
            out.println("Введите имя:");

            // Основной цикл чата
            userName = in.nextLine();
            out.println("Добро пожаловать в чат, " + userName + "!");
            server.sendAll(userName + " к чату.");
            String input = in.nextLine();
            while (!input.equals("пока")) {
                if (input.startsWith("@")) {
                    int spaceIndex = input.indexOf(' ');
                    if (spaceIndex != -1) {
                        String recipient = input.substring(1, spaceIndex);
                        String message = input.substring(spaceIndex + 1);
                        sendMessageToUser(recipient, message);
                    } else {
                        out.println("Неверный формат. Используйте '@кому текст'.");
                    }
                } else {
                    server.sendAll(userName + ": " + input);
                }
                input = in.nextLine();
            }
            // Уведомление остальных пользователей о выходе участника из чата
            server.sendAll(userName + " покинул чат.");

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
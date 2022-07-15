import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        int port = 8989;

        System.out.println("Starting server at " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                ) {
                    final String searchWord = in.readLine();

                    if (searchWord.equals("stop")) {
                        serverSocket.close();
                        out.close();
                        in.close();
                        break;
                    }

                    List<PageEntry> result = engine.search(searchWord);

                    if (!result.isEmpty()) {
                        out.println("Слово " + searchWord + " найдено в следующих документах:");
                        Type listType = new TypeToken<List<PageEntry>>() {
                        }.getType();
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();

                        out.println(gson.toJson(result, listType));
                    } else {
                        out.println("Слово " + searchWord + " в документах не найдено...");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера");
            e.printStackTrace();
        }
    }
}
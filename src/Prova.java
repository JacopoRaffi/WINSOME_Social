import Server.ServerBackup;
import Server.ServerPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.ConcurrentHashMap;

public class Prova {

    public static void main(String[]Args) throws Exception{
        ConcurrentHashMap<Long, ServerPost> map = new ConcurrentHashMap<>();
        ServerPost post = new ServerPost((long)1, "titolo", "conenu7to", "autorte");
        map.put((long)1, post);
        map.put((long)2, post);
        map.put((long)3, post);
        map.put((long)4, post);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter wr = new FileWriter("file.txt");
        wr.write(gson.toJson(map));
        wr.close();

    }
}

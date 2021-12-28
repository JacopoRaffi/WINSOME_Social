import Server.ServerPost;

public class Prova {

    public static void main(String[]Args){
        ServerPost post = new ServerPost((long)1, "titolo", "contenuto", "jacopo");
        post.addComment("sono bello", "gioele");
        post.addComment("sono brutto", "gioele");
        post.ratePost("giole", 1);
        post.ratePost("giolee", 1);
        ServerPost post2 = new ServerPost(post);
        System.out.println(post2.getComments() + "  " + post2.getLikes());
        post.ratePost("gioleee", 1);
        post.ratePost("gioleeee", 1);
        post.addComment("sono bipolare", "gioele");
        System.out.println(post2.getComments() + "  " + post2.getLikes());

    }
}

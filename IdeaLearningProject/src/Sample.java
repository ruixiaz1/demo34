import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

class CodeAssistance {

    public static final String EXT = "txt";

    public static void main(String[] args) throws FileNotFoundException {
        FileReader fileReader = new FileReader("input." + EXT);
        BufferedReader reader = new BufferedReader(fileReader);
        ArrayList<String> lines = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        String[] array = lines.toArray(new String[lines.size()]);
        Arrays.sort(array);
        for (String s : array) System.out.println(s);
    }
}

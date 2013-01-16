package UvA.agents;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI {
    public static void save(Object obj,String path) throws FileNotFoundException, IOException 
    {

    	System.out.println("Printing to " + path);

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(obj);
        oos.flush();
        oos.close();
    }
    public static Object load(String path) throws FileNotFoundException, IOException, ClassNotFoundException 
    {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        Object result = ois.readObject();
        ois.close();
        return result;
    }
}
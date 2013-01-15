package UvA.agents;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SLAPI {
    public static void save(Object obj,String path) throws Exception
    {
    	System.out.println("Printing to " + path);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        System.out.println("Done writing!");
    }
    
    public static Object load(String path) throws Exception
    {
    	System.out.println("Loading qValues from " + path);
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
        Object result = ois.readObject();
        ois.close();
        System.out.println("Done reading!");
        return result;
    }
}
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JGlasses {
    /*
      Fix URLClassLoader protected method
    */
    static class FixedURLClassLoader extends URLClassLoader {
        public FixedURLClassLoader(URL[] urls) { super(urls); }
        public Class<?> findClass(String name) throws ClassNotFoundException { return super.findClass(name); }
    }
    
    public static void main(String[] args) throws IOException {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        URL[] urls = new URL[args.length];
        for (int i = 0; i < args.length; i++) {
            urls[i] = new File(args[i]).toURI().toURL();
        }
        FixedURLClassLoader loader = new FixedURLClassLoader(urls);
        
        for (String arg : args) {
            System.out.println("Opening '" + arg + "'");
            
            JarFile jar = new JarFile(arg);
            for (JarEntry entry : Collections.list(jar.entries())) {
                String file = entry.getName();

                if (file.endsWith(".class")) {
                    String classname = file.replace('/', '.').substring(0, file.length() - 6);
                    try {
                        Class<?> c = loader.findClass(classname);
                        classes.add(c);
                        System.out.println(classname);                        
                    } catch (Throwable e) {
                        System.out.println("FAILED " + classname + " from " + file);
                    }
                }
            }
        }
    }
}

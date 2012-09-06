import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class JGlasses {
    /*
      Fix URLClassLoader protected method
    */
    static class FixedURLClassLoader extends URLClassLoader {
        public FixedURLClassLoader(URL[] urls) { super(urls); }
        public Class<?> findClass(String name) throws ClassNotFoundException { return super.findClass(name); }
    }
    /*
        Remove extra quotes if in windows, and they are present.
    */ 
    public static String fix_wildcard(String in) {
        if (System.getProperty("os.name").toLowerCase().indexOf("win") < 0) {
            return in;
        }
        if (in.charAt(0) == '\'' && in.charAt(in.length()-1) == '\'' || 
            in.charAt(0) == '"' && in.charAt(in.length()-1) == '"') {
            return in.substring(1, in.length()-1);
        }
        return in;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: java JGlasses <class regex> <method regex> <jar filename> ...");
            System.err.println("Note: on windows regexen will need to be double quoted: \"'.*'\" for .*, to avoid their damn wildcard expansion that can't be avoided. Microsoft and Oracle hate us.");
            return;
        }

        Pattern class_regex = Pattern.compile(fix_wildcard(args[0]));
        Pattern method_regex = Pattern.compile(fix_wildcard(args[1]));
        String[] jar_names = Arrays.copyOfRange(args, 2, args.length); 

        URL[] urls = new URL[args.length];
        for (int i = 0; i < jar_names.length; i++) {
            urls[i] = new File(jar_names[i]).toURI().toURL();
        }
        FixedURLClassLoader loader = new FixedURLClassLoader(urls);
        
        for (String jar_name : jar_names) {
            JarFile jar = new JarFile(jar_name);
            for (JarEntry entry : Collections.list(jar.entries())) {
                String file = entry.getName();

                if (file.endsWith(".class")) {
                    String classname = file.replace('/', '.').substring(0, file.length() - 6);

                    if (class_regex.matcher(classname).find()) { 
                        try {
                            Class<?> c = loader.findClass(classname);
                            for (Method m : c.getDeclaredMethods()) {
                                String method_string = m.toString();
                                if (method_regex.matcher(method_string).find()) { 
                                    System.out.println(method_string);
                                }
                            }
                        } catch (Throwable e) {
                            System.err.println(classname + " not found.");
                        }
                    }
                }
            }
        }
    }
}

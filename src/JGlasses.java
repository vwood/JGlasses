import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.List;
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

    static FixedURLClassLoader loader;

    static Pattern class_regex = Pattern.compile("");
    static Pattern method_regex = Pattern.compile("");

    public static void print_methods(String classname) {
        if (class_regex.matcher(classname).find()) { 
            Class<?> c;
            try {
                c = loader.findClass(classname);
                System.err.println(classname);
            } catch (Throwable e) {
                System.err.println(classname + " not found.");
                return;
            }

            try {
                for (Method m : c.getDeclaredMethods()) {
                    String method_string = m.toString();
                    if (method_regex.matcher(method_string).find()) { 
                        System.out.println(method_string);
                    }
                }
            } catch (Throwable e) {
                System.err.println("  methods not found.");
            }
        }
    }

    public static List<String> getAllChildClasses(String parent_path) {
        File parent = new File(parent_path);

        List<String> result = new ArrayList<String>();
        List<String> queue = new ArrayList<String>();
        queue.add("");

        if (parent == null) { return result; }

        while (!queue.isEmpty()) {
            String pathname = queue.get(0);
            queue.remove(0);
            File path = new File(parent, pathname);

            if (path == null) { continue; }

            if (path.isDirectory()) {
                String[] children = path.list();
                if (children == null) { continue; }
                for (String child : path.list()) {
                    if (pathname.equals("")) {
                        queue.add(child);
                    } else {
                        queue.add(pathname + File.separator + child);
                    }
                }
            } else {
                if (pathname.endsWith(".class")) {
                    result.add(pathname);
                }
            }
        } 

        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Usage: java JGlasses <classpath> [class regex] [method regex]");
            System.err.println("Note: on windows regexen will need to be double quoted: \"'.*'\" for .*,");
            System.err.println(" to avoid their damn wildcard expansion that can't be avoided.");
            System.err.println(" Microsoft and Oracle hate us.");
            return;
        }

        String[] classpaths = args[0].split(";");
        if (args.length > 1) {
            class_regex = Pattern.compile(fix_wildcard(args[1]));
        }
        if (args.length > 2) {
            method_regex = Pattern.compile(fix_wildcard(args[2]));
        }

        URL[] urls = new URL[classpaths.length];
        for (int i = 0; i < classpaths.length; i++) {
            urls[i] = new File(classpaths[i]).toURI().toURL();
        }
        loader = new FixedURLClassLoader(urls);
        
        for (String path : classpaths) {
            if (path.endsWith(".jar")) {
                JarFile jar = new JarFile(path);
                for (JarEntry entry : Collections.list(jar.entries())) {
                    String file = entry.getName();
                    if (file.endsWith(".class")) {
                        String classname = file.replace('/', '.').substring(0, file.length() - 6);
                        print_methods(classname); 
                    }
                }
            } else {
                for (String file : getAllChildClasses(path)) {
                    if (file.endsWith(".class")) {
                        String classname = file.replace('/', '.').replace('\\', '.').substring(0, file.length() - 6);
                        print_methods(classname); 
                    }
                }
            }
        }
    }
}

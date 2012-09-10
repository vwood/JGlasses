JGlasses
========

Query JAR files and class files. Given regexen for class names and method signatures, this will output all the matching methods. 

Running
-------

~~~
java -jar JGlasses.jar <classpath> [class regex] [method regex]
~~~

Example
-------

~~~
% java -jar JGlasses.jar JGlasses.jar . main

public static void JGlasses.main(java.lang.String[]) throws java.io.IOException
~~~


JFLAGS = -d bin

JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

SOURCES = src/JGlasses.java
CLASSES = $(SOURCES:.java=.class)

default: jar

classes: $(CLASSES)

jar: classes
	cd bin; jar cvfe JGlasses.jar JGlasses *.class

run:
	$(JRE) -cp 'bin/' JGlasses

clean: 
	$(RM) bin/*.class

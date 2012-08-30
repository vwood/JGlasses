JFLAGS = -d bin

JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = src/JGlasses.java

default: classes

classes: $(CLASSES:.java=.class)

run:
	$(JRE) -cp 'bin/' JGlasses

clean: 
	$(RM) bin/*.class

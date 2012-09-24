JFLAGS = -d bin

JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

SOURCES = $(shell find . -type f)
CLASSES = $(SOURCES:.java=.class)

default: jar

classes: $(CLASSES)

jar: classes
	cd bin; jar cvfe JGlasses.jar src.JGlasses `find . -iname *.class`

run:
	$(JRE) -jar 'bin/JGlasses.jar'

clean: 
	$(RM) $(shell find bin/ -iname *.class)

JFLAGS = -g 
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ej3.java\
	monitor\

default: classes

classes: $(CLASSES:.java=.class)

monitor: monitor/*.class
	$(JC) monitor/*.java -d .

clean:
	$(RM) -r -f *.class 


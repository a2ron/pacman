
all: PacmanFrame.class

PacmanFrame.class: Cronometro.java Mapa.java PacmanFrame.java Sonido.java
	javac *.java
	
clean:
	rm *.class

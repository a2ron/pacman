import java.io.*;
import javax.sound.sampled.*;

public class Sonido {
	// Definimos las variables necesarias para reproducir sonido
  private AudioFormat audioFormat;
  private AudioInputStream audioInputStream;
  private SourceDataLine sourceDataLine;
  
  public Sonido(String path) {
    try{
    	// Creamos y cargamos el archivo de sonido
      File soundFile = new File(path);
      audioInputStream = AudioSystem.getAudioInputStream(soundFile);
      audioFormat = audioInputStream.getFormat();

      DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat);

      sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
		// Iniciamos la reproduccion
      new PlayThread().start();
    }catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
	// Hebra con la que reproduciremos el sonido
	class PlayThread extends Thread{
	  byte tempBuffer[] = new byte[10000];

	  public void run(){
		 try{
		 	// Abrimos el archivos y comenzamos el flujo de datos
		   sourceDataLine.open(audioFormat);
		   sourceDataLine.start();

		   int cnt;
		   // Leemos los datos
		   while((cnt = audioInputStream.read(
		        tempBuffer,0,tempBuffer.length)) != -1){
		     if(cnt > 0){
		     	// Escribimos los datos en la linea de sonido
		       sourceDataLine.write(tempBuffer, 0, cnt);
		     }
		   }
		   sourceDataLine.drain();
		   // Cerramos el flujo
		   sourceDataLine.close();
		 }catch (Exception e) {
		   e.printStackTrace();
		   System.exit(0);
		 }
	  }
	}

}

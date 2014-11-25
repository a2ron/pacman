import javax.swing.JLabel;

public class Cronometro implements Runnable{

    Thread crono;
    int minutos=0, segundos=0;
    boolean parar; // Se utiliza para parar la hebra
    JLabel lab; // Se mostrara el tiempo en el

    public Cronometro(JLabel l) {
        crono = new Thread(this);
        lab = l;
        parar = false;
        crono.start();
    }

	// Sirve para parar la hebra
  public void parar()
   {
       parar = true;
   }
	// Simulamos un cronometro incrementando las variables cada segundo
    public void run()
    {
        try {
        		String m,s;
            while(!parar) {
            	 // Incrementamos los minutos cada 60 segundos
                if(segundos==59) { segundos=0; minutos++; }
                segundos++;
                // Con esto conseguimos el formato 00:00
                if(minutos < 10) m = "0";
                else m = "";
                if(segundos < 10) s = "0";
                else s = "";
                // Mostramos el tiempo transcurrido
                lab.setText(m+minutos+":"+s+segundos);
                crono.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author francisco
 */
public class Mapa extends javax.swing.JPanel {

	 // Enumerados para la direccion y los elementos del juego
    private static enum Elemento {MURO,COCO,SUPERCOCO,PUERTA,VACIO};
    private static enum Direccion{ARRIBA,ABAJO,IZQUIERDA,DERECHA};

    //CLASE PERSONAJE

    private abstract class Personaje {

        //Atributos
        protected int fila,columna;
        protected BufferedImage imagenes[];
        protected BufferedImage img_actual;
        protected Direccion direccion;

        public Personaje(){
            fila = 0;
            columna = 0;
            direccion =  Direccion.ARRIBA;
            imagenes = null;
            img_actual = null;
        }

			// Establecemos la posicion en el mapa
        public void setPosicion(int fila,int columna){
            this.columna = (columna);
            this.fila = (fila);
        }

			// Se dibuja en el mapa
        public void pintate(Graphics2D g2d){
            g2d.drawImage(img_actual,Mapa.dim_cuadrado*columna, Mapa.dim_cuadrado*fila,null);
        }

			// Devuelve true si el personaje se topa con un muro
        protected Boolean seChoca(){
            int f = fila, c = columna;
            switch(direccion){
                case ARRIBA: f--; break;
                case ABAJO: f++; break;
                case IZQUIERDA: c--; break;
                case DERECHA: c++; break;
            }
            // Si se sale del mapa, hago que aparezca por el otro lado
            if(c < 0 || c >= matriz[0].length())
            {
               if( c < 0) c = matriz[0].length()-1;
               else c = 0;
            }
            Elemento vaPisar =get(f,c);
            return (vaPisar==Elemento.MURO || (vaPisar==Elemento.PUERTA && direccion==Direccion.ABAJO));
        }
    }

    //CLASE FANTASMA

    private class Fantasma extends Personaje implements Runnable {

       private int id;
       private Boolean comestible;
       private boolean parar;
       private Elemento pisado;

       public Fantasma(int n){
           id = n;
			// Establecemos la posicion en el mapa
			// Cargamos las imagenes que mostraremos en el juego
           imagenes = new BufferedImage[2];
           try {
                imagenes[0] = ImageIO.read((new File("img/gosht"+ n +".png")));
                imagenes[1] = ImageIO.read((new File("img/goshteatable.png")));
            } catch (IOException ex) {
                try {
                    imagenes[0] = ImageIO.read(new File("img/gosht1.png"));
                    imagenes[1] = ImageIO.read((new File("img/goshteatable.png")));
                } catch (IOException ex1) {
                    Logger.getLogger(Fantasma.class.getName()).log(Level.SEVERE, null, ex1);
                }
           }
           img_actual = imagenes[0];
           direccion = Direccion.ARRIBA;
           comestible=false;
           parar = false;
           pisado = Elemento.VACIO;
           id = n;
           Thread t=new Thread(this);
           t.start();
       }

			// Permite que los fantasmas puedan cambiar de direccion
			// pero nunca vuelven por donde han venido
        private void cambiaDireccion(){
            Direccion dirOld = direccion;
            java.util.Random rand=new java.util.Random();
            int random;
            random=rand.nextInt(3);
            if(direccion==Direccion.ARRIBA || direccion==Direccion.ABAJO){
                do{
                    switch (random){
                        case 0: direccion = (Direccion.IZQUIERDA); break;
                        case 1: direccion = (Direccion.DERECHA); break;
                        case 2: direccion = dirOld; break;
                    }
                    random=(random+1)%3;
                }while(seChoca());
            }
            else{
                do{
                    switch (random){
                        case 0: direccion = (Direccion.ARRIBA); break;
                        case 1: direccion = (Direccion.ABAJO); break;
                        case 2: direccion = dirOld; break;
                    }
                    random=(random+1)%3;
                }while(seChoca());
            }
        }

			// Establecemos la posicion en el mapa
			// Permite el movimiento del fantasma en el mapa
        public void run() {
            try {
                Thread.sleep(4000 + 1000*id);
            } catch (InterruptedException ex) {
                Logger.getLogger(Fantasma.class.getName()).log(Level.SEVERE, null, ex);
            }
            Boolean repetir;
            int f = 0,c=0;
            int contador = -1;
            while(!parar){
                do{
                    cambiaDireccion();
                    repetir = false;
                    f =  fila;
                    c =  columna;
                    switch(direccion){
                        case ARRIBA: f--; break;
                        case ABAJO: f++; break;
                        case IZQUIERDA: c--; break;
                        case DERECHA: c++; break;
                    }
                    if(contador<-5 || seChoca()){
                        if(contador<-5) contador=-1;
                        cambiaDireccion();
                        repetir = true;
                    }
                   else{
                        // Si se sale del mapa, hago que aparezca por el otro lado
                        if(c < 0 || c >= matriz[0].length())
                        {
                           if( c < 0) c = matriz[0].length()-1;
                           else c = 0;
                        }
                        if(pisado==Elemento.PUERTA) contador=10;
                        else pisado =  get(f,c);
                        fila = f;
                        columna = c;
                        if(comecocos.fila==f && comecocos.columna==c){
                            if(comestible)
                                eliminarFantasma(id-1);
                            else
                                matarComecocos();
                        }
                    }
                }while(repetir && !parar);
                repaint();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Fantasma.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(contador<0)contador--;
            }
        }

        public void setComestible(Boolean comestible) {
            this.comestible = comestible;
            if(comestible)
                img_actual = imagenes[1];
            else
                img_actual = imagenes[0];
        }
    }

    //CLASE COMECOCOS

    private class Comecocos extends Personaje implements Runnable {

        private int c_comidos;
        private int puntuacion;
        private boolean parar;
        private int n_f_comidos;
        private int n_vidas;

        public Comecocos(){
			// Cargamos las imagenes
            imagenes = new BufferedImage[8];
            try {
                imagenes[0] = ImageIO.read((new File("img/pacmanIzquieda.png")));
                imagenes[1] = ImageIO.read((new File("img/pacmanAbajo.png")));
                imagenes[2] = ImageIO.read((new File("img/pacmanDerecha.png")));
                imagenes[3] = ImageIO.read((new File("img/pacmanArriba.png")));
                imagenes[4] = ImageIO.read((new File("img/pacmanIzqCerrado.png")));
                imagenes[5] = ImageIO.read((new File("img/pacmanAbajoCerrado.png")));
                imagenes[6] = ImageIO.read((new File("img/pacmanDerCerrado.png")));
                imagenes[7] = ImageIO.read((new File("img/pacmanArribaCerrado.png")));
            } catch (IOException ex) {
                Logger.getLogger(Comecocos.class.getName()).log(Level.SEVERE, null, ex);
            }
            img_actual = imagenes[0];
            puntuacion = 0;
            c_comidos = 0;
            n_f_comidos = 0;
            n_vidas = 3;
            parar = false;
            setDireccion(Direccion.IZQUIERDA);
            Thread t=new Thread(this);
            t.start();
        }

			// Establecemos la direccion del comecocos y cargamos
			// la imagen que corresponde a esa direccion
        public void setDireccion(Direccion direccion) {
            int i=0;
            if((fila+columna)%2==1) i = 4;
            super.direccion=direccion;
            switch(direccion)
            {
                case IZQUIERDA:
                    img_actual = imagenes[0+i];
                break;
                case ABAJO:
                    img_actual = imagenes[1+i];
                break;
                case DERECHA:
                    img_actual = imagenes[2+i];
                break;
                case ARRIBA:
                    img_actual = imagenes[3+i];
                break;
            }
            repaint();
        }

			// Permite el movimiento del comecocos por el mapa
        public void run() {
            try {
                reproduce(0);
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Comecocos.class.getName()).log(Level.SEVERE, null, ex);
            }
            int f = 0,c=0;
            while(!parar){
                setDireccion(direccion);
                set(fila, columna, Mapa.Elemento.VACIO);
                f =  fila;
                c =  columna;
                // Calcula la siguiente posicion del comecocos en funcion de su direccion
                switch(direccion){
                    case ARRIBA: f--; break;
                    case ABAJO: f++; break;
                    case IZQUIERDA: c--; break;
                    case DERECHA: c++; break;
                }
                // Si se sale del mapa, hago que aparezca por el otro lado
                if(c < 0 || c >= matriz[0].length())
                {
                    if( c < 0) c = matriz[0].length()-1;
                    else c = 0;
                }
                if(!seChoca()) // Si no se encuentra un muro
                {
                    // Si hay un fantasma, se lo come si puede o muere
                    if( !hayFantasma(f, c))
                    {
                        fila = f;
                        columna = c;
                    }
                    switch(get(f,c))
                    {
                        case SUPERCOCO:
                            c_comidos++;
                            reproduce(2);
                            puntuacion += 50;
                            for(int i=0; i<fantasmas.length && i<6; ++i)
                               if(fantasmas[i]!=null)
                                    fantasmas[i].setComestible(true);
                            
									// Se encarga de poner a los fantasmas como comestibles por un tiempo
                            Thread t = new Thread(new Runnable() {
                                public void run() {
                                    reproduce(5);
                                    n_f_comidos = 0;
                                    try {
                                        // Tiempo que estaran los fantasmas en estado comestible
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Comecocos.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    for(int i=0; i<fantasmas.length && i<6; ++i)
                                        if(fantasmas[i]!=null)
                                            fantasmas[i].setComestible(false);
                              }
                            });
                            t.start();
                        break;
                        case COCO:
                            reproduce(1);
                            puntuacion += 10;
                            c_comidos++;
                        break;
                    }
                    if(c_comidos==num_cocos)
                        finalizarJuego();
                }
                repaint();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Comecocos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
			// Devuelve true si hay un fantasma, se lo come si puede o muere
        public boolean hayFantasma(int f, int c)
        {
        		// Obtiene la posicion de los fantasmas y mira si hay alguno
        		// en la posicion a la que va a pasar
            for(int i=0; i<fantasmas.length; ++i)
                if(fantasmas[i]!=null)
                {
                    int posx = fantasmas[i].fila;
                    int posy = fantasmas[i].columna;
                    if(posx == f && posy == c)
                        if(fantasmas[i].comestible) // Si se lo puede comer
                        {
                            fila = f; columna = c;
                            n_f_comidos++;
                            eliminarFantasma(i);
                            puntuacion += (int) (Math.pow(2, n_f_comidos) * 100);
                            return true;
                        }
                        else // Si no es comestible
                        {
                            matarComecocos();
                            return true;
                        }
                }
            return false;
        }
    }

    //IMPLEMENTACION DEL MAPA

    // Dimension del cuadrado correspondiente a una posicion de la rejilla
    final static int dim_cuadrado = 20;
    // BufferedImagen para pintar el coco y el supercoco
    private static BufferedImage icoco = null;
    private static BufferedImage isupercoco = null;
    // Rejilla
    private String _matriz[] ={
        "MMMMMMMMMMMMMMMMMMMMMMMMMMMM",
        "M............MM............M",
        "M.MMMM.MMMMM.MM.MMMMM.MMMM.M",
        "MOMMMM.MMMMM.MM.MMMMM.MMMMOM",
        "M.MMMM.MMMMM.MM.MMMMM.MMMM.M",
        "M..........................M",
        "M.MMMM.MM.MMMMMMMM.MM.MMMM.M",
        "M.MMMM.MM.MMMMMMMM.MM.MMMM.M",
        "M......MM....MM....MM......M",
        "MMMMMM.MMMMM MM MMMMM.MMMMMM",
        "MMMMMM.MMMMM MM MMMMM.MMMMMM",
        "MMMMMM.MM          MM.MMMMMM",
        "MMMMMM.MM MMPPPPMM MM.MMMMMM",
        "MMMMMM.MM M      M MM.MMMMMM",
        "      .   M      M   .      ",
        "MMMMMM.MM M      M MM.MMMMMM",
        "MMMMMM.MM MMMMMMMM MM.MMMMMM",
        "MMMMMM.MM          MM.MMMMMM",
        "MMMMMM.MM MMMMMMMM MM.MMMMMM",
        "MMMMMM.MM MMMMMMMM MM.MMMMMM",
        "M............MM............M",
        "M.MMMM.MMMMM.MM.MMMMM.MMMM.M",
        "M.MMMM.MMMMM.MM.MMMMM.MMMM.M",
        "MO..MM................MM..OM",
        "MMM.MM.MM.MMMMMMMM.MM.MM.MMM",
        "MMM.MM.MM.MMMMMMMM.MM.MM.MMM",
        "M......MM....MM....MM......M",
        "M.MMMMMMMMMM.MM.MMMMMMMMMM.M",
        "M.MMMMMMMMMM.MM.MMMMMMMMMM.M",
        "M..........................M",
        "MMMMMMMMMMMMMMMMMMMMMMMMMMMM",
        };
    private String matriz[] = _matriz.clone();
    // Sonidos
    final static String[] sounds = {
        "sound/intro.wav",
        "sound/come.wav",
        "sound/supercoco.wav",
        "sound/fantasma.wav",
        "sound/muere.wav",
        "sound/intermision.wav"
    };
    private Comecocos comecocos;
    private Fantasma [] fantasmas;
    private Sonido sonido;
    private Boolean fin = false;
    // numero de cocos en el mapa
    private int num_cocos = 245;

	// reproduce el sonido que ocupa la posicion i en el conjunto soudns
    public void reproduce(int i)
    {
        sonido = new Sonido(sounds[i]);
    }

	// Permite modificar el mapa
    public void set(int f, int c, Elemento v)
    {
        String str = "" + matriz[f].charAt(c);
        switch( v )
        {
            case VACIO:
                str = " ";
            break;
            case COCO:
                str = ".";
            break;
            case SUPERCOCO:
                str = "O";
            break;
            default:
                System.out.println("NO SE PUEDE PONER " +  v.toString());
        }
        matriz[f] = matriz[f].substring(0,c) + str + matriz[f].substring(c+1,matriz[f].length());
    }

	// Devuelve una posicion concreta del mapa
    public Elemento get(int f, int c)
    {
        char str = matriz[f].charAt(c);
        switch( str )
        {
            case 'M':
                return Elemento.MURO;
            case ' ':
                return Elemento.VACIO;
            case '.':
                return Elemento.COCO;
            case 'O':
                return Elemento.SUPERCOCO;
            case 'P':
                return Elemento.PUERTA;

        }
        return Elemento.MURO;
    }

	// Dibuja el mapa
    public void pintate(Graphics2D g)
    {
        g.setColor(Color.orange);
        for(int i=0; i<matriz.length; ++i)
            for(int j=0; j<matriz[0].length(); ++j)
            {
                char str = matriz[i].charAt(j);
                switch( str )
                {
                    case 'M':
                        g.fillRect(j*dim_cuadrado, i*dim_cuadrado, dim_cuadrado, dim_cuadrado);
                    break;
                    case 'P':
                        g.drawLine(j*dim_cuadrado, i*dim_cuadrado + dim_cuadrado/2, j*dim_cuadrado + dim_cuadrado, i*dim_cuadrado + dim_cuadrado/2);
                    break;
                    case '.':
                        g.drawImage(icoco, null,j*dim_cuadrado, i*dim_cuadrado);
                    break;
                    case 'O':
                        g.drawImage(isupercoco, null,j*dim_cuadrado, i*dim_cuadrado);
                    break;
                }
            }
    }

	// Permite iniciar un juego nuevo
    public void iniciarJuego(){
        finalizarJuego();
        fin = false;
        matriz = _matriz.clone();
        // Creamos el comecocos y lo situamos en el mapa
        comecocos = new Comecocos();
        comecocos.setPosicion(23,14);
        // Creamos los fantasmas y los situamos en el mapa
        fantasmas = new Fantasma[4];
        for(int i=0; i<fantasmas.length; ++i)
        {
	      fantasmas[i] = new Fantasma(i+1);
	      fantasmas[i].setPosicion(15,11+i);
        }
        repaint();
    }

    public Mapa(){
        initComponents();
        try{
            icoco = ImageIO.read(new File("img/coco.png"));
            isupercoco = ImageIO.read(new File("img/supercoco.png"));
        } catch (IOException ex) {
            Logger.getLogger(Mapa.class.getName()).log(Level.SEVERE, null, ex);
        }
        iniciarJuego();
    }

	// Resetea un fantasma cuando este es comido
    public void eliminarFantasma(int i)
    {
        reproduce(3);
        fantasmas[i].setPosicion(15,11+i);
    }
    
	// Permite obtener el no de vidas actuales del comecocos
	// Si el comecocos muere, se decrementa el no de vidas y si es 0, finaliza el juego
    public void matarComecocos()
    {
        reproduce(4);
        comecocos.n_vidas--;
        // Si no le quedan mas vidas
        if(comecocos.n_vidas <=0 )
            finalizarJuego();
        else
        {
            comecocos.setPosicion(23,14);
            for(int i=0; i<fantasmas.length; ++i)
                  fantasmas[i].setPosicion(15,11+i);
            repaint();
        }
        
    }

	// Permite obtener el no de vidas actuales del comecocos
    public int getVidasComecocos()
    {
        return comecocos.n_vidas;
    }

	// Permite saber si se ha terminado el juego o no
    public boolean isFinished()
    {
        return fin;
    }

	// FInaliza el juego parando todas las hebras de movimiento
    public void finalizarJuego()
    {
        fin = true;
        if(comecocos!=null)
            comecocos.parar=true;
        if(fantasmas!=null)
            for(int i=0; i<fantasmas.length; ++i)
               if(fantasmas[i]!=null)
                  fantasmas[i].parar=true;
    }

	// Permite obtener la puntuacion actual que posee el comecocos
    public int getPuntuacion(){
        if(comecocos!=null)
            return comecocos.puntuacion;
        return -1;
    }
    
	// Metodo que se encarga de pintar todo el juego
    @Override
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        // Pintamos el mapa
        pintate(g2d);
        // Pintamos el comecocos
        comecocos.pintate(g2d);
        // Pintamos los fantasmas
        for(int i=0; i<fantasmas.length && i<6; ++i)
            if(fantasmas[i]!=null)
                fantasmas[i].pintate(g2d);
        // Si el juego ha terminado, mostramos un mensaje de fin
        if(fin){
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Algerian",Font.BOLD,50));
            g2d.drawString("Juego Terminado",(matriz[0].length()*dim_cuadrado)/12,300);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(1, 1, 1));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMaximumSize(new Dimension(dim_cuadrado*matriz[0].length(),dim_cuadrado*matriz.length));
        setMinimumSize(new Dimension(dim_cuadrado*matriz[0].length(),dim_cuadrado*matriz.length));
        setPreferredSize(new Dimension(dim_cuadrado*matriz[0].length(),dim_cuadrado*matriz.length));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        // TODO add your handling code here:
        requestFocus();
    }//GEN-LAST:event_formMouseEntered

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // Dependiendo del tipo de tecla pulsada se hace una aacion u otra
        switch(evt.getKeyCode())
        {
            case KeyEvent.VK_LEFT:
                comecocos.setDireccion(Direccion.IZQUIERDA);
            break;
            case KeyEvent.VK_DOWN:
                comecocos.setDireccion(Direccion.ABAJO);
            break;
            case KeyEvent.VK_RIGHT:
                comecocos.setDireccion(Direccion.DERECHA);
            break;
            case KeyEvent.VK_UP:
                comecocos.setDireccion(Direccion.ARRIBA);
            break;
        }
    }//GEN-LAST:event_formKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}

import monitor.*;
import java.util.Random;


/*
Clase Token
Mecanismo usado por el servidor y los procesos para comunicarse y sincronizar la entrada
a la seccion critica a ejecutar
*/

class Token extends AbstractMonitor{
	
	
	private int suma = 0;
	//Variables auxiliares para controlar la exclusión
	private volatile boolean ejecutando=false;
 	private volatile boolean espera=true;
 	private volatile boolean libre=true;
 	private volatile boolean salida=false;
 	
    	//Variables Condition que actúan como cerrojos
  	private final Condition salida_pro = makeCondition();
  	private final Condition proceso   = makeCondition();
  	private final Condition servidor = makeCondition();
  	
  	
  	//Función que ejecutan los clientes para obtener el tokens
	public void obtener_token (){
		enter();
		
		//El cliente se espera si el token ya está cogido
		if(!espera)
			proceso.await();
		//Si no, el cliente se espera a que el servidor tenga el tokens
		
		espera=false;						
		leave();
	}
	
	public void devolver_token (){
		enter();
		

		//El cliente se espera si el token ya está cogido
		proceso.signal();

		leave();
	}
	
	
	
}

//fin monitor -------------------------------------------------------------
/*
Clase Proceso
Pide token, lo recibe procede a la que tenga que hacer y lo devuelve
*/
class Proceso implements Runnable {
	int tiempo=2000;
	
	private Token testigo;
	public Proceso (Token t1) {
	this.testigo=t1;
	
	}
	public void run (){
		while (true){
		
			this.testigo.obtener_token ();
			
			 System.out.println(Thread.currentThread().getName() + " Ejecutando seccion critica ");//SECCION CRITICA
			
			
			try { Thread.sleep (tiempo); } catch (InterruptedException e ) {}

             this.testigo.devolver_token ();

             
		}
	}
}
//
/*
Clase servidor
Da token a los procesos

*/




class Ejemplo {
  public static void main(String[] args) {

    if(args.length != 1) {
      System.err.println("Uso: num_clientes");
    }
    else {
    try {
          
     Token testigo =new Token();
     
     Thread[] procesos = new Thread[Integer.parseInt(args[0])];
     for(int i = 0; i < procesos.length; i++) 
       procesos[i] = new Thread(new Proceso(testigo), "proceso "+(i+1));
       

     
       
     for(int i = 0; i < procesos.length; i++)
       procesos[i].start();
     
       
    }
      catch(Exception e) {
       System.err.println("Excepcion en principal: " + e);
      }
  }
}
}

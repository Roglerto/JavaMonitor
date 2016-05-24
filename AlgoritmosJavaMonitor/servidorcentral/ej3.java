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
		else { 
			espera=false;						
			if(!servidor.isEmpty())
				servidor.signal();			
			salida_pro.await();
		}
		leave();
	}
	
	//Función para dar un token a un cliente
	public void daToken (){
		enter();
		
		//Si no hay procesos a los que dar token, que espere el servidor
		if(proceso.isEmpty())
			servidor.await();
		//Si no, el servidor daría paso al cliente y esperaría a que termines
		else {
			salida_pro.signal();
			servidor.await();   
		}
		leave();
	}
	
	//Procedimiento que avisa que el cliente ha terminado y devuelve el token
	public void devuelveToken (){
	enter();					
		servidor.signal();
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
			
			 System.out.println(Thread.currentThread().getName() + " Coge Token y procede a ejecucion ");//SECCION CRITICA
			
			try { Thread.sleep (tiempo); } catch (InterruptedException e ) {}
			
			System.out.println("Devuelve token");
			this.testigo.devuelveToken ();
		}
	}
}
//
/*
Clase servidor
Da token a los procesos

*/

class Servidor implements Runnable {
	int tiempo=2000;
	
	private Token testigo ;
	public Servidor (Token t2) {
	this.testigo=t2;
	
	}
	public void run () {
		while (true){
                        System.out.println("Da token "); 
                        
			this.testigo.daToken ();
			
			
			
			try { Thread.sleep (tiempo); } catch (InterruptedException e) {}
			
		}
	}
}


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
       
     Thread servidor = new Thread(new Servidor(testigo),"servidor ");
     
       
     for(int i = 0; i < procesos.length; i++)
       procesos[i].start();
     
       servidor.start();
    }
      catch(Exception e) {
       System.err.println("Excepcion en principal: " + e);
      }
  }
}
}

package src;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;


public class SimuladorSwapping {
    String tipoMemoria; // "LRU" o "FIFO"
    Proceso[] memoriaPrincipal;
    Proceso[] memoriaIntercambio;
    Stack<Proceso> colaLRUMain;
    Queue<Proceso> colaFIFOMain;
    Stack<Proceso> colaLRUSwap;
    Queue<Proceso> colaFIFOSwap;
    int quantumDefault = 2;
    private final static int HILOS = 4;
    //private CyclicBarrier barrier;
    long startTime = System.currentTimeMillis();
    int FPS = 27;

    public SimuladorSwapping(int tamano_memoria_intercambio, int tamano_memoria_principal, String tipoMemoria) {
        this.tipoMemoria = tipoMemoria;
        this.memoriaPrincipal = new Proceso[tamano_memoria_principal];
        this.memoriaIntercambio = new Proceso[tamano_memoria_intercambio];
        this.colaLRUMain = new Stack<>();
        this.colaFIFOMain = new LinkedList<>();
        this.colaLRUSwap = new Stack<>();
        this.colaFIFOSwap = new LinkedList<>();
        //this.barrier = new CyclicBarrier(HILOS, () -> {
        //    actualizarINF();
        //});
    }

    public void agregarProceso(String nombre, int quantum) {
        Proceso nuevoProceso = new Proceso(nombre, quantum);
        Proceso aux = nuevoProceso; // auxiliar para agregar a la cola
        Proceso aux2 = nuevoProceso; // auxiliar para agregar a la cola
        Boolean espacioDisponible = true;
        Boolean espacioEnSwap = true;
        /**
         * Agregar proceso a la memoria principal si hay espacio disponible
         */
        for (int i = 0; i < memoriaPrincipal.length; i++) {
            if (memoriaPrincipal[i] == null) {
                memoriaPrincipal[i] = nuevoProceso;
                break;
            }
            // Si no hay espacio disponible, utilizar memoria de intercambio y agregar a la cola
            if (i == memoriaPrincipal.length - 1) {
                //System.out.println("No hay espacio en la memoria principal");
                //System.console().readLine();
                espacioDisponible = false;
            }
        }
        if (!espacioDisponible) {
            if (tipoMemoria.equals("LRU")) {
                // sacamos de la cola el proceso el ultimo que se uso
                aux = colaLRUMain.pop();
                colaFIFOMain.remove(aux);
                // buscamos el proceso en la memoria principal
                for (int i = 0; i < memoriaPrincipal.length; i++) {
                    if (memoriaPrincipal[i].equals(aux)) {
                        // agregamos el proceso a la memoria de intercambio
                        memoriaPrincipal[i] = nuevoProceso;
                        for (int j = 0; j < memoriaIntercambio.length; j++) {
                            if (memoriaIntercambio[j] == null) {
                                memoriaIntercambio[j] = aux;
                                colaLRUSwap.push(aux);
                                colaFIFOSwap.add(aux);
                                break;
                            }
                            // Si no hay espacio en la memoria de intercambio usamos la cola
                            if (j == memoriaIntercambio.length - 1) {
                                espacioEnSwap = false;
                            }
                        }
                        if (!espacioEnSwap) {
                            // sacamos de la cola el proceso que sea last recently used
                            aux2 = colaLRUSwap.pop();
                            colaFIFOSwap.remove(aux2);
                            // buscamos el proceso en la memoria intercamio
                            for (int k = 0; k < memoriaIntercambio.length; k++) {
                                if (memoriaIntercambio[k].equals(aux2)) {
                                    // agregamos el proceso a la memoria de intercambio
                                    memoriaIntercambio[k] = aux;
                                    colaLRUSwap.push(aux);
                                    colaFIFOSwap.add(aux);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            } else if (tipoMemoria.equals("FIFO")){
                // sacamos de la cola el proceso que sea first in first out
                aux = colaFIFOMain.poll();
                colaLRUMain.remove(aux);
                // buscamos el proceso en la memoria principal
                for (int i = 0; i < memoriaPrincipal.length; i++) {
                    if (memoriaPrincipal[i].equals(aux)) {
                        // agregamos el proceso a la memoria de intercambio
                        memoriaPrincipal[i] = nuevoProceso;
                        for (int j = 0; j < memoriaIntercambio.length; j++) {
                            if (memoriaIntercambio[j] == null) {
                                memoriaIntercambio[j] = aux;
                                colaFIFOSwap.add(aux);
                                colaLRUSwap.push(aux);
                                break;
                            }
                            // Si no hay espacio en la memoria de intercambio usamos la cola
                            if (j == memoriaIntercambio.length - 1) {
                                espacioEnSwap = false;
                            }
                        }
                        if (!espacioEnSwap) {
                            // sacamos de la cola el proceso que sea first in first out
                            aux2 = colaFIFOSwap.poll();
                            colaLRUSwap.remove(aux2);
                            // buscamos el proceso en la memoria intercamio
                            for (int k = 0; k < memoriaIntercambio.length; k++) {
                                if (memoriaIntercambio[k].equals(aux2)) {
                                    // agregamos el proceso a la memoria de intercambio
                                    memoriaIntercambio[k] = aux;
                                    colaFIFOSwap.add(aux);
                                    colaLRUSwap.push(aux);
                                    break;
                                }
                            }
                        }
                        // agregamos el proceso a la cola
                        break;
                    }
                }
            }
        }
        colaFIFOMain.add(nuevoProceso);
        colaLRUMain.add(nuevoProceso);
    }

    public void correr(){
        String titulo = "\n"+
            "\t┌───┐      ┌┐     ┌┐\n"+
            "\t│┌─┐│      ││     ││\n"+
            "\t│└──┬┬┐┌┬┐┌┤│┌──┬─┘├──┬─┐\n"+
            "\t└──┐├┤└┘││││││┌┐│┌┐│┌┐│┌┘\n"+
            "\t│└─┘│││││└┘│└┤┌┐│└┘│└┘││\n"+
            "\t└───┴┴┴┴┴──┴─┴┘└┴──┴──┴┘\n"+
            "\t┌───┐\n"+
            "\t│┌─┐│\n"+
            "\t│└──┬┐┌┐┌┬──┬──┬──┬┬─┐┌──┐\n"+
            "\t└──┐│└┘└┘│┌┐│┌┐│┌┐├┤┌┐┤┌┐│\n"+
            "\t│└─┘├┐┌┐┌┤┌┐│└┘│└┘│││││└┘│\n"+
            "\t└───┘└┘└┘└┘└┤┌─┤┌─┴┴┘└┴─┐│\n"+
            "\t            ││ ││     ┌─┘│\n"+
            "\t            └┘ └┘     └──┘\n";
        // mostramos un contador de tiempo de lo que llevamos de ejecucion en segundos
        this.startTime = System.currentTimeMillis();
        // limpiamos la consola
        System.out.print("\033[H\033[2J");
        System.out.flush();
        // mostramos el titulo en color verde
        System.out.println("\u001B[32m" + titulo + "\u001B[0m");
        System.out.println("Quedan procesos: " + (hayProcesos()? "si" : "no"));
        System.out.println("Tiempo: " + (System.currentTimeMillis() - startTime) / 1000 + " segundos");
        verTodosLosProcesos();

        Thread[] hilos = new Thread[HILOS];
        int tasa_de_refresco = 1000/FPS;
        // vinculamos el semaforo a los hilos
        for (int i = 0; i < HILOS; i++) {
            hilos[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    ejecutarProcesos();
                }
            });
            hilos[i].start();
        }
        //haremos un ultimo hilo que este actualizando la informacion cada 500 milisegundos
        Thread Informador = new Thread(new Runnable() {
            @Override
            public void run() {

                while (hayProcesos()) {
                    try {
                        Thread.sleep(tasa_de_refresco);// esto equivale a 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    actualizarINF();
                }
            }
        });
        Informador.start();
        // esperamos a que todos los hilos terminen
        try {
            Informador.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < HILOS; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void actualizarINF(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
        synchronized (this){
            String titulo = "\n"+
                "\t┌───┐      ┌┐     ┌┐\n"+
                "\t│┌─┐│      ││     ││\n"+
                "\t│└──┬┬┐┌┬┐┌┤│┌──┬─┘├──┬─┐\n"+
                "\t└──┐├┤└┘││││││┌┐│┌┐│┌┐│┌┘\n"+
                "\t│└─┘│││││└┘│└┤┌┐│└┘│└┘││\n"+
                "\t└───┴┴┴┴┴──┴─┴┘└┴──┴──┴┘\n"+
                "\t┌───┐\n"+
                "\t│┌─┐│\n"+
                "\t│└──┬┐┌┐┌┬──┬──┬──┬┬─┐┌──┐\n"+
                "\t└──┐│└┘└┘│┌┐│┌┐│┌┐├┤┌┐┤┌┐│\n"+
                "\t│└─┘├┐┌┐┌┤┌┐│└┘│└┘│││││└┘│\n"+
                "\t└───┘└┘└┘└┘└┤┌─┤┌─┴┴┘└┴─┐│\n"+
                "\t            ││ ││     ┌─┘│\n"+
                "\t            └┘ └┘     └──┘\n";
            // mostramos el titulo en color amarillo
            System.out.println("\u001B[33m" + titulo + "\u001B[0m");
            System.out.println("Quedan procesos: " + (hayProcesos()? "si" : "no"));
            System.out.println("Tiempo: " + (System.currentTimeMillis() - startTime) / 1000 + " segundos");
            verTodosLosProcesos();
        }
    }

    public void ejecutarProcesos() {
        /*String titulo = "\n"+
            "\t┌───┐      ┌┐     ┌┐\n"+
            "\t│┌─┐│      ││     ││\n"+
            "\t│└──┬┬┐┌┬┐┌┤│┌──┬─┘├──┬─┐\n"+
            "\t└──┐├┤└┘││││││┌┐│┌┐│┌┐│┌┘\n"+
            "\t│└─┘│││││└┘│└┤┌┐│└┘│└┘││\n"+
            "\t└───┴┴┴┴┴──┴─┴┘└┴──┴──┴┘\n"+
            "\t┌───┐\n"+
            "\t│┌─┐│\n"+
            "\t│└──┬┐┌┐┌┬──┬──┬──┬┬─┐┌──┐\n"+
            "\t└──┐│└┘└┘│┌┐│┌┐│┌┐├┤┌┐┤┌┐│\n"+
            "\t│└─┘├┐┌┐┌┤┌┐│└┘│└┘│││││└┘│\n"+
            "\t└───┘└┘└┘└┘└┤┌─┤┌─┴┴┘└┴─┐│\n"+
            "\t            ││ ││     ┌─┘│\n"+
            "\t            └┘ └┘     └──┘\n";*/
        
        // mostramos un contador de tiempo de lo que llevamos de ejecucion en segundos
        
        Proceso proceso = null;
        Proceso procesoEjecutado = null;
        while (hayProcesos()) {
            if (!hayProcesos()) {
                return;
            }
            // tomamos el proceso de la memoria principal dependiendo del algoritmo
            if (tipoMemoria.equals("LRU")) {
                // sacamos de la cola el proceso que sea last recently used
                synchronized (this) {
                    if (colaLRUMain.isEmpty()) {
                        return;
                    }else{
                        proceso = colaLRUMain.pop();
                        colaFIFOMain.remove(proceso);
                    }
                }
            } else if (tipoMemoria.equals("FIFO")) {
                // sacamos de la cola el proceso que sea first in first out
                synchronized (this) {
                    proceso = colaFIFOMain.poll();
                    colaLRUMain.remove(proceso);
                }
            }
            if (!hayProcesos()) {
                return;
            }
            for(int i = 0; i<quantumDefault && proceso != null; i++){
                proceso = run(proceso);
                // buscar el proceso en la memoria principal
                for (int j = 0; j < memoriaPrincipal.length; j++) {
                    synchronized (this) {
                        if (memoriaPrincipal[j] == null) {
                            break;
                        }else if (memoriaPrincipal[j].equals(proceso)) {
                            memoriaPrincipal[j] = proceso.quantum == 0 ? null : proceso;
                            break;
                        }
                    }
                }
                proceso = proceso.quantum == 0 ? null : proceso;
                /*try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }*/
            }
            if (!hayProcesos()) {
                return;
            }
            // si el proceso es distinto de null lo sacamos de memoria y lo volvemos a cargar
            procesoEjecutado = proceso;
            if (procesoEjecutado != null) {
                synchronized (this) {
                    eliminarProceso(procesoEjecutado.nombre);
                }
            }
            synchronized (this) {
                if (tipoMemoria.equals("LRU")) {
                    // sacamos de la cola el proceso que sea last recently used
                    if (!colaLRUSwap.isEmpty()) {
                        proceso = colaLRUSwap.pop();
                        colaFIFOSwap.remove(proceso);
                        // buscamos el proceso en la memoria intercamio
                        for (int k = 0; k < memoriaIntercambio.length; k++) {
                            if (memoriaIntercambio[k]!=null && memoriaIntercambio[k].equals(proceso)) {
                                // saca el proceso de la memoria de intercambio
                                memoriaIntercambio[k] = procesoEjecutado;
                                colaLRUSwap.push(procesoEjecutado);
                                colaFIFOSwap.add(procesoEjecutado);
                                colaLRUMain.push(proceso);
                                colaFIFOMain.add(proceso);
                                break;
                            }
                        }
                    }else{
                        proceso = null;
                        if (procesoEjecutado != null) {
                            agregarProceso(procesoEjecutado.nombre, procesoEjecutado.quantum);   
                        }
                    }
                } else if (tipoMemoria.equals("FIFO")){
                    // sacamos de la cola el proceso que sea first in first out
                    if (!colaFIFOSwap.isEmpty()) {
                        proceso = colaFIFOSwap.poll();
                        colaLRUSwap.remove(proceso);
                        // buscamos el proceso en la memoria intercamio
                        for (int k = 0; k < memoriaIntercambio.length; k++) {
                            if (memoriaIntercambio[k]!=null && memoriaIntercambio[k].equals(proceso)) {
                                //saca el proceso de la memoria de intercambio
                                memoriaIntercambio[k] = procesoEjecutado;
                                colaFIFOSwap.add(procesoEjecutado);
                                colaLRUSwap.push(procesoEjecutado);
                                colaFIFOMain.add(proceso);
                                colaLRUMain.push(proceso);
                            }
                        }
                    }else{
                        // si no hay procesos en la cola de swap agregamos el proceso a la memoria principal
                        proceso = null;
                        if(procesoEjecutado != null)
                            agregarProceso(procesoEjecutado.nombre, procesoEjecutado.quantum);
                    }
                }
            }
            if (!hayProcesos()) {
                return;
            }
            //System.out.println("se agrega a la memoria principal el proceso "+((proceso!=null)?proceso.nombre:"que no existe el proceso"));
            // agregamos el proceso a la memoria principal
            for (int j = 0; j < memoriaPrincipal.length; j++) {
                synchronized (this) {
                    if (memoriaPrincipal[j] == null) {
                        memoriaPrincipal[j] = proceso;
                        break;
                    }
                }
            } 
            // eliminamos todos los nulos que puedan estar en las listas de colas
            synchronized (this) {
                while (colaLRUMain.remove(null));
                while (colaFIFOMain.remove(null));
                while (colaLRUSwap.remove(null));
                while (colaFIFOSwap.remove(null));
            }
            //actualizarINF();System.out.print("\033[H\033[2J");
            
            /*try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }*/
            if (!hayProcesos()) {
                return;
            }
        }
    }
    private Proceso run(Proceso proceso){
        // correr proceso
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        proceso.quantum--;
        // si el proceso ya termino lo eliminamos
        if (proceso.quantum == 0) {
            eliminarProceso(proceso.nombre);
        }
        return proceso;
    }

    private boolean hayProcesos() {
        // Verificar si hay procesos en memoria principal
        for (Proceso proceso : memoriaPrincipal) {
            if (proceso != null) {
                return true;
            }
        }
        // Verificar si hay procesos en memoria de intercambio
        for (Proceso proceso : memoriaIntercambio) {
            if (proceso != null) {
                return true;
            }
        }
        return false;
    }   

    public void verProcesosMemoriaPrincipal() {
        // Mostrar procesos en memoria principal 
        // salida en color rosa
        System.out.println("\u001B[35mProcesos en memoria principal\u001B[0m");
        System.out.println("-----------------------------");
        for (Proceso proceso : memoriaPrincipal) {
            System.out.println("| " + ((proceso!=null) ? proceso : "      Celda | vacia      ") + " |");
        }
        System.out.println("-----------------------------");
    }

    public void verProcesosMemoriaIntercambio() {
        // Mostrar procesos en memoria de intercambio
        // salida en color rosa
        System.out.println("\u001B[35mProcesos en memoria de intercambio\u001B[0m");
        System.out.println("-----------------------------");
        for (Proceso proceso : memoriaIntercambio) {
            System.out.println("| " + ((proceso!=null) ? proceso : "      Celda | vacia      ") + " |");
        }
        System.out.println("-----------------------------");
    }

    public void verTodosLosProcesos() {
        ArrayList<String> salidas = new ArrayList<>();
        ArrayList<String[]> procesos = new ArrayList<>();
        String salida = "";
        // Mostrar todos los procesos
        // salida en color rosa
        salida = ("\u001B[35mProcesos en memoria principal\u001B[0m \t \u001B[35mProcesos en memoria de intercambio\u001B[0m");
        salidas.add(salida);
        int memoriaPrincipalLength = memoriaPrincipal.length;
        int memoriaIntercambioLength = memoriaIntercambio.length;
        int max = Math.max(memoriaPrincipalLength, memoriaIntercambioLength);
        int i = 0;
        for (i = 0; i < max; i++) {
            if (i >= memoriaPrincipalLength) {
                salida = ("\t|" + ((memoriaIntercambio[i]!=null) ? memoriaIntercambio[i] : "      Celda | vacia      ") + " |");
                salidas.add(salida);
                continue;
            }if (i >= memoriaIntercambioLength) {
                salida = ("| " + ((memoriaPrincipal[i]!=null) ? memoriaPrincipal[i] : "      Celda | vacia      ") + " | \t | \t |");
                salidas.add(salida);
                continue;
            }
            if (i<memoriaPrincipalLength && i<memoriaIntercambioLength){
                salida =  ("| " + ((memoriaPrincipal[i]!=null) ? memoriaPrincipal[i] : "      Celda | vacia      ") + " | \t | " +((memoriaIntercambio[i]!=null) ? memoriaIntercambio[i] : "      Celda | vacia      ") + " |");
                salidas.add(salida);
            }
        }
        // eliminamos los espacios vacios en las salidas
        procesos.add(salidas.get(0).split("\t"));
        for (i = 1; i < salidas.size(); i++) {
            salida = salidas.get(i);
            salida = salida.replace(" ", "");
            procesos.add(salida.split("\t"));
        }
        // calamos la cadena mas larga en 0 y 1
        int max0 = 0;
        int max1 = 0;
        for (i = 0; i < procesos.size(); i++) {
            String[] proceso = procesos.get(i);
            if (proceso[0].length() > max0) {
                max0 = proceso[0].length();
            }
            if (proceso[1].length() > max1) {
                max1 = proceso[1].length();
            }
        }
        // alargamos las cadenas mas cortas con espacios antes del ultimo |
        for (i = 0; i < procesos.size(); i++) {
            String[] proceso = procesos.get(i);
            if (proceso[0].length() < max0) {
                int diferencia = max0 - proceso[0].length();
                for (int j = 0; j < diferencia; j++) {
                    proceso[0] = " " + proceso[0];
                }
                procesos.set(i, proceso);
            }
            if (proceso[1].length() < max1) {
                int diferencia = max1 - proceso[1].length();
                for (int j = 0; j < diferencia; j++) {
                    proceso[1] = " " + proceso[1];
                }
                procesos.set(i, proceso);
            }
        }
        System.out.println(procesos.get(0)[0] + "\t\t" + procesos.get(0)[1]);
        // imprmimos tantos - como la cadena mas larga en 0 y 1 y separamos con un tab
        for (i = 0; i < max0; i++) {
            System.out.print("-");
        }
        System.out.print("\t");
        for (i = 0; i < max1; i++) {
            System.out.print("-");
        }
        System.out.println();
        // imprimimos los procesos
        for (i = 1; i < procesos.size(); i++) {
            System.out.println(procesos.get(i)[0] + "\t" + procesos.get(i)[1]);
        }
        for (i = 0; i < max0; i++) {
            System.out.print("-");
        }
        System.out.print("\t");
        for (i = 0; i < max1; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    public void verColaProcesos() {
        // Mostrar cola de procesos
        // salida en color rosa
        System.out.println("\u001B[35mCola de procesos Principal\u001B[0m");
        if (tipoMemoria.equals("LRU")) {
            for (Proceso proceso : colaLRUMain) {
                System.out.println("Proceso: " + proceso.nombre);
            }
        } else {
            for (Proceso proceso : colaFIFOMain) {
                System.out.println("Proceso: " + proceso.nombre);
            }
        }

        System.out.println("\u001B[35mCola de procesos Swap\u001B[0m");
        if (tipoMemoria.equals("LRU")) {
            for (Proceso proceso : colaLRUSwap) {
                System.out.println("Proceso: " + proceso.nombre);
            }
        } else {
            for (Proceso proceso : colaFIFOSwap) {
                System.out.println("Proceso: " + proceso.nombre);
            }
        }
    }

    public void cambiarAlgoritmo(String tipoMemoria) {
        // Cambiar algoritmo de reubicación
        this.tipoMemoria = tipoMemoria;
    }
    

    public void eliminarProceso(String nombre) {
        // Eliminar proceso de la memoria principal
        for (int i = 0; i < memoriaPrincipal.length; i++) {
            if (memoriaPrincipal[i] != null && memoriaPrincipal[i].nombre.equals(nombre)) {
                memoriaPrincipal[i] = null;
                break;
            }
        }

        // Eliminar proceso de la memoria de intercambio
        for (int i = 0; i < memoriaIntercambio.length; i++) {
            if (memoriaIntercambio[i] != null && memoriaIntercambio[i].nombre.equals(nombre)) {
                memoriaIntercambio[i] = null;
                break;
            }
        }

        // Eliminar proceso de la cola LRU
        for (int i = 0; i < colaLRUMain.size(); i++) {
            if (colaLRUMain.get(i) != null && colaLRUMain.get(i).nombre.equals(nombre)) {
                colaLRUMain.remove(i);
                break;
            }
        }
        for (int i = 0; i < colaLRUSwap.size(); i++) {
            if (colaLRUSwap.get(i)!=null && colaLRUSwap.get(i).nombre.equals(nombre)) {
                colaLRUSwap.remove(i);
                break;
            }
        }

        // Eliminar proceso de la cola FIFO
        for (int i = 0; i < colaFIFOMain.size(); i++) {
            if(colaFIFOMain.peek().nombre.equals(nombre)){
                colaFIFOMain.remove();
                break;
            }
        }

        for (int i = 0; i < colaFIFOSwap.size(); i++) {
            if(colaFIFOSwap.peek().nombre.equals(nombre)){
                colaFIFOSwap.remove();
                break;
            }
        }
    }
    public int getMemoriaDisponible(){
        int memoriaDisponible = 0;
        for (int i = 0; i < memoriaPrincipal.length; i++) {
            if (memoriaPrincipal[i] == null) {
                memoriaDisponible++;
            }
        }
        for (int i = 0; i < memoriaIntercambio.length; i++) {
            if (memoriaIntercambio[i] == null) {
                memoriaDisponible++;
            }
        }
        return memoriaDisponible;
    }

    public static void main(String[] args) {
        SimuladorSwapping simulador = new SimuladorSwapping(10, 5, "LRU");

        // Agregar procesos de ejemplo
        simulador.agregarProceso("ProcesoA", 5);
        simulador.agregarProceso("ProcesoB", 8);
        simulador.agregarProceso("ProcesoC", 3);

        // Ejecutar procesos y simular reubicación
        simulador.ejecutarProcesos();
        simulador.ejecutarProcesos();
    }
}

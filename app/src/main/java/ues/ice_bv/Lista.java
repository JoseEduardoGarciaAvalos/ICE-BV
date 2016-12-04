package ues.ice_bv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class Lista extends AppCompatActivity {

    private static final String TAG = "LOGl"; //Identificador en el log.
    private Utilidad utilidad = new Utilidad();
    private boolean hayHilo = false;

    private ListView jllv1;
    private ArrayAdapter datosLista;

    private Spinner jll1;
    private ArrayAdapter datosCombo;
    private static final String[] vista = {"Todo", "Alertas"};

    private TextView jltv3;
    private Button jlb2;
    private Button jlb3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista);
        // 1. Relacionar objetos
        jllv1 = (ListView) findViewById(R.id.jllv1);
        jll1 = (Spinner) findViewById(R.id.jll1);
        jltv3 = (TextView) findViewById(R.id.jltv3);
        jlb2 = (Button) findViewById(R.id.jlb2);
        jlb3 = (Button) findViewById(R.id.jlb3);
        // 2. Definir Datos y evento
        datosLista = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        jllv1.setAdapter(datosLista);
        jllv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),"Toco el elemento: " + datosLista.getItem(position).toString(), Toast.LENGTH_LONG).show();

                Intent i = new Intent(getApplicationContext(), Informacion.class);
                String[] aux = datosLista.getItem(position).toString().split("\n"); // Extraer el simbolo de la jllv1
                i.putExtra("simbolo", aux[1]); // El simoblo está en el segundo renglon
                utilidad.setPeticionCancelada(true);
                startActivity(i);
            }
        });
        datosCombo = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, vista);
        jll1.setAdapter(datosCombo);
        // 3. Cargar datosLista
        iniciarServicio();
        ver(null);
        utilidad.setCambioVariables(this, false);
    }

    // Métodos para los eventos en los botones.
    public void ver(View v) {
        utilidad.setPeticionCancelada(true);
        if (hayHilo) {
            setJlb2("Ver", false);
            return;
        } else {
            hayHilo = true;
            setJlb2("Cancelar", false);
            setJlb3(false);
        }
        // Si lo que está seleccionado en el combo es igual a vista[0] ...entonces
        if (jll1.getSelectedItem().toString().equals(vista[0])) {
            actualizarTodo(utilidad.getAcciones(this));
        } else {
            actualizarAlerta(utilidad.getAcciones(this));
        }
    }

    public void busqueda(View v) {
        utilidad.setPeticionCancelada(true);
        Intent i = new Intent(getApplicationContext(), Busqueda.class);
        startActivity(i);
    }

    public void acercaDe(View v) {
        utilidad.setPeticionCancelada(true);
        Intent i = new Intent(getApplicationContext(), AcercaDe.class);
        startActivity(i);
    }

    public void eliminar(View v) {
        utilidad.setPeticionCancelada(true);
        utilidad.eliminarTodo(this);
        utilidad.setPrimeraVez(this, false);
        utilidad.mostrarMensaje(this,"Se eliminarón todas las acciones");
        ver(null);
    }

    // Métodos para la actualización de datos
    public void actualizarTodo(final String simbolos) {
        // Android tiene dos reglas que se debe respetar, sino se cumplen lanzan excepción (se traba la aplicación) y manda un mensaje ARN ("Aplicación no responde")
        // 1- Cualquier consulta a la red (internet) deberá ejecutarse en un hilo aparte del principal
        // 2- Nunca deben llamarse objetos de vista dentro de los hilos.
        new Thread(new Runnable() {

            @Override
            public void run() {// El hilo se finaliza a sí mismo cuando termina su trabajo.
                Log.d(TAG, "inicio");
                // 1. Iniciar
                int i = 0;
                ejecutarAnimacion();
                utilidad.iniciarPeticion(Utilidad.A_LINEAxLINEA, simbolos, Utilidad.C_LISTA, 3);
                clearDatosLista();
                setJlb2("Cancelar", true);
                Log.d(TAG, "simbolos " + simbolos);
                // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algún error o se cancela por algún motivo.
                while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                    // 2.1 Conseguir valor.
                    String valor = utilidad.getPeticionParcial();
                    if (valor != null) {
                        i++;
                        // 2.1.1 Iniciar hilo UI para actualizarTodo la UI
                        setDatosLista(valor);
                    }
                }
                String resultado = i + " resultado" + (i != 1 ? "s" : "");
                setJltv3(resultado, true);
                Log.d(TAG, "Fin");
                hayHilo = false;
                setJlb2("Ver", true);
                setJlb3(true);
            }
        }).start();
    }

    public void actualizarAlerta(final String simbolos) {
        // Android tiene dos reglas que se debe respetar, sino se cumplen lanzan excepción (se traba la aplicación) y manda un mensaje ARN ("Aplicación no responde")
        // 1- Cualquier consulta a la red (internet) deberá ejecutarse en un hilo aparte del principal
        // 2- Nunca deben llamarse objetos de vista dentro de los hilos.
        new Thread(new Runnable() {

            @Override
            public synchronized void run() {// El hilo se finaliza a sí mismo cuando termina su trabajo.
                Log.d(TAG, "inicio");
                // 1. Iniciar
                int i = 0;
                ejecutarAnimacion();
                utilidad.iniciarPeticion(Utilidad.A_UNOxUNO, simbolos, Utilidad.C_LISTA, 3);
                clearDatosLista();
                setJlb2("Cancelar", true);
                Log.d(TAG, "simbolos " + simbolos);
                // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algún error o se cancela por algún motivo.
                while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                    // 2.1 Conseguir valores.
                    String nombre = utilidad.getPeticionParcial();
                    String simbolo = utilidad.getPeticionParcial();
                    String valor2 = utilidad.getPeticionParcial(); // Es el precio
                    try {

                        double minimo = Double.parseDouble(utilidad.getMinimo(getApplicationContext(), simbolo));
                        double precio = Double.parseDouble(valor2);
                        if (precio <= minimo) {
                            i++;
                            String valor = nombre + "\n" + simbolo + "\n" + valor2;
                            Log.d(TAG, "alerta simbolo=" + simbolo + " , precio= " + precio + " <= " + minimo);
                            // 2.1.1 Iniciar hilo UI para actualizarTodo la UI
                            setDatosLista(valor);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                String resultado = i + " resultado" + (i != 1 ? "s" : "");
                setJltv3(resultado, true);
                Log.d(TAG, "Fin");
                hayHilo = false;
                setJlb2("Ver", true);
                setJlb3(true);
            }
        }).start();
    }

    /* Para mostrar la animación de cargando
    * */
    private void ejecutarAnimacion() {
        final String[] animacion = {"   ", ".  ", ".. ", "..."};
        // Android tiene dos reglas que se debe respetar, sino se cumplen lanzan excepción (se traba la aplicación) y manda un mensaje ARN ("Aplicación no responde")
        // 1- Cualquier consulta a la red (internet) deberá ejecutarse en un hilo aparte del principal
        // 2- Nunca deben llamarse objetos de vista dentro de los hilos.
        new Thread(new Runnable() {

            @Override
            public void run() {// El hilo se finaliza a sí mismo cuando termina su trabajo.
                // 1. Iniciar
                Log.d(TAG, "Inicio de la animación");
                int i = 0;
                // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algún error o se cancela por algún motivo.
                while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                    // 2.1 Esperar 500 microsegundos.
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setJltv3("cargando" + animacion[i], false);
                    i = i == (animacion.length - 1) ? 0 : i + 1;
                }
                Log.d(TAG, "Fin de la animación");
            }
        }).start();
    }

    // Métodos para colocar datos a objetos o limpiarlos
    private synchronized void setJltv3(final String valor, boolean bandera) {
        // Si se deberá colocar resultados en la etiqueta o la petición no esta completada, no haya algún error o no se haya cancelado por algún motivo...entonces
        if (bandera || (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada())) {
            runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
                @Override
                public void run() {
                    jltv3.setText(valor);
                }
            });
        }
    }

    private synchronized void setDatosLista(final String valor) {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                datosLista.add(valor);
            }
        });
    }

    private synchronized void clearDatosLista() {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                datosLista.clear();
            }
        });
    }

    private synchronized void setJlb2(final String valor, final boolean habilitado) {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                jlb2.setText(valor);
                jlb2.setEnabled(habilitado);
            }
        });
    }

    private synchronized void setJlb3(final boolean habilitado) {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                jlb3.setEnabled(habilitado);
            }
        });
    }

    // Método para iniciar el servicio
    private void iniciarServicio() {
        if (utilidad.isPrimeraVez(this)) {
            guardarDatos();
            Intent service = new Intent(this, Servicio.class);
            startService(service);
            utilidad.setPrimeraVez(this, false);
        }
    }

    // Método para guardar datos iniciales
    private void guardarDatos() {
        String[] simbolos = {"AAAP", "TATT", "RARE", "SGMO", "YIN", "SLCT", "QLC", "ZYNE"};

        for (int i = 0; i < simbolos.length; i++) {
            utilidad.setAccion(this, simbolos[i], "10");
        }
    }

    // Método que se ejecuta cuando vuelve a esta pantalla
    @Override
    protected void onRestart() {
        super.onRestart();
        if (utilidad.isCambioVariables(this)) {
            ver(null);
            utilidad.setCambioVariables(this, false);
        }
    }

    // Método que se ejecuta cuando se va hacia atras.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        utilidad.setPeticionCancelada(true);
    }
}

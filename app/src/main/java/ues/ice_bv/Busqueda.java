package ues.ice_bv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class Busqueda extends AppCompatActivity {

    private static final String TAG = "LOGb"; //Identificador en el log.
    private Utilidad utilidad = new Utilidad();
    private boolean hayHilo = false;

    private ListView jblv1;
    private ArrayAdapter datosLista;

    private EditText jbet1;

    private TextView jbtv3;
    private Button jbb2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busqueda);
        // 1. Relacionar objetos
        jblv1 = (ListView) findViewById(R.id.jblv1);
        jbet1 = (EditText) findViewById(R.id.jbet1);
        jbtv3 = (TextView) findViewById(R.id.jbtv3);
        jbb2 = (Button) findViewById(R.id.jbb2);
        // 2. Definir Datos y evento
        datosLista = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        jblv1.setAdapter(datosLista);
        jblv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        buscar(null);
        utilidad.setCambioVariables(this, false);
    }

    // Métodos para los eventos en los botones.
    public void buscar(View v) {
        utilidad.setPeticionCancelada(true);
        if (hayHilo) {
            setJbb2("Ver", false);
            return;
        } else {
            hayHilo = true;
            setJbb2("Cancelar", false);
        }

        actualizar(jbet1.getText().toString());
    }

    public void ver(View v) {
        utilidad.setPeticionCancelada(true);
        Intent i = new Intent(getApplicationContext(), Informacion.class);
        i.putExtra("simbolo", jbet1.getText().toString()); // El simoblo está en el segundo renglon
        startActivity(i);
    }

    // Métodos para la actualización de datos
    public void actualizar(final String filtro) {
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
                utilidad.iniciarPeticion(Utilidad.A_LINEAxLINEA, Utilidad.B_TODO, Utilidad.C_LISTA, 3);
                clearDatosLista();
                setJbb2("Cancelar", true);
                Log.d(TAG, "filtro " + filtro);
                // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algún error o se cancela por algún motivo.
                while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                    // 2.1 Conseguir valor.
                    String valor = utilidad.getPeticionParcial();
                    if (valor != null && (filtro.equals("") || valor.startsWith(filtro))) {
                        i++;
                        // 2.1.1 Iniciar hilo UI para actualizarTodo la UI
                        setDatosLista(valor);
                    }
                }
                String resultado = i + " resultado" + (i != 1 ? "s" : "");
                setJbtv3(resultado, true);
                Log.d(TAG, "Fin");
                hayHilo = false;
                setJbb2("Ver", true);
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
                    setJbtv3("cargando" + animacion[i], false);
                    i = i == (animacion.length - 1) ? 0 : i + 1;
                }
                Log.d(TAG, "Fin de la animación");
            }
        }).start();
    }

    // Métodos para colocar datos a objetos o limpiarlos
    private synchronized void setJbtv3(final String valor, boolean bandera) {
        // Si se deberá colocar resultados en la etiqueta o la petición no esta completada, no haya algún error o no se haya cancelado por algún motivo...entonces
        if (bandera || (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada())) {
            runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
                @Override
                public void run() {
                    jbtv3.setText(valor);
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

    private synchronized void setJbb2(final String valor, final boolean habilitado) {
        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.
            @Override
            public void run() {
                jbb2.setText(valor);
                jbb2.setEnabled(habilitado);
            }
        });
    }


    // Método que se ejecuta cuando se va hacia atras.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        utilidad.setPeticionCancelada(true);
    }

}

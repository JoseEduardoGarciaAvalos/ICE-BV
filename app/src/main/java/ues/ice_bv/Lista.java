package ues.ice_bv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class Lista extends AppCompatActivity {

    private static final String TAG = "LOGl"; //Identificador en el log.
    private Utilidad utilidad = new Utilidad();

    private ListView jllv1;
    private ArrayAdapter datosLista;

    private Spinner jll1;
    private ArrayAdapter datosCombo;
    private static final String[] vista = {"Todo", "Alertas"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista);
        // 1. Relacionar objetos
        jllv1 = (ListView) findViewById(R.id.jllv1);
        jll1 = (Spinner) findViewById(R.id.jll1);
        // 2. Definir Datos y evento
        datosLista = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        jllv1.setAdapter(datosLista);
        jllv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(),"Toco el elemento: " + datosLista.getItem(position).toString(), Toast.LENGTH_LONG).show();

                Intent i = new Intent(getApplicationContext(), Informacion.class);
                String[] aux= datosLista.getItem(position).toString().split("\n"); // Extraer el simbolo de la jllv1
                i.putExtra("simbolo", aux[1]); // El simoblo está en el segundo renglon
                utilidad.setPeticionCancelada(true);
                startActivity(i);
            }
        });
        datosCombo = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, vista);
        jll1.setAdapter(datosCombo);
        // 3. Cargar datosLista
        actualizarTodo(utilidad.getAcciones(this));
    }

    public void ver(View v){
        // Si lo que está seleccionado en el combo es igual a vista[0] ...entonces
        if(jll1.getSelectedItem().toString().equals(vista[0])){
            actualizarTodo(utilidad.getAcciones(this));
        } else {
            actualizarAlerta(utilidad.getAcciones(this));
        }
    }

    public void busqueda(View v){
        Intent i = new Intent(getApplicationContext(), Busqueda.class);
        utilidad.setPeticionCancelada(true);
        startActivity(i);
    }


    public void actualizarTodo(final String simbolos) {
        // Android tiene dos reglas que se debe respetar, sino se cumplen lanzan excepción (se traba la aplicación) y manda un mensaje ARN ("Aplicación no responde")
        // 1- Cualquier consulta a la red (internet) deberá ejecutarse en un hilo aparte del principal
        // 2- Nunca deben llamarse objetos de vista dentro de los hilos.
        new Thread(new Runnable() {

            @Override
            public void run() {// El hilo se finaliza a sí mismo cuando termina su trabajo.
                Log.d(TAG, "inicio");
                // 1. Iniciar
                utilidad.iniciarPeticion(Utilidad.A_LINEAxLINEA, simbolos, Utilidad.C_LISTA);
                Log.d(TAG, "simbolos " + simbolos);
                // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algún error o se cancela por algún motivo.
                while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                    // 2.1 Conseguir valor.
                    final String valor = utilidad.getPeticionParcial();  // Cuando se utiliza en un hilo, es necesario colocar un final.
                    if (valor != null) {
                        // 2.1.1 Iniciar hilo UI para actualizarTodo la UI
                        runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.

                            @Override
                            public void run() {
                                datosLista.add(valor);
                                Log.d(TAG, "Actualizado UI");
                            }
                        });
                    }
                }
                Log.d(TAG, "Fin");
            }
        }).start();
    }


    public void actualizarAlerta(final String simbolos) {
        // Android tiene dos reglas que se debe respetar, sino se cumplen lanzan excepción (se traba la aplicación) y manda un mensaje ARN ("Aplicación no responde")
        // 1- Cualquier consulta a la red (internet) deberá ejecutarse en un hilo aparte del principal
        // 2- Nunca deben llamarse objetos de vista dentro de los hilos.
        new Thread(new Runnable() {

            @Override
            public void run() {// El hilo se finaliza a sí mismo cuando termina su trabajo.
                Log.d(TAG, "inicio");
                // 1. Iniciar
                utilidad.iniciarPeticion(Utilidad.A_UNOxUNO, simbolos, Utilidad.C_LISTA);
                Log.d(TAG, "simbolos " + simbolos);
                // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algún error o se cancela por algún motivo.
                while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                    // 2.1 Conseguir valores.
                    String nombre = utilidad.getPeticionParcial() + "\n";
                    String simbolo = utilidad.getPeticionParcial() + "\n";
                    String valor2 = utilidad.getPeticionParcial(); // Es el precio
                    try{
                        double minimo = Double.parseDouble(utilidad.getMinimo(getApplicationContext(),simbolo));
                        double precio = Double.parseDouble(valor2);
                        if( precio <= minimo){
                            final String valor = nombre + simbolo + valor2; // Cuando se utiliza en un hilo, es necesario colocar un final.
                            Log.d(TAG, "alerta simbolo=" + simbolo + " , precio= " + precio + " <= " + minimo);
                            // 2.1.1 Iniciar hilo UI para actualizarTodo la UI
                            runOnUiThread(new Runnable() { // Cuando se necesita modificar una variable de vista en un hilo, solo se hará por este hilo.

                                @Override
                                public void run() {
                                    datosLista.add(valor);
                                    Log.d(TAG, "Actualizado UI");
                                }
                            });
                        }
                    } catch (NumberFormatException e){
                    }
                }
                Log.d(TAG, "Fin");
            }
        }).start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Si lo que está seleccionado en el combo no es igual a vista[0] ...entonces
        if(!jll1.getSelectedItem().toString().equals(vista[0])){
            actualizarAlerta(utilidad.getAcciones(this));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        utilidad.setPeticionCancelada(true);
    }
}

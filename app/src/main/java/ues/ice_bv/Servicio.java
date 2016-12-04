package ues.ice_bv;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


/**
 * Created by jose on 10-24-16.
 */

public class Servicio extends Service {
    private static final String TAG = "LOGs"; //Identificador en el log.
    private boolean bandera = true;
    private Utilidad2 utilidad = new Utilidad2();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    // Evento que se ejecuta al crear el servicio.
    @Override
    public void onCreate() {
        super.onCreate();

        //Toast.makeText(this,"Servicio iniciado", Toast.LENGTH_LONG).show();
        bandera = true;
    }
    // Evento que se ejecuta cuando se instancia el servicio
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Proceso iniciado"); // Guarda un log
                // El servicio se finaliza a sí mismo cuando finaliza su
                // trabajo.
                try {

                    while (bandera) {
                        // Simulamos trabajo de 2 segundos.
                        Thread.sleep(2000);
                        // 0. Conseguir datos previos.
                        boolean notificar = false;
                        String simbolos = utilidad.getAcciones(getApplicationContext());
                        // 1. Iniciar
                        utilidad.iniciarPeticion(Utilidad.A_UNOxUNO, simbolos, Utilidad.C_SERVICIO);
                        Log.d(TAG, "simbolos " + simbolos);
                        // 2. Terminará el trabajo (salir del while) hasta que la petición se complete, haya algun error o se cancela por algún motivo.
                        while (!utilidad.isPeticionCompleta() && !utilidad.isPeticionErronea() && !utilidad.isPeticionCancelada()) {
                            // 2.1 Conseguir valores.
                            String simbolo = utilidad.getPeticionParcial();
                            String valor2 = utilidad.getPeticionParcial();
                            if(simbolo == null || valor2 == null){
                                break;
                            }
                            try{
                                double minimo = Double.parseDouble(utilidad.getMinimo(getApplicationContext(),simbolo));
                                double precio = Double.parseDouble(valor2);
                                if( precio <= minimo){
                                    notificar = true;
                                    utilidad.setPeticionCancelada(true);
                                    Log.d(TAG, "alerta simbolo=" + simbolo + " , precio= " + precio + " <= " + minimo);
                                }
                            } catch (NumberFormatException e){
                            }
                        }
                        Log.d(TAG, "notificacion ");
                        if(notificar){
                            // Instanciamos e inicializamos nuestro manager.
                            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                    getBaseContext())
                                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                                    .setContentTitle("Alerta")
                                    .setContentText("Accion(es) bajo del mínimo.")
                                    .setWhen(System.currentTimeMillis());

                            nManager.notify(123, builder.build());
//                             Tiene el mismo código "123" por lo que si se llama esta función dos veces o más
//                             y no ha borrado la notificación entonces no habrá otra notificación nueva.
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Proceso destruido");  // Guarda un log
            }
        }).start();

        //this.stopSelf(); // Para destruir el servicio
        return super.onStartCommand(intent, flags, startId);
    }

    //Se ejecuta cuando se destruye el servicio.
    @Override
    public void onDestroy() {
        super.onDestroy();

        //Toast.makeText(this,"Servicio destruido", Toast.LENGTH_LONG).show();
        bandera = false;
    }

}

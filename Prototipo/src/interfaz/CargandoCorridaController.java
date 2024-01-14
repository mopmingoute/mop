/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * CargandoCorridaController is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package interfaz;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class CargandoCorridaController extends GeneralidadesController{


    @FXML private JFXButton inputCancelar;

    private Manejador manejador;
    private Task task;

    private Timer timer;

    public CargandoCorridaController(Manejador manejador, Task<Void> cargaTask) {

        this.manejador = manejador;
        this.task = cargaTask;
    }

    @FXML
    public void initialize() {

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Ejecutar la tarea en el hilo de la aplicación JavaFX
                Platform.runLater(() -> {
                    // Realizar la tarea después de 2 minutos
                    if(manejador.datosCorrida.getNombre() == null) {
                        task.cancel();
                        ((Stage) inputCancelar.getScene().getWindow()).close();
                        manejador.clearBlur();
                        mostrarMensajeAvisoSinBlur("Se excedió el tiempo sin cargar la corrida. Revisar errores en la consola.");
                    }

                });
            }
        }, 90 * 1000); // 60 segundos

        inputCancelar.setOnAction(actionEvent -> {
            task.cancel();
            ((Stage) inputCancelar.getScene().getWindow()).close();
            manejador.clearBlur();
        });
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }
}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneralidadesController is part of MOP.
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

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utilitarios.UtilStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/***
 * Jerarquia agregada a los controladores de los participantes y otros controladores
 * para agregar funcionalidades comunes a todos elllos (mensajes de error, etc)
 * Para extenderlo el fxml debe tener el Label con el nombre: messageLabel
 */
public abstract class GeneralidadesController {

    @FXML
    public Label messageLabel;


    private int segundosVisible = 7;

    //Variables para realizar el delay en el control de la edicion de datos
    protected Timer timer = new Timer();
    protected static final int DELAY_MILLISECONDS = 1500; // 1.5 segundos
    protected String lastText = "";

    public enum TipoInfo {
        INFORMACION,
        ERROR,
        FEEDBACK
    }

    /***
     * Tener en cuenta que el largo del texto esta limitado por el largo del TXTField
     * @param mensaje
     * @param tipoInfo
     */
    public  void setLabelMessageTemporal(String mensaje, TipoInfo tipoInfo){

        if(!mensaje.equalsIgnoreCase("")){
            setearColorSegunTipoMensaje(tipoInfo);
           // messageLabel.setWrapText(true);
            messageLabel.setText(mensaje);
            messageLabel.setVisible(true);
            messageLabel.setAlignment(Pos.CENTER_LEFT);
            Timer timer = new Timer();
            TimerTask borrarMsje = new TimerTask() {
                    @Override
                    public void run() {
                        messageLabel.setVisible(false);
                    }
                };
            timer.schedule(borrarMsje, segundosVisible * 1000);

        }

    }

    private void setearColorSegunTipoMensaje(TipoInfo tipoInfo){
        switch (tipoInfo)
        {
            case FEEDBACK:
                messageLabel.setTextFill(javafx.scene.paint.Paint.valueOf("BROWN"));
                break;
            case INFORMACION:
                messageLabel.setTextFill(javafx.scene.paint.Paint.valueOf("BLUE"));
                break;
            case ERROR:
                messageLabel.setTextFill(javafx.scene.paint.Paint.valueOf("RED"));
                break;
        }
    }


    public abstract void confirmoMensaje(boolean confirmacion, int codigoMensaje);

    public void aceptoMensaje(int codigoMensaje){

    };

    /***
     *
     * @param nombreViejo
     * @param nombreNuevo
     * @param ordenParticipantes
     * @param edicion
     * @return  devuelve true si el nombre esta repetido y no se puede usar para el participante que se va a guardar
     */
    public boolean evaluarNombreRepetido(String nombreViejo,String nombreNuevo, ArrayList<String> ordenParticipantes, Boolean edicion) {
        Boolean ret = false;
        int cantOcc = UtilStrings.contarOcurrencias(ordenParticipantes, nombreNuevo);
        if(edicion){
            if((!nombreNuevo.equals(nombreViejo) && cantOcc >0) || (nombreNuevo.equals(nombreViejo) && cantOcc > 1)){
                ret = true;
            }
        }
        else {
            if(cantOcc >0){
                ret = true;
            }
        }
        return ret;
    }

    protected void mostrarMensajeConfirmacion(String mensaje, int codigo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MensajeConfirmacion.fxml"));
            MensajeConfirmacionController mcc = new MensajeConfirmacionController(this, mensaje, codigo, null);

            loader.setController(mcc);
            Parent root1 = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Usado para Avisar un mensaje y realizar una accion al aceptar, definida por el metodo aceptoMensaje
     * @param mensaje
     * @param padre
     * @param codigoMensaje
     */
    protected void mostrarMensajeAviso(String mensaje, Manejador padre, int codigoMensaje){

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MostrarMensaje.fxml"));

            MostrarMensajeController ventanaMsje = new MostrarMensajeController(mensaje, padre, codigoMensaje);
            loader.setController(ventanaMsje);
            Parent root1 = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            BoxBlur bb = new BoxBlur();
            bb.setWidth(3);
            bb.setHeight(3);
            bb.setIterations(3);

            padre.getMainPane().setEffect(bb);
            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    protected void mostrarMensajeAviso(String mensaje, Manejador padre){

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MostrarMensaje.fxml"));

            MostrarMensajeController ventanaMsje = new MostrarMensajeController(mensaje, padre, 0);
            loader.setController(ventanaMsje);
            Parent root1 = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            BoxBlur bb = new BoxBlur();
            bb.setWidth(3);
            bb.setHeight(3);
            bb.setIterations(3);

            padre.getMainPane().setEffect(bb);
            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void mostrarMensajeAvisoSinBlur(String mensaje){

        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MostrarMensaje.fxml"));

            MostrarMensajeController ventanaMsje = new MostrarMensajeController(mensaje);
            loader.setController(ventanaMsje);
            Parent root1 = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);

            Scene firstScene = new Scene(root1, 600, 200);
            firstScene.getRoot().requestFocus();
            stage.setScene(firstScene);
            stage.show();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected boolean controlDisponibilidadMedia(JFXTextField inputDispMediaTGEV) {
        boolean ret = true;
        if( !UtilStrings.esNumeroDouble(inputDispMediaTGEV.getText())) {
            setLabelMessageTemporal("La disponibilidad media debe ser dato numérico", TipoInfo.FEEDBACK);
            ret = false;
        }else{
            double dm = Double.valueOf(inputDispMediaTGEV.getText());
            if(dm > 1 || dm < 0){
                setLabelMessageTemporal("La disponibilidad media debe ser > 0 y < 1.", TipoInfo.FEEDBACK);
                ret = false;
            }
        }
        return ret;
    }

}

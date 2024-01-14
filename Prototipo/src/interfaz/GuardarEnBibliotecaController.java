/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GuardarEnBibliotecaController is part of MOP.
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
import com.jfoenix.controls.JFXComboBox;
import database.MongoDBHandler;
import datatypes.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class GuardarEnBibliotecaController extends GeneralidadesController{

    @FXML private JFXButton btnGuardar;
    @FXML private JFXComboBox<String> comboBiblioteca;
    String tipo;
    Object dataObject;
    DatosCorrida datosCorrida;
    Object controller;
    String nombre;

    public GuardarEnBibliotecaController(String tipo, Object dataObject, DatosCorrida datosCorrida, Object controller, String nombre){
        this.tipo = tipo;
        this.dataObject = dataObject;
        this.datosCorrida = datosCorrida;
        this.controller = controller;
        this.nombre = nombre;

    }

    @FXML
    public void initialize(){
        comboBiblioteca.getItems().addAll(MongoDBHandler.getInstance().getBibliotecas(Manejador.user));
        comboBiblioteca.getSelectionModel().selectFirst();
        btnGuardar.setOnAction(actionEvent -> {

            if(MongoDBHandler.getInstance().existeParticipante(nombre, tipo, comboBiblioteca.getValue())){
                pedirConfirmacionUpdate();
            }else {
                guardarEnBiblioteca(false);
            }

        });
    }

    private void guardarEnBiblioteca(boolean edita) {

        try {
            switch (tipo){

                case Text.TIPO_EOLICO_TEXT:
                    ((EditorEolicoController)controller).loadData();
                    MongoDBHandler.getInstance().setEolico((DatosEolicoCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_SOLAR_TEXT:
                    ((EditorSolarController)controller).loadData();
                    MongoDBHandler.getInstance().setFotovoltaico((DatosFotovoltaicoCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_TERMICO_TEXT:
                    ((EditorTermicoController)controller).loadData();
                    MongoDBHandler.getInstance().setTermico((DatosTermicoCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_HIDRAULICO_TEXT:
                    ((EditorHidraulicoController)controller).loadData();
                    MongoDBHandler.getInstance().setHidraulico((DatosHidraulicoCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_ACUMULADOR_TEXT:
                    ((EditorCentralAcumulacionController)controller).loadData();
                    MongoDBHandler.getInstance().setAcumulador((DatosAcumuladorCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_IMPOEXPO_TEXT:
                    ((EditorImpoExpoController)controller).loadData();
                    MongoDBHandler.getInstance().setImpoExpo((DatosImpoExpoCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_DEMANDA_TEXT:
                    ((EditorDemandaController)controller).loadData();
                    MongoDBHandler.getInstance().setDemanda((DatosDemandaCorrida) dataObject, comboBiblioteca.getValue(), edita);
                    break;
                case Text.TIPO_FALLA_TEXT:
                    ((EditorFallaController)controller).loadData();
                    MongoDBHandler.getInstance().setFalla((DatosFallaEscalonadaCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_COMBUSTIBLE_TEXT:
                    ((EditorCombustibleController)controller).loadData();
                    MongoDBHandler.getInstance().setCombustible((DatosCombustibleCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_IMPACTO_TEXT:
                    ((EditorImpactoController)controller).loadData();
                    MongoDBHandler.getInstance().setImpactoAmbiental((DatosImpactoCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
                case Text.TIPO_CONTRATO_ENERGIA_TEXT:
                    ((EditorContratoController)controller).loadData();
                    MongoDBHandler.getInstance().setContratoEnergia((DatosContratoEnergiaCorrida) dataObject, comboBiblioteca.getValue(), Manejador.getLineaTiempo(datosCorrida), edita);
                    break;
            }
            if(edita){
                ((GeneralidadesController)this.controller).setLabelMessageTemporal(Text.MSG_CONF_ACTUALIZAR_PARTICIPANTE_BIBLIOTECA, GeneralidadesController.TipoInfo.FEEDBACK);
            }else {
                ((GeneralidadesController)this.controller).setLabelMessageTemporal(Text.MSG_CONF_GUARDAR_PARTICIPANTE_BIBLIOTECA, GeneralidadesController.TipoInfo.ERROR);
            }

        } catch (Exception e){
            ((GeneralidadesController)this.controller).setLabelMessageTemporal(Text.MSG_ERR_AGREGAR_PARTICIIPANTE_BIBLIOTECA, GeneralidadesController.TipoInfo.ERROR);
        }


    }

    private void pedirConfirmacionUpdate() {


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MensajeConfirmacion.fxml"));
            MensajeConfirmacionController mcc = new MensajeConfirmacionController(this, Text.MSG_ASK_ACTUALIZAR_PARTICIPANTE_BIBLIOTECA, 1, null);

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

    public void confirmoMensaje(boolean confirmacion, int codigo){
        if(confirmacion && codigo == 1){
            guardarEnBiblioteca(true);
        }

    };


}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorCanioSimpleController is part of MOP.
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
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import datatypes.DatosContratoCombustibleCorrida;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.HashMap;

public class EditorCanioSimpleController {
    @FXML private JFXTextField inputNombre;
    @FXML private JFXComboBox<String> inputBarra;
    @FXML private JFXComboBox<String> inputCombustible;
    @FXML private JFXTextField inputCantModInst;
    @FXML private JFXButton inputCantModInstEV;
    @FXML private JFXComboBox<String> inputDispModUnidad;
    @FXML private JFXTextField inputCantModIni;
    @FXML private JFXTextField inputDispMedia;
    @FXML private JFXButton inputDispMediaEV;
    @FXML private JFXTextField inputTMedioArreglo;
    @FXML private JFXButton inputTMedioArregloEV;
    @FXML private JFXComboBox<String> inputTMedioArregloUnidad;
    @FXML private JFXTextField inputMantProg;
    @FXML private JFXButton inputMantProgEV;
    @FXML private JFXTextField inputCostoFijo;
    @FXML private JFXButton inputCostoFijoEV;
    @FXML private JFXTextField inputCaudalMaximo;
    @FXML private JFXButton inputCaudalMaximoEV;
    @FXML private JFXComboBox<String> inputCaudalMaximoUnidad;
    @FXML private JFXTextField inputPrecioCombustible;
    @FXML private JFXButton inputPrecioCombustibleEV;
    @FXML private JFXComboBox<String> inputPrecioCombustibleUnidad;
    @FXML private JFXCheckBox inputSalidaDetallada;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private ArrayList<String> listaBarras = new ArrayList<>();

    private DatosContratoCombustibleCorrida datosContratoCombustibleCorrida;
    private boolean edicion;
    private EditorCombustibleController parentController;

    private boolean editoVariables = false;

    public EditorCanioSimpleController(EditorCombustibleController editorCombustibleController){
        this.parentController = editorCombustibleController;
    }

    public EditorCanioSimpleController(EditorCombustibleController editorCombustibleController, DatosContratoCombustibleCorrida datosContratoCombustibleCorrida){
        this.datosContratoCombustibleCorrida = datosContratoCombustibleCorrida;
        edicion = true;
        this.parentController = editorCombustibleController;
    }

    public boolean isEditoVariables() { return editoVariables;  }

    public void setEditoVariables(boolean editoVariables) {  this.editoVariables = editoVariables;   }

    public void initialize(){

        listaBarras.addAll(parentController.getListaBarras());

        inputBarra.getItems().addAll(listaBarras);

        inputTMedioArregloUnidad.getItems().add(Text.UNIDAD_DIAS);
        inputTMedioArregloUnidad.getSelectionModel().selectFirst();

        inputDispModUnidad.getItems().add(Text.TIPO_EXPONENCIAL);
        inputDispModUnidad.getSelectionModel().selectFirst();

        inputCaudalMaximoUnidad.getItems().add(Text.UNIDAD_M3_H);
        inputCaudalMaximoUnidad.getSelectionModel().selectFirst();

        inputPrecioCombustibleUnidad.getItems().add(Text.UNIDAD_USD_M3);
        inputPrecioCombustibleUnidad.getSelectionModel().selectFirst();

        if(edicion){
            unloadData();
        }else{
            evForField(null, "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);
            evForField(null, "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);
            evForField(null, "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);
            evForField(null, "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);
            evForField(null, "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);
            evForField(null, "caudalMax", inputCaudalMaximoEV, Text.EV_NUM_DOUBLE, null, inputCaudalMaximo);
            evForField(null, "precioComb", inputPrecioCombustibleEV, Text.EV_NUM_DOUBLE, null, inputPrecioCombustible);
        }

        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputBarra.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModInst.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDispModUnidad.valueProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCantModIni.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputDispMedia.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputTMedioArreglo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputMantProg.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCostoFijo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputPrecioCombustible.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputCaudalMaximo.textProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;      });
        controlHayCambiosEnEvoluciones();

        editoVariables = false;
    }

    private void controlHayCambiosEnEvoluciones() {
        for (String clave : evsPorNombre.keySet()) {
            if(evsPorNombre.get(clave).getEv() != null && evsPorNombre.get(clave).getEv().isEditoValores()){
                editoVariables = true;
            }
        }

    }

    public void updateBarras(){
        ArrayList<String> elemsToAdd = new ArrayList<>(parentController.getListaBarras());
        elemsToAdd.removeAll(listaBarras);

        ArrayList<String> elemsToRemove = new ArrayList<>(listaBarras);
        elemsToRemove.removeAll(parentController.getListaBarras());

        listaBarras = parentController.getListaBarras();

        inputBarra.getItems().addAll(elemsToAdd);

        if(elemsToRemove.contains(inputBarra.getValue())){
            inputBarra.getSelectionModel().clearSelection();
        }
        inputBarra.getItems().removeAll(elemsToRemove);
    }

    public DatosContratoCombustibleCorrida getDatosContratoCombustibleCorrida(){
        loadData();
        return datosContratoCombustibleCorrida;
    }

    private void evForField(Evolucion ev, String key, JFXButton evBtn, String tipoData, ArrayList<String> vars, Node componenteAsociado){
        ArrayList<Evolucion> listaConUnaEv = new ArrayList<>();
        if(ev != null){
            listaConUnaEv.add(ev);
            EVController evController = new EVController(listaConUnaEv, parentController.datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);

        }else{
            EVController evController = new EVController(parentController.datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);
        }
    }


    /**
     *  Método para cargar los datos en el DatosContratoCombustibleCorrida
     */
    private void loadData(){
        System.out.println("LOAD DATA-CANIO SIMPLE");
        String nombre = inputNombre.getText();
        String barra = inputBarra.getValue();

        String combustible = inputCombustible.getValue();
        Evolucion<Integer> cantModInst = EVController.loadEVsegunTipo(inputCantModInst,Text.EV_NUM_INT, evsPorNombre, "cantModInst");
        Integer cantModIni = 0;
        if(UtilStrings.esNumeroEntero(inputCantModIni.getText())) { cantModIni = Integer.parseInt(inputCantModIni.getText()); }
        Evolucion<Double> dispMedia = EVController.loadEVsegunTipo(inputDispMedia,Text.EV_NUM_DOUBLE, evsPorNombre, "dispMedia");
        Evolucion<Double> tMedioArreglo = EVController.loadEVsegunTipo(inputTMedioArreglo,Text.EV_NUM_DOUBLE, evsPorNombre, "tMedioArreglo");
        Evolucion<Integer> mantProgramado = EVController.loadEVsegunTipo(inputMantProg,Text.EV_NUM_INT, evsPorNombre, "mantProg");
        Evolucion<Double> costoFijo = EVController.loadEVsegunTipo(inputCostoFijo,Text.EV_NUM_DOUBLE, evsPorNombre, "costoFijo");
        Evolucion<Double> caudalMax = EVController.loadEVsegunTipo(inputCaudalMaximo,Text.EV_NUM_DOUBLE, evsPorNombre, "caudalMax");
        Evolucion<Double> precioComb = EVController.loadEVsegunTipo(inputPrecioCombustible,Text.EV_NUM_DOUBLE, evsPorNombre, "precioComb");
        boolean salidaDetallada = inputSalidaDetallada.isSelected();
        if(edicion){
            datosContratoCombustibleCorrida.setNombre(nombre);
            datosContratoCombustibleCorrida.setBarra(barra);
            datosContratoCombustibleCorrida.setComb(combustible);
            datosContratoCombustibleCorrida.setCantModInst(cantModInst);
            datosContratoCombustibleCorrida.setCantModIni(cantModIni);
            datosContratoCombustibleCorrida.setDispMedia(dispMedia);
            datosContratoCombustibleCorrida.settMedioArreglo(tMedioArreglo);
            datosContratoCombustibleCorrida.setMantProgramado(mantProgramado);
            datosContratoCombustibleCorrida.setCostoFijo(costoFijo);
            datosContratoCombustibleCorrida.setCaudalMax(caudalMax);
            datosContratoCombustibleCorrida.setPrecioComb(precioComb);
            datosContratoCombustibleCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosContratoCombustibleCorrida = new DatosContratoCombustibleCorrida(nombre, barra, combustible, cantModInst, cantModIni,
                                                                                  dispMedia, tMedioArreglo, caudalMax, precioComb,
                                                                                  salidaDetallada, mantProgramado, costoFijo);
        }
    }

    /**
     * Método para obtener los datos del DatosContratoCombustibleCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-CANIO SIMPLE");
        inputNombre.setText(datosContratoCombustibleCorrida.getNombre());
        inputBarra.getSelectionModel().select(datosContratoCombustibleCorrida.getBarra());
        inputCombustible.getSelectionModel().select(datosContratoCombustibleCorrida.getComb());
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        if(datosContratoCombustibleCorrida.getCantModInst() != null) { inputCantModInst.setText(datosContratoCombustibleCorrida.getCantModInst().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.getCantModInst(), "cantModInst", inputCantModInstEV, Text.EV_NUM_INT, null, inputCantModInst);

        inputCantModIni.setText(datosContratoCombustibleCorrida.getCantModIni().toString());

        if(datosContratoCombustibleCorrida.getDispMedia() != null) { inputDispMedia.setText(datosContratoCombustibleCorrida.getDispMedia().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.getDispMedia(), "dispMedia", inputDispMediaEV, Text.EV_NUM_DOUBLE, null, inputDispMedia);

        if(datosContratoCombustibleCorrida.gettMedioArreglo() != null) { inputTMedioArreglo.setText(datosContratoCombustibleCorrida.gettMedioArreglo().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.gettMedioArreglo(), "tMedioArreglo", inputTMedioArregloEV, Text.EV_NUM_DOUBLE, null, inputTMedioArreglo);

        if(datosContratoCombustibleCorrida.getMantProgramado() != null) { inputMantProg.setText(datosContratoCombustibleCorrida.getMantProgramado().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.getMantProgramado(), "mantProg", inputMantProgEV, Text.EV_NUM_INT, null, inputMantProg);

        if(datosContratoCombustibleCorrida.getCostoFijo() != null) { inputCostoFijo.setText(datosContratoCombustibleCorrida.getCostoFijo().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.getCostoFijo(), "costoFijo", inputCostoFijoEV, Text.EV_NUM_DOUBLE, null, inputCostoFijo);

        if(datosContratoCombustibleCorrida.getCaudalMax() != null) { inputCaudalMaximo.setText(datosContratoCombustibleCorrida.getCaudalMax().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.getCaudalMax(), "caudalMax", inputCaudalMaximoEV, Text.EV_NUM_DOUBLE, null, inputCaudalMaximo);

        if(datosContratoCombustibleCorrida.getPrecioComb() != null) { inputPrecioCombustible.setText(datosContratoCombustibleCorrida.getPrecioComb().getValor(instanteActual).toString()); }
        evForField(datosContratoCombustibleCorrida.getPrecioComb(), "precioComb", inputPrecioCombustibleEV, Text.EV_NUM_DOUBLE, null, inputPrecioCombustible);

        inputSalidaDetallada.setSelected(datosContratoCombustibleCorrida.isSalDetallada());
    }

    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> errores = new ArrayList<>();

        if(inputNombre.getText().trim().equals("")){ errores.add("EditorCanioSimple sin nombre.");}
        if(inputBarra.getSelectionModel() == null) { errores.add("EditorCanioSimple: " + inputNombre.getText() + " sin barra seleccionada.");}
        if(inputCombustible.getSelectionModel() == null) { errores.add("EditorCanioSimple sin combustible seleccionado.");}

        if(inputCantModInst.getText().trim().equals("")){ errores.add("EditorCanioSimple: " + inputNombre.getText() + "Cantidad de modulos instalados vacía.");}
        if( !UtilStrings.esNumeroEntero(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Cantidad de modulos instalados no es número."); }

        if(inputCantModIni.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Cantidad de modulos inicial vacía.");}
        if(!UtilStrings.esNumeroEntero(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Cantidad de modulos inicial no es número."); }

        if(inputDispMedia.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Disponibilidad media vacía.");}
        if( !UtilStrings.esNumeroDouble(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Disponibilidad media no es decimal."); }

        if(inputTMedioArreglo.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Tiempo medio de arreglo vacío.");}
        if( !UtilStrings.esNumeroDouble(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Tiempo medio de arreglo no es decimal."); }

        if(inputMantProg.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Mantenimiento programado vacío.");}
        if( !UtilStrings.esNumeroEntero(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Mantenimiento programado no es número."); }

        if(inputCostoFijo.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Costo fijo vacío.");}
        if( !UtilStrings.esNumeroDouble(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Costo fijo no es decimal."); }

        if(inputCaudalMaximo.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Caudal máximo vacío. ");}
        if( !UtilStrings.esNumeroDouble(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Caudal máximo no es decimal."); }

        if(inputPrecioCombustible.getText().trim().equals("")){ errores.add("EditorCanioSimple: "+ inputNombre.getText() +" Precio combustible vacío. ");}
        if( !UtilStrings.esNumeroDouble(inputCantModInst.getText().trim() )){ errores.add("EditorCanioSimple:  "+ inputNombre.getText() +" Precio combustible no es decimal."); }


        return errores;
    }
}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BloqueImpoExpoController is part of MOP.
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
import com.jfoenix.controls.JFXTextField;
import datatypes.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.HashMap;

public class BloqueImpoExpoController {

    @FXML private JFXComboBox<String> inputPrecioProcOpt;
    @FXML private JFXComboBox<String> inputPrecioProcSim;
    @FXML private JFXComboBox<String> inputPrecioNombre;

    @FXML private JFXComboBox<String> inputPotenciaProcOpt;
    @FXML private JFXComboBox<String> inputPotenciaProcSim;
    @FXML private JFXComboBox<String> inputPotenciaNombre;

    @FXML private JFXButton inputFPre;
    @FXML private JFXButton inputFPot;
    @FXML private JFXButton inputFDisp;
    private FUNCController poliPreController;
    private FUNCController poliPotController;
    private FUNCController poliDispController;

    @FXML private JFXButton inputPreEvolEV;
    @FXML private JFXButton inputPotEvolEV;
    @FXML private JFXButton inputDispEvolEV;

    @FXML private GridPane gridPane;

    private String tipoImpoExpo;
    private DatosCorrida datosCorrida;
    //private DatosImpoExpoCorrida datosImpoExpoCorrida;
    private boolean edicion;
    private boolean esValPostInterna;
    private int cantPostes;

    //IEEVOL
    private Evolucion<ArrayList<Double>>  preEvol;
    private Evolucion<ArrayList<Double>>  potEvol;
    private Evolucion<ArrayList<Double>>  dispEvol;

    private JFXTextField preEvolElements;
    private JFXTextField potEvolElements;
    private JFXTextField dispEvolElements;

    //IEALEATPRPOT
    private DatosVariableAleatoria datPrecio;
    private DatosVariableAleatoria datPotencia;

    //IEALEATFORMUL
    private Evolucion<DatosPolinomio> poliPot;
    private Evolucion<DatosPolinomio> poliPre;
    private Evolucion<DatosPolinomio> poliDisp;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    private boolean editoVariables;




    public BloqueImpoExpoController(DatosCorrida datosCorrida, String tipoImpoExpo){
        this.tipoImpoExpo = tipoImpoExpo;
        edicion = false;
        this.esValPostInterna = datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_INTERNA);
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
        this.datosCorrida = datosCorrida;
    }

    //IEEVOL
    public BloqueImpoExpoController(DatosCorrida datosCorrida, Evolucion<ArrayList<Double>> potEvol, Evolucion<ArrayList<Double>> preEvol, Evolucion<ArrayList<Double>> dispEvol){
        this.potEvol = potEvol;
        this.preEvol = preEvol;
        this.dispEvol = dispEvol;
        tipoImpoExpo = Constantes.IEEVOL;
        edicion = true;
        this.esValPostInterna = datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_INTERNA);
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
        this.datosCorrida = datosCorrida;
    }

    //IEALEATPRPOT
    public BloqueImpoExpoController(DatosCorrida datosCorrida, DatosVariableAleatoria datPrecio, DatosVariableAleatoria datPotencia){
        this.datPrecio = datPrecio;
        this.datPotencia = datPotencia;
        tipoImpoExpo = Constantes.IEALEATPRPOT;
        edicion = true;
        this.esValPostInterna = datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_INTERNA);
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
        this.datosCorrida = datosCorrida;
    }

    //IEALEATFORMUL //el booleano aux se usa para evitar problemas de type erasure, se ignora a propósito
    public BloqueImpoExpoController(DatosCorrida datosCorrida, Evolucion<DatosPolinomio> poliPot, Evolucion<DatosPolinomio> poliPre, Evolucion<DatosPolinomio> poliDisp, boolean aux){
        this.poliPre = poliPre;
        this.poliPot = poliPot;
        this.poliDisp = poliDisp;
        tipoImpoExpo = Constantes.IEALEATFORMUL;
        edicion = true;
        this.esValPostInterna = datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_INTERNA);
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
        this.datosCorrida = datosCorrida;
    }
    public BloqueImpoExpoController (DatosCorrida datosCorrida, String tipoImpoExpo,
                                     Evolucion<ArrayList<Double>> potEvol, Evolucion<ArrayList<Double>> preEvol, Evolucion<ArrayList<Double>> dispEvol,
                                     DatosVariableAleatoria datPrecio, DatosVariableAleatoria datPotencia,
                                     Evolucion<DatosPolinomio> poliPre, Evolucion<DatosPolinomio> poliPot, Evolucion<DatosPolinomio> poliDisp){

        this.tipoImpoExpo = tipoImpoExpo;
        edicion = true;
        this.esValPostInterna = datosCorrida.getTipoValpostizacion().equalsIgnoreCase(Text.VALPOSTIZACION_INTERNA);
        this.cantPostes = datosCorrida.getLineaTiempo().getDurPostesPorBloque().get(0).size();
        this.datosCorrida = datosCorrida;

        this.potEvol = potEvol;
        this.preEvol = preEvol;
        this.dispEvol = dispEvol;

        this.datPrecio = datPrecio;
        this.datPotencia = datPotencia;

        this.poliPre = poliPre;
        this.poliPot = poliPot;
        this.poliDisp = poliDisp;

    }

    public boolean isEditoVariables() {

        if(tipoImpoExpo.equals(Constantes.IEEVOL)) {
            if(preEvol != null && preEvol.isEditoValores()){  editoVariables = true; }
            if(potEvol != null && potEvol.isEditoValores()){  editoVariables = true; }
            if(dispEvol != null && dispEvol.isEditoValores()){  editoVariables = true; }
        } else if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {
            if( poliDispController.getDatosPolinomioSinLoadData() != null && poliDispController.getDatosPolinomioSinLoadData().isEditoValores()) {  editoVariables = true; }
            if(poliPreController.getDatosPolinomioSinLoadData() != null && poliPreController.getDatosPolinomioSinLoadData().isEditoValores()) {  editoVariables = true; }
            if(poliPotController.getDatosPolinomioSinLoadData() != null && poliPotController.getDatosPolinomioSinLoadData().isEditoValores()) {  editoVariables = true; }

        }

        return editoVariables;
    }
    public void setEditoVariables(boolean editoVariables) {     this.editoVariables = editoVariables;  }

    public void initialize(){

        Manejador.popularListaNombresVA(datosCorrida, inputPrecioProcSim, inputPrecioProcOpt, inputPrecioNombre);
        Manejador.popularListaNombresVA(datosCorrida, inputPotenciaProcSim, inputPotenciaProcOpt, inputPotenciaNombre);

        renderInputs();
        disableInputs(tipoImpoExpo);
        if(edicion){
            unloadData();
        }else{
            poliPreController = new FUNCController("Precio");
            poliPotController = new FUNCController("Potencia");
            poliDispController = new FUNCController("Disponibilidad");
            FUNCController.setFuncBtnAction(inputFPre, poliPreController);
            FUNCController.setFuncBtnAction(inputFPot, poliPotController);
            FUNCController.setFuncBtnAction(inputFDisp, poliDispController);
            evForField(null, "preEvolEV", inputPreEvolEV, Text.EV_LISTA_NUM, null, preEvolElements);
            evForField(null, "potEvolEV", inputPotEvolEV, Text.EV_LISTA_NUM, null, potEvolElements);
            evForField(null, "dispEvolEV", inputDispEvolEV, Text.EV_LISTA_NUM, null, dispEvolElements);

        }

        inputPotenciaProcOpt.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputPotenciaProcSim.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputPotenciaNombre.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });

        inputPrecioProcSim.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputPrecioProcOpt.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });
        inputPrecioNombre.valueProperty().addListener((observable, oldValue, newValue) -> {   editoVariables = true;    });

        editoVariables = false;
    }

    public void disableInputs(String tipo){
        this.tipoImpoExpo = tipo;
        preEvolElements.setDisable(true);
        potEvolElements.setDisable(true);
        dispEvolElements.setDisable(true);
        if(tipo.equals(Constantes.IEEVOL)){
            inputPrecioProcOpt.setDisable(true);
            inputPrecioProcSim.setDisable(true);
            inputPrecioNombre.setDisable(true);
            inputPotenciaProcOpt.setDisable(true);
            inputPotenciaProcSim.setDisable(true);
            inputPotenciaNombre.setDisable(true);
            inputFPre.setDisable(true);
            inputFPot.setDisable(true);
            inputFDisp.setDisable(true);

            inputPreEvolEV.setDisable(false);
            inputPotEvolEV.setDisable(false);
            inputDispEvolEV.setDisable(false);
        }else if(tipo.equals(Constantes.IEALEATPRPOT)){
            inputFPre.setDisable(true);
            inputFPot.setDisable(true);
            inputFDisp.setDisable(true);

            inputPreEvolEV.setDisable(true);
            inputPotEvolEV.setDisable(true);
            inputDispEvolEV.setDisable(true);
            inputPrecioProcOpt.setDisable(false);
            inputPrecioProcSim.setDisable(false);
            inputPrecioNombre.setDisable(false);
            inputPotenciaProcOpt.setDisable(false);
            inputPotenciaProcSim.setDisable(false);
            inputPotenciaNombre.setDisable(false);
        }else{
            inputPrecioProcOpt.setDisable(true);
            inputPrecioProcSim.setDisable(true);
            inputPrecioNombre.setDisable(true);
            inputPotenciaProcOpt.setDisable(true);
            inputPotenciaProcSim.setDisable(true);
            inputPotenciaNombre.setDisable(true);

            inputPreEvolEV.setDisable(true);
            inputPotEvolEV.setDisable(true);
            inputDispEvolEV.setDisable(true);
            inputFPre.setDisable(false);
            inputFPot.setDisable(false);
            inputFDisp.setDisable(false);

        }
    }


    private void renderInputs(){
        preEvolElements = new JFXTextField();
        potEvolElements = new JFXTextField();
        dispEvolElements = new JFXTextField();
    }

    // IEALEATPRPOT
    public DatosVariableAleatoria getDatPrecio(){
        return new DatosVariableAleatoria(inputPrecioProcOpt.getValue(), inputPrecioProcSim.getValue(), inputPrecioNombre.getValue());
    }
    public DatosVariableAleatoria getDatPotencia(){
        return new DatosVariableAleatoria(inputPotenciaProcOpt.getValue(), inputPotenciaProcSim.getValue(), inputPotenciaNombre.getValue());
    }

    // IEALEATFORMUL
    public Evolucion<DatosPolinomio> getPoliPot(){
        return new EvolucionConstante<>(poliPotController.getDatosPolinomio(), new SentidoTiempo(1));
    }
    public Evolucion<DatosPolinomio> getPoliPre(){
        return new EvolucionConstante<>(poliPreController.getDatosPolinomio(), new SentidoTiempo(1));
    }
    public Evolucion<DatosPolinomio> getPoliDisp(){
        return new EvolucionConstante<>(poliDispController.getDatosPolinomio(), new SentidoTiempo(1));
    }

    private void evForField(Evolucion ev, String key, JFXButton evBtn, String tipoData, ArrayList<String> vars, Node componenteAsociado){
        ArrayList<Evolucion> listaConUnaEv = new ArrayList<>();
        if(ev != null){
            listaConUnaEv.add(ev);
            EVController evController = new EVController(listaConUnaEv, datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);

        }else{
            EVController evController = new EVController(datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));
            evController.setEVBtnAction(evBtn, evController);
            evsPorNombre.put(key, evController);
        }
    }

    /**
     *  Método para cargar los datos en el DatosImpoExpoCorrida
     */
    public void loadData(String tipoImpoExpo, DatosImpoExpoCorrida datosImpoExpoCorrida){
        if(tipoImpoExpo != null && tipoImpoExpo.equals(Constantes.IEALEATPRPOT)) {
            if(inputPrecioProcOpt.getValue() != null && inputPrecioProcSim.getValue()  != null && inputPrecioNombre.getValue()  != null ) {
             datosImpoExpoCorrida.getDatPrecio().add(getDatPrecio());}
        if(inputPotenciaProcOpt.getValue() != null && inputPotenciaProcSim.getValue()!= null && inputPotenciaNombre.getValue() != null ){
            datosImpoExpoCorrida.getDatPotencia().add(getDatPotencia());}

        }else if(tipoImpoExpo != null && tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {
            if(poliPotController.getDatosPolinomioSinLoadData() != null){
                datosImpoExpoCorrida.getPoliPot().add(getPoliPot());}
            if(poliPreController.getDatosPolinomioSinLoadData() != null ){
                datosImpoExpoCorrida.getPoliPre().add(getPoliPre());}
            if(poliDispController.getDatosPolinomioSinLoadData() != null ){
                datosImpoExpoCorrida.getPoliDisp().add(getPoliDisp());}


        }else{

            datosImpoExpoCorrida.getPotEvol().add(evsPorNombre.get("potEvolEV").loadData());
            datosImpoExpoCorrida.getPreEvol().add(evsPorNombre.get("preEvolEV").loadData());
            datosImpoExpoCorrida.getDispEvol().add(evsPorNombre.get("dispEvolEV").loadData());

        }

    }

    /**
     * Método para obtener los datos del DatosImpoExpoCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-BLOQUE IMPO/EXPO");
        long instanteActual = CorridaHandler.getInstance().dameInstanteActual();
        evForField(preEvol, "preEvolEV", inputPreEvolEV, Text.EV_LISTA_NUM, null, preEvolElements);
        evForField(potEvol, "potEvolEV", inputPotEvolEV, Text.EV_LISTA_NUM, null, potEvolElements);
        evForField(dispEvol, "dispEvolEV", inputDispEvolEV, Text.EV_LISTA_NUM, null, dispEvolElements);

        if( tipoImpoExpo.equals(Constantes.IEALEATFORMUL) ) {
            if(poliPre != null){poliPreController = new FUNCController(poliPre.getValor(instanteActual), "Precio");}
            else {poliPreController = new FUNCController("Precio");}

            if(poliPot != null){poliPotController = new FUNCController(poliPot.getValor(instanteActual), "Potencia");}
            else {poliPotController = new FUNCController("Potencia");}

            if(poliDisp != null){poliDispController = new FUNCController(poliDisp.getValor(instanteActual), "Disponibilidad");}
            else {poliDispController = new FUNCController("Disponibilidad");}

            FUNCController.setFuncBtnAction(inputFPre, poliPreController);
            FUNCController.setFuncBtnAction(inputFPot, poliPotController);
            FUNCController.setFuncBtnAction(inputFDisp, poliDispController);
        } else {
            poliPreController = new FUNCController("Precio");
            poliPotController = new FUNCController("Potencia");
            poliDispController = new FUNCController("Disponibilidad");
            FUNCController.setFuncBtnAction(inputFPre, poliPreController);
            FUNCController.setFuncBtnAction(inputFPot, poliPotController);
            FUNCController.setFuncBtnAction(inputFDisp, poliDispController);
            poliPreController.setDatosPolinomio(new DatosPolinomio());
            poliPotController.setDatosPolinomio(new DatosPolinomio());
            poliDispController.setDatosPolinomio(new DatosPolinomio());

        }

         if(tipoImpoExpo.equals(Constantes.IEALEATPRPOT)) {
            inputPrecioProcOpt.getSelectionModel().select(datPrecio.getProcOptimizacion());
            inputPrecioProcSim.getSelectionModel().select(datPrecio.getProcSimulacion());
            inputPrecioNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datPrecio.getProcOptimizacion()),
                                                                                     datosCorrida.getProcesosEstocasticos().get(datPrecio.getProcSimulacion())));
            inputPrecioNombre.getSelectionModel().select(datPrecio.getNombre());
            inputPotenciaProcOpt.getSelectionModel().select(datPotencia.getProcOptimizacion());
            inputPotenciaProcSim.getSelectionModel().select(datPotencia.getProcSimulacion());
            inputPotenciaNombre.getItems().addAll(Manejador.getListaNombresVA(datosCorrida.getProcesosEstocasticos().get(datPotencia.getProcOptimizacion()),
                                                                                       datosCorrida.getProcesosEstocasticos().get(datPotencia.getProcSimulacion())));
            inputPotenciaNombre.getSelectionModel().select(datPotencia.getNombre());

        }
    }

    public ArrayList<String> controlDatosCompletos(){
        ArrayList<String> errores = new ArrayList<>();
        if(tipoImpoExpo.equals(Constantes.IEALEATPRPOT)) {

            if(inputPrecioProcOpt.getSelectionModel() == null){ errores.add("Bloque ImpoExpo: Precio procSimulacion vacío."); }
            if(inputPrecioProcSim.getSelectionModel() == null){ errores.add("Bloque ImpoExpo: Precio procOptimizacion vacío."); }
            if(inputPrecioNombre.getSelectionModel() == null){ errores.add("Bloque ImpoExpo: Precio Nombre vacío."); }

            if(inputPotenciaProcOpt.getSelectionModel() == null){ errores.add("Bloque ImpoExpo: Potencia procOptimizacion vacío."); }
            if(inputPotenciaProcSim.getSelectionModel() == null){ errores.add("Bloque ImpoExpo: Potencia procSimulacion vacío."); }
            if(inputPotenciaNombre.getSelectionModel() == null){ errores.add("Bloque ImpoExpo: Precio Nombre vacío."); }

        }
        if(tipoImpoExpo.equals(Constantes.IEALEATFORMUL)) {

            if(!poliPreController.polinomioCompleto(poliPotController.getDatosPolinomioSinLoadData())){ errores.add("Bloque ImpoExpo: fPre vacía."); }
            if(!poliPotController.polinomioCompleto(poliPotController.getDatosPolinomioSinLoadData())){ errores.add("Bloque ImpoExpo: fPot vacía."); }
            if(!poliDispController.polinomioCompleto(poliDispController.getDatosPolinomioSinLoadData())){ errores.add("Bloque ImpoExpo: fDisp vacía."); }

        }
        if(tipoImpoExpo.equals(Constantes.IEEVOL)) {

            if(evsPorNombre.get("potEvolEV").controlDatosCompletos().size() >0 ){ errores.add("Bloque ImpoExpo: potEvolEV vacía."); }
            if(evsPorNombre.get("preEvolEV").controlDatosCompletos().size() >0 ){ errores.add("Bloque ImpoExpo: preEvolEV vacía."); }
            if(evsPorNombre.get("dispEvolEV").controlDatosCompletos().size() >0 ){ errores.add("Bloque ImpoExpo: dispEvolEV vacía."); }

        }

        return errores;
    }

}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaHidraulicosController is part of MOP.
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
import datatypes.*;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.util.*;


public class ListaHidraulicosController extends ListaParticipante{
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> inputCompLago;
    @FXML private JFXComboBox<String> inputCompCoefEnergetico;
    @FXML private JFXButton inputCompLagoEV;
    @FXML private JFXButton inputCompCoefEnergeticoEV;

    private DatosHidraulicosCorrida datosHidraulicosCorrida;
    private Hashtable<String, DatosHidraulicoCorrida> listaDatosHidraulicoCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();
    private ListaHidraulicosController thisController;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();
    private String hidraulicoABorrar;
    private int indHidraulicoABorrar;

    public ListaHidraulicosController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida, parentController);
        this.datosHidraulicosCorrida = datosCorrida.getHidraulicos();
        this.listaDatosHidraulicoCorrida = datosCorrida.getHidraulicos().getHidraulicos();
        thisController = this;
    }


    @FXML
    public void initialize(){

        inputCompLago.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.HIDROCONLAGO, Constantes.HIDROSINLAGO, Constantes.HIDROSINLAGOENOPTIM)));
        inputCompCoefEnergetico.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.HIDROCOEFENERGCONSTANTES, Constantes.HIDROPOTENCIACAUDAL)));

        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);
        unloadData();
    }



    /**
     *  Método para cargar los datos en el DatosHidraulicosCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA HIDRÁULICOS");
        Hashtable<String, Evolucion<String>> valoresComportamiento = new Hashtable<>();
        Evolucion<String> compLago = new EvolucionConstante<>(inputCompLago.getValue(), new SentidoTiempo(1));
        Evolucion<String> compCoefEnergetico = new EvolucionConstante<>(inputCompCoefEnergetico.getValue(), new SentidoTiempo(1));
        valoresComportamiento.put(Constantes.COMPLAGO, compLago);
        valoresComportamiento.put(Constantes.COMPCOEFENERGETICO, compCoefEnergetico);
        datosHidraulicosCorrida.setValoresComportamiento(valoresComportamiento);
        datosHidraulicosCorrida.setHidraulicos(listaDatosHidraulicoCorrida);
        getDatosCorrida().setHidraulicos(datosHidraulicosCorrida);
    }

    /**
     * Método para obtener los datos del DatosHidraulicosCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA HIDRÁULICOS");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosHidraulicoCorrida datosHidraulicoCorrida : listaDatosHidraulicoCorrida.values()){
            Label nombre = new Label(datosHidraulicoCorrida.getNombre());
            if(datosHidraulicoCorrida.controlDatosCompletos().size() > 0){
                nombre.setText(datosHidraulicoCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                getParentController().setVisibleParticipate(datosHidraulicoCorrida.getNombre()+"(H)", visible.isSelected());
                if(!visible.isSelected()){
                    todosVisiblesInput.setSelected(false);
                }else{
                    boolean allVisible = true;
                    for(JFXCheckBox checkBox : checkBoxes){
                        allVisible &= checkBox.isSelected();
                    }
                    if(!todosVisiblesInput.isSelected()){
                        todosVisiblesInput.setSelected(allVisible);
                    }
                }
            });
            checkBoxes.add(visible);
            GridPane.setMargin(visible, new Insets(0,0, 0,16));
            MaterialIconView editIcon = new MaterialIconView(MaterialIcon.EDIT);
            editIcon.getStyleClass().add("glyph-icon-edit");
            JFXButton editButton = new JFXButton("", editIcon);
            editButton.getStyleClass().add("btn_edit_part");
            editButton.setOnMouseEntered(event -> nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;"));
            editButton.setOnMouseExited(event -> nombre.setStyle("-fx-font-weight: normal; -fx-font-size: 15px;"));
            MaterialIconView cloneIcon = new MaterialIconView(MaterialIcon.CONTENT_COPY);
            cloneIcon.getStyleClass().add("glyph-icon-clone");
            JFXButton cloneButton = new JFXButton("", cloneIcon);
            cloneButton.getStyleClass().add("btn_clone_part");
            cloneButton.setOnMouseEntered(event -> nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;"));
            cloneButton.setOnMouseExited(event -> nombre.setStyle("-fx-font-weight: normal; -fx-font-size: 15px;"));
            MaterialIconView closeIcon = new MaterialIconView(MaterialIcon.CLOSE);
            closeIcon.getStyleClass().add("glyph-icon-delete");
            JFXButton deleteButton = new JFXButton("", closeIcon);
            deleteButton.getStyleClass().add("btn_del_part");
            deleteButton.setOnMouseEntered(event -> nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;"));
            deleteButton.setOnMouseExited(event -> nombre.setStyle("-fx-font-weight: normal; -fx-font-size: 15px;"));
            GridPane.setMargin(nombre, new Insets(4, 0, 0, 0));
            GridPane.setMargin(visible, new Insets(4, 0, 0, 16));
            GridPane.setMargin(editButton, new Insets(4, 0, 0, 0));
            GridPane.setMargin(cloneButton, new Insets(4, 0, 0, 0));
            GridPane.setMargin(deleteButton, new Insets(4, 0, 0, 0));
            gridPane.add(nombre, 0, nodesByRow.size()+3);
            gridPane.add(visible, 1, nodesByRow.size()+3);
            gridPane.add(editButton, 2, nodesByRow.size()+3);
            gridPane.add(cloneButton, 3, nodesByRow.size()+3);
            gridPane.add(deleteButton, 4, nodesByRow.size()+3);
            Integer index = nodesByRow.size() > 0 ? Collections.max(nodesByRow.keySet())+1 : 0;
            nodesByRow.put(index, new ArrayList<>(Arrays.asList(nombre, visible, editButton, cloneButton, deleteButton)));
            editButton.setOnAction(actionEvent -> getParentController().openEditor("EditorHidraulico.fxml", "Editor Hidráulico - " + datosHidraulicoCorrida.getNombre(), Text.HIDRAULICO_SIZE[0], Text.HIDRAULICO_SIZE[1], new EditorHidraulicoController(getDatosCorrida(), datosHidraulicoCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {
                DatosHidraulicoCorrida clonado = new DatosHidraulicoCorrida("Hidraulico clonado", "UTE", datosHidraulicoCorrida.getBarra(),datosHidraulicoCorrida.getRutaPQ(),
                        datosHidraulicoCorrida.getCantModInst(),datosHidraulicoCorrida.getFactorCompartir(),datosHidraulicoCorrida.getHidraulicosAguasArriba(),
                        datosHidraulicoCorrida.getHidraulicoAguasAbajo(), datosHidraulicoCorrida.getPotMin(), datosHidraulicoCorrida.getPotMax(), datosHidraulicoCorrida.getRendPotMin(),
                        datosHidraulicoCorrida.getRendPotMax(), datosHidraulicoCorrida.getVolFijo(), datosHidraulicoCorrida.getqTur1Max(), datosHidraulicoCorrida.getAporte(),
                        datosHidraulicoCorrida.getCantModIni(), datosHidraulicoCorrida.getDispMedia(), datosHidraulicoCorrida.gettMedioArreglo(), datosHidraulicoCorrida.getfCoefEnerg(),
                        datosHidraulicoCorrida.getfCoAA(), datosHidraulicoCorrida.getSaltoMin(), datosHidraulicoCorrida.getCotaInundacionAguasAbajo(), datosHidraulicoCorrida.getCotaInundacionAguasArriba(),
                        datosHidraulicoCorrida.getfQEroMin(), datosHidraulicoCorrida.getfCoVo(), datosHidraulicoCorrida.getfVoCo(), datosHidraulicoCorrida.getfEvaporacion(),
                        datosHidraulicoCorrida.getCoefEvaporacion(), datosHidraulicoCorrida.getfFiltracion(), datosHidraulicoCorrida.getfQVerM(), datosHidraulicoCorrida.getVarsEstado(),
                        datosHidraulicoCorrida.getEpsilonCaudalErogadoIteracion(), datosHidraulicoCorrida.isSalDetallada(), datosHidraulicoCorrida.getMantProgramado(),
                        datosHidraulicoCorrida.getCostoFijo(), datosHidraulicoCorrida.getCostoVariable(), datosHidraulicoCorrida.getVolReservaEstrategica(), datosHidraulicoCorrida.getValorMinReserva(),
                        datosHidraulicoCorrida.isValorAplicaOptim(), datosHidraulicoCorrida.isHayReservaEstrategica(),datosHidraulicoCorrida.isVertimientoConstante(),
                        datosHidraulicoCorrida.isHayVolObjVert(), datosHidraulicoCorrida.getVolObjVert(), datosHidraulicoCorrida.isHayControldeCotasMinimas(),
                        datosHidraulicoCorrida.getVolumenControlMinimo(), datosHidraulicoCorrida.getPenalidadControlMinimo(),datosHidraulicoCorrida.isHayControldeCotasMaximas(),
                        datosHidraulicoCorrida.getVolumenControlMaximo(), datosHidraulicoCorrida.getPenalidadControlMaximo());

                clonado.getValoresComportamientos().put(Constantes.COMPLAGO, datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPLAGO));
                clonado.getValoresComportamientos().put(Constantes.COMPCOEFENERGETICO, datosHidraulicoCorrida.getValoresComportamientos().get(Constantes.COMPCOEFENERGETICO));


                getParentController().openEditor("EditorHidraulico.fxml", "Editor Hidráulico - " + clonado.getNombre(), Text.HIDRAULICO_SIZE[0], Text.HIDRAULICO_SIZE[1], new EditorHidraulicoController(getDatosCorrida(), clonado, thisController, true));
                datosHidraulicosCorrida.getOrdenCargaXML().add("Hidraulico clonado");
                datosHidraulicosCorrida.getListaUtilizados().add("Hidraulico clonado");

            });
            deleteButton.setOnAction(actionEvent -> {
                hidraulicoABorrar = datosHidraulicoCorrida.getNombre();
                indHidraulicoABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Hidráulico " + hidraulicoABorrar);
            });
        }
        long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        inputCompLago.getSelectionModel().select(datosHidraulicosCorrida.getValoresComportamiento().get(Constantes.COMPLAGO).getValor(instanteActual));
        inputCompCoefEnergetico.getSelectionModel().select(datosHidraulicosCorrida.getValoresComportamiento().get(Constantes.COMPCOEFENERGETICO).getValor(instanteActual));
        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });

        evForField(datosHidraulicosCorrida.getValoresComportamiento().get(Constantes.COMPLAGO), "compLago", inputCompLagoEV, Text.EV_VAR, new ArrayList<>(Arrays.asList(Constantes.HIDROCONLAGO, Constantes.HIDROSINLAGO, Constantes.HIDROSINLAGOENOPTIM)), inputCompLago);
        evForField(datosHidraulicosCorrida.getValoresComportamiento().get(Constantes.COMPCOEFENERGETICO), "compCoefEnergetico", inputCompCoefEnergeticoEV, Text.EV_VAR, new ArrayList<>(Arrays.asList(Constantes.HIDROCOEFENERGCONSTANTES, Constantes.HIDROPOTENCIACAUDAL)), inputCompCoefEnergetico);

    }

    public void borrarHidraulico(String hidraulicoABorrar){
        datosHidraulicosCorrida.getOrdenCargaXML().remove(hidraulicoABorrar);
        datosHidraulicosCorrida.getListaUtilizados().remove(hidraulicoABorrar);
        listaDatosHidraulicoCorrida.remove(hidraulicoABorrar);
        unloadData();
        refresh();
    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){
            if(confirmacion) {
                borrarHidraulico(hidraulicoABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }
}

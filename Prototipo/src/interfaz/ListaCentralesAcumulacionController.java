/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaCentralesAcumulacionController is part of MOP.
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
import datatypes.DatosCorrida;
import datatypes.DatosAcumuladorCorrida;
import datatypes.DatosAcumuladoresCorrida;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import logica.CorridaHandler;
import tiempo.Evolucion;
import tiempo.EvolucionConstante;
import tiempo.SentidoTiempo;
import utilitarios.Constantes;
import utilitarios.UtilStrings;

import java.util.*;


public class ListaCentralesAcumulacionController extends ListaParticipante{
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> inputCompPaso;

    @FXML private JFXButton inputCompPasoEV;

    private DatosAcumuladoresCorrida datosAcumuladoresCorrida;
    private Hashtable<String, DatosAcumuladorCorrida> listaDatosAcumuladorCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();
     private ListaCentralesAcumulacionController thisController;

    private String bateriaABorrar;
    private int indBateriaABorrar;




    public ListaCentralesAcumulacionController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida, parentController);
        this.datosAcumuladoresCorrida = datosCorrida.getAcumuladores();
        this.listaDatosAcumuladorCorrida = datosCorrida.getAcumuladores().getAcumuladores();
        thisController = this;
    }

    @FXML
    public void initialize(){

        inputCompPaso.getItems().addAll(new ArrayList<>(Arrays.asList(Constantes.ACUCIERRAPASO, Constantes.ACUMULTIPASO, Constantes.ACUBALANCECRONOLOGICO)));

        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);
        unloadData();
    }





    /**
     *  Método para cargar los datos en el DatosAcumuladoresCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA CENTRALES ACUMULACIÓN");
        Hashtable<String, Evolucion<String>> valoresComportamiento = new Hashtable<>();
        Evolucion<String> comPaso = new EvolucionConstante<>(inputCompPaso.getValue(), new SentidoTiempo(1));
        valoresComportamiento.put(Constantes.COMPPASO, comPaso);
        datosAcumuladoresCorrida.setValoresComportamiento(valoresComportamiento);
        datosAcumuladoresCorrida.setAcumuladores(listaDatosAcumuladorCorrida);
        getDatosCorrida().setAcumuladores(datosAcumuladoresCorrida);
    }

    /**
     * Método para obtener los datos del DatosAcumuladoresCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA CENTRALES ACUMULACIÓN");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosAcumuladorCorrida datosAcumuladorCorrida : listaDatosAcumuladorCorrida.values()){
            Label nombre = new Label(datosAcumuladorCorrida.getNombre());
            if(datosAcumuladorCorrida.controlDatosCompletos().size() > 0){
                nombre.setText(datosAcumuladorCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                getParentController().setVisibleParticipate(datosAcumuladorCorrida.getNombre()+"(A)", visible.isSelected());
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
            editButton.setOnAction(actionEvent -> getParentController().openEditor("EditorCentralAcumulacion.fxml", "Editor Central Acumulación - " + datosAcumuladorCorrida.getNombre() , Text.ACUMULADOR_SIZE[0],  Text.ACUMULADOR_SIZE[1], new EditorCentralAcumulacionController(getDatosCorrida(), datosAcumuladorCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {
                DatosAcumuladorCorrida clonado = new DatosAcumuladorCorrida("Acumulador clonado", null, datosAcumuladorCorrida.getBarra(), datosAcumuladorCorrida.getCantModInst(),
                        datosAcumuladorCorrida.getPotMin(), datosAcumuladorCorrida.getPotMax(), datosAcumuladorCorrida.getPotAlmacenadaMin(), datosAcumuladorCorrida.getPotAlmacenadaMax(),
                        datosAcumuladorCorrida.getEnergAlmacMax(), datosAcumuladorCorrida.getRendIny(),datosAcumuladorCorrida.getRendAlmac(), datosAcumuladorCorrida.getCantModIni(),
                        datosAcumuladorCorrida.getDispMedia(), datosAcumuladorCorrida.gettMedioArreglo(), datosAcumuladorCorrida.isSalDetallada(), datosAcumuladorCorrida.getMantProgramado(),
                        datosAcumuladorCorrida.getCostoFijo(), datosAcumuladorCorrida.getCostoVariable(), datosAcumuladorCorrida.getVarsEstado(), datosAcumuladorCorrida.getFactorUso(),
                        datosAcumuladorCorrida.getEnergIniPaso(), datosAcumuladorCorrida.isSalDetallada(), datosAcumuladorCorrida.getHayPotObligatoria(), datosAcumuladorCorrida.getCostoFallaPotOblig(),
                        datosAcumuladorCorrida.getPotOblig());
                getParentController().openEditor("EditorCentralAcumulacion.fxml", "Editor Central Acumulación - " + clonado.getNombre(), Text.ACUMULADOR_SIZE[0],  Text.ACUMULADOR_SIZE[1], new EditorCentralAcumulacionController(getDatosCorrida(), clonado, thisController, true));
                datosAcumuladoresCorrida.getOrdenCargaXML().add("Acumulador clonado");
                datosAcumuladoresCorrida.getListaUtilizados().add("Acumulador clonado");
            });
            deleteButton.setOnAction(actionEvent -> {

                bateriaABorrar = datosAcumuladorCorrida.getNombre();
                indBateriaABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Central Acumulacion " + bateriaABorrar);
            });
        }
        inputCompPaso.getSelectionModel().select(datosAcumuladoresCorrida.getValoresComportamiento().get(Constantes.COMPPASO).getValor(CorridaHandler.getInstance().dameInstanteActual()));

        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });
        evForField(datosAcumuladoresCorrida.getValoresComportamiento().get(Constantes.COMPPASO), "compPaso", inputCompPasoEV, Text.EV_VAR, new ArrayList<>(Arrays.asList(Constantes.ACUCIERRAPASO, Constantes.ACUMULTIPASO, Constantes.ACUBALANCECRONOLOGICO)), inputCompPaso);

    }

    public void borrarAcumulador(String acumuladorABorrar){
        datosAcumuladoresCorrida.getOrdenCargaXML().remove(acumuladorABorrar);
        datosAcumuladoresCorrida.getListaUtilizados().remove(acumuladorABorrar);
        listaDatosAcumuladorCorrida.remove(acumuladorABorrar);
        unloadData();
        refresh();
    }
    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){

            if(confirmacion) {
                borrarAcumulador(bateriaABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }
}

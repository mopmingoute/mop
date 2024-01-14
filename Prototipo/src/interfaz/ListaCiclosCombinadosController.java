/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaCiclosCombinadosController is part of MOP.
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
import datatypes.DatosCicloCombinadoCorrida;
import datatypes.DatosCiclosCombinadosCorrida;
import datatypes.DatosCorrida;
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

import java.util.*;

public class ListaCiclosCombinadosController extends ListaParticipante {

    @FXML
    private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> inputCompCicloCombinado;
    @FXML private JFXButton inputCompCicloCombinadoEV;


    private DatosCiclosCombinadosCorrida datosCiclosCombinadosCorrida;
    private Hashtable<String, DatosCicloCombinadoCorrida> listaDatosCicloCombinadoCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();

    private String cCombinadoABorrar;
    private int indCCombinadoABorrar;


    private ListaCiclosCombinadosController thisController;

    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    public ListaCiclosCombinadosController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida,parentController);
        this.datosCiclosCombinadosCorrida = datosCorrida.getCcombinados();
        this.listaDatosCicloCombinadoCorrida = datosCorrida.getCcombinados().getCcombinados();
        thisController = this;
    }

    @FXML
    public void initialize(){

        inputCompCicloCombinado.getItems().addAll(new ArrayList<>(Text.COMP_CICLO_COMB_VALS));

        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);
        unloadData();
    }




    /**
     *  Método para cargar los datos en el DatosTermicosCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA CICLOS COMBINADOS");
        Hashtable<String, Evolucion<String>> valoresComportamiento = new Hashtable<>();
        Evolucion<String> compCicloComb = new EvolucionConstante<>(inputCompCicloCombinado.getValue(), new SentidoTiempo(1));
        valoresComportamiento.put(Constantes.COMPCC, compCicloComb);
        datosCiclosCombinadosCorrida.setValoresComportamiento(valoresComportamiento);
        datosCiclosCombinadosCorrida.setCcombinados(listaDatosCicloCombinadoCorrida);
        getDatosCorrida().setCcombinados(datosCiclosCombinadosCorrida);
    }

    /**
     * Método para obtener los datos del DatosTermicosCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA CICLOS COMBINADOS");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosCicloCombinadoCorrida datosCicloCombinadoCorrida : listaDatosCicloCombinadoCorrida.values()){
            Label nombre = new Label(datosCicloCombinadoCorrida.getNombre());
            if(datosCicloCombinadoCorrida.controlDatosCompletos().size() > 0){
                nombre.setText(datosCicloCombinadoCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                getParentController().setVisibleParticipate(datosCicloCombinadoCorrida.getNombre()+"(C)", visible.isSelected());
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
            editButton.setOnAction(actionEvent -> getParentController().openEditor("EditorCicloCombinado.fxml", "Editor Ciclo Combinado - " + datosCicloCombinadoCorrida.getNombre(), Text.CICLO_COMBINADO_SIZE[0], Text.CICLO_COMBINADO_SIZE[1], new EditorCicloCombinadoController(getDatosCorrida(), datosCicloCombinadoCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {
                DatosCicloCombinadoCorrida clonado = new DatosCicloCombinadoCorrida(datosCicloCombinadoCorrida.getDatosTGs(),
                        datosCicloCombinadoCorrida.getDatosCCs(),datosCicloCombinadoCorrida.getPotMax1CV());

                clonado.setNombre("Ciclo Combinado clonado");

                clonado.setPropietario("UTE");
                clonado.setBarra(datosCicloCombinadoCorrida.getBarra());

                clonado.setPotMax1CV(datosCicloCombinadoCorrida.getPotMax1CV());

                clonado.setCostoArranque1TGCicloAbierto(datosCicloCombinadoCorrida.getCostoArranque1TGCicloAbierto());
                clonado.setCostoArranque1TGCicloCombinado(datosCicloCombinadoCorrida.getCostoArranque1TGCicloCombinado());

                clonado.setListaCombustibles(datosCicloCombinadoCorrida.getListaCombustibles());
                clonado.setBarrasCombustible(datosCicloCombinadoCorrida.getBarrasCombustible());

                clonado.setPosiblesArranques(datosCicloCombinadoCorrida.getPosiblesArranques());
                clonado.setPotRampaArranque(datosCicloCombinadoCorrida.getPotRampaArranque());
                clonado.setPosiblesParadas(datosCicloCombinadoCorrida.getPosiblesParadas());

                clonado.setValoresComportamientos(datosCicloCombinadoCorrida.getValoresComportamientos());
                clonado.setSalDetallada(datosCicloCombinadoCorrida.isSalDetallada());

                getParentController().openEditor("EditorCicloCombinado.fxml", "Editor Ciclo Combinado - " + clonado.getNombre(), Text.CICLO_COMBINADO_SIZE[0], Text.CICLO_COMBINADO_SIZE[1], new EditorCicloCombinadoController(getDatosCorrida(), clonado, thisController, true));
                datosCiclosCombinadosCorrida.getOrdenCargaXML().add("Ciclo Combinado clonado");
                datosCiclosCombinadosCorrida.getListaUtilizados().add("Ciclo Combinado clonado");
            });
            deleteButton.setOnAction(actionEvent -> {
                cCombinadoABorrar = datosCicloCombinadoCorrida.getNombre();
                indCCombinadoABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Ciclo combinado " + cCombinadoABorrar);
            });
        }
        long instanteActual =CorridaHandler.getInstance().dameInstanteActual(); 
        //inputCompCicloCombinado.getSelectionModel().select(datosCiclosCombinadosCorrida.getValoresComportamiento().get(Constantes.COMPMINTEC).getValor());
        inputCompCicloCombinado.getSelectionModel().select(datosCiclosCombinadosCorrida.getValoresComportamiento().get(Constantes.COMPCC).getValor(instanteActual));
        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });

        evForField(datosCiclosCombinadosCorrida.getValoresComportamiento().get(Constantes.COMPMINTEC), "compMinTecnicos", inputCompCicloCombinadoEV, Text.EV_VAR, new ArrayList<>(Arrays.asList(Constantes.TERSINMINTEC, Constantes.TERMINTECFORZADO, Constantes.TERVARENTERAS, Constantes.TERVARENTERASYVARESTADO, Constantes.TERDOSPASOS)), inputCompCicloCombinado);

    }

    public void borrarCicloCombinado(String cCombinadoABorrar){
        datosCiclosCombinadosCorrida.getOrdenCargaXML().remove(cCombinadoABorrar);
        datosCiclosCombinadosCorrida.getListaUtilizados().remove(cCombinadoABorrar);
        listaDatosCicloCombinadoCorrida.remove(cCombinadoABorrar);
        unloadData();
        refresh();
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){

            if(confirmacion) {
                borrarCicloCombinado(cCombinadoABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }
}

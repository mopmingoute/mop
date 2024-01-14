/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ListaEolicosController is part of MOP.
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
import datatypes.*;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.util.*;


public class ListaEolicosController extends ListaParticipante {
    @FXML private JFXButton compPorDefectoBtn;
    @FXML private JFXButton compPorDefectoDoneBtn;
    @FXML private AnchorPane compPorDefectoPane;
    @FXML private JFXCheckBox todosVisiblesInput;
    @FXML private GridPane gridPane;


    private DatosEolicosCorrida datosEolicosCorrida;
    private Hashtable<String, DatosEolicoCorrida> listaDatosEolicoCorrida;
    private HashMap<Integer, List<Node>> nodesByRow = new HashMap<>();

    private ListaEolicosController thisController;

    private String eolicoABorrar;
    private int indEolicoABorrar;

    public ListaEolicosController(DatosCorrida datosCorrida, Manejador parentController) {
        super(datosCorrida, parentController);
        this.datosEolicosCorrida = datosCorrida.getEolicos();
        this.listaDatosEolicoCorrida = datosCorrida.getEolicos().getEolicos();
        thisController = this;
    }

    @FXML
    public void initialize(){
        getParentController().asignarTooltips(thisController);

        compPorDefectoBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(true));
        compPorDefectoDoneBtn.setOnAction(actionEvent -> compPorDefectoPane.setVisible(false));
        todosVisiblesInput.setSelected(true);
        unloadData();
    }



    /**
     *  Método para cargar los datos en el DatosEolicosCorrida
     */
    public void loadData(){
        System.out.println("LOAD DATA-LISTA EÓLICOS");
        datosEolicosCorrida.setEolicos(listaDatosEolicoCorrida);
        getDatosCorrida().setEolicos(datosEolicosCorrida);
    }

    /**
     * Método para obtener los datos del DatosEolicosCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    public void unloadData() {
        System.out.println("UNLOAD DATA-LISTA EóLICOS");
        ArrayList<JFXCheckBox> checkBoxes = new ArrayList<>();
        for(DatosEolicoCorrida datosEolicoCorrida : listaDatosEolicoCorrida.values()){
            Label nombre = new Label(datosEolicoCorrida.getNombre());
            if(datosEolicoCorrida.controlDatosCompletos().size() > 0){
                nombre.setText(datosEolicoCorrida.getNombre() + " (*)");
                nombre.setTextFill(Text.COLOR_NOMBRE_PARTICIPANTE_INCOMPLETO);
            }
            nombre.setStyle("-fx-font-size: 15px;");
            JFXCheckBox visible = new JFXCheckBox(" ");
            visible.setSelected(true);
            visible.setOnAction(action -> {
                getParentController().setVisibleParticipate(datosEolicoCorrida.getNombre()+"(E)", visible.isSelected());
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
            editButton.setOnAction(actionEvent -> getParentController().openEditor("EditorEolico.fxml", "Editor Eólico - " + datosEolicoCorrida.getNombre(), Text.EOLICO_SIZE[0],  Text.EOLICO_SIZE[1], new EditorEolicoController(getDatosCorrida(), datosEolicoCorrida, thisController, false)));
            cloneButton.setOnAction(actionEvent -> {
                DatosEolicoCorrida datosEolicoCorrida2 = new DatosEolicoCorrida("Eolico clonado", "UTE", datosEolicoCorrida.getBarra(), datosEolicoCorrida.getCantModInst(),
                        datosEolicoCorrida.getPotMin(),datosEolicoCorrida.getPotMax(), datosEolicoCorrida.getFactor(), datosEolicoCorrida.getCantModIni(), datosEolicoCorrida.getDispMedia(),
                        datosEolicoCorrida.gettMedioArreglo(), datosEolicoCorrida.isSalDetallada(), datosEolicoCorrida.getMantProgramado(), datosEolicoCorrida.getCostoFijo(), datosEolicoCorrida.getCostoVariable());
                getDatosCorrida().getEolicos().getEolicos().put("Eolico clonado",datosEolicoCorrida2);
                getParentController().openEditor("EditorEolico.fxml", "Editor Eólico - " + datosEolicoCorrida2.getNombre(), Text.EOLICO_SIZE[0],  Text.EOLICO_SIZE[1], new EditorEolicoController(getDatosCorrida(), datosEolicoCorrida2,thisController, true));
                datosEolicosCorrida.getOrdenCargaXML().add("Eolico clonado");
                datosEolicosCorrida.getListaUtilizados().add("Eolico clonado");
            });
            deleteButton.setOnAction(actionEvent -> {
                eolicoABorrar = datosEolicoCorrida.getNombre();
                indEolicoABorrar = index;
                super.mostrarMensajeConfirmacionBorrado("Eólico " + eolicoABorrar);
            });
        }

        todosVisiblesInput.setOnAction(action -> {
            for(JFXCheckBox checkBox : checkBoxes){
                checkBox.setSelected(!todosVisiblesInput.isSelected());
                checkBox.fire();
            }
        });
    }

    public void borrarEolico(String eolicoABorrar){
        datosEolicosCorrida.getOrdenCargaXML().remove(eolicoABorrar);
        datosEolicosCorrida.getListaUtilizados().remove(eolicoABorrar);
        listaDatosEolicoCorrida.remove(eolicoABorrar);
        //gridPane.getChildren().removeAll(nodesByRow.get(indEolicoABorrar));
        //nodesByRow.remove(indEolicoABorrar);
        unloadData();
        refresh();
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {
        if(codigoMensaje == 2){

            if(confirmacion) {
                borrarEolico(eolicoABorrar);
                mostrarMensajeAviso("El participante se eliminó correctamente", this.getParentController(),0);
            }
        }
    }
}

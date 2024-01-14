/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * EditorBarraController is part of MOP.
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

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import datatypes.DatosBarraCombCorrida;
import javafx.fxml.FXML;

import java.util.ArrayList;

public class EditorBarraController {
    @FXML private JFXTextField inputNombre;
    @FXML private JFXCheckBox inputSalidaDetallada;

    private DatosBarraCombCorrida datosBarraCombCorrida;
    private boolean edicion;

    private boolean editoVariables = false;

    public EditorBarraController(){}

    public EditorBarraController(DatosBarraCombCorrida datosBarraCombCorrida){
        this.datosBarraCombCorrida = datosBarraCombCorrida;
        edicion = true;
    }
    public boolean isEditoVariables() { return editoVariables;  }

    public void setEditoVariables(boolean editoVariables) {  this.editoVariables = editoVariables;   }
    @FXML
    public void initialize(){
        if(edicion){
            unloadData();
        }
    }

    public DatosBarraCombCorrida getDatosBarraCombCorrida(){
        loadData();
        return datosBarraCombCorrida;
    }


    /**
     *  Método para cargar los datos en el DatosBarraCombCorrida
     */
    private void loadData(){
        System.out.println("LOAD DATA-BARRA");
        String nombre = inputNombre.getText();


        if(edicion){
            datosBarraCombCorrida.setNombre(nombre);
//            datosBarraCombCorrida.setSalDetallada(salidaDetallada);
        }else {
            datosBarraCombCorrida = new DatosBarraCombCorrida(nombre);
        }

    }

    /**
     * Método para obtener los datos del DatosBarraCombCorrida y ponerlos en la interfaz (solo en modo edición)
     */
    private void unloadData() {
        System.out.println("UNLOAD DATA-BARRA");
        inputNombre.setText(datosBarraCombCorrida.getNombre());
        inputNombre.textProperty().addListener((observable, oldValue, newValue) -> {    editoVariables = true;   });
//        inputSalidaDetallada.selectedProperty().addListener((observable, oldValue, newValue) -> {  editoVariables = true;    });
//        inputSalidaDetallada.setSelected(datosBarraCombCorrida.isSalDetallada());
        editoVariables = false;
    }

    public ArrayList<String> controlDatosCompletos(){
        ArrayList errores = new ArrayList();
        if(inputNombre.getText().trim().equals("")){ errores.add("Editor Barra sin nombre."); }
        return errores;
    }
}

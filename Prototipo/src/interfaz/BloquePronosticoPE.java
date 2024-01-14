/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * BloquePronosticoPE is part of MOP.
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
import com.jfoenix.controls.JFXTextField;
import datatypes.DatosCorrida;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import tiempo.Evolucion;
import utilitarios.UtilStrings;

import java.util.ArrayList;
import java.util.HashMap;

public class BloquePronosticoPE {

    @FXML  private Label labelNombre;

    @FXML private JFXTextField inputPeso;
    @FXML private JFXTextField inputValor;
    @FXML private JFXButton inputPesoEV;
    @FXML private JFXButton inputValorEV;

    private String nombre;

    private DatosCorrida datosCorrida;
    private HashMap<String, EVController> evsPorNombre = new HashMap<>();

    public BloquePronosticoPE(DatosCorrida datosCorrida, String nombre){
        this.nombre = nombre;
        this.datosCorrida = datosCorrida;

    }


    @FXML
    public void initialize() {
        labelNombre.setText(nombre);


        //EVController evController = new EVController(listaConUnaEv, datosCorrida, tipoData, vars, componenteAsociado, "EV: " + UtilStrings.separarCamelCase(key));

        evForField(null, "inputPeso", inputPesoEV, Text.EV_NUM_DOUBLE, null, inputPeso);
        evForField(null, "inputValor", inputValorEV, Text.EV_NUM_DOUBLE, null, inputValor);

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
    public Evolucion getPeso(){
        return evsPorNombre.get("inputPeso").getEv();
    }

    public Evolucion getValor(){
        return evsPorNombre.get("inputValor").getEv();
    }

}

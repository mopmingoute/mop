/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LineaDeEntradaCardController is part of MOP.
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

import datatypes.DatosMaquinaLT;
import datatypes.DatosUsoMaquinaLT;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.text.SimpleDateFormat;


public class LineaDeEntradaCardController {
    @FXML private GridPane gridPane;
    @FXML private Label labelNombre;
    @FXML private Label labelPotencia;
    @FXML private Label labelFechaIni;
    @FXML private Label labelFechaFin;
    @FXML private Circle circuloTipoParticipante;

    private DatosMaquinaLT datosMaquinaLT;
    private DatosUsoMaquinaLT datosUsoMaquinaLT;

    public LineaDeEntradaCardController(DatosMaquinaLT datosMaquinaLT, DatosUsoMaquinaLT datosUsoMaquinaLT){
        this.datosMaquinaLT = datosMaquinaLT;
        this.datosUsoMaquinaLT = datosUsoMaquinaLT;
    }

    @FXML
    public void initialize(){
        labelNombre.setText(datosMaquinaLT.getNombre());
        labelPotencia.setText(String.valueOf(datosUsoMaquinaLT.getPotInst()));
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        labelFechaIni.setText(format.format(datosUsoMaquinaLT.getFechaIni().getTime()));
        labelFechaFin.setText(datosUsoMaquinaLT.getFechaFin() == null ? "FIN_TIEMPO" : format.format(datosUsoMaquinaLT.getFechaFin().getTime()));
        String color = Text.COLORES.get(datosMaquinaLT.getTipo());
        circuloTipoParticipante.setFill(Paint.valueOf(color));

        if(datosUsoMaquinaLT.getCantModInst() > 4){
            Rectangle mod = new Rectangle();
            mod.setWidth(15);
            mod.setHeight(15);
            mod.setArcWidth(5);
            mod.setArcHeight(5);
            mod.setFill(Paint.valueOf(color));
            gridPane.add(mod, 1, 2);
            GridPane.setMargin(mod, new Insets(0, 0, 0, 10));
            Label labelCantMod = new Label("x"+datosUsoMaquinaLT.getCantModInst());
            gridPane.add(labelCantMod, 1, 2);
            GridPane.setMargin(labelCantMod, new Insets(0, 0, 0, 28));
        }else {
            for (int i = 0; i < datosUsoMaquinaLT.getCantModInst(); i++) {
                Rectangle mod = new Rectangle();
                mod.setWidth(15);
                mod.setHeight(15);
                mod.setArcWidth(5);
                mod.setArcHeight(5);
                mod.setFill(Paint.valueOf(color));
                gridPane.add(mod, 1, 2);
                GridPane.setMargin(mod, new Insets(0, 0, 0, 10 + 20 * i));
            }
        }
    }
}

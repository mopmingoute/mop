/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LineaDeEntradaRenderer is part of MOP.
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
import com.jfoenix.controls.JFXSlider;
import datatypes.DatosCorrida;
import datatypes.DatosMaquinaLT;
import datatypes.DatosUsoMaquinaLT;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import presentacion.PresentacionHandler;

import java.util.*;

public class LineaDeEntradaController {

    public static final double ESCALA_1_ANIO  = 116.5;
    public static final double ESCALA_5_ANIO  = 23.3;
    public static final double ESCALA_20_ANIO = 5.825;
    public static final double ESCALA_INCIAL  = 51.26;

    @FXML private GridPane gridPane;
    @FXML private GridPane gridPaneNombres;
    @FXML private GridPane gridPaneFechas;
    @FXML private ScrollPane scrollPaneContent;
    @FXML private ScrollPane scrollPaneNombres;
    @FXML private ScrollPane scrollPaneFechas;
    @FXML private JFXSlider sliderEscala;

    private PresentacionHandler ph = PresentacionHandler.getInstance();

    private ArrayList<DatosMaquinaLT> listaDatosMaquinaLT = new ArrayList<>();
    private HashMap<String, Integer> posParticipante = new HashMap<>();


    public LineaDeEntradaController(){

    }

    @FXML
    public void initialize() {

        gridPane.getRowConstraints().clear();

        sliderEscala.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!oldValue.equals(newValue)) {
                double escala = (double)newValue <= 2 ? -93.2*(double)newValue+209.7 : -17.475*(double)newValue+58.25;
                if(escala != -1) {
                    update(escala);
                }
            }
        });

        scrollPaneNombres.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneFechas.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneNombres.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneFechas.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneNombres.setPannable(false);
        scrollPaneFechas.setPannable(false);
        scrollPaneContent.setStyle("-fx-background-color:transparent;");
        scrollPaneNombres.setStyle("-fx-background-color:transparent;");
        scrollPaneFechas.setStyle("-fx-background-color:transparent;");
        scrollPaneNombres.vvalueProperty().bindBidirectional(scrollPaneContent.vvalueProperty());
        scrollPaneFechas.hvalueProperty().bindBidirectional(scrollPaneContent.hvalueProperty());

    }

    public void popularListaDatosMaquinaLT(DatosCorrida datosCorrida){
        listaDatosMaquinaLT = ph.dameDatosGraficoLineaTiempo(datosCorrida);
    }


    public void render(Double escala, DatosCorrida datosCorrida){

        gridPane.getRowConstraints().clear();
        gridPaneNombres.getRowConstraints().clear();
        gridPaneFechas.getColumnConstraints().clear();
        gridPane.getColumnConstraints().clear();

        gridPane.getChildren().removeAll(gridPane.getChildren());
        gridPaneNombres.getChildren().removeAll(gridPaneNombres.getChildren());
        gridPaneFechas.getChildren().removeAll(gridPaneFechas.getChildren());

        posParticipante.clear();


        int anioIni = 10000;
        int anioFin = 0;

        int minAlpha = 50;
        int maxAlpha = 150;

        for (DatosMaquinaLT datosMaquinaLT : listaDatosMaquinaLT){
            for(DatosUsoMaquinaLT datosUsoMaquinaLT : datosMaquinaLT.getUsos()){
                if(datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR) < anioIni){
                    anioIni = datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR);
                }
                if(datosUsoMaquinaLT.getFechaFin() != null && datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR) > anioFin){
                    anioFin = datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR);
                }
                if(datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR) > anioFin){
                    anioFin = datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR);
                }
                if(datosUsoMaquinaLT.getPotInst() > maxAlpha){
                    maxAlpha = (int) Math.round(datosUsoMaquinaLT.getPotInst());
                }
                if(datosUsoMaquinaLT.getPotInst() < minAlpha){
                    minAlpha = (int) Math.round(datosUsoMaquinaLT.getPotInst());
                }
            }
        }

        int anioFinCorrida = Integer.parseInt(datosCorrida.getFinCorrida().substring(6,10));
        anioFin = (anioFin < anioFinCorrida)? anioFin:anioFinCorrida;

        minAlpha = (maxAlpha - minAlpha) / 2; // TODO: 05/08/2020 mejor fórmula

        gridPane.getColumnConstraints().add(new ColumnConstraints(80,80,80));
        gridPaneFechas.getColumnConstraints().add(new ColumnConstraints(80,80,80));
        for(int i=anioIni;i<=anioFin;i++){
            Label labelAnio = new Label(String.valueOf(i));
            labelAnio.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
            gridPaneFechas.add(labelAnio,12*(i-anioIni)+1+1,0, 12,1);
            for(int j=1;j<=12;j++) {
                ColumnConstraints colConstraints = new ColumnConstraints();
                colConstraints.setMinWidth(escala);
                colConstraints.setMaxWidth(escala);
                gridPane.getColumnConstraints().add(colConstraints);
                gridPaneFechas.getColumnConstraints().add(colConstraints);
            }
        }

        for (DatosMaquinaLT datosMaquinaLT : listaDatosMaquinaLT) {
            boolean addRow = false;
            if (!posParticipante.containsKey(datosMaquinaLT.getNombre())) {
                posParticipante.put(datosMaquinaLT.getNombre(), posParticipante.size() + 1);
                addRow = true;
            }
            for (DatosUsoMaquinaLT datosUsoMaquinaLT : datosMaquinaLT.getUsos()) {
                JFXButton reprParticipante = new JFXButton();
                if(datosUsoMaquinaLT.getCantModInst() > 4) {
                    reprParticipante.setText("■" + " x" + datosUsoMaquinaLT.getCantModInst() + "    " + datosMaquinaLT.getNombre() + "    " + datosUsoMaquinaLT.getPotInst());
                }else{
                    reprParticipante.setText("■" + " ■".repeat(Math.max(0, datosUsoMaquinaLT.getCantModInst() - 1)) +"    "+datosMaquinaLT.getNombre()+"    "+datosUsoMaquinaLT.getPotInst());
                }
                reprParticipante.setPrefHeight(38);
                reprParticipante.setPrefWidth(50000);
                String color = Text.COLORES.get(datosMaquinaLT.getTipo());

                int intAlpha;
                if(datosUsoMaquinaLT.getPotInst() <= 50){
                    intAlpha = 40;
                }else if(datosUsoMaquinaLT.getPotInst() <= 100){
                    intAlpha = 55;
                }else if(datosUsoMaquinaLT.getPotInst() <= 200){
                    intAlpha = 70;
                }else if(datosUsoMaquinaLT.getPotInst() <= 500){
                    intAlpha = 85;
                }else{
                    intAlpha = 90;
                }

                String alpha = Integer.toHexString(intAlpha*255/100);
                reprParticipante.setStyle("-fx-background-color: " + color + alpha + "; -fx-font-weight: bold; -fx-font-size: 15px;");
                reprParticipante.setTextFill(Paint.valueOf("ffffff"));
                int start = 12*(datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR) - anioIni) + datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH) + 1;

                int anioFinUsoMaquina = 0;
                int mesFinUsoMaquina = 0;
                if(datosUsoMaquinaLT.getFechaFin() == null || anioFin < datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR)){
                    anioFinUsoMaquina = anioFin;
                    mesFinUsoMaquina = 12;
                }else{
                    anioFinUsoMaquina = datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR);
                    mesFinUsoMaquina = datosUsoMaquinaLT.getFechaFin().get(Calendar.MONTH);
                }
                int diffAnios = (datosUsoMaquinaLT.getFechaFin() == null ? (anioFin - datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR)) : anioFinUsoMaquina - datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR));


                int span = 0;
                if (diffAnios == 0) {
                    span = (datosUsoMaquinaLT.getFechaFin() == null ? (12 - datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH)) : (mesFinUsoMaquina - datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH) + 1));
                } else if (diffAnios > 0) {
                    span = (12 - datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH)) + 12 * (diffAnios - 1) + (datosUsoMaquinaLT.getFechaFin() == null ? 12 : (mesFinUsoMaquina + 1));
                }

                if (addRow) {
                    Label nombreMaquina = new Label(datosMaquinaLT.getNombre());
                    nombreMaquina.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
                    gridPaneNombres.add(nombreMaquina, 0, posParticipante.size()-1);
                    RowConstraints rowConstraints = new RowConstraints();
                    rowConstraints.setMinHeight(50);
                    gridPane.getRowConstraints().add(rowConstraints);
                    gridPaneNombres.getRowConstraints().add(rowConstraints);
                    addRow = false;
                }

                if (span > 0){

                    gridPane.add(reprParticipante, start, posParticipante.size()-1, span, 1);


                    //Card: pop up que se ve al hacer mouse over sobre los btn que representan las maquinas
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeEntradaCard.fxml"));
                        loader.setController(new LineaDeEntradaCardController(datosMaquinaLT, datosUsoMaquinaLT));
                        AnchorPane newLoadedPane = loader.load();
                        PopOver popOver = new PopOver(newLoadedPane);
                        //popOver.setTitle("EV");

                        reprParticipante.setOnMouseEntered(actionEvent -> {
                            popOver.setFadeInDuration(Duration.seconds(.8));
                            popOver.show(reprParticipante);
                        });
                        reprParticipante.setOnMouseExited(actionEvent -> {
                            popOver.setFadeOutDuration(Duration.seconds(.8));
                            popOver.hide();
                        });

                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        int stop = 1+2;
    }

    private void update(Double escala){
        for(int i=1;i<gridPane.getColumnConstraints().size();i++){
            gridPane.getColumnConstraints().get(i).setMinWidth(escala);
            gridPane.getColumnConstraints().get(i).setMaxWidth(escala);
        }
    }

    public void setVisibleParticipate(String participante, Boolean visible){
        for(Node node : gridPane.getChildren()){
            if(posParticipante.containsKey(participante) && GridPane.getRowIndex(node) == posParticipante.get(participante)-1){
                node.setVisible(visible);
            }
        }
    }

    public void uncapHeight(){
        scrollPaneContent.setMaxHeight(730);
        scrollPaneNombres.setMaxHeight(730);
    }

    public void capHeight(){
        scrollPaneContent.setMaxHeight(307);
        scrollPaneNombres.setMaxHeight(307);
    }

    public void clean(){
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
        gridPaneNombres.getChildren().clear();
        gridPaneNombres.getColumnConstraints().clear();
        gridPaneNombres.getRowConstraints().clear();
        gridPaneFechas.getChildren().clear();
        gridPaneFechas.getColumnConstraints().clear();
        gridPaneFechas.getRowConstraints().clear();
        listaDatosMaquinaLT.clear();
        posParticipante.clear();
    }

    private ArrayList<DatosMaquinaLT> dummyData(){
        DatosMaquinaLT maqTermica1 = new DatosMaquinaLT("CTR",       Text.TIPO_TERMICO);
        DatosMaquinaLT maqTermica2 = new DatosMaquinaLT("TGExp",     Text.TIPO_TERMICO);
        DatosMaquinaLT maqTermica3 = new DatosMaquinaLT("PTigreA",   Text.TIPO_TERMICO);
        DatosMaquinaLT maqTermica4 = new DatosMaquinaLT("PTrigreB",  Text.TIPO_TERMICO);
        DatosMaquinaLT maqHidro1   = new DatosMaquinaLT("Bonete",    Text.TIPO_HIDRAULICO);
        DatosMaquinaLT maqHidro2   = new DatosMaquinaLT("Palmar",    Text.TIPO_HIDRAULICO);
        DatosMaquinaLT maqHidro3   = new DatosMaquinaLT("Salto",     Text.TIPO_HIDRAULICO);
        DatosMaquinaLT maqEolo1    = new DatosMaquinaLT("EoloDeci",  Text.TIPO_EOLICO);
        DatosMaquinaLT maqEolo2    = new DatosMaquinaLT("Biomasa",   Text.TIPO_EOLICO);
        DatosMaquinaLT maqSolar1   = new DatosMaquinaLT("SolarDeci", Text.TIPO_SOLAR);
        DatosMaquinaLT maqAcum1    = new DatosMaquinaLT("Bateria",   Text.TIPO_SOLAR);

        DatosUsoMaquinaLT uso1Termica1 = crearUso(2018, 2021, Calendar.JANUARY, Calendar.MARCH, 1);
        DatosUsoMaquinaLT uso2Termica1 = crearUso(2021, 2030, Calendar.JUNE, Calendar.OCTOBER, 2);
        maqTermica1.setUsos(new ArrayList<>(Arrays.asList(uso1Termica1, uso2Termica1)));

        DatosUsoMaquinaLT uso1Termica2 = crearUso(2016, 2024, Calendar.JANUARY, Calendar.JULY, 3);
        maqTermica2.setUsos(new ArrayList<>(Arrays.asList(uso1Termica2)));

        DatosUsoMaquinaLT uso1Termica3 = crearUso(2015, 2026, Calendar.JANUARY, Calendar.JULY, 2);
        DatosUsoMaquinaLT uso2Termica3 = crearUso(2027, 2029, Calendar.SEPTEMBER, Calendar.NOVEMBER, 2);
        maqTermica3.setUsos(new ArrayList<>(Arrays.asList(uso1Termica3, uso2Termica3)));

        DatosUsoMaquinaLT uso1Termica4 = crearUso(2012, 2025, Calendar.MAY, Calendar.AUGUST, 4);
        maqTermica4.setUsos(new ArrayList<>(Arrays.asList(uso1Termica4)));

        DatosUsoMaquinaLT uso1Hidro1 = crearUso(2013, 2020, Calendar.FEBRUARY, Calendar.AUGUST, 2);
        DatosUsoMaquinaLT uso2Hidro1 = crearUso(2023, 2027, Calendar.OCTOBER, Calendar.DECEMBER, 4);
        maqHidro1.setUsos(new ArrayList<>(Arrays.asList(uso1Hidro1, uso2Hidro1)));

        DatosUsoMaquinaLT uso1Hidro2 = crearUso(2024, 2030, Calendar.JANUARY, Calendar.NOVEMBER, 1);
        maqHidro2.setUsos(new ArrayList<>(Arrays.asList(uso1Hidro2)));

        DatosUsoMaquinaLT uso1Hidro3 = crearUso(2017, 2022, Calendar.FEBRUARY, Calendar.APRIL, 2);
        DatosUsoMaquinaLT uso2Hidro3 = crearUso(2022, 2030, Calendar.JULY, Calendar.DECEMBER, 4);
        maqHidro3.setUsos(new ArrayList<>(Arrays.asList(uso1Hidro3, uso2Hidro3)));

        DatosUsoMaquinaLT uso1Eolo1 = crearUso(2019, 2026, Calendar.APRIL, Calendar.OCTOBER, 2);
        maqEolo1.setUsos(new ArrayList<>(Arrays.asList(uso1Eolo1)));

        DatosUsoMaquinaLT uso1Eolo2 = crearUso(2015, 2029, Calendar.AUGUST, Calendar.OCTOBER, 1);
        maqEolo2.setUsos(new ArrayList<>(Arrays.asList(uso1Eolo2)));

        DatosUsoMaquinaLT uso1Solar1 = crearUso(2015, 2020, Calendar.MARCH, Calendar.AUGUST, 2);
        DatosUsoMaquinaLT uso2Solar1 = crearUso(2025, 2029, Calendar.AUGUST, Calendar.DECEMBER, 3);
        maqSolar1.setUsos(new ArrayList<>(Arrays.asList(uso1Solar1, uso2Solar1)));

        DatosUsoMaquinaLT uso1Acum1 = crearUso(2020, 2023, Calendar.JANUARY, Calendar.MAY, 1);
        DatosUsoMaquinaLT uso2Acum1 = crearUso(2024, 2027, Calendar.MAY, Calendar.SEPTEMBER, 4);
        maqAcum1.setUsos(new ArrayList<>(Arrays.asList(uso1Acum1, uso2Acum1)));

        return new ArrayList<>(Arrays.asList(maqTermica1, maqTermica2, maqTermica3, maqTermica4, maqHidro1, maqHidro2, maqHidro3, maqEolo1, maqEolo2, maqSolar1, maqAcum1));
//        return new ArrayList<>(Arrays.asList(maqTermica1, maqTermica2, maqTermica3, maqTermica4, maqHidro1, maqHidro2, maqHidro3, maqEolo1));
    }

    private DatosUsoMaquinaLT crearUso(int anioIni, int anioFin, int ini, int fin, int mods){

        return new DatosUsoMaquinaLT(new GregorianCalendar(anioIni, ini,1), new GregorianCalendar(anioFin, fin, 1), mods, new Random().nextInt(150));
    }
}

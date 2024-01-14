/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LineaDeTiempoRenderer is part of MOP.
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import presentacion.PresentacionHandler;

import java.util.*;

public class LineaDeTiempoRenderer {

    public static final double ESCALA_1_ANIO  = 116.5;
    public static final double ESCALA_5_ANIO  = 23.3;
    public static final double ESCALA_20_ANIO = 5.825;

    @FXML private GridPane gridPane;
    @FXML private GridPane gridPaneNombres;
    @FXML private GridPane gridPaneFechas;
    @FXML private ScrollPane scrollPaneContent;
    @FXML private ScrollPane scrollPaneNombres;
    @FXML private ScrollPane scrollPaneFechas;
    @FXML private JFXSlider sliderEscala;

    private DatosCorrida datosCorrida;
    private PresentacionHandler ph = PresentacionHandler.getInstance();

    private ArrayList<DatosMaquinaLT> listaDatosMaquinaLT = new ArrayList<>();
    private HashMap<String, Integer> posParticipante = new HashMap<>();

    public LineaDeTiempoRenderer(DatosCorrida datosCorrida){
        this.datosCorrida = datosCorrida;
    }

    @FXML
    public void initialize() {
//        Manejador.ph.cargarCorrida("C:\\Users\\D255728\\Downloads\\Prueba_postizacion_interna_CORTINA.xml", false);
//        listaDatosMaquinaLT = ph.dameDatosGraficoLineaTiempo(datosCorrida);

        gridPane.getRowConstraints().clear();

        sliderEscala.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!oldValue.equals(newValue)) {
//                double escala = sliderEscala.getValue() == 1 ? ESCALA_1_ANIO : sliderEscala.getValue() == 2 ? ESCALA_5_ANIO : sliderEscala.getValue() == 3 ? ESCALA_20_ANIO : -1;
                double escala;
                if((double)newValue <= 2){
                    escala = -93.2*(double)newValue+209.7;
                }else{
                    escala = -17.475*(double)newValue+58.25;
                }
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


////        for (String participante : datosMostrarLT.getParticipantesAMostrar()) {
//        RowConstraints rowConstraints = new RowConstraints();
//        rowConstraints.setMinHeight(50);
//        for (int i=1;i<15;i++) {
////            if(!posParticipante.containsKey(participante)){
////                posParticipante.put(participante, posParticipante.size()+1);
////            }
//            posParticipante.put("CTR"+i, posParticipante.size()+1);
//            JFXButton reprParticipante = new JFXButton();
////            reprParticipante.setText(participante);
//            reprParticipante.setText("CTR"+i);
//            reprParticipante.setPrefHeight(38);
//            reprParticipante.setPrefWidth(1538);
//            reprParticipante.setStyle("-fx-background-color: #27468d; -fx-font-weight: bold; -fx-font-size: 15px;");
//            reprParticipante.setTextFill(Paint.valueOf("ffffff"));
//            int start1 = new Random().nextInt(3)+1;
//            int span1 = new Random().nextInt(6)+1;
//            gridPane.add(reprParticipante, start1, posParticipante.size(), span1, 1);//TODO: de-randomize
//            if(i % 3 == 0 || i % 5 == 0){
//                JFXButton reprParticipante2 = new JFXButton();
////            reprParticipante2.setText(participante);
//                reprParticipante2.setText("CTR"+i);
//                reprParticipante2.setPrefHeight(38);
//                reprParticipante2.setPrefWidth(1538);
//                reprParticipante2.setStyle("-fx-background-color: #27468d; -fx-font-weight: bold; -fx-font-size: 15px;");
//                reprParticipante2.setTextFill(Paint.valueOf("ffffff"));
//                gridPane.add(reprParticipante2, start1+span1+new Random().nextInt(3)+1, posParticipante.size(), new Random().nextInt(4)+1, 1);//TODO: de-randomize
//            }
//            gridPane.getRowConstraints().add(rowConstraints);
//        }
//        gridPane.add(new Label(),1, posParticipante.size()+1);
//        gridPane.getRowConstraints().add(rowConstraints);
//        gridPane.getRowConstraints().add(rowConstraints);
//        for(Node node : gridPane.getChildren()){
//            if(node instanceof Line){
//                GridPane.setRowIndex(node, posParticipante.size()+3);
//            }
//            if(node instanceof Label){
//                GridPane.setRowIndex(node, posParticipante.size()+4);
//            }
//        }

        // datos pruebas
//        listaDatosMaquinaLT = dummyData();
//        render(ESCALA_20_ANIO);
    }

    public void popularListaDatosMaquinaLT(DatosCorrida datosCorrida){
        listaDatosMaquinaLT = ph.dameDatosGraficoLineaTiempo(datosCorrida);
    }

    public void render(Double escala){
        gridPane.getRowConstraints().clear();
        gridPane.getColumnConstraints().clear();
        int anioIni = 10000;
        int anioFin = 0;

        int minAlpha = 50;
        int maxAlpha = 150;

        for (DatosMaquinaLT datosMaquinaLT : listaDatosMaquinaLT){
            for(DatosUsoMaquinaLT datosUsoMaquinaLT : datosMaquinaLT.getUsos()){
                if(datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR) < anioIni){
                    anioIni = datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR);
                }
                if(datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR) > anioFin){
                    anioFin = datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR);
                }
                if(datosUsoMaquinaLT.getPotInst() > maxAlpha){
                    maxAlpha = (int) Math.round(datosUsoMaquinaLT.getPotInst());
                }
                if(datosUsoMaquinaLT.getPotInst() < minAlpha){
                    minAlpha = (int) Math.round(datosUsoMaquinaLT.getPotInst());
                }
            }
        }
        minAlpha = (maxAlpha - minAlpha) / 2; // TODO: 05/08/2020 mejor fórmula

        gridPane.getColumnConstraints().add(new ColumnConstraints(80,80,80));
        gridPaneFechas.getColumnConstraints().add(new ColumnConstraints(80,80,80));
        for(int i=anioIni;i<=anioFin;i++){
//            gridPane.addColumn(i-minIni+1);
            Label labelAnio = new Label(String.valueOf(i));
            labelAnio.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
            gridPaneFechas.add(labelAnio,12*(i-anioIni)+1+1,0, 12,1);
            for(int j=1;j<=12;j++) {
//                gridPane.add(new Label(String.valueOf(j)),12*(i-anioIni)+j,6);
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

//                reprParticipante.setPrefWidth(1538);
                reprParticipante.setPrefWidth(50000);
                String color = Text.COLORES.get(datosMaquinaLT.getTipo());

//                String alpha = "ff";
//                String alpha = Text.ALPHA.get(datosUsoMaquinaLT.getCantModInst());

                String alpha = Integer.toHexString((int)Math.round(datosUsoMaquinaLT.getPotInst()*50/maxAlpha + 50)*255/100);
//                String alpha = Integer.toHexString((int)Math.round(50.0/(maxAlpha-minAlpha)*datosUsoMaquinaLT.getPotInst()+50-minAlpha*(50.0/(maxAlpha-minAlpha)))*255/100);
//                System.out.println("alpha->"+datosUsoMaquinaLT.getPotInst()+"->"+alpha+"->"+(int)Math.round(datosUsoMaquinaLT.getPotInst()*50/150 + 50));
                reprParticipante.setStyle("-fx-background-color: " + color + alpha + "; -fx-font-weight: bold; -fx-font-size: 15px;");
                reprParticipante.setTextFill(Paint.valueOf("ffffff"));
//                int start = datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH)+1;
                int start = 12*(datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR) - anioIni) + datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH) + 1;
//                int span = datosUsoMaquinaLT.getFechaFin().get(Calendar.MONTH)+1 - start;
                int diffAnios = (datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR) - datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR));
                int span = diffAnios == 0 ?
                        (datosUsoMaquinaLT.getFechaFin().get(Calendar.MONTH) - datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH) + 1) :
                        (12 - datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH)) + 12*(diffAnios - 1) + (datosUsoMaquinaLT.getFechaFin().get(Calendar.MONTH) + 1);

//                System.out.println(datosUsoMaquinaLT.getFechaIni().get(Calendar.MONTH));
//                System.out.println(datosUsoMaquinaLT.getFechaIni().get(Calendar.YEAR));
//                System.out.println(datosUsoMaquinaLT.getFechaFin().get(Calendar.MONTH));
//                System.out.println(datosUsoMaquinaLT.getFechaFin().get(Calendar.YEAR));
                gridPane.add(reprParticipante, start, posParticipante.size()-1, span, 1);

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

                //Card
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("LineaDeEntradaCard.fxml"));
                    loader.setController(new LineaDeEntradaCardController(datosMaquinaLT, datosUsoMaquinaLT));
                    AnchorPane newLoadedPane = loader.load();
                    PopOver popOver = new PopOver(newLoadedPane);
                    popOver.setTitle("EV");

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
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(50);
//        gridPane.getRowConstraints().add(rowConstraints);
//        gridPane.getRowConstraints().add(rowConstraints);
//        gridPane.getRowConstraints().add(rowConstraints);
//        gridPane.getRowConstraints().add(rowConstraints);
//        gridPaneNombres.getRowConstraints().add(rowConstraints);
//        gridPaneNombres.getRowConstraints().add(rowConstraints);
//        gridPaneNombres.getRowConstraints().add(rowConstraints);
//        gridPaneNombres.getRowConstraints().add(rowConstraints);
//        for (Node node : gridPane.getChildren()) {
////            if (node instanceof Line) {
////                GridPane.setRowIndex(node, posParticipante.size() + 3);
////            }
//            if (node instanceof Label) {
//                GridPane.setRowIndex(node, posParticipante.size() + 3);
//            }
//        }
    }

    private void update(Double escala){
//        System.out.println("update->"+escala);
        for(int i=1;i<gridPane.getColumnConstraints().size();i++){
            gridPane.getColumnConstraints().get(i).setMinWidth(escala);
            gridPane.getColumnConstraints().get(i).setMaxWidth(escala);
        }
    }

    public void uncapHeight(){
        scrollPaneContent.setMaxHeight(730);
        scrollPaneNombres.setMaxHeight(730);
    }

    public void capHeight(){
        scrollPaneContent.setMaxHeight(290);
        scrollPaneNombres.setMaxHeight(290);
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
//        return new DatosUsoMaquinaLT(new GregorianCalendar(2018, ini,1), new GregorianCalendar(2018, fin, 1), mods, new Random().nextInt(150));
        return new DatosUsoMaquinaLT(new GregorianCalendar(anioIni, ini,1), new GregorianCalendar(anioFin, fin, 1), mods, new Random().nextInt(150));
    }
}

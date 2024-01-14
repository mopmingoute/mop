/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ReportesContentController is part of MOP.
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

import com.jfoenix.controls.JFXComboBox;
import datatypes.DatosGraficaGUI;
import datatypes.DatosReporteGUI;
import datatypes.DatosResumenGUI;
import datatypes.Pair;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;


public class ReportesContentController extends GeneralidadesController{
    @FXML private AnchorPane content;
    @FXML private GridPane gridPane;
    @FXML private JFXComboBox<String> dataInput;
    @FXML private LineChart<String, Number> chart;
    @FXML private CategoryAxis xAxisId;
    @FXML private NumberAxis yAxisId;
    @FXML private StackedAreaChart<String, Number> stackedAreaChart;
    @FXML private CategoryAxis stackedAreaXAxisId;
    @FXML private NumberAxis stackedAreaYAxisId;

    private HashMap<String, XYChart.Series> data = new HashMap<>();

    public DatosReporteGUI datosReporteGUI;
    private Integer cantGraphs = 1;

    @FXML private Button updateTest;

    public ReportesContentController(){}

    public ReportesContentController(DatosReporteGUI datosReporteGUI){
        this.datosReporteGUI = datosReporteGUI;
    }

    @FXML
    public void initialize(){
//        final NumberAxis xAxis = new NumberAxis();
//        final NumberAxis yAxis = new NumberAxis();
//        final CategoryAxis xAxis = new CategoryAxis();
        datosReporteGUI = new DatosReporteGUI();//dummyData();
//
//        xAxisId.setLabel("Mes");
//        yAxisId.setLabel("Potencia");
//
//        stackedAreaXAxisId.setLabel("Mes");
//        stackedAreaYAxisId.setLabel("Potencia");
//
////        chart = new LineChart<>(xAxisId,yAxisId);
//
//        chart.setTitle("Potencia Mensual");
//
//        XYChart.Series<String, Number> seriesHidro = new XYChart.Series<>();
//        seriesHidro.getData().add(new XYChart.Data<>("Ene",150));
//        seriesHidro.getData().add(new XYChart.Data<>("Feb",100));
//        seriesHidro.getData().add(new XYChart.Data<>("Mar",200));
//        seriesHidro.getData().add(new XYChart.Data<>("Abr",50));
//        seriesHidro.getData().add(new XYChart.Data<>("May",50));
//        seriesHidro.getData().add(new XYChart.Data<>("Jun",50));
//        seriesHidro.getData().add(new XYChart.Data<>("Jul",150));
//        seriesHidro.getData().add(new XYChart.Data<>("Ago",225));
//        seriesHidro.getData().add(new XYChart.Data<>("Set",250));
//        seriesHidro.getData().add(new XYChart.Data<>("Oct",300));
//        seriesHidro.getData().add(new XYChart.Data<>("Nov",200));
//        seriesHidro.getData().add(new XYChart.Data<>("Dic",190));
//        seriesHidro.setName("Hidro");
//
//        XYChart.Series<String, Number> seriesTermico = new XYChart.Series<>();
//        seriesTermico.getData().add(new XYChart.Data<>("Ene",60));
//        seriesTermico.getData().add(new XYChart.Data<>("Feb",60));
//        seriesTermico.getData().add(new XYChart.Data<>("Mar",80));
//        seriesTermico.getData().add(new XYChart.Data<>("Abr",95));
//        seriesTermico.getData().add(new XYChart.Data<>("May",120));
//        seriesTermico.getData().add(new XYChart.Data<>("Jun",120));
//        seriesTermico.getData().add(new XYChart.Data<>("Jul",150));
//        seriesTermico.getData().add(new XYChart.Data<>("Ago",90));
//        seriesTermico.getData().add(new XYChart.Data<>("Set",75));
//        seriesTermico.getData().add(new XYChart.Data<>("Oct",100));
//        seriesTermico.getData().add(new XYChart.Data<>("Nov",110));
//        seriesTermico.getData().add(new XYChart.Data<>("Dic",90));
//        seriesTermico.setName("Termico");
//
//        data.put("Hidro", seriesHidro);
//        data.put("Termico", seriesTermico);
//
//        //        chart.getData().addAll(seriesHidro,seriesTermico);
//        chart.setData(FXCollections.observableArrayList(seriesHidro,seriesTermico));
//        chart.getXAxis().setAutoRanging(true);
//        chart.getYAxis().setAutoRanging(true);
//
//        System.out.println(chart.getData().get(1).getData());
//
//        stackedAreaChart.setTitle("Potencia Mensual");
//
//        XYChart.Series<String, Number> serieAreaHidro = new XYChart.Series<>();
//        serieAreaHidro.getData().add(new XYChart.Data<>("Ene",150));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Feb",100));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Mar",200));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Abr",50));
//        serieAreaHidro.getData().add(new XYChart.Data<>("May",50));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Jun",50));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Jul",150));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Ago",225));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Set",250));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Oct",300));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Nov",200));
//        serieAreaHidro.getData().add(new XYChart.Data<>("Dic",190));
//        serieAreaHidro.setName("Hidro");
//
//        XYChart.Series<String, Number> serieAreaTermico = new XYChart.Series<>();
//        serieAreaTermico.getData().add(new XYChart.Data<>("Ene",60));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Feb",60));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Mar",80));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Abr",95));
//        serieAreaTermico.getData().add(new XYChart.Data<>("May",120));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Jun",120));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Jul",150));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Ago",90));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Set",75));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Oct",100));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Nov",110));
//        serieAreaTermico.getData().add(new XYChart.Data<>("Dic",90));
//        serieAreaTermico.setName("Termico");
//
//        stackedAreaChart.getData().addAll(serieAreaHidro,serieAreaTermico);
//        stackedAreaYAxisId.setUpperBound(425);
////        stackerAreaChart.getXAxis().setAutoRanging(true);
////        stackerAreaChart.getYAxis().setAutoRanging(true);
//
//
//        dataInput.getItems().addAll("Todo","Hidro","Termico");
//        dataInput.getSelectionModel().selectFirst();
//        dataInput.setOnAction(event -> {
//            if(dataInput.getSelectionModel().getSelectedItem().equalsIgnoreCase("Hidro")){
//                chart.getData().remove(seriesTermico);
//                if(!chart.getData().contains(seriesHidro)) {
//                    chart.getData().add(seriesHidro);
//                }
//                // STACKED AREA CHART
//                stackedAreaChart.getData().remove(serieAreaTermico);
//                if(!stackedAreaChart.getData().contains(serieAreaHidro)) {
//                    stackedAreaChart.getData().add(serieAreaHidro);
//                }
//            }else if(dataInput.getSelectionModel().getSelectedItem().equalsIgnoreCase("Termico")){
//                chart.getData().remove(seriesHidro);
//                if(!chart.getData().contains(seriesTermico)) {
//                    chart.getData().add(seriesTermico);
//                }
//                // STACKED AREA CHART
//                stackedAreaChart.getData().remove(serieAreaHidro);
//                if(!stackedAreaChart.getData().contains(serieAreaTermico)) {
//                    stackedAreaChart.getData().add(serieAreaTermico);
//                }
//            }else{
////                chart.getData().removeAll(seriesHidro,seriesTermico);
////                chart.getData().addAll(seriesHidro,seriesTermico);
//                if(!chart.getData().contains(seriesHidro)) {
//                    chart.getData().add(seriesHidro);
//                }
//                if(!chart.getData().contains(seriesTermico)) {
//                    chart.getData().add(seriesTermico);
//                }
//                // STACKED AREA CHART
////                stackerAreaChart.getData().remove(serieAreaHidro);
////                stackerAreaChart.getData().remove(serieAreaTermico);
////                stackedAreaChart.setData(null);
////                stackedAreaChart.setData(FXCollections.observableArrayList(serieAreaHidro, serieAreaTermico));
////                stackedAreaChart.getData().add(serieAreaHidro);
////                stackedAreaChart.getData().add(serieAreaTermico);
//                if(!stackedAreaChart.getData().contains(serieAreaHidro)) {
//                    stackedAreaChart.getData().add(0, serieAreaHidro);
//                }
//                if(!stackedAreaChart.getData().contains(serieAreaTermico)) {
//                    stackedAreaChart.getData().add(1, serieAreaTermico);
//                }
//            }
//        });

        // DYNAMIC CHART TEST
//        generar(seriesHidro.getData(), seriesTermico.getData());
        gen();

        updateTest.setOnAction(action -> {
//            actualizar(dummyData());
//            PrinterJob job = PrinterJob.createPrinterJob();
//            if(job != null){
//                job.showPrintDialog(content.getScene().getWindow());
////                job.printPage(content);
//                job.printPage(gridPane);
//                job.endJob();
//            }




//            System.out.println("W: "+(int)content.getWidth());
//            System.out.println("H: "+(int)content.getBoundsInLocal().getHeight());
//            for(Node child : content.getChildren()){
//                System.out.println("H1: "+child.getLayoutBounds().getHeight());
//            }
//            System.out.println("Hg1: "+gridPane.getHeight());
//            System.out.println("Hg2: "+gridPane.getLayoutBounds().getHeight());
            WritableImage img = new WritableImage((int)content.getWidth(),(int)gridPane.getHeight());
//            img = content.snapshot(new SnapshotParameters(),img);
            img = gridPane.snapshot(new SnapshotParameters(),img);
            try {
                LocalDateTime now = LocalDateTime.now();
                String dirReporte = "XML/reporte_"+ now.getYear() + "_"+now.getMonthValue() +"_"+ now.getDayOfMonth() + "_"+now.getNano() + ".png";
                ImageIO.write(SwingFXUtils.fromFXImage(img, null),"png",new File(dirReporte));
                setLabelMessageTemporal(Text.MSG_CONF_REPORTES_PDF, TipoInfo.FEEDBACK);

            } catch (IOException e) {
                e.printStackTrace();
                setLabelMessageTemporal(Text.MSG_ERR_REPORTES_PDF, TipoInfo.ERROR);
            }
        });
    }

    private void gen() {
//        for(DatosGraficaGUI datosGraficaGUI : datosGraficasGUI){
        for(int i=0; i<datosReporteGUI.getGraficas().size()+datosReporteGUI.getResumenes().size(); i++){

            //grafica
            if(datosReporteGUI.getGraficas().containsKey(i)){
                DatosGraficaGUI datosGraficaGUI = datosReporteGUI.getGraficas().get(i);
                // TODO: 14/07/2020 categoric ?   // cuando no hay datos numericos sino datos con categorias
                NumberAxis xAxis = new NumberAxis();
                NumberAxis yAxis = new NumberAxis();
                XYChart dynamicChart;
                xAxis.setLabel(datosGraficaGUI.getTituloX() + " (" + datosGraficaGUI.getUnidadX() + ")");
                yAxis.setLabel(datosGraficaGUI.getTituloY() + " (" + datosGraficaGUI.getUnidadY() + ")");
                xAxis.setAutoRanging(false);  // esta el false no calcula automatico
                Double lowerBound = null;
                Double upperBound = null;
                for(ArrayList<Pair<Integer, Double>> serie : datosGraficaGUI.getSeries().values()){   //Se calcula el valor de los ejes manualmente
                    for(Pair<Integer, Double> dato : serie){
                        if(lowerBound == null || dato.first < lowerBound){
                            lowerBound = Double.valueOf(dato.first);
                        }
                        if(upperBound == null || dato.first > upperBound){
                            upperBound = Double.valueOf(dato.first);
                        }
                    }
                }
                xAxis.setLowerBound(--lowerBound);
                xAxis.setUpperBound(++upperBound);
                xAxis.setTickUnit(1);
                xAxis.setTickLength(1);
                xAxis.setMinorTickCount(1);
                xAxis.setMinorTickLength(1);
                xAxis.setTickLabelFormatter(new StringConverter<>() {
                    @Override
                    public String toString(Number object) {
                        return new DecimalFormat("#").format(object);
                    }

                    @Override
                    public Number fromString(String string) {
                        return null;
                    }
                });
                if(datosGraficaGUI.getTipo() == DatosGraficaGUI.GRAF_LINEAS){
                    dynamicChart = new LineChart<>(xAxis, yAxis);
                }else{
//                    dynamicChart = new StackedAreaChart<>(new NumberAxis(), new NumberAxis());
                    dynamicChart = new StackedAreaChart<>(xAxis, yAxis);
                }
                dynamicChart.setTitle(datosGraficaGUI.getTitulo());
//                dynamicChart.getXAxis().setAutoRanging(true);

                double chartHeight = 900;

                dynamicChart.getYAxis().setAutoRanging(true);
                dynamicChart.setMinHeight(chartHeight);
                for(String nomSerie : datosGraficaGUI.getSeries().keySet()){
                    XYChart.Series<Number,Number> serie = new XYChart.Series<>();
                    serie.setName(nomSerie);
                    for(Pair<Integer,Double> dataPoint : datosGraficaGUI.getSeries().get(nomSerie)){
                        serie.getData().add(new XYChart.Data<>(dataPoint.first, dataPoint.second));
                    }
                    dynamicChart.getData().add(serie);
                }

                gridPane.getRowConstraints().add(new RowConstraints(chartHeight,chartHeight,chartHeight));
                gridPane.add(dynamicChart, 0, cantGraphs++);



                // TEST_LINE
//                XYChart test = new LineChart<>(xAxis, yAxis);
//                XYChart.Series<Number,Number> testDatosDemanda = new XYChart.Series<>();
//                for(int j=2020;j<=2023;j++){
//                    testDatosDemanda.getData().add(new XYChart.Data<>(i,4000+((i-2020)*750)));
//                }
//                test.getData().add(testDatosDemanda);
//                gridPane.add(test, 0, cantGraphs-1);
                // TEST_LINE
            }

            //resumen
            if(datosReporteGUI.getResumenes().containsKey(i)){
                DatosResumenGUI datosResumenGUI = datosReporteGUI.getResumenes().get(i);
                TableView<FilaResumen> dynamicResumen = new TableView<>();
                TableColumn<FilaResumen,String> primeraColumna = new TableColumn<>("Participante");
                primeraColumna.setCellValueFactory(fila -> new ReadOnlyObjectWrapper(fila.getValue().getTituloFila()));
                primeraColumna.setPrefWidth(120);
                dynamicResumen.getColumns().add(primeraColumna);
                ArrayList<String> cols = new ArrayList<String>(datosResumenGUI.getValores().get(datosResumenGUI.getValores().keySet().toArray()[0]).keySet());
                Collections.sort(cols);
                for(String nombreColumna : cols){
                    TableColumn<FilaResumen,String> col = new TableColumn<>(nombreColumna);
                    col.setCellValueFactory(fila -> new ReadOnlyObjectWrapper(fila.getValue().getValores().get(nombreColumna)));
                    col.setPrefWidth(120);
                    dynamicResumen.getColumns().add(col);
                }

                for(String nombreFila : datosResumenGUI.getValores().keySet()){
                    dynamicResumen.getItems().add(new FilaResumen(nombreFila, datosResumenGUI.getValores().get(nombreFila)));
                }

                gridPane.getRowConstraints().add(new RowConstraints(614,614,614));
                gridPane.add(dynamicResumen, 0, cantGraphs++);
            }

        }
    }

    private void clean(){
        gridPane.getRowConstraints().remove(1, gridPane.getRowConstraints().size());
        gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
        cantGraphs = 1;
    }

//    public void actualizar(ArrayList<DatosGraficaGUI> newDatosGraficasGUI){
    public void actualizar(DatosReporteGUI datosReporteGUI){
        this.datosReporteGUI = datosReporteGUI;
        clean();
        gen();
    }

    public static DatosReporteGUI dummyData(){
        HashMap<Integer,DatosGraficaGUI> dgg = new HashMap<>();
//        dgg.put(1,dummyDGG(1, 0, 3, 90.0, 220.0));
//        dgg.put(3,dummyDGG(2, 0, 5, 55.0, 255.0));
//        dgg.put(5,dummyDGG(3, 1, 3, 60.0, 95.0));

        HashMap<Integer,DatosResumenGUI> drg = new HashMap<>();
//        drg.put(2,dummyDRG(1, 5, 5));
//        drg.put(4,dummyDRG(2, 20, 25));
        return new DatosReporteGUI("Reporte", null, drg, dgg);
		
    }

    private static DatosGraficaGUI dummyDGG(Integer nombreNum, Integer tipo, Integer cantSeries, Double lowerRange, Double upperRange){
        Hashtable<String, ArrayList<Pair<Integer, Double>>> series = new Hashtable<>();
        for(int i=1;i<=cantSeries;i++){
            ArrayList<Pair<Integer, Double>> dataSerie = new ArrayList<>();
            for(int j=1;j<=12;j++) {
                dataSerie.add(new Pair<>(j, new Random().nextDouble() * upperRange + lowerRange));
            }
            series.put("Serie " + i, dataSerie);
        }
        return new DatosGraficaGUI("Titulo "+nombreNum, "unidad X "+nombreNum, "Eje X "+nombreNum, "unidad Y "+nombreNum, "Eje Y "+nombreNum , series, tipo);
    }

    private static DatosResumenGUI dummyDRG(Integer nombreNum, Integer numFilas,  Integer numColumnas){
        HashMap<String, HashMap<String,Double>> valores = new HashMap<>();
        String titulo = "Titulo " + nombreNum;
        for(int i=1;i<=numFilas;i++){
            String tituloFila = "Fila " + i;
            HashMap<String,Double> filaVals = new HashMap<>();
            for(int j=1;j<=numColumnas;j++){
                String tituloColuma = "Columna " + j;
                filaVals.put(tituloColuma, new Random().nextDouble());
            }
            valores.put(tituloFila, filaVals);
        }
        return new DatosResumenGUI(titulo, valores);
    }

    @Override
    public void confirmoMensaje(boolean confirmacion, int codigoMensaje) {

    }


    private class FilaResumen{ 

        private String tituloFila; //otro val en el hash?
        private HashMap<String,Double> valores;

        private FilaResumen(String tituloFila, HashMap<String,Double> valores){
            this.tituloFila = tituloFila;
            this.valores = valores;
        }

        public String getTituloFila() {
            return tituloFila;
        }

        public void setTituloFila(String tituloFila) {
            this.tituloFila = tituloFila;
        }

        public HashMap<String, Double> getValores() {
            return valores;
        }

        public void setValores(HashMap<String, Double> valores) {
            this.valores = valores;
        }
    }
}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorDatosLineaDeTiempo is part of MOP.
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

import datatypesTiempo.DatosLineaTiempo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneradorDatosLineaDeTiempo {

    private static HashMap<Integer,DetalleLTData> detalleLTDataAnios = new HashMap<>();

    private static void generarLTData(int anioIni, int anioFin, String paso) {
        int cantAnios = anioFin - anioIni + 1;
        for(int i=0;i<cantAnios;i++){
            int anioInt = anioIni+i;

            if(!detalleLTDataAnios.containsKey(anioInt)){
                DetalleLTData detalleAnio = new DetalleLTData();
                detalleAnio.setPaso(paso);
                for(int j=0;j<12;j++){
                    DetalleLTData detalleMes = new DetalleLTData();
                    detalleMes.setPaso(paso);
                    detalleAnio.getSubDetalleMap().put(j+1, detalleMes);
                }
                detalleLTDataAnios.put(anioInt, detalleAnio);
            }
        }
    }

    private static String generarCadenaBloques(int anioFin){
        String bloqueActual = null;
        Integer anioIni = -1;
        int nuevoBloqueIni = -1;
        int bloqueIni = -1;
        int bloqueFin = -1;
        int mesesBloqueActual = 0;
        StringBuilder cadenaBloques = new StringBuilder();
        Map<Integer,DetalleLTData> sortedMap = new TreeMap<>(detalleLTDataAnios);

//        for(Integer anio : detalleLTDataAnios.keySet()){
        for(Integer anio : sortedMap.keySet()){
//            System.out.println("---ANIO_KEY---"+anio);
            for(Integer mes : detalleLTDataAnios.get(anio).getSubDetalleMap().keySet()){
                if(bloqueActual == null || bloqueActual.equalsIgnoreCase(detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso())){
                    mesesBloqueActual++;
                    if(bloqueActual == null){
                        bloqueIni = 1;
                        anioIni = anio;
                    }else if(bloqueActual.equalsIgnoreCase(utilitarios.Constantes.PASO_SEMANAL) && mes == 12){
                        bloqueFin = LocalDate.of(anio,1,1).lengthOfYear();
                        nuevoBloqueIni = 1;
                        cadenaBloques.append(bloqueActual).append(":").append(mesesBloqueActual)
                                .append("(").append(bloqueIni).append("[").append(anio).append("]").append("-")
                                .append(bloqueFin).append("[").append(anio).append("]").append(anio == anioFin ? ")" : "),");
//                                .append(bloqueFin).append("[").append(anio).append("]").append(anio == inputFechaFin.getValue().getYear() ? ")" : "),");
                        bloqueIni = nuevoBloqueIni;
                        mesesBloqueActual = 0;
                    }

                    bloqueActual = detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso();
                }else{
                    if(detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso().equalsIgnoreCase(utilitarios.Constantes.PASO_SEMANAL)) {
                        LocalDate f = LocalDate.of(anio, mes, 1);
                        bloqueFin = ((f.getDayOfYear()-1) / 7) * 7 + 7 * ((f.getDayOfYear()-1) % 7 > 0 ? 1 : 0);
                        nuevoBloqueIni = bloqueFin + 1;
                    }else if(bloqueActual.equalsIgnoreCase(utilitarios.Constantes.PASO_SEMANAL)){
                        if(mes == 1){
                            bloqueFin = LocalDate.of(anio-1,1,1).lengthOfYear();
                            nuevoBloqueIni = 1;
                        }else {
                            LocalDate f = LocalDate.of(anio, mes, 1);
                            nuevoBloqueIni = (f.getDayOfYear() / 7)*7 + 1;
                            bloqueFin = nuevoBloqueIni == 1 ? LocalDate.of(anio-1,1,1).lengthOfYear() : nuevoBloqueIni - 1;
                        }
                    }else{
                        LocalDate f = LocalDate.of(anio, mes, 1);
                        nuevoBloqueIni = f.getDayOfYear();
                        bloqueFin = nuevoBloqueIni == 1 ? LocalDate.of(anio-1,1,1).lengthOfYear() : nuevoBloqueIni - 1;
                    }

                    if(mesesBloqueActual > 0) {
                        cadenaBloques.append(bloqueActual).append(":").append(mesesBloqueActual)
                                .append("(").append(bloqueIni).append("[").append(anioIni).append("]").append("-")
                                .append(bloqueFin).append("[").append(bloqueFin == LocalDate.of(anio - 1, 1, 1).lengthOfYear() ? anio - 1 : anio)
                                .append("]").append("),");
                    }

                    bloqueActual = detalleLTDataAnios.get(anio).getSubDetalleMap().get(mes).getPaso();
                    mesesBloqueActual = 1;

                    bloqueIni = nuevoBloqueIni;
                    anioIni = anio;
                }
            }
        }
        if(mesesBloqueActual > 0){
//            bloqueFin = inputFechaFin.getValue().lengthOfYear();
            bloqueFin = LocalDate.of(anioFin, 1, 1).lengthOfYear();
            cadenaBloques.append(bloqueActual).append(":").append(mesesBloqueActual)
                    .append("(").append(bloqueIni).append("[").append(anioIni).append("]")
                    .append("-").append(bloqueFin).append("[").append(anioFin).append("]").append(")");
//                    .append("-").append(bloqueFin).append("[").append(inputFechaFin.getValue().getYear()).append("]").append(")");
        }

        return cadenaBloques.toString();
    }

    public static DatosLineaTiempo parsearCadenaBloques(int anioInicial, int anioFinal, String paso){
        DatosLineaTiempo datosLineaTiempo = new DatosLineaTiempo();

        datosLineaTiempo.setTiempoInicial(LocalDate.of(anioInicial, 1, 1).format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        datosLineaTiempo.setTiempoInicialEvoluciones(LocalDate.of(anioInicial, 1, 1).format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 00:00:00");
        datosLineaTiempo.setTiempoFinal(LocalDate.of(anioFinal, 12, 31).format(DateTimeFormatter.ofPattern("dd MM yyyy")) + " 23:59:59");

        generarLTData(anioInicial, anioFinal, paso);

        String cadenaBloques = generarCadenaBloques(anioFinal);

        System.out.println(cadenaBloques);
        String[] segmentos = cadenaBloques.split(",");

        for(String segmento : segmentos) {
            Pattern pattern = Pattern.compile("(\\d+)\\[(\\d+)\\]-(\\d+)\\[(\\d+)\\]");
            Matcher matcher = pattern.matcher(segmento);
            matcher.find();
            Integer bloqueIni = Integer.valueOf(matcher.group(1));
            Integer anioIni = Integer.valueOf(matcher.group(2));
            Integer bloqueFin = Integer.valueOf(matcher.group(3));
            Integer anioFin = Integer.valueOf(matcher.group(4));

            if(segmento.contains(utilitarios.Constantes.PASO_HORARIO)){
                // add bloque horario
                int cantPasos;
                if(anioIni.equals(anioFin)){
                    cantPasos = bloqueFin - bloqueIni + 1;
                    cantPasos *= 24;
                }else{
                    cantPasos = 365 - bloqueIni + 1;
                    cantPasos += LocalDate.of(anioIni,1,1).isLeapYear() ? 1 : 0;
                    for(int i=anioIni+1;i<=anioFin-1;i++){
                        cantPasos += LocalDate.of(i,1,1).isLeapYear() ? 366 : 365;
                    }
                    cantPasos *= 24;
                }
                datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(3600), String.valueOf(3600), String.valueOf(1),
                        new ArrayList<>(Arrays.asList(3600)),
                        String.valueOf(0), String.valueOf(true));
            }else if(segmento.contains(utilitarios.Constantes.PASO_DIARIO)){
                // add bloque diario 24p - 1hr -cron T
                int cantPasos;
                if(anioIni.equals(anioFin)){
                    cantPasos = bloqueFin - bloqueIni + 1;
                }else{
                    cantPasos = 365 - bloqueIni + 1;
                    cantPasos += LocalDate.of(anioIni,1,1).isLeapYear() ? 1 : 0;
                    for(int i=anioIni+1;i<=anioFin-1;i++){
                        cantPasos += LocalDate.of(i,1,1).isLeapYear() ? 366 : 365;
                    }
                }
                datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(3600*24), String.valueOf(3600), String.valueOf(24),
                        new ArrayList<>(Arrays.asList(3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600,3600)),
                        String.valueOf(0), String.valueOf(true));
            }else if(segmento.contains(utilitarios.Constantes.PASO_SEMANAL)){
                // add bloque(s) semanal(es) 4p -cron F
                int cantPasos;
                cantPasos = (bloqueFin - bloqueIni + 1) / 7;
                cantPasos -= (bloqueFin - bloqueIni + 1) % 7 == 0 ? 0 : 1;
                datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(604800), String.valueOf(3600), String.valueOf(4),
                        new ArrayList<>(Arrays.asList(18000,108000,327600,151200)),
                        String.valueOf(0), String.valueOf(false));
                if((bloqueFin - bloqueIni + 1) % 7 == 1){
//                    System.out.println("anio_8=>"+anioIni);
                    cantPasos = 1;
                    // semana larga - no bisiesto
                    datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(691200), String.valueOf(3600), String.valueOf(4),
                            new ArrayList<>(Arrays.asList(21600,122400,374400,172800)),
                            String.valueOf(0), String.valueOf(false));
                }else if((bloqueFin - bloqueIni + 1) % 7 == 2) {
//                    System.out.println("anio_9=>"+anioIni);
                    cantPasos = 1;
                    // semana larga - bisiesto
                    datosLineaTiempo.agregarBloque(String.valueOf(cantPasos), String.valueOf(777600), String.valueOf(3600), String.valueOf(4),
                            new ArrayList<>(Arrays.asList(21600,140400,421200,194400)),
                            String.valueOf(0), String.valueOf(false));
                }


            }
        }

//        datosLineaTiempo.print();

        return datosLineaTiempo;
    }

    private static class DetalleLTData{
        private String paso;
        private HashMap<Integer, DetalleLTData> subDetalleMap = new HashMap<>();

        private DetalleLTData(){}

        public String getPaso() {
            return paso;
        }

        public void setPaso(String paso) {
            this.paso = paso;
        }

        public HashMap<Integer, DetalleLTData> getSubDetalleMap() {
            return subDetalleMap;
        }

        public void setSubDetalleMap(HashMap<Integer, DetalleLTData> subDetalleMap) {
            this.subDetalleMap = subDetalleMap;
        }

    }
}

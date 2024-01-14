/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * LectorArchivosSalidaSimulDiaria is part of MOP.
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

package persistencia;

import java.util.ArrayList;

import demandaSectores.DatosGenSectores;
import logica.CorridaHandler;
import pizarron.PizarronRedis;
import tiempo.LineaTiempo;
import utilitarios.AsistenteLectorEscritorTextos;

public class LectorArchivosSalidaSimulDiaria {
	/**
	 * Contiene métodos para leer las salidas de texto de la simulación de un recurso
	 * para las corridas de paso diario.
	 * Para cada día se leen cantIntMuestreo valores promedio y por crónica.
	 */
	private String dirArchivo;
	private int cantIntMuestreo;
	private int cantCron;
	private String[] fechas;
	private double[][] promedios;  // primer índice paso, segundo índice intervalo de muestreo
	private double[][][] valores;  // primer índice paso, segundo índice escenario(crónica), tercer índice int. de muestreo

	/**
	 * Valores por crónica y por intervalo de muestreo (hora) para un año elegido
	 *  primer índice crónica, el índice 0 es el promedio en las crónicas
	 *  segundo índice hora del año
	 */
	private double[][] valCronHora;  
	
	public LectorArchivosSalidaSimulDiaria(String dirArchivo, int cantIntMuestreo, int cantCron) {
		this.cantIntMuestreo = cantIntMuestreo;
		this.cantCron = cantCron;
		this.dirArchivo = dirArchivo;
	}
	
	
	/**
	 * Lee las potencias por día y por crónica de dirArchivo con formato de salida por paso y por crónica
	 * @param dirArchivo
	 * @param cantIntMuestreo
	 */
	public void leePotenciasPorDiaYCronica(){
		ArrayList<ArrayList<String>> texto = utilitarios.LeerDatosArchivo.getDatos(dirArchivo);
		AsistenteLectorEscritorTextos ale = new AsistenteLectorEscritorTextos(texto, dirArchivo);
		int i=2;
		int cantPasos = texto.size()-2; 
		promedios = new double[cantPasos][cantIntMuestreo];
		valores = new double[cantPasos][cantCron][cantIntMuestreo];
		fechas = new String[cantPasos];
		while(i<cantPasos+2) {
			int im;
			for(im=0; im<cantIntMuestreo; im++) {
				fechas[i-2]= texto.get(i).get(1);
				promedios[i-2][im] = Double.parseDouble(texto.get(i).get(im+2));
			}
			int icol = cantIntMuestreo + 2;
			for(int icron=0; icron<cantCron; icron++) {
				for(im=0; im<cantIntMuestreo; im++) {
					valores[i-2][icron][im] = Double.parseDouble(texto.get(i).get(icol));
//					System.out.println("icron " + icron + " im " + im + " icol "+ icol + " valores[i-2][icron][im] " + valores[i-2][icron][im]);
					icol++;
				}
			}			
			i++;
		}	
	}
	
	/**
	 * Calcula valores por crónica e intervalo de muestreo (hora) del recurso leído en un año elegido
	 * 
	 * @param anio
	 * @return double[][] result primer índice promedio o crónica, segundo índice hora del año 
	 * 
	 */
	public double[][] calculaValCronHora(int anio) {
		int cantDiasAnio = utilitarios.Constantes.CANT_DIAS_ANIO_NOBISIESTO;
		if(LineaTiempo.bisiesto(anio)) {
			cantDiasAnio ++;
		}
		double[][] result = new double[cantCron+1][cantDiasAnio*cantIntMuestreo];
		int cantDatos = this.fechas.length;
		int diaIni=0;
		while(LineaTiempo.anioDeFecha(this.fechas[diaIni])<anio) {
			diaIni++;	
			if(diaIni==cantDatos) {
				System.out.println("Error en lectura de datos diarios archivo " + dirArchivo + " No se encontró el año " + anio);
				if (CorridaHandler.getInstance().isParalelo()){
					//PizarronRedis pp = new PizarronRedis();
				//	pp.matarServidores();
				}
				System.exit(1);
			}
		}
		int indHora = 0;
		for(int id=0; id<cantDiasAnio; id++) {
			for(int im=0; im<cantIntMuestreo; im++) {
				result[0][indHora]=promedios[diaIni+id][im];
				for(int ic=1; ic<=cantCron; ic++) {
					result[ic][indHora]=valores[diaIni+id][ic-1][im];
				}
				indHora++;
			}
		}
		return result;
	}
	
	
	public static void main(String[] args) {

		
		String dirArchivo = "T:\\ArmarioGEN\\2021\\24_Pruebas_SimSEE_vs_MOP\\00_corrida_horaria\\ResultadosCorridaMopRealidadSimsee21-26hasta27guardia\\2021-9-13-21-19-19-SIM-PAR\\EOLO_MDP\\potencias.xlt";
		int cantCron = 112;
		int cantIm = 24;
		LectorArchivosSalidaSimulDiaria lec = new LectorArchivosSalidaSimulDiaria(dirArchivo, cantIm, cantCron);
		lec.leePotenciasPorDiaYCronica();
		System.out.println("Terminó lectura");
	}
}

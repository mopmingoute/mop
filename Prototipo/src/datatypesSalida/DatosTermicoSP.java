/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosTermicoSP is part of MOP.
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

package datatypesSalida;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Datos de resultados de un paso para un generador tórmico
 * 
 * @author ut469262
 *
 */

public class DatosTermicoSP {
	private String nombre;
	private String nombreBarra;
	private double potMax; // potencia móxima por módulo
	private double potMin; // potencia mónima por módulo
	private int cantModIni; // cantidad de módulos en paralelo inicialmente, sólo aplica si hay variable de
							// estado
	private int cantModDisp; // cantidad de módulos disponibles en el paso
	private int[] cantModDesp; // cantidad de módulos despachados en el paso
	
	private int cantFuncUltimoIMpaso; // cant de módulos en funcionamiento en el último intervalo de muestreo del paso
	private double[] potencias; // potencia despachada por poste en MW
	private double rendMax; // rendimiento en el móximo
	private double rendMin; // rendimiento en el mónimo tócnico
	private ArrayList<String> listaCombustibles; // combustibles en el orden
	private double costoVarMintec; // costo variable medio del mintec USD/MWh
	private double[] costoVarPropC; // costo variable por encima del mintec USD/MWh para cada combustible de
									// listaCombustibles
	private double costoTotPaso; // costo total en el paso

	private double[] volC; // volumen de combustible usado en el paso para cada combustible en unidades de
							// combustible
	private double[] enerTC; // energía térmica usada en el paso para cada combustible (MWh)
	private double[][] enerTPC; // energía térmica para cada poste y combustible, primer índice postte, segundo
								// combustible
	private double[] enerEC; // energía eléctrica generada en el paso para cada combustible (MWh)
	private double[] costoC; // costo variable en el paso para cada combustible (USD)
	private double[][] volPC; // volumen de combustible usado en el paso para cada poste para cada
								// combustible, primer índice poste segundo índice combustible (unidades de
								// combustible)
	private double[][] potEPC; // energía eléctrica generada en cada poste del paso para cada combustible,
								// primer índice poste, segundo índice combustible (MW)

	private double[] gradGestion; // gradiente de gestión en el paso en USD/MW de potencia para cada uno de los
									// combustibles

	public DatosTermicoSP(String nombre, String nombreBarra, double potMax, double potMin, int cantModIni,
			int cantFuncUltimoIMPaso, int cantModDisp, int[] cantModDesp, double[] potencias, ArrayList<String> listaCombustibles, double[] volC,
			double[] enerTC, double[][] enerTPC, double[] enerEC, double[] costoC, double[][] volPC, double[][] potEPC,
			double costoVarMintec, double costoVarPropC[], double costoTotPaso, double[] gradGestion) {
		super();
		this.nombre = nombre;
		this.nombreBarra = nombreBarra;
		this.potMax = potMax;
		this.potMin = potMin;
		this.cantModIni = cantModIni;
		this.cantModDisp = cantModDisp;
		this.cantModDesp = cantModDesp;
		this.potencias = potencias;
		/*
		 * this.rendMax = rendMax; this.rendMin = rendMin;
		 */
		this.listaCombustibles = listaCombustibles;
		this.volC = volC;
		this.enerTC = enerTC;
		this.enerTPC = enerTPC;
		this.enerEC = enerEC;
		this.costoC = costoC;
		this.volPC = volPC;
		this.potEPC = potEPC;
		this.costoVarMintec = costoVarMintec;
		this.costoVarPropC = costoVarPropC;
		this.costoTotPaso = costoTotPaso;
		this.gradGestion = gradGestion;
		this.cantFuncUltimoIMpaso = cantFuncUltimoIMPaso;

	}

	public String getNombreBarra() {
		return nombreBarra;
	}

	public void setNombreBarra(String nombreBarra) {
		this.nombreBarra = nombreBarra;
	}

	public double getPotMax() {
		return potMax;
	}

	public void setPotMax(double potMax) {
		this.potMax = potMax;
	}

	public double getPotMin() {
		return potMin;
	}

	public void setPotMin(double potMin) {
		this.potMin = potMin;
	}

	public int getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(int cantModIni) {
		this.cantModIni = cantModIni;
	}

	public int getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public int[] getCantModDesp() {
		return cantModDesp;
	}

	public void setCantModDesp(int[] cantModDesp) {
		this.cantModDesp = cantModDesp;
	}

	public double[] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

	public double getRendMax() {
		return rendMax;
	}

	public void setRendMax(double rendMax) {
		this.rendMax = rendMax;
	}

	public double getRendMin() {
		return rendMin;
	}

	public void setRendMin(double rendMin) {
		this.rendMin = rendMin;
	}

	public double[] getCostoVarPropC() {
		return costoVarPropC;
	}

	public void setCostoVarPropC(double[] costoVarPropC) {
		this.costoVarPropC = costoVarPropC;
	}

	public double[] getVolC() {
		return volC;
	}

	public void setVolC(double[] volC) {
		this.volC = volC;
	}

	public double[] getEnerTC() {
		return enerTC;
	}

	public void setEnerTC(double[] enerTC) {
		this.enerTC = enerTC;
	}

	public double[] getEnerEC() {
		return enerEC;
	}

	public void setEnerEC(double[] enerEC) {
		this.enerEC = enerEC;
	}

	public double[][] getEnerTPC() {
		return enerTPC;
	}

	public void setEnerTPC(double[][] enerTPC) {
		this.enerTPC = enerTPC;
	}

	public double[] getCostoC() {
		return costoC;
	}

	public void setCostoC(double[] costoC) {
		this.costoC = costoC;
	}

	public double[][] getVolPC() {
		return volPC;
	}

	public void setVolPC(double[][] volPC) {
		this.volPC = volPC;
	}

	public double[][] getPotEPC() {
		return potEPC;
	}

	public void setPotEPC(double[][] potEPC) {
		this.potEPC = potEPC;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ArrayList<String> getListaCombustibles() {
		return listaCombustibles;
	}

	public void setListaCombustibles(ArrayList<String> listaCombustibles) {
		this.listaCombustibles = listaCombustibles;
	}

	public double getCostoTotPaso() {
		return costoTotPaso;
	}

	public void setCostoTotPaso(double costoTot) {
		this.costoTotPaso = costoTotPaso;
	}
	

	public int getCantFuncUltimoIMpaso() {
		return cantFuncUltimoIMpaso;
	}

	public void setCantFuncUltimoIMpaso(int cantFuncUltimoIMpaso) {
		this.cantFuncUltimoIMpaso = cantFuncUltimoIMpaso;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Generador Tórmico: " + nombre);
		System.out.println("Barra: " + nombreBarra);
		System.out.println("Potencia móxima por módulo: " + potMax);
		System.out.println("Potencia mónima por módulo" + potMin);
		System.out.println("Cantidad de módulos inicial (sólo estado): " + cantModIni);
		System.out.println("Cantidad de módulos disponibles: " + cantModDisp);
		System.out.println("Cantidad de módulos despachados: " + Arrays.toString(cantModDesp));
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("Rendimiento en el móximo: " + rendMax);
		System.out.println("Rendimiento en el mónimo: " + rendMin);
		System.out.println("------------------------------------------------------------------------");

	}

	public double getCostoVarMintec() {
		return costoVarMintec;
	}

	public void setCostoVarMintec(double costoVarMintec) {
		this.costoVarMintec = costoVarMintec;
	}

	public double[] getGradGestion() {
		return gradGestion;
	}

	public void setGradGestion(double[] gradGestion) {
		this.gradGestion = gradGestion;
	}

}

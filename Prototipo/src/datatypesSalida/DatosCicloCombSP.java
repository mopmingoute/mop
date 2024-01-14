/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosCicloCombSP is part of MOP.
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
import java.util.Hashtable;

import tiempo.Evolucion;

public class DatosCicloCombSP {

	private String nombre;
	private String nombreBarra;
	private double potMaxTG; // potencia móxima por módulo de TG en ciclo abierto
	private double potMinTG; // potencia mónima por módulo de TG en ciclo abierto
	private double potMaxCC; // pot max por módulo de TG combinado
	private double potMinCC; // pot min por módulo de TG combinado

//	private int cantModIni; // cantidad de módulos en paralelo inicialmente, sólo aplica si hay variable de estado
	private int cantModTGDisp; // cantidad de módulos disponibles de TG en el paso
	private int cantModCVDisp; // cantidad de módulos disponibles de ciclo de vapor en el paso
	private int[] cantModTGAbDesp; // cantidad de módulos despachados de TG en ciclo abierto por poste
	private int[] cantModTGCombDesp; // cantidad de módulos despachados de TG combinados por poste
	private double[] potencias; // potencia despachada por poste en MW
	private Hashtable<String, Evolucion<Double>> rendMaxTG; // rendimiento en el máximo de TG en ciclo abierto
	private Hashtable<String, Evolucion<Double>> rendMinTG; // rendimiento en el mínimo técnico de TG en ciclo abierto
	private Hashtable<String, Evolucion<Double>> rendMaxCC; // rendimiento en el máximo del ciclo combinado en el máximo
	private Hashtable<String, Evolucion<Double>> rendMinCC; // rendimiento en el mínimo técnico

	private ArrayList<String> listaCombustibles; // combustibles en el orden
//	private double costoVarMintec; // costo variable medio del mintec USD/MWh
	private double[] costoVarPropTG; // para cada combustible de listaCombustibles, costo variable por encima del
										// mintec USD/MWh de las TG del ciclo
	private double[] costoVarPropCC; // para cada combustible de listaCombustibles, costo variable por encima del
										// mintec USD/MWh del ciclo combinado

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

	private double[] potTGs; // potencia generada por las TGs en el paso
	private double[] potTVs; // potencia generada por las TVs en el paso
	private double[] potAb; // potencia generada por las TGs que están en ciclo abierto
	private double[] potComb; // potencia generada por las TGs en ciclo combinado más la potencia de CVs

	private double[] gradGestion; // gradiente de gestión en el paso en USD/MW de potencia para cada uno de los
									// combustibles

	private int modTGdisp; // cantidad de módulos de TGs disponibles en el paso
	private int modCVdisp; // cantidad de módulos de CVs disponibles en el paso
	private int cantFuncUltimoIMpaso;  // cantidad de módulos de TG combinadas en el último int. de muestreo del paso

	public DatosCicloCombSP(String nombre, String nombreBarra, double potMaxTG, double potMinTG, double potMaxCC,
			double potMinCC, int cantModTGDisp, int cantModCVDisp, int cantFuncUltimoIMPaso, int[] cantModTGAbDesp, int[] cantModTGCombDesp, 
			double[] potencias, Hashtable<String, Evolucion<Double>> rendMaxTG,
			Hashtable<String, Evolucion<Double>> rendMinTG, Hashtable<String, Evolucion<Double>> rendMaxCC,
			Hashtable<String, Evolucion<Double>> rendMinCC, ArrayList<String> listaCombustibles, double[] volC,
			double[] enerTC, double[][] enerTPC, double[] enerEC, double[] costoC, double[][] volPC, double[][] potEPC,
			double costoVarMintec, double costoVarPropTG[], double costoVarPropCC[], double costoTotPaso,
			double[] potTGs, double[] potTVs, double[] potAb, double[] potComb, double[] gradGestion) {

		super();
		this.nombre = nombre;
		this.nombreBarra = nombreBarra;
		this.potMaxTG = potMaxTG;
		this.potMinTG = potMinTG;
		this.potMaxCC = potMaxCC;
		this.potMinCC = potMinTG;

		this.cantModTGDisp = cantModTGDisp;
		this.cantModCVDisp = cantModCVDisp;
		this.cantFuncUltimoIMpaso = cantFuncUltimoIMPaso;
		this.cantModTGAbDesp = cantModTGAbDesp;
		this.cantModTGCombDesp = cantModTGCombDesp;
		this.potencias = potencias;
		this.rendMaxTG = rendMaxTG;
		this.rendMinTG = rendMinTG;
		this.rendMaxCC = rendMaxCC;
		this.rendMinCC = rendMinCC;
		this.listaCombustibles = listaCombustibles;
		this.volC = volC;
		this.enerTC = enerTC;
		this.enerTPC = enerTPC;
		this.enerEC = enerEC;
		this.costoC = costoC;
		this.volPC = volPC;
		this.potEPC = potEPC;
//		this.costoVarMintec = costoVarMintec;
//		this.costoVarPropC = costoVarPropC;
		this.costoTotPaso = costoTotPaso;
		this.gradGestion = gradGestion;
		this.potTGs = potTGs;
		this.potTVs = potTVs;
		this.potAb = potAb;
		this.potComb = potComb;
	}

	public String getNombreBarra() {
		return nombreBarra;
	}

	public void setNombreBarra(String nombreBarra) {
		this.nombreBarra = nombreBarra;
	}

	public double getPotMaxTG() {
		return potMaxTG;
	}

	public void setPotMaxTG(double potMaxTG) {
		this.potMaxTG = potMaxTG;
	}

	public double getPotMinTG() {
		return potMinTG;
	}

	public void setPotMinTG(double potMinTG) {
		this.potMinTG = potMinTG;
	}

	public double getPotMaxCC() {
		return potMaxCC;
	}

	public void setPotMaxCC(double potMaxCC) {
		this.potMaxCC = potMaxCC;
	}

	public double getPotMinCC() {
		return potMinCC;
	}

	public void setPotMinCC(double potMinCC) {
		this.potMinCC = potMinCC;
	}

	public int getCantModTGDisp() {
		return cantModTGDisp;
	}

	public void setCantModTGDisp(int cantModTGDisp) {
		this.cantModTGDisp = cantModTGDisp;
	}

	public int getCantModCVDisp() {
		return cantModCVDisp;
	}

	public void setCantModCVDisp(int cantModCVDisp) {
		this.cantModCVDisp = cantModCVDisp;
	}

	public int[] getCantModTGAbDesp() {
		return cantModTGAbDesp;
	}

	public void setCantModTGAbDesp(int[] cantModDesp) {
		this.cantModTGAbDesp = cantModDesp;
	}

	public int[] getCantModTGCombDesp() {
		return cantModTGCombDesp;
	}

	public void setCantModTGCombDesp(int[] cantModTGCombDesp) {
		this.cantModTGCombDesp = cantModTGCombDesp;
	}

	public double[] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

	public Hashtable<String, Evolucion<Double>> getRendMaxTG() {
		return rendMaxTG;
	}

	public void setRendMaxTG(Hashtable<String, Evolucion<Double>> rendMaxTG) {
		this.rendMaxTG = rendMaxTG;
	}

	public Hashtable<String, Evolucion<Double>> getRendMinTG() {
		return rendMinTG;
	}

	public void setRendMinTG(Hashtable<String, Evolucion<Double>> rendMinTG) {
		this.rendMinTG = rendMinTG;
	}

	public Hashtable<String, Evolucion<Double>> getRendMaxCC() {
		return rendMaxCC;
	}

	public void setRendMaxCC(Hashtable<String, Evolucion<Double>> rendMaxCC) {
		this.rendMaxCC = rendMaxCC;
	}

	public Hashtable<String, Evolucion<Double>> getRendMinCC() {
		return rendMinCC;
	}

	public void setRendMinCC(Hashtable<String, Evolucion<Double>> rendMinCC) {
		this.rendMinCC = rendMinCC;
	}

	public int getModTGdisp() {
		return modTGdisp;
	}

	public void setModTGdisp(int modTGdisp) {
		this.modTGdisp = modTGdisp;
	}

	public int getModCVdisp() {
		return modCVdisp;
	}

	public void setModCVdisp(int modCVdisp) {
		this.modCVdisp = modCVdisp;
	}
	
	

	public int getCantFuncUltimoIMpaso() {
		return cantFuncUltimoIMpaso;
	}

	public void setCantFuncUltimoIMpaso(int cantFuncUltimoIMpaso) {
		this.cantFuncUltimoIMpaso = cantFuncUltimoIMpaso;
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

	public double[] getPotTGs() {
		return potTGs;
	}

	public void setPotTGs(double[] potTGs) {
		this.potTGs = potTGs;
	}

	public double[] getPotTVs() {
		return potTVs;
	}

	public void setPotTVs(double[] potTVs) {
		this.potTVs = potTVs;
	}

	public double[] getCostoVarPropTG() {
		return costoVarPropTG;
	}

	public void setCostoVarPropTG(double[] costoVarPropTG) {
		this.costoVarPropTG = costoVarPropTG;
	}

	public double[] getCostoVarPropCC() {
		return costoVarPropCC;
	}

	public void setCostoVarPropCC(double[] costoVarPropCC) {
		this.costoVarPropCC = costoVarPropCC;
	}

	public double[] getPotAb() {
		return potAb;
	}

	public void setPotAb(double[] potAb) {
		this.potAb = potAb;
	}

	public double[] getPotComb() {
		return potComb;
	}

	public void setPotComb(double[] potComb) {
		this.potComb = potComb;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Generador Tórmico: " + nombre);
		System.out.println("Barra: " + nombreBarra);
		System.out.println("Potencia móxima por módulo TG en ciclo abierto: " + potMaxTG);
		System.out.println("Potencia mónima por módulo TG en ciclo abierto" + potMinTG);
		System.out.println("Potencia móxima por módulo TG en ciclo combinado: " + potMaxCC);
		System.out.println("Potencia mónima por módulo TG en ciclo combinado" + potMinCC);
//		System.out.println("Cantidad de módulos inicial (sólo estado): " + cantModIni);
		System.out.println("Cantidad de módulos disponibles TG: " + cantModTGDisp);
		System.out.println("Cantidad de módulos disponibles CV: " + cantModCVDisp);
		System.out.println("Cantidad de módulos TG en ciclo abierto despachados: " + Arrays.toString(cantModTGAbDesp));
		System.out.println(
				"Cantidad de módulos TG en ciclo combinado despachados: " + Arrays.toString(cantModTGCombDesp));
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("Rendimiento en el máximo TG: " + rendMaxTG);
		System.out.println("Rendimiento en el mínimo TG: " + rendMinTG);
		System.out.println("Rendimiento en el máximo CC: " + rendMaxCC);
		System.out.println("Rendimiento en el mínimo CC: " + rendMinCC);
		System.out.println("------------------------------------------------------------------------");

	}

	public double[] getGradGestion() {
		return gradGestion;
	}

	public void setGradGestion(double[] gradGestion) {
		this.gradGestion = gradGestion;
	}

	public int getModTGDisp() {
		return modTGdisp;
	}

	public int getModCVDisp() {
		return modCVdisp;
	}

}

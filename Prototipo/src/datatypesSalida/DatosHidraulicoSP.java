/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosHidraulicoSP is part of MOP.
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

import java.util.Arrays;

/**
 * Datos de resultados de un paso para un generador hidróulico
 * 
 * @author ut469262
 *
 */
public class DatosHidraulicoSP {
	private String nombre;
	private String nombreBarra;
	private double potMax; // potencia móxima por módulo
	private double qturMax; // turbinado móximo por modulo en m3/s
	private int cantModDisp; // cantidad de módulos disponibles en el paso
	private double[] qvert; // caudal vertido en m3/s
	private double aporte; // aporte en m3/s del paso
	private double volIni; // volumen inicial del paso en (hm)3
	private double volFin; // volumen final del paso en (hm)3 SOLO en la opción HIPERPLANOS
	private double dualVol; // variable dual de la rest. de volumen en USD/m3 ATENCIóN UNIDADES
	private double cotaArribaIni; // cota inicial aguas arriba del paso en m
	private double volEroMin; // volumen erogado mónimo del paso en (hm)3
	private double qverMax; // caudal vertible móximo del paso
	private double valAgua; // valor del agua de la central
	private double valAguaNeto; // valor del agua de la central menos valor del agua de la central aguas abajo
	private double[] potencias; // potencia despachada por poste en MW
	private double[] coefEnergMWm3s; // coeficiente energético en MW/(m3/s)
	private double[] qturb; // caudales turbinados en m3/s
	private double[] salto; // salto en cada poste en m
	private double costoTotalPaso; // costo total del paso asociado a los costos de O&M
	private double costoPenalEco; // costo de caudal faltante en USD
	private double dualvert;
	private double cotaSuperiorIncumplida;
	private double volumenSuperiorIncumplido;
	private double cotaInferiorIncumplida;
	private double volumenInferiorIncumplido;
	private double costoIncuplimientoSuperior;
	private double costoIncuplimientoInferior;

	public DatosHidraulicoSP(String nombre, String nombreBarra, double potMax, double qturMax, int cantModDisp,
			double[] qvert, double aporte, double volIni, double volFin, double dualVol, double cotaArribaIni,
			double volEroMin, double qverMax, double valAgua, double valAguaNeto, double[] potencias,
			double[] coefEnerg, double[] qturb, double[] salto, double costoTotalPaso, double costoPenalEco,
			double dualvert, double cotaSuperiorIncumplida, double volumenSuperiorIncumplido,
			double cotaInferiorIncumplida, double volumenInferiorIncumplido, double costoIncuplimientoSuperior,
			double costoIncuplimientoInferior) {
		super();
		this.nombre = nombre;
		this.nombreBarra = nombreBarra;
		this.potMax = potMax;
		this.qturMax = qturMax;
		this.cantModDisp = cantModDisp;
		this.qvert = qvert;
		this.aporte = aporte;
		this.volIni = volIni;
		this.volFin = volFin;
		this.dualVol = dualVol;
		this.cotaArribaIni = cotaArribaIni;
		this.volEroMin = volEroMin;
		this.qverMax = qverMax;
		this.valAgua = valAgua * utilitarios.Constantes.M3XHM3; // convierte a USD/hm3
		this.valAguaNeto = valAguaNeto;
		this.potencias = potencias;
		this.qturb = qturb;
		this.salto = salto;
		this.coefEnergMWm3s = coefEnerg;
		this.costoTotalPaso = costoTotalPaso;
		this.costoPenalEco = costoPenalEco;
		this.dualvert = dualvert;
		this.cotaSuperiorIncumplida = cotaSuperiorIncumplida;
		this.volumenSuperiorIncumplido = volumenSuperiorIncumplido;
		this.cotaInferiorIncumplida = cotaInferiorIncumplida;
		this.volumenInferiorIncumplido = volumenInferiorIncumplido;
		this.costoIncuplimientoSuperior = costoIncuplimientoSuperior;
		this.costoIncuplimientoInferior = costoIncuplimientoInferior;

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

	public double getQturMax() {
		return qturMax;
	}

	public void setQturMax(double qturMax) {
		this.qturMax = qturMax;
	}

	public int getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public double[] getQvert() {
		return qvert;
	}

	public void setQvert(double[] qvert) {
		this.qvert = qvert;
	}

	public double getAporte() {
		return aporte;
	}

	public void setAporte(double aporte) {
		this.aporte = aporte;
	}

	public double getVolIni() {
		return volIni;
	}

	public void setVolIni(double volIni) {
		this.volIni = volIni;
	}

	public double getVolFin() {
		return volFin;
	}

	public void setVolFin(double volFin) {
		this.volFin = volFin;
	}

	public double getDualVol() {
		return dualVol;
	}

	public void setDualVol(double dualVol) {
		this.dualVol = dualVol;
	}

	public double getCotaArribaIni() {
		return cotaArribaIni;
	}

	public void setCotaArribaIni(double cotaArribaIni) {
		this.cotaArribaIni = cotaArribaIni;
	}

	public double getVolEroMin() {
		return volEroMin;
	}

	public void setVolEroMin(double volEroMin) {
		this.volEroMin = volEroMin;
	}

	public double getQverMax() {
		return qverMax;
	}

	public void setQverMax(double qverMax) {
		this.qverMax = qverMax;
	}

	public double getValAgua() {
		return valAgua;
	}

	public void setValAgua(double valAgua) {
		this.valAgua = valAgua;
	}

	public double getValAguaNeto() {
		return valAguaNeto;
	}

	public void setValAguaNeto(double valAguaNeto) {
		this.valAguaNeto = valAguaNeto;
	}

	public double[] getPotencias() {
		return potencias;
	}

	public void setPotencias(double[] potencias) {
		this.potencias = potencias;
	}

	public double[] getQturb() {
		return qturb;
	}

	public void setQturb(double[] qturb) {
		this.qturb = qturb;
	}

	public double[] getSalto() {
		return salto;
	}

	public void setSalto(double[] salto) {
		this.salto = salto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getCostoPenalEco() {
		return costoPenalEco;
	}

	public void setCostoPenalEco(double costoPenalEco) {
		this.costoPenalEco = costoPenalEco;
	}

	public void imprimir() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Generador Hidróulico: " + nombre);
		System.out.println("Barra: " + nombreBarra);
		System.out.println("Potencia móxima por módulo: " + potMax);
		System.out.println("Caudal turbinable móximo :" + qturMax);
		System.out.println("Cantidad de módulos disponibles: " + cantModDisp);
		System.out.println("Caudal vertido en el paso: " + qvert);
		System.out.println("Aporte: " + aporte);
		System.out.println("Volumen Inicial: " + volIni);
		System.out.println("Cota aguas arriba: " + cotaArribaIni);
		System.out.println("Volumen Erogado Minimo: " + volEroMin);
		System.out.println("Caudal vertible móximo: " + qverMax);
		System.out.println("Valor del agua: " + valAgua);
		System.out.println("Valor del agua neto: " + valAguaNeto);
		System.out.println("Potencias: " + Arrays.toString(potencias));
		System.out.println("Turbinado: " + Arrays.toString(qturb));
		System.out.println("Salto en cada poste: " + Arrays.toString(salto));
		System.out.println("------------------------------------------------------------------------");

	}

	public double[] getCoefEnergMWm3s() {
		return coefEnergMWm3s;
	}

	public void setCoefEnergMWm3s(double[] coefEnergMWm3s) {
		this.coefEnergMWm3s = coefEnergMWm3s;
	}

	public double getCostoTotalPaso() {
		return costoTotalPaso;
	}

	public void setCostoTotalPaso(double costoTotalPaso) {
		this.costoTotalPaso = costoTotalPaso;
	}

	public double getDualvert() {
		return dualvert;
	}

	public void setDualvert(double dualvert) {
		this.dualvert = dualvert;
	}

	public double getCotaSuperiorIncumplida() {
		return cotaSuperiorIncumplida;
	}

	public void setCotaSuperiorIncumplida(double cotaSuperiorIncumplida) {
		this.cotaSuperiorIncumplida = cotaSuperiorIncumplida;
	}

	public double getVolumenSuperiorIncumplido() {
		return volumenSuperiorIncumplido;
	}

	public void setVolumenSuperiorIncumplido(double volumenSuperiorIncumplido) {
		this.volumenSuperiorIncumplido = volumenSuperiorIncumplido;
	}

	public double getCotaInferiorIncumplida() {
		return cotaInferiorIncumplida;
	}

	public void setCotaInferiorIncumplida(double cotaInferiorIncumplida) {
		this.cotaInferiorIncumplida = cotaInferiorIncumplida;
	}

	public double getVolumenInferiorIncumplido() {
		return volumenInferiorIncumplido;
	}

	public void setVolumenInferiorIncumplido(double volumenInferiorIncumplido) {
		this.volumenInferiorIncumplido = volumenInferiorIncumplido;
	}

	public double getCostoIncuplimientoSuperior() {
		return costoIncuplimientoSuperior;
	}

	public void setCostoIncuplimientoSuperior(double costoIncuplimientoSuperior) {
		this.costoIncuplimientoSuperior = costoIncuplimientoSuperior;
	}

	public double getCostoIncuplimientoInferior() {
		return costoIncuplimientoInferior;
	}

	public void setCostoIncuplimientoInferior(double costoIncuplimientoInferior) {
		this.costoIncuplimientoInferior = costoIncuplimientoInferior;
	}
}

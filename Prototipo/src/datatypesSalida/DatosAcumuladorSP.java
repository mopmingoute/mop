/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosAcumuladorSP is part of MOP.
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

public class DatosAcumuladorSP {
	private String nombre;
	private String nombreBarra;
	private double potMax; // potencia móxima por módulo
	private double potMaxAlmac;
	private int cantModDisp; // cantidad de módulos disponibles en el paso
	private double energAlmacIni; // volumen inicial del paso en (hm)3
	private double valEnerg; // valor del agua de la central
	private double[] potenciasIny; // potencia despachada por poste en MW
	private double[] potenciasAlm; // potencia despachada por poste en MW
	private double[] potenciaNeta;
	private double[] energAcum;
	private double costoTotalPaso; // costo total del paso asociado al acumulador
	private double gradGestion; // gradiente de gestión en el paso en USD/MW de potencia

	public DatosAcumuladorSP(String nombre, String nombreBarra, double potMax, double potMaxAlmac, int cantModDisp,
			double energAlmacIni, double valEnerg, double[] potenciasIny, double[] potenciasAlm, double[] potenciaNeta,
			double[] energAcum, double costoTotalP, double gradGestion) {
		super();
		this.nombre = nombre;
		this.nombreBarra = nombreBarra;
		this.potMax = potMax;
		this.potMaxAlmac = potMaxAlmac;
		this.cantModDisp = cantModDisp;
		this.energAlmacIni = energAlmacIni;
		this.valEnerg = valEnerg;
		this.potenciasIny = potenciasIny;
		this.potenciasAlm = potenciasAlm;
		this.costoTotalPaso = costoTotalP;
		this.gradGestion = gradGestion;
		this.potenciaNeta = potenciaNeta;
		this.setEnergAcum(energAcum);
	}

	public void setGradGestion(double gradGestion) {
		this.gradGestion = gradGestion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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

	public double getPotMaxAlmac() {
		return potMaxAlmac;
	}

	public void setPotMaxAlmac(double potMaxAlmac) {
		this.potMaxAlmac = potMaxAlmac;
	}

	public int getCantModDisp() {
		return cantModDisp;
	}

	public void setCantModDisp(int cantModDisp) {
		this.cantModDisp = cantModDisp;
	}

	public double getEnergAlmacIni() {
		return energAlmacIni;
	}

	public void setEnergAlmacIni(double energAlmacIni) {
		this.energAlmacIni = energAlmacIni;
	}

	public double getValEnerg() {
		return valEnerg;
	}

	public void setValEnerg(double valEnerg) {
		this.valEnerg = valEnerg;
	}

	public double[] getPotenciasIny() {
		return potenciasIny;
	}

	public void setPotenciasIny(double[] potenciasIny) {
		this.potenciasIny = potenciasIny;
	}

	public double[] getPotenciasAlm() {
		return potenciasAlm;
	}

	public void setPotenciasAlm(double[] potenciasAlm) {
		this.potenciasAlm = potenciasAlm;
	}

	public double getCostoTotalPaso() {
		return costoTotalPaso;
	}

	public void setCostoTotalPaso(double costoTotalPaso) {
		this.costoTotalPaso = costoTotalPaso;
	}

	public double[] getPotenciaNeta() {
		return potenciaNeta;
	}

	public void setPotenciaNeta(double[] potenciaNeta) {
		this.potenciaNeta = potenciaNeta;
	}

	public double[] getEnergAcum() {
		return energAcum;
	}

	public void setEnergAcum(double[] energAcum) {
		this.energAcum = energAcum;
	}

	public double getGradGestion() {
		return gradGestion;
	}

}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosSalidaPaso is part of MOP.
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

public class DatosSalidaPaso {
	
	private DatosPaso paso;
	private DatosRedSP red;
	private ArrayList<DatosRedCombustibleSP> redesComb;
	private DatosConstructorHiperplanosSP consHip;
	private ArrayList<DatosImpactoSP> impactos;
	private ArrayList<DatosContratoEnergiaSP> contratosEnergia;
	private ResultadoIteracionSP despSinExp;
	
	public DatosSalidaPaso() {
		super();
		redesComb = new ArrayList<>();
		impactos = new ArrayList<DatosImpactoSP>();
		contratosEnergia = new ArrayList<DatosContratoEnergiaSP>();
		despSinExp = new ResultadoIteracionSP();
	}
	
	public DatosPaso getPaso() {
		return paso;
	}
	public void setPaso(DatosPaso paso) {
		this.paso = paso;
	}
	public DatosRedSP getRed() {
		return red;
	}
	public void setRed(DatosRedSP red) {
		this.red = red;
	}
	public ArrayList<DatosRedCombustibleSP> getRedesComb() {
		return redesComb;
	}
	public void setRedesComb(ArrayList<DatosRedCombustibleSP> redesComb) {
		this.redesComb = redesComb;
	}
	public void agregarRedCombustible(DatosRedCombustibleSP dred) {
		this.redesComb.add(dred);		
	}
	public DatosRedCombustibleSP getRedComb(String nombre) {
		for (DatosRedCombustibleSP r: redesComb) {
			if (r.getNombre().equalsIgnoreCase(nombre)) {
				return r;
			}
		}
		return null;
	}
	
	public void imprimir() {
		System.out.println("Salida Paso : " + paso.getNumPaso());
		red.imprimir();
		for (DatosRedCombustibleSP drc: redesComb) {
			drc.imprimir();
		}
		System.out.println("Fin");
	}
	public DatosConstructorHiperplanosSP getConsHip() {
		return consHip;
	}
	public void setConsHip(DatosConstructorHiperplanosSP consHip) {
		this.consHip = consHip;
	}
	public void agregarImpacto(DatosImpactoSP imp) {
		this.impactos.add(imp);		
	}
	
	public ArrayList<DatosImpactoSP> getImpactos() {
		return impactos;
	}
	public void setImpactos(ArrayList<DatosImpactoSP> impactos) {
		this.impactos = impactos;
	}
	public ArrayList<DatosContratoEnergiaSP> getContratosEnergia() {
		return contratosEnergia;
	}
	public void setContratosEnergia(ArrayList<DatosContratoEnergiaSP> contratosEnergia) {
		this.contratosEnergia = contratosEnergia;
	}
	
	public void agregarContratoEnergia(DatosContratoEnergiaSP con) {
		this.contratosEnergia.add(con);		
	}
	
    public ResultadoIteracionSP getDespSinExp() {
		return despSinExp;
	}

	public void setDespSinExp(ResultadoIteracionSP despSinExp) {
		this.despSinExp = despSinExp;
	}
}


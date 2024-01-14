/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosBarraSP is part of MOP.
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


public class DatosBarraSP {

	private String nombre;
	private ArrayList<DatosDemandaSP> demandas;
	private ArrayList<DatosHidraulicoSP> hidraulicos;
	private ArrayList<DatosTermicoSP> termicos;
	private ArrayList<DatosCicloCombSP> ciclosCombinados;
	private ArrayList<DatosEolicoSP> eolicos;
	private ArrayList<DatosFotovoltaicoSP> fotovoltaicos;
	private ArrayList<DatosAcumuladorSP> acumuladores;
	private ArrayList<DatosImpoExpoSP> impoExpos;
	private double[] costoMarginal; // costos marginales por poste

	
	public DatosBarraSP() {
		super();
		this.demandas = new ArrayList<DatosDemandaSP>();
		this.hidraulicos = new ArrayList<DatosHidraulicoSP>();
		this.termicos = new ArrayList<DatosTermicoSP>();
		this.ciclosCombinados = new ArrayList<DatosCicloCombSP>();
		this.eolicos = new ArrayList<DatosEolicoSP>();	
		this.fotovoltaicos = new ArrayList<DatosFotovoltaicoSP>();
		this.acumuladores = new ArrayList<DatosAcumuladorSP>();
		this.impoExpos = new ArrayList<DatosImpoExpoSP>();
	}
	

	

	public ArrayList<DatosImpoExpoSP> getImpoExpos() {
		return impoExpos;
	}

	public void setImpoExpos(ArrayList<DatosImpoExpoSP> impoExpos) {
		this.impoExpos = impoExpos;
	}

	public ArrayList<DatosDemandaSP> getDemandas() {
		return demandas;
	}
	public void setDemandas(ArrayList<DatosDemandaSP> demandas) {
		this.demandas = demandas;
	}
	public ArrayList<DatosHidraulicoSP> getHidraulicos() {
		return hidraulicos;
	}
	public void setHidraulicos(ArrayList<DatosHidraulicoSP> hidraulicos) {
		this.hidraulicos = hidraulicos;
	}
	public ArrayList<DatosTermicoSP> getTermicos() {
		return termicos;
	}
	public void setTermicos(ArrayList<DatosTermicoSP> termicos) {
		this.termicos = termicos;
	}
	
	public ArrayList<DatosCicloCombSP> getCiclosCombinados() {
		return ciclosCombinados;
	}

	public void setCiclosCombinados(ArrayList<DatosCicloCombSP> ciclosCombinados) {
		this.ciclosCombinados = ciclosCombinados;
	}

	public void setFotovoltaicos(ArrayList<DatosFotovoltaicoSP> fotovoltaicos) {
		this.fotovoltaicos = fotovoltaicos;
	}

	public ArrayList<DatosEolicoSP> getEolicos() {
		return eolicos;
	}
	public void setEolicos(ArrayList<DatosEolicoSP> eolicos) {
		this.eolicos = eolicos;
	}
	public ArrayList<DatosFotovoltaicoSP> getFotovoltaicos() {
		return fotovoltaicos;
	}
	public void setFotovoltaico(ArrayList<DatosFotovoltaicoSP> fotovoltaicos) {
		this.fotovoltaicos = fotovoltaicos;
	}
	public double[] getCostoMarginal() {
		return costoMarginal;
	}
	public void setCostoMarginal(double[] costoMarginal) {
		this.costoMarginal = costoMarginal;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
	


	public void agregarDemanda(DatosDemandaSP demanda) {
		this.demandas.add(demanda);
		
	}


	
	public void agregarImpoExpo(DatosImpoExpoSP dat){
		this.impoExpos.add(dat);
	}

	public void agregarEolico(DatosEolicoSP eolo) {
		this.eolicos.add(eolo);		
	}
	
	public void agregarFotovoltaico(DatosFotovoltaicoSP foto) {
		this.fotovoltaicos.add(foto);		
	}

	public void agregarTermico(DatosTermicoSP ter) {
		this.termicos.add(ter);
	}
	
	public void agregarCicloCombinado(DatosCicloCombSP dcc) {
		this.ciclosCombinados.add(dcc);
	}

	public void agregarHidraulico(DatosHidraulicoSP hid) {
		this.hidraulicos.add(hid);
	}
	
	
	public void imprimir() {
	
		
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Barra :" + nombre);
		System.out.println("Costo Marginal: " + Arrays.toString(costoMarginal));
		System.out.println("------------------------------------------------------------------------");
		
		for (DatosDemandaSP ddsp: demandas) {
			ddsp.imprimir();
		}
		for (DatosHidraulicoSP dhsp: hidraulicos) {
			dhsp.imprimir();
		}
		for (DatosTermicoSP dtsp: termicos) {
			dtsp.imprimir();
		}
		for (DatosEolicoSP desp: eolicos) {
			desp.imprimir();
		}
		for (DatosFotovoltaicoSP dfsp: fotovoltaicos) {
			dfsp.imprimir();
		}

		System.out.println("------------------------------------------------------------------------");
	}

	public ArrayList<DatosAcumuladorSP> getAcumuladores() {
		return acumuladores;
	}

	public void setAcumuladores(ArrayList<DatosAcumuladorSP> acumuladores) {
		this.acumuladores = acumuladores;
	}

	public void agregarAcumulador(DatosAcumuladorSP acu) {
		this.acumuladores.add(acu);		
	}


	
}


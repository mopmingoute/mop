/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * DatosRamaCorrida is part of MOP.
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

package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import tiempo.Evolucion;



/**
 * Datatype asociado a los datos de una rama en la red elóctrica
 * @author ut602614
 *
 */

public class DatosRamaCorrida implements Serializable{

	private static final long serialVersionUID = 1L;
	private String nombre;
	private String propietario;
	private String barra1;
	private String barra2;
	private Evolucion<Double> peaje12;
	private Evolucion<Double> peaje21;
	private Evolucion<Double> potMax12;
	private Evolucion<Double> potMax21;
	private Evolucion<Double> perdidas12;
	private Evolucion<Double> perdidas21;
	private Evolucion<Double> X;
	private Evolucion<Double> R;
	private Evolucion<Integer> cantModInst;
	private Evolucion<Integer> mantProgramado;

	private Evolucion<String> compRama;
	private Integer cantModIni;
	private Evolucion<Double> dispMedia;
	private Evolucion<Double> tMedioArreglo;
	
	private Hashtable<String,Evolucion<String>> valoresComportamientos;
	private Evolucion<Double> costoFijo;
	private boolean salDetallada;

	
	public DatosRamaCorrida(String nombre, String barra1, String barra2,
			Evolucion<Double> peaje12, Evolucion<Double> peaje21,
			Evolucion<Double> potMax12, Evolucion<Double> potMax21,
			Evolucion<Double> perdidas12, Evolucion<Double> perdidas21,
			Evolucion<Double> x, Evolucion<Double> r,
			Evolucion<Integer> cantModInst, 
			Integer cantModIni,
			Evolucion<String> compRama,
			Evolucion<Double> dispMedia, Evolucion<Double> tMedioArreglo,
			Hashtable<String, Evolucion<String>> valoresComportamientos, boolean salDetallada,
			Evolucion<Integer> mantProgramado, Evolucion<Double> costoFijo) {
		super();
		this.nombre = nombre;
		this.barra1 = barra1;
		this.barra2 = barra2;
		this.peaje12 = peaje12;
		this.peaje21 = peaje21;
		this.potMax12 = potMax12;
		this.potMax21 = potMax21;
		this.perdidas12 = perdidas12;
		this.perdidas21 = perdidas21;
		X = x;
		R = r;
		this.cantModInst = cantModInst;
		this.compRama = compRama;
		this.cantModIni = cantModIni;
		this.dispMedia = dispMedia;
		this.tMedioArreglo = tMedioArreglo;
		this.valoresComportamientos = valoresComportamientos;
		this.setSalDetallada(salDetallada);
		this.setMantProgramado(mantProgramado);
		this.setCostoFijo(costoFijo);
	}

	public boolean isSalDetallada() {
		return salDetallada;
	}

	public void setSalDetallada(boolean salDetallada) {
		this.salDetallada = salDetallada;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getBarra1() {
		return barra1;
	}

	public void setBarra1(String barra1) {
		this.barra1 = barra1;
	}

	public String getBarra2() {
		return barra2;
	}

	public void setBarra2(String barra2) {
		this.barra2 = barra2;
	}


	public Evolucion<Double> getPeaje12() {
		return peaje12;
	}

	public void setPeaje12(Evolucion<Double> peaje12) {
		this.peaje12 = peaje12;
	}

	public Evolucion<Double> getPeaje21() {
		return peaje21;
	}

	public void setPeaje21(Evolucion<Double> peaje21) {
		this.peaje21 = peaje21;
	}

	public Evolucion<Double> getPotMax12() {
		return potMax12;
	}

	public void setPotMax12(Evolucion<Double> potMax12) {
		this.potMax12 = potMax12;
	}

	public Evolucion<Double> getPotMax21() {
		return potMax21;
	}

	public void setPotMax21(Evolucion<Double> potMax21) {
		this.potMax21 = potMax21;
	}

	public Evolucion<Double> getPerdidas12() {
		return perdidas12;
	}

	public void setPerdidas12(Evolucion<Double> perdidas12) {
		this.perdidas12 = perdidas12;
	}

	public Evolucion<Double> getPerdidas21() {
		return perdidas21;
	}

	public void setPerdidas21(Evolucion<Double> perdidas21) {
		this.perdidas21 = perdidas21;
	}

	public Evolucion<Double> getX() {
		return X;
	}

	public void setX(Evolucion<Double> x) {
		X = x;
	}

	public Evolucion<Double> getR() {
		return R;
	}

	public void setR(Evolucion<Double> r) {
		R = r;
	}

	public Evolucion<Integer> getCantModInst() {
		return cantModInst;
	}

	public void setCantModInst(Evolucion<Integer> cantModInst) {
		this.cantModInst = cantModInst;
	}

	public Integer getCantModIni() {
		return cantModIni;
	}

	public void setCantModIni(Integer cantModIni) {
		this.cantModIni = cantModIni;
	}

	public Evolucion<Double> getDispMedia() {
		return dispMedia;
	}

	public void setDispMedia(Evolucion<Double> dispMedia) {
		this.dispMedia = dispMedia;
	}

	
	
	public Evolucion<Double> gettMedioArreglo() {
		return tMedioArreglo;
	}

	public void settMedioArreglo(Evolucion<Double> tMedioArreglo) {
		this.tMedioArreglo = tMedioArreglo;
	}

	public Hashtable<String, Evolucion<String>> getValoresComportamientos() {
		return valoresComportamientos;
	}

	public void setValoresComportamientos(
			Hashtable<String, Evolucion<String>> valoresComportamientos) {
		this.valoresComportamientos = valoresComportamientos;
	}

	public Evolucion<Integer> getMantProgramado() {
		return mantProgramado;
	}

	public void setMantProgramado(Evolucion<Integer> mantProgramado) {
		this.mantProgramado = mantProgramado;
	}

	public Evolucion<Double> getCostoFijo() {
		return costoFijo;
	}

	public void setCostoFijo(Evolucion<Double> costoFijo) {
		this.costoFijo = costoFijo;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}

	public ArrayList<String> controlDatosCompletos() {
		ArrayList<String> errores = new ArrayList<>();

		if(nombre.trim().equals("")) errores.add("Rama: Nombre vacío.");
		if(barra1 == null) { errores.add("Rama " +  nombre + ": Barra1 vacío."); }
		if(barra2 == null) { errores.add("Rama " +  nombre + ": Barra2 vacío."); }

		if(peaje12 == null) { errores.add("Rama " +  nombre + ": peaje12 vacío."); }
		else if (peaje12.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": peaje12 vacío."); }

		if(peaje21 == null) { errores.add("Rama " +  nombre + ": peaje21 vacío."); }
		else if (peaje21.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": peaje21 vacío."); }

		if(potMax12 == null) { errores.add("Rama " +  nombre + ": potMax12 vacío."); }
		else if (potMax12.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": potMax12 vacío."); }
		if(potMax21 == null) { errores.add("Rama " +  nombre + ": potMax21 vacío."); }
		else if (potMax21.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": potMax21 vacío."); }

		if(perdidas12 == null) { errores.add("Rama " +  nombre + ": perdidas12 vacío."); }
		else if (perdidas12.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": perdidas12 vacío."); }
		if(perdidas21 == null) { errores.add("Rama " +  nombre + ": perdidas21 vacío."); }
		else if (perdidas21.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": perdidas21 vacío."); }

		if(X == null) { errores.add("Rama " +  nombre + ": X vacío."); }
		else if (X.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": X vacío."); }
		if(R == null) { errores.add("Rama " +  nombre + ": R vacío."); }
		else if (R.controlDatosCompletos().size() > 0 ) { errores.add("Rama " +  nombre + ": R vacío."); }


		if(cantModInst == null ) { errores.add("Rama " +  nombre + ": cantModInst vacío."); }
		else if (cantModInst.controlDatosCompletos().size() > 0){ errores.add("Rama " +  nombre + ": cantModInst vacío."); }

		if(mantProgramado == null ) { errores.add("Rama " +  nombre + ": mantProgramado vacío."); }
		else if (mantProgramado.controlDatosCompletos().size() > 0){ errores.add("Rama " +  nombre + ": mantProgramado vacío."); }

		return errores;
	}

	public Evolucion<String> getCompRama() {
		return compRama;
	}

	public void setCompRama(Evolucion<String> compRama) {
		this.compRama = compRama;
	}

	

}
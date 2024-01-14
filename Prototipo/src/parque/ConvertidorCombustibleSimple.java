/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ConvertidorCombustibleSimple is part of MOP.
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

package parque;

import compdespacho.ConvertCombSimpleCompDesp;

import datatypes.DatosConvertidorCombustibleSimpleCorrida;

/**
 * Clase que representa el convertidor de combustible simple
 * @author ut602614
 *
 */
public class ConvertidorCombustibleSimple extends ConvertidorCombustible{
	private ConvertCombSimpleCompDesp comportamiento;



	public ConvertidorCombustibleSimple(
			DatosConvertidorCombustibleSimpleCorrida dccc,
			Combustible combOrigen, Combustible combDestino,
			BarraCombustible bcorigen, BarraCombustible bcdestino) {
//		this.setBarraDestino(bcdestino);
//		this.setBarraOrigen(bcorigen);
//		this.setCombustibleOrigen(combOrigen);
//		this.setCombustibleTransformado(combDestino);
//		this.setNombre(dccc.getNombre());
//		this.setCantModulos(dccc.getCantModulos());
//		//this.setCantModulosDisponibles(dccc.getCantModulosDisponibles());
//		this.setFlujoMaxConvertido(dccc.getFlujoMaxConvertido());
//		this.setFlujoMaxOrigen(dccc.getFlujoMaxOrigen());
//		this.setRelacion(dccc.getRelacion());		
//		this.comportamiento = new ConvertCombSimpleCompDesp(dccc,this);
	}

	public ConvertCombSimpleCompDesp getComportamiento() {
		return (ConvertCombSimpleCompDesp)comportamiento;
	}

	public void setComportamiento(ConvertCombSimpleCompDesp comportamiento) {
		this.comportamiento = comportamiento;
	}
	
	public void cargarDatosSimulacion(){
		
	}
}

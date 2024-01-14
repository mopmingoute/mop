/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ClaveDatosAtributosDetallados is part of MOP.
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

public class ClaveDatosAtributosDetallados {
	
	private String nombreRecurso; // nombre del recurso, participante, barra o escalón de falla
	private String nombreAtributo; // nombre del atributo detallado en utilitarios.Constantes
	
	
	
	public ClaveDatosAtributosDetallados(String nombreRecurso, String nombreAtributo) {
		super();
		this.nombreRecurso = nombreRecurso;
		this.nombreAtributo = nombreAtributo;
	}
	
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nombreAtributo == null) ? 0 : nombreAtributo.hashCode());
		result = prime * result + ((nombreRecurso == null) ? 0 : nombreRecurso.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClaveDatosAtributosDetallados other = (ClaveDatosAtributosDetallados) obj;
		if (nombreAtributo == null) {
			if (other.nombreAtributo != null)
				return false;
		} else if (!nombreAtributo.equals(other.nombreAtributo))
			return false;
		if (nombreRecurso == null) {
			if (other.nombreRecurso != null)
				return false;
		} else if (!nombreRecurso.equals(other.nombreRecurso))
			return false;
		return true;
	}




	public String getNombreRecurso() {
		return nombreRecurso;
	}
	public void setNombreRecurso(String nombreRecurso) {
		this.nombreRecurso = nombreRecurso;
	}
	public String getNombreAtributo() {
		return nombreAtributo;
	}
	public void setNombreAtributo(String nombreAtributo) {
		this.nombreAtributo = nombreAtributo;
	}
	
	

}

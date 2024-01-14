/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * XcargaDatos is part of MOP.
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

package matrices;



/**
 *
 * @author ut600232
 */
public class XcargaDatos extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String descripcion;

    public XcargaDatos() {
        descripcion = "";
    }

    public XcargaDatos(String texto) {
        descripcion = texto;
    }

    public void setDescripcion(String texto) {
        descripcion = texto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}

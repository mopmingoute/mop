/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * GeneradorDistUniformeLCXOr is part of MOP.
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

package procesosEstocasticos;

import java.lang.management.ManagementFactory;

import lpsolve.LpSolve;
import utilitarios.Constantes;
import utilitarios.DirectoriosYArchivos;

public class GeneradorDistUniformeLCXOr extends GeneradorDistUniforme {
	

	private long v = 4101842887655102017L;
	private long w = 1;
	

	public GeneradorDistUniformeLCXOr(long semilla) {
		super(semilla);
		this.semilla = semilla;
		// TODO Auto-generated constructor stub
	}

	public long randomLong() {		
		//DirectoriosYArchivos.agregaTexto("d:\\detalleGeneradoresAleat.txt", Long.toString(semilla));
		semilla = semilla * 2862933555777941757L + 7046029254386353087L;
		v ^= v >>> 17;
		v ^= v << 31;
		v ^= v >>> 8;
		w = 4294957665L * (w & 0xffffffff) + (w >>> 32);
		long x = semilla ^ (semilla << 21);
		x ^= x >>> 35;
		x ^= x << 4;
		long ret = (x + v) ^ w;
		return ret;
	}

	@Override
	public Double generarValor() {		
	//	DirectoriosYArchivos.agregaTexto(Constantes.ruta_log_paralelismo+"semillas_"+ManagementFactory.getRuntimeMXBean().getName()+".txt", this.getSemilla().toString());
		Double divisor = new Double(Long.MAX_VALUE);
		divisor *= 2;
		return randomLong() / divisor + 0.5d;
	}


}

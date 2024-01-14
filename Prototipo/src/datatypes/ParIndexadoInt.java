/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * ParIndexadoInt is part of MOP.
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

public class ParIndexadoInt implements Comparable<ParIndexadoInt> {

	public final int first;
	public final double second;

	public ParIndexadoInt(int first, double second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public int compareTo(ParIndexadoInt p) {
		return (int) Math.signum(first-p.first);		
	}

}

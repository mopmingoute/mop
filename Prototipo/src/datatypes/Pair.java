/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Pair is part of MOP.
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

/**
 * Datatype genórico que representa un par
 * @author ut602614
 *
 * @param <F> tipo del primer valor del par
 * @param <S> tipo del segundo valor del par
 */
public class Pair<F, S> implements Serializable{
    private static final long serialVersionUID = 1L;
	public final F first;
    public final S second;

   
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

//    public boolean equals(Object o) {
//        if (!(o instanceof Pair)) {
//            return false;
//        }
//        Pair<?, ?> p = (Pair<?, ?>) o;
//        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
//    }

    
    
    
	public Pair<S,F> espejo() {
		return new Pair<S,F>(this.second, this.first);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
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
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}
	
/*	public int compareTo(Pair p) {
		return (int) p.first + 100*p.second;
		
	}*/

	@Override
	public  String toString(){
		return "(" + this.first + "," + this.second + ")";
	}

}
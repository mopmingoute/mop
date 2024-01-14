/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * mainLlamadorMingo is part of MOP.
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

package presentacion;

import java.io.IOException;

public class mainLlamadorMingo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Process runtime = Runtime.getRuntime().exec("cmd /c start C:\\Users\\ut469262\\Desktop\\mainMingo.bat");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * Sistema is part of MOP.
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

package utilitarios;

public class Sistema {
	
	public static void muestraMemoria() {
		// Get current size of heap in bytes
		long heapSize = Runtime.getRuntime().totalMemory(); 

		// Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
		long heapMaxSize = Runtime.getRuntime().maxMemory();

		 // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
		long heapFreeSize = Runtime.getRuntime().freeMemory(); 	
		
		System.out.println("Memoria máxima posible heap: " + heapMaxSize/1024/1024 + "Mb ");
		System.out.println("Memoria actual heap: " + heapSize/1024/1024 + "Mb ");
		System.out.println("Memoria libre heap: " + heapFreeSize/1024/1024 + "Mb ");
		
	}

}

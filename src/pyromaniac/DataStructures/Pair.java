/*
 * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
 * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
 * 
 * 	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pyromaniac.DataStructures;

// TODO: Auto-generated Javadoc
/**
 * The Class Pair.
 *
 * @param <T> the generic type
 * @param <S> the generic type
 */
public class Pair<T, S>
{
	//private variables
	  /** The first. */
	private T first;
	  
  	/** The second. */
  	private S second;

	  /**
  	 * Instantiates a new pair.
  	 *
  	 * @param f the f
  	 * @param s the s
  	 */
  	public Pair(T f, S s){ 
	    first = f;
	    second = s;   
	  }
	 
	  /**
  	 * Gets the first.
  	 *
  	 * @return the first
  	 */
  	public T getFirst(){
	    return first;
	  }
	 
	  /**
  	 * Gets the second.
  	 *
  	 * @return the second
  	 */
  	public S getSecond()   {
	    return second;
	  }
	 
	  /* (non-Javadoc)
  	 * @see java.lang.Object#toString()
  	 */
  	public String toString()  { 
	    return "(" + first.toString() + ", " + second.toString() + ")"; 
	  }
	 
}


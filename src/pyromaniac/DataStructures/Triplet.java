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
 * The Class Triplet.
 *
 * @param <T1> the generic type
 * @param <T2> the generic type
 * @param <T3> the generic type
 */
public class Triplet<T1, T2, T3> 
{
    
    /**
     * Creates a new instance of Pair.
     *
     * @param first first value
     * @param second second value
     * @param third the third
     */
    public Triplet(T1 first, T2 second, T3 third) 
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) 
    {
        if (obj instanceof Pair) {
            Triplet other = (Triplet)obj;
             return (other.first == null ? this.first == null : other.first.equals(this.first)) && 
                     (other.second == null ? this.second == null : other.second.equals(this.second)
                    && (other.third == null? this.third == null : other.third.equals(this.third))		 
                     );
        }
        else
            return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return first.toString() +"::"+second.toString() + "::" + third.toString();
    }

    
    /** The first. */
    public T1 first;
    
    /** The second. */
    public T2 second;
    
    /** The third. */
    public T3 third;
}

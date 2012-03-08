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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


// TODO: Auto-generated Javadoc
/**
 * The Class PatriciaTrie.
 */
public class PatriciaTrie implements Collection <HashSet <Pyrotag>>
{
	
	/** The head. */
	private Node head;
	
	/** The Constant MAX_CHILDREN. */
	public static final int MAX_CHILDREN = 5;
	
	/** The Constant STRING_END. */
	public static final char STRING_END = '$';
	
	/** The Constant baseToArrayPos. */
	public static final HashMap <Character, Integer> baseToArrayPos;
	
	/** The Constant arrayPosToBase. */
	public static final HashMap <Integer, Character> arrayPosToBase;
	
	static
	{
		//alphabetical order
		baseToArrayPos =  new HashMap <Character, Integer>(17);
		baseToArrayPos.put('$', 0);
		baseToArrayPos.put('A', 1);
		baseToArrayPos.put('C', 2);
		baseToArrayPos.put('G', 3);
		baseToArrayPos.put('T', 4);
		
		arrayPosToBase = new HashMap <Integer, Character>(17);
		arrayPosToBase.put(0, '$');
		arrayPosToBase.put(1, 'A');
		arrayPosToBase.put(2, 'C');
		arrayPosToBase.put(3, 'G');
		arrayPosToBase.put(4, 'T');
	}
	
	/**
	 * Instantiates a new patricia trie.
	 */
	public PatriciaTrie()
	{
		this.head = new Node('^');
	}
	
	/**
	 * Insert string.
	 *
	 * @param value the value
	 * @param p the p
	 */
	public void insertString(String value, Pyrotag p)
	{
		this.head.insertSeq(value, 0, p);
	}
	
	/**
	 * Gets the prefix sets.
	 *
	 * @return the prefix sets
	 */
	public LinkedList <Pair <HashSet <Pyrotag>, String>> getPrefixSets()
	{
		ArrayDeque <Pair<Node, HashSet <Pyrotag>>> queue = new ArrayDeque<Pair <Node, HashSet <Pyrotag>>>();
		
		queue.addFirst(new Pair <Node, HashSet <Pyrotag>> (this.head, new HashSet <Pyrotag>()));
		
		LinkedList <Pair <HashSet <Pyrotag>,  String>> sharedPrefixSets = new LinkedList <Pair <HashSet <Pyrotag>, String>> ();
		
		while(queue.size() > 0)
		{
			Pair <Node, HashSet <Pyrotag>> pairToProcess = queue.pop();
			
			Node noValue = pairToProcess.getFirst().getChild(STRING_END);
			
			boolean finishedPath = true;
			for(int i = 0; i < PatriciaTrie.MAX_CHILDREN; i++)
			{	
				char currChild = arrayPosToBase.get(i); 
				if(currChild != STRING_END && pairToProcess.getFirst().getChild(currChild) != null)
				{
					finishedPath = false;
					
					
					HashSet <Pyrotag> clone = new HashSet <Pyrotag>();
					clone.addAll(pairToProcess.getSecond());
					
					if(noValue != null)
					{
						clone.addAll(noValue.getTags());
					}
					
					queue.push(new Pair <Node, HashSet <Pyrotag>>(pairToProcess.getFirst().getChild(currChild),clone));
				}
			}
			
			if(finishedPath && pairToProcess.getFirst() !=  this.head) //the second case is silly.
			{
				if(noValue != null)
				{
					pairToProcess.getSecond().addAll(noValue.getTags());
				}
				
				Pyrotag rep = pairToProcess.getSecond().iterator().next(); //the longest sequences shall be the rep.
				String repSeq = new String(rep.getCollapsedRead());
				sharedPrefixSets.push(new Pair <HashSet <Pyrotag>, String>(pairToProcess.getSecond(), repSeq));
			}
		}
		
		return sharedPrefixSets;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	public PTIterator iterator()
	{
		return new PTIterator(this.head);
	}
	
	//iterator
	/**
	 * The Class PTIterator.
	 */
	class PTIterator implements Iterator <HashSet <Pyrotag>>
	{
		
		/** The head. */
		Node head;
		
		/** The queue. */
		ArrayDeque <Pair <Node, HashSet <Pyrotag>>> queue;
	
		/**
		 * Instantiates a new pT iterator.
		 *
		 * @param head the head
		 */
		public PTIterator(Node head)
		{
			this.head = head;
			this.queue = new ArrayDeque<Pair <Node, HashSet <Pyrotag>>>();
			queue.addFirst(new Pair <Node, HashSet <Pyrotag>> (this.head, new HashSet <Pyrotag>()));
		}
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() 
		{
			if(queue.size() > 0  && queue.peek().getFirst().hasChildren())
			{
				return true;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public HashSet<Pyrotag> next() 
		{
			assert(queue.size() > 0);
			
			while(queue.size() > 0)
			{
				Pair <Node, HashSet <Pyrotag>> pairToProcess = queue.pop();
				Node noValue = pairToProcess.getFirst().getChild(STRING_END);
				boolean finishedPath = true;
				for(int i = 0; i < PatriciaTrie.MAX_CHILDREN; i++)
				{	
					char currChild = arrayPosToBase.get(i); 
					if(currChild != STRING_END && pairToProcess.getFirst().getChild(currChild) != null)
					{
						finishedPath = false;
						
						
						HashSet <Pyrotag> clone = new HashSet <Pyrotag>();
						clone.addAll(pairToProcess.getSecond());
						
						if(noValue != null)
						{
							clone.addAll(noValue.getTags());
						}
						
						queue.push(new Pair <Node, HashSet <Pyrotag>>(pairToProcess.getFirst().getChild(currChild),clone));
					}
				}
					
				if(finishedPath && pairToProcess.getFirst() !=  this.head) //the second case is silly.
				{
					if(noValue != null)
					{
						pairToProcess.getSecond().addAll(noValue.getTags());
					}
					
					return pairToProcess.getSecond();
				}			
			}
			
			assert(false); 
			//shouldn't ever get to this code.
			return null;
		}

		//DOES NOTHING
		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() 
		{
			// TODO Auto-generated method stub
			
		}
	}
	



	/**
	 * The Class Node.
	 */
	class Node
	{
		
		/** The value. */
		private char value;
		
		/** The children. */
		private Node [] children;	
		
		/** The tags. */
		HashSet <Pyrotag> tags;
		
		/**
		 * Instantiates a new node.
		 *
		 * @param value the value
		 */
		public Node(char value)
		{
			this.value = value;
			children = null;
			tags = null;
		}
		
		/**
		 * Checks for children.
		 *
		 * @return true, if successful
		 */
		public boolean hasChildren() 
		{
			for(Node n: children)
			{
				if(n != null)
					return true;
			}

			return false;
		}

		/**
		 * Adds the tag.
		 *
		 * @param p the p
		 */
		public void addTag(Pyrotag p)
		{
			if(tags == null)
				tags = new HashSet <Pyrotag>();
			
			tags.add(p);
		}
		
		/**
		 * Delete tags.
		 */
		public void deleteTags()
		{
			this.tags = null;
		}
		
		/**
		 * Gets the tags.
		 *
		 * @return the tags
		 */
		public HashSet <Pyrotag> getTags()
		{
			return this.tags;
		}
		
		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public char getValue() 
		{
			return value;
		}
		
		/**
		 * Sets the value.
		 *
		 * @param value the new value
		 */
		public void setValue(char value) 
		{
			this.value = value;
		}
		
		/**
		 * Gets the children.
		 *
		 * @return the children
		 */
		public Node[] getChildren() 
		{
			return children;
		}
		
		/**
		 * Sets the children.
		 *
		 * @param children the new children
		 */
		public void setChildren(Node[] children) 
		{
			this.children = children;
		}
		
		/**
		 * Adds the child.
		 *
		 * @param node the node
		 */
		public void addChild(Node node)
		{
			if(this.children == null)
				this.children = new Node [PatriciaTrie.MAX_CHILDREN]; 
			
			this.children[baseToArrayPos.get(node.getValue())] = node;
		}
		
		/**
		 * Gets the child.
		 *
		 * @param value the value
		 * @return the child
		 */
		public Node getChild(char value)
		{
			if(this.children == null)
				return null;
			
			return this.children[baseToArrayPos.get(value)];
		}
		
		/**
		 * Insert seq.
		 *
		 * @param sequence the sequence
		 * @param offset the offset
		 * @param p the p
		 */
		public void insertSeq(String sequence, int offset, Pyrotag p)
		{
			if(sequence.length() == offset)
			{
				if(this.getChild(STRING_END) == null)
				{
					Node newChild = new Node(STRING_END);
					
					newChild.addTag(p);
					this.addChild(newChild);
				}
				else
				{
					this.getChild(STRING_END).addTag(p);
				}
			}
			else
			{
				char currInSeq = sequence.charAt(offset);
				if(getChild(currInSeq) == null)
				{
					Node newChild = new Node(currInSeq);
					this.addChild(newChild);
					newChild.insertSeq(sequence, offset + 1, p);
				}
				else
				{
					getChild(currInSeq).insertSeq(sequence, offset + 1, p);
				}
			}
		}
	}




	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(HashSet<Pyrotag> e) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends HashSet<Pyrotag>> c) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() 
	{
		// TODO Auto-generated method stub	
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object o) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	public int size() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) 
	{
		// TODO Auto-generated method stub
		return null;
	}	
}

package com.kyperbox.managers;

import java.util.Comparator;

public interface Priority {
	public static final int LOW = 0;
	public static final int MEDIUM = 1;
	public static final int HIGH = 2;
	
	public int getPriority();
	public void setPriority(int priority);
	
	
	public static class PriorityComparator implements Comparator<Priority>{
		@Override
		public int compare(Priority o1, Priority o2) {
			if(o1.getPriority() < o2.getPriority())
				return 1;
			else if(o1.getPriority() > o2.getPriority())
				return -1;
			return 0;
		}
		
	}
}

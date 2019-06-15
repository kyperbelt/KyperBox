package com.kyperbox.managers;

import java.util.Comparator;

public interface Priority {
	
	public int getPriority();
	
	public static class PriorityComparator implements Comparator<Priority>{
		@Override
		public int compare(Priority o1, Priority o2) {
			return Integer.compare(o2.getPriority(), o1.getPriority());
		}
		
	}
}

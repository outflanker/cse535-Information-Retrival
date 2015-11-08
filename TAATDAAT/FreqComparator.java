package assignment;

import java.util.Comparator;

public class FreqComparator implements Comparator<Posting>{
	

	@Override
	public int compare(Posting o1, Posting o2) {
		if(o1.frequency<=o2.frequency){
			if(o1.docID<o2.docID){
				return -1;
			}
			return 1;
		}
		else {
			return -1;
		}
	}
}

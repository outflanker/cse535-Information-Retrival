package assignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class CSE535Assignment {
	static int K;
	static String termFile;
	static String logFile;
	static String queryFile;
	static Writer writer;
	static HashMap<String, LinkedList<Posting>> data=new HashMap<String, LinkedList<Posting>>();
		
	static int minHighest;
	static String topTerms[];
	static int topFreq[];
	private static void initialize(){
		minHighest=0;
		topTerms=new String[K];
		topFreq=new int[K];
		for(int i=0;i<K;i++){
			topFreq[i]=0;
			topTerms[i]="";
		}
	}
	private static void addTerm(String term, int frequency){
		if(frequency<=minHighest){
			return;
		}
		else{
			int pos=K-1;
			for(int i=K-1;i>=0;i--){
				if(topFreq[i]<frequency){
					pos=i;
				}
			}
			for(int i=K-1;i>pos;i--){
				topFreq[i]=topFreq[i-1];
				topTerms[i]=topTerms[i-1];
			}
			topFreq[pos]=frequency;
			topTerms[pos]=term;
			minHighest=topFreq[K-1];
		}
	}		
	private static String[] getTopTerms(){
		return topTerms;
	}

	public static void main(String[] args) {
		try {
			K = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			System.out.println("Input K: " + args[2] + " is not of type integer!");
			return;
		}
		initialize();
		termFile = args[0];
		File f = new File(termFile);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Term index file :" + termFile + " not found!");
			return;
		}
		BufferedReader reader = null;
		HashMap<String, Integer> frequency=new HashMap<String, Integer>();
		try {
			String term,list,tempSplit[];
			int count;
			reader = new BufferedReader(new FileReader(f));
			String line;
			String tempDocIds[];
			LinkedList<Posting> docIds;
			int tid,tfreq;
			
			while ((line = reader.readLine()) != null) {
				tempSplit=line.split("\\\\c");
				term=tempSplit[0];
				count=Integer.parseInt(tempSplit[1].split("\\\\m")[0]);
				
				addTerm(term, count);
				
				frequency.put(term, count);
				list=tempSplit[1].split("\\\\m")[1];
				list=list.replace("[", "");
				list=list.replace("]", "");
				
				docIds=new LinkedList<Posting>();
				tempDocIds=list.split(", ");

				for (String s : tempDocIds) {
					tid=Integer.parseInt(s.split("/")[0]);
					tfreq=Integer.parseInt(s.split("/")[1]);
					docIds.add(new Posting(tid,tfreq));
				}
				data.put(term, docIds);
								
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		logFile = args[1];
		f = new File(logFile);
		if (f.exists()) {
			System.out.println("Log file: " + logFile + " already exists! Overwriting!");
		}
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * Indexed successfully!
		 * Calling getTopK
		 */

		try {
			getTopK();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * Querying
		 */
		queryFile = args[3];
		f = new File(queryFile);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Query file :" + queryFile + " not found!");
			return;
		}
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String terms[]=line.split("\\s+");
				for(String term:terms){
					getPostings(term);
				}
				line=line.replaceAll("\\s+", ", ");
				writer.write("FUNCTION: termAtATimeQueryAnd "+line+"\n");
				termAtATimeQueryAnd(terms);
				writer.write("FUNCTION: termAtATimeQueryOr "+line+"\n");
				termAtATimeQueryOr(terms);
				writer.write("FUNCTION: docAtATimeQueryAnd "+line+"\n");
				docAtATimeQueryAnd(terms);
				writer.write("FUNCTION: docAtATimeQueryOr "+line+"\n");
				docAtATimeQueryOr(terms);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				writer.close();
			} catch (Exception we) {
				we.printStackTrace();
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Completed!");
	}
	/**
	 * Function to get top K terms
	 * @throws IOException
	 */
	private static void getTopK() throws IOException {
		int i=0;
		writer.write("FUNCTION: getTopK " + K+"\n");
		writer.write("Result: ");
		String topTerms[]=getTopTerms();
		for (i=0;i<topTerms.length-1;i++) {
			writer.write(topTerms[i]+", ");
		}
		writer.write(topTerms[i]+"\n");
	}
	/**
	 * Function to get postings list for term
	 * @param term
	 * @throws IOException
	 */
	private static void getPostings(String term) throws IOException{
		writer.write("FUNCTION: getPostings "+term+"\n");
		LinkedList<Posting> postings=new LinkedList<Posting>();
		postings=data.get(term);
		if(postings==null){
			writer.write("Term not found: "+term+"\n");
			return;
		}
		else{
			LinkedList<Posting> posts=new LinkedList<Posting>();
			writer.write("Ordered by doc IDs: ");
			int n=postings.size(),i;
			int docID[] = new int[n];
			int tfreq;
			for(i=0;i<n-1;i++){
				docID[i]=postings.get(i).docID;
				writer.write(docID[i]+", ");
				tfreq=postings.get(i).frequency;
				posts.add(new Posting(docID[i], tfreq));
			}
			docID[i]=postings.get(i).docID;
			tfreq=postings.get(i).frequency;
			writer.write(docID[i]+"\n");			
			posts.add(new Posting(docID[i], tfreq));
			Collections.sort(posts, new FreqComparator());
			writer.write("Ordered by TF: ");
			for(i=0;i<n-1;i++){
				writer.write(posts.get(i).docID+", ");
			}
			writer.write(posts.get(i).docID+"\n");
		}
	}
	/**
	 * Term-At-A-Time Query for AND
	 * @param terms
	 * @throws IOException
	 */
	private static void termAtATimeQueryAnd(String[] terms) throws IOException {
		LinkedList<Integer> queryDocs=new LinkedList<Integer>();
		LinkedList<Posting> postings=new LinkedList<Posting>();
		if(terms.length==0){
			writer.write("Null query list!\n");
			return;
		}
		postings=new LinkedList<Posting>(data.get(terms[0]));
		Collections.sort(postings, new FreqComparator());
		int comparisions=0;
		boolean found;
		
		long time = System.currentTimeMillis();
		
		for (Posting posting : postings) {
			queryDocs.add(posting.docID);
		}
		if(terms.length==1){
			writer.write(queryDocs.size()+" documents are found\n");
			writer.write("0 comparisions are made\n");
			writer.write("0 seconds are used\n");
			writer.write("0 comparisons are made with optimization\n");
			return;
		}
		
		for (int i=1;i<terms.length;i++) {
			postings=new LinkedList<Posting>(data.get(terms[i]));
			Collections.sort(postings, new FreqComparator());
			for(int j=0;j<queryDocs.size();j++){
				found=false;
				for(int k=0;k<postings.size();k++){
					comparisions++;
					if(queryDocs.get(j)==postings.get(k).docID){
						found=true;
						break;
					}
				}
				if(found){
					found=false;
				}else{
					queryDocs.remove(j);
					j=j-1;
				}
			}
		}
		time=System.currentTimeMillis()-time;
		writer.write(queryDocs.size()+" documents are found\n");
		writer.write(comparisions+" comparisions are made\n");
		writer.write(time+" milliseconds are used\n");
		
		writer.write(comparisions+" comparisons are made with optimization\n");
		writer.write("Result: ");
		Collections.sort(queryDocs);
		int i;
		for(i=0;i<queryDocs.size()-1;i++){
			writer.write(queryDocs.get(i)+", ");
		}
		writer.write(queryDocs.get(i)+"\n");
	}
	/**
	 * Term-At-A-Time Query for OR
	 * @param terms
	 * @throws IOException
	 */
	private static void termAtATimeQueryOr(String[] terms) throws IOException {
		LinkedList<Integer> queryDocs=new LinkedList<Integer>();
		LinkedList<Posting> postings=new LinkedList<Posting>();
		if(terms.length==0){
			writer.write("Null query list!\n");
			return;
		}
		postings=new LinkedList<Posting>(data.get(terms[0]));

		Collections.sort(postings, new FreqComparator());
		int comparisions=0;
		boolean found;
		
		long time = System.currentTimeMillis();
		
		for (Posting posting : postings) {
			queryDocs.add(posting.docID);
		}
		if(terms.length==1){
			writer.write(queryDocs.size()+" documents are found\n");
			writer.write("0 comparisions are made\n");
			writer.write("0 seconds are used\n");
			writer.write("0 comparisons are made with optimization\n");
			return;
		}
		
		for (int i=1;i<terms.length;i++) {
			postings=new LinkedList<Posting>(data.get(terms[i]));

			Collections.sort(postings, new FreqComparator());
			for(int k=0;k<postings.size();k++){
				found=false;
				for(int j=0;j<queryDocs.size();j++){
					comparisions++;
					if(queryDocs.get(j)==postings.get(k).docID){
						found=true;
						break;
					}
				}
				if(found){
					found=false;
				}else{
					queryDocs.add(postings.get(k).docID);
				}
			}
		}
		time=System.currentTimeMillis()-time;
		writer.write(queryDocs.size()+" documents are found\n");
		writer.write(comparisions+" comparisions are made\n");
		writer.write(time+" milliseconds are used\n");
		
		writer.write(comparisions+" comparisons are made with optimization\n");
		writer.write("Result: ");
		Collections.sort(queryDocs);
		int i;
		for(i=0;i<queryDocs.size()-1;i++){
			writer.write(queryDocs.get(i)+", ");
		}
		writer.write(queryDocs.get(i)+"\n");
	}
	/**
	 * Doc-At-A-Time Query for AND
	 * @param terms
	 * @throws IOException
	 */
	private static void docAtATimeQueryAnd(String[] terms) throws IOException {
		int n=terms.length;
		LinkedList<Integer> queryDocs=new LinkedList<Integer>();
		ArrayList<LinkedList<Posting>> postings=new ArrayList<LinkedList<Posting>>();
		int pointers[]=new int[n];
		if(terms.length==0){
			writer.write("Null query list!\n");
			return;
		}
		int comparisions=0;
		int max=0;
		for (int i=0;i<n;i++) {
			pointers[i]=0;
			LinkedList<Posting> posts;
			posts=new LinkedList<Posting>(data.get(terms[i]));
			if(posts.size()>max){
				max=posts.size();
			}
			postings.add(posts);
		}
		boolean found=false;
		boolean end=false;
		
		long time = System.currentTimeMillis();
		int maxid=postings.get(0).get(pointers[0]).docID;
		while(true){
			for(int j=0;j<n;j++){
				comparisions++;
				while(postings.get(j).get(pointers[j]).docID<maxid){
					if(pointers[j]>=postings.get(j).size()-1){
						break;
					}
					pointers[j]=pointers[j]+1;
				}
			}
			found=true;
			for(int j=0;j<n;j++){
				comparisions++;
				if(postings.get(j).get(pointers[j]).docID!=maxid){
					found=false;
					if(maxid<postings.get(j).get(pointers[j]).docID){
						maxid=postings.get(j).get(pointers[j]).docID;
					}
				}
			}
			if(found){
				queryDocs.add(maxid);
				found=false;
				for(int j=0;j<n;j++){
					if(pointers[j]<postings.get(j).size()-1){
						pointers[j]=pointers[j]+1;
						maxid=postings.get(j).get(pointers[j]).docID;;
						break;
					}
				}
			}
			end=false;
			for(int j=0;j<n;j++){
				if(pointers[j]>=postings.get(j).size()-1){
					end=true;
				}
			}
			if(end){
				break;
			}
		}
		
		time=System.currentTimeMillis()-time;
		writer.write(queryDocs.size()+" documents are found\n");
		writer.write(comparisions+" comparisions are made\n");
		writer.write(time+" milliseconds are used\n");
		writer.write("Result: ");
		Collections.sort(queryDocs);
		int i;
		for(i=0;i<queryDocs.size()-1;i++){
			writer.write(queryDocs.get(i)+", ");
		}
		writer.write(queryDocs.get(i)+"\n");
	}
	/**
	 * Doc-At-A-Time Query for OR
	 * @param terms
	 * @throws IOException
	 */
	private static void docAtATimeQueryOr(String[] terms) throws IOException {
		int n=terms.length;
		LinkedList<Integer> queryDocs=new LinkedList<Integer>();
		ArrayList<LinkedList<Posting>> postings=new ArrayList<LinkedList<Posting>>();
		int pointers[]=new int[n];
		if(terms.length==0){
			writer.write("Null query list!\n");
			return;
		}
		int comparisions=0;
		int max=0;
		for (int i=0;i<n;i++) {
			pointers[i]=0;
			LinkedList<Posting> posts=new LinkedList<Posting>();
			posts=data.get(terms[i]);
			if(posts.size()>max){
				max=posts.size();
			}
			postings.add(posts);
		}
		boolean found=false;
		boolean end=false;
		
		long time = System.currentTimeMillis();
		int maxid=postings.get(0).get(pointers[0]).docID;
		
		while(true){
			for(int j=0;j<n;j++){
				comparisions++;
				while(postings.get(j).get(pointers[j]).docID<maxid){
					found=false;
					if(pointers[j]>=postings.get(j).size()-1){
						break;
					}
					for(int i=0;i<queryDocs.size();i++){
						comparisions++;
						if(postings.get(j).get(pointers[j]).docID==queryDocs.get(i)){
							pointers[j]=pointers[j]+1;
							found=true;
							break;
						}
					}
					if(!found){
						queryDocs.add(postings.get(j).get(pointers[j]).docID);
					}
					pointers[j]=pointers[j]+1;
				}
			}
			queryDocs.add(maxid);
			for(int j=0;j<n;j++){				
				if(postings.get(j).get(pointers[j]).docID>maxid){
					if(pointers[j]<postings.get(j).size()-1){
						maxid=postings.get(j).get(pointers[j]).docID;
						pointers[j]=pointers[j]+1;
						break;
					}
				}
				
			}
			end=true;
			for(int j=0;j<n;j++){
				if(pointers[j]<postings.get(j).size()-1){
					end=false;
				}
			}
			if(end){
				break;
			}
		}
		
		time=System.currentTimeMillis()-time;
		writer.write(queryDocs.size()+" documents are found\n");
		writer.write(comparisions+" comparisions are made\n");
		writer.write(time+" milliseconds are used\n");
		writer.write("Result: ");
		Collections.sort(queryDocs);
		int i;
		for(i=0;i<queryDocs.size()-1;i++){
			writer.write(queryDocs.get(i)+", ");
		}
		writer.write(queryDocs.get(i)+"\n");
	}
}

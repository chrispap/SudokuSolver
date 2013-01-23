package sudoku;

import java.util.Arrays;
import java.util.Comparator;

public class Solver {

	Cell[][] SDK;
	Usage[]  lineUsage;
	Usage[]  columnUsage;
	Usage[]  squareUsage;
	int countEmpty;

	public Solver(Cell[][] sdk  ){
		SDK = sdk;
		lineUsage 		= new Usage[10];
		columnUsage 	= new Usage[10];
		squareUsage 	= new Usage[10];
		countEmpty = countEmpty();
		checkSDK();
	}
	
	/**
	 * Parses the sudoku and fills the
	 * usage arrays
	 */
	private void constructUsageArrays() {
		int line,column,square;
		int i;

		for (line=1;line<10;line++){
			lineUsage[line] = new Usage();
			for (column=1;column<10;column++){
				if (SDK[line][column].filled){
					i = ~(1<<SDK[line][column].value);
					lineUsage[line].poss &=i;
				}
			}
		}

		for (column=1;column<10;column++){
			columnUsage[column] = new Usage();
			for (line=1;line<10;line++){
				if (SDK[line][column].filled){
					i = ~(1<<SDK[line][column].value);
					columnUsage[column].poss &=i;
				}
			}
		}

		int cell;
		for (square=1;square<10;square++){
			squareUsage[square] = new Usage();
			for (cell=1;cell<10;cell++){
				line   = Cell.grammh(square, cell);
				column = Cell.sthlh(square, cell);
				if (SDK[line][column].filled){
					i = ~(1<<SDK[line][column].value);
					squareUsage[square].poss &=i;
				}
			}
		}

	}

	/**
	 * @return The number of non found sudoku cells
	 */
	private int countEmpty() {
		int line,column,counter=0;
		for (line=1;line<10;line++){
			for (column=1;column<10;column++){
				if (!SDK[line][column].filled) counter++;
			}
		}
		return counter;
	}

	/**
	 * ΕΛΕΓΧΕΙ ΤΗΝ ΟΡΘΟΤΗΤΑ ΤΟΥ ΠΑΖΛ
	 * ΑΝ ΔΗΛΑΔΗ ΥΠΑΡΧΟΥΝ ΣΥΓΚΡΟΥΣΕΙΣ 
	 * ΣΥΜΦΩΝΑ ΜΕ ΤΟΝ ΒΑΣΙΚΟ ΚΑΝΟΝΑ ΤΟΥ
	 * 
	 * @return true-> σωστό SDK / false->λάθος SDK
	 */
	public boolean checkSDK(){

		constructUsageArrays();
		cellPossibleUpdate();

		int line,column,square;
		Cell cell;
		int errorCounter=0;
		int[] counter ;

		for (line=1;line<10;line++){
			for (column=1;column<10;column++){
				SDK[line][column].conflict = false;
			}
		}

		for (line=1;line<10;line++){
			counter = new int[]{0,0,0,0,0,0,0,0,0,0};
			for (column=1;column<10;column++){
				cell = SDK[line][column]; 
				if (cell.filled) {
					counter[cell.value]++;
				}
			}
			for (column=1;column<10;column++){
				if (SDK[line][column].filled && counter[SDK[line][column].value]>1) {
					SDK[line][column].conflict = true;
					errorCounter++;
				}
			}
		}

		for (column=1;column<10;column++){
			counter = new int[]{0,0,0,0,0,0,0,0,0,0};
			for (line=1;line<10;line++){
				cell = SDK[line][column]; 
				if (cell.filled) {
					counter[cell.value]++;
				}
			}
			for (line=1;line<10;line++){
				if (SDK[line][column].filled && counter[SDK[line][column].value]>1) {
					SDK[line][column].conflict = true;
					errorCounter++;
				}
			}
		}

		int i;
		for (square=1;square<10;square++){
			counter = new int[]{0,0,0,0,0,0,0,0,0,0};
			for (i=1;i<10;i++){
				line   = Cell.grammh(square, i);
				column = Cell.sthlh(square, i);
				cell = SDK[line][column];
				if (cell.filled) {
					counter[cell.value]++;
				}
			}
			for (i=1;i<10;i++){
				line   = Cell.grammh(square, i);
				column = Cell.sthlh(square, i);
				if (SDK[line][column].filled && counter[SDK[line][column].value]>1) {
					SDK[line][column].conflict = true;
					errorCounter++;
				}
			}
		}
		return errorCounter==0? true: false;
	}

	/** 
	 * Attempts to solve the current puzzle
	 * @return -1 on error 
	 * 		The number of found cells otherwise.
	 */
	public int solveSDK() {
		int arxikaEmpty;
		if (!checkSDK()){
			return -1;
		}
		
		arxikaEmpty = countEmpty =  countEmpty();

		constructUsageArrays();
		cellPossibleUpdate();
		int digitsCount = 0;
		digitsCount += cellValuesFind();//ΚΑΝΕΙ ΠΡΩΙΜΟ ΕΛΕΓΧΟ

		int prin;
		while (countEmpty>0){
			prin=digitsCount;
			digitsCount += monadikesTimes();
			digitsCount += arcConsistent();
			if(prin==digitsCount)break;//an den vrethei kanena keli se kapoio loop tote stamatame kai to programma den mporei na lysei to sygkekrimeno sudoku
		}
		
		countEmpty = countEmpty();
		
		return arxikaEmpty - countEmpty;
	}


	/*ΒΟΗΘΗΤΙΚΕΣ ΣΥΝΑΡΤΗΣΕΙΣ */
	
	private  int arcConsistent() {
		int  digitsCount=0 ;
		int found=0;
		do{
			digitsCount=found;
			arcConsistentLine();found += cellValuesFind();
			arcConsistentColumn();found += cellValuesFind();
			arcConsistentSquare();found += cellValuesFind();
		}while (digitsCount!=found);
		return digitsCount;
	}
	private void arcConsistentLine(){
		int x,y;
		for (x=1;x<10;x++){
			int n=0;
			for(y=1;y<10;y++){//ΒΡΙΣΚΩ ΤΟ ΠΛΗΘΟΣ ΤΩΝ ΑΔΕΙΩΝ ΚΕΛΙΩΝ ΑΥΤΗΣ ΤΗΣ ΓΡΑΜΜΗΣ
				n = (!SDK[x][y].filled)? n+1:n;
			}

			Integer kelia[] = new Integer[n]; 
			int i=0;
			for (y=1;y<10;y++){ //ΦΤΙΑΧΝΩ ΤΟΝ ΠΙΝΑΚΑ ΜΕ ΤΑ ΚΕΛΙΑ ΣΕ ΑΥΞΟΥΣΑ ΣΕΙΡΑ ΩΣ ΠΡΟΣ ΤΟ ΠΛΗΘΟΣ ΠΙΘΑΝΩΝ ΤΙΜΩΝ
				if (!SDK[x][y].filled) kelia[i++]=SDK[x][y].poss;
			}

			try{
				Arrays.sort((Object[])kelia, new CellComparator());
			}catch(Exception exc){
				exc.printStackTrace();
			}

			int d=0; 
			int count=1;
			if (!(kelia.length>0)) return;
			d=kelia[0];
			for (i=1;i<n;i++){
				d|=kelia[i];
				count++;
				if (possibleCount(d)==count ) break;//ΒΡΙΣΚΩ ΕΝΑ ΣΥΝΟΛΟ count ΨΗΦΙΩΝ ΠΟΥ ΤΑ ΜΟΙΡΑΖΟΝΤΑΙ count ΚΕΛΙΑ
			}

			if (possibleCount(d)==count){
				for (y=1;y<10;y++){
					if (!SDK[x][y].filled){
						if (((~d)&(SDK[x][y].poss))!=0)
							SDK[x][y].poss &= (~d);
					}
				}
			}
		}
	}
	private void arcConsistentColumn(){
		int x,y;
		for (x=1;x<10;x++){
			int n=0;
			for(y=1;y<10;y++){//ΒΡΙΣΚΩ ΤΟ ΠΛΗΘΟΣ ΤΩΝ ΑΔΕΙΩΝ ΚΕΛΙΩΝ ΑΥΤΗΣ ΤΗΣ ΣΤΗΛΗΣ
				n = (!SDK[y][x].filled)? n+1:n;
			}

			Integer kelia[] = new Integer[n];
			int i=0;
			for (y=1;y<10;y++){ //ΦΤΙΑΧΝΩ ΤΟΝ ΠΙΝΑΚΑ ΜΕ ΤΑ ΚΕΛΙΑ ΣΕ ΑΥΞΟΥΣΑ ΣΕΙΡΑ ΩΣ ΠΡΟΣ ΤΟ ΠΛΗΘΟΣ ΠΙΘΑΝΩΝ ΤΙΜΩΝ
				if (!SDK[y][x].filled) kelia[i++]=SDK[y][x].poss;
			}

			try{
				Arrays.sort((Object[])kelia, new CellComparator());
			}catch(Exception exc){
				exc.printStackTrace();
			}

			int d=0; 
			int count=1;
			if (!(kelia.length>0)) return;
			d=kelia[0];
			for (i=1;i<n;i++){
				d|=kelia[i];
				count++;
				if (possibleCount(d)==count ) break;//ΒΡΙΣΚΩ ΕΝΑ ΣΥΝΟΛΟ count ΨΗΦΙΩΝ ΠΟΥ ΤΑ ΜΟΙΡΑΖΟΝΤΑΙ count ΚΕΛΙΑ
			}
			if (possibleCount(d)==count){
				for (y=1;y<10;y++){
					if (!SDK[y][x].filled){
						if (((~d)&(SDK[y][x].poss))!=0)
							SDK[y][x].poss &= (~d);
					}
				}
			}
		}
	}
	private void arcConsistentSquare() {
		int sq,cl,x,y;
		for (sq=1;sq<10;sq++){
			int n=0;
			for(cl=1;cl<10;cl++){
				y=Cell.sthlh(sq,cl);
				x=Cell.grammh(sq,cl);
				n = (!SDK[x][y].filled)? n+1:n;
			}//ΒΡΙΣΚΩ ΤΟ ΠΛΗΘΟΣ ΤΩΝ ΑΔΕΙΩΝ ΚΕΛΙΩΝ ΑΥΤΟΥ ΤΟΥ ΤΕΤΡΑΓΩΝΟΥ

			int i=0;
			Integer kelia[] = new Integer[n];
			for(cl=1;cl<10;cl++){
				y = Cell.sthlh(sq,cl);
				x = Cell.grammh(sq,cl);
				if (!SDK[x][y].filled)
					kelia[i++]=SDK[x][y].poss;
			}//ΦΤΙΑΧΝΩ ΤΟΝ ΠΙΝΑΚΑ ΜΕ ΤΑ ΚΕΛΙΑ ΣΕ ΑΥΞΟΥΣΑ ΣΕΙΡΑ ΩΣ ΠΡΟΣ ΤΟ ΠΛΗΘΟΣ ΠΙΘΑΝΩΝ ΤΙΜΩΝ
			try {
				Arrays.sort((Object[])kelia, new CellComparator());
			} catch (Exception exc) {
				exc.printStackTrace();
			}

			int d=0; 
			int count=1;
			if (!(kelia.length>0)) return;
			d=kelia[0];
			for (i=1;i<n;i++){
				d|=kelia[i];
				count++;
				if (possibleCount(d)==count ) break;//ΒΡΙΣΚΩ ΕΝΑ ΣΥΝΟΛΟ count ΨΗΦΙΩΝ ΠΟΥ ΤΑ ΜΟΙΡΑΖΟΝΤΑΙ count ΚΕΛΙΑ
			}
			if (possibleCount(d)==count){
				for(cl=1;cl<10;cl++){
					y = Cell.sthlh(sq,cl);
					x = Cell.grammh(sq,cl);
					if (!SDK[x][y].filled){
						if (((~d)&(SDK[x][y].poss))!=0)
							SDK[x][y].poss &= (~d);
					}
				}
			}
		}
	}

	private  int monadikesTimes() {
		int  digitsCount=0 ;
		int found=0;
		do{
			digitsCount=found;
			monadikesTimesLine();found += cellValuesFind();
			monadikesTimesColumn();found += cellValuesFind();
			monadikesTimesSquare();found += cellValuesFind();
		}while (digitsCount!=found);
		return digitsCount;
	}
	private void monadikesTimesLine(){
		int x,y;
		for (x=1;x<10;x++){
			int d=0,carry=0;
			for (y=1;y<10;y++){
				if (SDK[x][y].filled) continue;
				carry |= d&SDK[x][y].poss ;
				d |= SDK[x][y].poss;
			}//ΣΕ ΑΥΤΟ ΤΟ for ΒΡΙΣΚΩ ΤΙΣ ΜΟΝΑΔΙΚΕΣ ΤΙΜΕΣ
			d ^= carry;//Το d έχει άσσο στα bit που αντιπροσωπεύουν μοναδική τιμή σε αυτο το πεδίο περιορισμών
			for (y=1;y<10;y++){
				if (!SDK[x][y].filled && (SDK[x][y].poss&d)!=0 ){
					if(possibleCount(SDK[x][y].poss & d)==1)
						SDK[x][y].poss &= d;
				}
			}
		}
	}
	private void monadikesTimesColumn(){
		int x,y;
		for (y=1;y<10;y++){
			int d=0,carry=0;
			for (x=1;x<10;x++){
				if (SDK[x][y].filled) continue;
				carry |= d&SDK[x][y].poss ;
				d |= SDK[x][y].poss;
			}
			d ^= carry;
			for (x=1;x<10;x++){
				if (!SDK[x][y].filled && (SDK[x][y].poss&d)!=0 ){
					if(possibleCount(SDK[x][y].poss & d)==1)
						SDK[x][y].poss &= d;
				}
			}
		}
	}
	private void monadikesTimesSquare(){
		int sqr,cell,x,y;
		for (sqr=1;sqr<10;sqr++){
			int d=0,carry=0;
			for (cell=1;cell<10;cell++){
				x=Cell.grammh(sqr,cell);
				y=Cell.sthlh(sqr,cell);
				if (SDK[x][y].filled) continue;
				carry |= d&SDK[x][y].poss ;
				d |= SDK[x][y].poss;
			}
			d ^= carry;
			for (cell=1;cell<10;cell++){
				x=Cell.grammh(sqr,cell);
				y=Cell.sthlh(sqr,cell);
				if (!SDK[x][y].filled && (SDK[x][y].poss&d)!=0 ){
					if(possibleCount(SDK[x][y].poss & d)==1)
						SDK[x][y].poss &= d;
				}
			}
		}
	}

	private  int cellValuesFind() {
		int u,d;
		int x,y,digitsCount=0;
		for(x=1;x<10;x++){
			for(y=1;y<10;y++){
				d=SDK[x][y].poss;						//d τα πιθανά ψηφία του κελιου
				u=possibleCount(d);					//πόσα πιθανά ψηφία έχει αυτό το κελί.
				if (SDK[x][y].filled==false && u==1){	//αν μόνο 1 πιθανό στοιχείο τότε είναι και η τελική τιμή του.
					countEmpty--;
					SDK[x][y].filled = true;
					SDK[x][y].poss = Cell.NONE_POSSIBLE;
					SDK[x][y].value = uniqDigit(d);	// ΠΡΩΙΜΟΣ ΕΛΕΓΧΟΣ
					d=(~d);							//συμπληρώνεται το d
					lineUsage[x].poss          &=d;	// ΕΝΗΜΕΡΩΝΩ ΤΙΣ ΓΡΑΜΜΕΣ/ΣΤΗΛΕΣ/ΤΕΤΡΑΓΩΝΑ
					columnUsage[y].poss        &=d;	// ΓΙΑ ΤΟ ΚΕΛΙ ΠΟΥ ΜΟΛΙΣ ΒΡΗΚΑ.
					squareUsage[Cell.sqr(x,y)].poss &=d;// ΔΗΛΑΔΗ ΝΑ ΣΒΗΣΟΥΝ ΤΗΝ ΤΙΜΗ ΑΥΤΗ ΑΠΟ ΤΙΣ ΠΙΘΑΝΕΣ ΤΟΥΣ
					cellPossibleUpdate();
					digitsCount++;
				}
			}
		}
		return digitsCount;
	}
	private  int uniqDigit(int f	) {
		int i;
		for (i=1 ; i<10 ; ++i)
		{
			if ( ((1<<i)&f) != 0 )
				return i;
		}
		return 0;
	}
	private  int possibleCount(int f) {
		int i;
		int x=0;
		for (i=1 ; i<10 ; ++i)
		{
			if ( ((1<<i)&f) != 0 )
				x++;
		}
		return x;
	}
	private void cellPossibleUpdate() {
		int x,y;
		for(x=1;x<10;x++){
			for (y=1;y<10;y++){
				SDK[x][y].poss &= lineUsage[x].poss & columnUsage[y].poss & squareUsage[Cell.sqr(x,y)].poss;
			}
		}

	}
}


class Usage {
	int poss;
	boolean filled;	

	public Usage() {
		poss = Cell.ALL_POSSIBLE;
		filled = false;
	}
}

class CellComparator implements Comparator<Object> {
	public int compare(Object keli1, Object keli2) {
		return possibleCount((Integer)keli1) - possibleCount((Integer)keli2);
	}
	private int possibleCount(Integer f) {
		int i;
		int x=0;
		for (i=1 ; i<10 ; i++){
			if ( ((1<<i)&f) != 0 ) x++;
		}
		return x;
	}
}

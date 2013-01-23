package sudoku;
import java.util.Vector;


public class Cell {
	
		public static final int ALL_POSSIBLE  = 0x3FE; 	//bits: 0011 1111 1110
		public static final int NONE_POSSIBLE = 0x0;	//bits: 0000 0000 0000
		public static final int EMPTY 		  = 0x0;	//bits: 0000 0000 0000
		public static final int FULL 		  = 0x3FE; 	//bits: 0011 1111 1110
		
		public int value;
		public int poss;
		public boolean filled;
		public boolean conflict;
	
	public Cell(){
		this.value  = 0;
		this.poss   = ALL_POSSIBLE;
		this.filled = false;
		this.conflict = false;
	}
	
	public Cell(int val){
		this.value  = val;
		this.poss   = NONE_POSSIBLE;
		this.filled = true;
		this.conflict = false;
	}
	
	public Vector<Integer> getPossibleValueArray()  {
		Vector<Integer> v = new Vector<Integer>();
		for (int i=1;i<10;i++){
			if ((1<<i & this.poss)!=0) v.add(i);
		}
		return v;
	}
	
	
	public static Cell[][] makePuzzle() {
		return resetPuzzle( new Cell[10][10]);
		
	}
	
	static int sqr   (int x,int y)			{return (1+(y-1)/3 )+ (((x-1)/3)*3);}
	
	static int sthlh (int square, int cell){return (1+(cell-1)%3) + (3*((square-1)%3));}

	static int grammh(int square, int cell){return (1+3*((square-1)/3)) + (((cell-1)/3));}

	static Cell[][] resetPuzzle(Cell SDK[][]){
		for (int x=0;x<10;x++){
			for (int y=0;y<10;y++){
				SDK[x][y] = new Cell();
			}
		}
		
		return SDK;
	}

}

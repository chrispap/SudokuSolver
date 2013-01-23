package sudoku;

public class SudokuSolverApp {

	public static void main(String[] args) {
		
		Cell[][] sdkPuzzle = Cell.makePuzzle();
		
		Solver sdkSolver = new Solver(sdkPuzzle);
		
		new Gui(sdkPuzzle, sdkSolver);
		
	}
	
}
